package com.kinnara.kecakplugins.heatmapreportmenu;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.LogUtil;
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
        ApplicationContext          appContext      = AppUtil.getApplicationContext();
        PluginManager               pluginManager   = (PluginManager) appContext.getBean("pluginManager");
        WorkflowManager             workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        AppDefinition               appDefinition   = AppUtil.getCurrentAppDefinition();
        Collection<WorkflowProcess> processList     = workflowManager.getRunningProcessList(appDefinition.getAppId(), getPropertyString("processId"), "", "", "", false, 0, 0);

        JSONArray listActivity = new JSONArray();
        String    firstKey     = "";
        int       max          = 0;

        Map<String, Integer> mapActivity = new TreeMap<>();
        for (WorkflowProcess each : processList) {

            for (WorkflowActivity workflowActivity : workflowManager.getActivityList(each.getInstanceId(), 0, 0, "", false)) {
                if (isActivity(workflowActivity)) {
                    String name  = workflowActivity.getName().replaceAll("\\s+", "").trim();
                    int    total = mapActivity.get(name) == null ? 1 : mapActivity.get(name) + 1;

                    if (total > max) {
                        max = total;
                    }

                    mapActivity.put(name, total);
                }
            }


            if (firstKey.isEmpty()) {
                firstKey = each.getInstanceId();
            }

        }

        for (String key : mapActivity.keySet()) {
            Map<String, Object> temp = new LinkedHashMap<>();
            temp.put("key", key);
            temp.put("value", mapActivity.get(key));

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
        dataModel.put("appID", appDefinition.getAppId());
        dataModel.put("listActivity", listActivity);
        dataModel.put("xpdl", xpdl);
        dataModel.put("maxActivity", max);
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

    private Boolean isActivity(WorkflowActivity activity) {
//    return !activity.getActivityDefId().startsWith("route");
        LogUtil.info(getClassName(), "activity [" + activity.getActivityDefId() + "] type [" + activity.getType() + "]");
        return activity.getType() != null && activity.getType().equals(WorkflowActivity.TYPE_NORMAL);
    }
}
