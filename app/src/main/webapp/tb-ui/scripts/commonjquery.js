$(function() {
   var token = $("meta[name='_csrf']").attr("content");
   var header = $("meta[name='_csrf_header']").attr("content");
   $(document).ajaxSend(function(event, xhr, settings) {
     if (settings.type == 'POST' || settings.type == 'PUT' || settings.type == 'DELETE') {
        xhr.setRequestHeader(header, token);
     }
   });
});
