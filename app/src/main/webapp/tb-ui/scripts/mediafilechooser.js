$(function() {
  var data = {};
  $.templates({
    formTemplate: '#formTemplate'
  });
  function updateFileList(data) {
    $.link.formTemplate("#formBody", data);
  }
  function refreshDirectoryList() {
    var recordId = $('#recordId').attr('value');
    checkLoggedIn(function() {
      $.ajax({
         type: "GET",
         url: contextPath + '/tb-ui/authoring/rest/weblog/' + recordId + '/mediadirectories',
         success: function(data, textStatus, xhr) {
           $('#mediachooser-select-directory').empty();
           $.each(data, function(i, d) {
              $('#mediachooser-select-directory').append('<option value="' + d.id + '">' + d.name + '</option>');
           });
         }
      });
    });
  }
  $("#select-item").click(function(e) {
     e.preventDefault();
     var selectedId = $('#mediachooser-select-directory').val();
     checkLoggedIn(function() {
       $.ajax({
          type: "GET",
          url: contextPath + '/tb-ui/authoring/rest/mediadirectories/' + selectedId + '/files',
          success: function(data, textStatus, xhr) {
            updateFileList(data);
          }
       });
     });
  });
  $(function() {
    refreshDirectoryList();
  });
});
function highlight(el, flag) {
    if (flag) {
        $(el).addClass("mediaFileHighlight");
    } else {
        $(el).removeClass("mediaFileHighlight");
    }
}
