import { Component, inject } from '@angular/core';
import { SnackbarService } from './snackbar.service';

@Component({
  selector: 'app-snackbar-host',
  standalone: true,
  template: `
    <div class="snackbar-host" aria-live="polite">
      @for (msg of snackbar.messages(); track msg.id) {
        <div class="snackbar" [class]="'snackbar snackbar-' + msg.type" role="status">
          <span>{{ msg.text }}</span>
          <button type="button" class="snackbar-close" (click)="snackbar.dismiss(msg.id)" aria-label="Fechar">
            ×
          </button>
        </div>
      }
    </div>
  `,
  styles: [
    `
      .snackbar-host {
        position: fixed;
        right: 1rem;
        bottom: 1rem;
        z-index: 1000;
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        width: min(420px, calc(100vw - 2rem));
        pointer-events: none;
      }

      .snackbar {
        pointer-events: auto;
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 0.75rem;
        padding: 0.85rem 1rem;
        border-radius: var(--radius);
        box-shadow: 0 12px 32px rgb(15 23 42 / 18%);
        border: 1px solid transparent;
        font-size: 0.9rem;
        line-height: 1.35;
        animation: snackbar-in 0.18s ease-out;
      }

      .snackbar-success {
        background: oklch(96% 0.04 150);
        color: oklch(35% 0.1 150);
        border-color: oklch(88% 0.05 150);
      }

      .snackbar-error {
        background: color-mix(in oklch, var(--destructive) 10%, white);
        color: var(--destructive);
        border-color: color-mix(in oklch, var(--destructive) 28%, white);
      }

      .snackbar-info {
        background: color-mix(in oklch, var(--primary) 10%, white);
        color: oklch(40% 0.14 260);
        border-color: color-mix(in oklch, var(--primary) 25%, white);
      }

      .snackbar-close {
        border: none;
        background: transparent;
        color: inherit;
        cursor: pointer;
        font-size: 1.15rem;
        line-height: 1;
        padding: 0;
        opacity: 0.7;
      }

      .snackbar-close:hover {
        opacity: 1;
      }

      @keyframes snackbar-in {
        from {
          opacity: 0;
          transform: translateY(8px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }
    `,
  ],
})
export class SnackbarHostComponent {
  readonly snackbar = inject(SnackbarService);
}
