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

### Introducing Client to Server Sync with JavaFX and Couchbase Mobile

### Mobilizing the Spring Boot RESTful API with Sync Gateway REST Endpoints

## Resources

Couchbase - [https://www.couchbase.com](https://www.couchbase.com)

Couchbase Developer Portal - [https://developer.couchbase.com](https://developer.couchbase.com)

RxJava - [https://github.com/ReactiveX/RxJava](https://github.com/ReactiveX/RxJava)