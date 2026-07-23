import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LucideAngularModule, Pencil, Trash2 } from 'lucide-angular';
import { Course } from '../../models';
import { CourseService } from '../../services/course.service';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { PagerComponent } from '../../shared/components/pager/pager.component';
import { SnackbarService } from '../../shared/components/snackbar/snackbar.service';
import { extractApiError } from '../../shared/utils/api-error';

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [ReactiveFormsModule, ConfirmModalComponent, PagerComponent, LucideAngularModule],
  templateUrl: './courses.component.html',
})
export class CoursesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly courseService = inject(CourseService);
  private readonly snackbar = inject(SnackbarService);

  readonly pencilIcon = Pencil;
  readonly trashIcon = Trash2;

  courses: Course[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  saving = false;
  deleting = false;
  formOpen = false;
  editing: Course | null = null;
  deleteTarget: Course | null = null;

  readonly filters = this.fb.nonNullable.group({
    name: [''],
    active: ['' as '' | 'true' | 'false'],
  });

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.maxLength(500)]],
    active: [true],
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    const f = this.filters.getRawValue();
    this.courseService
      .list({
        page: this.page,
        size: this.size,
        name: f.name.trim() || undefined,
        active: f.active === '' ? undefined : f.active === 'true',
      })
      .subscribe({
        next: (res) => {
          this.courses = res.content;
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

  applyFilters(): void {
    this.page = 0;
    this.load();
  }

  clearFilters(): void {
    this.filters.reset({ name: '', active: '' });
    this.page = 0;
    this.load();
  }

  onPageChange(page: number): void {
    this.page = page;
    this.load();
  }

  openCreate(): void {
    this.editing = null;
    this.form.reset({ name: '', description: '', active: true });
    this.formOpen = true;
  }

  openEdit(course: Course): void {
    this.editing = course;
    this.form.reset({
      name: course.name,
      description: course.description ?? '',
      active: course.active,
    });
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
      this.snackbar.error('Informe o nome do curso (obrigatório). A descrição é opcional.');
      return;
    }
    const value = this.form.getRawValue();
    this.saving = true;
    const req$ = this.editing
      ? this.courseService.update(this.editing.publicId, value)
      : this.courseService.create(value);
    req$.subscribe({
      next: () => {
        this.saving = false;
        this.formOpen = false;
        this.snackbar.success(this.editing ? 'Curso atualizado.' : 'Curso criado.');
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.snackbar.error(extractApiError(err));
      },
    });
  }

  requestDelete(course: Course): void {
    this.deleteTarget = course;
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
    this.courseService.delete(this.deleteTarget.publicId).subscribe({
      next: () => {
        this.deleting = false;
        this.deleteTarget = null;
        this.snackbar.success('Curso excluído.');
        this.load();
      },
      error: (err) => {
        this.deleting = false;
        this.deleteTarget = null;
        this.snackbar.error(extractApiError(err));
      },
    });
  }
}
