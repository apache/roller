$(function() {
  $.templates({
    errorMessageTemplate: '#errorMessageTemplate'
  });
  $("#confirm-switch").dialog({
    autoOpen: false,
    resizable: true,
    height:310,
    modal: true,
    buttons: [
       {
          text: msg.confirmLabel,
          click: function() {
            var handle = $('#recordId').val();
            var newTheme = $('#themeSelector').val();
            checkLoggedIn(function() {
              $.ajax({
               type: "POST",
               url: contextPath + '/tb-ui/authoring/rest/weblog/' + handle + '/switchtheme/' + newTheme,
               success: function(data, textStatus, xhr) {
                 document.themeForm.submit();
               },
               error: function(xhr, status, errorThrown) {
                 $('#success-message').hide();
                 if (xhr.status == 400) {
                   var html = $.render.errorMessageTemplate(xhr.responseJSON);
                   $('#failure-message').html(html);
                 } else {
                   $('#failure-message .textSpan').text('Error Code: ' + xhr.status);
                 }
                 $('#failure-message').show();
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
  $("#update-button").click(function(e) {
    e.preventDefault();
    $('#confirm-switch').dialog('open');
  });
});
function fullPreview(selector) {
    selected = selector.selectedIndex;
    window.open(contextPath  + '/tb-ui/authoring/preview/' + weblogHandle + '?theme=' + selector.options[selected].value);
}
