import {Component, effect, inject, signal} from '@angular/core';
import {Chat} from './chat/chat';
import {Upload} from './upload/upload';
import {DocumentList} from './document-list/document-list';
import {ButtonDirective} from 'primeng/button';
import {Toast} from 'primeng/toast';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  imports: [Chat, Upload, DocumentList, ButtonDirective, Toast, TranslatePipe],
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

  private translate = inject(TranslateService);

  lang = signal<'de' | 'en'>(
    (localStorage.getItem('lang') as 'de' | 'en' | null) ??
    (navigator.language.startsWith('de') ? 'de' : 'en')
  );

  constructor() {
    effect(() => {
      document.documentElement.classList.toggle('app-dark', this.dark());
      localStorage.setItem('theme', this.dark() ? 'dark' : 'light');
    });
    effect(() => {
      const lang = this.lang();
      this.translate.use(lang);
      localStorage.setItem('lang', lang);
      document.documentElement.lang = lang;
    });
  }
}
