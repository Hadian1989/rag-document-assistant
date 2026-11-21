import {Component, effect, signal} from '@angular/core';
import {Chat} from './chat/chat';
import {Upload} from './upload/upload';
import {DocumentList} from './document-list/document-list';
import { ButtonDirective} from 'primeng/button';
import {Toast} from 'primeng/toast';

@Component({
  imports: [Chat, Upload, DocumentList, ButtonDirective, Toast],
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
