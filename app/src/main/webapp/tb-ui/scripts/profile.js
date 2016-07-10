$(function() {
  var data = {
    "user" : {
      "locale" : "en"
    },
    "credentials" : {
    }
  };
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
           success: function(ldapData, textStatus, xhr) {
             data.user = ldapData;
             data.credentials = {};
             updateEditForm(data);
           },
           error: function(xhr, status, errorThrown) {
              if (xhr.status == 404) {
                $('div .ldapok').hide();
                $('#errorMessageNoLDAPAuth').show();
              }
           }
        });
      } else {
        updateEditForm(data);
      }
    } else {
      $.ajax({
         type: "GET",
         url: contextPath + '/tb-ui/authoring/rest/userprofile/' + recordId,
         success: function(dbData, textStatus, xhr) {
           data.user = dbData;
           data.credentials = {};
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
    var isUpdate = view.data.user.hasOwnProperty('id');
    var urlToUse = contextPath + (isUpdate ? '/tb-ui/authoring/rest/userprofile/' + view.data.user.id
      : '/tb-ui/register/rest/registeruser');
    $.ajax({
       type: "POST",
       url: urlToUse,
       data: JSON.stringify(view.data),
       contentType: "application/json",
       success: function(dbData, textStatus, xhr) {
         if (!isUpdate) {
           $('div .notregistered').hide();
         }
         if (dbData.status == 'ENABLED') {
           $('#successMessageDiv').show();
         } else {
           $('#successMessageNeedActivation').show();
         }
         data.user = dbData;
         data.credentials = {};
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
