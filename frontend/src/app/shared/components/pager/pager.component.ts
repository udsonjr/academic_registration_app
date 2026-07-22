import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-pager',
  standalone: true,
  template: `
    @if (totalPages > 1) {
      <div class="pager">
        <button type="button" class="btn btn-ghost" [disabled]="page <= 0" (click)="go(page - 1)">
          Anterior
        </button>
        <span>Página {{ page + 1 }} de {{ totalPages }} ({{ totalElements }} itens)</span>
        <button
          type="button"
          class="btn btn-ghost"
          [disabled]="page >= totalPages - 1"
          (click)="go(page + 1)"
        >
          Próxima
        </button>
      </div>
    }
  `,
  styles: [
    `
      .pager {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
        margin-top: 1rem;
        font-size: 0.875rem;
        color: var(--muted-foreground);
      }
    `,
  ],
})
export class PagerComponent {
  @Input() page = 0;
  @Input() totalPages = 0;
  @Input() totalElements = 0;
  @Output() pageChange = new EventEmitter<number>();

  go(next: number): void {
    this.pageChange.emit(next);
  }
}
