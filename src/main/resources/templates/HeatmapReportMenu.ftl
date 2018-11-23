<div id="pviewer-container">
    <div id="viewport" style="margin-top: 0; margin-left: 0; top: 112px;">
        <label>Process:</label>
        <select id="processList">
            <option value="">Choose process</option>
        <#list processList as each>
            <option value="${each.processId}">${each.processName}</option>
        </#list>
        </select>

        <label style="margin-left:16px;">Report Type:</label>
        <select id="reportType">
            <option value="hitCount">Hit Count</option>
            <option value="leadTime">Lead Time</option>
        </select>

        <label style="margin-left:16px;">Period:</label>
        <input name="startDate" id="dateCreatedFilter" class="datetimepicker" type="text" value="${valueFrom!?html}" placeholder="From (${dateFormat})" readonly>
        <input name="finishDate" id="dateFinishedFilter" class="datetimepicker" type="text" value="${valueTo!?html}" placeholder="To (${dateFormat})" readonly>

        <button style="margin-left:16px;" onclick="prepareHeatMap()">Show</button>

        <hr/>

        <div style="background: transparent;">
            <img id="loading" src="${request.contextPath}/plugin/${className}/img/loading.gif" style="visibility: hidden;">
            <div id="canvas" style="visibility: hidden"></div>
        </div>
    </div>
</div>

<link rel="stylesheet" type="text/css" href="${request.contextPath}/js/font-awesome/css/font-awesome.min.css"/>
<link rel="stylesheet" type="text/css" href="${request.contextPath}/pbuilder/css/pbuilder.css"/>
<link rel="stylesheet" type="text/css" href="${request.contextPath}/plugin/${className}/bower_components/smalot-bootstrap-datetimepicker/css/bootstrap-datetimepicker.css">

<style>
    #loading {
        background : transparent;
        position   : fixed;
        top        : 50%;
        left       : 50%;
        transform  : translateX(-50%);
        transform  : translateY(-50%);
    }
</style>

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
<script src="${request.contextPath}/plugin/${className}/bower_components/smalot-bootstrap-datetimepicker/js/bootstrap-datetimepicker.js"></script>
<script>
    var json    = null;
    var heatmap = null;

    var firstTime = true;
    function fillHeatMap() {
        if (firstTime) {
            $("#processList option:first").remove();
            firstTime = false;
        }

        var points = [];
        $.each(json.activities, function (index, each) {
            console.log(each);
            var hitCount = each.activityAverageHitCount;
            var leadTime = each.activityAverageLeadTime;

            var div       = $("div#node_" + each.activityId);
            var baseWidth = parseInt($("div.participant_handle_vertical")[0].offsetHeight);

            var point = {
                x    : baseWidth + parseInt(div.css("left").replace('px', '')),
                y    : getOffset(div[0]).top,
                value: $("#reportType").val() === "hitCount" ? hitCount : leadTime
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

    function prepareHeatMap() {
        if (($("#process-list").val() + "").length > 0) {

            $("#loading").css("visibility", "visible");
            $("#canvas").css("visibility", "hidden");

            var processId  = $("#processList").val();
            var startDate  = $("#dateCreatedFilter").val();
            var finishDate = $("#dateFinishedFilter").val();

            startDate  = startDate || "";
            finishDate = finishDate || "";

            var urlQuery = "${request.contextPath}/web/json/plugin/${dataProvider}/service?appId=${appID}&appVersion=${appVersion}&processId=" + processId + "&startDate=" + encodeURI(startDate) + "&finishDate=" + encodeURI(finishDate);
            console.log(urlQuery);
            $.getJSON(urlQuery, function (response) {
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

                $("#loading").css("visibility", "hidden");
                $("#canvas").css("visibility", "visible");
                fillHeatMap();
            })
        }
    }

    $(document).ready(function () {
        $(".datetimepicker").datetimepicker({
            format        : "${dateFormat}",
            autoclose     : true,
            todayBtn      : true,
            pickerPosition: "bottom-left",
            minView       : 'day'
        });

        $("#dateCreatedFilter").datetimepicker().on("changeDate", function (e) {
            $("#dateFinishedFilter").datetimepicker('setStartDate', e.date);
        });
    });
</script>
