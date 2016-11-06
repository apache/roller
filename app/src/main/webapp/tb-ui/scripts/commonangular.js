var tightblogApp = angular.module('tightblogApp', []);

tightblogApp.config(['$httpProvider', function($httpProvider) {
    $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
    var header = $("meta[name='_csrf_header']").attr("content");
    var token = $("meta[name='_csrf']").attr("content");
    $httpProvider.defaults.headers.delete = {};
    $httpProvider.defaults.headers.delete[header] = token;
    $httpProvider.defaults.headers.post[header] = token;
    $httpProvider.defaults.headers.put[header] = token;
}]);

