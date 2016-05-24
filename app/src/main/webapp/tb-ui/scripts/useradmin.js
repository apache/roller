$(function() {
  var data = {};
  $.templates("#formTemplate").link("#formBody", data);
  function updateEditForm(data) {
    var view = $.view("#someVal");
    view.data = data;
  }
  function refreshUserList() {
    checkLoggedIn(function() {
      $.ajax({
         type: "GET",
         url: contextPath + '/tb-ui/admin/rest/useradmin/userlist',
         success: function(data, textStatus, xhr) {
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
  $("#save-link").click(function(e) {
    e.preventDefault();
    var view = $.view("#someVal");
    checkLoggedIn(function() {
      $.ajax({
         type: "PUT",
         url: contextPath + '/tb-ui/admin/rest/useradmin/' + ((view.data.id == '') ? 'users' : 'user/' + view.data.id),
         data: JSON.stringify(view.data),
         success: function(data, textStatus, xhr) {
           updateEditForm(data);
         }
      });
    });
  });
});
