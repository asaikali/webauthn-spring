import { Component, OnInit } from '@angular/core';
import { OidcSecurityService, OpenIdConfiguration, UserDataResult } from 'angular-auth-oidc-client';
import { Observable } from 'rxjs';
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-home',
  templateUrl: 'home.component.html',
})
export class HomeComponent implements OnInit {
  configuration: OpenIdConfiguration | undefined;
  //userDataChanged$: Observable<OidcClientNotification<any>>;
  userData$: Observable<UserDataResult> | undefined;
  isAuthenticated = false;
  constructor(public oidcSecurityService: OidcSecurityService,   private http:HttpClient) {}

  ngOnInit() {
    this.configuration = this.oidcSecurityService.getConfiguration();
    this.userData$ = this.oidcSecurityService.userData$;

    this.oidcSecurityService.isAuthenticated$.subscribe(({ isAuthenticated }) => {
      this.isAuthenticated = isAuthenticated;

      console.warn('authenticated: ', isAuthenticated);
    });
  }

  dynamicClientReg() {
    console.log("will call auth server and try to use the user jwt token to register a confidential client")

    const reg = "http:///127.0.0.1:9090/connect/register"
    const body =  `
     {
      "application_type": "web",
       "redirect_uris":
         ["https://client.example.org/callback",
          "https://client.example.org/callback2"],
       "client_name": "My Example",
       "token_endpoint_auth_method": "client_secret_basic"
      }
    `


    const accessTokenValue = 'Bearer ' + this.oidcSecurityService.getAccessToken()

    this.http.post<string>( reg, body, {
      headers: {
        Authorization: accessTokenValue,
        responseType: 'text'
      }
    }).subscribe( data => {
      console.log("response : " + data)
    });
  }

  /*
  HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setBearerAuth(accessToken.getTokenValue());

		// Register the client
		mvcResult = this.mvc.perform(post(DEFAULT_OIDC_CLIENT_REGISTRATION_ENDPOINT_URI)
				.headers(httpHeaders)
				.contentType(MediaType.APPLICATION_JSON)
				.content(getClientRegistrationRequestContent(clientRegistration)))
				.andExpect(status().isCreated())
				.andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
				.andExpect(header().string(HttpHeaders.PRAGMA, containsString("no-cache")))
				.andReturn();
   */


  login() {
    this.oidcSecurityService.authorize();
  }

  logout() {
    this.oidcSecurityService.logoff();
  }

  logoffAndRevokeTokens() {
    this.oidcSecurityService.logoffAndRevokeTokens().subscribe(
      (result) => {
        console.log(result)
      }
    );
  }


  revokeAccessToken() {
    this.oidcSecurityService.revokeAccessToken().subscribe((result) => {
      console.log(result)
    });
  }
}
