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

### Bringing the Client Front-End to Desktop with JavaFX

### Introducing Client to Server Sync with JavaFX and Couchbase Mobile

### Mobilizing the Spring Boot RESTful API with Sync Gateway REST Endpoints

## Resources

Couchbase - [https://www.couchbase.com](https://www.couchbase.com)

Couchbase Developer Portal - [https://developer.couchbase.com](https://developer.couchbase.com)

RxJava - [https://github.com/ReactiveX/RxJava](https://github.com/ReactiveX/RxJava)