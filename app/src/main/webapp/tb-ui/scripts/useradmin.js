$(function() {
  var data = {};
  $.templates({
    formTemplate: '#formTemplate',
    tableTemplate: '#tableTemplate',
    errorMessageTemplate: '#errorMessageTemplate'
  });
  function updateEditForm(data) {
    $.link.formTemplate("#formBody", data);

    $.ajax({
       type: "GET",
       url: contextPath + '/tb-ui/admin/rest/useradmin/user/' + data.id + '/weblogs',
       success: function(data, textStatus, xhr) {
         var html = $.render.tableTemplate(data);
         $('#tableBody').html(html);
       }
    });
  }
  function refreshUserList(id) {
    checkLoggedIn(function() {
      $.ajax({
         type: "GET",
         url: contextPath + '/tb-ui/admin/rest/useradmin/userlist',
         success: function(data, textStatus, xhr) {
           $('#useradmin-select-user').empty();
           for (var key in data) {
             $('#useradmin-select-user').append('<option value="' + key + '">' + data[key] + '</option>');
           }
           if (id) {
             $('#useradmin-select-user').val(id);
           }
         }
      });
    });
  }
  $(function() {
    refreshUserList();
  });
  $("#select-user").click(function(e) {
     e.preventDefault();
     $('#userCreate').hide();
     $('#errorMessageDiv').hide();
     var selectedId = $('#useradmin-select-user').val();
     checkLoggedIn(function() {
       $.ajax({
          type: "GET",
          url: contextPath + '/tb-ui/admin/rest/useradmin/user/' + selectedId,
          success: function(data, textStatus, xhr) {
            updateEditForm(data);
            $('div .showinguser').show();
          }
       });
     });
  });
  $("#create-user").click(function(e) {
     e.preventDefault();
     $('#userEdit').hide();
     $('#errorMessageDiv').hide();
     checkLoggedIn(function() {
       $('div .showinguser').hide();
       var data = {};
       updateEditForm(data);
     });
  });
  $("#cancel-link").click(function (e) {
    e.preventDefault();
    $('div .showinguser').hide();
    window.location.replace($('#refreshURL').attr('value'));
  });
  $("#myForm").submit(function(e) {
    e.preventDefault();
    $('#errorMessageDiv').hide();
    var view = $.view("#recordId");
    checkLoggedIn(function() {
      $.ajax({
         type: "PUT",
         url: contextPath + '/tb-ui/admin/rest/useradmin/' + (view.data.id == null ? 'users' : ('user/' + view.data.id)),
         data: JSON.stringify(view.data),
         contentType: "application/json",
         success: function(data, textStatus, xhr) {
           $('div .showinguser').show();
           updateEditForm(data);
           $('#userEdit').show();
           $('#userCreate').show();
           refreshUserList(data.id);
         },
         error: function(xhr, status, errorThrown) {
            if (xhr.status == 400) {
              $('div .showinguser').hide();
              var html = $.render.errorMessageTemplate(xhr.responseJSON);
              $('#errorMessageDiv').html(html);
              $('#errorMessageDiv').show();
            }
         }
      });
    });
  });
});
