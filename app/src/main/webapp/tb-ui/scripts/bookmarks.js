$(function() {
   $("#bookmark-edit").dialog({
      autoOpen: false,
      height: 270,
      width: 570,
      modal: true,
      buttons: [
         {
            text: msg.saveLabel,
            click: function() {
               var idToUpdate = $(this).data('bookmarkId');
               var newName = $('#bookmark-edit-name').val().trim();
               var newUrl = $('#bookmark-edit-url').val().trim();
               var newDescription = $('#bookmark-edit-description').val().trim();
               var newData = {
                  "name": newName,
                  "url": newUrl,
                  "description": newDescription
               };
               var dg = $(this);
               if (newName.length > 0 && newUrl.length > 0) {
                  $.ajax({
                     type: "PUT",
                     url: contextPath + ((idToUpdate == '') ? '/tb-ui/authoring/rest/bookmarks?weblogId=' + $("#actionWeblogId").val() : '/tb-ui/authoring/rest/bookmark/' + idToUpdate),
                     data: JSON.stringify(newData),
                     contentType: "application/json; charset=utf-8",
                     processData: "false",
                     success: function(data, textStatus, xhr) {
                       dg.dialog('close');
                       angular.element('#bookmark-list').scope().ctrl.loadBookmarks();
                       angular.element('#bookmark-list').scope().$apply();
                     },
                     error: function(xhr, status, errorThrown) {
                        if (xhr.status in this.statusCode)
                           return;
                        $('#bookmark-edit-error').css("display", "inline");
                     }
                  });
               }
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
   $("#confirm-delete").dialog({
      autoOpen: false,
      resizable: true,
      height: 200,
      modal: true,
      buttons: [
         {
            text: msg.confirmLabel,
            click: function() {
               var idsToRemove = [];
               $('input[name="selectedBookmarks"]:checked').each(function(){
                 idsToRemove.push($(this).val());
               });
               $(this).dialog("close");
               if (idsToRemove.length > 0) {
                 for (i = 0; i < idsToRemove.length; i++) {
                   $.ajax({
                      type: "DELETE",
                      url: contextPath + '/tb-ui/authoring/rest/bookmark/' + idsToRemove[i],
                      success: function(data, textStatus, xhr) {
                      }
                   });
                 }
                 angular.element('#bookmark-list').scope().ctrl.loadBookmarks();
                 angular.element('#bookmark-list').scope().$apply();
               }
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
   $("#tableBody").on('click', '.edit-link', function(e) {
      e.preventDefault();
      var tr = $(this).closest('tr');
      var idToEdit = tr.attr('id');
      $('#bookmark-edit').dialog('option', 'title', msg.editTitle)
      $('#bookmark-edit-name').val(tr.find('td.bookmark-name').html());
      $('#bookmark-edit-url').val(tr.find('td.bookmark-url').html());
      $('#bookmark-edit-description').val(tr.find('td.bookmark-description').html());
      $('#bookmark-edit-error').css("display", "none");
      checkLoggedIn(function() {
         $('#bookmark-edit').data('bookmarkId', idToEdit).dialog('open');
      });
   });
   $("#add-link").click(function(e) {
      e.preventDefault();
      $('#bookmark-edit').dialog('option', 'title', msg.addTitle)
      $('#bookmark-edit-name').val('');
      $('#bookmark-edit-url').val('');
      $('#bookmark-edit-description').val('');
      $('#bookmark-edit-error').css("display", "none");
      checkLoggedIn(function() {
         $('#bookmark-edit').data('bookmarkId', '').dialog('open');
      });
   });
   $(".control").on('click', '#delete-link', function(e) {
      e.preventDefault();
      $('#confirm-delete').dialog('open');
   });
});

var bookmarkApp = angular.module('bookmarkApp', []);

bookmarkApp.controller('BookmarkController', ['$http', function BookmarkController($http) {
    var self = this;
    this.loadBookmarks = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + $("#actionWeblogId").val() + '/bookmarks').then(function(response) {
        self.bookmarks = response.data;
      });
    };
    this.loadBookmarks();
  }]);
