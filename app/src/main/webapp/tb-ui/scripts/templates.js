$(function() {
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
     var newData = {

     };

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
