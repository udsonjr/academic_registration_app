import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { ClassGroup, Course, Enrollment, Subject, User } from '../../models';
import { ClassGroupService } from '../../services/class-group.service';
import { CourseService } from '../../services/course.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { SubjectService } from '../../services/subject.service';
import { UserService } from '../../services/user.service';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { PagerComponent } from '../../shared/components/pager/pager.component';
import { SnackbarService } from '../../shared/components/snackbar/snackbar.service';
import { enrollmentStatusLabel, extractApiError } from '../../shared/utils/api-error';

@Component({
  selector: 'app-enrollments',
  standalone: true,
  imports: [ReactiveFormsModule, ConfirmModalComponent, PagerComponent],
  templateUrl: './enrollments.component.html',
})
export class EnrollmentsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly enrollmentService = inject(EnrollmentService);
  private readonly courseService = inject(CourseService);
  private readonly subjectService = inject(SubjectService);
  private readonly classGroupService = inject(ClassGroupService);
  private readonly userService = inject(UserService);
  private readonly snackbar = inject(SnackbarService);

  enrollments: Enrollment[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  loading = false;
  actionLoading = false;

  createOpen = false;
  createLoading = false;
  createStep = 1;

  courses: Course[] = [];
  subjects: Subject[] = [];
  classGroups: ClassGroup[] = [];
  users: User[] = [];

  confirmOpen = false;
  confirmTitle = '';
  confirmMessage = '';
  confirmLabel = 'Confirmar';
  pendingAction: (() => void) | null = null;

  readonly createForm = this.fb.nonNullable.group({
    userPublicId: [''],
    coursePublicId: ['', Validators.required],
    subjectPublicId: ['', Validators.required],
    classGroupPublicId: ['', Validators.required],
  });

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  get pageTitle(): string {
    return this.isAdmin ? 'Matrículas' : 'Minhas matrículas';
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.enrollmentService.list({ page: this.page, size: this.size }).subscribe({
      next: (response) => {
        this.enrollments = response.content;
        this.page = response.page;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
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

  statusLabel(status: string): string {
    return enrollmentStatusLabel(status);
  }

  statusClass(status: string): string {
    return `badge badge-${status.toLowerCase()}`;
  }

  openCreate(): void {
    this.createOpen = true;
    this.createStep = this.isAdmin ? 0 : 1;
    this.createLoading = false;
    this.createForm.reset({
      userPublicId: this.auth.currentUser()?.publicId ?? '',
      coursePublicId: '',
      subjectPublicId: '',
      classGroupPublicId: '',
    });
    this.subjects = [];
    this.classGroups = [];
    this.loadCourses();
    if (this.isAdmin) {
      this.loadUsers();
    }
  }

  closeCreate(): void {
    if (!this.createLoading) {
      this.createOpen = false;
    }
  }

  loadUsers(): void {
    this.userService.list({ page: 0, size: 100 }).subscribe({
      next: (res) => (this.users = res.content.filter((u) => u.role === 'STUDENT')),
      error: (err) => this.snackbar.error(extractApiError(err)),
    });
  }

  loadCourses(): void {
    this.courseService.list({ page: 0, size: 100 }).subscribe({
      next: (res) => (this.courses = res.content.filter((c) => c.active)),
      error: (err) => this.snackbar.error(extractApiError(err)),
    });
  }

  onCourseChange(): void {
    const coursePublicId = this.createForm.controls.coursePublicId.value;
    this.createForm.patchValue({ subjectPublicId: '', classGroupPublicId: '' });
    this.subjects = [];
    this.classGroups = [];
    if (!coursePublicId) {
      return;
    }
    this.subjectService.list({ page: 0, size: 100, coursePublicId }).subscribe({
      next: (res) => (this.subjects = res.content),
      error: (err) => this.snackbar.error(extractApiError(err)),
    });
  }

  onSubjectChange(): void {
    const subjectPublicId = this.createForm.controls.subjectPublicId.value;
    this.createForm.patchValue({ classGroupPublicId: '' });
    this.classGroups = [];
    if (!subjectPublicId) {
      return;
    }
    this.classGroupService.list({ page: 0, size: 100, subjectPublicId }).subscribe({
      next: (res) => (this.classGroups = res.content.filter((g) => g.openForEnrollment)),
      error: (err) => this.snackbar.error(extractApiError(err)),
    });
  }

  nextCreateStep(): void {
    if (this.createStep === 0 && !this.createForm.controls.userPublicId.value) {
      this.snackbar.error('Selecione um aluno.');
      return;
    }
    if (this.createStep === 1 && !this.createForm.controls.coursePublicId.value) {
      this.snackbar.error('Selecione um curso.');
      return;
    }
    if (this.createStep === 2 && !this.createForm.controls.subjectPublicId.value) {
      this.snackbar.error('Selecione uma disciplina.');
      return;
    }
    this.createStep += 1;
  }

  prevCreateStep(): void {
    this.createStep -= 1;
  }

  submitCreate(): void {
    const value = this.createForm.getRawValue();
    const userPublicId = this.isAdmin
      ? value.userPublicId
      : (this.auth.currentUser()?.publicId ?? '');
    if (!userPublicId || !value.classGroupPublicId) {
      this.snackbar.error('Preencha todos os passos da matrícula.');
      return;
    }
    this.createLoading = true;
    this.enrollmentService
      .create({ userPublicId, classGroupPublicId: value.classGroupPublicId })
      .subscribe({
        next: () => {
          this.createLoading = false;
          this.createOpen = false;
          this.snackbar.success('Matrícula criada com sucesso.');
          this.page = 0;
          this.load();
        },
        error: (err) => {
          this.createLoading = false;
          this.snackbar.error(extractApiError(err));
        },
      });
  }

  requestCancel(enrollment: Enrollment): void {
    this.confirmTitle = 'Cancelar matrícula';
    this.confirmMessage = 'Deseja cancelar esta matrícula?';
    this.confirmLabel = 'Cancelar matrícula';
    this.pendingAction = () => this.runCancel(enrollment.publicId);
    this.confirmOpen = true;
  }

  requestConfirm(enrollment: Enrollment): void {
    this.confirmTitle = 'Aprovar matrícula';
    this.confirmMessage = 'Deseja aprovar esta matrícula?';
    this.confirmLabel = 'Aprovar';
    this.pendingAction = () => this.runConfirm(enrollment.publicId);
    this.confirmOpen = true;
  }

  closeConfirm(): void {
    if (!this.actionLoading) {
      this.confirmOpen = false;
      this.pendingAction = null;
    }
  }

  executeConfirm(): void {
    this.pendingAction?.();
  }

  private runCancel(publicId: string): void {
    this.actionLoading = true;
    this.enrollmentService.cancel(publicId).subscribe({
      next: () => {
        this.actionLoading = false;
        this.confirmOpen = false;
        this.snackbar.success('Matrícula cancelada.');
        this.load();
      },
      error: (err) => {
        this.actionLoading = false;
        this.confirmOpen = false;
        this.snackbar.error(extractApiError(err));
      },
    });
  }

  private runConfirm(publicId: string): void {
    this.actionLoading = true;
    this.enrollmentService.confirm(publicId).subscribe({
      next: () => {
        this.actionLoading = false;
        this.confirmOpen = false;
        this.snackbar.success('Matrícula aprovada.');
        this.load();
      },
      error: (err) => {
        this.actionLoading = false;
        this.confirmOpen = false;
        this.snackbar.error(extractApiError(err));
      },
    });
  }
}
