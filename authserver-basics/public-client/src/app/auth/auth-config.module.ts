import { NgModule } from '@angular/core';
import { AuthModule } from 'angular-auth-oidc-client';


@NgModule({
    imports: [AuthModule.forRoot({
        config: {
              authority: 'http://localhost:9090',
              redirectUrl: window.location.origin,
              postLogoutRedirectUri: window.location.origin,
              clientId: 'public-client',
              scope: 'openid quotes.read', // 'openid profile offline_access ' + your scopes
              responseType: 'code',
              useRefreshToken: false, // not currently supported by spring auth server
              silentRenew: true,
              silentRenewUrl: window.location.origin + '/silent-renew.html',
              renewTimeBeforeTokenExpiresInSeconds: 10,
          }
      })],
    exports: [AuthModule],
})
export class AuthConfigModule {}

/*



import { NgModule } from '@angular/core';
import { AuthModule } from 'angular-auth-oidc-client';


@NgModule({
  imports: [AuthModule.forRoot({
    config: {

      autoUserInfo: false
    }
  })],
  exports: [AuthModule],
})
export class AuthConfigModule {
}

 */
