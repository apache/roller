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
    var recordId = $('#userId').val();
    checkLoggedIn(function() {
      $.ajax({
         type: "GET",
         url: contextPath + '/tb-ui/authoring/rest/userprofile/' + recordId,
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
         url: contextPath + '/tb-ui/authoring/rest/userprofile/' + view.data.id,
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
});
