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
                   document.planetEditForm.submit();
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
     var planet = $('#planetHandle').val();
     var subUrl = encodeURIComponent($('#subUrl').val());
     $.get(contextPath + '/tb-ui/authoring/rest/categories/loggedin', function() {
       $.ajax({
          type: "PUT",
          url: contextPath + '/tb-ui/admin/rest/planetsubscriptions?planet=' + planet + '&subUrl=' + subUrl,
          success: function(data, textStatus, xhr) {
             document.planetEditForm.submit();
          }
       });
     });
   });
});
