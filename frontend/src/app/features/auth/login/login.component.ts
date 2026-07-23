import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Eye, EyeOff, LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/auth.service';
import { SnackbarService } from '../../../shared/components/snackbar/snackbar.service';
import { extractApiError } from '../../../shared/utils/api-error';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, LucideAngularModule],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackbar = inject(SnackbarService);

  readonly eyeIcon = Eye;
  readonly eyeOffIcon = EyeOff;

  loading = false;
  showPassword = false;

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.snackbar.error('Informe e-mail e senha para entrar.');
      return;
    }
    this.loading = true;
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigate(['/enrollments']);
      },
      error: (err) => {
        this.loading = false;
        this.snackbar.error(extractApiError(err, 'Falha no login.'));
      },
    });
  }
}
