  $(function() {
    $('#deleteEntryModal').on('show.bs.modal', function(e) {
        // get data-id attribute of the clicked element
        var id = $(e.relatedTarget).data('id');
        // ${title} used in template below
        var title = $(e.relatedTarget).data('title');

        // populate delete modal with tag-specific information
        var modal = $(this)
        modal.find('button[id="deleteButton"]').attr("data-id", id);
        var tmpl = eval('`' + msg.confirmDeleteTmpl + '`')
        modal.find('#confirmDeleteMsg').html(tmpl);
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

    this.deleteWeblogEntry = function(obj) {
        $('#deleteEntryModal').modal('hide');

        // https://stackoverflow.com/a/18030442/1207540
        var entryId = obj.target.getAttribute("data-id");

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
        } else {
           self.errorMsg = response.data;
        }
    }

    this.loadLookupFields();
    this.loadEntries();

}]);
