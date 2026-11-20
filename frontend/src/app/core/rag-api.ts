import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpEvent} from '@angular/common/http';
import {Observable} from 'rxjs';

export interface Answer {
  answer: string;
  sources: string[];
}

export interface DocumentInfo {
  fileName: string;
  pages: number;
}

@Injectable({providedIn: 'root'})
export class RagApi {
  private http = inject(HttpClient);
  private sessionId = Date.now();


  ask(question: string): Observable<Answer> {
    return this.http.post<Answer>('/api/rag/ask', {sessionId: this.sessionId, question});
  }

  documents(): Observable<DocumentInfo[]> {
    return this.http.get<DocumentInfo[]>('/api/rag/documents');
  }

  upload(file: File): Observable<HttpEvent<string>> {
    const body = new FormData();
    body.append('file', file);
    return this.http.post<HttpEvent<string>>('/api/rag/documents', body, {
      responseType: 'text', observe: 'events', reportProgress: true
    });
  }
}
