$(function() {
    $('#resignWeblogModal').on('show.bs.modal', function(e) {
        //get data-id attribute of the clicked element
        var weblogName = $(e.relatedTarget).attr('data-weblog-name');
        var userRoleId = $(e.relatedTarget).attr('data-userrole-id');

        // populate delete modal with tag-specific information
        var modal = $(this)
        var tmpl = eval('`' + msg.confirmResignationTmpl + '`')
        modal.find('#resignWeblogMsg').html(tmpl);
        modal.find('button[id="resignButton"]').attr("data-userrole-id", userRoleId);
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

    this.resignWeblog = function(obj) {
        $('#resignWeblogModal').modal('hide');

        // https://stackoverflow.com/a/18030442/1207540
        var userRoleId = obj.target.getAttribute("data-userrole-id");

        $http.post(contextPath + '/tb-ui/authoring/rest/weblogrole/' + userRoleId + '/detach').then(
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
