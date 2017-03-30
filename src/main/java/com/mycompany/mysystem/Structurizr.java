package com.mycompany.mysystem;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.documentation.Format;
import com.structurizr.documentation.StructurizrDocumentation;
import com.structurizr.model.*;
import com.structurizr.view.*;

/**
 * This is a simple example of how to get started with Structurizr for Java.
 */
public class Structurizr {

    public static void main(String[] args) throws Exception {
        // a Structurizr workspace is the wrapper for a software architecture model, views and documentation
        Workspace workspace = create();
        uploadWorkspaceToStructurizr(workspace);
    }

    private static final String DATABASE_TAG = "Database";

    public static Workspace create() {
        Workspace workspace = new Workspace("Smart Mobility", "Real-time ridesharing service.");
        Model model = workspace.getModel();

        Person driver = model.addPerson("Driver user", "A user (student/teacher) that wants to share his trip.");
        Person passenger = model.addPerson("Passenger user", "A user (student/teacher) that wants to travel.");
        Person admin = model.addPerson("Administrator user", "A system administrator user.");

        SoftwareSystem softwareSystem = model.addSoftwareSystem("Software System", "A real-time ride sharing service");
        driver.uses(softwareSystem, "Registers trips");
        passenger.uses(softwareSystem, "Finds travel plans and registers trip");
        admin.uses(softwareSystem, "Monitors the system.");

        SoftwareSystem transport = model.addSoftwareSystem("Public transport API", "Returns buss and train information.");
        softwareSystem.uses(transport, "Fetches public transportation from");

        SoftwareSystem sas = model.addSoftwareSystem("SAS Rooster", "Contains the calendar of a student/teacher");
        softwareSystem.uses(sas, "Fetches the appointments/classes using a webcal feed from");

        Container mobileApplication = softwareSystem.addContainer("Smart Mobility mobile APP", "The Smart Mobility mobile APP allows users to interface the Smart Mobility API on their mobile phones.", "Android React app");
        Container backendForFrontend = softwareSystem.addContainer("Smart Mobility API", "The Smart Mobility mobile APP allows users to register trips, plan/register for travel plans and monitor the total system.", "Spring MVC on Apache Tomcat");
        Container database = softwareSystem.addContainer("Database", "Stores interesting data.", "MySQL");
        database.addTags(DATABASE_TAG);

        driver.uses(mobileApplication, "Registers trips");
        passenger.uses(mobileApplication, "Finds travel plans and registers trip");
        admin.uses(mobileApplication, "Monitors the system.");

        mobileApplication.uses(backendForFrontend, "Uses", "HTTPS");
        backendForFrontend.uses(database, "Reads from and writes to", "JDBC");

        Component someController = backendForFrontend.addComponent("Some Controller", "A description of some controller.", "Spring MVC RestController");
        Component someService = backendForFrontend.addComponent("Some Service", "A description of some service.", "Spring Bean");
        Component someRepository = backendForFrontend.addComponent("Some Repository", "A description of some repository.", "Spring Data");

        mobileApplication.uses(someController, "Uses", "JSON/HTTPS");
        someController.uses(someService, "Uses");
        someService.uses(someRepository, "Uses");
        someRepository.uses(database, "Reads to and writes from", "JDBC");

        // create some views
        ViewSet viewSet = workspace.getViews();
        SystemContextView contextView = viewSet.createSystemContextView(softwareSystem, "Context", "System context diagram for the Smart Mobility project.");
        contextView.addAllSoftwareSystems();
        contextView.addAllPeople();
        contextView.setPaperSize(PaperSize.A5_Landscape);

        ContainerView containerView = viewSet.createContainerView(softwareSystem, "Containers", "Container diagram for the Smart Mobility project.");
        containerView.addAllPeople();
        containerView.addAllContainers();
        containerView.setPaperSize(PaperSize.A5_Landscape);

        ComponentView componentView = viewSet.createComponentView(backendForFrontend, "Components", "Components diagram for the Smart Mobility project.");
        componentView.addAllContainers();
        componentView.addAllComponents();
        componentView.setPaperSize(PaperSize.A5_Landscape);

        // tag and style some elements
        Styles styles = viewSet.getConfiguration().getStyles();
        styles.addElementStyle(Tags.ELEMENT).color("#ffffff").width(650).height(400).fontSize(36);
        styles.addElementStyle(Tags.SOFTWARE_SYSTEM).background("#1168bd");
        styles.addElementStyle(Tags.CONTAINER).background("#438dd5");
        styles.addElementStyle(Tags.COMPONENT).background("#85bbf0").color("#000000");
        styles.addRelationshipStyle(Tags.RELATIONSHIP).thickness(5).routing(Routing.Direct).fontSize(32).width(400);

        styles.addElementStyle(Tags.PERSON).background("#08427b").width(550).shape(Shape.Person);
        styles.addElementStyle(DATABASE_TAG).shape(Shape.Cylinder);

        // add some documentation
        StructurizrDocumentation documentation = new StructurizrDocumentation(model);
        workspace.setDocumentation(documentation);
        documentation.addContextSection(softwareSystem, Format.Markdown,
                "Here is some context about the software system...\n" +
                        "\n" +
                        "![](embed:Context)");
        documentation.addContainersSection(softwareSystem, Format.Markdown,
                "Here is some information about the containers...\n" +
                        "\n" +
                        "![](embed:Containers)");
        documentation.addComponentsSection(backendForFrontend, Format.Markdown,
                "Here is some information about the Backend for Frontend...\n" +
                        "\n" +
                        "![](embed:Components)");
        documentation.addCodeSection(someController, Format.Markdown,
                "Here is some information about the SomeController component...");

        return workspace;
    }

    private static void uploadWorkspaceToStructurizr(Workspace workspace) throws Exception {
        StructurizrClient structurizrClient = new StructurizrClient(Private.API_KEY, Private.API_SECRET);
        structurizrClient.putWorkspace(Private.WORKSPACE_ID, workspace);
    }

}