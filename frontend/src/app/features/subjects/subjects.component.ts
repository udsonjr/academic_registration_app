import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Course, Subject } from '../../models';
import { CourseService } from '../../services/course.service';
import { SubjectService } from '../../services/subject.service';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { PagerComponent } from '../../shared/components/pager/pager.component';
import { SnackbarService } from '../../shared/components/snackbar/snackbar.service';
import { extractApiError } from '../../shared/utils/api-error';

@Component({
  selector: 'app-subjects',
  standalone: true,
  imports: [ReactiveFormsModule, ConfirmModalComponent, PagerComponent],
  templateUrl: './subjects.component.html',
})
export class SubjectsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly subjectService = inject(SubjectService);
  private readonly courseService = inject(CourseService);
  private readonly snackbar = inject(SnackbarService);

  subjects: Subject[] = [];
  courses: Course[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  saving = false;
  deleting = false;
  formOpen = false;
  editing: Subject | null = null;
  deleteTarget: Subject | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.maxLength(500)]],
    coursePublicId: ['', Validators.required],
  });

  ngOnInit(): void {
    this.load();
    this.courseService.list({ page: 0, size: 100 }).subscribe({
      next: (res) => (this.courses = res.content),
      error: (err) => this.snackbar.error(extractApiError(err)),
    });
  }

  load(): void {
    this.loading = true;
    this.subjectService.list({ page: this.page, size: this.size }).subscribe({
      next: (res) => {
        this.subjects = res.content;
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
    this.form.reset({ name: '', description: '', coursePublicId: '' });
    this.formOpen = true;
  }

  openEdit(subject: Subject): void {
    this.editing = subject;
    this.form.reset({
      name: subject.name,
      description: subject.description ?? '',
      coursePublicId: subject.course.publicId,
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
      this.snackbar.error('Preencha nome e curso. A descrição é opcional.');
      return;
    }
    const value = this.form.getRawValue();
    this.saving = true;
    const req$ = this.editing
      ? this.subjectService.update(this.editing.publicId, value)
      : this.subjectService.create(value);
    req$.subscribe({
      next: () => {
        this.saving = false;
        this.formOpen = false;
        this.snackbar.success(this.editing ? 'Disciplina atualizada.' : 'Disciplina criada.');
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.snackbar.error(extractApiError(err));
      },
    });
  }

  requestDelete(subject: Subject): void {
    this.deleteTarget = subject;
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
    this.subjectService.delete(this.deleteTarget.publicId).subscribe({
      next: () => {
        this.deleting = false;
        this.deleteTarget = null;
        this.snackbar.success('Disciplina excluída.');
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
