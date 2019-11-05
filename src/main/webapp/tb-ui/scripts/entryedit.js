function split( val ) {
    return val.split( / \s*/ );
}

function extractLast( term ) {
    return split( term ).pop();
}

function onClickAddImage(){
    $("#mediaFileChooser" ).attr('src', mediaFileChooserUrl);
    $("#insertMediaFileModal").modal('show');
}

function onSelectMediaFile(name, url, alt, title, anchor, isImage) {
    $("#insertMediaFileModal").modal('hide');
    $("#mediaFileChooser").attr('src','about:blank');
    var anchorTag;
    if (isImage === true) {
        anchorTag = (anchor ? '<a href="' + anchor + '">' : '') +
        '<img src="' + url + '"' +
        ' alt="' + (alt ? alt : name) + '"' +
         (title ? ' title="' + title + '"' : '') +
         '>' +
        (anchor ? '</a>' : '');
    } else {
        anchorTag = '<a href="' + url + '"' +
         (title ? ' title="' + title + '"' : '') +
        '>' + (alt ? alt : name) + '</a>';
    }
    angular.element('#ngapp-div').scope().ctrl.insertMediaFile(anchorTag);
    angular.element('#ngapp-div').scope().$apply();
}

$(function() {
    $( "#accordion" ).accordion({});

    $( "#publishDateString" ).datepicker({
        showOn: "button",
        buttonImage: "../../../images/calendar.png",
        buttonImageOnly: true,
        changeMonth: true,
        changeYear: true
    });

    $('#deleteEntryModal').on('show.bs.modal', function(e) {
        //get data-id attribute of the clicked element
        var title = $(e.relatedTarget).attr('data-title');

        // populate delete modal with tag-specific information
        var modal = $(this)
        var tmpl = eval('`' + msg.confirmDeleteTmpl + '`')
        modal.find('#confirmDeleteMsg').html(tmpl);
    });

    // tag autocomplete
    $( "#tags" )
    // don't navigate away from the field on tab when selecting an item
    .bind( "keydown", function( event ) {
        if ( event.keyCode === $.ui.keyCode.TAB && $( this ).autocomplete( "instance" ).menu.active ) {
            event.preventDefault();
        }
    })
    .autocomplete({
        delay: 500,
        source: function(request, response) {
            $.getJSON(contextPath + "/tb-ui/authoring/rest/weblogentries/" + weblogId + "/tagdata",
            { prefix: extractLast( request.term ) },
            function(data) {
                response($.map(data.tagcounts, function (dataValue) {
                    return {
                        value: dataValue.name
                    };
                }))
            })
        },
        focus: function() {
            // prevent value inserted on focus
            return false;
        },
        select: function( event, ui ) {
            var terms = split( this.value );
            // remove the current input
            terms.pop();
            // add the selected item
            terms.push( ui.item.value );
            // add placeholder to get the space at the end
            terms.push( "" );
            this.value = terms.join( " " );
            return false;
        }
    });
});

tightblogApp.requires.push('ngSanitize');

tightblogApp.controller('PageController', ['$http', '$interpolate', '$sce',
    function PageController($http, $interpolate, $sce) {
        var self = this;
        this.recentEntries = {};
        this.urlRoot = contextPath + '/tb-ui/authoring/rest/weblogentries/';
        this.entry = { commentCountIncludingUnapproved : 0, category : {} };
        this.quillEditor = null;
        this.errorObj = {};

        this.getRecentEntries = function(entryType) {
            $http.get(this.urlRoot + weblogId + '/recententries/' + entryType).then(
              function(response) {
                 self.recentEntries[entryType] = response.data;
              }
            )
        };

        this.initializeQuill = function() {
            this.quillEditor = new Quill("#editor_quill", {
              modules: {
                'toolbar': { container: '#toolbar_quill' },
                'link-tooltip': true
              },
              theme: 'snow'
            });
        }

        this.loadMetadata = function() {
            $http.get(this.urlRoot + weblogId + '/entryeditmetadata').then(
            function(response) {
                self.metadata = response.data;
                if (!entryId) {
                    // new entry init
                    self.entry.category.id = Object.keys(self.metadata.categories)[0];
                    self.entry.commentDays = "" + self.metadata.defaultCommentDays;
                    self.entry.editFormat = self.metadata.defaultEditFormat;
                }
                // initialize RTE
                if ('RICHTEXT' == self.entry.editFormat) {
                    self.initializeQuill();
                }
              },
              self.commonErrorResponse
            )
        };

        this.insertMediaFile = function(anchorTag) {
            if ('RICHTEXT' == self.entry.editFormat) {
                var html = $("#ql-editor-1").html();
                self.entry.text = html + anchorTag;
                $("#ql-editor-1").html(self.entry.text);
            } else {
                if (self.entry.text) {
                    self.entry.text += anchorTag;
                } else {
                    self.entry.text = anchorTag;
                }
            }
        }

        this.getEntry = function() {
            $http.get(this.urlRoot + entryId).then(
              function(response) {
                 self.entry = response.data;
                 self.commentCountMsg = $sce.trustAsHtml($interpolate(msg.commentCountTmpl)
                    ({commentCount:self.entry.commentCountIncludingUnapproved}));
                 self.entry.commentDays = "" + self.entry.commentDays;

                 // move into RTE.
                 if ('RICHTEXT' == self.entry.editFormat) {
                     if (!self.quillEditor) {
                         self.initializeQuill();
                     }
                     $("#ql-editor-1").html(self.entry.text);
                 }
              }
            )
        };

        this.saveEntry = function(saveType) {
            this.messageClear();
            var urlStem = weblogId + '/entries';

            oldStatus = self.entry.status;
            self.entry.status = saveType;

            // get text from RTE
            if ('RICHTEXT' == self.entry.editFormat) {
                var html = $("#ql-editor-1").html();
                self.entry.text = html;
            }

            $http.post(self.urlRoot + urlStem, JSON.stringify(self.entry)).then(
              function(response) {
                entryId = response.data.entryId;
                self.successMessage = response.data.message;
                self.errorObj = {};
                self.loadRecentEntries();
                self.getEntry();
                window.scrollTo(0, 0);
              },
             function(response) {
               self.entry.status = oldStatus;
               if (response.status == 408) {
                 self.errorObj.errorMessage = $sce.trustAsHtml($interpolate(msg.sessionTimeoutTmpl)({loginUrl}));
                 window.scrollTo(0, 0);
               } else if (response.status == 400) {
                 self.errorObj = response.data;
                 window.scrollTo(0, 0);
               }
            })
        };

        this.previewEntry = function() {
            window.open(self.entry.previewUrl);
        }

        this.deleteWeblogEntry = function() {
            $('#deleteWeblogEntryModal').modal('hide');

            $http.delete(this.urlRoot + entryId).then(
                function(response) {
                    document.location.href=newEntryUrl;
                },
                self.commonErrorResponse
            )
        }

        this.loadRecentEntries = function() {
            this.getRecentEntries('DRAFT');
            this.getRecentEntries('PUBLISHED');
            this.getRecentEntries('SCHEDULED');
            this.getRecentEntries('PENDING');
        }

        this.messageClear = function() {
            this.successMessage = null;
            this.errorObj = {};
        }

        this.commonErrorResponse = function(response) {
            if (response.status == 408) {
               window.location.replace($('#refreshURL').attr('value'));
            } else {
               self.errorObj = response.data;
            }
        }

        this.loadMetadata();
        this.loadRecentEntries();
        if (entryId) {
            this.getEntry();
        }
    }]
);
