import { Injectable, signal } from '@angular/core';

export type SnackbarType = 'success' | 'error' | 'info';

export interface SnackbarMessage {
  id: number;
  text: string;
  type: SnackbarType;
}

@Injectable({ providedIn: 'root' })
export class SnackbarService {
  private nextId = 1;
  private readonly timeouts = new Map<number, ReturnType<typeof setTimeout>>();

  readonly messages = signal<SnackbarMessage[]>([]);

  success(text: string): void {
    this.show(text, 'success');
  }

  error(text: string): void {
    this.show(text, 'error');
  }

  info(text: string): void {
    this.show(text, 'info');
  }

  dismiss(id: number): void {
    const timer = this.timeouts.get(id);
    if (timer) {
      clearTimeout(timer);
      this.timeouts.delete(id);
    }
    this.messages.update((list) => list.filter((m) => m.id !== id));
  }

  private show(text: string, type: SnackbarType): void {
    const id = this.nextId++;
    this.messages.update((list) => [...list, { id, text, type }]);
    const timer = setTimeout(() => this.dismiss(id), 4500);
    this.timeouts.set(id, timer);
  }
}
