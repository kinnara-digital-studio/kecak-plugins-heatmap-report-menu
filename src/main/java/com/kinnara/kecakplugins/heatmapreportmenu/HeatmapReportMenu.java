package com.kinnara.kecakplugins.heatmapreportmenu;

import com.kinnarastudio.commons.Try;
import com.kinnarastudio.commons.jsonstream.JSONCollectors;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.report.dao.ReportWorkflowActivityInstanceDao;
import org.joget.report.model.ReportWorkflowActivityInstance;
import org.joget.report.service.ReportManager;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Heatmap Report Menu
 */
public class HeatmapReportMenu extends UserviewMenu implements PluginWebSupport {
    public final static String LABEL = "Heatmap Report";
    public final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static String BEGIN_OF_TIME = "1970-01-01 00:00:00";
    public final static String END_OF_TIME = "9999-12-31 23:59:59";

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/HeatmapReportMenu.json", new String[]{getClassName()}, false, "/messages/HeatmapReportMenu");
    }

    @Override
    public String getName() {
        return LABEL;
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getCategory() {
        return "Kecak";
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getRenderPage() {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        PackageDefinition packageDefinition = appDefinition.getPackageDefinition();
        Long packageVersion = packageDefinition != null ? packageDefinition.getVersion() : new Long(1);
        Collection<WorkflowProcess> processList = workflowManager.getProcessList(appDefinition.getAppId(), packageVersion.toString());

        Map<String, Map<String, String>> eliminator = new HashMap<>();
        for (WorkflowProcess each : processList) {
            Map<String, String> temp = new TreeMap<>();

            temp.put("processId", each.getIdWithoutVersion());
            temp.put("processName", each.getName());

            eliminator.put(each.getIdWithoutVersion(), temp);
        }

        List<Map<String, String>> json = new ArrayList<>();
        for (String key : eliminator.keySet()) {
            json.add(eliminator.get(key));
        }

        Map<String, Object> dataModel = new LinkedHashMap<>();
        dataModel.put("appID", appDefinition.getAppId());
        dataModel.put("appVersion", appDefinition.getVersion());
        dataModel.put("className", getClassName());
        dataModel.put("dateFormat", "yyyy-mm-dd hh:ii:ss");
        dataModel.put("dataProvider", getClassName());
        dataModel.put("processList", json);

        String reportType = getPropertyString("reportType");
        dataModel.put("reportType", reportType);

        WorkflowProcess workflowProcess = processList.stream()
                .filter(p -> getPropertyString("process").equals(p.getIdWithoutVersion()))
                .findAny()
                .orElse(null);

        dataModel.put("request", request);

        if (workflowProcess != null) {
            final String processId = workflowProcess.getIdWithoutVersion();
            dataModel.put("processId", processId);
            byte[] xpdlBytes = workflowManager.getPackageContent(workflowProcess.getPackageId(), workflowProcess.getVersion());
            if (xpdlBytes != null) {
                try {
                    JSONObject j = new JSONObject();
                    j.put("jsonXpdl", new String(xpdlBytes, StandardCharsets.UTF_8));
                    dataModel.put("jsonXpdl", j);
                    dataModel.put("xpdl", new String(xpdlBytes, StandardCharsets.UTF_8).replaceAll("\"", "\\\\\""));
                } catch (JSONException e) {
                    LogUtil.error(getClassName(), e, e.getMessage());
                }

                try {
                    JSONObject heatmapData = getHeatmapData(processId, dateFormat.parse(BEGIN_OF_TIME), dateFormat.parse(END_OF_TIME));
                    dataModel.put("heatmapData", heatmapData.toString());
                } catch (RestApiException | ParseException e) {
                    LogUtil.error(getClassName(), e, e.getMessage());
                }
            }
        }

        String csrfParameter = SecurityUtil.getCsrfTokenName();
        String csrfToken = SecurityUtil.getCsrfTokenValue(request);
        dataModel.put("csrfParameter", csrfParameter);
        dataModel.put("csrfToken", csrfToken);
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/HeatmapReportMenu.ftl", null);
    }

    @Override
    public boolean isHomePageSupported() {
        return true;
    }

    @Override
    public String getDecoratedMenu() {
        return null;
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (!"GET".equalsIgnoreCase(request.getMethod())) {
                throw new RestApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method [" + request.getMethod() + "] is not supported");
            }

            String action = getRequiredParameter(request, "action");
            if ("getProcesses".equalsIgnoreCase(action)) {
                JSONArray jsonArray = getProcesses();
                response.getWriter().write(jsonArray.toString());
            } else {
                throw new RestApiException(HttpServletResponse.SC_BAD_REQUEST, String.format("Action [%s] not supported", action));
            }
        } catch (RestApiException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            response.sendError(e.getErrorCode(), e.getMessage());
        }
    }

    protected JSONObject getHeatmapData(String processId, Date startDate, Date finishDate) throws RestApiException {
        ApplicationContext applicationContext = AppUtil.getApplicationContext();
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        WorkflowManager workflowManager = (WorkflowManager) applicationContext.getBean("workflowManager");
        ReportManager reportManager = (ReportManager) applicationContext.getBean("reportManager");
        ReportWorkflowActivityInstanceDao reportWorkflowActivityInstanceDao = (ReportWorkflowActivityInstanceDao) applicationContext.getBean("reportWorkflowActivityInstanceDao");

        JSONObject json = new JSONObject();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, ActivityInfo> map = new TreeMap<>();

        int totalHitCount = 0;
        long totalLeadTime = 0L;

        if (workflowManager == null) {
            throw new RestApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "WorkflowManager is null");
        }

        @Nonnull final Collection<ReportWorkflowActivityInstance> instances = Optional.ofNullable(reportManager.getReportWorkflowActivityInstanceList(appDefinition.getAppId(), appDefinition.getVersion().toString(), processId, null, null, startDate, finishDate, null, null, null, null))
                .orElseGet(ArrayList::new);

        for (ReportWorkflowActivityInstance each : instances) {
            String processDefId = workflowManager.getProcessDefIdByInstanceId(each.getReportWorkflowProcessInstance().getInstanceId());
            WorkflowActivity activityDefinition = processDefId == null ? null : workflowManager.getProcessActivityDefinition(processDefId, each.getReportWorkflowActivity().getActivityDefId());
            if (activityDefinition != null && WorkflowActivity.TYPE_NORMAL.equals(activityDefinition.getType())) {
                String activityId = each.getReportWorkflowActivity().getActivityDefId();

                ActivityInfo activityInfo = Optional.ofNullable(map.get(activityId)).orElseGet(ActivityInfo::new);
                activityInfo.setActivityId(activityId);
                activityInfo.setActivityHitCount(activityInfo.getActivityHitCount() + 1);
                activityInfo.setActivityLeadTime(each.getTimeConsumingFromCreatedTime());
                activityInfo.setStartDate(each.getStartedTime());
                activityInfo.setFinishDate(each.getFinishTime());

                totalHitCount++;
                totalLeadTime += each.getTimeConsumingFromCreatedTime();

                map.put(activityId, activityInfo);
            }
        }

        for (String key : map.keySet()) {
            ActivityInfo activityInfo = map.get(key);
            activityInfo.setActivityAverageHitCount((double) (activityInfo.getActivityHitCount() * 100) / totalHitCount);
            activityInfo.setActivityAverageLeadTime((double) (activityInfo.getActivityLeadTime() * 100) / totalLeadTime);

            list.add(activityInfo.toJson());
        }

        try {
            json.accumulate("activities", list);
        } catch (JSONException e) {
            throw new RestApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return json;
    }

    protected JSONArray getProcesses() throws RestApiException {
        boolean isAdmin = WorkflowUtil.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN);
        if (!isAdmin) {
            throw new RestApiException(HttpServletResponse.SC_UNAUTHORIZED, "Current user is not admin");
        }

        ApplicationContext applicationContext = AppUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) applicationContext.getBean("workflowManager");
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        PackageDefinition packageDefinition = appDefinition.getPackageDefinition();
        long packageVersion = Optional.of(packageDefinition).map(PackageDefinition::getVersion).orElse(1L);
        Collection<WorkflowProcess> processList = workflowManager.getProcessList(appDefinition.getAppId(), Long.toString(packageVersion));

        return Optional.ofNullable(processList)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(Try.onFunction(p -> {
                    JSONObject option = new JSONObject();
                    option.put("value", p.getIdWithoutVersion());
                    option.put("label", p.getName() + " (" + p.getIdWithoutVersion() + ")");
                    return option;
                }))
                .collect(JSONCollectors.toJSONArray());
    }

    /**
     * Get required parameter or throw {@link RestApiException}
     *
     * @param request
     * @param parameterName
     * @return
     * @throws RestApiException
     */
    protected String getRequiredParameter(HttpServletRequest request, String parameterName) throws RestApiException {
        return optParameter(request, parameterName)
                .orElseThrow(() -> new RestApiException(HttpServletResponse.SC_BAD_REQUEST, String.format("Required parameter [%s] is not supplied", parameterName)));
    }

    /**
     * Get optional parameter
     *
     * @param request
     * @param parameterName
     * @return
     */
    protected Optional<String> optParameter(HttpServletRequest request, String parameterName) {
        return Optional.of(request)
                .map(r -> r.getParameter(parameterName))
                .filter(s -> !s.isEmpty());
    }

}
