tightblogApp.requires.push('ngSanitize');

tightblogApp.controller('PageController', ['$http', '$interpolate', '$sce', '$filter',
    function PageController($http, $interpolate, $sce, $filter) {
        var self = this;
        this.weblog = {
           "theme" : "rolling",
           "locale" : "en",
           "timeZone" : "America/New_York",
           "editFormat" : "HTML",
           "allowComments" : "NONE",
           "spamPolicy"    : "NO_EMAIL",
           "visible" : true,
           "entriesPerPage" : 12,
           "defaultCommentDays" : -1
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
                  window.scrollTo(0, 0);
              },
              function(response) {
                if (response.status == 400) {
                   self.errorObj = response.data;
                   window.scrollTo(0, 0);
                } else {
                   self.commonErrorResponse(response);
                }
              })
        }

        this.deleteWeblog = function() {
            $('#deleteWeblogModal').modal('hide');

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
            } else {
               self.errorMessage = response.data;
               window.scrollTo(0, 0);
            }
        }

        this.cancelChanges = function() {
            this.messageClear();
            if (weblogId) {
                this.loadWeblog();
                window.scrollTo(0, 0);
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
