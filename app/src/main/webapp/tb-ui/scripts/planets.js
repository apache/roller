$(function() {
   $.ajaxSetup({
      statusCode: {
         408: function() {
            document.planetsForm.submit();
         }
      }
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
              var idToRemove = $(this).data('deleteId');
              $.ajax({
                 type: "DELETE",
                 url: contextPath + '/tb-ui/admin/rest/planets/' + idToRemove,
                 success: function(data, textStatus, xhr) {
                    document.planetsForm.submit();
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
      var tr = $(this).closest('tr');
      var idToRemove = tr.attr('id');
      var planetName = tr.find('td.title-cell').text();
      $('#confirm-delete')
          .dialog('option', 'title', planetName)
          .data('deleteId', idToRemove).dialog('open');
   });
});
