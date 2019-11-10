tightblogApp.controller('PageController', ['$http', function PageController($http) {

    var self = this;
    this.errorObj = {};

    this.updateRoles = function() {
      self.messageClear();
      $http.post(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/memberupdate', JSON.stringify(self.roles)).then(
        function(response) {
          self.successMessage = response.data;
          self.loadMembers();
        },
          self.commonErrorResponse
        )
    }

    this.addUserToWeblog = function() {
      self.messageClear();
      if (!self.userToAdd || !self.userToAddRole) {
        return;
      }
      $http.post(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/user/' + self.userToAdd +
        '/role/' + self.userToAddRole + '/attach').then(
        function(response) {
          self.successMessage = response.data;
          self.userToAdd = '';
          self.userToAddRole = '';
          self.loadMembers();
        },
            self.commonErrorResponse
        )
    }

    this.loadPotentialMembers = function() {
      self.userToAdd = null;
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/potentialmembers').then(function(response) {
        self.potentialMembers = response.data;
        if (Object.keys(self.potentialMembers).length > 0) {
          for (first in self.potentialMembers) {
             self.userToAdd = first;
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

    this.messageClear = function() {
        this.successMessage = null;
        this.errorObj = {};
    }

    this.commonErrorResponse = function(response) {
        if (response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));
        } else {
           self.errorObj = response.data;
        }
    }

  }]);
