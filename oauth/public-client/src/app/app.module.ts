import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthConfigModule } from './auth/auth-config.module';
import { HomeComponent } from './home/home.component';
import { QuotesComponent } from './quotes/quotes.component';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    QuotesComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    AuthConfigModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
