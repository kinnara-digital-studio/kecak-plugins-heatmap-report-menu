package com.kinnara.kecakplugins.heatmapreportmenu;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class HeatmapReportMenu extends UserviewMenu {

    //
    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        AppDefinition appDef     = AppUtil.getCurrentAppDefinition();
        String        appId      = appDef.getId();
        String        appVersion = appDef.getVersion().toString();
        return AppUtil.readPluginResource(getClassName(), "/properties/heatmap.json", new String[]{appId, appVersion}, false);

    }

    @Override
    public String getName() {
        return "Kecak Heatmap Report";
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return "Artifact ID : " + getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getCategory() {
        return "Kecak Enterprise";
    }

    @Override
    public String getIcon() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRenderPage() {
        ApplicationContext appContext    = AppUtil.getApplicationContext();
        AppDefinition      appDefinition = AppUtil.getCurrentAppDefinition();
        String             appID         = appDefinition.getAppId();

        PluginManager               pluginManager   = (PluginManager) appContext.getBean("pluginManager");
        WorkflowManager             workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        Collection<WorkflowProcess> processList     = workflowManager.getRunningProcessList(appID, "", "", "", "", false, 0, 0);
        processList.addAll(workflowManager.getCompletedProcessList(appID, "", "", "", "", false, 0, 0));

        JSONArray listActivity = new JSONArray();
        String    firstKey     = "";

        // State must not be aborted
        Map<String, Integer> mapActivity = new TreeMap<>();
        int                  total       = 0;
        for (WorkflowProcess each : processList) {

            Collection<WorkflowActivity> workflowDefList = workflowManager.getProcessActivityDefinitionList(each.getId());
            for (WorkflowActivity workflowActivity : workflowManager.getActivityList(each.getInstanceId(), 0, 0, "", false)) {
                //                workflowActivity.getState()
                if (isActivity(workflowDefList, workflowActivity.getActivityDefId())) {

//                    WorkflowActivity temp = workflowManager.getRunningActivityInfo(workflowActivity.getId());
//
//                    LogUtil.info(getClassName(), "Activity Def ID   : " + temp.getActivityDefId());
//                    LogUtil.info(getClassName(), "Activity ID       : " + temp.getId());
//                    LogUtil.info(getClassName(), "Time from created : " + Long.toString(temp.getTimeConsumingFromDateCreatedInSeconds()));

                    String activityId    = workflowActivity.getActivityDefId();
                    int    activityCount = mapActivity.get(activityId) == null ? 1 : mapActivity.get(activityId) + 1;

                    mapActivity.put(activityId, activityCount);

                    total++;
                }
            }

            if (firstKey.isEmpty()) {
                firstKey = each.getInstanceId();
            }

        }

        for (String key : mapActivity.keySet()) {
            Map<String, Object> temp = new LinkedHashMap<>();
            temp.put("key", key);
            temp.put("value", (double) (mapActivity.get(key) * 100) / total);

            listActivity.put(new JSONObject(temp));
        }

        Map<String, Object> dataModel = new LinkedHashMap<>();
        WorkflowProcess     wfProcess = workflowManager.getRunningProcessById(firstKey);
        String xpdl = new String(workflowManager.getPackageContent(wfProcess.getPackageId(), wfProcess.getVersion()))
         .replaceAll("&", "&amp;")
         .replaceAll("<", "&lt;")
         .replaceAll(">", "&gt;")
         .replaceAll("\"", "&quot;")
         .replaceAll("\'", "&apos;");

        dataModel.put("wfProcess", wfProcess);
        dataModel.put("appID", appID);
        dataModel.put("listActivity", listActivity);
        dataModel.put("xpdl", xpdl);
        dataModel.put("className", getClassName());

        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/heatmap.ftl", null);
    }

    @Override
    public boolean isHomePageSupported() {
        return true;
    }

    @Override
    public String getDecoratedMenu() {
        return null;
    }

    public Boolean isActivity(Collection<WorkflowActivity> workflowDefList, String activityDefId) {
        for (WorkflowActivity wfActivity : workflowDefList) {
            if (wfActivity.getActivityDefId().equals(activityDefId)) {
                return wfActivity.getType().equals(WorkflowActivity.TYPE_NORMAL);
            }
        }
        return false;
    }
}
