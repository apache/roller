tightblogApp.controller('PageController', ['$http', function PageController($http) {
      var self = this;
      var templateData = {};
      var lastSavedRelativePath = null;
      var errorObj = null;
      var showSuccessMessage = false;

      this.launchPage = function() {
          window.open(weblogUrl + 'page/' + self.lastSavedRelativePath, '_blank');
      };

      this.loadTemplate = function() {
          var urlStem;
          if (templateId) {
              urlStem = '/tb-ui/authoring/rest/template/' + templateId;
          } else {
              urlStem = '/tb-ui/authoring/rest/weblog/' + weblogId + '/templatename/' + templateName + '/';
          }
          $http.get(contextPath + urlStem).then(
          function(response) {
              self.templateData = response.data;
              self.lastSavedRelativePath = self.templateData.relativePath;
          });
      };

      this.saveTemplate = function() {
          this.messageClear();
          var urlStem = '/tb-ui/authoring/rest/weblog/' + weblogId + '/templates';
          var templateToSend = JSON.parse(JSON.stringify(self.templateData));

          $http.post(contextPath + urlStem, JSON.stringify(templateToSend)).then(
           function(response) {
              templateId = response.data;
              self.loadTemplate();
              self.showSuccessMessage = true;
           },
           function(response) {
             if (response.status == 408)
               window.location.replace($('#refreshURL').attr('value'));
             if (response.status == 400) {
               self.errorObj = response.data;
             }
          })
      };

      this.messageClear = function() {
          this.showSuccessMessage = false;
          this.errorObj = null;
      }

      this.loadTemplate();

  }]);

tightblogApp.directive('templateTabs', function() {
    return {
        restrict: 'A',
        link: function(scope, elm, attrs) {
            var jqueryElm = $(elm[0]);
            $(jqueryElm).tabs()
        }
    };
})
