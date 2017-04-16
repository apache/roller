$(function() {
   $("#confirm-delete").dialog({
      autoOpen: false,
      resizable: true,
      height: 200,
      modal: true,
      buttons: [
         {
            text: msg.confirmLabel,
            click: function() {
               var idsToRemove = [];
               $('input[name="idSelections"]:checked').each(function(){
                  idsToRemove.push($(this).val());
               });
               $(this).dialog("close");
               if (idsToRemove.length > 0) {
                  for (i = 0; i < idsToRemove.length; i++) {
                    angular.element('#ngapp-div').scope().ctrl.deleteItem(idsToRemove[i]);
                  }
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
      height: 270,
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

    this.loadItems = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + actionWeblogId + '/bookmarks').then(
         function(response) {
            self.items = response.data;
         },
         self.commonErrorResponse
      );
    };

    this.deleteItem = function(itemId) {
      $http.delete(contextPath + '/tb-ui/authoring/rest/bookmark/' + itemId).then(
         function(response) {
           self.loadItems();
         },
         self.commonErrorResponse
      );
    }

    this.editItem = function(item) {
        angular.copy(item, this.itemToEdit);
    }

    this.addItem = function() {
        this.itemToEdit = {};
    }

    this.updateItem = function() {
        this.messageClear();
        if (this.itemToEdit.name && this.itemToEdit.url) {
            $http.put(contextPath + (this.itemToEdit.id ? '/tb-ui/authoring/rest/bookmark/' + this.itemToEdit.id
                : '/tb-ui/authoring/rest/bookmarks?weblogId=' + actionWeblogId),
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
                if ($('input[name="idSelections"]:checked').size() > 0) {
                    $(dialogId).dialog('open');
                }
            });
        }
    };
});
