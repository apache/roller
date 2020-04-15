$(function() {
    $('#changeTagModal').on('show.bs.modal', function(e) {
        //get data-id attribute of the clicked element
        var tagName = $(e.relatedTarget).attr('current-tag');
        var action = $(e.relatedTarget).attr('action');

        // populate delete modal with tag-specific information
        var modal = $(this)
        var button = modal.find('button[id="changeButton"]');
        button.data("currentTag", tagName);
        button.data("action", action);
        var tmpl = eval('`' + (action == 'replace' ? msg.replaceTagTitleTmpl : msg.addTagTitleTmpl) + '`')
        modal.find('#changeTagModalTitle').html(tmpl);
    });
});

tightblogApp.controller('PageController', ['$http',
    function PageController($http) {
    var self = this;
    this.tagData = {};
    this.errorObj = null;
    this.pageNum = 0;
    this.urlRoot = contextPath + '/tb-ui/authoring/rest/tags/';
    this.resultsMap = {};

    this.tagsSelected = function() {
        return $('input[name="idSelections"]:checked').size() > 0;
    }

    this.toggleCheckboxes = function(checked) {
        $('input[name="idSelections"]').each(function(){
            $(this).prop('checked', checked);
        });
    }

    this.deleteTags = function() {
        this.messageClear();
        $('#deleteTagsModal').modal('hide');

        var selectedTagNames = [];
        $('input[name="idSelections"]:checked').each(function(){
            selectedTagNames.push($(this).val());
        });

        $http.post(contextPath + '/tb-ui/authoring/rest/tags/weblog/' + weblogId + '/delete',
            JSON.stringify(selectedTagNames)).then(
            function(response) {
                self.successMessage = selectedTagNames.length + ' tag(s) deleted';
                self.loadTags();
            }
        );
    }

    this.tagUpdate = function() {
        this.messageClear();
        var changeButton = $('#changeButton');
        var currentTag = changeButton.data('currentTag');

        if (changeButton.data('action') == 'replace') {
            this.replaceTag(currentTag, this.newTagName);
        } else {
            this.addTag(currentTag, this.newTagName);
        }
        $('#changeTagModal').modal('hide');
        this.inputClear();
    }

    this.addTag = function(currentTag, newTag) {
        this.messageClear();
        $http.post(this.urlRoot + 'weblog/' + weblogId + '/add/currenttag/' + currentTag + '/newtag/' + newTag).then(
          function(response) {
             self.resultsMap = response.data;
             self.successMessage = 'Added [' + newTag + '] to ' + self.resultsMap.updated + ' entries having ['
                + currentTag + (self.resultsMap.unchanged > 0 ? '] (' + self.resultsMap.unchanged
                + ' already had [' + newTag + '])' : ']');
             self.loadTags();
          },
          self.commonErrorResponse
        )
    }

    this.replaceTag = function(currentTag, newTag) {
        this.messageClear();
        $http.post(this.urlRoot + 'weblog/' + weblogId + '/replace/currenttag/' + currentTag + '/newtag/' + newTag).then(
          function(response) {
             self.resultsMap = response.data;
             self.successMessage = 'Replaced [' + currentTag + '] with [' + newTag + '] in ' + self.resultsMap.updated
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
        this.messageClear();
        this.pageNum--;
        this.loadTags();
    };

    this.nextPage = function() {
        this.messageClear();
        this.pageNum++;
        this.loadTags();
    };

    this.commonErrorResponse = function(response) {
        if (response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));
        }  else {
           self.errorObj = response.data;
        }
    }

    this.messageClear = function() {
        self.successMessage = '';
        this.errorObj = {};
    }

    this.inputClear = function() {
        this.newTagName = '';
    }

    this.loadTags();

  }]);
