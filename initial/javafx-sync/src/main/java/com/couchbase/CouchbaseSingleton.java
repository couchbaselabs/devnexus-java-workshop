package com.couchbase;

import com.couchbase.lite.*;
import com.couchbase.lite.replicator.Replication;

import java.net.URL;
import java.util.*;

public class CouchbaseSingleton {

    private Manager manager;
    private Database database;
    private Replication pushReplication;
    private Replication pullReplication;

    private static CouchbaseSingleton instance = null;

    private CouchbaseSingleton() {
        try {
            // Step #1 - Creating an Opening a Local Database
            // Hint
            // Let the manager create the database, and create a view to your data
            /* CUSTOM CODE HERE */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CouchbaseSingleton getInstance() {
        if(instance == null) {
            instance = new CouchbaseSingleton();
        }
        return instance;
    }

    public Database getDatabase() {
        return this.database;
    }

    public void startReplication(URL gateway, boolean continuous) {
        // Step #4 - Synchronize the Local Data with Couchbase Server
        // Hint
        // Replications need to happen in both directions with Sync Gateway
        /* CUSTOM CODE HERE */
    }

    public void stopReplication() {
        this.pushReplication.stop();
        this.pullReplication.stop();
    }

    public Movie save(Movie movie) {
        // Step #2 - Saving Locally to Couchbase Lite
        // Hint
        // Create a Map of the Movie properties and save it
        /* CUSTOM CODE HERE */
        return movie;
    }

    public ArrayList<Movie> query() {
        ArrayList<Movie> results = new ArrayList<Movie>();
        try {
            // Step #3 - Querying for Locally Stored Documents
            // Hint
            // Use the MapReduce View for querying
            /* CUSTOM CODE HERE */
            Document document = null;
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                document = row.getDocument();
                results.add(new Movie(document.getId(), (String) document.getProperty("title"), (String) document.getProperty("genre"), ((Map<String, Boolean>) document.getProperty("formats")).get("digital"), ((Map<String, Boolean>) document.getProperty("formats")).get("bluray"), ((Map<String, Boolean>) document.getProperty("formats")).get("dvd")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

}
