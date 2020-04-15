$(function() {
    $('#deleteCategoryModal').on('show.bs.modal', function(e) {
        var categoryId = $(e.relatedTarget).data('category-id');
        angular.element('#ngapp-div').scope().ctrl.selectedCategoryId = categoryId;
        angular.element('#ngapp-div').scope().$apply();

        // used by tmpl below
        var categoryName = $(e.relatedTarget).data('category-name');


        var modal = $(this)
        var tmpl = eval('`' + msg.confirmDeleteTmpl + '`')
        modal.find('#deleteCategoryModalTitle').html(tmpl);
    });

    $('#editCategoryModal').on('show.bs.modal', function(e) {
        $('#category-name').val('');

        var categoryId = $(e.relatedTarget).data('category-id');

        // used by tmpl below for renames
        var categoryName = $(e.relatedTarget).data('category-name');

        var action = $(e.relatedTarget).data('action');

        // populate edit modal with category-specific information
        var modal = $(this)
        var button = modal.find('button[id="saveButton"]');
        button.attr("data-category-id", categoryId);
        button.attr("data-action", action);
        var tmpl = eval('`' + (action == 'rename' ? msg.editTitleTmpl : msg.addTitle) + '`');
        modal.find('#editCategoryModalTitle').html(tmpl);
    });
});

tightblogApp.controller('PageController', ['$http', function PageController($http) {
    var self = this;
    this.errorObj = {};

    this.updateItem = function(obj) {
        this.messageClear();
        // https://stackoverflow.com/a/18030442/1207540
        var categoryId = obj.target.getAttribute("data-category-id");

        this.messageClear();
        if (this.itemToEdit.name) {
            this.itemToEdit.name = this.itemToEdit.name.replace(/[,%"/]/g,'');
            if (this.itemToEdit.name) {
                $http.put(contextPath + (categoryId ? '/tb-ui/authoring/rest/category/' + categoryId
                    : '/tb-ui/authoring/rest/categories?weblogId=' + actionWeblogId),
                    JSON.stringify(this.itemToEdit)).then(
                  function(response) {
                     $('#editCategoryModal').modal('hide');
                     self.itemToEdit = {};
                     self.loadItems();
                  },
                  self.commonErrorResponse
                )
            }
        }
    }

    this.deleteItem = function() {
      this.messageClear();
      $('#deleteCategoryModal').modal('hide');

      $http.delete(contextPath + '/tb-ui/authoring/rest/category/' + this.selectedCategoryId + '?targetCategoryId=' + this.targetCategoryId).then(
         function(response) {
           self.targetCategoryId = null;
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
        } else {
            self.errorObj = response.data;
         }
    }

    this.messageClear = function() {
        this.showUpdateErrorMessage = false;
        this.errorObj = {};
    }

    this.inputClear = function() {
        self.messageClear();
        this.itemToEdit = {};
    }

    this.messageClear();
    this.loadItems();
  }]);
