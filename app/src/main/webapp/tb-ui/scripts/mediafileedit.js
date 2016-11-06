$(document).ready(function() {
    $("input[type='file']").change(function() {
        var name = '';
        var fileControls = $("input[type='file']");
        for (var i=0; i<fileControls.size(); i++) {
            if (jQuery.trim(fileControls.get(i).value).length > 0) {
                name = fileControls.get(i).value;
            }
        }
    });

    // do not send empty file fields
    $('#entry').submit(function() {
      var fileVal = $('#fileControl').val();
      if (fileVal === undefined || fileVal === "") {
         $('#fileControl').removeAttr('name');
      }
    });
    $('#cancelbtn').click(function() {
      $('#entry').attr('novalidate','');
    });
});

function getFileName(fullName) {
   var backslashIndex = fullName.lastIndexOf('/');
   var fwdslashIndex = fullName.lastIndexOf('\\');
   var fileName;
   if (backslashIndex >= 0) {
       fileName = fullName.substring(backslashIndex + 1);
   } else if (fwdslashIndex >= 0) {
       fileName = fullName.substring(fwdslashIndex + 1);
   }
   else {
       fileName = fullName;
   }
   return fileName;
}

var mediaFileEditApp = angular.module('mediaFileEditApp', []);

mediaFileEditApp.config(['$httpProvider', function($httpProvider) {
    $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
    var header = $("meta[name='_csrf_header']").attr("content");
    var token = $("meta[name='_csrf']").attr("content");
    $httpProvider.defaults.headers.delete = {};
    $httpProvider.defaults.headers.delete[header] = token;
    $httpProvider.defaults.headers.post[header] = token;
    $httpProvider.defaults.headers.put[header] = token;
}]);

mediaFileEditApp.directive('fileModel', ['$parse', function ($parse) {
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

mediaFileEditApp.service('fileUpload', ['$http', function ($http) {
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

mediaFileEditApp.controller('MediaFileEditController', ['$http', 'fileUpload', function MediaFileEditController($http, fileUpload) {
    var self = this;
    var mediaFileData = {};
    var errorMsg = null;

    this.loadMediaFile = function() {
        $http.get(contextPath + '/tb-ui/authoring/rest/mediafile/' + mediaFileId).then(function(response) {
            self.mediaFileData = response.data;
        });
    };

    if (mediaFileId) {
        this.loadMediaFile();
    } else {
        this.mediaFileData = { directory : {"id": directoryId} };
    }

    this.saveMediaFile = function() {
        var uploadUrl = contextPath + '/tb-ui/authoring/rest/mediafiles';
        fileUpload.uploadFileToUrl(self.myMediaFile, self.mediaFileData, uploadUrl)
        .success(function(){
            window.location.replace(mediaViewUrl + '&directoryId=' + self.mediaFileData.directory.id);
        })
        .error(function(response, status) {
             if (status == 408) {
               self.errorMsg = null;
               window.location.replace(mediaViewUrl + '&directoryId=' + self.mediaFileData.directory.id);  // return;
             } else if (status == 400) {
               self.errorMsg = response;
             }
        });

    };

}]);
