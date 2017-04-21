tightblogApp.directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;

            element.bind('change', function(){
                scope.$apply(function(){
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);

tightblogApp.service('fileUpload', ['$http', function ($http) {
    var self = this;

    this.uploadFileToUrl = function(file, mediaData, uploadUrl){
        var fd = new FormData();
        if (file) {
            fd.append('uploadFile', file);
        }
        fd.append('mediaFileData', new Blob([JSON.stringify(mediaData)], {type: "application/json"}));
        return $http.post(uploadUrl, fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        })
    }
}]);

tightblogApp.controller('PageController', ['$http', 'fileUpload', function PageController($http, fileUpload) {
    var self = this;
    var mediaFileData = {};
    this.errorObj = null;

    this.loadMediaFile = function() {
        $http.get(contextPath + '/tb-ui/authoring/rest/mediafile/' + mediaFileId).then(function(response) {
            self.mediaFileData = response.data;
        });
    };

    this.saveMediaFile = function() {
        this.messageClear();
        var uploadUrl = contextPath + '/tb-ui/authoring/rest/mediafiles';
        fileUpload.uploadFileToUrl(self.myMediaFile, self.mediaFileData, uploadUrl).then(
            function() {
                window.location.replace(mediaViewUrl + '&directoryId=' + directoryId);
            },
            function(response) {
                if (response.status == 408) {
                    window.location.replace($('#refreshURL').attr('value'));
                } else if (response.status == 400) {
                    self.errorObj = response.data;
                }
            }
        );
    };

    this.messageClear = function() {
        this.errorObj = null;
    }

    if (mediaFileId) {
        this.loadMediaFile();
    } else {
        this.mediaFileData = { directory : {"id": directoryId} };
    }
}]);
