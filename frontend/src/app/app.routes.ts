import { Routes } from '@angular/router';
import { adminGuard, authGuard, guestGuard } from './core/auth.guard';
import { ShellLayoutComponent } from './layout/shell-layout.component';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { ProfileComponent } from './features/profile/profile.component';
import { EnrollmentsComponent } from './features/enrollments/enrollments.component';
import { UsersComponent } from './features/users/users.component';
import { CoursesComponent } from './features/courses/courses.component';
import { SubjectsComponent } from './features/subjects/subjects.component';
import { ClassGroupsComponent } from './features/class-groups/class-groups.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [guestGuard] },
  {
    path: '',
    component: ShellLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'enrollments' },
      { path: 'enrollments', component: EnrollmentsComponent },
      { path: 'profile', component: ProfileComponent },
      { path: 'users', component: UsersComponent, canActivate: [adminGuard] },
      { path: 'courses', component: CoursesComponent, canActivate: [adminGuard] },
      { path: 'subjects', component: SubjectsComponent, canActivate: [adminGuard] },
      { path: 'class-groups', component: ClassGroupsComponent, canActivate: [adminGuard] },
    ],
  },
  { path: '**', redirectTo: 'enrollments' },
];
