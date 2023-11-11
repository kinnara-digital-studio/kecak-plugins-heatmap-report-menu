
<script src="${request.contextPath}/js/JSONError.js"></script>
<script src="${request.contextPath}/js/JSON.js"></script>
<script src="${request.contextPath}/js/jquery/jquery.jeditable.js"></script>
<script src="${request.contextPath}/js/chosen/chosen.jquery.js"></script>
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
<script src="${request.contextPath}/plugin/${className}/node_modules/heatmap.js/build/heatmap.min.js"></script>

<#if locale! != ''>
    <script type="text/javascript" src="${request.contextPath}/js/jquery/ui/i18n/jquery.ui.datepicker-${locale}.js"></script>
</#if>

<script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/js/jquery.placeholder.min.js"></script>
<link rel="stylesheet" href="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/css/datePicker.css" />
<link rel="stylesheet" href="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/css/jquery-ui-timepicker-addon.css" />
<link rel="stylesheet" type="text/css" href="${request.contextPath}/js/font-awesome/css/font-awesome.min.css"/>
<link rel="stylesheet" type="text/css" href="${request.contextPath}/pbuilder/css/pbuilder.css"/>
<script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/js/jquery-ui-timepicker-addon.js"></script>
<script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/js/jquery.custom.datepicker.js"></script>

<script type="text/javascript">
    $(document).ready(function() {
        $("input.datetimepicker").each(function(i, e) {
            let config = {
                 showOn: "focus",
                 buttonImage: "${request.contextPath}/css/images/calendar.png",
                 buttonImageOnly: true,
                 changeMonth: true,
                 changeYear: true,
                 timeInput: true,
                 dateFormat: "yy-mm-dd"
            };

            $(e).cdatepicker(config);
        });
    });
</script>

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


<div id="pviewer-container">
    <div id="viewport">
        <form name="filters_heatmap" class="filter_form" id="filters_heatmap" action="?" method="POST">
            <input name="process" id="process" type="hidden" value="${processId!}">
            <input name="reportType" id="reportType" type="hidden" value="${reportType!}">
            <input type="hidden" name="${csrfParameter}" value="${csrfToken}">

            <div style="float: left;padding-right: 10px;"> <input id="dateFromFilter" name="dateFromFilter" class="datetimepicker" type="text" value="${valueFrom!?html}" placeholder="From (${dateFormat})" readonly> </div>
            <div style="float: left;padding-right: 10px;"> <input id="dateToFilter" name="dateToFilter" class="datetimepicker" type="text" value="${valueTo!?html}" placeholder="To (${dateFormat})" readonly> </div>
            <input type="submit" class="waves-button-input" value="Show" style="background-color:rgba(0,0,0,0);">

            <hr/>

            <div id="canvas"></div>
        </form>
    </div>
</div>

<script>
    $(document).ready(function () {
        const HEATMAP_CONFIG = {
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

        ProcessBuilder.ApiClient.baseUrl = ProcessBuilder.ApiClient.designerBaseUrl = "${request.contextPath}";
        ProcessBuilder.Designer.editable = false;
        ProcessBuilder.Designer.init(`${xpdl!}`);
        ProcessBuilder.Designer.setZoom(0.7);
        ProcessBuilder.Actions.viewProcess("${processId!}");
        // $("#subheader_list").css("visibility", "hidden");

        // Init HeatMap
        <#-- this confuses me, why do we need to initiate here? -->
        $('#canvas').css('zoom', '100%');

        let heatmapData = ${heatmapData!'{}'};
        fillHeatMap(heatmapData, HEATMAP_CONFIG);
    });

    function getOffset(el) {
        let rect = el.getBoundingClientRect();
        return {
            left: rect.left,
            top : rect.top
        };
    }

    function fillHeatMap(json, config) {
        debugger;

        let points = [];
        $.each(json.activities, function (index, each) {
            let hitCount = each.activityAverageHitCount;
            let leadTime = each.activityAverageLeadTime;
            let activityId = each.activityId;
            let div = $("div#node_" + activityId);
            // let baseWidth = parseInt($("div.participant_handle_vertical")[0].offsetHeight);
            let elBefore = div.parent().prevAll();
            let top = 0;

            for(let e of elBefore) {
                let height = $(e).height();
                top += height;
            }

            let width = div.width();
            let height = div.height();

            let point = {
                x : Math.floor(parseInt(div.css("left").replace('px', '')) + (width / 2) + 15),
                y : Math.floor(top + parseInt(div.css("top").replace('px', '')) + (height / 2) + 30),
                value : ${reportType!'hitCount'}
            };

            points.push(point);
        });

        let data = {
            max : 100,
            data: points
        };

        let heatmap = h337.create(config);

        heatmap.setData(data);
    }
</script>