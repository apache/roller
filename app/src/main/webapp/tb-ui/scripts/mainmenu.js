$(function() {
  $("#confirm-resign").dialog({
    autoOpen: false,
    resizable: true,
    height:170,
    modal: true,
    buttons: [
      {
        text: msg.yesLabel,
        click: function() {
          angular.element('#ngapp-div').scope().ctrl.updateRole(encodeURIComponent($(this).data('roleId')), 'detach');
          angular.element('#ngapp-div').scope().$apply();
          $(this).dialog("close");
        },
      },
      {
        text: msg.noLabel,
        click: function() {
          $(this).dialog("close");
        }
      }
    ]
  });
});

tightblogApp.controller('PageController', ['$http', function PageController($http) {
    var self = this;

    this.acceptBlog = function(role) {
        this.updateRole(role.id, 'attach');
    }

    this.declineBlog = function(role) {
        this.updateRole(role.id, 'detach');
    }

    this.updateRole = function(roleId, command) {
        $http.post(contextPath + '/tb-ui/authoring/rest/weblogrole/' + roleId + '/' + command).then(
          function(response) {
             self.loadItems();
          },
          self.commonErrorResponse
        )
    }

    this.loadItems = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/loggedinuser/weblogs').then(function(response) {
        self.roles = response.data;
      });
    };

    this.loadItems();
  }]);

tightblogApp.directive('confirmResignDialog', function(){
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            var dialogId = '#' + attr.confirmResignDialog;
            elem.bind('click', function(e) {
                $(dialogId).data('roleId',  attr.roleId)
                    .dialog("option", {"title" : attr.weblogName})
                    .dialog('open');
            });
        }
    };
});
