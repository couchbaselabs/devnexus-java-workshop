import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Http } from '@angular/http';
import { Location } from '@angular/common';
import "rxjs/Rx";

@Component({
    selector: 'app-movies',
    templateUrl: './movies.component.html',
    styleUrls: ['./movies.component.css']
})
export class MoviesComponent implements OnInit {

    public movies: any;

    public constructor(private router: Router, private http: Http, private location: Location) {
        this.movies = [];
    }

    public ngOnInit() {
        // Step #2 - Obtaining Data when the Page Loads
        // Hint
        // 'Refresh' data after navigation and on load
        /* CUSTOM CODE HERE */
    }

    public refresh(query?: any) {
        // Step #1 - Requesting Data from a RESTful API
        // Hint
        // HTTP with RxJS, similar to RxJava
        /* CUSTOM CODE HERE */
    }

    public create() {
        this.router.navigate(["create"]);
    }

}
