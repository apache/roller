tightblogApp.controller('PageController', ['$http', function PageController($http) {
    var self = this;
    this.urlRoot = contextPath + '/tb-ui/admin/rest/server/';
    this.metadata = {};
    this.weblogToReindex = null;
    this.successMessage = null;
    this.errorMessage = null;

    this.loadMetadata = function() {
        $http.get(this.urlRoot + 'webloglist').then(
          function(response) {
            self.metadata.weblogList = response.data;
            if (self.metadata.weblogList && self.metadata.weblogList.length > 0) {
                self.weblogToReindex = self.metadata.weblogList[0];
            }
          },
          self.commonErrorResponse
        )
    };

    this.loadItems = function() {
        $http.get(this.urlRoot + "caches").then(
          function(response) {
            self.cacheData = response.data;
          },
          self.commonErrorResponse
        )
    };

    this.clearCache = function(cacheItem) {
        this.messageClear();
        $http.post(this.urlRoot + 'cache/' + cacheItem + '/clear').then(
          function(response) {
             self.successMessage = response.data;
             self.loadItems();
          },
          self.commonErrorResponse
        )
    }

    this.resetHitCounts = function() {
        this.messageClear();
        $http.post(this.urlRoot + 'resethitcount').then(
          function(response) {
             self.successMessage = response.data;
          },
          self.commonErrorResponse
        )
    }

    this.reindexWeblog = function() {
        this.messageClear();
        if (self.weblogToReindex) {
            $http.post(this.urlRoot + 'weblog/' + self.weblogToReindex + '/rebuildindex').then(
              function(response) {
                 self.successMessage = response.data;
              },
              self.commonErrorResponse
            )
        }
    }

    this.commonErrorResponse = function(response) {
        if (response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));
        } else if (response.status == 400) {
           self.errorMessage = response.data;
        }
    }

    this.messageClear = function() {
        this.successMessage = null;
        this.errorMessage = null;
    }

    this.loadMetadata();
    this.loadItems();
}]);
