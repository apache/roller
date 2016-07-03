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
            angular.element('#templates-list').scope().ctrl.deleteTemplate(idsToRemove[i]);
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
  $("#delete-link").click(function(e) {
    e.preventDefault();
    if ($('input[name="idSelections"]:checked').size() > 0) {
      $('#confirm-delete').dialog('open');
    }
  });
});

var templatesApp = angular.module('TemplatesApp', []);

templatesApp.config(['$httpProvider', function($httpProvider) {
    $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
    var header = $("meta[name='_csrf_header']").attr("content");
    var token = $("meta[name='_csrf']").attr("content");
    $httpProvider.defaults.headers.delete = {};
    $httpProvider.defaults.headers.delete[header] = token;
    $httpProvider.defaults.headers.post[header] = token;
    $httpProvider.defaults.headers.put[header] = token;
}]);

templatesApp.controller('TemplatesController', ['$http', function TemplatesController($http) {
    var self = this;
    this.selectedRole = 'CUSTOM_EXTERNAL';
    this.newTemplateName = '';

    this.errorObj = {};

    this.loadTemplateData = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + $("#actionWeblogId").val() + '/templates').then(function(response) {
        self.weblogTemplateData = response.data;
      });
    };
    this.loadTemplateData();

    this.deleteTemplate = function(templateId) {
      $http.delete(contextPath + '/tb-ui/authoring/rest/template/' + templateId).then(
         function(response) {
           self.loadTemplateData();
         }
      );
    }

    this.addTemplate = function() {
      $('#errorMessageDiv').hide();
      $('#successMessageDiv').hide();
      var newData = {
        "name" : this.newTemplateName,
        "role" : this.selectedRole
      };
      $http.post(contextPath + '/tb-ui/authoring/rest/weblog/' + $("#actionWeblogId").val() + '/templates', JSON.stringify(newData)).then(
        function(response) {
          $('#successMessageDiv').show();
          self.loadTemplateData();
        },
        function(response) {
         if (response.status == 408)
           window.location.replace($('#refreshURL').attr('value'));  // return;
         if (response.status == 400) {
           self.errorObj = response.data;
           $('#errorMessageDiv').show();
         }
      })

    }
  }]);
