import {Component, OnInit} from '@angular/core';
import {LoginResponse, OidcSecurityService} from 'angular-auth-oidc-client';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: false
})
export class AppComponent implements OnInit {
  title = 'spa-pkce';
  constructor(public oidcSecurityService: OidcSecurityService) {}

  ngOnInit() {
    this.oidcSecurityService.checkAuth().subscribe(( loginResponse: LoginResponse) => {
      console.log('app authenticated', loginResponse.isAuthenticated);
      console.log(`Current access token is '${loginResponse.accessToken}'`);
      console.log(`Current id token is '${loginResponse.idToken}'`);
    });
  }
}
