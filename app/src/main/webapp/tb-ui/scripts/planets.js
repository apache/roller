$(function() {
   $.ajaxSetup({
      statusCode: {
         408: function() {
            document.planetsForm.submit();
         }
      }
   });
   $.ajax({
      type: "GET",
      url: contextPath + '/tb-ui/admin/rest/planets',
      success: function(data, textStatus, xhr) {
        $('.tableBody').loadTemplate($("#tableTemplate"), data);
        $(".rollertable tr").removeClass("altrow").filter(":even").addClass("altrow");
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
                    $('tr#' + idToRemove).remove();
                    $(".rollertable tr").removeClass("altrow").filter(":even").addClass("altrow");
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
