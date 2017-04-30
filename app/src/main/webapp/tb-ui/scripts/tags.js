  $(function() {
    $("#confirm-delete-dialog").dialog({
      autoOpen: false,
      resizable: false,
      height:170,
      modal: true,
      buttons: [
      {
        text: msg.deleteLabel,
        click: function() {
            angular.element('#ngapp-div').scope().ctrl.deleteTag(encodeURIComponent($(this).data('tagName')));
            angular.element('#ngapp-div').scope().$apply();
            $( this ).dialog( "close" );
        }
      },
      {
        text: msg.cancelLabel,
        click: function() {
          $( this ).dialog( "close" );
        }
      }
      ]
    });

    $("#change-tag-dialog").dialog({
      autoOpen: false,
      resizable: false,
      height:170,
      modal: true,
      buttons: [
      {
        text: msg.updateLabel,
        click: function() {
            var currentTag = $(this).data('currentTag');
            var newTag = $('#new-tag').val().trim();
            if ($(this).data('action') == 'rename') {
                angular.element('#ngapp-div').scope().ctrl.renameTag(currentTag, newTag);
            } else {
                angular.element('#ngapp-div').scope().ctrl.addTag(currentTag, newTag);
            }
            angular.element('#ngapp-div').scope().$apply();
            $( this ).dialog( "close" );
            $('#new-tag').val('');
        }
      },
      {
        text: msg.cancelLabel,
        click: function() {
          $( this ).dialog( "close" );
          $('#new-tag').val('');
        }
      }
      ]
    });

});

tightblogApp.controller('PageController', ['$http', function PageController($http) {
    var self = this;
    this.tagData = {};
    this.errorObj = null;
    this.pageNum = 0;
    this.urlRoot = contextPath + '/tb-ui/authoring/rest/tags/';
    this.resultsMap = {};

    this.deleteTag = function(tagName) {
        $http.delete(this.urlRoot + 'weblog/' + weblogId + '/tagname/' + tagName).then(
          function(response) {
             self.resultsMessage = '[' + tagName + '] deleted';
             self.loadTags();
          },
          self.commonErrorResponse
        )
    }

    this.addTag = function(currentTag, newTag) {
        $http.post(this.urlRoot + 'weblog/' + weblogId + '/add/currenttag/' + currentTag + '/newtag/' + newTag).then(
          function(response) {
             self.resultsMap = response.data;
             self.resultsMessage = 'Added [' + newTag + '] to ' + self.resultsMap.updated + ' entries having ['
                + currentTag + (self.resultsMap.unchanged > 0 ? '] (' + self.resultsMap.unchanged
                + ' already had [' + newTag + '])' : ']');
             self.loadTags();
          },
          self.commonErrorResponse
        )
    }

    this.renameTag = function(currentTag, newTag) {
        $http.post(this.urlRoot + 'weblog/' + weblogId + '/rename/currenttag/' + currentTag + '/newtag/' + newTag).then(
          function(response) {
             self.resultsMap = response.data;
             self.resultsMessage = 'Renamed [' + currentTag + '] to [' + newTag + '] in ' + self.resultsMap.updated
                + ' entries' + (self.resultsMap.unchanged > 0 ? ', deleted [' + currentTag + '] from '
                + self.resultsMap.unchanged + ' entries already having [' + newTag + ']': '');
             self.loadTags();
          },
          self.commonErrorResponse
        )
    }

    this.loadTags = function() {
        $http.get(this.urlRoot + weblogId + '/page/' + this.pageNum).then(
        function(response) {
            self.tagData = response.data;
          },
          self.commonErrorResponse
        )
    };

    this.previousPage = function() {
        self.resultsMessage = '';
        this.pageNum--;
        this.loadTags();
    };

    this.nextPage = function() {
        self.resultsMessage = '';
        this.pageNum++;
        this.loadTags();
    };

    this.commonErrorResponse = function(response) {
        if (response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));
        } else if (response.status == 400) {
           self.resultsMessage = response.data;
        }
    }

    this.loadTags();

  }]);

function showTagDialog(action, titleStub) {
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            elem.bind('click', function(e) {
                $('#change-tag-dialog').data('currentTag',  attr.currentTag)
                           .data('action',  action)
                           .dialog("option", {"title" : titleStub + attr.currentTag})
                           .dialog('open');
            });
        }
    };
}

tightblogApp.directive('addTagDialog', function(){return showTagDialog('add', 'Add to ')});

tightblogApp.directive('renameTagDialog', function(){return showTagDialog('rename', 'Rename ')});

tightblogApp.directive('confirmDeleteDialog', function(){
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            var dialogId = '#' + attr.confirmDeleteDialog;
            elem.bind('click', function(e) {
                $(dialogId).data('tagName',  attr.nameToDelete)
                    .dialog("option", {"title" : attr.nameToDelete})
                    .dialog('open');
            });
        }
    };
});
