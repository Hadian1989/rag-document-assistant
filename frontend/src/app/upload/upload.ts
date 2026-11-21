import {Component, inject, signal} from '@angular/core';
import {RagApi} from '../core/rag-api';
import {finalize} from 'rxjs';
import {HttpEventType} from '@angular/common/http';
import {ProgressBar} from 'primeng/progressbar';
import {Message} from 'primeng/message';
import {DocumentStore} from '../core/document-store';
import {MessageService} from 'primeng/api';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

const MAX_MB = 20;

@Component({
  imports: [
    ProgressBar,
    Message,
    TranslatePipe
  ],
  selector: 'app-upload',
  styleUrl: './upload.scss',
  templateUrl: './upload.html',
})
export class Upload {
  private api = inject(RagApi);
  private store = inject(DocumentStore);
  private toast = inject(MessageService);
  private translate = inject(TranslateService);
  progress = signal<number | null>(null);
  processing = signal(false);
  dragging = signal(false);
  // Store the translation key (not the text), so the message follows a language switch
  message = signal<{ severity: 'success' | 'error'; key: string; params?: Record<string, unknown> } | null>(null);

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragging.set(true);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragging.set(false);
    const file = event.dataTransfer?.files[0];
    if (file) this.uploadFile(file);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      this.uploadFile(file);
      input.value = '';
    }
  }

  private uploadFile(file: File) {
    if (this.progress() !== null) return;
    if (file.type !== 'application/pdf') {
      this.message.set({severity: 'error', key: 'UPLOAD.ONLY_PDF'});
      this.showToast('warn', 'UPLOAD.WRONG_TYPE_TITLE', 'UPLOAD.ONLY_PDF');
      return;
    }
    if (file.size > MAX_MB * 1024 * 1024) {
      this.message.set({severity: 'error', key: 'UPLOAD.TOO_BIG', params: {max: MAX_MB}});
      this.showToast('warn', 'UPLOAD.TOO_BIG_TITLE', 'UPLOAD.TOO_BIG', {max: MAX_MB});
      return;
    }
    this.message.set(null);
    this.progress.set(0);
    this.api.upload(file).pipe(finalize(() => {
      this.progress.set(null);
      this.processing.set(false);
    })).subscribe({
      next: event => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          const p = Math.round(100 * event.loaded / event.total);
          this.progress.set(p);
          if (p === 100) this.processing.set(true);
        }
        if (event.type === HttpEventType.Response) {
          this.store.reload();
          this.message.set({severity: 'success', key: 'UPLOAD.SUCCESS', params: {name: file.name}});
          this.toast.add({
            severity: 'success',
            summary: this.translate.instant('UPLOAD.SUCCESS_TITLE'),
            detail: file.name,
          });
        }
      },
      error: () => {
        this.message.set({severity: 'error', key: 'UPLOAD.FAILED'});
        this.showToast('error', 'UPLOAD.FAILED_TITLE', 'UPLOAD.RETRY');
      }
    })
  }

  // A toast is only visible for a few seconds, so translating it once with instant() is enough
  private showToast(severity: 'warn' | 'error', summaryKey: string, detailKey: string, params?: Record<string, unknown>) {
    this.toast.add({
      severity,
      summary: this.translate.instant(summaryKey),
      detail: this.translate.instant(detailKey, params),
    });
  }
}
