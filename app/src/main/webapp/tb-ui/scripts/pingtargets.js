$(function() {
  $("#confirm-delete-dialog").dialog({
     autoOpen: false,
     height:200,
     modal: true,
     buttons: [
        {
           text: msg.confirmLabel,
           click: function() {
              angular.element('#ngapp-div').scope().ctrl.deletePingTarget(encodeURIComponent($(this).data('pingTargetId')),
                encodeURIComponent($(this).data('pingTargetName')));
              angular.element('#ngapp-div').scope().$apply();
              $( this ).dialog( "close" );
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
  $("#edit-dialog").dialog({
     autoOpen: false,
     height: 210,
     width: 570,
     modal: true,
     buttons: [
        {
           text: msg.saveLabel,
           click: function() {
              var test = angular.element('#ngapp-div').scope().ctrl.pingTargetToEdit;
              if (test.name && test.pingUrl && test.name.length > 0 && test.pingUrl.length > 0) {
                  angular.element('#ngapp-div').scope().ctrl.updatePingTarget();
                  angular.element('#ngapp-div').scope().$apply();
              }
           }
        },
        {
           text: msg.cancelLabel,
           click: function() {
              angular.element('#ngapp-div').scope().ctrl.pingTargetToEdit = {};
              angular.element('#ngapp-div').scope().$apply();
              $(this).dialog("close");
           }
        }
      ]
  });
});

tightblogApp.controller('PageController', ['$http', function PageController($http) {
    var self = this;
    this.urlRoot = contextPath + '/tb-ui/admin/rest/';
    this.metadata = {};
    this.pingTargetToToggle = null;
    this.successMessage = null;
    this.errorMessage = null;
    this.showUpdateErrorMessage = false;
    this.pingTargetToEdit = {};

    this.loadItems = function() {
        $http.get(this.urlRoot + "pingtargets").then(
          function(response) {
            self.listData = response.data;
          },
          self.commonErrorResponse
        )
    };

    this.pingTest = function(pingTarget) {
        this.messageClear();
        $http.post(this.urlRoot + 'pingtargets/test/' + pingTarget.id).then(
          function(response) {
             self.successMessage = 'Result from ' + pingTarget.name + ': error? ' + response.data.error + '; message: ' + response.data.message;
          },
          self.commonErrorResponse
        )
    }

    this.toggleEnabled = function(pingTarget) {
        this.messageClear();
        $http.post(this.urlRoot + 'pingtargets/' + (pingTarget.enabled ? 'disable' : 'enable') + '/' + pingTarget.id).then(
          function(response) {
             self.loadItems();
          },
          self.commonErrorResponse
        )
    }

    this.deletePingTarget = function(pingTargetId, pingTargetName) {
        this.messageClear();
        $http.delete(this.urlRoot + 'pingtarget/' + pingTargetId).then(
          function(response) {
             self.successMessage = '[' + pingTargetName + '] deleted';
             self.loadItems();
          },
          self.commonErrorResponse
        )
    }

    this.updatePingTarget = function() {
        this.messageClear();
        $http.put(this.urlRoot + (this.pingTargetToEdit.id ? 'pingtarget/' + this.pingTargetToEdit.id : 'pingtargets'),
            JSON.stringify(this.pingTargetToEdit)).then(
          function(response) {
             $("#edit-dialog").dialog("close");
             self.pingTargetToEdit = {};
             self.loadItems();
          },
          function(response) {
            if (response.status == 408) {
               window.location.replace($('#refreshURL').attr('value'));
            } else if (response.status == 409) {
               self.showUpdateErrorMessage = true;
            }
          }
        )
    }

    this.editPingTarget = function(item) {
        angular.copy(item, this.pingTargetToEdit);
    }

    this.commonErrorResponse = function(response) {
        if (response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));
        } else if (response.status == 400) {
           self.errorMessage = response.data;
        }
    }

    this.messageClear = function() {
        this.successMessage = null;
        this.errorMessage = null;
        this.showUpdateErrorMessage = false;
    }

    this.loadItems();
}]);

tightblogApp.directive('updateDialog', function(){
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            var dialogId = '#' + attr.updateDialog;
            elem.bind('click', function(e) {
                $(dialogId).dialog("option", {"title" : msg.addTitle})
                           .dialog('open');
            });
        }
    };
});

tightblogApp.directive('confirmDeleteDialog', function(){
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            var dialogId = '#' + attr.confirmDeleteDialog;
            elem.bind('click', function(e) {
                $(dialogId).data('pingTargetId', attr.idToDelete)
                    .data('pingTargetName',  attr.nameToDelete)
                    .dialog("option", {"title" : attr.nameToDelete})
                    .dialog('open');
            });
        }
    };
});
