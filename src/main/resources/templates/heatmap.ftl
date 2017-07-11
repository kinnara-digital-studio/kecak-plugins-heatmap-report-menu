<textarea id="xpdl" cols="240" rows="32" style="display:none">${xpdl}</textarea>

<div id="pviewer-container">
    <div id="viewport" style="margin-left: 0px">
        <div id="canvas"></div>
    </div>
</div>

<link href="${request.contextPath}/js/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css"/>
<link href="${request.contextPath}/pbuilder/css/pbuilder.css" rel="stylesheet"/>
<script src="${request.contextPath}/js/JSONError.js"></script>
<script src="${request.contextPath}/js/JSON.js"></script>
<script src="${request.contextPath}/js/jquery/jquery-1.9.1.min.js"></script>
<script src="${request.contextPath}/js/jquery/jquery-migrate-1.2.1.min.js"></script>
<script src="${request.contextPath}/js/jquery/ui/jquery-ui-1.10.3.min.js"></script>
<script src="${request.contextPath}/js/jquery/jquery.jeditable.js"></script>
<script src="${request.contextPath}/pbuilder/js/jquery.jsPlumb-1.6.4-min.js"></script>
<script src="${request.contextPath}/pbuilder/js/html2canvas-0.4.1.js"></script>
<script src="${request.contextPath}/pbuilder/js/jquery.plugin.html2canvas.js"></script>
<script src="${request.contextPath}/pbuilder/js/rgbcolor.js"></script>
<script src="${request.contextPath}/pbuilder/js/StackBlur.js"></script>
<script src="${request.contextPath}/pbuilder/js/canvg.js"></script>
<script src="${request.contextPath}/web/console/i18n/peditor"></script>
<script src="${request.contextPath}/js/jquery/jquery.propertyeditor.js"></script>
<script src="${request.contextPath}/js/json/util.js"></script>
<script src="${request.contextPath}/pbuilder/js/undomanager.js"></script>
<script src="${request.contextPath}/pbuilder/js/jquery.format.js"></script>
<script src="${request.contextPath}/web/console/i18n/pbuilder"></script>
<script src="${request.contextPath}/pbuilder/js/pbuilder.js"></script>
<script src="${request.contextPath}/plugin/${className}/bower_components/heatmap.js-amd/build/heatmap.min.js"></script>
<script>
    function getOffset(el) {
        el = el.getBoundingClientRect();
        return {
            left: el.left,
            top : el.top - (el.height / 1.25)
        }
    }

    $(document).ready(function () {
        var activities = ${listActivity};
        var xpdl       = $("#xpdl").text();
        var points     = [];

        ProcessBuilder.ApiClient.baseUrl         = "${request.contextPath}";
        ProcessBuilder.ApiClient.designerBaseUrl = "${request.contextPath}";
        ProcessBuilder.Designer.setZoom(1);
        ProcessBuilder.Designer.editable = false;

        if (xpdl && xpdl !== '') {
            ProcessBuilder.Designer.init(xpdl);
//      ProcessBuilder.Designer.setZoom(0.7);

            $.when(ProcessBuilder.Actions.viewProcess('${appID}')).done(function () {
                $.each(activities, function (index, each) {

                    var div = $('div').filter(function () {
                        return ($(this).attr('title') + '').toLowerCase().indexOf((each.key + '').toLowerCase()) !== -1;
                    });

//          var div        = $("div[title='activity" + each.key + "']");
                    var isActivity = div.hasClass("activity");

                    if (isActivity) {
                        var baseWidth = parseInt($("div.participant_handle_vertical")[0].offsetHeight);

                        var point = {
                            x    : baseWidth + parseInt(div.css("left").replace(/\D/g, '')),
                            y    : getOffset(div[0]).top,
                            value: each.value
                        };
                        points.push(point);
                    }
                });

                var data = {
                    max : ${maxActivity},
                    data: points
                };

                console.log(data);

                var heatmapInstance = h337.create({
                    container: document.querySelector('#canvas')
                });
                heatmapInstance.setData(data);

                $(".quickEdit").css("z-index", 9);
            });

        }
    });
</script>
<#--<div id="builder-message"></div>-->
<#--<div id="builder-screenshot"></div>-->


