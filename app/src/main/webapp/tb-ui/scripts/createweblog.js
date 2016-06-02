$(function() {
  var data = {
    newWeblog : {"theme":"basic", "tagline":"", "editorPage" : "editor-text.jsp"},
    themeList : []
  };
  $.templates({
    formTemplate: '#formTemplate',
    errorMessageTemplate: '#errorMessageTemplate',
    selectedThemeTemplate: '#selectedThemeTemplate'
  });
  function getThemes() {
    checkLoggedIn(function() {
      $.ajax({
        url: contextPath + "/tb-ui/authoring/rest/themes/null",
        contentType: "application/json",
        success: function(themes) { $
          data.themeList = themes;
          $.link.formTemplate("#formBody", data);
          $('#themeSelector option[value=basic]').prop('selected', 'selected').trigger('change');
        }
      });
    });
  }
  $(function() {
    getThemes();
  });
  $("#formBody").on('change', '#themeSelector', function(e) {
    var selInx = $(this).prop('selectedIndex');
    var html = $.render.selectedThemeTemplate(data.themeList[selInx]);
    $('#themeDetails').html(html);
  });
  $("#myForm").submit(function(e) {
    e.preventDefault();
    $('#errorMessageDiv').hide();
    var view = $.view("#recordId");
    checkLoggedIn(function() {
      $.ajax({
         type: "PUT",
         url: contextPath + '/tb-ui/authoring/rest/weblogs',
         data: JSON.stringify(view.data.newWeblog),
         contentType: "application/json",
         success: function(data, textStatus, xhr) {
           window.location.replace($('#menuURL').attr('value'));
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
