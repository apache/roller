$(function() {
   $("#delete-dialog").dialog({
      autoOpen: false,
      height: 255,
      modal: true,
      buttons: [
         {
            text: msg.confirmLabel,
            click: function() {
                if (angular.element('#ngapp-div').scope().ctrl.targetCategoryId) {
                    $(this).dialog("close");
                    angular.element('#ngapp-div').scope().ctrl.deleteItem();
                    angular.element('#ngapp-div').scope().$apply();
                }
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
      height: 200,
      width: 570,
      modal: true,
      buttons: [
         {
            text: msg.saveLabel,
            click: function() {
               angular.element('#ngapp-div').scope().ctrl.updateItem();
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
});

tightblogApp.controller('PageController', ['$http', function PageController($http) {
    var self = this;
    this.itemToEdit = {};
    this.showUpdateErrorMessage = false;

    this.formatDate = function(inDate) {
        if (inDate) {
            return inDate[0] + '-' + ("00" + inDate[1]).slice(-2) + '-' + ("00" + inDate[2]).slice(-2);
        } else {
            return '';
        }
    }

    this.setDeleteItem = function(item) {
        this.targetCategoryId = null;
        this.itemToDelete = item;
    }

    this.setEditItem = function(item) {
        angular.copy(item, this.itemToEdit);
    }

    this.addItem = function() {
        this.itemToEdit = {};
    }

    this.updateItem = function() {
        this.messageClear();
        if (this.itemToEdit.name) {
            this.itemToEdit.name = this.itemToEdit.name.replace(/[,%"/]/g,'');
            if (this.itemToEdit.name) {
                $http.put(contextPath + (this.itemToEdit.id ? '/tb-ui/authoring/rest/category/' + this.itemToEdit.id
                    : '/tb-ui/authoring/rest/categories?weblogId=' + actionWeblogId),
                    JSON.stringify(this.itemToEdit)).then(
                  function(response) {
                     $("#edit-dialog").dialog("close");
                     self.itemToEdit = {};
                     self.loadItems();
                  },
                  self.commonErrorResponse
                )
            }
        }
    }

    this.deleteItem = function() {
      $http.delete(contextPath + '/tb-ui/authoring/rest/category/' + this.itemToDelete.id + '?targetCategoryId=' + this.targetCategoryId).then(
         function(response) {
           self.loadItems();
         },
         self.commonErrorResponse
      );
    }

    this.loadItems = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/categories?weblogId=' + actionWeblogId).then(
         function(response) {
            self.items = response.data;
         },
         self.commonErrorResponse
      );
    };

    this.commonErrorResponse = function(response) {
        if (response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));
        } else if (response.status == 409) {
           self.showUpdateErrorMessage = true;
        }
    }

    this.messageClear = function() {
        this.showUpdateErrorMessage = false;
    }

    this.loadItems();
  }]);

function showEditDialog(title) {
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            var dialogId = '#edit-dialog';
            elem.bind('click', function(e) {
                $(dialogId).dialog("option", {"title" : title})
                           .dialog('open');
            });
        }
    };
}

tightblogApp.directive('addDialog', function(){return showEditDialog(msg.addTitle)});

tightblogApp.directive('editDialog', function(){return showEditDialog(msg.editTitle)});

tightblogApp.directive('confirmDeleteDialog', function(){
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            var dialogId = '#' + attr.confirmDeleteDialog;
            elem.bind('click', function(e) {
                $(dialogId).dialog("option", {"title" : "Delete Category " + attr.nameToDelete})
                           .dialog('open');
            });
        }
    };
});

