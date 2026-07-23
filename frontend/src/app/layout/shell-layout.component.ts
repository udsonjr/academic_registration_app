import { Component, inject, computed } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import {
  BookOpen,
  ClipboardList,
  Layers,
  LogOut,
  LucideAngularModule,
  LucideIconData,
  NotebookPen,
  User,
  Users,
} from 'lucide-angular';
import { AuthService } from '../core/auth.service';

interface NavItem {
  label: string;
  path: string;
  icon: LucideIconData;
  adminOnly?: boolean;
}

@Component({
  selector: 'app-shell-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LucideAngularModule],
  templateUrl: './shell-layout.component.html',
  styleUrl: './shell-layout.component.scss',
})
export class ShellLayoutComponent {
  private readonly auth = inject(AuthService);

  readonly logOutIcon = LogOut;

  private readonly allItems: NavItem[] = [
    { label: 'Perfil', path: '/profile', icon: User },
    { label: 'Matrículas', path: '/enrollments', icon: ClipboardList },
    { label: 'Usuários', path: '/users', icon: Users, adminOnly: true },
    { label: 'Cursos', path: '/courses', icon: BookOpen, adminOnly: true },
    { label: 'Disciplinas', path: '/subjects', icon: NotebookPen, adminOnly: true },
    { label: 'Turmas', path: '/class-groups', icon: Layers, adminOnly: true },
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
