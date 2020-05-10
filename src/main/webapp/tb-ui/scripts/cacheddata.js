var dataObj = {
    urlRoot : contextPath + '/tb-ui/admin/rest/server/',
    metadata : { weblogList : [] },
    cacheData : {},
    weblogToReindex : null,
    successMessage : null,
    errorMessage : null
}

var vm = new Vue({
    data: dataObj,
    el: '#template',
    methods: {
        messageClear: function() {
            this.successMessage = null;
            this.errorObj = {};
        },
        loadWeblogList: function() {
            axios
            .get(this.urlRoot + 'visibleWeblogHandles')
            .then(response => {
                this.metadata.weblogList = response.data;
                if (this.metadata.weblogList && this.metadata.weblogList.length > 0) {
                    this.weblogToReindex = this.metadata.weblogList[0];
                }
            })
            .catch(error => this.commonErrorResponse(error, 'Weblog list cannot be retrieved.'));
        },
        loadCacheData: function() {
            axios
            .get(this.urlRoot + 'caches')
            .then(response => (this.cacheData = response.data))
            .catch(error => this.commonErrorResponse(error, null))
        },
        clearCache: function(cacheItem) {
            this.messageClear();
            axios
            .post(this.urlRoot + 'cache/' + cacheItem + '/clear')
            .then(response => {
                this.successMessage = response.data;
                this.loadCacheData();
            })
            .catch(error => this.commonErrorResponse(error, null))
        },
        resetHitCounts: function() {
            axios
            .post(this.urlRoot + 'resethitcount')
            .then(response => this.successMessage = response.data)
            .catch(error => this.commonErrorResponse(error, null))
        },
        reindexWeblog: function() {
            if (this.weblogToReindex) {
                this.messageClear();
                axios
                .post(this.urlRoot + 'weblog/' + this.weblogToReindex + '/rebuildindex')
                .then(response => this.successMessage = response.data)
                .catch(error => this.commonErrorResponse(error, null))
            }
        },
        commonErrorResponse: function(error, errorMsg) {
            if (error.response.status == 408) {
               window.location.replace($('#refreshURL').attr('value'));
            } else {
               this.errorMessage = errorMsg ? errorMsg : error.response.data.error;
            }
        }
    },
    mounted: function() {
        this.loadWeblogList();
        this.loadCacheData();
    }
})
