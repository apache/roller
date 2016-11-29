tightblogApp.controller('EntryEditController', ['$http', function EntryEditController($http) {
    var self = this;
    this.recentEntries = {};
    this.urlRoot = contextPath + '/tb-ui/authoring/rest/weblogentries/';

    this.getRecentEntries = function(entryType) {
        $http.get(this.urlRoot + weblogId + '/recententries/' + entryType).then(
          function(response) {
             self.recentEntries[entryType] = response.data;
          }
        )
    }

    this.getRecentEntries('DRAFT');
    this.getRecentEntries('PUBLISHED');
    this.getRecentEntries('SCHEDULED');
    this.getRecentEntries('PENDING');
}]);
