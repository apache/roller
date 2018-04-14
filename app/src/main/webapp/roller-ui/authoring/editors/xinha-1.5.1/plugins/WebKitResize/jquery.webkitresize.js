/* 
* Webkitresize (http://editorboost.net/webkitresize)
* Copyright 2012 Editorboost. All rights reserved. 
*
* Webkitresize commercial licenses may be obtained at http://editorboost.net/home/licenses.
* If you do not own a commercial license, this file shall be governed by the
* GNU General Public License (GPL) version 3. For GPL requirements, please
* review: http://www.gnu.org/copyleft/gpl.html
*
* Version date: March 19 2013
* REQUIRES: jquery 1.7.1+
*/

; (function ($) {
    $.fn.webkitimageresize = function (options) {
        return this.each(function () {

            if (!$.browser.webkit) {
                return;
            }


            var settings = $.extend({
            }, options);

            var lastCrc;
            var imageResizeinProgress = false;
            var currentImage;
            var currentImage_HxW_Rate;
            var currentImage_WxH_Rate;
            var initialHeight;
            var initialWidth;

            var methods = {
                
                guidGenerator: function() {
                    var S4 = function() {
                       return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
                    };
                    return (S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
                },

                guidFilter: function(context){
                    return "[data-guid='" + context.guid + "']";
                },

                removeResizeElements: function (context) {
                    $(".img-resize-selector").filter(methods.guidFilter(context)).remove();
                    $(".img-resize-region").filter(methods.guidFilter(context)).remove();
                },

                imageClick: function (context, img) {
                    if (settings.beforeElementSelect) {
                        settings.beforeElementSelect(img);
                    }

                    methods.removeResizeElements(context);
                    currentImage = img;

                    var imgHeight = $(img).outerHeight();
                    var imgWidth = $(img).outerWidth();
                    var iframePos = context.$ifrm.offset();
                    var imgPosition = $(img).offset();       
                    var ifrmScrollTop = Math.max(context.ifrm.contentWindow.document.documentElement.scrollTop, context.$ifrmBody.scrollTop());
                    var ifrmScrollLeft = Math.max(context.ifrm.contentWindow.document.documentElement.scrollLeft, context.$ifrmBody.scrollLeft());

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='img-resize-selector' style='margin:10px;position:absolute;top:" + (iframePos.top + imgPosition.top - ifrmScrollTop + imgHeight - 10) + "px;left:" + (iframePos.left + imgPosition.left - ifrmScrollLeft + imgWidth - 10) + "px;border:solid 2px red;width:6px;height:6px;cursor:se-resize;z-index:1000;'></span>");

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='img-resize-region region-top-right' style='position:absolute;z-index:1000;top:" + (iframePos.top + imgPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + imgPosition.left - ifrmScrollLeft) + "px;border:dashed 1px grey;width:" + imgWidth + "px;height:0px;'></span>");
                    context.$docBody.append("<span data-guid='" + context.guid + "' class='img-resize-region region-top-down' style='position:absolute;z-index:1000;top:" + (iframePos.top + imgPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + imgPosition.left - ifrmScrollLeft) + "px;border:dashed 1px grey;width:0px;height:" + imgHeight + "px;'></span>");

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='img-resize-region region-right-down' style='position:absolute;z-index:1000;top:" + (iframePos.top + imgPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + imgPosition.left - ifrmScrollLeft + imgWidth) + "px;border:dashed 1px grey;width:0px;height:" + imgHeight + "px;'></span>");
                    context.$docBody.append("<span data-guid='" + context.guid + "' class='img-resize-region region-down-left' style='position:absolute;z-index:1000;top:" + (iframePos.top + imgPosition.top - ifrmScrollTop + imgHeight) + "px;left:" + (iframePos.left + imgPosition.left - ifrmScrollLeft) + "px;border:dashed 1px grey;width:" + imgWidth + "px;height:0px;'></span>");


                    var dragStop = function () {                        
                        if (imageResizeinProgress) {
                            $(currentImage)
                                .css("width", $(".region-top-right").filter(methods.guidFilter(context)).width() + "px")
                                .css('height', $(".region-top-down").filter(methods.guidFilter(context)).height() + "px");
                            methods.refresh(context);
                            $(currentImage).click();

                            context.$ifrm.trigger('webkitresize-updatecrc', [methods.crc(context.$ifrmBody.html())]);

                            imageResizeinProgress = false;

                            if (settings.afterResize) {
                                settings.afterResize(currentImage);
                            }
                        }
                    };

                    var iframeMouseMove = function (e) {
                        if (imageResizeinProgress) {

                            var resWidth = imgWidth;
                            var resHeight = imgHeight;

                            resHeight = e.pageY - imgPosition.top;
                            resWidth = e.pageX - imgPosition.left;

                            if (resHeight < 1) {
                                resHeight = 1;
                            }
                            if (resWidth < 1) {
                                resWidth = 1;
                            }
                            
                            if(settings.keepAspectRatio || e.ctrlKey){
                                var heightDiff = initialHeight - resHeight;
                                if(heightDiff < 0){
                                    heightDiff = heightDiff * -1.0;
                                }
                                var widthDiff = initialWidth - resWidth;
                                if(widthDiff < 0){
                                    widthDiff = widthDiff * -1.0;
                                }

                                if(heightDiff > widthDiff){
                                    resWidth = resHeight * currentImage_WxH_Rate;
                                }
                                else{
                                    resHeight = resWidth * currentImage_HxW_Rate;
                                }      
                            }                           

                            $(".img-resize-selector").filter(methods.guidFilter(context)).css("top", (iframePos.top + imgPosition.top - ifrmScrollTop + resHeight - 10) + 'px').css("left", (iframePos.left + imgPosition.left - ifrmScrollLeft + resWidth - 10) + "px");
                            $(".region-top-right").filter(methods.guidFilter(context)).css("width", resWidth + "px");
                            $(".region-top-down").filter(methods.guidFilter(context)).css("height", resHeight + "px");

                            $(".region-right-down").filter(methods.guidFilter(context)).css("left", (iframePos.left + imgPosition.left - ifrmScrollLeft + resWidth) + "px").css("height", resHeight + "px");
                            $(".region-down-left").filter(methods.guidFilter(context)).css("top", (iframePos.top + imgPosition.top - ifrmScrollTop + resHeight) + "px").css("width", resWidth + "px");
                        }

                        return false;
                    };


                    var windowMouseMove = function (e) {
                        if (imageResizeinProgress) {

                            var resWidth = imgWidth;
                            var resHeight = imgHeight;

                            resHeight = e.pageY - (iframePos.top + imgPosition.top - ifrmScrollTop);
                            resWidth = e.pageX - (iframePos.left + imgPosition.left - ifrmScrollLeft);

                            if (resHeight < 1) {
                                resHeight = 1;
                            }
                            if (resWidth < 1) {
                                resWidth = 1;
                            }
                            
                            if(settings.keepAspectRatio || e.ctrlKey){
                                var heightDiff = initialHeight - resHeight;
                                if(heightDiff < 0){
                                    heightDiff = heightDiff * -1.0;
                                }
                                var widthDiff = initialWidth - resWidth;
                                if(widthDiff < 0){
                                    widthDiff = widthDiff * -1.0;
                                }

                                if(heightDiff > widthDiff){
                                    resWidth = resHeight * currentImage_WxH_Rate;
                                }
                                else{
                                    resHeight = resWidth * currentImage_HxW_Rate;
                                }       
                            }                        

                            $(".img-resize-selector").filter(methods.guidFilter(context)).css("top", (iframePos.top + imgPosition.top - ifrmScrollTop + resHeight - 10) + 'px').css("left", (iframePos.left + imgPosition.left - ifrmScrollLeft + resWidth - 10) + "px");
                            $(".region-top-right").filter(methods.guidFilter(context)).css("width", resWidth + "px");
                            $(".region-top-down").filter(methods.guidFilter(context)).css("height", resHeight + "px");

                            $(".region-right-down").filter(methods.guidFilter(context)).css("left", (iframePos.left + imgPosition.left - ifrmScrollLeft + resWidth) + "px").css("height", resHeight + "px");
                            $(".region-down-left").filter(methods.guidFilter(context)).css("top", (iframePos.top + imgPosition.top - ifrmScrollTop + resHeight) + "px").css("width", resWidth + "px");
                        }

                        return false;
                    };

                    $(".img-resize-selector").filter(methods.guidFilter(context)).mousedown(function (e) {
                        if (settings.beforeResizeStart) {
                            settings.beforeResizeStart(currentImage);
                        }
                        
                        var imgH = $(currentImage).height();
                        var imgW = $(currentImage).width();

                        currentImage_HxW_Rate = imgH / imgW;
                        currentImage_WxH_Rate = imgW / imgH;
                        if(imgH > imgW){
                            initialHeight = 0;
                            initialWidth = (imgH - imgW) * -1;
                        }
                        else{
                            initialWidth = 0;
                            initialHeight = (imgW - imgH) * -1;
                        }

                        imageResizeinProgress = true;

                        return false;
                    });

                    $(context.ifrm.contentWindow.document).mouseup(function () {
                        if (imageResizeinProgress) {
                            dragStop();
                        }
                    });

                    $(window.document).mouseup(function () {
                        if (imageResizeinProgress) {
                            dragStop();
                        }
                    });

                    $(context.ifrm.contentWindow.document).mousemove(function (e) {
                        iframeMouseMove(e);
                    });

                    $(window.document).mousemove(function (e) {
                        windowMouseMove(e);
                    });

                    if (settings.afterElementSelect) {
                        settings.afterElementSelect(currentImage);
                    }
                },

                rebind: function (context) {
                    context.$ifrm.contents().find("img").each(function (i, v) {
                        $(v).unbind('click');
                        $(v).click(function (e) {
                            if (e.target == v) {
                                methods.imageClick(context, v);
                            }
                        });
                    });
                },
                
                debind: function (context) {
                    context.$ifrm.contents().find("img").each(function (i, v) {
                        $(v).unbind('click');                        
                    });
                },

                refresh: function (context) {                                                   
                    methods.rebind(context);
                    
                    methods.removeResizeElements(context);
                    
                    if (!currentImage) {
                        if (settings.afterRefresh) {
                            settings.afterRefresh(null);
                        }
                        return;
                    }
                    
                    var img = currentImage;

                    var imgHeight = $(img).outerHeight();
                    var imgWidth = $(img).outerWidth();
                    var iframePos = context.$ifrm.offset();
                    var imgPosition = $(img).offset();
                    var ifrmScrollTop = context.$ifrmBody.scrollTop();
                    var ifrmScrollLeft = context.$ifrmBody.scrollLeft();

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='img-resize-selector' style='position:absolute;top:" + (iframePos.top + imgPosition.top - ifrmScrollTop + imgHeight) + "px;left:" + (iframePos.left + imgPosition.left - ifrmScrollLeft + imgWidth) + "px;border:solid 2px red;width:6px;height:6px;cursor:se-resize;z-index:1000;'></span>");

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='img-resize-region' style='position:absolute;z-index:1000;top:" + (iframePos.top + imgPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + imgPosition.left - ifrmScrollLeft) + "px;border:dashed 1px grey;width:" + imgWidth + "px;height:0px;'></span>");
                    context.$docBody.append("<span data-guid='" + context.guid + "' class='img-resize-region' style='position:absolute;z-index:1000;top:" + (iframePos.top + imgPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + imgPosition.left - ifrmScrollLeft) + "px;border:dashed 1px grey;width:0px;height:" + imgHeight + "px;'></span>");

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='img-resize-region' style='position:absolute;z-index:1000;top:" + (iframePos.top + imgPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + imgPosition.left - ifrmScrollLeft + imgWidth) + "px;border:dashed 1px grey;width:0px;height:" + imgHeight + "px;'></span>");
                    context.$docBody.append("<span data-guid='" + context.guid + "' class='img-resize-region' style='position:absolute;z-index:1000;top:" + (iframePos.top + imgPosition.top - ifrmScrollTop + imgHeight) + "px;left:" + (iframePos.left + imgPosition.left - ifrmScrollLeft) + "px;border:dashed 1px grey;width:" + imgWidth + "px;height:0px;'></span>");

                    lastCrc = methods.crc(context.$ifrmBody.html());

                    if (settings.afterRefresh) {
                        settings.afterRefresh(currentImage);
                    }
                },

                reset: function (context) {
                    if (currentImage != null) {
                        currentImage = null;
                        imageResizeinProgress = false;
                        methods.removeResizeElements(context);

                        if (settings.afterReset) {
                            settings.afterReset();
                        }
                    }
                    methods.rebind(context);
                },

                crc: function (str) {
                    var hash = 0;
                    if (str.length == 0) return hash;
                    for (i = 0; i < str.length; i++) {
                        char = str.charCodeAt(i);
                        hash = ((hash << 5) - hash) + char;
                        hash = hash & hash;
                    }
                    return hash;
                }
            };

            var ifrm = this;
            var $ifrm = $(this);
            var $docBody = $("body");
            var $ifrmBody = $ifrm.contents().find("body");

            lastCrc = methods.crc($ifrmBody.html());

            if (!$ifrm.is('iframe')) {
                return;
            }

            var context = {
                ifrm: ifrm,
                $ifrm: $ifrm,
                $docBody: $docBody,
                $ifrmBody: $ifrmBody,
                guid: methods.guidGenerator()
            };

            ifrm.contentWindow.addEventListener('scroll', function () {
                methods.reset(context);
            }, false);

            $(ifrm.contentWindow.document).mouseup(function (e) {
                if (!imageResizeinProgress) {
                    var x = (e.x) ? e.x : e.clientX;
                    var y = (e.y) ? e.y : e.clientY;
                    var mouseUpElement = ifrm.contentWindow.document.elementFromPoint(x, y);
                    if (mouseUpElement) {
                        var matchingElement;
                        var $select = context.$ifrm.contents().find(settings.selector);
                        var $parentsSelect = $(mouseUpElement).parents();
                        for (var psi = 0; psi < $parentsSelect.length; psi++) {
                            for (var i = 0; i < $select.length; i++) {
                                if ($select[i] == $parentsSelect[psi]) {
                                    matchingElement = $select[i];
                                    break;
                                }
                            }
                            if (matchingElement) {
                                break;
                            }
                        }
                        if (!matchingElement) {
                            methods.reset(context);
                        }
                        else {
                            methods.imageClick(context, matchingElement);
                        }
                    }
                }
            });            

            $(document).keyup(function (e) {
                if (e.keyCode == 27) {
                    methods.reset(context);
                }
            });

            $(ifrm.contentWindow.document).keyup(function (e) {
                if (e.keyCode == 27) {
                    methods.reset(context);
                }
            });


            if (!ifrm.crcChecker) {
                ifrm.crcChecker = setInterval(function () {
                    var currentCrc = methods.crc($ifrmBody.html());
                    if (lastCrc != currentCrc) {
                        $ifrm.trigger('webkitresize-crcchanged', [currentCrc]);
                    }
                }, 1000);
            }

            $(window).resize(function(){
                methods.reset(context);
            });

            $ifrm.bind('webkitresize-crcchanged', function (event, crc) {
                lastCrc = crc;
                methods.reset(context);
            });

            $ifrm.bind('webkitresize-updatecrc', function (event, crc) {                
                lastCrc = crc;
            });
            
            methods.refresh(context);

            ifrm._WebKitImageResizeEnd   = function(){ methods.reset(context); methods.removeResizeElements(context); methods.debind(context); }
            ifrm._WebKitImageResizeStart = function(){ methods.reset(context); context.$ifrmBody = $ifrm.contents().find("body"); }
        });
    };


    $.fn.webkittableresize = function (options) {
        return this.each(function () {

            if (!$.browser.webkit) {
            //    return;
            }


            var settings = $.extend({
            }, options);

            var lastCrc;
            var tableResizeinProgress = false;
            var currenttable;

            var methods = {

                guidGenerator: function() {
                    var S4 = function() {
                       return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
                    };
                    return (S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
                },

                guidFilter: function(context){
                    return "[data-guid='" + context.guid + "']";
                },

                removeResizeElements: function (context) {
                    $(".resize-selector").filter(methods.guidFilter(context)).remove();
                    $(".resize-region").filter(methods.guidFilter(context)).remove();
                },

                tableClick: function (context, tbl) {
                    if (settings.beforeElementSelect) {
                        settings.beforeElementSelect(tbl);
                    }

                    methods.removeResizeElements(context);
                    currenttable = tbl;

                    var tblHeight = $(tbl).outerHeight();
                    var tblWidth = $(tbl).outerWidth();
                    var iframePos = context.$ifrm.offset();
                    var tblPosition = $(tbl).offset();
                    var ifrmScrollTop = Math.max(context.ifrm.contentWindow.document.documentElement.scrollTop, context.$ifrmBody.scrollTop());
                    var ifrmScrollLeft = Math.max(context.ifrm.contentWindow.document.documentElement.scrollLeft, context.$ifrmBody.scrollLeft());

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='resize-selector' style='margin:10px;position:absolute;top:" + (iframePos.top + tblPosition.top - ifrmScrollTop + tblHeight - 10) + "px;left:" + (iframePos.left + tblPosition.left - ifrmScrollLeft + tblWidth - 10) + "px;border:solid 2px red;width:6px;height:6px;cursor:se-resize;z-index:1000;'></span>");

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='resize-region region-top-right' style='position:absolute;z-index:1000;top:" + (iframePos.top + tblPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + tblPosition.left - ifrmScrollLeft) + "px;border:dashed 1px grey;width:" + tblWidth + "px;height:0px;'></span>");
                    context.$docBody.append("<span data-guid='" + context.guid + "' class='resize-region region-top-down' style='position:absolute;z-index:1000;top:" + (iframePos.top + tblPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + tblPosition.left - ifrmScrollLeft) + "px;border:dashed 1px grey;width:0px;height:" + tblHeight + "px;'></span>");

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='resize-region region-right-down' style='position:absolute;z-index:1000;top:" + (iframePos.top + tblPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + tblPosition.left - ifrmScrollLeft + tblWidth) + "px;border:dashed 1px grey;width:0px;height:" + tblHeight + "px;'></span>");
                    context.$docBody.append("<span data-guid='" + context.guid + "' class='resize-region region-down-left' style='position:absolute;z-index:1000;top:" + (iframePos.top + tblPosition.top - ifrmScrollTop + tblHeight) + "px;left:" + (iframePos.left + tblPosition.left - ifrmScrollLeft) + "px;border:dashed 1px grey;width:" + tblWidth + "px;height:0px;'></span>");


                    var dragStop = function () {
                        if (tableResizeinProgress) {
                            $(currenttable)
                                .css("width", $(".region-top-right").filter(methods.guidFilter(context)).width() + "px")
                                .css('height', $(".region-top-down").filter(methods.guidFilter(context)).height() + "px");
                            methods.refresh(context);
                            $(currenttable).click();

                            context.$ifrm.trigger('webkitresize-updatecrc', [methods.crc(context.$ifrmBody.html())]);
                            context.$ifrm.trigger('webkitresize-table-resized', []);

                            tableResizeinProgress = false;

                            if (settings.afterResize) {
                                settings.afterResize(currenttable);
                            }
                        }
                    };

                    var iframeMouseMove = function (e) {
                        if (tableResizeinProgress) {

                            var resWidth = tblWidth;
                            var resHeight = tblHeight;

                            resHeight = e.pageY - tblPosition.top;
                            resWidth = e.pageX - tblPosition.left;

                            if (resHeight < 1) {
                                resHeight = 1;
                            }
                            if (resWidth < 1) {
                                resWidth = 1;
                            }

                            $(".resize-selector").filter(methods.guidFilter(context)).css("top", (iframePos.top + tblPosition.top - ifrmScrollTop + resHeight - 10) + 'px').css("left", (iframePos.left + tblPosition.left - ifrmScrollLeft + resWidth - 10) + "px");
                            $(".region-top-right").filter(methods.guidFilter(context)).css("width", resWidth + "px");
                            $(".region-top-down").filter(methods.guidFilter(context)).css("height", resHeight + "px");

                            $(".region-right-down").filter(methods.guidFilter(context)).css("left", (iframePos.left + tblPosition.left - ifrmScrollLeft + resWidth) + "px").css("height", resHeight + "px");
                            $(".region-down-left").filter(methods.guidFilter(context)).css("top", (iframePos.top + tblPosition.top - ifrmScrollTop + resHeight) + "px").css("width", resWidth + "px");
                        }

                        return false;
                    };


                    var windowMouseMove = function (e) {
                        if (tableResizeinProgress) {

                            var resWidth = tblWidth;
                            var resHeight = tblHeight;

                            resHeight = e.pageY - (iframePos.top + tblPosition.top - ifrmScrollTop);
                            resWidth = e.pageX - (iframePos.left + tblPosition.left - ifrmScrollLeft);

                            if (resHeight < 1) {
                                resHeight = 1;
                            }
                            if (resWidth < 1) {
                                resWidth = 1;
                            }

                            $(".resize-selector").filter(methods.guidFilter(context)).css("top", (iframePos.top + tblPosition.top - ifrmScrollTop + resHeight - 10) + 'px').css("left", (iframePos.left + tblPosition.left - ifrmScrollLeft + resWidth - 10) + "px");
                            $(".region-top-right").filter(methods.guidFilter(context)).css("width", resWidth + "px");
                            $(".region-top-down").filter(methods.guidFilter(context)).css("height", resHeight + "px");

                            $(".region-right-down").filter(methods.guidFilter(context)).css("left", (iframePos.left + tblPosition.left - ifrmScrollLeft + resWidth) + "px").css("height", resHeight + "px");
                            $(".region-down-left").filter(methods.guidFilter(context)).css("top", (iframePos.top + tblPosition.top - ifrmScrollTop + resHeight) + "px").css("width", resWidth + "px");
                        }

                        return false;
                    };

                    $(".resize-selector").filter(methods.guidFilter(context)).mousedown(function (e) {
                        if (settings.beforeResizeStart) {
                            settings.beforeResizeStart(currenttable);
                        }
                        tableResizeinProgress = true;
                        return false;
                    });

                    $("*").mouseup(function () {
                        if (tableResizeinProgress) {
                            dragStop();
                        }
                    });

                    $(context.ifrm.contentWindow).mousemove(function (e) {
                        iframeMouseMove(e);
                    });

                    $(window).mousemove(function (e) {
                        windowMouseMove(e);
                    });

                    if (settings.afterElementSelect) {
                        settings.afterElementSelect(currenttable);
                    }
                },

                rebind: function (context) {
                    context.$ifrm.contents().find("table").each(function (i, v) {
                        $(v).unbind('click');
                        $(v).click(function (e) {
                            if (e.target == v || (($(e.target).is('td')||$(e.target).is('th')) && $(e.target).parents("table")[0] == v)) {
                                methods.tableClick(context, v);
                            }
                        });
                    });
                },

                debind: function (context) {
                    context.$ifrm.contents().find("table").each(function (i, v) {
                        $(v).unbind('click');                        
                    });
                },

                refresh: function (context) {
                    methods.rebind(context);

                    methods.removeResizeElements(context);

                    if (!currenttable) {
                        if (settings.afterRefresh) {
                            settings.afterRefresh(null);
                        }
                        return;
                    }

                    var tbl = currenttable;

                    var tblHeight = $(tbl).outerHeight();
                    var tblWidth = $(tbl).outerWidth();
                    var iframePos = context.$ifrm.offset();
                    var tblPosition = $(tbl).offset();
                    var ifrmScrollTop = context.$ifrmBody.scrollTop();
                    var ifrmScrollLeft = context.$ifrmBody.scrollLeft();

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='resize-selector' style='position:absolute;top:" + (iframePos.top + tblPosition.top - ifrmScrollTop + tblHeight) + "px;left:" + (iframePos.left + tblPosition.left - ifrmScrollLeft + tblWidth) + "px;border:solid 2px red;width:6px;height:6px;cursor:se-resize;z-index:1000;'></span>");

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='resize-region' style='position:absolute;z-index:1000;top:" + (iframePos.top + tblPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + tblPosition.left - ifrmScrollLeft) + "px;border:dashed 1px grey;width:" + tblWidth + "px;height:0px;'></span>");
                    context.$docBody.append("<span data-guid='" + context.guid + "' class='resize-region' style='position:absolute;z-index:1000;top:" + (iframePos.top + tblPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + tblPosition.left - ifrmScrollLeft) + "px;border:dashed 1px grey;width:0px;height:" + tblHeight + "px;'></span>");

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='resize-region' style='position:absolute;z-index:1000;top:" + (iframePos.top + tblPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + tblPosition.left - ifrmScrollLeft + tblWidth) + "px;border:dashed 1px grey;width:0px;height:" + tblHeight + "px;'></span>");
                    context.$docBody.append("<span data-guid='" + context.guid + "' class='resize-region' style='position:absolute;z-index:1000;top:" + (iframePos.top + tblPosition.top - ifrmScrollTop + tblHeight) + "px;left:" + (iframePos.left + tblPosition.left - ifrmScrollLeft) + "px;border:dashed 1px grey;width:" + tblWidth + "px;height:0px;'></span>");

                    lastCrc = methods.crc(context.$ifrmBody.html());

                    if (settings.afterRefresh) {
                        settings.afterRefresh(currenttable);
                    }
                },

                reset: function (context) {
                    if (currenttable != null) {
                        currenttable = null;
                        tableResizeinProgress = false;
                        methods.removeResizeElements(context);

                        if (settings.afterReset) {
                            settings.afterReset();
                        }
                    }

                    methods.rebind(context);
                },

                crc: function (str) {
                    var hash = 0;
                    if (str.length == 0) return hash;
                    for (i = 0; i < str.length; i++) {
                        char = str.charCodeAt(i);
                        hash = ((hash << 5) - hash) + char;
                        hash = hash & hash;
                    }
                    return hash;
                }

            };

            var ifrm = this;
            var $ifrm = $(this);
            var $docBody = $("body");
            var $ifrmBody = $ifrm.contents().find("body");

            lastCrc = methods.crc($ifrmBody.html());

            if (!$ifrm.is('iframe')) {
                return;
            }

            var context = {
                ifrm: ifrm,
                $ifrm: $ifrm,
                $docBody: $docBody,
                $ifrmBody: $ifrmBody,
                guid: methods.guidGenerator()
            };

            ifrm.contentWindow.addEventListener('scroll', function () {
                methods.reset(context);
            }, false);

            $(ifrm.contentWindow.document).mouseup(function (e) {
                if (!tableResizeinProgress) {
                    var x = (e.x) ? e.x : e.clientX;
                    var y = (e.y) ? e.y : e.clientY;
                    var mouseUpElement = ifrm.contentWindow.document.elementFromPoint(x, y);
                    if (mouseUpElement) {
                        var matchingElement;
                        var $select = context.$ifrm.contents().find(settings.selector);
                        var $parentsSelect = $(mouseUpElement).parents();
                        for (var psi = 0; psi < $parentsSelect.length; psi++) {
                            for (var i = 0; i < $select.length; i++) {
                                if ($select[i] == $parentsSelect[psi]) {
                                    matchingElement = $select[i];
                                    break;
                                }
                            }
                            if (matchingElement) {
                                break;
                            }
                        }
                        if (!matchingElement) {
                            methods.reset(context);
                        }
                        else {
                            methods.tableClick(context, matchingElement);
                        }
                    }
                }
            });

            $(document).keyup(function (e) {
                if (e.keyCode == 27) {
                    methods.reset(context);
                }
            });

            if (!ifrm.crcChecker) {
                ifrm.crcChecker = setInterval(function () {
                    var currentCrc = methods.crc($ifrmBody.html());
                    if (lastCrc != currentCrc) {
                        $ifrm.trigger('webkitresize-crcchanged', [currentCrc]);
                    }
                }, 1000);
            }

            $(window).resize(function(){
                methods.reset(context);
            });

            $ifrm.bind('webkitresize-crcchanged', function (event, crc) {
                lastCrc = crc;
                methods.reset(context);
            });

            $ifrm.bind('webkitresize-updatecrc', function (event, crc) {
                lastCrc = crc;
            });

            methods.refresh(context);

            ifrm._WebKitTableResizeEnd = function(){ methods.reset(context); methods.removeResizeElements(context); methods.debind(context); }
            ifrm._WebKitImageResizeStart = function(){ methods.reset(context); context.$ifrmBody = $ifrm.contents().find("body"); }
        });
    };


    $.fn.webkittdresize = function (options) {
        return this.each(function () {

            if (!$.browser.webkit) {
           //     return;
            }


            var settings = $.extend({
            }, options);

            var lastCrc;
            var tdResizeinProgress = false;
            var currenttd;

            var methods = {

                guidGenerator: function() {
                    var S4 = function() {
                       return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
                    };
                    return (S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
                },

                guidFilter: function(context){
                    return "[data-guid='" + context.guid + "']";
                },

                removeResizeElements: function (context) {
                    $(".td-resize-selector").filter(methods.guidFilter(context)).remove();
                    $(".td-resize-region").filter(methods.guidFilter(context)).remove();
                },

                tdClick: function (context, td) {
                    if (settings.beforeElementSelect) {
                        settings.beforeElementSelect(td);
                    }

                    methods.removeResizeElements(context);
                    currenttd = td;

                    var tdHeight = $(td).outerHeight();
                    var tdWidth = $(td).outerWidth();
                    var iframePos = context.$ifrm.offset();
                    var tdPosition = $(td).offset();
                    var ifrmScrollTop = Math.max(context.ifrm.contentWindow.document.documentElement.scrollTop, context.$ifrmBody.scrollTop());
                    var ifrmScrollLeft = Math.max(context.ifrm.contentWindow.document.documentElement.scrollLeft, context.$ifrmBody.scrollLeft());

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='td-resize-selector' style='margin:10px;position:absolute;top:" + (iframePos.top + tdPosition.top - ifrmScrollTop + tdHeight - 10) + "px;left:" + (iframePos.left + tdPosition.left - ifrmScrollLeft + tdWidth - 10) + "px;border:solid 2px red;width:6px;height:6px;cursor:se-resize;z-index:1000;'></span>");

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='td-resize-region td-region-top-right' style='position:absolute;z-index:1000;top:" + (iframePos.top + tdPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + tdPosition.left - ifrmScrollLeft) + "px;border:dashed 1px green;width:" + tdWidth + "px;height:0px;'></span>");
                    context.$docBody.append("<span data-guid='" + context.guid + "' class='td-resize-region td-region-top-down' style='position:absolute;z-index:1000;top:" + (iframePos.top + tdPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + tdPosition.left - ifrmScrollLeft) + "px;border:dashed 1px green;width:0px;height:" + tdHeight + "px;'></span>");

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='td-resize-region td-region-right-down' style='position:absolute;z-index:1000;top:" + (iframePos.top + tdPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + tdPosition.left - ifrmScrollLeft + tdWidth) + "px;border:dashed 1px green;width:0px;height:" + tdHeight + "px;'></span>");
                    context.$docBody.append("<span data-guid='" + context.guid + "' class='td-resize-region td-region-down-left' style='position:absolute;z-index:1000;top:" + (iframePos.top + tdPosition.top - ifrmScrollTop + tdHeight) + "px;left:" + (iframePos.left + tdPosition.left - ifrmScrollLeft) + "px;border:dashed 1px green;width:" + tdWidth + "px;height:0px;'></span>");


                    var dragStop = function () {
                        if (tdResizeinProgress) {
                            $(currenttd)
                                .css("width", $(".td-region-top-right").filter(methods.guidFilter(context)).width() + "px")
                                .css('height', $(".td-region-top-down").filter(methods.guidFilter(context)).height() + "px");
                            methods.refresh(context);
                            $(currenttd).click();

                            context.$ifrm.trigger('webkitresize-updatecrc', [methods.crc(context.$ifrmBody.html())]);

                            tdResizeinProgress = false;

                            if (settings.afterResize) {
                                settings.afterResize(currenttd);
                            }
                        }
                    };

                    var iframeMouseMove = function (e) {
                        if (tdResizeinProgress) {

                            var resWidth = tdWidth;
                            var resHeight = tdHeight;

                            resHeight = e.pageY - tdPosition.top;
                            resWidth = e.pageX - tdPosition.left;

                            if (resHeight < 1) {
                                resHeight = 1;
                            }
                            if (resWidth < 1) {
                                resWidth = 1;
                            }

                            $(".td-resize-selector").filter(methods.guidFilter(context)).css("top", (iframePos.top + tdPosition.top - ifrmScrollTop + resHeight - 10) + 'px').css("left", (iframePos.left + tdPosition.left - ifrmScrollLeft + resWidth - 10) + "px");
                            $(".td-region-top-right").filter(methods.guidFilter(context)).css("width", resWidth + "px");
                            $(".td-region-top-down").filter(methods.guidFilter(context)).css("height", resHeight + "px");

                            $(".td-region-right-down").filter(methods.guidFilter(context)).css("left", (iframePos.left + tdPosition.left - ifrmScrollLeft + resWidth) + "px").css("height", resHeight + "px");
                            $(".td-region-down-left").filter(methods.guidFilter(context)).css("top", (iframePos.top + tdPosition.top - ifrmScrollTop + resHeight) + "px").css("width", resWidth + "px");
                        }

                        return false;
                    };


                    var windowMouseMove = function (e) {
                        if (tdResizeinProgress) {

                            var resWidth = tdWidth;
                            var resHeight = tdHeight;

                            resHeight = e.pageY - (iframePos.top + tdPosition.top - ifrmScrollTop);
                            resWidth = e.pageX - (iframePos.left + tdPosition.left - ifrmScrollLeft);

                            if (resHeight < 1) {
                                resHeight = 1;
                            }
                            if (resWidth < 1) {
                                resWidth = 1;
                            }

                            $(".td-resize-selector").filter(methods.guidFilter(context)).css("top", (iframePos.top + tdPosition.top - ifrmScrollTop + resHeight - 10) + 'px').css("left", (iframePos.left + tdPosition.left - ifrmScrollLeft + resWidth - 10) + "px");
                            $(".td-region-top-right").filter(methods.guidFilter(context)).css("width", resWidth + "px");
                            $(".td-region-top-down").filter(methods.guidFilter(context)).css("height", resHeight + "px");

                            $(".td-region-right-down").filter(methods.guidFilter(context)).css("left", (iframePos.left + tdPosition.left - ifrmScrollLeft + resWidth) + "px").css("height", resHeight + "px");
                            $(".td-region-down-left").filter(methods.guidFilter(context)).css("top", (iframePos.top + tdPosition.top - ifrmScrollTop + resHeight) + "px").css("width", resWidth + "px");
                        }

                        return false;
                    };

                    $(".td-resize-selector").filter(methods.guidFilter(context)).mousedown(function (e) {
                        if (settings.beforeResizeStart) {
                            settings.beforeResizeStart(currenttd);
                        }
                        tdResizeinProgress = true;
                        return false;
                    });

                    $("*").mouseup(function () {
                        if (tdResizeinProgress) {
                            dragStop();
                        }
                    });

                    $(context.ifrm.contentWindow).mousemove(function (e) {
                        iframeMouseMove(e);
                    });

                    $(window).mousemove(function (e) {
                        windowMouseMove(e);
                    });

                    if (settings.afterElementSelect) {
                        settings.afterElementSelect(currenttd);
                    }
                },

                rebind: function (context) {
                    context.$ifrm.contents().find("td,th").each(function (i, v) {
                        $(v).unbind('click');
                        $(v).click(function (e) {
                            if (e.target == v) {
                                methods.tdClick(context, v);
                            }
                        });
                    });
                },
                
                debind: function (context) {
                    context.$ifrm.contents().find("td,th").each(function (i, v) {
                        $(v).unbind('click');                        
                    });
                },

                refresh: function (context) {
                    methods.rebind(context);

                    methods.removeResizeElements(context);

                    if (!currenttd) {
                        if (settings.afterRefresh) {
                            settings.afterRefresh(null);
                        }
                        return;
                    }

                    var td = currenttd;

                    var tdHeight = $(td).outerHeight();
                    var tdWidth = $(td).outerWidth();
                    var iframePos = context.$ifrm.offset();
                    var tdPosition = $(td).offset();
                    var ifrmScrollTop = context.$ifrmBody.scrollTop();
                    var ifrmScrollLeft = context.$ifrmBody.scrollLeft();

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='td-resize-selector' style='position:absolute;top:" + (iframePos.top + tdPosition.top - ifrmScrollTop + tdHeight) + "px;left:" + (iframePos.left + tdPosition.left - ifrmScrollLeft + tdWidth) + "px;border:solid 2px red;width:6px;height:6px;cursor:se-resize;z-index:1000;'></span>");

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='td-resize-region' style='position:absolute;z-index:1000;top:" + (iframePos.top + tdPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + tdPosition.left - ifrmScrollLeft) + "px;border:dashed 1px green;width:" + tdWidth + "px;height:0px;'></span>");
                    context.$docBody.append("<span data-guid='" + context.guid + "' class='td-resize-region' style='position:absolute;z-index:1000;top:" + (iframePos.top + tdPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + tdPosition.left - ifrmScrollLeft) + "px;border:dashed 1px green;width:0px;height:" + tdHeight + "px;'></span>");

                    context.$docBody.append("<span data-guid='" + context.guid + "' class='td-resize-region' style='position:absolute;z-index:1000;top:" + (iframePos.top + tdPosition.top - ifrmScrollTop) + "px;left:" + (iframePos.left + tdPosition.left - ifrmScrollLeft + tdWidth) + "px;border:dashed 1px green;width:0px;height:" + tdHeight + "px;'></span>");
                    context.$docBody.append("<span data-guid='" + context.guid + "' class='td-resize-region' style='position:absolute;z-index:1000;top:" + (iframePos.top + tdPosition.top - ifrmScrollTop + tdHeight) + "px;left:" + (iframePos.left + tdPosition.left - ifrmScrollLeft) + "px;border:dashed 1px green;width:" + tdWidth + "px;height:0px;'></span>");

                    lastCrc = methods.crc(context.$ifrmBody.html());

                    if (settings.afterRefresh) {
                        settings.afterRefresh(currenttd);
                    }
                },

                reset: function (context) {
                    if (currenttd != null) {
                        currenttd = null;
                        tdResizeinProgress = false;
                        methods.removeResizeElements(context);

                        if (settings.afterReset) {
                            settings.afterReset();
                        }
                    }

                    methods.rebind(context);
                },

                crc: function (str) {
                    var hash = 0;
                    if (str.length == 0) return hash;
                    for (i = 0; i < str.length; i++) {
                        char = str.charCodeAt(i);
                        hash = ((hash << 5) - hash) + char;
                        hash = hash & hash;
                    }
                    return hash;
                }

            };

            var ifrm = this;
            var $ifrm = $(this);
            var $docBody = $("body");
            var $ifrmBody = $ifrm.contents().find("body");

            lastCrc = methods.crc($ifrmBody.html());

            if (!$ifrm.is('iframe')) {
                return;
            }

            var context = {
                ifrm: ifrm,
                $ifrm: $ifrm,
                $docBody: $docBody,
                $ifrmBody: $ifrmBody,
                guid: methods.guidGenerator()
            };

            ifrm.contentWindow.addEventListener('scroll', function () {
                methods.reset(context);
            }, false);

            $(ifrm.contentWindow.document).mouseup(function (e) {
                if (!tdResizeinProgress) {
                    var x = (e.x) ? e.x : e.clientX;
                    var y = (e.y) ? e.y : e.clientY;
                    var mouseUpElement = ifrm.contentWindow.document.elementFromPoint(x, y);
                    if (mouseUpElement) {
                        var matchingElement;
                        var $select = context.$ifrm.contents().find(settings.selector);
                        var $parentsSelect = $(mouseUpElement).parents();
                        for (var psi = 0; psi < $parentsSelect.length; psi++) {
                            for (var i = 0; i < $select.length; i++) {
                                if ($select[i] == $parentsSelect[psi]) {
                                    matchingElement = $select[i];
                                    break;
                                }
                            }
                            if (matchingElement) {
                                break;
                            }
                        }
                        if (!matchingElement) {
                            methods.reset(context);
                        }
                        else {
                            methods.tdClick(context, matchingElement);
                        }
                    }
                }                
            });

            $(document).keyup(function (e) {
                if (e.keyCode == 27) {
                    methods.reset(context);
                }
            });

            if (!ifrm.crcChecker) {
                ifrm.crcChecker = setInterval(function () {
                    var currentCrc = methods.crc($ifrmBody.html());
                    if (lastCrc != currentCrc) {
                        $ifrm.trigger('webkitresize-crcchanged', [currentCrc]);
                    }
                }, 1000);
            }

            $(window).resize(function(){
                methods.reset(context);
            });

            $ifrm.bind('webkitresize-crcchanged', function (event, crc) {
                lastCrc = crc;
                methods.reset(context);
            });

            $ifrm.bind('webkitresize-updatecrc', function (event, crc) {
                lastCrc = crc;
            });

            $ifrm.bind('webkitresize-table-resized', function () {
                methods.reset(context);
            });

            methods.refresh(context);

            ifrm._WebKitTdResizeEnd = function(){ methods.reset(context); methods.removeResizeElements(context); methods.debind(context);  }
            ifrm._WebKitImageResizeStart = function(){ methods.reset(context); context.$ifrmBody = $ifrm.contents().find("body"); }
        });
    };
})(jQuery);