package com.kinnara.kecakplugins.heatmapreportmenu;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.report.model.ReportWorkflowActivityInstance;
import org.joget.report.service.ReportManager;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by akbar on 7/18/2017.
 */
public class ReportHelper extends Element implements PluginWebSupport {

    private ApplicationContext applicationContext;
    private WorkflowManager    workflowManager;

    private List<String> xmlData = new ArrayList<>();

    //
    private void setXMl(final String workflowProcessId) {
        WorkflowProcess detailedWorkflowProcess = workflowManager.getRunningProcessById(workflowProcessId);
        xmlData.add(new String(workflowManager.getPackageContent(detailedWorkflowProcess.getPackageId(), detailedWorkflowProcess.getVersion())));
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

    @Override
    public void webService(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws ServletException, IOException {
        applicationContext = AppUtil.getApplicationContext();
        workflowManager = (WorkflowManager) applicationContext.getBean("workflowManager");

        String appId      = request.getParameter("appId");
        String appVersion = request.getParameter("appVersion");
        String processId  = request.getParameter("processId");

        ReportManager                        reportManager = (ReportManager) applicationContext.getBean("reportManager");
        List<ReportWorkflowActivityInstance> instances     = new ArrayList<>(reportManager.getReportWorkflowActivityInstanceList(appId, appVersion, processId, null, null, null, null, null));

        JSONObject                json = new JSONObject();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, ActivityInfo> map  = new TreeMap<>();

        int  totalHitCount = 0;
        long totalLeadTime = 0l;

        for (ReportWorkflowActivityInstance each : instances) {
            if (each.getState().equalsIgnoreCase("closed.completed")) {
                String  activityId = each.getReportWorkflowActivity().getActivityDefId();
                Boolean isNew      = map.get(activityId) == null;

                setXMl(each.getReportWorkflowProcessInstance().getInstanceId());

                ActivityInfo activityInfo = isNew ? new ActivityInfo() : map.get(activityId);
                activityInfo.setActivityId(activityId);
                activityInfo.setActivityHitCount(activityInfo.getActivityHitCount() + 1);
                activityInfo.setActivityLeadTime(each.getTimeConsumingFromCreatedTime());

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
            json.accumulate("XML", getXMl());
            json.accumulate("activities", list);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        response.getWriter().write(json.toString());


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
