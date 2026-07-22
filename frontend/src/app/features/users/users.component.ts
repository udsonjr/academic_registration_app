import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { User, UserRole } from '../../models';
import { UserService } from '../../services/user.service';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { PagerComponent } from '../../shared/components/pager/pager.component';
import { SnackbarService } from '../../shared/components/snackbar/snackbar.service';
import { extractApiError, roleLabel } from '../../shared/utils/api-error';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [ReactiveFormsModule, ConfirmModalComponent, PagerComponent],
  templateUrl: './users.component.html',
})
export class UsersComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly snackbar = inject(SnackbarService);

  users: User[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  saving = false;
  deleting = false;
  formOpen = false;
  editing: User | null = null;
  deleteTarget: User | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: [''],
    confirmPassword: [''],
    role: ['STUDENT' as UserRole, Validators.required],
  });

  readonly roleLabel = roleLabel;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.userService.list({ page: this.page, size: this.size }).subscribe({
      next: (res) => {
        this.users = res.content;
        this.page = res.page;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.snackbar.error(extractApiError(err));
      },
    });
  }

  onPageChange(page: number): void {
    this.page = page;
    this.load();
  }

  openCreate(): void {
    this.editing = null;
    this.form.reset({
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      role: 'STUDENT',
    });
    this.form.controls.password.setValidators([
      Validators.required,
      Validators.minLength(6),
      Validators.maxLength(255),
    ]);
    this.form.controls.confirmPassword.setValidators([
      Validators.required,
      Validators.minLength(6),
    ]);
    this.form.controls.password.updateValueAndValidity();
    this.form.controls.confirmPassword.updateValueAndValidity();
    this.formOpen = true;
  }

  openEdit(user: User): void {
    this.editing = user;
    this.form.reset({
      name: user.name,
      email: user.email,
      password: '',
      confirmPassword: '',
      role: user.role,
    });
    this.form.controls.password.clearValidators();
    this.form.controls.confirmPassword.clearValidators();
    this.form.controls.password.updateValueAndValidity();
    this.form.controls.confirmPassword.updateValueAndValidity();
    this.formOpen = true;
  }

  closeForm(): void {
    if (!this.saving) {
      this.formOpen = false;
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.snackbar.error(
        this.editing
          ? 'Preencha nome e e-mail válidos.'
          : 'Preencha todos os campos. A senha deve ter no mínimo 6 caracteres.',
      );
      return;
    }
    const value = this.form.getRawValue();
    this.saving = true;

    if (this.editing) {
      this.userService
        .update(this.editing.publicId, { name: value.name, email: value.email })
        .subscribe({
          next: () => this.afterSave('Usuário atualizado.'),
          error: (err) => this.onSaveError(err),
        });
      return;
    }

    if (value.password !== value.confirmPassword) {
      this.saving = false;
      this.snackbar.error('Senha e confirmação não conferem.');
      return;
    }

    this.userService
      .create({
        name: value.name,
        email: value.email,
        password: value.password,
        confirmPassword: value.confirmPassword,
        role: value.role,
      })
      .subscribe({
        next: () => this.afterSave('Usuário criado.'),
        error: (err) => this.onSaveError(err),
      });
  }

  requestDelete(user: User): void {
    this.deleteTarget = user;
  }

  closeDelete(): void {
    if (!this.deleting) {
      this.deleteTarget = null;
    }
  }

  confirmDelete(): void {
    if (!this.deleteTarget) {
      return;
    }
    this.deleting = true;
    this.userService.delete(this.deleteTarget.publicId).subscribe({
      next: () => {
        this.deleting = false;
        this.deleteTarget = null;
        this.snackbar.success('Usuário excluído.');
        this.load();
      },
      error: (err) => {
        this.deleting = false;
        this.deleteTarget = null;
        this.snackbar.error(extractApiError(err));
      },
    });
  }

  private afterSave(message: string): void {
    this.saving = false;
    this.formOpen = false;
    this.snackbar.success(message);
    this.load();
  }

  private onSaveError(err: unknown): void {
    this.saving = false;
    this.snackbar.error(extractApiError(err));
  }
}
