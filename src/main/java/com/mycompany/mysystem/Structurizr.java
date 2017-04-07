package com.mycompany.mysystem;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.documentation.StructurizrDocumentation;
import com.structurizr.model.*;
import com.structurizr.view.*;

public class Structurizr {

    public static void main(String[] args) throws Exception {
        // a Structurizr workspace is the wrapper for a software architecture model, views and documentation
        Workspace workspace = create();
        uploadWorkspaceToStructurizr(workspace);
    }

    private static final String DATABASE_TAG = "Database";
    private static final String MICROSERVICE_TAG = "Microservice";
    private static final String MICROSERVICE_API_TECHNOLOGY = "Java with Spring + Swagger";//Java and Spring Boot
    private static final String TECH_API = "JSON/HTTPS";
    private static final String TECH_DATABASE = "MongoDB";
    private static final String TECH_NON_PERIS_DATABASE = "Redis";
    //https://github.com/spring-projects/spring-data-mongodb
    private static final String TECH_MONGO_DATABASE_CONNECTION = "Spring-data-mongodb";
    private static final String TECH_REDIS_DATABASE_CONNECTION = "Jedis";
    private static final String TECH_APP_COMPONENTS = "React component";
    private static final String TECH_APP_STORE = "React store";

    private static Workspace create() {
        Workspace workspace = new Workspace("Smart Mobility", "Real-time ridesharing service.");
        Model model = workspace.getModel();

        //Actors
        Person driver = model.addPerson("Driver user", "A student or teacher");
        Person passenger = model.addPerson("Passenger user", "A student or teacher");
        Person admin = model.addPerson("Administrator user", "A system administrator user.");

        //Build general software system
        SoftwareSystem softwareSystem = model.addSoftwareSystem("Software System", "A real-time ride sharing service");
        softwareSystem.setLocation(Location.Internal);
        driver.uses(softwareSystem, "Registers trips using");
        passenger.uses(softwareSystem, "Finds travel plans and registers trip using");

        //Add external software systems this software system uses.
        SoftwareSystem publicTransportApi = model.addSoftwareSystem("Google Transit APIs", "Returns buss and train information.");
        publicTransportApi.setUrl("https://developers.google.com/transit/");
        publicTransportApi.setLocation(Location.External);
        softwareSystem.uses(publicTransportApi, "Fetches public transportation from");

        SoftwareSystem sas = model.addSoftwareSystem("SAS schedule", "Contains the calendar of a student/teacher");
        sas.setLocation(Location.External);
        sas.setUrl("http://sas.han.nl");
        softwareSystem.uses(sas, "Fetches the appointments/classes using a webcal feed from");

        SoftwareSystem datadog = model.addSoftwareSystem("Datadog", "Contains metrics and logging");
        datadog.setUrl("https://www.datadoghq.com");
        datadog.setLocation(Location.External);
        softwareSystem.uses(datadog, "Sends metrics and logs to");
        admin.uses(datadog, "Monitors the system through");

        //Build mobile app.
        Container mobileApplication = softwareSystem.addContainer("Smart Mobility mobile APP", "Mobile APP to interface the API.", "Android React (web/mob)app");
        driver.uses(mobileApplication, "Registers trips");
        passenger.uses(mobileApplication, "Finds travel plans and registers trip");
        mobileApplication.uses(sas, "Fetches the appointments/classes using a webcal feed from");

        //Build gateway
        Container gateway = softwareSystem.addContainer("Smart Mobility gateway", "API gateway aggregating results invoking multiple microservices", MICROSERVICE_API_TECHNOLOGY);
        gateway.addTags(MICROSERVICE_TAG);
        mobileApplication.uses(gateway, "Uses", TECH_API);

        //Auth service
        Container authService = softwareSystem.addContainer("Auth Service", "The point of auth info.", MICROSERVICE_API_TECHNOLOGY);
        authService.addTags(MICROSERVICE_TAG);
        gateway.uses(authService, "Validates auth tokens", TECH_API, InteractionStyle.Synchronous);

        //Auth database
        Container authDatabase = softwareSystem.addContainer("Auth Database", "Stores authentication tokens.", TECH_NON_PERIS_DATABASE);
        authDatabase.addTags(DATABASE_TAG);
        authService.setUrl("https://redis.io");
        //https://github.com/xetorthio/jedis
        authService.uses(authDatabase, "Reads to and writes from", TECH_REDIS_DATABASE_CONNECTION, InteractionStyle.Synchronous);

        //Trip service
        Container tripService = softwareSystem.addContainer("Trip service", "The point of access for trip and travel plans information.", MICROSERVICE_API_TECHNOLOGY);
        tripService.addTags(MICROSERVICE_TAG);
        gateway.uses(tripService, "Fetches and creates trips using", TECH_API, InteractionStyle.Synchronous);

        //Registered trips database
        Container tripDatabase = softwareSystem.addContainer("Trip Database", "Stores registered trips.", TECH_DATABASE);
        tripDatabase.addTags(DATABASE_TAG);
        tripService.uses(tripDatabase, "Reads to and writes from", TECH_MONGO_DATABASE_CONNECTION, InteractionStyle.Synchronous);

        //User service
        Container userService = softwareSystem.addContainer("User Service", "The point of access for user information.", MICROSERVICE_API_TECHNOLOGY);
        userService.addTags(MICROSERVICE_TAG);
        userService.uses(authService, "Fetches new auth token from");
        gateway.uses(userService, "Fetches user information using", TECH_API, InteractionStyle.Synchronous);

        //User database
        Container userDatabase = softwareSystem.addContainer("User Database", "Stores user information.", TECH_DATABASE);
        userDatabase.addTags(DATABASE_TAG);
        userService.uses(userDatabase, "Reads to and writes from", TECH_MONGO_DATABASE_CONNECTION, InteractionStyle.Synchronous);

        //Travel service
        Container travelService = softwareSystem.addContainer("Travel Service", "The point of access for travel plan information.", MICROSERVICE_API_TECHNOLOGY);
        travelService.addTags(MICROSERVICE_TAG);
        travelService.uses(tripService, "fetches registered trips from");
        travelService.uses(publicTransportApi, "Fetches public transportation from", TECH_API);
        gateway.uses(travelService, "Fetches travel plans and registers for them from", TECH_API, InteractionStyle.Synchronous);

        //Describe components in mobile app.
        Component plannerComponent = mobileApplication.addComponent("Planner Component", "First view and controller. To input shared variables for search and register.", TECH_APP_COMPONENTS);
        Component searchComponent = mobileApplication.addComponent("Search Component", "List view and controller. To show the available travel plans.", TECH_APP_COMPONENTS);
        Component registerTripComponent = mobileApplication.addComponent("Register trip Component", "Register view and controller. To complete trip details and register it.", TECH_APP_COMPONENTS);
        Component travelPlanComponent = mobileApplication.addComponent("Travel plan Component", "A view and controller. A detailed view of what trips the travel plan includes.", TECH_APP_COMPONENTS);
        Component myTravelPlansComponent = mobileApplication.addComponent("My Travel plans Component", "List view and controller. To show the travel plans registered for.", TECH_APP_COMPONENTS);
        Component SettingsComponent = mobileApplication.addComponent("Settings Component", "A view and controller. To set calendar link", TECH_APP_COMPONENTS);
        Component loginComponent = mobileApplication.addComponent("User login Component", "A view and controller. To login.", TECH_APP_COMPONENTS);
        Component userRegistrationComponent = mobileApplication.addComponent("User register Component", "A view and controller. To register an account.", TECH_APP_COMPONENTS);

        Component userStore = mobileApplication.addComponent("User store", "Register/Login and get user details", TECH_APP_STORE);
        Component tripStore = mobileApplication.addComponent("Trip store", "Register trip and get trip details", TECH_APP_STORE);
        Component travelPlansStore = mobileApplication.addComponent("Travel plan store", "Find travel plans get travel plan details", TECH_APP_STORE);
        searchComponent.uses(travelPlansStore, "Finds travel plans using");
        travelPlanComponent.uses(travelPlansStore, "Get trips of travel plan using");
        plannerComponent.uses(sas, "Fetches latest appointment/class using webcal from");
        userRegistrationComponent.uses(userStore, "Creates new user using");
        loginComponent.uses(userStore, "Login user using");
        myTravelPlansComponent.uses(travelPlansStore, "Finds travel plans of user (passenger) from");
        myTravelPlansComponent.uses(tripStore, "Finds trips of user (driver) from");
        SettingsComponent.uses(sas, "Validates calendar using webcal from");
        registerTripComponent.uses(tripStore, "Registers trip to");


        //Describe components in auth service
        Component authServiceComponent = authService.addComponent("Auth component", "A crud service for the sessions", MICROSERVICE_API_TECHNOLOGY);
        authServiceComponent.uses(authDatabase, "Reads to and writes from");

        //Describe components in user service.
        Component userServiceComponent = userService.addComponent("User component", "A crud service, allowing changes to a user", MICROSERVICE_API_TECHNOLOGY);
        Component userRepositoryComponent = userService.addComponent("User repository", "Repository class for User domain objects All method names are compliant with Spring Data naming.", "Spring Repository");
        mobileApplication.uses(userServiceComponent, "Uses", TECH_API);
        userServiceComponent.uses(userRepositoryComponent, "Uses");
        userRepositoryComponent.uses(userDatabase, "Reads to and writes from", TECH_MONGO_DATABASE_CONNECTION, InteractionStyle.Synchronous);

        //Describe components in the trip service
        Component tripServiceComponent = tripService.addComponent("Trip component", "A crud service, allowing changes to a trip", MICROSERVICE_API_TECHNOLOGY);
        Component tripRepositoryComponent = tripService.addComponent("Trip repository", "Repository class for trip domain objects All method names are compliant with Spring Data naming.", "Spring Repository");
        tripServiceComponent.uses(tripRepositoryComponent, "Uses");
        tripRepositoryComponent.uses(tripDatabase, "Reads to and writes from", TECH_MONGO_DATABASE_CONNECTION, InteractionStyle.Synchronous);

        //Describe components in the travel service
        Component travelServiceComponent = travelService.addComponent("Travel component", "A crud service, allowing to search for trips. Presented as travelplans", MICROSERVICE_API_TECHNOLOGY);
        Component travelTripRepositoryComponent = travelService.addComponent("Trips repository", "Repository class for fetching registered trips.", "Spring Repository");
        Component travelPublicTransportRepositoryComponent = travelService.addComponent("Public transport repository", "Repository class for fetching public transport trips.", "Spring Repository");
        travelServiceComponent.uses(travelTripRepositoryComponent, "Uses");
        travelServiceComponent.uses(travelPublicTransportRepositoryComponent, "Uses");
        travelTripRepositoryComponent.uses(tripServiceComponent, "From", TECH_API, InteractionStyle.Synchronous);
        travelPublicTransportRepositoryComponent.uses(publicTransportApi, "From", TECH_API, InteractionStyle.Synchronous);

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

        //Component view for mobile app
        ComponentView componentViewFrontMobileApp = viewSet.createComponentView(mobileApplication, "Components mobile application", "Components diagram for the frontend of the Smart Mobility project.");
        componentViewFrontMobileApp.addAllComponents();
        componentViewFrontMobileApp.add(sas);
        componentViewFrontMobileApp.setPaperSize(PaperSize.A5_Landscape);

        //Component view for auth service.
        ComponentView authServiceComponentView = viewSet.createComponentView(authService, "Components auth service", "Components diagram for the auth service.");
        authServiceComponentView.addAllComponents();
        authServiceComponentView.add(authDatabase);
        authServiceComponentView.setPaperSize(PaperSize.A5_Landscape);

        //Component view for trip service.
        ComponentView tripServiceComponentView = viewSet.createComponentView(tripService, "Components trip service", "Components diagram for the trip service.");
        tripServiceComponentView.addAllComponents();
        tripServiceComponentView.add(tripDatabase);
        tripServiceComponentView.setPaperSize(PaperSize.A5_Landscape);

        //Component view for travel service.
        ComponentView travelServiceComponentView = viewSet.createComponentView(travelService, "Components travel service", "Components diagram for the travel service.");
        travelServiceComponentView.addAllComponents();
        travelServiceComponentView.add(publicTransportApi);
        travelServiceComponentView.setPaperSize(PaperSize.A5_Landscape);

        //Component view for user service.
        ComponentView userServiceComponentView = viewSet.createComponentView(userService, "Components user service", "Components diagram for the user service.");
        userServiceComponentView.addAllComponents();
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

        return workspace;
    }

    private static void uploadWorkspaceToStructurizr(Workspace workspace) throws Exception {
        StructurizrClient structurizrClient = new StructurizrClient(Private.API_KEY, Private.API_SECRET);
        structurizrClient.putWorkspace(Private.WORKSPACE_ID, workspace);
    }

}