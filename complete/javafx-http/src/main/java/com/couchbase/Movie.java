package com.couchbase;

import java.util.*;
import com.couchbase.client.java.document.json.*;

public class Movie {

    private String documentId;
    private String title;
    private String genre;
    private boolean digital;
    private boolean bluray;
    private boolean dvd;

    public Movie(String documentId, String title, String genre, boolean digital, boolean bluray, boolean dvd) {
        this.documentId = documentId;
        this.title = title;
        this.genre = genre;
        this.digital = digital;
        this.bluray = bluray;
        this.dvd = dvd;
    }

    public Movie(String title, String genre, boolean digital, boolean bluray, boolean dvd) {
        this.documentId = UUID.randomUUID().toString();
        this.title = title;
        this.genre = genre;
        this.digital = digital;
        this.bluray = bluray;
        this.dvd = dvd;
    }

    public Movie(JsonObject movie) throws Exception {
        this.documentId = movie.getString("id");
        this.title = movie.getString("title");
        this.genre = movie.getString("genre");
        if(movie.getObject("formats") != null) {
            this.digital = (movie.getObject("formats")).getBoolean("digital");
            this.bluray = (movie.getObject("formats")).getBoolean("bluray");
            this.dvd = (movie.getObject("formats")).getBoolean("dvd");
        }
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentId() {
        return this.documentId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getGenre() {
        return this.genre;
    }

    public boolean getDigitalCopy() {
        return this.digital;
    }

    public boolean getBluray() {
        return this.bluray;
    }

    public boolean getDvd() {
        return this.dvd;
    }

    public JsonObject getJsonObject() {
        JsonObject movie = JsonObject.create().put("title", this.title).put("genre", this.genre);
        JsonObject formats = JsonObject.create().put("digital", this.digital).put("bluray", this.bluray).put("dvd", this.dvd);
        movie.put("formats", formats);
        return movie;
    }

}
