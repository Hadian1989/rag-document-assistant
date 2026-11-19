import {Component, signal} from '@angular/core';
import {Chat} from './chat/chat';
import {Upload} from './upload/upload';
import {DocumentList} from './document-list/document-list';

@Component({
  imports: [Chat, Upload, DocumentList],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('frontend');
}
