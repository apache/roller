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
          angular.element('#ngapp-div').scope().ctrl.resignFromBlog(encodeURIComponent($(this).data('roleId')));
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

tightblogApp.controller('PageController', ['$http', '$interpolate', '$sce',
        function PageController($http, $interpolate, $sce) {
    var self = this;

    this.toggleEmails = function(role) {
        $http.post(contextPath + '/tb-ui/authoring/rest/weblogrole/' + role.id + '/emails/' + role.emailComments).then(
          function(response) {
          },
          self.commonErrorResponse
        )
    }

    this.getUnapprovedCommentsString = function(commentCount) {
        return $sce.trustAsHtml($interpolate(msg.unapprovedCommentsTmpl)
                                   ({unapprovedCommentCount:commentCount}));
    }

    this.resignFromBlog = function(roleId) {
        $http.post(contextPath + '/tb-ui/authoring/rest/weblogrole/' + roleId + '/detach').then(
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
