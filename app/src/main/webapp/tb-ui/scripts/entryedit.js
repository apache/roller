function split( val ) {
    return val.split( / \s*/ );
}

function extractLast( term ) {
    return split( term ).pop();
}

function onClickAddImage(){
    $( "#mediaFileChooser" ).attr('src', mediaFileChooserUrl);
    $(function() {
        $("#mediafile_edit_lightbox").dialog({
            modal  : true,
            width  : 600,
            height : 600
        });
    });
}

function onClose() {
    $("#mediaFileChooser").attr('src','about:blank');
}

function onSelectMediaFile(name, url, alt, title, anchor, isImage) {
    $("#mediafile_edit_lightbox").dialog("close");
    $("#mediaFileChooser").attr('src','about:blank');
    var anchorTag;
    if (isImage === true) {
        anchorTag = (anchor ? '<a href="' + anchor + '">' : '') +
        '<img src="' + url + '"' +
        ' alt="' + (alt ? alt : name) + '"' +
         (title ? ' title="' + title + '"' : '') +
         '/>' +
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

    $("#confirm-delete").dialog({
        autoOpen: false,
        resizable: false,
        height:170,
        modal: true,
        buttons: [
            {
                text: msg.deleteLabel,
                click: function() {
                    angular.element('#ngapp-div').scope().ctrl.deleteWeblogEntry();
                    angular.element('#ngapp-div').scope().$apply();
                    $( this ).dialog( "close" );
                    document.location.href=newEntryUrl;
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

    $( "#tagAutoComplete" )
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
        this.entry = { commentCount : 0, category : {} };
        this.quillEditor = null;

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
            $http.get(this.urlRoot + weblogId + '/entryeditmetadata'  ).then(
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
                 self.commentCountMsg = $sce.trustAsHtml($interpolate(commentCountTmpl)({commentCount:self.entry.commentCount}));
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
            var urlStem = weblogId + '/entries';

            self.entry.status = saveType;

            // get text from RTE
            if ('RICHTEXT' == self.entry.editFormat) {
                var html = $("#ql-editor-1").html();
                self.entry.text = html;
            }

            $http.post(self.urlRoot + urlStem, JSON.stringify(self.entry)).then(
              function(response) {
                entryId = response.data.entryId;
                self.saveResponseMessage = response.data.message;
                self.errorObj = {};
                self.loadRecentEntries();
                self.getEntry();
              },
             function(response) {
               if (response.status == 408)
                 window.location.replace($('#refreshURL').attr('value'));
               if (response.status == 400) {
                 self.errorObj = response.data;
               }
            })
        };

        this.previewEntry = function() {
            window.open(self.entry.previewUrl);
        }

        this.deleteWeblogEntry = function() {
            $http.delete(this.urlRoot + entryId).then(
              function(response) {
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

        this.loadMetadata();
        this.loadRecentEntries();
        if (entryId) {
            this.getEntry();
        }
    }]
);

function showDialog(dialogId) {
    return {
        restrict: 'A',
        link: function(scope, elem, attr, ctrl) {
            elem.bind('click', function(e) {
                $(dialogId).dialog('open');
            });
        }
    };
}

tightblogApp.directive('deleteEntryDialog', function(){return showDialog('#confirm-delete')});
