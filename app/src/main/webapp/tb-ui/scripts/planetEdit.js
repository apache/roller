$(function() {
   $.templates({
     formTmpl: '#formTemplate',
     tableTmpl: '#tableTemplate'
   });
   function updateEditForm(data) {
     var html = $.render.formTmpl(data);
     $("#planetEditFields").html(html);
   }
   function refreshView(tableAlso) {
     var planetId = $('#planetEditForm_planetId').attr('value');
     if (planetId != '') {
       $.get(contextPath + '/tb-ui/authoring/rest/categories/loggedin', function() {
         $.ajax({
            type: "GET",
            url: contextPath + '/tb-ui/admin/rest/planet/' + planetId,
            success: function(data, textStatus, xhr) {
              updateEditForm(data);
              if (tableAlso) {
                var html = $.render.tableTmpl(data.subscriptions);
                $("#tableBody").html(html);
                $(".rollertable tr").removeClass("altrow").filter(":even").addClass("altrow");
              }
              $("#feedManagement").toggle(true);
            }
         });
       });
     } else {
       var data = { "title": '',
         "handle": '',
         "description": '',
         "subscriptions": []
       };
       updateEditForm(data);
     }
   }
   $(function() {
     refreshView(true);
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
   $("#reset-planet").click(function(e) {
     refreshView(false);
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
            var html = $.render.tableTmpl(data);
            $("#tableBody").append(html);
            $(".rollertable tr").removeClass("altrow").filter(":even").addClass("altrow");
          }
       });
     });
   });
   $("#save-planet").click(function(e) {
     e.preventDefault();
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
          url: contextPath + '/tb-ui/admin/rest/' + ((idToUpdate == '') ? 'planets' : 'planet/' + idToUpdate),
          data: JSON.stringify(newData),
          contentType: "application/json; charset=utf-8",
          processData: "false",
          success: function(data, textStatus, xhr) {
            if (idToUpdate == '') {
               $('#planetEditForm_planetId').attr('value', data.id);
            }
            $("#feedManagement").toggle(true);
            updateEditForm(data);
          }
       });
     });
   });
});
