import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth.service';
import { SnackbarService } from '../../../shared/components/snackbar/snackbar.service';
import { extractApiError } from '../../../shared/utils/api-error';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackbar = inject(SnackbarService);

  loading = false;

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(255)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(255)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.snackbar.error('Preencha todos os campos corretamente. A senha deve ter no mínimo 6 caracteres.');
      return;
    }
    const value = this.form.getRawValue();
    if (value.password !== value.confirmPassword) {
      this.snackbar.error('Senha e confirmação não conferem.');
      return;
    }
    this.loading = true;
    this.auth.register(value).subscribe({
      next: () => {
        this.loading = false;
        this.snackbar.success('Conta criada. Faça login para continuar.');
        void this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading = false;
        this.snackbar.error(extractApiError(err, 'Falha no cadastro.'));
      },
    });
  }
}
