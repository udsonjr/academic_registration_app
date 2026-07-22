import { Component, inject, computed } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

interface NavItem {
  label: string;
  path: string;
  adminOnly?: boolean;
}

@Component({
  selector: 'app-shell-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell-layout.component.html',
  styleUrl: './shell-layout.component.scss',
})
export class ShellLayoutComponent {
  private readonly auth = inject(AuthService);

  private readonly allItems: NavItem[] = [
    { label: 'Perfil', path: '/profile' },
    { label: 'Matrículas', path: '/enrollments' },
    { label: 'Usuários', path: '/users', adminOnly: true },
    { label: 'Cursos', path: '/courses', adminOnly: true },
    { label: 'Disciplinas', path: '/subjects', adminOnly: true },
    { label: 'Turmas', path: '/class-groups', adminOnly: true },
  ];

  readonly navItems = computed(() => {
    const isAdmin = this.auth.isAdmin();
    return this.allItems.filter((item) => !item.adminOnly || isAdmin);
  });

  readonly user = this.auth.currentUser;

  logout(): void {
    this.auth.logout();
  }
}
