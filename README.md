# DevNexus 2017 Java Workshop

This repository is a full stack development workshop for Java developers.  It was intended to demonstrate the development of Java applications for various platforms and share data with a NoSQL database, Couchbase.

The Java development stack that this workshop is based around, includes:

* Couchbase NoSQL, including Couchbase Mobile
* Java with Spring Boot
* JavaFX
* Angular

Painting a picture, Java with Spring Boot will act as a RESTful API that communicates with a client as well as Couchbase.  Angular can act as the client that communicates with the RESTful API and so can the JavaFX desktop application.  However, the RESTful approach isn't the only approach, which is where Couchbase Mobile comes in.

## Requirements

There are many pieces to this project, each with their own set of dependencies.  A full set of dependencies include the following:

* Docker
* JDK 1.8
* Maven
* Gradle
* Node.js 6.0+

## The Workshop Breakdown

The workshop is broken down into two core sections, each with their own sub-sections.  The two sections consist of an **initial** project and a **complete** project.  The instructions in this README will walk you through the development of the **initial** project which will become **complete** as a final result.  If at any point in time you'd like to validate your work, find the corresponding sub-section in the **complete** directory.

## Instructions

The **initial** project is split into six parts, where each part is essentially a new part of the stack.

### Deploying Couchbase with Docker

### Building a RESTful API with Java, Spring Boot, and Couchbase Server

The goal of this section is to create a RESTful API that can be consumed from any front-end client.  This API is powered by Java as the logic layer and supplied with data from Couchbase Server.

Open the project, **initial/spring-boot-sdk**, as all development will be done here.

#### Step 1 - Connect to the Couchbase Cluster

If you recall, each Couchbase node in a cluster is aware of all other nodes in the cluster.  This means that we only need to pick a single node that we wish to connect to.

Check the project's **src/main/resources/application.properties** file.  You'll notice that host and Bucket information are already supplied to you.  You just need to figure out how to use that information.

Typically one would connect to a cluster like this, using the Java SDK for Couchbase:

```
Cluster cluster = CouchbaseCluster.create("HOST_HERE");
```

More information on connecting to a Couchbase cluster can be found in the [Managing Connections](https://developer.couchbase.com/documentation/server/current/sdk/java/managing-connections.html) documentation.

Now open the project's **src/main/java/couchbase/Application.java** file and look for the first step.  See if you can supply the code necessary to establish a connection to the cluster.

#### Step 2 - Open a Couchbase Bucket

With a connect to the cluster established, you have your pick at whatever Buckets are available.  Before data can be queried or created, the desired Bucket must be opened using its name and password.

We already know the Bucket information is in our **src/main/resources/application.properties** file, we just need to apply it.  Typically a Bucket would be opened like the following:

```
Bucket bucket = cluster.openBucket("BUCKET_HERE", "PASSWORD_HERE");
```

More information on opening a Couchbase Bucket can be found in the [Managing Connections](https://developer.couchbase.com/documentation/server/current/sdk/java/managing-connections.html) documentation.

There is no password on our Bucket so it can be left blank, but how about the remaining?  Open the project's **src/main/java/couchbase/Application.java** file and look for the second step.  See if you can supply the code necessary to open our defined Bucket.

#### Step 3 - Insert a New Couchbase Document

The Spring Boot application is now connected to Couchbase and a particular Bucket has been opened.  As a first step (third step), it makes sense to get data created.

Some will argue that POST requests create data and others will argue that PUT requests create data.  For us, it doesn't really matter how we get to the data creation point, we only care about creating such data.

There are several ways to create data in Couchbase using the Java SDK.  You could use N1QL or you could do a basic key-value CRUD operation.

To create data using a CRUD operation, one might do something like this:

```
JsonObject content = JsonObject.empty().put("name", "Nic Raboy");
JsonDocument document = JsonDocument.create("DOCID_HERE", content);
JsonDocument inserted = bucket.insert(document);
```

A `JsonDocument` can be created out of a `JsonObject` and a key.  However, if you try to insert documents that share the same key, you're going to get an error.  If the scenario calls for it, you might find value in using a UUID as the key.

Creating a UUID is as simple as the following:

```
String uuid = UUID.randomUUID().toString();
```

More information on Bucket CRUD operations can be found in the [Creating Documents](https://developer.couchbase.com/documentation/server/4.1/sdks/java-2.2/documents-creating.html) documentation.

The alternative to CRUD, is to use a N1QL query.  Here is an example of a N1QL query for inserting documents:

```
String queryStr = "INSERT INTO `BUCKET_NAME_HERE` (KEY, VALUE) VALUES " +
                "($1, {'name': $2})";
JsonArray parameters = JsonArray.create()
        .add("DOCID_HERE")
        .add("Nic Raboy");
ParameterizedN1qlQuery query = ParameterizedN1qlQuery.parameterized(queryStr, parameters);
N1qlQueryResult queryResult = bucket.query(query);
```

The above example query includes parameterization of the user inputted values.  This will prevent SQL injection attacks that plague relational databases.

More information on the N1QL `INSERT` statement can be found in the [INSERT](https://developer.couchbase.com/documentation/server/current/n1ql/n1ql-language-reference/insert.html) documentation.

In the **src/main/java/couchbase/Application.java** file, try to find the third step and insert the supplied `JsonObject` into Couchbase.  Feel free to add extra data validation.

#### Step 4 - Query for All Documents in the Couchbase Bucket

With data going into the database, we need it to come out of the database as well.  There are several ways to do, but since we want multiple documents to be returned, it is best to use a query rather than a CRUD operation.

You got a taste of N1QL in the third step, but it's time to put it to use in this step.

When it comes to Java, there is the synchronous approach and the asynchronous approach.  There is no better approach of the two, but it is more based on your application needs.  A synchronous query example might look like the following:

```
String query = "SELECT * FROM `BUCKET_NAME_HERE`";
N1qlQueryResult result = bucket.query(N1qlQuery.simple(query));
```

The results would then be parsed into a format that better meets the needs of the Java application.  Parsing a `N1qlQueryResult` might look like the following:

```
List<Map<String, Object>> content = new ArrayList<Map<String, Object>>();
for (N1qlQueryRow row : result) {
    content.add(row.value().toMap());
}
```

Now what if you wanted to query the database asynchronously?  This can be accomplished using [RxJava](https://github.com/ReactiveX/RxJava) and Reactive programming.

Let's take a look at that same example and make it asynchronous:

```
String query = "SELECT * FROM `BUCKET_NAME_HERE`";
<List<Map<String, Object>> bucket.async().query(N1qlQuery.simple(query))
    .flatMap(AsyncN1qlQueryResult::rows)
    .map(result -> result.value().toMap())
    .toList()
    .toBlocking()
    .single();
```

The code above starts with a standard N1QL query, but it gets executed as an asynchronous query that returns an observable.  This observable is a stream of data that can be manipulated as it goes down the stream.

For example, it starts as an `AsyncN1qlQueryResult`, is flattened, and then each row is converted into a `Map`.  These `Map` items are combined into a `List` and eventually returned.

Give it a try in the application.  Find the fourth step in the **src/main/java/couchbase/Application.java** file and try to query for all documents in the Bucket.  You may need to define your query consistency so you may want to read about [Scan Consistency](http://docs.couchbase.com/sdk-api/couchbase-java-client-2.2.0/com/couchbase/client/java/query/consistency/ScanConsistency.html) in the documentation.

#### Step 5 - Query for Documents with Parameterization and N1QL

In the previous step you may or may not have included query parameters in your N1QL query.  This time around, they are a necessity.

Regardless on if you plan to use RxJava or not, the setup will be the same.  Parameterization comes in two flavors, placeholder and list.  Using placeholders can offer you more control.  An example is seen below:

```
String statement = "SELECT * FROM `BUCKET_NAME_HERE` WHERE firstname = $name";
JsonObject parameters = JsonObject.create().put("name", title.toLowerCase());
ParameterizedN1qlQuery query = ParameterizedN1qlQuery.parameterized(statement, parameters);
```

How you choose to execute the above query is up to you.  In the case of placeholder parameters, they act as named parameters defined in a `JsonObject` object.

In the project's **src/main/java/couchbase/Application.java** file, try to query for documents by the `title` property.  In the fifth step we want to implement a endpoint for searching for documents.  Remember to allow querying for data that is similar, but maybe not the same.

#### Step 6 - Running the Application with Gradle

This application uses Gradle for building and running.  To run this application, execute the following from the Command Prompt (Windows) or Terminal (Mac and Linux):

```
./gradlew run
```

Provided there are no errors, the three endpoint RESTful API will be serving at http://localhost:8080.  Test out the API endpoints using a browser or a tool like [Postman](https://www.getpostman.com/).

### Developing a Client Front-End with Angular

Using a RESTful API through tools like Postman isn't necessarily an attractive solution.  Instead it makes sense to build a client application that can push and consume data.  This is where Angular comes in.  It allows us to build a client-facing website that communicates with our Java backend.

The Angular client front-end will be developed within the **initial/angular** project.

#### Step 1 - Requesting Data from a RESTful API

When it comes to getting data from a RESTful API, an application needs to make an HTTP request to an available endpoint.  In Angular, there is what is called the `Http` service.  This service allows for pretty much any kind of HTTP request.

A typical request might look like the following:

```
var myObservable = http.get("http://example.com/endpoint");
```

You'll notice that we're working with observables, just like with RxJava.  This is because Angular uses the JavaScript version called RxJS.

This means that we have control of our data stream.  For example, the following transformation is valid:

```
http.get("http://example.com/endpoint");
    .map(result => result.json())
    .subscribe(result => {
        console.log(result);
    });
```

Open the project's **src/app/movies/movies.component.ts** file and look out for the first step.  The goal here is to make a request against the `/movies` endpoint of the Java application.  The result of the request should be stored in the public `movies` variable.

#### Step 2 - Obtaining Data when the Page Loads

When the application loads, it is a good idea to populate the screen with data from our backend.  This is typically done in the Angular `ngOnInit` method rather than the `constructor` which triggers first.

Two things need to be considered when initializing a page in Angular:

1. Was the page navigated to?
2. Was the page returned to?

In Angular, it is possible to navigate to a page, making it the top item in the navigation stack.  It is also possible to pop from the navigation stack, which is the same thing as navigating back to a page.  These are two different navigation scenarios.

When a page is navigated directly to, the `constructor` and `ngOnInit` methods are triggered.  If the page is navigated back to, neither of those methods are triggered.

Instead, we have to subscribe to changes in the navigation stack, otherwise we won't know if we've returned.  This is possible through:

```
location.subscribe(() => {
    console.log("I am back!");
});
```

So why is this important?  If we've navigated back, we should probably refresh the data on the screen because it could be stale.

Open the project's **src/app/movies/movies.component.ts** and look for the second step.  Update it so the data on the screen is refreshed for both types of navigation.

#### Step 3 - Rendering Remote Data to the Screen

After making requests to the Java RESTful API, the `movies` variable should now contain data to be rendered to the screen.  This requires some changes to the HTML.

To display data from an array, one would typically do the following:

```
<ul>
    <li *ngFor="let item of items">
        {{ item }}
    </li>
</ul>
```

In the above example, `items` would be an array of strings.  Each iteration of the loop will allow us to print out a particular item called `item`.

Open the project's **src/app/movies/movies.component.html** and find the third step.  Take note that we're not working with an unordered list and our array is not of strings.

#### Step 4 - Sending User Input to the RESTful API

When making a request to create data, you typically would not use a GET request, but instead a PUT or a POST request.  In Angular, there are minimal differences when constructing either of these request types.  However, the server is expecting JSON, so the sender must define the request as JSON.

In Angular, and other technologies, this is done through the request header:

```
let headers = new Headers({ "Content-Type": "application/json" });
let options = new RequestOptions({ "headers": headers });
```

Without a header definition, any data sent will be considered plain text by default on the receiving end.  Not too big a deal if the server anticipates it, but in our situation, we are expecting JSON.

Actually performing a POST HTTP request would look something like the following:

```
http.post("http://example.com/endpoint", "JSON_HERE", options)
    .subscribe(result => {
        console.log(result);
    });
```

In the project's **src/app/create/create.component.ts** file, search for the fourth step.  The goal here is to make a POST request against the server and navigate backwards to the previous page, only if the request was successful.

#### Step 5 - Binding Form Elements to the TypeScript Logic

Often data will need to be user generated before making a request to the backend of the stack.  User generated data is made possible through forms and TypeScript bound variables.

In Angular, there are several ways to bind the HTML UI to the front-end logic.  Take for example Angular Template syntax:

```
<input #firstname type="text" placeholder="First Name" />
<button (click)="save(firstname.value)">Save</Button>
```

In the above scenario, the input field is referenced by `firstname` at which point we are passing the value of that reference to a function.  This is not a two-way data bind.

To two-way data bind, and an alternative to the Template syntax, the following is an acceptable solution:

```
<input [(ngModel)]="firstname" type="text" placeholder="First Name" />
<button (click)="save()">Save</button>
```

By using the `[(ngModel)]` tag, the `firstname` reference is bound to a public variable in the TypeScript code.  Updating the variable on one side will reflect on the other, hence there is no need to pass it via the button function.

Open the project's **src/app/create/create.component.html** file and find the fifth step.  Try to add a form that will be used to submit data to the backend for creation.

#### Step 6 - Running the Application with the Angular CLI

In this workshop, the Angular application will not be distributed as part of the Java RESTful API.  Instead it will be served along-side the backend on a different port.

Using the Node Package Manager (NPM) which comes with Node.js, execute the following to obtain all the project dependencies:

```
npm install
```

When the Angular dependencies are installed, the project can be run by executing the following:

```
ng serve
```

The backend should be available at http://localhost:8080 while the Angular application should be available at http://localhost:4200.  Make sure all the features work.

### Bringing the Client Front-End to Desktop with JavaFX

As of right there are three parts to this full stack application.  There is the NoSQL database, the Spring Boot powered RESTful API, and there is the Angular front-end.  However, as of right now, this stack is limited to being used from the web.  It doesn't have to be though.

What would it take to bring this application to desktop clients?  This is where JavaFX comes into play.  JavaFx can act as a desktop client that communicates to the Spring Boot backend, replacing or complimenting the Angular layer.

Development for this section will happen in the **initial/javafx-http** project.

#### Step 1 - Creating the JavaFX UI

The UI in a JavaFX application is powered by XML, similar to HTML.  This XML can be crafted by hand or with a design application called [Scene Builder](http://docs.oracle.com/javase/8/scene-builder-2/get-started-tutorial/overview.htm#JSBGS164).

Within a JavaFX scene, the window pane can be sized and UI components such as text fields and buttons can be added and positioned.

Take the following window pane:

```
<Pane maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="380.0" prefWidth="600.0" xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.couchbase.JavaFXController">
    <children>
        <TextField fx:id="name" layoutX="14.0" layoutY="287.0" prefHeight="25.0" prefWidth="270.0" promptText="Name" />
        <TextField fx:id="address" layoutX="14.0" layoutY="340.0" prefHeight="27.0" prefWidth="270.0" promptText="Address" />
        <Button fx:id="send" layoutX="454.0" layoutY="340.0" mnemonicParsing="false" text="Send" />
    </children>
</Pane>
```

The above window pane is 600x380 in size with three components positioned absolutely in the space.  Each component has an `fx:id` property that will be bound in the controller specified in the `fx:controller` attribute.

Open the project's **src/main/resources/MovieFX.fxml** file and search for the first step.  There are eight components that we want to add where each component relates to the data we wish to send to the server and to the data we wish to receive from the server.  This means we'll want list views, text fields, checkboxes, and buttons.  Look in the project's **src/main/java/com/couchbase/MovieFXController** for the `fx:id` names.

#### Step 2 - Request Data from the Backend with a GET Request

Just like with the Angular application, data needs to be consumed from the Spring Boot Java application.  This means that an HTTP request must be made against the server and any data returned added to the list component.

In Java there are a few ways to make GET requests against a web service.  The most common ways are to make use of the `HttpClient`, `HttpGet`, and `HttpResponse` classes.  An example of such a request might look like the following:

```
private String makeGetRequest(String url) {
    String result = "";
    try {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
            result += line;
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return result;
}
```

In the above function, a request is made against the provided URL.  The response is then gathered and returned as a string back to the calling method.

Open the project's **src/main/java/com/couchbase/MovieFXController.java** file and find the second step.  Here we'll want to make a GET request, parse the results, and add them to the list component.  Remember, our results are an array coming back in string format.  For help, take a look at the project's **src/main/java/com/couchbase/Movie.java** file to see what kind of data is expected to enter the list component.

#### Step 3 - Sending Data to the Backend Java API

When it comes to adding data to the database, a request must be made against the Spring Boot application.  Just like with the Angular application, this is done through a POST request.

A POST request in Java is similar to that of the GET request.  An example might look like the following:

```
private String makePostRequest(String url, String body) {
    String result = "";
    try {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(body, ContentType.create("application/json")));
        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
            result += line;
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return result;
}
```

In the above example, a request body is expected.  This request body is what we wish to save and it comes from the FXML form elements.

Open the project's **src/main/java/com/couchbase/MovieFXController.java** file and find the third step.  Your goal is to take the form data and send it via a POST request to the Spring Boot backend.  Take note that the backend expects the POST body to be in JSON format.

#### Step 4 - Running the Client-Facing Application with Maven

To be successful with the JavaFX desktop application, Couchbase and the Spring Boot API must be functional and running.  The backend should be running at http://localhost:8080.

At this point the JavaFX application can be built and run.  From the command line, execute the following with Maven:

```
mvn jfx:run
```

This will launch a desktop application that will communicate with the Spring Boot backend.

### Introducing Client to Server Sync with JavaFX and Couchbase Mobile

Being able to consume data on demand is great, but with many different clients and platforms it can become difficult to know when data has changed without refreshing one of the queries.  Instead of making requests, it might make sense to make use of data synchronization and change listeners.

Synchronization with Couchbase eliminates a lot of the RESTful API need when performing client to server communications.

This section will use the **initial/javafx-sync** project.

#### Step 1 - Creating an Opening a Local Database

Things are a little different when working with data synchronization.  Instead of working strictly with remote data, there is now a local copy.  This local copy of the data is replicated back and forth with Couchbase Sync Gateway rather than a RESTful API such as the one created with Spring Boot.

That said, the application needs to be able to open the local database for any data operations.  Using what is called a Couchbase `Manager`, any particular Couchbase Lite database on the device can be opened:

```
Manager manager = new Manager(new JavaContext("data"), Manager.DEFAULT_OPTIONS);
Database database = manager.getDatabase("DATABASE_NAME_HERE");
```

If the database had not been created, the `getDatabase` method will create it before opening.

With an open database, basic key-value operations can happen.  For example, data can be inserted, updated, retrieved, or deleted based on the key of the documents.  This is great, but it doesn't make use of the features found in a document database.

MapReduce views should be created when the database is opened so data can be queried.  An example of such view can be seen below:

```
View personView = database.getView("people");
personView.setMap(new Mapper() {
    @Override
    public void map(Map<String, Object> document, Emitter emitter) {
        String type = (String) document.get("type");
        if(type.equals("person")) {
            emitter.emit(document.get("_id"), document);
        }
    }
}, "1");
```

The above code creates a view called `people`.  When queried, as long as the documents contain a `type` property that equals `person`, a result will be emitted.  Once a view is created, they will not be created again until the version number of the view changes.

More on Couchbase Lite Views can be found in the [documentation](https://developer.couchbase.com/documentation/mobile/current/guides/couchbase-lite/native-api/view/index.html).

Open the project's **src/main/java/com/couchbase/CouchbaseSingleton.java** file and find the first step.  Your goal is to open a local database and create a few for querying the movie data that is saved.

#### Step 2 - Saving Locally to Couchbase Lite

Saving data to Couchbase Lite in Java is a little different than saving it directly to Couchbase Server.  For example, the following is an object that can be saved:

```
Map<String, Object> properties = new HashMap<String, Object>();
properties.put("type", "person");
properties.put("firstname", "Nic");
properties.put("lastname", "Raboy");
```

The value of map key can be any object, for example it can even be another `Map<String, Object>`.  Design the documents based on your needs.

Once the properties are created, a document can be created and saved:

```
Document document = database.createDocument();
document.putProperties(properties);
```

A key is automatically defined and the document remains local to the computer that is running the application.  Synchronization will happen soon.

Open the project's **src/main/java/com/couchbase/CouchbaseSingleton.java** and look for the second step.  You need to take a `Movie` object and save it to the database.  Remember how we expect the data to look server side.  The document should have a nested `formats` object.  Once saved, don't forget to save the document id into the `Movie` object before returning it.

#### Step 3 - Querying for Locally Stored Documents

Remember that view that was created when Couchbase Lite was opened?  It is time to use it to query for documents that satisfy that particular criteria.

To query a view you'd do something like the following:

```
View personView = database.getView("people");
Query query = personView.createQuery();
QueryEnumerator result = query.run();
```

The `QueryEnumerator` result can be queried over, where each iteration, a document gets added to the list that will be bound to the JavaFX list component.

```
Document document = null;
for (Iterator<QueryRow> it = result; it.hasNext(); ) {
    QueryRow row = it.next();
    document = row.getDocument();
    System.out.println((String) document.getProperty("firstname"));
}
```

Remember, the document saved was in `Map<String, Object>` format, so don't forget to cast each property to the format that you wish to use.

Open the project's **src/main/java/com/couchbase/CouchbaseSingleton.java** file and find the third step.  The goal here is to query the view that was created and return an `ArrayList` of movies to be displayed in the UI.

#### Step 4 - Synchronize the Local Data with Couchbase Server

Everything up until now dealt with local data.  While certain scenarios will only call for a local database, ours depends on a remote database as well.

Sync Gateway should be running as part of the Docker configuration, so replication needs to be configured to replicate in both directions from the JavaFX application.

A typical replication will look like the following:

```
Replication push = database.createPushReplication("http://localhost:4984/bucket_name_here");
push.start();
```

In the above example, data in the local database will be pushed to the Sync Gateway instance.  This is done one time, but there are options for doing this continuously as data changes.

More information on synchronization can be found in the [Replication](https://developer.couchbase.com/documentation/mobile/current/guides/couchbase-lite/native-api/replication/index.html) documentation.

Open the project's **src/main/java/com/couchbase/CouchbaseSingleton.java** file and look for the fourth step.  Our goal is to configure replication to happen continuously and in both directions.  This means data needs to be pushed to Sync Gateway and pulled from Sync Gateway.

#### Step 5 - Listening for Changes Rather Than Querying for Changes

Now that data is synchronizing continuously in all directions, it is no longer efficient to keep querying to see if it has changed.  Instead, it makes sense to set up listeners to listen for when data has changed.

There are many different types of change listeners available when it comes to Couchbase Lite, but the one that matters to us is the listener on the database that tells us if anything has changed.  It might look something like the following:

```
database.addChangeListener(new ChangeListener() {
    @Override
    public void changed(Database.ChangeEvent event) {
        for(int i = 0; i < event.getChanges().size(); i++) {
            Document document = database.getDocument(event.getChanges().get(i).getDocumentId());
        }
    }
});
```

In the above example, when something changes in the database, each change is reviewed.  Various information such as the document id, or if the document was deleted will come in with those changes.  With the document id, each full document can be retrieved.

Open the project's **src/main/java/com/couchbase/MovieFXController.java** file and look for the fifth step.  The goal here is to setup a change listener, find out what changed, and update the list component of the JavaFX application.  If the document was deleted then it should be removed from the list, and likewise with updates and creates.

Something to note about JavaFX and change listeners.  They happen on separate threads, so any updates to the UI need to be wrapped in the following:

```
Platform.runLater(new Runnable() {
    @Override
    public void run() {

    }
});
```

Adding any UI logic in the `runLater` block will prevent headache.

#### Step 6 - Running the Application with Maven

The JavaFX application with synchronization support should be ready to run.  The Spring Boot RESTful API is not in the equation for this particular application.  It will communicate directly with Sync Gateway which will communicate with Couchbase Server.

Execute the following to run the JavaFX application:

```
mv jfx:run
```

If configured correctly, any data saved will be visible in the Sync Gateway administrative dashboard as well as Couchbase Server.

### Mobilizing the Spring Boot RESTful API with Sync Gateway REST Endpoints

The JavaFX application is now synchronizing data rather than requesting it.  This is made possible because of Couchbase Sync Gateway.  However, Sync Gateway does its own configuration against the database and it currently differs to that of the Spring Boot example.

To keep compatibility, some adjustments need to be made so the Angular application continues to work and the JavaFX application continues to sync.

This section will focus on the **initial/spring-boot-sync** project.

#### Step 1 - Using the Sync Gateway RESTful API

To create documents managed by Sync Gateway, the Sync Gateway RESTful API must be used.  Information on the Sync Gateway RESTful API can be found in the [documentation](https://developer.couchbase.com/documentation/mobile/1.3/references/sync-gateway/rest-api/index.html#/).

Essentially, the following Sync Gateway endpoint must be hit in order to save data:

```
POST http://localhost:4984/devnexus/
BODY application/json
```

Think back to how POST requests were made via a Java client.  The same concept stands true for the Spring Boot server side application.

Open the project's **src/main/java/couchbase/Application.java** file and find the first step.  The goal is to accept JSON data from a client request and POST it to the Sync Gateway RESTful API.

#### Step 2 - Building the Spring Boot Application with Gradle

Like with the pure Java SDK version of the Spring Boot project, it can be built and run using the following Gradle command:

```
./gradlew run
```

With the new version of the RESTful API running, try launching the Angular application again.  It should work exactly the same, but this time changes are being synchronized to the JavaFX version.

## Resources

Couchbase - [https://www.couchbase.com](https://www.couchbase.com)

Couchbase Developer Portal - [https://developer.couchbase.com](https://developer.couchbase.com)

RxJava - [https://github.com/ReactiveX/RxJava](https://github.com/ReactiveX/RxJava)