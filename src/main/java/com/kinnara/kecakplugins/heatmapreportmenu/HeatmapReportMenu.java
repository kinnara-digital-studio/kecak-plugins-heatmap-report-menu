package com.kinnara.kecakplugins.heatmapreportmenu;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        ApplicationContext appContext = AppUtil.getApplicationContext();
        String             appID      = AppUtil.getCurrentAppDefinition().getAppId();

        PluginManager   pluginManager   = (PluginManager) appContext.getBean("pluginManager");
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");

        List<WorkflowProcess> processList = new ArrayList<>(workflowManager.getRunningProcessList(appID, "", "", "", "", false, 0, 0));
        processList.addAll(workflowManager.getCompletedProcessList(appID, "", "", "", "", false, 0, 0));

        List<String>         listProcess = new ArrayList<>();
        Map<String, Integer> mapProcess  = new TreeMap<>();

        for (WorkflowProcess each : processList) mapProcess.put(each.getIdWithoutVersion(), 0);

        for (String each : mapProcess.keySet()) listProcess.add(each);

        Map<String, Object> dataModel = new LinkedHashMap<>();
        dataModel.put("appID", appID);
        dataModel.put("appVersion", AppUtil.getCurrentAppDefinition().getVersion());
        dataModel.put("className", getClassName());
        dataModel.put("dataProvider", HeatmapReportWebApi.class.getName());
        dataModel.put("processList", listProcess);

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
}
