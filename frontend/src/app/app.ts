import {Component, effect, signal} from '@angular/core';
import {Chat} from './chat/chat';
import {Upload} from './upload/upload';
import {DocumentList} from './document-list/document-list';
import {Button} from 'primeng/button';

@Component({
  imports: [Chat, Upload, DocumentList, Button],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('frontend');
  dark = signal(
    localStorage.getItem('theme') === 'dark' ||
    (!localStorage.getItem('theme') && matchMedia('(prefers-color-scheme: dark)').matches)
  );

  constructor() {
    effect(() => {
      document.documentElement.classList.toggle('app-dark', this.dark());
      localStorage.setItem('theme', this.dark() ? 'dark' : 'light');
    });
  }
}
