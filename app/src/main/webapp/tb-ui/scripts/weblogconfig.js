$(function() {
  var data = {};
  $.templates({
    formTemplate: '#formTemplate',
    errorMessageTemplate: '#errorMessageTemplate'
  });
  function updateEditForm(data) {
    $.link.formTemplate("#formBody", data);
  }
  $(function() {
    var recordId = $('#weblogId').val();
    checkLoggedIn(function() {
      $.ajax({
         type: "GET",
         url: contextPath + '/tb-ui/authoring/rest/weblog/' + recordId,
         success: function(data, textStatus, xhr) {
           updateEditForm(data);
         }
      });
    });
  });
  $("#myForm").submit(function(e) {
    e.preventDefault();
    $('#errorMessageDiv').hide();
    $('#successMessageDiv').hide();
    var view = $.view("#recordId");
    checkLoggedIn(function() {
      $.ajax({
         type: "POST",
         url: contextPath + '/tb-ui/authoring/rest/weblog/' + view.data.id,
         data: JSON.stringify(view.data),
         contentType: "application/json",
         success: function(data, textStatus, xhr) {
           $('#successMessageDiv').show();
           updateEditForm(data);
         },
         error: function(xhr, status, errorThrown) {
           if (xhr.status == 400) {
              var html = $.render.errorMessageTemplate(xhr.responseJSON);
              $('#errorMessageDiv').html(html);
              $('#errorMessageDiv').show();
           }
         }
      });
    });
  });
  $("#confirm-delete").dialog({
    autoOpen: false,
    resizable: true,
    height:310,
    modal: true,
    buttons: [
       {
          text: msg.deleteLabel,
          click: function() {
            var idToRemove = $('#recordId').attr('data-id');
            checkLoggedIn(function() {
              $.ajax({
               type: "DELETE",
               url: contextPath + '/tb-ui/authoring/rest/weblog/' + idToRemove,
               success: function(data, textStatus, xhr) {
                 window.location.replace($('#menuURL').attr('value'));
               }
              });
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
    $('#confirm-delete').dialog('open');
  });
});
