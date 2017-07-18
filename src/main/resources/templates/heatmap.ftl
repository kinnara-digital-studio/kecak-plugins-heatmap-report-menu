<div id="pviewer-container">
    <div id="viewport" style="margin-top: 0; margin-left: 0; top: 112px;">
        Process:
        <select onchange="prepareHeatMap($(this).val())">
            <option value="">Choose process</option>
        <#list processList as each>
            <option value="${each}">${each}</option>
        </#list>
        </select>

        Report Type:
        <select onchange="fillHeatMap($(this).val())">
            <option value="hitCount">Hit Count</option>
            <option value="leadTime">Lead Time</option>
        </select>

        <hr/>

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
    var json    = null;
    var heatmap = null;

    function fillHeatMap(sourceType) {
        var points = [];
        $.each(json.activities, function (index, each) {
            var hitCount = each.activityAverageHitCount;
            var leadTime = each.activityAverageLeadTime;

            var div       = $("div#node_" + each.activityId);
            var baseWidth = parseInt($("div.participant_handle_vertical")[0].offsetHeight);

            var point = {
                x    : baseWidth + parseInt(div.css("left").replace('px', '')),
                y    : getOffset(div[0]).top,
                value: sourceType === "hitCount" ? hitCount : leadTime
            };
            points.push(point);
        });

        var data = {
            max : 100,
            data: points
        };

        heatmap.setData(data);
    }

    function getOffset(el) {
        el = el.getBoundingClientRect();
        return {
            left: el.left,
            top : (el.top - 112) - (el.height / 2)
        }
    }

    function prepareHeatMap(processId) {
        if (($("#select-process").val() + "").length > 0) {
            $.getJSON("${request.contextPath}/web/json/plugin/${dataProvider}/service?appId=${appID}&appVersion=${appVersion}&processId=" + processId, function (response) {
                json                                     = response;
                ProcessBuilder.ApiClient.baseUrl         = "${request.contextPath}";
                ProcessBuilder.ApiClient.designerBaseUrl = "${request.contextPath}";

                ProcessBuilder.Designer.setZoom(1);
                ProcessBuilder.Designer.editable = false;
                ProcessBuilder.Designer.init(response.XML);

                ProcessBuilder.Actions.viewProcess(processId);

                // Init HeatMap
                $('#canvas').css('zoom', '50%');
                heatmap = h337.create({
                    container : document.querySelector('#canvas'),
                    minOpacity: 0,
                    maxOpacity: 0.5,
                    blur      : 0.5,
                    gradient  : {
                        '.0' : 'blue',
                        '.25': 'green',
                        '.5' : 'yellow',
                        '.75': 'orange',
                        '1'  : 'red',
                    },
                    radius    : 50
                });
                $('#canvas').css('zoom', '100%');

                $(".quickEdit").css("z-index", 9);

                fillHeatMap("hitCount")
            })
        }
    }
</script>
