import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {Chat} from './chat/chat';

@Component({
  imports: [RouterOutlet, Chat],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('frontend');
}
