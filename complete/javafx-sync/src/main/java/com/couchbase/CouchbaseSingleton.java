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
            this.manager = new Manager(new JavaContext("data"), Manager.DEFAULT_OPTIONS);
            this.database = this.manager.getDatabase("devnexus");
            View movieView = database.getView("movies");
            movieView.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    emitter.emit(document.get("_id"), document);
                }
            }, "1");
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
        this.pushReplication = this.database.createPushReplication(gateway);
        this.pullReplication = this.database.createPullReplication(gateway);
        this.pushReplication.setContinuous(continuous);
        this.pullReplication.setContinuous(continuous);
        this.pushReplication.start();
        this.pullReplication.start();
    }

    public void stopReplication() {
        this.pushReplication.stop();
        this.pullReplication.stop();
    }

    public Movie save(Movie movie) {
        Map<String, Object> properties = new HashMap<String, Object>();
        Document document = this.database.createDocument();
        properties.put("type", "movie");
        properties.put("title", movie.getTitle());
        properties.put("genre", movie.getGenre());
        Map<String, Boolean> formats = new HashMap<String, Boolean>();
        formats.put("digital", movie.getDigitalCopy());
        formats.put("bluray", movie.getBluray());
        formats.put("dvd", movie.getDvd());
        properties.put("formats", formats);
        try {
            movie.setDocumentId(document.putProperties(properties).getDocument().getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movie;
    }

    public ArrayList<Movie> query() {
        ArrayList<Movie> results = new ArrayList<Movie>();
        try {
            View movieView = this.database.getView("movies");
            Query query = movieView.createQuery();
            QueryEnumerator result = query.run();
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
