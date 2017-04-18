tightblogApp.controller('PageController', ['$http', '$window', function PageController($http, $window) {
    var self = this;

    this.loadDirectories = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + actionWeblogId + '/mediadirectories').then(
         function(response) {
            self.directories = response.data;
         },
         self.commonErrorResponse
      );
    };

    this.loadImages = function() {
      if (this.selectedDirectory) {
          $http.get(contextPath + '/tb-ui/authoring/rest/mediadirectories/' + this.selectedDirectory + '/files').then(
             function(response) {
                self.images = response.data;
             },
             self.commonErrorResponse
          );
      }
    };

    this.commonErrorResponse = function(response) {
        if (response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));
        }
    }

    this.chooseFile = function(item) {
        $window.parent.onSelectMediaFile(item.name, item.permalink, item.altText, item.titleText, item.anchor, item.imageFile);
    }

    this.loadDirectories();

}]);
