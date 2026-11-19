import {Component, inject, signal} from '@angular/core';
import {RagApi} from '../core/rag-api';
import {finalize} from 'rxjs';
// @ts-ignore
import {Message} from 'primeng/message';
import {Textarea} from 'primeng/textarea';
import {FormsModule} from '@angular/forms';
import {Button} from 'primeng/button';

interface Message {
  role: 'user' | 'bot';
  text: string;
  sources?: string[];
}

@Component({
  imports: [
    Message,
    Textarea,
    FormsModule,
    Button
  ],
  selector: 'app-chat',
  styleUrl: './chat.scss',
  templateUrl: './chat.html',
})
export class Chat {
  private api = inject(RagApi);
  messages = signal<Message[]>([]);
  question = signal('');
  loading = signal(false);
  error = signal<string | null>(null);

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
}
