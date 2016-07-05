$(function() {
  var data = { "approved" : true };
  function formatDateFunction(dateValue) {
    return (dateValue == null) ? null : new Date(dateValue).toLocaleString();
  }
  var myHelpers = { formatDate : formatDateFunction };
  $.templates({
    formTemplate: { markup: '#formTemplate', helpers: myHelpers },
    pendingTemplate: '#pendingTemplate',
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
  function getPendingRegistrations() {
    checkLoggedIn(function() {
      $.ajax({
         type: "GET",
         url: contextPath + '/tb-ui/admin/rest/useradmin/registrationapproval',
         success: function(data, textStatus, xhr) {
           var html = $.render.pendingTemplate(data);
           $('#pendingList').html(html);
         }
      });
    });
  }
  $(function() {
    getPendingRegistrations();
    refreshUserList();
  });
  $("#pendingList").on('click', '.approve-button', function(e) {
     e.preventDefault();
     var span = $(this).closest('span');
     var userId = span.attr('id');
     processRegistration(userId, 'approve');
  });

  $("#pendingList").on('click', '.decline-button', function(e) {
     e.preventDefault();
     var span = $(this).closest('span');
     var userId = span.attr('id');
     processRegistration(userId, 'reject');
  });

  function processRegistration(userId, command) {
    checkLoggedIn(function() {
       $.ajax({
          type: "POST",
          url: contextPath + '/tb-ui/admin/rest/useradmin/registrationapproval/' + userId + '/' + command,
          success: function(data, textStatus, xhr) {
             getPendingRegistrations();
             refreshUserList();
          }
       });
    });
  }

  $("#select-user").click(function(e) {
     e.preventDefault();
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
         url: contextPath + '/tb-ui/admin/rest/useradmin/user/' + view.data.id,
         data: JSON.stringify(view.data),
         contentType: "application/json",
         success: function(data, textStatus, xhr) {
           $('div .showinguser').show();
           updateEditForm(data);
           $('#userEdit').show();
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
