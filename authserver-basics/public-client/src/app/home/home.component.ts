import { Component, OnInit } from '@angular/core';
import { OidcSecurityService, OpenIdConfiguration, UserDataResult } from 'angular-auth-oidc-client';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-home',
  templateUrl: 'home.component.html',
})
export class HomeComponent implements OnInit {
  configuration: OpenIdConfiguration | undefined;
  //userDataChanged$: Observable<OidcClientNotification<any>>;
  userData$: Observable<UserDataResult> | undefined;
  isAuthenticated = false;
  constructor(public oidcSecurityService: OidcSecurityService) {}

  ngOnInit() {
    this.configuration = this.oidcSecurityService.getConfiguration();
    this.userData$ = this.oidcSecurityService.userData$;

    this.oidcSecurityService.isAuthenticated$.subscribe(({ isAuthenticated }) => {
      this.isAuthenticated = isAuthenticated;

      console.warn('authenticated: ', isAuthenticated);
    });
  }

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
