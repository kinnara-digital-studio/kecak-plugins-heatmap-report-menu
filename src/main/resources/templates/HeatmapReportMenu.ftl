
    <div id="pviewer-container">
        <div id="viewport">
            <!--
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

             -->

            <input name="process" id="process" type="hidden" value="${processId!}">
            <input name="reportType" id="reportType" type="hidden" value="${reportType!}">

            <label style="margin-left:16px;">Period:</label>
            <input name="startDate" id="dateCreatedFilter" class="datetimepicker" type="text" value="${valueFrom!?html}" placeholder="From (${dateFormat})" readonly>
            <input name="finishDate" id="dateFinishedFilter" class="datetimepicker" type="text" value="${valueTo!?html}" placeholder="To (${dateFormat})" readonly>


            <button style="margin-left:16px;" onclick="prepareHeatMap()">Show</button>

            <hr/>
            <img id="loading" src="${request.contextPath}/plugin/${className}/img/loading.gif" style="visibility: hidden;">
            <div id="canvas"></div>
        </div>
    </div>


<link rel="stylesheet" type="text/css" href="${request.contextPath}/js/font-awesome/css/font-awesome.min.css"/>
<link rel="stylesheet" type="text/css" href="${request.contextPath}/pbuilder/css/pbuilder.css"/>
<link rel="stylesheet" type="text/css" href="${request.contextPath}/plugin/${className}/bower_components/smalot-bootstrap-datetimepicker/css/bootstrap-datetimepicker.css">

<style>
    #loading {
        background : transparent !important;
        position   : fixed !important;
        top        : 50% !important;
        left       : 50% !important;
        transform  : translateX(-50%) !important;
        transform  : translateY(-50%) !important;
    }
    #viewport {
        top: unset !important;
        margin-left: 0px !important;
        height: 80% !important;
        width:80% !important;
    }
    ::-webkit-scrollbar {
        width: 0px;  /* remove scrollbar space */
        background: transparent;  /* optional: just make scrollbar invisible */
    }
    #canvas{
        display:table;
    }
</style>

<script src="${request.contextPath}/js/JSONError.js"></script>
<script src="${request.contextPath}/js/JSON.js"></script>
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
    const config = {
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
     };
    var jsonXpdl = ${jsonXpdl!};
    var heatmap = null;

    var firstTime = true;
    function fillHeatMap(json) {
        if (firstTime) {
            $("#processList option:first").remove();
            firstTime = false;
        }

        var points = [];
        $.each(json.activities, function (index, each) {
            let hitCount = each.activityAverageHitCount;
            let leadTime = each.activityAverageLeadTime;
            let activityId = each.activityId;
            let div = $("div#node_" + activityId);
            // let baseWidth = parseInt($("div.participant_handle_vertical")[0].offsetHeight);
            let elBefore = div.parent().prevAll();
            let top = 0;

            /*
            for(let i = 0; i < elBefore.length; i++){
                let e = elBefore[i];
                let height = $(e).height();
                top += height;
            }
            */

            // elBefore.map(e => $(e)).map($e => $e.height()).filter(e => e).forEach(e => top += e) ;

            let width = div.width();
            let height = div.height();

            let point = {
                x : parseInt(div.css("left").replace('px', '')) + (width / 2) + 15,
                y : top + parseInt(div.css("top").replace('px', '')) + (height / 2) + 30,
                // value: $("#reportType").val() === "hitCount" ? hitCount : leadTime
                value : hitCount
            };

            points.push(point);
        });

        let data = {
            max : 100,
            data: points
        };

        heatmap.setData(data);
    }

    function getOffset(el) {
        let rect = el.getBoundingClientRect();
        return {
            left: rect.left,
            top : rect.top
        };
    }

    function prepareHeatMap() {
        let processList = $("#process-list").val();

        if ((processList + "").length > 0) {
            $("#loading").css("visibility", "visible");
            $("#canvas").css("visibility", "hidden");

            var processId  = $("#processList").val();
            var startDate  = $("#dateCreatedFilter").val();
            var finishDate = $("#dateFinishedFilter").val();

            startDate  = startDate || "";
            finishDate = finishDate || "";

            var urlQuery = "${request.contextPath}/web/json/app/${appID}/${appVersion}/plugin/${dataProvider}/service?action=getHeatmapData&processId=${processId!}&startDate=" + encodeURI(startDate) + "&finishDate=" + encodeURI(finishDate);
            $.getJSON(urlQuery, function (response) {
                // ProcessBuilder.Designer.init(response.XML);

                ProcessBuilder.Actions.viewProcess(processId);

                $('#canvas').css('zoom', '70%');

                if(!heatmap) {
                    heatmap = h337.create(config);
                }

                $('#canvas').css('zoom', '100%');

                $(".quickEdit").css("z-index", 9);

                $("#loading").css("visibility", "hidden");
                $("#canvas").css("visibility", "visible");
                fillHeatMap(response);
                heatmap.repaint();
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

        ProcessBuilder.ApiClient.baseUrl = ProcessBuilder.ApiClient.designerBaseUrl = "${request.contextPath}";
        ProcessBuilder.Designer.editable = false;
        ProcessBuilder.Designer.init(`${xpdl!}`);
        ProcessBuilder.Designer.setZoom(0.7);
        ProcessBuilder.Actions.viewProcess("${processId!}");
        $("#subheader_list").css("visibility", "hidden");

        // Init HeatMap
        <#-- this confuses me, why do we need to initiate here? -->
        $('#canvas').css('zoom', '100%');


        h337.create(config);

    });
</script>
