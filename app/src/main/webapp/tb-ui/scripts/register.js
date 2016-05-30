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
    var startData = {};
    if (authMethod == "ldap") {
      $.ajax({
         type: "GET",
         url: contextPath + '/tb-ui/register/rest/ldapdata',
         success: function(data, textStatus, xhr) {
           startData = data;
         },
         error: function(xhr, status, errorThrown) {
            if (xhr.status == 404) {
              $('div .ldapok').hide();
              $('#errorMessageNoLDAPAuth').show();
            }
         }
      });
    }
    updateEditForm(data);
  });
  $("#cancel-link").click(function (e) {
    e.preventDefault();
    window.location.replace($('#cancelURL').attr('value'));
  });
  $("#myForm").submit(function(e) {
    e.preventDefault();
    $('#errorMessageDiv').hide();
    var view = $.view("#recordId");
    $.ajax({
       type: "PUT",
       url: contextPath + '/tb-ui/register/rest/registeruser',
       data: JSON.stringify(view.data),
       contentType: "application/json",
       success: function(data, textStatus, xhr) {
         $('div .notregistered').hide();
         if (data.enabled == true) {
           $('#successMessageDiv').show();
         } else {
           $('#successMessageNeedActivation').show();
         }
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
