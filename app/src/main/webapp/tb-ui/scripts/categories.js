$(function() {
   $("#category-edit").dialog({
      autoOpen: false,
      height: 200,
      modal: true,
      buttons: [
         {
            text: msg.saveLabel,
            click: function() {
               var idToUpdate = $(this).data('categoryId');
               var newName = $('#category-edit-name').val().trim();
               var dg = $(this);
               if (newName.length > 0) {
                  $.ajax({
                     type: "PUT",
                     url: contextPath + ((idToUpdate == '') ? '/tb-ui/authoring/rest/categories?weblog=' + $("#actionWeblog").val() : '/tb-ui/authoring/rest/category/' + idToUpdate),
                     data: JSON.stringify(newName),
                     contentType: "application/json",
                     processData: "false",
                     success: function(data, textStatus, xhr) {
                       dg.dialog("close");
                       angular.element('#category-list').scope().ctrl.loadCategories();
                       angular.element('#category-list').scope().$apply();
                     },
                     error: function(xhr, status, errorThrown) {
                        if (xhr.status in this.statusCode)
                           return;
                        $('#category-edit-error').css("display", "inline");
                     }
                  });
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

   $("#category-remove").dialog({
      autoOpen: false,
      height: 275,
      modal: true,
      buttons: [
         {
            text: msg.confirmLabel,
            click: function() {
               var idToRemove = $(this).data('categoryId');
               var targetCategoryId = $('#category-remove-targetlist').val();
               var dg = $(this);
               $.ajax({
                  type: "DELETE",
                  url: contextPath + '/tb-ui/authoring/rest/category/' + idToRemove + '?targetCategoryId=' + targetCategoryId,
                  success: function(data, textStatus, xhr) {
                     dg.dialog("close");
                     angular.element('#category-list').scope().ctrl.loadCategories();
                     angular.element('#category-list').scope().$apply();
                  }
               });
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

   $("#tableBody").on('click', '.edit-link', function(e) {
      e.preventDefault();
      var tr = $(this).closest('tr');
      var idToEdit = tr.attr('id');
      $('#category-edit').dialog('option', 'title', msg.editTitle);
      $('#category-edit-name').val(tr.find('td.category-name').html());
      $('#category-edit-error').css("display", "none");
      checkLoggedIn(function() {
         $('#category-edit').data('categoryId', idToEdit).dialog('open');
      });
   });

   $("#add-link").click(function(e) {
      e.preventDefault();
      $('#category-edit').dialog('option', 'title', msg.addTitle)
      $('#category-edit-name').val('');
      $('#category-edit-error').css("display", "none");
      checkLoggedIn(function() {
         $('#category-edit').data('categoryId', '').dialog('open');
      });
   });

   $("#tableBody").on('click', '.delete-link', function(e) {
      e.preventDefault();
      var tr = $(this).closest('tr');
      var idToRemove = tr.attr('id');
      $.ajax({
         type: "GET",
         url: contextPath + '/tb-ui/authoring/rest/categories/inuse?categoryId=' + idToRemove,
         success: function(data, textStatus, xhr) {
            if (data == true) {
               $('#category-remove-targetlist').empty()
               $.ajax({
                  type: "GET",
                  url: contextPath + '/tb-ui/authoring/rest/categories?weblog=' + $("#actionWeblog").val() + '&skipCategoryId=' + idToRemove,
                  dataType: "json",
                  success: function(data, textStatus, xhr) {
                     $.each(data, function(i, d) {
                        $('#category-remove-targetlist').append('<option value="' + d.id + '">' + d.name + '</option>');
                     });
                  }
               });
               $('#category-remove-mustmove').css("display", "inline");
            } else {
               $('#category-remove-mustmove').css("display", "none");
            }
            $('#category-remove').data('categoryId', idToRemove).dialog('open');
         }
      });
   });
});

var tightBlogApp = angular.module('tightBlogApp', []);

tightBlogApp.controller('CategoryController', ['$http', function CategoryController($http) {
    var self = this;
    this.loadCategories = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/categories?weblog=' + $("#actionWeblog").val()).then(function(response) {
        self.categories = response.data;
      });
    };
    this.loadCategories();
  }]);
