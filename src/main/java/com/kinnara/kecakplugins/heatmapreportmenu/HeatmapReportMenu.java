package com.kinnara.kecakplugins.heatmapreportmenu;

import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HeatmapReportMenu extends UserviewMenu implements PluginWebSupport {

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
        return AppUtil.readPluginResource(getClassName(), "/properties/heatmap.json", new String[]{getClassName()}, false);
    }

    @Override
    public String getName() {
        return "Heatmap Report";
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
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

        PluginManager   pluginManager   = (PluginManager) appContext.getBean("pluginManager");
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        PackageDefinition packageDefinition = appDefinition.getPackageDefinition();
        Long packageVersion = packageDefinition != null ? packageDefinition.getVersion() : new Long(1);
        Collection<WorkflowProcess> processList = workflowManager.getProcessList(appDefinition.getAppId(), packageVersion.toString());
//        WorkflowProcess workflowProcess = processList.stream().peek(p -> LogUtil.info(getClassName(), "processList ["+p.getId()+"] ["+p.getIdWithoutVersion()+"]")).filter(p -> getPropertyString("process").equals(p.getIdWithoutVersion())).findFirst().orElse(null);
        WorkflowProcess workflowProcess = workflowManager.getRunningProcessById("6745_pttimah_eapproval_sij");

        Map<String, Map<String, String>> eliminator = new TreeMap<>();
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
        dataModel.put("dataProvider", HeatmapReportWebApi.class.getName());
        dataModel.put("processList", json);
        dataModel.put("reportType", getPropertyString("reportType"));

        if(workflowProcess != null) {
            dataModel.put("processId", workflowProcess.getIdWithoutVersion());
            byte[] xpdlBytes = workflowManager.getPackageContent(workflowProcess.getPackageId(), workflowProcess.getVersion());
            if(xpdlBytes != null) {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    ByteArrayInputStream input = new ByteArrayInputStream(xpdlBytes);
                    Document doc = builder.parse(input);

                    LogUtil.info(getClassName(), "root tag ["+doc.getDocumentElement().getTagName()+"] encoding ["+doc.getXmlEncoding()+"]");

                    JSONObject j = new JSONObject();
                    j.put("jsonXpdl", new String(xpdlBytes, StandardCharsets.UTF_8));
                    dataModel.put("jsonXpdl", j);
                    dataModel.put("xpdl", new String(xpdlBytes, StandardCharsets.UTF_8).replaceAll("\"", "\\\\\""));
                } catch (Exception e) { LogUtil.error(getClassName(), e, e.getMessage());}
            }
        }

        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/HeatmapReportMenu.ftl", null);
//        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/heatmap.ftl", null);
    }

    public String getStringFromDocument(Document doc) {
        try
        {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch(TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
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
        boolean isAdmin = WorkflowUtil.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN);
        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String action = request.getParameter("action");

        if ("getProcesses".equals(action)) {
            String appId = request.getParameter("appId");
            String appVersion = request.getParameter("appVersion");
            try {
                JSONArray jsonArray = new JSONArray();

                ApplicationContext ac = AppUtil.getApplicationContext();
                AppService appService = (AppService) ac.getBean("appService");
                WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");
                AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
                PackageDefinition packageDefinition = appDef.getPackageDefinition();
                Long packageVersion = (packageDefinition != null) ? packageDefinition.getVersion() : new Long(1);
                Collection<WorkflowProcess> processList = workflowManager.getProcessList(appId, packageVersion.toString());

                Map<String, String> empty = new HashMap<String, String>();
                empty.put("value", "");
                empty.put("label", "");
                jsonArray.put(empty);

                for (WorkflowProcess p : processList) {
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("value", p.getIdWithoutVersion());
                    option.put("label", p.getName() + " (" + p.getIdWithoutVersion() + ")");
                    jsonArray.put(option);
                }

                jsonArray.write(response.getWriter());
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Get Run Process's options Error!");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}
