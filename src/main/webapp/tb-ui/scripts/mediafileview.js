$(function() {
    $("#confirm-delete-files").dialog({
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

tightblogApp.controller('PageController', ['$http', function PageController($http) {
    var self = this;
    this.successMessage = null;
    this.errorMessage = null;

    this.loadMediaDirectories = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/mediadirectories').then(function(response) {
        self.mediaDirectories = response.data;
        if (self.mediaDirectories && self.mediaDirectories.length > 0) {
          if (directoryId) {
            self.directoryToView = directoryId;
            self.directoryToMoveTo = directoryId;
          } else {
            self.directoryToView = self.mediaDirectories[0].id;
            self.directoryToMoveTo = self.mediaDirectories[0].id;
          }
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
      this.messageClear();
      $http.put(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/mediadirectories',
         JSON.stringify(self.newDirectoryName)).then(
        function(response) {
          self.loadMediaDirectories();
          self.directoryToView = response.data;
          self.newDirectoryName = '';
        },
        self.commonErrorResponse
      )
    }

    this.deleteFolder = function() {
      this.messageClear();
      $http.delete(contextPath + '/tb-ui/authoring/rest/mediadirectory/' + self.directoryToView).then(
        function(response) {
          self.successMessage = msg.folderDeleteSuccess;
          self.loadMediaDirectories();
        },
        self.commonErrorResponse
      )
    }

    this.deleteFiles = function() {
      this.messageClear();
      var selectedFiles = [];
      angular.forEach(this.mediaFiles, function(mediaFile) {
          if (!!mediaFile.selected) selectedFiles.push(mediaFile.id);
      })

      $http.post(contextPath + '/tb-ui/authoring/rest/mediafiles/weblog/' + weblogId,
      JSON.stringify(selectedFiles)).then(
        function(response) {
          self.successMessage = msg.fileDeleteSuccess;
          self.loadMediaFiles();
        },
        self.commonErrorResponse
      )
    }

    this.moveFiles = function() {
      this.messageClear();
      var selectedFiles = [];
      angular.forEach(this.mediaFiles, function(mediaFile) {
          if (!!mediaFile.selected) selectedFiles.push(mediaFile.id);
      })

      $http.post(contextPath + '/tb-ui/authoring/rest/mediafiles/weblog/' + weblogId +
      "/todirectory/" + self.directoryToMoveTo,
      JSON.stringify(selectedFiles)).then(
        function(response) {
          self.successMessage = msg.fileMoveSuccess;
          self.loadMediaFiles();
        },
        function(response) {
         if (response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));  // return;
         } else {
           self.errorMessage = msg.fileMoveError;
         }
        })
    }

    this.commonErrorResponse = function(response) {
         if (response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));  // return;
         } else if (response.status == 400) {
           self.errorMessage = response.data;
         }
    }

    this.messageClear = function() {
        this.successMessage = null;
        this.errorMessage = null;
    }

}]);

function showDialog(dialogId) {
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            elem.bind('click', function(e) {
                $(dialogId).dialog('open');
            });
        }
    };
}

tightblogApp.directive('moveFilesDialog', function(){return showDialog('#confirm-move-file')});

tightblogApp.directive('deleteFilesDialog', function(){return showDialog('#confirm-delete-files')});

tightblogApp.directive('deleteFolderDialog', function(){return showDialog('#confirm-delete-folder')});
