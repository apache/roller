$(function() {
   $.templates({
     tableTmpl: '#tableTemplate'
   });
   function updateView(data) {
     var html = $.render.tableTmpl(data);
     $("#tableBody").html(html);
     $(".rollertable tr").removeClass("altrow").filter(":even").addClass("altrow");
   }
   function refreshData() {
     checkLoggedIn(function() {
       $.ajax({
          type: "GET",
          url: contextPath + '/tb-ui/admin/rest/server/caches',
          success: function(data, textStatus, xhr) {
            updateView(data);
          }
       });
     });
   }
   refreshData();
   $("#confirm-resetall").dialog({
      autoOpen: false,
      resizable: true,
      height: 200,
      modal: true,
      buttons: [
         {
            text: msg.confirmLabel,
            click: function() {
              checkLoggedIn(function() {
                $.ajax({
                   type: "POST",
                   url: contextPath + '/tb-ui/admin/rest/server/caches/clear',
                   success: function(data, textStatus, xhr) {
                     updateView(data);
                   }
                });
              });
              $(this).dialog("close");
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
   $("#resetall-link").click(function(e) {
      e.preventDefault();
      checkLoggedIn(function() {
        $('#confirm-resetall').dialog('open');
      });
   });
   $("#refresh-link").click(function(e) {
      e.preventDefault();
      refreshData();
   });
   $("#tableBody").on('click', '.reset-link', function(e) {
      e.preventDefault();
      var tr = $(this).closest('tr');
      var actionId = tr.attr('id');
      var itemName = tr.find('td.title-cell').text();
      checkLoggedIn(function() {
        $.ajax({
           type: "POST",
           url: contextPath + '/tb-ui/admin/rest/server/cache/' + actionId + '/clear',
           success: function(data, textStatus, xhr) {
             var html = $.render.tableTmpl(data);
             tr.prop('outerHTML', html);
             $(".rollertable tr").removeClass("altrow").filter(":even").addClass("altrow");
           }
        });
      });
   });
});
