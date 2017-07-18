package com.kinnara.kecakplugins.heatmapreportmenu;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class HeatmapReportMenu extends UserviewMenu implements PluginWebSupport {

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

        PluginManager               pluginManager   = (PluginManager) appContext.getBean("pluginManager");
        WorkflowManager             workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        Collection<WorkflowProcess> processList     = workflowManager.getRunningProcessList(appID, "", "", "", "", false, 0, 0);
        processList.addAll(workflowManager.getCompletedProcessList(appID, "", "", "", "", false, 0, 0));

        Map<String, Object> dataModel = new LinkedHashMap<>();
        dataModel.put("appID", appID);
        dataModel.put("className", getClassName());
        dataModel.put("dataProvider", ReportHelper.class.getName());

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
            if (wfActivity.getActivityDefId().equals(activityDefId))
                return wfActivity.getType().equals(WorkflowActivity.TYPE_NORMAL);
        }
        return false;
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {


    }
}
