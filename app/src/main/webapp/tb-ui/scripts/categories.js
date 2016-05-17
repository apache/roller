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
               if (newName.length > 0) {
                  $.ajax({
                     type: "PUT",
                     url: contextPath + ((idToUpdate == '') ? '/tb-ui/authoring/rest/categories?weblog=' + $("#actionWeblog").val() : '/tb-ui/authoring/rest/category/' + idToUpdate),
                     data: JSON.stringify(newName),
                     contentType: "application/json",
                     processData: "false",
                     success: function(data, textStatus, xhr) {
                        if (idToUpdate == '') {
                           document.categoriesForm.submit();
                        } else {
                           $('#catname-' + idToUpdate).text(newName)
                           $('#catid-' + idToUpdate).attr('data-name', newName)
                           $("#category-edit").dialog().dialog("close");
                        }
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
               $.ajax({
                  type: "DELETE",
                  url: contextPath + '/tb-ui/authoring/rest/category/' + idToRemove + '?targetCategoryId=' + targetCategoryId,
                  success: function(data, textStatus, xhr) {
                     document.categoriesForm.submit();
                  }
               });
            },
         },
         {
            text: msg.cancelLabel,
            click: function() {
               $(this).dialog("close");
            }
         }
      ]
   });

   $(".edit-link").click(function(e) {
      e.preventDefault();
      $('#category-edit').dialog('option', 'title', msg.editTitle)
      $('#category-edit-name').val($(this).attr("data-name")).select();
      $('#category-edit-error').css("display", "none");
      var dataId = $(this).attr("data-id");
      $.get(contextPath + '/tb-ui/authoring/rest/categories/loggedin', function() {
         $('#category-edit').data('categoryId', dataId).dialog('open');
      });
   });

   $("#add-link").click(function(e) {
      e.preventDefault();
      $('#category-edit').dialog('option', 'title', msg.addTitle)
      $('#category-edit-name').val('');
      $('#category-edit-error').css("display", "none");
      $.get(contextPath + '/tb-ui/authoring/rest/categories/loggedin', function() {
         $('#category-edit').data('categoryId', '').dialog('open');
      });
   });

   $(".remove-link").click(function(e) {
      e.preventDefault();
      var idToRemove = $(this).attr("data-id");
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
