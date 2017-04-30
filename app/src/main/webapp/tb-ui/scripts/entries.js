  $(function() {
    $("#confirm-delete").dialog({
      autoOpen: false,
      resizable: false,
      height:170,
      modal: true,
      buttons: [
      {
        text: msg.deleteLabel,
        click: function() {
            angular.element('#ngapp-div').scope().ctrl.deleteWeblogEntry(encodeURIComponent($(this).data('entryId')));
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

tightblogApp.controller('PageController', ['$http', function PageController($http) {
    var self = this;
    this.lookupFields = {};
    this.searchParams = {
      sortBy : "PUBLICATION_TIME"
    };
    this.entriesData = {};
    this.errorObj = null;
    this.pageNum = 0;
    this.urlRoot = contextPath + '/tb-ui/authoring/rest/weblogentries/';

    this.deleteWeblogEntry = function(entryId) {
        $http.delete(this.urlRoot + entryId).then(
          function(response) {
             self.loadEntries();
          },
          self.commonErrorResponse
        )
    }

    this.loadLookupFields = function() {
        $http.get(this.urlRoot + weblogId + '/searchfields'  ).then(
        function(response) {
            self.lookupFields = response.data;
          },
          self.commonErrorResponse
        )
    };

    this.dateToSeconds = function(dateStr, addOne) {
        if (dateStr) {
            return Math.floor( Date.parse(dateStr) / 1000 ) + (addOne ? 1440 * 60 - 1 : 0);
        } else {
            return null;
        }
    }

    this.loadEntries = function() {
        this.searchParams.startDate = this.dateToSeconds(this.searchParams.startDateString, false);
        this.searchParams.endDate = this.dateToSeconds(this.searchParams.endDateString, true);

        $http.post(this.urlRoot + weblogId + '/page/' + this.pageNum, JSON.stringify(this.searchParams)).then(
        function(response) {
            self.entriesData = response.data;
          },
          self.commonErrorResponse
        )
    };

    this.previousPage = function() {
        this.pageNum--;
        this.loadEntries();
    };

    this.nextPage = function() {
        this.pageNum++;
        this.loadEntries();
    };

    this.commonErrorResponse = function(response) {
        if (response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));
        } else if (response.status == 400) {
           self.errorMsg = response.data;
        }
    }

    this.loadLookupFields();
    this.loadEntries();

  }]);

tightblogApp.directive('confirmDeleteDialog', function(){
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            var dialogId = '#' + attr.confirmDeleteDialog;
            elem.bind('click', function(e) {
                $(dialogId).data('entryId',  attr.idToDelete).dialog('open');
            });
        }
    };
});
