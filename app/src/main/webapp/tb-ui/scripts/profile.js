tightblogApp.controller('PageController', ['$http',
    function PageController($http) {
        var self = this;
        this.userBeingEdited = {
           "locale" : "en"
        };
        this.userCredentials = {};
        this.ldapInvalid = false;
        this.errorObj = {};
        this.hideButtons = false;

        this.loadMetadata = function() {
            $http.get(contextPath + '/tb-ui/register/rest/useradminmetadata').then(
            function(response) {
                self.metadata = response.data;
              },
              self.commonErrorResponse
            )
        };

        this.loadUser = function() {
            $http.get(contextPath + '/tb-ui/authoring/rest/userprofile/' + userId).then(
              function(response) {
                 self.userBeingEdited = response.data;
                 self.userCredentials = {};
              }
            )
        }

        this.loadLDAPData = function() {
            $http.get(contextPath + '/tb-ui/register/rest/ldapdata').then(
              function(response) {
                 self.userBeingEdited = response.data;
                 self.userCredentials = {};
              },
              function(response, status) {
                 if (status = 404) {
                    self.ldapInvalid = true;
                    self.errorObj.errorMessage = ldapMissing;
                 } else {
                    commonErrorResponse(response);
                 }
              }
            )
        }

        this.updateUser = function() {
            this.messageClear();
            var userData = {};
            userData.user = this.userBeingEdited;
            userData.credentials = this.userCredentials;
            var urlToUse = contextPath + (userId ? '/tb-ui/authoring/rest/userprofile/' + userId
              : '/tb-ui/register/rest/registeruser');

            $http.post(urlToUse, JSON.stringify(userData)).then(
              function(response) {
                  self.userBeingEdited = response.data;
                  if (!userId) {
                     self.hideButtons = true;
                     userId = self.userBeingEdited.id;
                  }
                  self.userCredentials = {};
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

        this.cancelChanges = function() {
            this.messageClear();
            this.userBeingEdited = null;
            this.credentials = {};
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
            this.ldapInvalid = false;
            this.showSuccessMessage = false;
        }

        this.messageClear();
        this.loadMetadata();
        if (userId) {
            this.loadUser();
        } else if (authMethod == "LDAP") {
            this.loadLDAPData();
        }
    }]
);
