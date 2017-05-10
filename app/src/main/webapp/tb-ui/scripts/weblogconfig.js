$(function() {
  $("#confirm-delete").dialog({
    autoOpen: false,
    resizable: true,
    height:310,
    modal: true,
    buttons: [
       {
          text: msg.deleteLabel,
          click: function() {
             angular.element('#ngapp-div').scope().ctrl.deleteWeblog();
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

tightblogApp.requires.push('ngSanitize');

tightblogApp.controller('PageController', ['$http', '$interpolate', '$sce', '$filter',
    function PageController($http, $interpolate, $sce, $filter) {
        var self = this;
        this.weblog = {
           "theme" : "basic",
           "locale" : "en",
           "timeZone" : "America/New_York",
           "editFormat" : "HTML",
           "allowComments" : "NONE",
           "emailComments" : false,
           "visible" : true,
           "entriesPerPage" : 12,
           "defaultCommentDays" : "-1"
        };
        this.errorObj = {};

        this.loadMetadata = function() {
            $http.get(contextPath + '/tb-ui/authoring/rest/weblogconfig/metadata').then(
            function(response) {
                self.metadata = response.data;
              },
              self.commonErrorResponse
            )
        };

        this.loadWeblog = function() {
            $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId).then(
              function(response) {
                 self.weblog = response.data;
                 self.deleteWeblogConfirmation =
                    $sce.trustAsHtml($interpolate(msg.deleteWeblogTmpl)({weblogHandle:self.weblog.handle}));
              }
            )
        }

        this.updateWeblog = function() {
            this.messageClear();
            var urlToUse = contextPath + (weblogId ? '/tb-ui/authoring/rest/weblog/' + weblogId
              : '/tb-ui/authoring/rest/weblogs');

            $http.post(urlToUse, JSON.stringify(this.weblog)).then(
              function(response) {
                  self.weblog = response.data;
                  if (!weblogId) {
                     window.location.replace(homeUrl);
                  }
                  self.showSuccessMessage = true;
              },
              function(response) {
                if (response.status == 400) {
                   self.errorObj = response.data;
                } else {
                   self.commonErrorResponse(response);
                }
              })
        }

        this.deleteWeblog = function() {
            $http.delete(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId).then(
              function(response) {
                 window.location.replace(homeUrl);
              },
              self.commonErrorResponse
            )
        }

        this.commonErrorResponse = function(response) {
            if (response.status == 408) {
               window.location.replace($('#refreshURL').attr('value'));
            } else if (response.status == 400) {
               self.errorMessage = response.data;
            }
        }

        this.cancelChanges = function() {
            this.messageClear();
            if (weblogId) {
                this.loadWeblog();
            } else {
               window.location.replace(homeUrl);
            }
            this.userBeingEdited = null;
            this.credentials = {};
        }

        this.messageClear = function() {
            this.errorObj = {};
            this.showSuccessMessage = false;
        }

        this.messageClear();
        this.loadMetadata();
        if (weblogId) {
            this.loadWeblog();
        }
    }]
);

tightblogApp.directive('confirmDeleteDialog', function() {
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            var dialogId = '#' + attr.confirmDeleteDialog;
            elem.bind('click', function(e) {
                $(dialogId).dialog('open');
            });
        }
    };
});
