tightblogApp.controller('PageController', ['$http', function PageController($http) {
    var self = this;
    var showSuccessMessage = false;
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

    this.deleteTemplates = function() {
        $('#deleteTemplatesModal').modal('hide');

        var selectedItems = [];
        $('input[name="idSelections"]:checked').each(function(){
            selectedItems.push($(this).val());
        });

        $http.post(contextPath + '/tb-ui/authoring/rest/templates/delete',
            JSON.stringify(selectedItems)).then(
            function(response) {
                self.loadTemplateData();
            }
        );
    }

    this.templatesSelected = function() {
        return $('input[name="idSelections"]:checked').size() > 0;
    }

    this.toggleCheckboxes = function(checked) {
        $('input[name="idSelections"]').each(function(){
            $(this).prop('checked', checked);
        });
    }

    this.addTemplate = function() {
      self.showSuccessMessage = false;
      var newData = {
        "name" : this.newTemplateName,
        "roleName" : this.selectedRole,
        "template" : ""
      };
      $http.post(contextPath + '/tb-ui/authoring/rest/weblog/' + actionWeblogId + '/templates', JSON.stringify(newData)).then(
        function(response) {
          self.showSuccessMessage = true;
          self.loadTemplateData();
          self.resetAddTemplateData();
        },
        function(response) {
         if (response.status == 408)
           window.location.replace($('#refreshURL').attr('value'));  // return;
         if (response.status == 400) {
           self.errorObj = response.data;
         }
      })
    }
}]);
