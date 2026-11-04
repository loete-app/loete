/**
 * Root-Komponente der Loete-Anwendung.
 *
 * Enthält die Navigationsleiste und den Router-Outlet
 * für die seitenbezogenen Komponenten.
 */
import { Component } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { Navbar } from "./shared/components/navbar/navbar";

@Component({
  selector: "app-root",
  imports: [RouterOutlet, Navbar],
  template: `
    <app-navbar />
    <main class="main-content">
      <router-outlet />
    </main>
  `,
  styleUrl: "./app.scss",
})
export class App {}
