import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {OidcSecurityService} from "angular-auth-oidc-client";

@Component({
  selector: 'app-quotes',
  templateUrl: './quotes.component.html',
  styleUrls: ['./quotes.component.scss']
})
export class QuotesComponent implements OnInit {
  quote : string = 'loading ...'

  constructor(private route: ActivatedRoute,
              private http:HttpClient,
              private oidcSecurityService: OidcSecurityService) { }

  ngOnInit(): void {
    setInterval(() => {

      if(this.oidcSecurityService.isAuthenticated()) {
        let h = 'Bearer ' + this.oidcSecurityService.getAccessToken()
        this.http.get<Quote>('http://127.0.0.1:8081/random-quote', {
          headers: {
            Authorization: h
          }
          ,
          observe: 'body',
          responseType: 'json'
        }).subscribe(data => {
          console.log(data)

          this.quote = data?.quote + ' -- ' + data?.author;
          return data;
        })

        console.log(this.quote);
      } else {
        this.quote = "You need to login"
      }
    }, 4_000)
  }

}

export interface Quote {
  id: number;
  quote: string;
  author: any;
}
