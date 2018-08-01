$(function() {
  $("#confirm-switch").dialog({
    autoOpen: false,
    resizable: true,
    height:310,
    modal: true,
    buttons: [
       {
          text: msg.confirmLabel,
          click: function() {
             angular.element('#ngapp-div').scope().ctrl.switchTheme();
             angular.element('#ngapp-div').scope().$apply();
             $( this ).dialog( "close" );
          }
       },
       {
          text: msg.cancelLabel,
          click: function() {
             $(this).dialog("close");
          }
       }
    ]
  });

});

tightblogApp.controller('PageController', ['$http',
    function PageController($http) {
        var self = this;
        this.errorObj = {};

        this.loadMetadata = function() {
            $http.get(contextPath + '/tb-ui/authoring/rest/weblogconfig/metadata').then(
            function(response) {
                self.metadata = response.data;
                delete self.metadata.sharedThemeMap[currentTheme];
                for (var props in self.metadata.sharedThemeMap) {
                    self.selectedTheme = props;
                    break;
                }
              },
              self.commonErrorResponse
            )
        };

        this.switchTheme = function() {
            this.messageClear();

            $http.post(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/switchtheme/' + this.selectedTheme).then(
              function(response) {
                  window.location.replace(templatePageUrl);
              },
              function(response) {
                if (response.status == 400) {
                   self.errorObj = response.data;
                } else {
                   self.commonErrorResponse(response);
                }
              }
            )
        }

        this.previewTheme = function() {
            window.open(contextPath  + '/tb-ui/authoring/preview/' + weblogHandle + '?theme=' + this.selectedTheme);
        }

        this.commonErrorResponse = function(response) {
            if (response.status == 408) {
               window.location.replace($('#refreshURL').attr('value'));
            } else if (response.status == 400) {
               self.errorMessage = response.data;
            }
        }

        this.messageClear = function() {
            this.errorObj = {};
        }

        this.loadMetadata();
    }
]);

tightblogApp.directive('confirmSwitchDialog', function() {
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            var dialogId = '#' + attr.confirmSwitchDialog;
            elem.bind('click', function(e) {
                $(dialogId).dialog('open');
            });
        }
    };
});
