$(function() {
   $("#confirm-clearall").dialog({
      autoOpen: false,
      resizable: true,
      height: 200,
      modal: true,
      buttons: [
         {
            text: msg.confirmLabel,
            click: function() {
                angular.element('#ngapp-div').scope().ctrl.clearAllCaches();
                angular.element('#ngapp-div').scope().$apply();
                $( this ).dialog( "close" );
            }
         },
         {
            text: msg.cancelLabel,
            click: function() {
               $(this).dialog("close");
            }
         }
      ]
   });
});

tightblogApp.controller('AppController', ['$http', function AppController($http) {
    var self = this;
    this.urlRoot = contextPath + '/tb-ui/admin/rest/server/';
    this.metadata = {};
    this.weblogToReindex = null;
    this.resultsMessage = null;

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
        $http.post(this.urlRoot + 'cache/' + cacheItem + '/clear').then(
          function(response) {
             self.loadItems();
          },
          self.commonErrorResponse
        )
    }

    this.clearAllCaches = function() {
        $http.post(this.urlRoot + 'caches/clear').then(
          function(response) {
             self.loadItems();
          },
          self.commonErrorResponse
        )
    }

    this.resetHitCounts = function() {
        $http.post(this.urlRoot + 'resethitcount').then(
          function(response) {
             self.resultsMessage = response.data;
          },
          self.commonErrorResponse
        )
    }

    this.reindexWeblog = function() {
        if (self.weblogToReindex) {
            $http.post(this.urlRoot + 'weblog/' + self.weblogToReindex + '/rebuildindex').then(
              function(response) {
                 self.resultsMessage = response.data;
              },
              self.commonErrorResponse
            )
        }
    }

    this.commonErrorResponse = function(response) {
        if (response.status == 408)
           window.location.replace($('#refreshURL').attr('value'));
        if (response.status == 400) {
           self.resultsMessage = response.data;
        }
    }

    this.loadMetadata();
    this.loadItems();
}]);

tightblogApp.directive('confirmClearAllDialog', function(){
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            var dialogId = '#' + attr.confirmClearAllDialog;
            elem.bind('click', function(e) {
                $(dialogId).dialog('open');
            });
        }
    };
});
