$(function() {
  var data = {};
  var myTemplate = $.templates("#formTemplate");
  function updateEditForm(data) {
    myTemplate.link("#formBody", data);
  }
  function refreshUserList() {
    checkLoggedIn(function() {
      $.ajax({
         type: "GET",
         url: contextPath + '/tb-ui/admin/rest/useradmin/userlist',
         success: function(data, textStatus, xhr) {
           $('#useradmin-select-user').empty();
           for (var key in data) {
             $('#useradmin-select-user').append('<option value="' + key + '">' + data[key] + '</option>');
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
     var selectedId = $('#useradmin-select-user').val();
     checkLoggedIn(function() {
       $.ajax({
          type: "GET",
          url: contextPath + '/tb-ui/admin/rest/useradmin/user/' + selectedId,
          success: function(data, textStatus, xhr) {
            updateEditForm(data);
          }
       });
     });
  });
  $("#create-user").click(function(e) {
     e.preventDefault();
     var selectedId = $('#useradmin-select-user').val();
     checkLoggedIn(function() {
       var data = {};
       updateEditForm(data);
     });
  });
  $("#save-link").click(function(e) {
    e.preventDefault();
    var view = $.view("#recordId");
    checkLoggedIn(function() {
      $.ajax({
         type: "PUT",
         url: contextPath + '/tb-ui/admin/rest/useradmin/' + (view.data.id == null ? 'users' : ('user/' + view.data.id)),
         data: JSON.stringify(view.data),
         contentType: "application/json",
         success: function(data, textStatus, xhr) {
           updateEditForm(data);
           refreshUserList();
         }
      });
    });
  });
});
