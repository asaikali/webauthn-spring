import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { OidcSecurityService } from 'angular-auth-oidc-client';

import { QuotesComponent } from './quotes.component';

describe('QuotesComponent', () => {
  let component: QuotesComponent;
  let fixture: ComponentFixture<QuotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ QuotesComponent ],
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {}
        },
        {
          provide: OidcSecurityService,
          useValue: {
            isAuthenticated: () => of(false),
            getAccessToken: () => of('')
          }
        }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(QuotesComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
