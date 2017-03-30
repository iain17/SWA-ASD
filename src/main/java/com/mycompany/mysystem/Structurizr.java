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
    private static final String MICROSERVICE_TAG = "Microservice";

    private static Workspace create() {
        Workspace workspace = new Workspace("Smart Mobility", "Real-time ridesharing service.");
        Model model = workspace.getModel();

        //Actors
        Person driver = model.addPerson("Driver user", "A student or teacher");
        Person passenger = model.addPerson("Passenger user", "A student or teacher");
        Person admin = model.addPerson("Administrator user", "A system administrator user.");

        //Build general software system
        SoftwareSystem softwareSystem = model.addSoftwareSystem("Software System", "A real-time ride sharing service");
        driver.uses(softwareSystem, "Registers trips");
        passenger.uses(softwareSystem, "Finds travel plans and registers trip");
        admin.uses(softwareSystem, "Monitors the system.");

        //Add external software systems this software system uses.
        SoftwareSystem publicTransportApi = model.addSoftwareSystem("Public transport API", "Returns buss and train information.");
        softwareSystem.uses(publicTransportApi, "Fetches public transportation from");

        SoftwareSystem sas = model.addSoftwareSystem("SAS Rooster", "Contains the calendar of a student/teacher");
        softwareSystem.uses(sas, "Fetches the appointments/classes using a webcal feed from");

        //Build mobile app.
        Container mobileApplication = softwareSystem.addContainer("Smart Mobility mobile APP", "The Smart Mobility mobile APP allows users to interface the Smart Mobility API on their mobile phones.", "Android React app");
        driver.uses(mobileApplication, "Registers trips");
        passenger.uses(mobileApplication, "Finds travel plans and registers trip");
        admin.uses(mobileApplication, "Monitors the system.");
        mobileApplication.uses(sas, "Fetches the appointments/classes using a webcal feed from");

        //Build gateway
        Container gateway = softwareSystem.addContainer("Smart Mobility API gateway", "The Smart Mobility mobile APP allows users to register trips, plan/register for travel plans and monitor the total system.", "Spring MVC on Apache Tomcat");
        gateway.addTags(MICROSERVICE_TAG);
        mobileApplication.uses(gateway, "Uses", "HTTPS");

        //travelPlan service
        Container travelPlanService = softwareSystem.addContainer("Travel plan Service", "The point of access for information.", "Java and Spring Boot");
        travelPlanService.addTags(MICROSERVICE_TAG);
        travelPlanService.uses(publicTransportApi, "Fetches public transportation from", "JSON/HTTPS");
        gateway.uses(travelPlanService, "Fetches travel plans information using", "JSON/HTTPS", InteractionStyle.Synchronous);

        //Registered trips database
        Container customerDatabase = softwareSystem.addContainer("Trip Database", "Stores registered trips.", "MySQL");
        customerDatabase.addTags(DATABASE_TAG);
        travelPlanService.uses(customerDatabase, "Reads to and writes from", "JDBC", InteractionStyle.Synchronous);

        //User service
        Container userService = softwareSystem.addContainer("User Service", "The point of access for user information.", "Java and Spring Boot");
        userService.addTags(MICROSERVICE_TAG);
        gateway.uses(userService, "Fetches user information using", "JSON/HTTPS", InteractionStyle.Synchronous);

        //User database
        Container userDatabase = softwareSystem.addContainer("User Database", "Stores user information.", "MySQL");
        userDatabase.addTags(DATABASE_TAG);
        userService.uses(userDatabase, "Reads to and writes from", "JDBC", InteractionStyle.Synchronous);

        //Describe components in mobile app.
//        Component homeController = mobileApplication.addComponent("Some Controller", "A description of some controller.", "Spring MVC RestController");

        //Describe components in gateway.
//        Component someController = gateway.addComponent("Some Controller", "A description of some controller.", "Spring MVC RestController");
//        Component someService = gateway.addComponent("Some Service", "A description of some service.", "Spring Bean");
//        Component someRepository = gateway.addComponent("Some Repository", "A description of some repository.", "Spring Data");
//        mobileApplication.uses(someController, "Uses", "JSON/HTTPS");
//        someController.uses(someService, "Uses");
//        someService.uses(someRepository, "Uses");

        //
        // Create the views
        //
        ViewSet viewSet = workspace.getViews();

        //Context
        SystemContextView contextView = viewSet.createSystemContextView(softwareSystem, "Context", "System context diagram for the Smart Mobility project.");
        contextView.addAllSoftwareSystems();
        contextView.addAllPeople();
        contextView.setPaperSize(PaperSize.A5_Landscape);

        //Containers
        ContainerView containerView = viewSet.createContainerView(softwareSystem, "Containers", "Container diagram for the Smart Mobility project.");
        containerView.addAllPeople();
        containerView.addAllContainers();
        containerView.setPaperSize(PaperSize.A5_Landscape);


//        ComponentView componentViewFrontend = viewSet.createComponentView(mobileApplication, "Components frontend", "Components diagram for the frontend of the Smart Mobility project.");
//        componentViewFrontend.addAllContainers();
//        componentViewFrontend.addAllComponents();
////        componentViewFrontend.remove(database);
//        componentViewFrontend.setPaperSize(PaperSize.A5_Landscape);

//        ComponentView componentViewBackend = viewSet.createComponentView(gateway, "Components backend", "Components diagram for the backend of the Smart Mobility project.");
//        componentViewBackend.addAllContainers();
//        componentViewBackend.addAllComponents();
//        componentViewBackend.setPaperSize(PaperSize.A5_Landscape);

        // tag and style some elements
        Styles styles = viewSet.getConfiguration().getStyles();
        styles.addElementStyle(Tags.ELEMENT).color("#000000");
        styles.addElementStyle(Tags.PERSON).background("#ffbf00").shape(Shape.Person);
        styles.addElementStyle(Tags.CONTAINER).background("#facc2E");
        styles.addElementStyle(MICROSERVICE_TAG).shape(Shape.Hexagon);
        styles.addElementStyle(DATABASE_TAG).background("#f5da81").shape(Shape.Cylinder);
//        styles.addRelationshipStyle(Tags.RELATIONSHIP).routing(Routing.Orthogonal);

        styles.addRelationshipStyle(Tags.ASYNCHRONOUS).dashed(true);
        styles.addRelationshipStyle(Tags.SYNCHRONOUS).dashed(false);

        // add some documentation
        StructurizrDocumentation documentation = new StructurizrDocumentation(model);
        workspace.setDocumentation(documentation);
//        documentation.addContextSection(softwareSystem, Format.Markdown,
//                "Here is some context about the software system...\n" +
//                        "\n" +
//                        "![](embed:Context)");
//        documentation.addContainersSection(softwareSystem, Format.Markdown,
//                "Here is some information about the containers...\n" +
//                        "\n" +
//                        "![](embed:Containers)");
//        documentation.addComponentsSection(backendForFrontend, Format.Markdown,
//                "Here is some information about the Backend for Frontend...\n" +
//                        "\n" +
//                        "![](embed:Components)");
//        documentation.addCodeSection(someController, Format.Markdown,
//                "Here is some information about the SomeController component...");

        return workspace;
    }

    private static void uploadWorkspaceToStructurizr(Workspace workspace) throws Exception {
        StructurizrClient structurizrClient = new StructurizrClient(Private.API_KEY, Private.API_SECRET);
        structurizrClient.putWorkspace(Private.WORKSPACE_ID, workspace);
    }

}