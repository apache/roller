$(function() {
   $.ajaxSetup({
      statusCode: {
         408: function() {
            document.planetEditForm.submit();
         }
      }
   });
   $("#confirm-delete").dialog({
     autoOpen: false,
     resizable: false,
     height:170,
     modal: true,
     buttons: [
        {
           text: msg.confirmLabel,
           click: function() {
             var idToRemove = $(this).data('deleteId');
             $.ajax({
                type: "DELETE",
                url: contextPath + '/tb-ui/admin/rest/planetsubscriptions/' + idToRemove,
                success: function(data, textStatus, xhr) {
                   document.planetFeedForm.submit();
                }
             });
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
   $(".delete-link").click(function(e) {
     e.preventDefault();
     $('#confirm-delete').data('deleteId',  $(this).attr("data-id")).dialog('open');
   });
   $("#add-link").click(function(e) {
     e.preventDefault();
     var planet = $('#planetFeedForm_planetHandle').val();
     var feedUrl = encodeURIComponent($('#feedUrl').val());
     if (planet == '' || feedUrl == '') return;
     $.get(contextPath + '/tb-ui/authoring/rest/categories/loggedin', function() {
       $.ajax({
          type: "PUT",
          url: contextPath + '/tb-ui/admin/rest/planetsubscriptions?planet=' + planet + '&feedUrl=' + feedUrl,
          success: function(data, textStatus, xhr) {
             document.planetFeedForm.submit();
          }
       });
     });
   });
   $("#save-planet").click(function(e) {
     e.preventDefault();
     var salt = $("#salt").val();
     var idToUpdate = $("#planetEditForm_bean_id").val();
     var newData = {
        "title": $('#edit-title').val(),
        "handle": $('#edit-handle').val(),
        "description": $('#edit-description').val()
     };
     if (newData.name == '' || newData.handle == '') return;
     $.get(contextPath + '/tb-ui/authoring/rest/categories/loggedin', function() {
       $.ajax({
          type: "PUT",
          url: contextPath + '/tb-ui/admin/rest/' + ((idToUpdate == '') ? 'planets?salt=' + salt : 'planet/' + idToUpdate + '?salt=' + salt),
          data: JSON.stringify(newData),
          contentType: "application/json; charset=utf-8",
          processData: "false",
          success: function(data, textStatus, xhr) {
            if (idToUpdate == '') {
               $('#planetEditForm_bean_id').attr('value', data);
            }
            $('#planetFeedForm_planetHandle').attr('value', newData.handle);
          }
       });
     });
   });
});
