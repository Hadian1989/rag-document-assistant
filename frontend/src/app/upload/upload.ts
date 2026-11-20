import {Component, inject, signal} from '@angular/core';
import {RagApi} from '../core/rag-api';
import {finalize} from 'rxjs';
import {HttpEventType} from '@angular/common/http';
import {ProgressBar} from 'primeng/progressbar';
import {Message} from 'primeng/message';
import {DocumentStore} from '../core/document-store';

const MAX_MB = 20;

@Component({
  imports: [
    ProgressBar,
    Message
  ],
  selector: 'app-upload',
  styleUrl: './upload.scss',
  templateUrl: './upload.html',
})
export class Upload {
  private api = inject(RagApi);
  private store = inject(DocumentStore);
  progress = signal<number | null>(null);
  processing = signal(false);
  dragging = signal(false);
  message = signal<{ severity: 'success' | 'error'; text: string } | null>(null)

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
      this.message.set({severity: 'error', text: 'Nur PDF-Dateien erlaubt.'});
      return;
    }
    if (file.size > MAX_MB * 1024 * 1024) {
      this.message.set({severity: 'error', text: `Maximal ${MAX_MB} MB.`});
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
          this.message.set({severity: 'success', text: `${file.name} hochgeladen`});
        }
      },
      error: () => this.message.set({severity: 'error', text: 'Upload fehlgeschlafen.'})
    });
  }
}
