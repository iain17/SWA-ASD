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
        driver.uses(softwareSystem, "Registers trips using");
        passenger.uses(softwareSystem, "Finds travel plans and registers trip using");

        //Add external software systems this software system uses.
        SoftwareSystem publicTransportApi = model.addSoftwareSystem("Public transport API", "Returns buss and train information.");
        softwareSystem.uses(publicTransportApi, "Fetches public transportation from");

        SoftwareSystem sas = model.addSoftwareSystem("SAS schedule", "Contains the calendar of a student/teacher");
        softwareSystem.uses(sas, "Fetches the appointments/classes using a webcal feed from");

        SoftwareSystem datadog = model.addSoftwareSystem("Datadog", "Contains metrics and logging");
        softwareSystem.uses(datadog, "Sends metrics and logs to");
        admin.uses(datadog, "Monitors the system through");

        //Build mobile app.
        Container mobileApplication = softwareSystem.addContainer("Smart Mobility mobile APP", "Mobile APP to interface the API.", "Android React (web/mob)app");
        driver.uses(mobileApplication, "Registers trips");
        passenger.uses(mobileApplication, "Finds travel plans and registers trip");
        mobileApplication.uses(sas, "Fetches the appointments/classes using a webcal feed from");

        //Build gateway
        Container gateway = softwareSystem.addContainer("Smart Mobility gateway", "API gateway aggregating results invoking multiple microservices", "Spring MVC on Apache Tomcat");
        gateway.addTags(MICROSERVICE_TAG);

        mobileApplication.uses(gateway, "Uses", "HTTPS");

        //Auth service
        Container authService = softwareSystem.addContainer("Auth Service", "The point of auth info.", "Java and Spring Boot");
        authService.addTags(MICROSERVICE_TAG);
        gateway.uses(authService, "Validates auth tokens", "JSON/HTTPS", InteractionStyle.Synchronous);

        //Trip service
        Container tripService = softwareSystem.addContainer("Trip service", "The point of access for trip and travel plans information.", "Java and Spring Boot");
        tripService.addTags(MICROSERVICE_TAG);
        gateway.uses(tripService, "Fetches and creates trips using", "JSON/HTTPS", InteractionStyle.Synchronous);

        //Registered trips database
        Container customerDatabase = softwareSystem.addContainer("Trip Database", "Stores registered trips.", "MySQL");
        customerDatabase.addTags(DATABASE_TAG);
        tripService.uses(customerDatabase, "Reads to and writes from", "JDBC", InteractionStyle.Synchronous);

        //User service
        Container userService = softwareSystem.addContainer("User Service", "The point of access for user information.", "Java and Spring Boot");
        userService.addTags(MICROSERVICE_TAG);
        userService.uses(authService, "Fetches new auth token from");
        gateway.uses(userService, "Fetches user information using", "JSON/HTTPS", InteractionStyle.Synchronous);

        //User database
        Container userDatabase = softwareSystem.addContainer("User Database", "Stores user information.", "MySQL");
        userDatabase.addTags(DATABASE_TAG);
        userService.uses(userDatabase, "Reads to and writes from", "JDBC", InteractionStyle.Synchronous);

        //Travel service
        Container travelService = softwareSystem.addContainer("Travel Service", "The point of access for travel plan information.", "Java and Spring Boot");
        travelService.addTags(MICROSERVICE_TAG);
        travelService.uses(tripService, "fetches registered trips from");
        travelService.uses(publicTransportApi, "Fetches public transportation from", "JSON/HTTPS");
        gateway.uses(travelService, "Fetches travel plans and registers for them from", "JSON/HTTPS", InteractionStyle.Synchronous);

        //Describe components in mobile app.
        Component homeController = mobileApplication.addComponent("Some Controller", "A description of some controller.", "iOS UIViewController");

        //Describe components in user service.
        Component userServiceComponent = userService.addComponent("User component", "A crud service, allowing changes to a user", "Spring Service");
        Component userRepositoryComponent = userService.addComponent("User repository", "Repository class for User domain objects All method names are compliant with Spring Data naming.", "Spring Repository");
        mobileApplication.uses(userServiceComponent, "Uses", "JSON/HTTPS");
        userServiceComponent.uses(userRepositoryComponent, "Uses");
        userRepositoryComponent.uses(userDatabase, "Reads to and writes from", "JDBC", InteractionStyle.Synchronous);

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
        containerView.addAllElements();
//        Relationship relationship = new Relationship(containerView, datadog, "Sends metrics/logs to", "HTTP/JSON", InteractionStyle.Asynchronous);
//        RelationshipView relationshipView = new RelationshipView(relationship);
        containerView.setPaperSize(PaperSize.Slide_16_9);


        ComponentView componentViewFrontMobileApp = viewSet.createComponentView(mobileApplication, "Components mobile application", "Components diagram for the frontend of the Smart Mobility project.");
        componentViewFrontMobileApp.addAllComponents();
        componentViewFrontMobileApp.setPaperSize(PaperSize.A5_Landscape);

        //Containers for user service.
        ComponentView userServiceComponentView = viewSet.createComponentView(userService, "Components user service", "Components diagram for the user service.");
        userServiceComponentView.add(userServiceComponent);
        userServiceComponentView.add(userRepositoryComponent);
        userServiceComponentView.add(userDatabase);
        userServiceComponentView.setPaperSize(PaperSize.A5_Landscape);

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