import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {Chat} from './chat/chat';
import {Upload} from './upload/upload';
import {DocumentList} from './document-list/document-list';

@Component({
  imports: [RouterOutlet, Chat, Upload, DocumentList],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('frontend');
}
