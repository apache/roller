$(function() {
    $('#deleteFilesModal').on('show.bs.modal', function(e) {
        var count = $('input[name="idSelections"]:checked').size();

        // populate delete modal with tag-specific information
        var modal = $(this)
        var tmpl = eval('`' + msg.confirmDeleteFilesTmpl + '`')
        modal.find('#deleteFilesMsg').html(tmpl);
    });

    $('#deleteFolderModal').on('show.bs.modal', function(e) {
        // get data-id attribute of the clicked element
        var folderId = $(e.relatedTarget).attr('data-folder-id');
        var folderName = angular.element('#ngapp-div').scope().ctrl.getFolderName(folderId);
        var count = angular.element('#ngapp-div').scope().ctrl.mediaFiles.length;

        // populate delete modal with tag-specific information
        var modal = $(this)
        var tmpl = eval('`' + msg.confirmDeleteFolderTmpl + '`')
        modal.find('#deleteFolderMsg').html(tmpl);
    });

    $('#moveFilesModal').on('show.bs.modal', function(e) {
        // get data-id attribute of the clicked element
        var targetFolderId = $(e.relatedTarget).attr('data-folder-id');
        var targetFolderName = angular.element('#ngapp-div').scope().ctrl.getFolderName(targetFolderId);
        var count = $('input[name="idSelections"]:checked').size();

        // populate delete modal with tag-specific information
        var modal = $(this)
        var tmpl = eval('`' + msg.confirmMoveFilesTmpl + '`')
        modal.find('#moveFilesMsg').html(tmpl);
    });
});

tightblogApp.controller('PageController', ['$http', function PageController($http) {
    var self = this;
    this.successMessage = null;
    this.errorMessage = null;
    this.mediaDirectories = null;

    this.loadMediaDirectories = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/mediadirectories').then(function(response) {
        self.mediaDirectories = response.data;
        if (self.mediaDirectories && self.mediaDirectories.length > 0) {
            if (!self.currentFolderId) {
                self.currentFolderId = directoryId ? directoryId : self.mediaDirectories[0].id;
            }
            self.loadMediaFiles();
        }
      });
    }

    this.getFolderName = function(folderId) {
        for (i = 0; self.mediaDirectories && i < self.mediaDirectories.length; i++) {
           if (self.mediaDirectories[i].id == folderId) {
               return self.mediaDirectories[i].name;
           }
        }
        return null;
    }

    this.filesSelected = function() {
        return $('input[name="idSelections"]:checked').size() > 0;
    }

    this.loadMediaFiles = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/mediadirectories/' + self.currentFolderId + '/files').then(function(response) {
        self.mediaFiles = response.data;
      });
    };
    this.loadMediaDirectories();

    var allFilesSelected = false;
    this.onToggle = function() {
        allFilesSelected = !allFilesSelected;
        angular.forEach(this.mediaFiles, function(mediaFile) {
          mediaFile.selected = allFilesSelected;
        })
    }

    this.addFolder = function() {
      if (!self.newFolderName) {
        return;
      }
      this.messageClear();
      $http.put(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/mediadirectories',
         JSON.stringify(self.newFolderName)).then(
        function(response) {
          self.currentFolderId = response.data;
          self.newFolderName = '';
          self.loadMediaDirectories();
        },
        self.commonErrorResponse
      )
    }

    this.deleteFolder = function() {
      this.messageClear();
      $('#deleteFolderModal').modal('hide');
      $http.delete(contextPath + '/tb-ui/authoring/rest/mediadirectory/' + self.currentFolderId).then(
        function(response) {
          self.successMessage = msg.folderDeleteSuccess;
          self.currentFolderId = null;
          self.loadMediaDirectories();
        },
        self.commonErrorResponse
      )
    }

    this.deleteFiles = function() {
      this.messageClear();
      $('#deleteFilesModal').modal('hide');
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
      $('#moveFilesModal').modal('hide');
      var selectedFiles = [];
      angular.forEach(this.mediaFiles, function(mediaFile) {
          if (!!mediaFile.selected) selectedFiles.push(mediaFile.id);
      })

      $http.post(contextPath + '/tb-ui/authoring/rest/mediafiles/weblog/' + weblogId +
      "/todirectory/" + self.targetFolderId,
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
