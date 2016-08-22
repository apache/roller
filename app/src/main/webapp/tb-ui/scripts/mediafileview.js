$(function() {
    $(".delete-file-link").click(function(e) {
      e.preventDefault();
      $('#confirm-delete-file').dialog('open');
    });
    $(".delete-folder-link").click(function(e) {
      e.preventDefault();
      $('#confirm-delete-folder').dialog('open');
    });
    $(".move-file-link").click(function(e) {
      e.preventDefault();
      $('#confirm-move-file').dialog('open');
    });

    $("#confirm-delete-file").dialog({
      autoOpen: false,
      resizable: true,
      height:200,
      modal: true,
      buttons: [
        {
          text: msg.deleteLabel,
          click: function() {
            angular.element('#ngapp-div').scope().ctrl.deleteFiles();
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
    $("#confirm-delete-folder").dialog({
      autoOpen: false,
      resizable: true,
      height:200,
      modal: true,
      buttons: [
        {
          text: msg.deleteLabel,
          click: function() {
            angular.element('#ngapp-div').scope().ctrl.deleteFolder();
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
    $("#confirm-move-file").dialog({
      autoOpen: false,
      resizable: true,
      height:200,
      modal: true,
      buttons: [
        {
          text: msg.confirmLabel,
          click: function() {
            angular.element('#ngapp-div').scope().ctrl.moveFiles();
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

var mediaFileViewApp = angular.module('mediaFileViewApp', []);

mediaFileViewApp.config(['$httpProvider', function($httpProvider) {
    $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
    var header = $("meta[name='_csrf_header']").attr("content");
    var token = $("meta[name='_csrf']").attr("content");
    $httpProvider.defaults.headers.delete = {};
    $httpProvider.defaults.headers.delete[header] = token;
    $httpProvider.defaults.headers.post[header] = token;
    $httpProvider.defaults.headers.put[header] = token;
}]);

mediaFileViewApp.controller('MediaFileViewController', ['$http', function MediaFileViewController($http) {
    var self = this;

    this.loadMediaDirectories = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/mediadirectories').then(function(response) {
        self.mediaDirectories = response.data;
        if (self.mediaDirectories && self.mediaDirectories.length > 0) {
          self.directoryToView = self.mediaDirectories[0].id;
          self.directoryToMoveTo = self.mediaDirectories[0].id;
          self.loadMediaFiles();
        }
      });
    }

    this.loadMediaFiles = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/mediadirectories/' + self.directoryToView + '/files').then(function(response) {
        self.mediaFiles = response.data;
      });
    };
    this.loadMediaDirectories();

    var toggleState = 'Off'

    this.onToggle = function() {
        if (toggleState == 'Off') {
            toggleState = 'On';
            angular.forEach(this.mediaFiles, function(mediaFile) {
              mediaFile.selected = true;
            })
        } else {
            toggleState = 'Off';
            angular.forEach(this.mediaFiles, function(mediaFile) {
              mediaFile.selected = false;
            })
        }
    }

    this.createNewDirectory = function() {
      if (!self.newDirectoryName) {
        return;
      }
      $http.put(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/mediadirectories',
         JSON.stringify(self.newDirectoryName)).then(
        function(response) {
          $('#errorMessageDiv').hide();
          $('#successMessageDiv').show();
          self.loadMediaDirectories();
          self.directoryToView = response.data;
          self.newDirectoryName = '';
        },
        self.commonErrorResponse
      )
    }

    this.deleteFolder = function() {
      $http.delete(contextPath + '/tb-ui/authoring/rest/mediadirectory/' + self.directoryToView).then(
        function(response) {
          $('#errorMessageDiv').hide();
          $('#successMessageDiv').show();
          self.loadMediaDirectories();
        },
        self.commonErrorResponse
      )
    }

    this.deleteFiles = function() {
      var selectedFiles = [];
      angular.forEach(this.mediaFiles, function(mediaFile) {
          if (!!mediaFile.selected) selectedFiles.push(mediaFile.id);
      })

      $http.post(contextPath + '/tb-ui/authoring/rest/mediafiles/weblog/' + weblogId,
      JSON.stringify(selectedFiles)).then(
        function(response) {
          $('#errorMessageDiv').hide();
          $('#successMessageDiv').show();
          self.loadMediaFiles();
        },
        self.commonErrorResponse
      )
    }

    this.moveFiles = function() {
      var selectedFiles = [];
      angular.forEach(this.mediaFiles, function(mediaFile) {
          if (!!mediaFile.selected) selectedFiles.push(mediaFile.id);
      })

      $http.post(contextPath + '/tb-ui/authoring/rest/mediafiles/weblog/' + weblogId +
      "/todirectory/" + self.directoryToMoveTo,
      JSON.stringify(selectedFiles)).then(
        function(response) {
          $('#errorMessageDiv').hide();
          $('#successMessageDiv').show();
          self.loadMediaFiles();
        },
        self.commonErrorResponse
      )
    }

    this.commonErrorResponse = function(response) {
         if (response.status == 408)
           window.location.replace($('#refreshURL').attr('value'));  // return;
         if (response.status == 400) {
           self.errorObj = response.data;
           $('#successMessageDiv').hide();
           $('#errorMessageDiv').show();
         }
    }

}]);
