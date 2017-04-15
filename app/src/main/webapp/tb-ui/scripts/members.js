tightblogApp.controller('PageController', ['$http', function PageController($http) {

    var self = this;

    this.updateRoles = function() {
      $http.post(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/memberupdate', JSON.stringify(self.roles)).then(
        function(response) {
          $('#errorMessageDiv').hide();
          $('#successMessageDiv').show();
          self.loadMembers();
        },
        function(response) {
         if (response.status == 408)
           window.location.replace($('#refreshURL').attr('value'));  // return;
         if (response.status == 400) {
           self.errorObj = response.data;
           $('#successMessageDiv').hide();
           $('#errorMessageDiv').show();
         }
      })
    }

    this.inviteUser = function() {
      if (!self.userToInvite || !self.inviteeRole) {
        return;
      }
      $http.post(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/user/' + self.userToInvite +
        '/role/' + self.inviteeRole + '/invite').then(
        function(response) {
          $('#errorMessageDiv').hide();
          $('#successMessageDiv').show();
          self.userToInvite = '';
          self.inviteeRole = '';
          self.loadMembers();
        },
        function(response) {
         if (response.status == 408)
           window.location.replace($('#refreshURL').attr('value'));  // return;
         if (response.status == 400) {
           self.errorObj = response.data;
           $('#successMessageDiv').hide();
           $('#errorMessageDiv').show();
         }
      })
    }

    this.loadPotentialMembers = function() {
      self.userToInvite = null;
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/potentialmembers').then(function(response) {
        self.potentialMembers = response.data;
        if (Object.keys(self.potentialMembers).length > 0) {
          for (first in self.potentialMembers) {
             self.userToInvite = first;
             break;
          }
        }
      });
    };

    this.loadMembers = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/members').then(function(response) {
        self.roles = response.data;
      });
      this.loadPotentialMembers();
    };
    this.loadMembers();

  }]);
