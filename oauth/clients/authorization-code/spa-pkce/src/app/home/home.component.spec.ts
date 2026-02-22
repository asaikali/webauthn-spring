import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { OidcSecurityService } from 'angular-auth-oidc-client';

import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HomeComponent ],
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: OidcSecurityService,
          useValue: {
            getConfiguration: () => of(null),
            userData$: of({}),
            isAuthenticated$: of({ isAuthenticated: false }),
            getAccessToken: () => of(''),
            authorize: () => undefined,
            logoff: () => undefined,
            logoffAndRevokeTokens: () => of({}),
            revokeAccessToken: () => of({})
          }
        }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
