import {afterRenderEffect, Component, ElementRef, inject, signal, viewChild} from '@angular/core';
import {RagApi} from '../core/rag-api';
import {finalize} from 'rxjs';
import {Message} from 'primeng/message';
import {Textarea} from 'primeng/textarea';
import {FormsModule} from '@angular/forms';
import {ButtonDirective} from 'primeng/button';
import {SourceChips} from '../source-chips/source-chips';

interface ChatMessage {
  role: 'user' | 'bot';
  text: string;
  sources?: string[];
}

@Component({
  imports: [
    Message,
    Textarea,
    FormsModule,
    SourceChips,
    ButtonDirective
  ],
  selector: 'app-chat',
  styleUrl: './chat.scss',
  templateUrl: './chat.html',
})
export class Chat {
  private api = inject(RagApi);
  private scroller = viewChild<ElementRef<HTMLElement>>('scroller');
  messages = signal<ChatMessage[]>([]);
  question = signal('');
  loading = signal(false);
  error = signal<string | null>(null);

  constructor() {
    afterRenderEffect(() => {
      this.messages();
      const el = this.scroller()?.nativeElement;
      el?.scrollTo({top: el.scrollHeight, behavior: 'smooth'});
    })
  }

  send() {
    const q = this.question().trim();
    if (!q || this.loading()) return;
    this.messages.update(m => [...m, {role: 'user', text: q}]);
    this.question.set('');
    this.error.set(null);
    this.loading.set(true);
    this.api.ask(q).pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: a => this.messages.update(m => [...m, {role: 'bot', text: a.answer, sources: a.sources}]),
        error: () => this.error.set('Die Antwort konnte nicht geladen werden.')
      });
  }

  onEnter(event: Event) {
    event.preventDefault();   // kein Zeilenumbruch
    this.send();
  }
}
