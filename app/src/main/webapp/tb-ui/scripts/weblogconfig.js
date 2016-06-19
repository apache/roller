$(function() {
  var data = {
    weblogData : {"theme":"basic", "tagline":"", "editorPage" : "editor-text.jsp",
    "allowComments" : true, "emailComments" : false, "approveComments" : true, "visible" : true,
    "entriesPerPage" : 15, "defaultCommentDaysString" : "-1"},
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
        success: function(themes) {
          data.themeList = themes;
          $.link.formTemplate("#formBody", data);
          $('#themeSelector option[value=basic]').prop('selected', 'selected').trigger('change');
        }
      });
    });
  }
  function updateEditForm(data) {
    $.link.formTemplate("#formBody", data);
  }
  $(function() {
    var recordId = $('#weblogId').val();
    if (recordId != '') {
      checkLoggedIn(function() {
        $.ajax({
           type: "GET",
           url: contextPath + '/tb-ui/authoring/rest/weblog/' + recordId,
           success: function(weblogData, textStatus, xhr) {
             data.weblogData = weblogData;
             updateEditForm(data);
           }
        });
      });
    } else {
      getThemes();
    }
  });
  $("#formBody").on('change', '#themeSelector', function(e) {
    var selInx = $(this).prop('selectedIndex');
    var html = $.render.selectedThemeTemplate(data.themeList[selInx]);
    $('#themeDetails').html(html);
  });
  $("#myForm").submit(function(e) {
    e.preventDefault();
    $('#errorMessageDiv').hide();
    $('#successMessageDiv').hide();
    var view = $.view("#recordId");
    var update = view.data.weblogData.hasOwnProperty('id');
    var urlToUse = contextPath + (update ?
      '/tb-ui/authoring/rest/weblog/' + view.data.weblogData.id : '/tb-ui/authoring/rest/weblogs');
    checkLoggedIn(function() {
      $.ajax({
         type: "POST",
         url: urlToUse,
         data: JSON.stringify(view.data.weblogData),
         contentType: "application/json",
         success: function(data, textStatus, xhr) {
           if (update) {
             $('#successMessageDiv').show();
             updateEditForm(data);
           } else {
             window.location.replace($('#menuURL').attr('value'));
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
  $("#confirm-delete").dialog({
    autoOpen: false,
    resizable: true,
    height:310,
    modal: true,
    buttons: [
       {
          text: msg.deleteLabel,
          click: function() {
            var idToRemove = $('#recordId').attr('data-id');
            checkLoggedIn(function() {
              $.ajax({
               type: "DELETE",
               url: contextPath + '/tb-ui/authoring/rest/weblog/' + idToRemove,
               success: function(data, textStatus, xhr) {
                 window.location.replace($('#menuURL').attr('value'));
               }
              });
            });
            $(this).dialog("close");
          }
       },
       {
          text: msg.cancelLabel,
          click: function() {
             $(this).dialog("close");
          }
       }
    ]
  });
  $("#delete-link").click(function(e) {
    e.preventDefault();
    $('#confirm-delete').dialog('open');
  });
});
