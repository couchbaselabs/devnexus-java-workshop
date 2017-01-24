package com.couchbase;

import java.util.*;
import com.couchbase.lite.*;

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

    public Movie(Document document) {
        this.documentId = document.getId();
        this.title = (String) document.getProperty("title");
        this.genre = (String) document.getProperty("genre");
        this.digital = ((Map<String, Boolean>) document.getProperty("formats")).get("digital");
        this.bluray = ((Map<String, Boolean>) document.getProperty("formats")).get("bluray");
        this.dvd = ((Map<String, Boolean>) document.getProperty("formats")).get("dvd");
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

}
