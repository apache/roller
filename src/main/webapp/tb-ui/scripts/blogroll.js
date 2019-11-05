$(function() {
    $('#editLinkModal').on('show.bs.modal', function(e) {
        var action = $(e.relatedTarget).data('action');

        var modal = $(this)
        var button = modal.find('button[id="saveButton"]');
        button.attr("data-action", action);
        var title = (action == 'edit') ? msg.editTitle : msg.addTitle;
        modal.find('#editLinkModalTitle').html(title);
    });
});

tightblogApp.controller('PageController', ['$http', function PageController($http) {
    var self = this;
    this.itemToEdit = {};
    this.errorObj = {};

    this.itemsSelected = function() {
        return $('input[name="idSelections"]:checked').size() > 0;
    }

    this.toggleCheckboxes = function(checked) {
        $('input[name="idSelections"]').each(function(){
            $(this).prop('checked', checked);
        });
    }

    this.loadItems = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/weblog/' + actionWeblogId + '/bookmarks').then(
         function(response) {
            self.items = response.data;
         },
         self.commonErrorResponse
      );
    };

    this.deleteLinks = function() {
        this.messageClear();
        $('#deleteLinksModal').modal('hide');

        var selectedLinkIds = [];
        $('input[name="idSelections"]:checked').each(function(){
            selectedLinkIds.push($(this).val());
        });

        $http.post(contextPath + '/tb-ui/authoring/rest/bookmarks/delete',
            JSON.stringify(selectedLinkIds)).then(
            function(response) {
                self.successMessage = selectedLinkIds.length + ' link(s) deleted';
                self.loadItems();
            }
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
                 $("#editLinkModal").modal("hide");
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
        } else {
           self.errorObj = response.data;
        }
    }

    this.messageClear = function() {
        this.errorObj = {};
    }

    this.loadItems();
  }]);
