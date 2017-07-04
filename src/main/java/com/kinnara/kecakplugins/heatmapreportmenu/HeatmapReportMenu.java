package com.kinnara.kecakplugins.heatmapreportmenu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.ModelMap;

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
		// TODO Auto-generated method stub
		return null;
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
		return null;
	}
	
	@Override
	public String getJspPage() {
		String processId = (String)getRequestParameter("processId");
		
		ApplicationContext appContext = AppUtil.getApplicationContext();
		WorkflowManager workflowManager = (WorkflowManager)appContext.getBean("workflowManager");
		
        // get process info
        WorkflowProcess wfProcess = workflowManager.getRunningProcessById(processId);

        // get process xpdl
        byte[] xpdlBytes = workflowManager.getPackageContent(wfProcess.getPackageId(), wfProcess.getVersion());
        if (xpdlBytes != null) {
            String xpdl = null;

            try {
                xpdl = new String(xpdlBytes, "UTF-8");
            } catch (Exception e) {
                LogUtil.debug(getClassName(), "XPDL cannot load");
            }
            // get running activities
            Collection<String> runningActivityIdList = new ArrayList<String>();
            List<WorkflowActivity> activityList = (List<WorkflowActivity>) workflowManager.getActivityList(processId, 0, -1, "id", false);
            for (WorkflowActivity wa : activityList) {
                if (wa.getState().indexOf("open") >= 0) {
                    runningActivityIdList.add(wa.getActivityDefId());
                }
            }
            String[] runningActivityIds = (String[]) runningActivityIdList.toArray(new String[0]);
            
            ModelMap map = new ModelMap();
            map.addAttribute("wfProcess", wfProcess);
            map.addAttribute("xpdl", xpdl);
        }

        return "pbuilder/pviewer.jsp";
	}

	@Override
	public boolean isHomePageSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDecoratedMenu() {
		// TODO Auto-generated method stub
		return null;
	}
}
