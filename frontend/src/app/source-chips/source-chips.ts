import {Component, input} from '@angular/core';
import {Chip} from 'primeng/chip';

@Component({
  imports: [
    Chip
  ],
  selector: 'app-source-chips',
  styleUrl: './source-chips.scss',
  template: `
    <div class="flex flex-wrap gap-2 mt-2">
      @for (s of sources(); track s) {
        <p-chip [label]="'📄' + s"/>
      }
    </div>
  `
})
export class SourceChips {
  sources = input.required<string[]>();
}

