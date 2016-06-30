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
    if (recordId == '') {
      if (authMethod == "ldap") {
        $.ajax({
           type: "GET",
           url: contextPath + '/tb-ui/register/rest/ldapdata',
           success: function(data, textStatus, xhr) {
             updateEditForm(data);
           },
           error: function(xhr, status, errorThrown) {
              if (xhr.status == 404) {
                $('div .ldapok').hide();
                $('#errorMessageNoLDAPAuth').show();
              }
           }
        });
      }
    } else {
      $.ajax({
         type: "GET",
         url: contextPath + '/tb-ui/authoring/rest/userprofile/' + recordId,
         success: function(data, textStatus, xhr) {
           updateEditForm(data);
         }
      });
    }
  });
  $("#cancel-link").click(function (e) {
    e.preventDefault();
    window.location.replace($('#cancelURL').attr('value'));
  });
  $("#myForm").submit(function(e) {
    e.preventDefault();
    $('#errorMessageDiv').hide();
    $('#successMessageDiv').hide();
    var view = $.view("#recordId");
    var isUpdate = view.data.hasOwnProperty('id');
    var urlToUse = contextPath + (isUpdate ? '/tb-ui/authoring/rest/userprofile/' + view.data.id
      : '/tb-ui/register/rest/registeruser');
    $.ajax({
       type: "POST",
       url: urlToUse,
       data: JSON.stringify(view.data),
       contentType: "application/json",
       success: function(data, textStatus, xhr) {
         if (!isUpdate) {
           $('div .notregistered').hide();
         }
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
