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
   $.ajax({
      type: "GET",
      url: contextPath + '/tb-ui/admin/rest/server/webloglist',
      success: function(data, textStatus, xhr) {
        $.each(data, function(i, d) {
           $('#weblog-to-reindex').append('<option value="' + d + '">' + d + '</option>');
        });
      }
   });
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
   $("#clear-all-caches").click(function(e) {
      e.preventDefault();
      checkLoggedIn(function() {
        $('#confirm-resetall').dialog('open');
      });
   });
   $("#refresh-cache-stats").click(function(e) {
      e.preventDefault();
      refreshData();
   });
   $("#reset-hit-counts").click(function(e) {
      e.preventDefault();
      $('#success-message').hide();
      $('#failure-message').hide();
      checkLoggedIn(function() {
        $.ajax({
           type: "POST",
           url: contextPath + '/tb-ui/admin/rest/server/resethitcount',
           success: function(data, textStatus, xhr) {
             $('#success-message .textSpan').text(data);
             $('#success-message').show();
           },
           error: function(xhr, status, errorThrown) {
             $('#success-message .textSpan').text(xhr.responseText);
             $('#failure-message').show();
           }
        });
      });
    });
    $("#index-weblog").click(function(e) {
       e.preventDefault();
       $('#success-message').hide();
       $('#failure-message').hide();
       var weblogToIndex = $('#weblog-to-reindex').val();
       checkLoggedIn(function() {
         $.ajax({
            type: "POST",
            url: contextPath + '/tb-ui/admin/rest/server/weblog/' + weblogToIndex + '/rebuildindex',
            success: function(data, textStatus, xhr) {
              $('#success-message .textSpan').text(data);
              $('#success-message').show();
            },
            error: function(xhr, status, errorThrown) {
              $('#success-message .textSpan').text(xhr.responseText);
              $('#failure-message').show();
            }
         });
       });
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
