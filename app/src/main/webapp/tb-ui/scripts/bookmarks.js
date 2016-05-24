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
               if (newName.length > 0 && newUrl.length > 0) {
                  $.ajax({
                     type: "PUT",
                     url: contextPath + ((idToUpdate == '') ? '/tb-ui/authoring/rest/bookmarks?weblog=' + $("#actionWeblog").val() : '/tb-ui/authoring/rest/bookmark/' + idToUpdate),
                     data: JSON.stringify(newData),
                     contentType: "application/json; charset=utf-8",
                     processData: "false",
                     success: function(data, textStatus, xhr) {
                        if (idToUpdate == '') {
                           document.bookmarksForm.action = $("#add-link").attr("formaction");
                           document.bookmarksForm.submit();
                        } else {
                           $('#bkname-' + idToUpdate).text(newName.trim());
                           $('#bkurl-' + idToUpdate).text(newUrl.trim());
                           $('#bkdescription-' + idToUpdate).text(newDescription.trim());
                           $("#bookmark-edit").dialog().dialog("close");
                        }
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
               document.bookmarksForm.action = $("#delete-link").attr("formaction");
               document.bookmarksForm.submit();
               $(this).dialog("close");
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
   $(".edit-link").click(function(e) {
      e.preventDefault();
      $('#bookmark-edit').dialog('option', 'title', msg.editTitle)
      var idBeingUpdated = $(this).attr("data-id");
      $('#bookmark-edit-name').val($('#bkname-' + idBeingUpdated).text()).select();
      $('#bookmark-edit-url').val($('#bkurl-' + idBeingUpdated).text());
      $('#bookmark-edit-description').val($('#bkdescription-' + idBeingUpdated).text());
      $('#bookmark-edit-error').css("display", "none");
      var dataId = $(this).attr("data-id");
      checkLoggedIn(function() {
         $('#bookmark-edit').data('bookmarkId', dataId).dialog('open');
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
   $("#delete-link").click(function(e) {
      e.preventDefault();
      $('#confirm-delete').dialog('open');
   });
});
