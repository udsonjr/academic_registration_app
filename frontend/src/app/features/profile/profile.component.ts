import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { UserService } from '../../services/user.service';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { SnackbarService } from '../../shared/components/snackbar/snackbar.service';
import { extractApiError, roleLabel } from '../../shared/utils/api-error';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, ConfirmModalComponent],
  templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly snackbar = inject(SnackbarService);

  loading = false;
  deleting = false;
  confirmDeleteOpen = false;
  roleText = '';

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
  });

  ngOnInit(): void {
    const user = this.auth.currentUser();
    if (!user) {
      return;
    }
    this.roleText = roleLabel(user.role);
    this.loading = true;
    this.userService.getById(user.publicId).subscribe({
      next: (fresh) => {
        this.form.patchValue({ name: fresh.name, email: fresh.email });
        this.auth.setCurrentUser(fresh);
        this.roleText = roleLabel(fresh.role);
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.snackbar.error(extractApiError(err));
      },
    });
  }

  save(): void {
    const user = this.auth.currentUser();
    if (!user) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.snackbar.error('Preencha nome e e-mail válidos.');
      return;
    }
    this.loading = true;
    this.userService.update(user.publicId, this.form.getRawValue()).subscribe({
      next: (updated) => {
        this.auth.setCurrentUser(updated);
        this.loading = false;
        this.snackbar.success('Perfil atualizado.');
      },
      error: (err) => {
        this.loading = false;
        this.snackbar.error(extractApiError(err));
      },
    });
  }

  openDelete(): void {
    this.confirmDeleteOpen = true;
  }

  closeDelete(): void {
    if (!this.deleting) {
      this.confirmDeleteOpen = false;
    }
  }

  confirmDelete(): void {
    const user = this.auth.currentUser();
    if (!user) {
      return;
    }
    this.deleting = true;
    this.userService.delete(user.publicId).subscribe({
      next: () => {
        this.deleting = false;
        this.confirmDeleteOpen = false;
        this.snackbar.success('Conta excluída.');
        this.auth.logout();
      },
      error: (err) => {
        this.deleting = false;
        this.confirmDeleteOpen = false;
        this.snackbar.error(extractApiError(err));
      },
    });
  }
}
