import { Component, OnInit } from '@angular/core';
import { Http, Headers, RequestOptions } from '@angular/http';
import { Location } from '@angular/common';

@Component({
    selector: 'app-create',
    templateUrl: './create.component.html',
    styleUrls: ['./create.component.css']
})
export class CreateComponent implements OnInit {

    public movie: any;

    public constructor(private http: Http, private location: Location) {
        this.movie = {
            "title": "",
            "genre": "",
            "formats": {
                "digital": false,
                "bluray": false,
                "dvd": false
            }
        };
    }

    public ngOnInit() { }

    public save() {
        if(this.movie.title && this.movie.genre) {
            let headers = new Headers({ "Content-Type": "application/json" });
            let options = new RequestOptions({ "headers": headers });
            // Step #4 - Sending User Input to the RESTful API
            // Hint
            // What does the server expect, and in what format?
            /* CUSTOM CODE HERE */
        }
    }

}
