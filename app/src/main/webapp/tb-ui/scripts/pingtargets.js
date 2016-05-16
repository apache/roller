$(function() {
  $.ajaxSetup({
     statusCode: {
        408: function() {
           document.pingTargetsForm.submit();
        }
     }
  });
  $("#pingtarget-edit").dialog({
     autoOpen: false,
     height: 210,
     width: 570,
     modal: true,
     buttons: [
        {
           text: msg.saveLabel,
           click: function() {
              var idToUpdate = $(this).data('pingtargetId');
              var newName = $('#pingtarget-edit-name').val().trim();
              var newUrl = $('#pingtarget-edit-url').val().trim();
              var newData = {
                 "name": newName,
                 "url": newUrl
              };
              if (newName.length > 0 && newUrl.length > 0) {
                 $.ajax({
                    type: "PUT",
                    url: contextPath + '/tb-ui/admin/rest/' + ((idToUpdate == '') ? 'pingtargets?weblog=' + $("#actionWeblog").val() : 'pingtarget/' + idToUpdate),
                    data: JSON.stringify(newData),
                    contentType: "application/json; charset=utf-8",
                    processData: "false",
                    success: function(data, textStatus, xhr) {
                       if (idToUpdate == '') {
                          document.pingTargetsForm.submit();
                       } else {
                          $('#ptname-' + idToUpdate).text(newName.trim());
                          $('#pturl-' + idToUpdate).text(newUrl.trim());
                          $("#pingtarget-edit").dialog().dialog("close");
                       }
                    },
                    error: function(xhr, status, errorThrown) {
                       if (xhr.status in this.statusCode)
                          return;
                       $('#pingtarget-edit-error').css("display", "inline");
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
     height:200,
     modal: true,
     buttons: [
        {
           text: msg.confirmLabel,
           click: function() {
              var idToRemove = encodeURIComponent($(this).data('target'));
              $.ajax({
                 type: "DELETE",
                 url: contextPath + '/tb-ui/admin/rest/pingtarget/' + idToRemove,
                 success: function(data, textStatus, xhr) {
                    document.pingTargetsForm.submit();
                 }
              });
           },
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
     $('#pingtarget-edit').dialog('option', 'title', msg.editTitle)
     var idBeingUpdated = $(this).attr("data-id");
     $('#pingtarget-edit-name').val($('#ptname-' + idBeingUpdated).text()).select();
     $('#pingtarget-edit-url').val($('#pturl-' + idBeingUpdated).text());
     $('#pingtarget-edit-error').css("display", "none");
     var dataId = $(this).attr("data-id");
     $.get(contextPath + '/tb-ui/authoring/rest/categories/loggedin', function() {
        $('#pingtarget-edit').data('pingtargetId', dataId).dialog('open');
     });
  });
  $("#add-link").click(function(e) {
     e.preventDefault();
     $('#pingtarget-edit').dialog('option', 'title', msg.addTitle)
     $('#pingtarget-edit-name').val('');
     $('#pingtarget-edit-url').val('');
     $('#pingtarget-edit-error').css("display", "none");
     $.get(contextPath + '/tb-ui/authoring/rest/categories/loggedin', function() {
        $('#pingtarget-edit').data('pingtargetId', '').dialog('open');
     });
  });
  $(".delete-link").click(function(e) {
    e.preventDefault();
    $('#confirm-delete').data('target',  $(this).attr("data-id")).dialog('open');
  });
  $(".enable-toggle").click(function(e) {
     e.preventDefault();
     var changeStateCell = $(this);
     var targetId = changeStateCell.attr("data-id");
     var currentStateCell = $('#enablestate-' + targetId);
     var bEnable = !changeStateCell.data("enabled");
     var targetUrl = contextPath + '/tb-ui/admin/rest/pingtargets/' + (bEnable ? 'enable' : 'disable') + '/' + targetId;
     $.ajax({
        type: "POST",
        url: targetUrl,
        success: function(enabled) {
           changeStateCell.data("enabled", enabled);
           changeStateCell.text(enabled ? msg.pingTargetDisable : msg.pingTargetEnable);
           currentStateCell.text(enabled ? msg.pingTargetEnabledIndicator : msg.pingTargetDisabledIndicator);
        }
     });
  });
});
