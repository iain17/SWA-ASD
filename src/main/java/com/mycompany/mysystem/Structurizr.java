package com.mycompany.mysystem;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.documentation.Format;
import com.structurizr.documentation.StructurizrDocumentation;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.model.Tags;
import com.structurizr.view.PaperSize;
import com.structurizr.view.Styles;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.ViewSet;

/**
 * This is a simple example of how to get started with Structurizr for Java.
 */
public class Structurizr {

    private static final String API_KEY = "df6a40e0-59b7-4f0e-908c-2659ff96cd49 ";
    private static final String API_SECRET = "971375a0-6f5b-4e7c-833a-846da43afe34";
    private static final long WORKSPACE_ID = 29991;

    public static void main(String[] args) throws Exception {
        // a Structurizr workspace is the wrapper for a software architecture model, views and documentation
        Workspace workspace = new Workspace("Smart Mobility", "Real-time ridesharing service.");
        Model model = workspace.getModel();
        ViewSet viewSet = workspace.getViews();
        Styles styles = viewSet.getConfiguration().getStyles();

        // add some elements to your software architecture model
        Person driver = model.addPerson("Driver user", "A user (student/teacher) that wants to share his trip.");
        Person passenger = model.addPerson("Passenger user", "A user (student/teacher) that wants to travel.");
        Person admin = model.addPerson("Administrator user", "A system administrator user.");

        SoftwareSystem mobApp = model.addSoftwareSystem("Smart Mobility mobile APP", "The Smart Mobility mobile APP allows users to interface the Smart Mobility API on their mobile phones.");
        driver.uses(mobApp, "Registers trips");
        passenger.uses(mobApp, "Finds travel plans and registers trip");
        admin.uses(mobApp, "Monitors the system.");

        SoftwareSystem api = model.addSoftwareSystem("Smart Mobility API", "The Smart Mobility mobile APP allows users to register trips, plan/register for travel plans and monitor the total system.");
        mobApp.uses(api, "Consumes the API of the smart mobility api");

        //Add external systems
        SoftwareSystem transport = model.addSoftwareSystem("Public transport API", "Returns buss and train information.");
        api.uses(transport, "Fetches public transportation from");

        SoftwareSystem sas = model.addSoftwareSystem("SAS Rooster", "");
        mobApp.uses(sas, "Fetches the appointments/classes using a webcal feed from");

        // define some views (the diagrams you would like to see)
        SystemContextView contextView = viewSet.createSystemContextView(mobApp, "Context", "System context diagram for the Smart Mobility project.");
        contextView.addAllSoftwareSystems();
        contextView.addAllPeople();
        contextView.setPaperSize(PaperSize.A5_Landscape);

        // add some documentation
        StructurizrDocumentation documentation = new StructurizrDocumentation(model);
        workspace.setDocumentation(documentation);
        documentation.addContextSection(mobApp, Format.Markdown,
                "P\n" +
                        "\n" +
                        "![](embed:Context)");

        // optionally, add some styling
        styles.addElementStyle(Tags.SOFTWARE_SYSTEM).background("#1168bd").color("#ffffff");
        styles.addElementStyle(Tags.PERSON).background("#08427b").color("#ffffff");

        uploadWorkspaceToStructurizr(workspace);
    }

    private static void uploadWorkspaceToStructurizr(Workspace workspace) throws Exception {
        StructurizrClient structurizrClient = new StructurizrClient(API_KEY, API_SECRET);
        structurizrClient.putWorkspace(WORKSPACE_ID, workspace);
    }

}