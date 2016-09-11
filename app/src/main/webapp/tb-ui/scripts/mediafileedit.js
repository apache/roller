$(document).ready(function() {
    $("input[type='file']").change(function() {
        var name = '';
        var fileControls = $("input[type='file']");
        for (var i=0; i<fileControls.size(); i++) {
            if (jQuery.trim(fileControls.get(i).value).length > 0) {
                name = fileControls.get(i).value;
            }
        }
//        $("#entry_bean_name").get(0).disabled = false;
//        $("#entry_bean_name").get(0).value = name;
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
    this.uploadFileToUrl = function(file, mediaData, uploadUrl){
        var fd = new FormData();
        if (file) {
            fd.append('uploadFile', file);
        }
        fd.append('mediaFileData', new Blob([JSON.stringify(mediaData)], {type: "application/json"}));
        $http.post(uploadUrl, fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        })
        .success(function(){
        })
        .error(function(){
        });
    }
}]);

mediaFileEditApp.controller('MediaFileEditController', ['$http', 'fileUpload', function MediaFileEditController($http,
fileUpload) {
    var self = this;

    this.loadMediaDirectories = function() {
        $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/mediadirectories').then(function(response) {
            self.mediaDirectories = response.data;
        });
    }

    this.loadMediaFile = function() {
        $http.get(contextPath + '/tb-ui/authoring/rest/mediafile/' + mediaFileId).then(function(response) {
            self.mediaFileData = response.data;
        });
    };
    this.loadMediaDirectories();

    if (mediaFileId) {
        this.loadMediaFile();
    }

    this.saveMediaFile = function(){
        var uploadUrl = contextPath;
        if (mediaFileId) {
          uploadUrl += '/tb-ui/authoring/rest/mediafile/' + mediaFileId;
        } else {
          uploadUrl += '/tb-ui/authoring/rest/mediadirectory/' + directoryId + '/files';
        }
        fileUpload.uploadFileToUrl(self.myMediaFile, self.mediaFileData, uploadUrl);
    };

}]);
