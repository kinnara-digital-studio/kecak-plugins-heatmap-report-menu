package com.kinnara.kecakplugins.heatmapreportmenu;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by akbar on 7/18/2017.
 */
public class ReportHelper extends Element implements PluginWebSupport {

    private ApplicationContext appContext;
    private WorkflowManager    workflowManager;
    //
    private SimpleDateFormat          dateFormat       = new SimpleDateFormat("dd MMM yyyy (HH:mm)");
    private String                    appID            = "";
    private Integer                   totalHitCount    = 0;
    private Long                      totalLeadTime    = 0l;
    //
    private List<String>              xmlData          = new ArrayList<>();
    private List<WorkflowActivity>    activityList     = new ArrayList<>();
    private List<WorkflowProcess>     processList      = new ArrayList<>();
    private List<Map<String, Object>> activityJson     = new ArrayList<>();
    //
    private Map<String, ActivityInfo> filteredActivity = new TreeMap<>();

    //
    //
    //
    private void setXMl(final WorkflowProcess workflowProcess) {
        WorkflowProcess detailedWorkflowProcess = workflowManager.getRunningProcessById(workflowProcess.getInstanceId());

        xmlData.add(new String(workflowManager.getPackageContent(detailedWorkflowProcess.getPackageId(), detailedWorkflowProcess.getVersion())));
    }

    private List<WorkflowActivity> getActivities(final WorkflowProcess workflowProcess) {
        activityList.clear();

        //Loop to get list of Normal Activity & Basic Info
        for (WorkflowActivity workflowActivity : workflowManager.getProcessActivityDefinitionList(workflowProcess.getId())) {
            if (workflowActivity.getType() == WorkflowActivity.TYPE_NORMAL) {
                Boolean      isNew        = filteredActivity.get(workflowActivity.getActivityDefId()) == null;
                ActivityInfo activityInfo = isNew ? new ActivityInfo() : filteredActivity.get(workflowActivity.getActivityDefId());

                activityInfo.setActivityId(workflowActivity.getActivityDefId());
                activityInfo.setActivityName(workflowActivity.getName());
                activityInfo.setActivityHitCount(isNew ? 0 : activityInfo.getActivityHitCount() + 1);

                filteredActivity.put(workflowActivity.getActivityDefId(), activityInfo);
            }
        }

        //Loop to get Activities based on Normal Acivity and extra info
        for (WorkflowActivity workflowActivity : workflowManager.getActivityList(workflowProcess.getInstanceId(), 0, 0, "", false)) {
            WorkflowActivity detail = workflowManager.getRunningActivityInfo(workflowActivity.getId());

            ActivityInfo activityInfo;
            if ((activityInfo = filteredActivity.get(workflowActivity.getActivityDefId())) != null) {
                activityInfo.setActivityLeadTime(detail.getTimeConsumingFromDateCreatedInSeconds());
                activityInfo.setActivityDue(workflowActivity.getLimitInSeconds());
                activityInfo.setActivityDateCreated(dateFormat.format(workflowActivity.getCreatedTime()));
                activityInfo.setActivityUser(detail.getAssignmentUsers());

                activityList.add(workflowActivity);
            }
        }

        //Check which activity that doesn't have created date (tool?)
        List<String> keys = new ArrayList<>();
        for (String key : filteredActivity.keySet()) {
            if (filteredActivity.get(key).getActivityDateCreated().trim().isEmpty()) keys.add(key);
        }

        //Remove corresponding activity
        for (String key : keys) {
            filteredActivity.remove(key);
        }

        return activityList;
    }

    private List<WorkflowProcess> getProcesses(final String appID, final ProcessType processType) {
        processList.clear();

        if (processType == ProcessType.COMPLETED || processType == ProcessType.ALL) processList.addAll(workflowManager.getCompletedProcessList(appID, "", "", "", "", false, 0, 0));
        if (processType == ProcessType.RUNNING || processType == ProcessType.ALL) processList.addAll(workflowManager.getRunningProcessList(appID, "", "", "", "", false, 0, 0));

        return processList;
    }

    private String getXMl() {
        int    max = 0;
        String xml = "";

        for (String each : xmlData) {
            if (each.length() > max) {
                max = each.length();
                xml = each;
            }
        }

        return xml;
    }

    private String response() throws JSONException {
        for (WorkflowProcess workflowProcess : getProcesses(appID, ProcessType.ALL)) {
            getActivities(workflowProcess);
            setXMl(workflowProcess);
        }

        for (String each : filteredActivity.keySet()) {
            totalHitCount += filteredActivity.get(each).getActivityHitCount();
            totalLeadTime += filteredActivity.get(each).getActivityLeadTime();
            activityJson.add(filteredActivity.get(each).toJson());
        }

        JSONObject response = new JSONObject();
        response.accumulate("AppID", appID);
        response.accumulate("XML", getXMl());
        response.accumulate("totalHitCount", totalHitCount);
        response.accumulate("totalLeadTime", totalLeadTime);
        response.accumulate("activities", activityJson);

        return response.toString();
    }

    @Override
    public void webService(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws ServletException, IOException {
        appContext = AppUtil.getApplicationContext();
        workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
//        ReportManager reportManager = (ReportManager) appContext.getBean("reportManager");

        appID = request.getParameter("appId") == null ? "" : request.getParameter("appId");
        try {
            if (!appID.isEmpty()) response.getWriter().write(response());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //<editor-fold desc="Commons Getter" defaultstate="collapsed">
    @Override
    public String renderTemplate(FormData formData, Map map) {
        return null;
    }

    @Override
    public String getName() {
        return "Kecak Report Helper";
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return "Artifact ID: " + getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getClassName() {
        return getName();
    }

    @Override
    public String getPropertyOptions() {
        return null;
    }
    //</editor-fold>
}
