$(function() {
   $.ajaxSetup({
      statusCode: {
         408: function() {
            document.planetEditForm.submit();
         }
      }
   });
   $(function() {
     $.ajax({
        type: "GET",
        url: contextPath + '/tb-ui/admin/rest/planet/' + $('#planetEditForm_planetId').attr('value'),
        success: function(data, textStatus, xhr) {
          var tmpl = $.templates({formTemplate: '#formTemplate', tableTemplate: '#tableTemplate'});
          var html = $.render.formTemplate(data);
          $("#planetEditFields").html(html);
          html = $.render.tableTemplate(data);
          $("#tableBody").html(html);
          $(".rollertable tr").removeClass("altrow").filter(":even").addClass("altrow");
        }
     });
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
   $("#tableBody").on('click', '.delete-link', function(e) {
      e.preventDefault();
      var tr = $(this).closest('tr');
      var idToRemove = tr.attr('id');
      var feedName = tr.find('td.title-cell').text();
      $('#confirm-delete')
          .dialog('option', 'title', feedName)
          .data('deleteId', idToRemove).dialog('open');
   });
   $("#add-link").click(function(e) {
     e.preventDefault();
     var planetId = $('#planetEditForm_planetId').val();
     var feedUrl = encodeURIComponent($('#feedUrl').val());
     if (planetId == '' || feedUrl == '') return;
     $.get(contextPath + '/tb-ui/authoring/rest/categories/loggedin', function() {
       $.ajax({
          type: "PUT",
          url: contextPath + '/tb-ui/admin/rest/planetsubscriptions?planetId=' + planetId + '&feedUrl=' + feedUrl,
          success: function(data, textStatus, xhr) {
             document.planetEditForm.submit();
          }
       });
     });
   });
   $("#save-planet").click(function(e) {
     e.preventDefault();
     var salt = $("#salt").val();
     var idToUpdate = $("#planetEditForm_planetId").val();
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
               $('#planetEditForm_planetId').attr('value', data);
            }
          }
       });
     });
   });
});
