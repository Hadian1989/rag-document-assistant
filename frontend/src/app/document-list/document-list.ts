import {Component, inject} from '@angular/core';
import {DocumentStore} from '../core/document-store';
import {Message} from 'primeng/message';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  imports: [
    Message,
    TranslatePipe
  ],
  selector: 'app-document-list',
  styleUrl: './document-list.scss',
  templateUrl: './document-list.html',
})
export class DocumentList {
  store = inject(DocumentStore);
}
