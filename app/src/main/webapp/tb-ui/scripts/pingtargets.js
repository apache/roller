$(function() {
  $.templates({
    tableTmpl: '#tableTemplate'
  });
  function updateView(data) {
    var html = $.render.tableTmpl(data);
    $("#tableBody").html(html);
    $(".rollertable tr").removeClass("altrow").filter(":even").addClass("altrow");
  }
  function refreshData() {
    checkLoggedIn(function() {
      $.ajax({
         type: "GET",
         url: contextPath + '/tb-ui/admin/rest/pingtargets',
         success: function(data, textStatus, xhr) {
           updateView(data);
         }
      });
    });
  }
  refreshData();
  $("#pingtarget-edit").dialog({
     autoOpen: false,
     height: 210,
     width: 570,
     modal: true,
     buttons: [
        {
           text: msg.saveLabel,
           click: function() {
              var idToUpdate = $(this).data('actionId');
              var newName = $('#pingtarget-edit-name').val().trim();
              var newUrl = $('#pingtarget-edit-url').val().trim();
              var newData = {
                 "name": newName,
                 "pingUrl": newUrl
              };
              if (newName.length > 0 && newUrl.length > 0) {
                 $.ajax({
                    type: "PUT",
                    url: contextPath + '/tb-ui/admin/rest/' + ((idToUpdate == '') ? 'pingtargets?weblog=' + $("#actionWeblog").val() : 'pingtarget/' + idToUpdate),
                    data: JSON.stringify(newData),
                    contentType: "application/json; charset=utf-8",
                    processData: "false",
                    success: function(data, textStatus, xhr) {
                       var html = $.render.tableTmpl(data);
                       if (idToUpdate == '') {
                         $("#tableBody").append(html);
                       } else {
                         $('#' + idToUpdate).prop('outerHTML', html);
                       }
                       $(".rollertable tr").removeClass("altrow").filter(":even").addClass("altrow");
                       $("#pingtarget-edit").dialog("close");
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
              var idToRemove = $(this).data('actionId');
              $.ajax({
                 type: "DELETE",
                 url: contextPath + '/tb-ui/admin/rest/pingtarget/' + idToRemove,
                 success: function(data, textStatus, xhr) {
                    $('#' + idToRemove).remove();
                    $(".rollertable tr").removeClass("altrow").filter(":even").addClass("altrow");
                 }
              });
              $(this).dialog("close");
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
  $("#tableBody").on('click', '.edit-link', function(e) {
     e.preventDefault();
     $('#successMessageDiv').hide();
     $('#errorMessageDiv').hide();
     var tr = $(this).closest('tr');
     var actionId = tr.attr('id');
     $('#pingtarget-edit').dialog('option', 'title', msg.editTitle)
     $('#pingtarget-edit-name').val(tr.find('.name-cell').text()).select();
     $('#pingtarget-edit-url').val(tr.find('.url-cell').text());
     $('#pingtarget-edit-error').css("display", "none");
     checkLoggedIn(function() {
        $('#pingtarget-edit').data('actionId', actionId).dialog('open');
     });
  });
  $("#tableBody").on('click', '.test-link', function(e) {
     e.preventDefault();
     var tr = $(this).closest('tr');
     var actionId = tr.attr('id');
     $('#successMessageDiv').hide();
     $('#errorMessageDiv').hide();
     checkLoggedIn(function() {
       $.ajax({
          type: "POST",
          url: contextPath + '/tb-ui/admin/rest/pingtargets/test/' + actionId,
          success: function(data, textStatus, xhr) {
             $('#successMessageDiv span').text('Result: error? ' + data.error + '; message: ' + data.message);
             $('#successMessageDiv').show();
          },
          error: function(xhr, status, errorThrown) {
            $('#errorMessageDiv span').text('Result: ' + xhr.responseText);
            $('#errorMessageDiv').show();
          }
       });
     });
  });
  $("#tableBody").on('click', '.delete-link', function(e) {
    e.preventDefault();
    $('#successMessageDiv').hide();
    $('#errorMessageDiv').hide();
    var actionId = $(this).closest('tr').attr('id');
    $('#confirm-delete').data('actionId',  actionId).dialog('open');
  });
  $("#add-link").click(function(e) {
     e.preventDefault();
     $('#successMessageDiv').hide();
     $('#errorMessageDiv').hide();
     $('#pingtarget-edit').dialog('option', 'title', msg.addTitle)
     $('#pingtarget-edit-name').val('');
     $('#pingtarget-edit-url').val('');
     $('#pingtarget-edit-error').css("display", "none");
     checkLoggedIn(function() {
        $('#pingtarget-edit').data('actionId', '').dialog('open');
     });
  });
  $("#tableBody").on('click', '.enable-toggle', function(e) {
     e.preventDefault();
     $('#successMessageDiv').hide();
     $('#errorMessageDiv').hide();
     var tr = $(this).closest('tr');
     var targetId = tr.attr('id');
     var changeStateCell = $(this);
     var currentStateCell = tr.find('.current-state-cell');
     var bEnable = !changeStateCell.data("enabled");
     var targetUrl = contextPath + '/tb-ui/admin/rest/pingtargets/' + (bEnable ? 'enable' : 'disable') + '/' + targetId;
     $.ajax({
        type: "POST",
        url: targetUrl,
        success: function(data) {
          var html = $.render.tableTmpl(data);
          tr.prop('outerHTML', html);
        }
     });
  });
});
