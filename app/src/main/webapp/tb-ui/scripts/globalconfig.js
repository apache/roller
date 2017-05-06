tightblogApp.controller('PageController', ['$http',
    function PageController($http) {
        var self = this;
        this.urlRoot = contextPath + '/tb-ui/admin/rest/server/';
        this.webloggerProps = {};

        this.loadMetadata = function() {
            $http.get(this.urlRoot + 'globalconfigmetadata').then(
            function(response) {
                self.metadata = response.data;
              },
              self.commonErrorResponse
            )
        };

        this.loadWebloggerProperties = function() {
            $http.get(this.urlRoot + 'webloggerproperties').then(
              function(response) {
                 self.webloggerProps = response.data;
              }
            )
        };

        this.updateProperties = function(saveType) {
            // if no main blog chosen, set it to null
            if (self.webloggerProps.mainBlog && !self.webloggerProps.mainBlog.id) {
                self.webloggerProps.mainBlog = null;
            }

            $http.post(self.urlRoot + 'webloggerproperties', JSON.stringify(self.webloggerProps)).then(
              function(response) {
                self.errorObj = {};
                self.saveResponseMessage = response.data;
                self.loadWebloggerProperties();
              },
             function(response) {
               if (response.status == 408)
                 window.location.replace($('#refreshURL').attr('value'));
               if (response.status == 400) {
                 self.errorObj = response.data;
               }
            })
        };

        this.loadMetadata();
        this.loadWebloggerProperties();
    }]
);
