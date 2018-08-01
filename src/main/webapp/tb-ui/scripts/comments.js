$(function() {
    $( "#startDateString" ).datepicker({
        showOn: "button",
        buttonImage: "../../../images/calendar.png",
        buttonImageOnly: true,
        changeMonth: true,
        changeYear: true
    });

    $( "#endDateString" ).datepicker({
        showOn: "button",
        buttonImage: "../../../images/calendar.png",
        buttonImageOnly: true,
        changeMonth: true,
        changeYear: true
    });
});


tightblogApp.requires.push('ngSanitize');

tightblogApp.controller('PageController', ['$http', '$interpolate', '$sce', '$filter',
    function PageController($http, $interpolate, $sce, $filter) {
    var self = this;
    this.lookupFields = {};
    this.searchParams = {};
    this.commentData = {};
    this.errorObj = null;
    this.pageNum = 0;
    this.urlRoot = contextPath + '/tb-ui/authoring/rest/comments/';

    this.loadLookupFields = function() {
        $http.get(this.urlRoot + weblogId + '/searchfields'  ).then(
        function(response) {
            self.lookupFields = response.data;
          },
          self.commonErrorResponse
        )
    };

    this.editComment = function(comment) {
        self.errorMsg = null;
        comment.editable = true;
    }

    this.saveComment = function(comment) {
        if (!comment.editable) {
            return;
        }
        self.errorMsg = null;
        comment.editable = false;

        $http.put(this.urlRoot + comment.id + '/content' , comment.content).then(
        function(response) {
            // update original value, in case of a subsequent edit & cancel
            var oldItem = $filter('filter')(self.originalCommentData.comments, {id : comment.id}, true);
            if (oldItem && oldItem.length) {
                oldItem[0].content = comment.content;
            }
          },
          self.commonErrorResponse
        )
    }

    this.editCommentCancel = function(comment) {
        comment.editable = false;
        self.errorMsg = null;
        var oldItem = $filter('filter')(self.originalCommentData.comments, {id : comment.id}, true);
        if (oldItem && oldItem.length) {
            comment.content = oldItem[0].content;
        }
    }

    this.approveComment = function(comment) {
        $http.post(this.urlRoot + comment.id + '/approve').then(
          function(response) {
             self.errorMsg = null;
             comment.status = 'APPROVED';
          },
          self.commonErrorResponse
        )
    }

    this.hideComment = function(comment) {
        $http.post(this.urlRoot + comment.id + '/hide').then(
          function(response) {
             self.errorMsg = null;
             comment.status = 'DISAPPROVED';
          },
          self.commonErrorResponse
        )
    }

    this.deleteComment = function(comment) {
        $http.delete(this.urlRoot + comment.id).then(
          function(response) {
             self.loadComments();
          },
          self.commonErrorResponse
        )
    }

    this.getCommentHeader = function(comment) {
        return $sce.trustAsHtml($interpolate(commentHeaderTmpl)
            ({name:comment.name, email:comment.email, remoteHost:comment.remoteHost}));
    }

    this.dateToSeconds = function(dateStr, addOne) {
        if (dateStr) {
            return Math.floor( Date.parse(dateStr) / 1000 ) + (addOne ? 1440 * 60 - 1 : 0);
        } else {
            return null;
        }
    }

    this.loadComments = function() {
        self.errorMsg = null;
        var urlToUse = this.urlRoot + weblogId + '/page/' + this.pageNum;
        if (entryId) {
            urlToUse += "?entryId=" + entryId
            entryTitleMsg = ' ';
        }

        this.searchParams.startDate = this.dateToSeconds(this.searchParams.startDateString, false);
        this.searchParams.endDate = this.dateToSeconds(this.searchParams.endDateString, true);

        $http.post(urlToUse, JSON.stringify(this.searchParams)).then(
        function(response) {
            self.commentData = response.data;
            self.originalCommentData = angular.copy(self.commentData);
            if (entryId) {
                self.entryTitleMsg = $sce.trustAsHtml($interpolate(entryTitleTmpl)({entryTitle:self.commentData.entryTitle}));
            }
            self.nowShowingMsg = $sce.trustAsHtml($interpolate(nowShowingTmpl)({count:self.commentData.comments.length}));
          },
          self.commonErrorResponse
        )
    };

    this.previousPage = function() {
        this.pageNum--;
        this.loadComments();
    };

    this.nextPage = function() {
        this.pageNum++;
        this.loadComments();
    };

    this.commonErrorResponse = function(response) {
        if (response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));
        } else if (response.status == 400) {
           self.errorMsg = response.data;
        }
    }

    this.loadLookupFields();
    this.loadComments();

  }]);
