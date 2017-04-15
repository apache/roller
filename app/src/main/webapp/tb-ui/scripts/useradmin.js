tightblogApp.controller('PageController', ['$http',
    function PageController($http) {

        var self = this;
        this.urlRoot = contextPath + '/tb-ui/admin/rest/useradmin/';
        this.pendingList = {};
        this.userList = {};
        this.userToEdit = null;
        this.userBeingEdited = null;
        this.userCredentials = {};
        this.userBlogList = {};
        this.errorObj = {};

        this.loadMetadata = function() {
            $http.get(contextPath + '/tb-ui/register/rest/useradminmetadata').then(
            function(response) {
                self.metadata = response.data;
              },
              self.commonErrorResponse
            )
        };

        this.loadUserList = function() {
            $http.get(this.urlRoot + 'userlist').then(
            function(response) {
                self.userList = response.data;
                if (!self.userToEdit && Object.keys(self.userList).length > 0) {
                  for (first in self.userList) {
                     self.userToEdit = first;
                     break;
                  }
                }
              },
              self.commonErrorResponse
            )
        };

        this.getPendingRegistrations = function() {
            $http.get(this.urlRoot + 'registrationapproval').then(
              function(response) {
                 self.pendingList = response.data;
              }
            )
        }

        this.approveUser = function(userId) {
            this.processRegistration(userId, 'approve');
        }

        this.declineUser = function(userId) {
            this.processRegistration(userId, 'reject');
        }

        this.processRegistration = function(userId, command) {
            this.messageClear();
            $http.post(this.urlRoot + 'registrationapproval/' + userId + '/' + command).then(
                function(response) {
                   self.getPendingRegistrations();
                   self.loadUserList();
                },
                self.commonErrorResponse
            )
        }

        this.loadUser = function() {
            this.messageClear();
            $http.get(this.urlRoot + 'user/' + this.userToEdit).then(
              function(response) {
                 self.userBeingEdited = response.data;
                 self.userCredentials = {};
              }
            )

            $http.get(this.urlRoot + 'user/' + this.userToEdit + '/weblogs').then(
              function(response) {
                 self.userBlogList = response.data;
              }
            )
        }

        this.updateUser = function() {
            this.messageClear();
            var userData = {};
            userData.user = this.userBeingEdited;
            userData.credentials = this.userCredentials;

            $http.put(self.urlRoot + 'user/' + this.userBeingEdited.id, JSON.stringify(userData)).then(
              function(response) {
                  self.userBeingEdited = response.data;
                  self.userCredentials = {};
                  self.loadUserList();
                  self.getPendingRegistrations();
                  self.successMessage = "User [" + self.userBeingEdited.screenName + "] updated."
              },
              function(response) {
                if (response.status == 400) {
                   self.errorObj = response.data;
                } else {
                   self.commonErrorResponse;
                }
              })
        }

        this.cancelChanges = function() {
            this.messageClear();
            this.userBeingEdited = null;
            this.credentials = {};
        }

        this.commonErrorResponse = function(response) {
            if (response.status == 408) {
               window.location.replace($('#refreshURL').attr('value'));
            } else if (response.status == 400) {
               self.errorObj.errorMessage = response.data;
            }
        }

        this.messageClear = function() {
            this.successMessage = null;
            this.errorObj = {};
        }

        this.loadMetadata();
        this.getPendingRegistrations();
        this.loadUserList();
    }]
);
