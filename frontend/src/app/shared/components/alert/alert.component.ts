import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-alert',
  standalone: true,
  template: `
    @if (message) {
      <div class="alert" [class.alert-error]="type === 'error'" [class.alert-success]="type === 'success'">
        {{ message }}
      </div>
    }
  `,
  styles: [
    `
      .alert {
        padding: 0.75rem 1rem;
        border-radius: var(--radius);
        margin-bottom: 1rem;
        font-size: 0.9rem;
      }
      .alert-error {
        background: color-mix(in oklch, var(--destructive) 12%, white);
        color: var(--destructive);
        border: 1px solid color-mix(in oklch, var(--destructive) 30%, white);
      }
      .alert-success {
        background: oklch(96% 0.03 150);
        color: oklch(40% 0.1 150);
        border: 1px solid oklch(88% 0.05 150);
      }
    `,
  ],
})
export class AlertComponent {
  @Input() message = '';
  @Input() type: 'error' | 'success' = 'error';
}
