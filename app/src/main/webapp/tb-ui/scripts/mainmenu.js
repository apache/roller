$(function() {
  $("#confirm-resign").dialog({
    autoOpen: false,
    resizable: true,
    height:170,
    modal: true,
    buttons: [
      {
        text: msg.yesLabel,
        click: function() {
          var roleId = $(this).data('roleId');
          updateRole(roleId, 'detach');
          $(this).dialog("close");
        },
      },
      {
        text: msg.noLabel,
        click: function() {
          $(this).dialog("close");
        }
      }
    ]
  });

  $("#allBlogs").on('click', '.resign-link', function(e) {
    e.preventDefault();
    var idHaver = $(this).closest('tr');
    var roleId = idHaver.attr('id');
    var itemName = idHaver.attr('data-name');
    $('#confirm-resign')
      .dialog('option', 'title', itemName)
      .data('roleId', roleId).dialog('open');
  });

  $("#allBlogs").on('click', '.accept-button', function(e) {
     e.preventDefault();
     var span = $(this).closest('span');
     var roleId = span.attr('id');
     updateRole(roleId, 'attach');
  });

  $("#allBlogs").on('click', '.decline-button', function(e) {
     e.preventDefault();
     var span = $(this).closest('span');
     var roleId = span.attr('id');
     updateRole(roleId, 'detach');
  });

  function updateRole(roleId, command) {
    checkLoggedIn(function() {
       $.ajax({
          type: "POST",
          url: contextPath + '/tb-ui/authoring/rest/weblogrole/' + roleId + '/' + command,
          success: function(data, textStatus, xhr) {
             angular.element('#blog-list').scope().ctrl.loadItems();
             angular.element('#blog-list').scope().$apply();
          }
       });
    });
  }
});

var mainMenuApp = angular.module('mainMenuApp', []);

mainMenuApp.controller('MainMenuController', ['$http', function MainMenuController($http) {
    var self = this;
    this.loadItems = function() {
      $http.get(contextPath + '/tb-ui/authoring/rest/loggedinuser/weblogs').then(function(response) {
        self.roles = response.data;
      });
    };
    this.loadItems();
  }]);
