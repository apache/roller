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
            angular.element('#ngapp-div').scope().ctrl.deleteTemplate(idsToRemove[i]);
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
});

tightblogApp.controller('PageController', ['$http', function PageController($http) {
    var self = this;
    this.errorObj = {};

    this.resetAddTemplateData = function() {
        this.selectedRole = 'CUSTOM_EXTERNAL';
        this.newTemplateName = '';
    }

    this.loadTemplateData = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + actionWeblogId + '/templates').then(function(response) {
        self.weblogTemplateData = response.data;
      });
    };
    this.loadTemplateData();
    this.resetAddTemplateData();

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
        "roleName" : this.selectedRole,
        "template" : ""
      };
      $http.post(contextPath + '/tb-ui/authoring/rest/weblog/' + actionWeblogId + '/templates', JSON.stringify(newData)).then(
        function(response) {
          $('#successMessageDiv').show();
          self.loadTemplateData();
          self.resetAddTemplateData();
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

tightblogApp.directive('confirmDeleteDialog', function(){
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            var dialogId = '#' + attr.confirmDeleteDialog;
            elem.bind('click', function(e) {
                if ($('input[name="idSelections"]:checked').size() > 0) {
                    $(dialogId).dialog('open');
                }
            });
        }
    };
});
