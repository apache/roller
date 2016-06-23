$(function() {
  $.templates({
    errorMessageTemplate: '#errorMessageTemplate'
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
        var idsToRemove = [];
        $('input[name="idSelections"]:checked').each(function(){
          idsToRemove.push($(this).val());
        });
        $(this).dialog("close");
        if (idsToRemove.length > 0) {
          for (i = 0; i < idsToRemove.length; i++) {
            $.ajax({
               type: "DELETE",
               url: contextPath + '/tb-ui/authoring/rest/template/' + idsToRemove[i],
               success: function(data, textStatus, xhr) {
                 angular.element('#templates-list').scope().ctrl.loadTemplateData();
                 angular.element('#templates-list').scope().$apply();
               }
            });
          }
        }
      }
      },
      {
        text: msg.cancelLabel,
        click: function() {
          $( this ).dialog( "close" );
        }
      }
    ]
  });
  $("#add-link").click(function(e) {
     e.preventDefault();
     $('#errorMessageDiv').hide();
     $('#successMessageDiv').hide();
     var newTemplateName = $('#newTmplName').val().trim();
     var newTemplateAction = $('#newTemplAction').val().trim();
     var newData = {
       "name" : newTemplateName,
       "role" : newTemplateAction
     };
     if (newTemplateName.length > 0 && newTemplateAction.length > 0) {
        $.ajax({
           type: "PUT",
           url: contextPath + '/tb-ui/authoring/rest/weblog/' + $("#actionWeblogId").val() + '/templates',
           data: JSON.stringify(newData),
           contentType: "application/json; charset=utf-8",
           processData: "false",
           success: function(data, textStatus, xhr) {
             $('#successMessageDiv').show();
             angular.element('#templates-list').scope().ctrl.loadTemplateData();
             angular.element('#templates-list').scope().$apply();
           },
           error: function(xhr, status, errorThrown) {
              if (xhr.status in this.statusCode)
                 return;
                 if (xhr.status == 400) {
                    var html = $.render.errorMessageTemplate(xhr.responseJSON);
                    $('#errorMessageDiv').html(html);
                    $('#errorMessageDiv').show();
                 }
           }
        });
     }
  });
  $("#delete-link").click(function(e) {
    e.preventDefault();
    if ($('input[name="idSelections"]:checked').size() > 0) {
      $('#confirm-delete').dialog('open');
    }
  });
});

var templatesApp = angular.module('TemplatesApp', []);

templatesApp.controller('TemplatesController', ['$http', function TemplatesController($http) {
    var self = this;

    this.loadTemplateData = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + $("#actionWeblogId").val() + '/templates').then(function(response) {
        self.weblogTemplateData = response.data;
      });
    };
    this.loadTemplateData();
    this.selectedRole = 'STYLESHEET';

  }]);
