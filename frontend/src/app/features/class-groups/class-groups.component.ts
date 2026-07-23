import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LucideAngularModule, Pencil, Trash2 } from 'lucide-angular';
import { ClassGroup, Course, Subject } from '../../models';
import { ClassGroupService } from '../../services/class-group.service';
import { CourseService } from '../../services/course.service';
import { SubjectService } from '../../services/subject.service';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { PagerComponent } from '../../shared/components/pager/pager.component';
import { SnackbarService } from '../../shared/components/snackbar/snackbar.service';
import { extractApiError } from '../../shared/utils/api-error';

@Component({
  selector: 'app-class-groups',
  standalone: true,
  imports: [ReactiveFormsModule, ConfirmModalComponent, PagerComponent, LucideAngularModule],
  templateUrl: './class-groups.component.html',
})
export class ClassGroupsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly classGroupService = inject(ClassGroupService);
  private readonly subjectService = inject(SubjectService);
  private readonly courseService = inject(CourseService);
  private readonly snackbar = inject(SnackbarService);

  readonly pencilIcon = Pencil;
  readonly trashIcon = Trash2;

  classGroups: ClassGroup[] = [];
  subjects: Subject[] = [];
  formSubjects: Subject[] = [];
  courses: Course[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  saving = false;
  deleting = false;
  formOpen = false;
  editing: ClassGroup | null = null;
  deleteTarget: ClassGroup | null = null;

  readonly filters = this.fb.nonNullable.group({
    name: [''],
    openForEnrollment: ['' as '' | 'true' | 'false'],
    coursePublicId: [''],
    subjectPublicId: [''],
  });

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.maxLength(500)]],
    coursePublicId: ['', Validators.required],
    subjectPublicId: ['', Validators.required],
    vacancyLimit: [40, [Validators.required, Validators.min(1)]],
    openForEnrollment: [true],
  });

  ngOnInit(): void {
    this.load();
    this.courseService.list({ page: 0, size: 100 }).subscribe({
      next: (res) => (this.courses = res.content),
      error: (err) => this.snackbar.error(extractApiError(err)),
    });
    this.loadSubjects();
  }

  loadSubjects(coursePublicId?: string): void {
    this.subjectService
      .list({
        page: 0,
        size: 100,
        ...(coursePublicId ? { coursePublicId } : {}),
      })
      .subscribe({
        next: (res) => (this.subjects = res.content),
        error: (err) => this.snackbar.error(extractApiError(err)),
      });
  }

  loadFormSubjects(coursePublicId?: string): void {
    if (!coursePublicId) {
      this.formSubjects = [];
      return;
    }
    this.subjectService
      .list({
        page: 0,
        size: 100,
        coursePublicId,
      })
      .subscribe({
        next: (res) => (this.formSubjects = res.content),
        error: (err) => this.snackbar.error(extractApiError(err)),
      });
  }

  load(): void {
    this.loading = true;
    const f = this.filters.getRawValue();
    this.classGroupService
      .list({
        page: this.page,
        size: this.size,
        name: f.name.trim() || undefined,
        openForEnrollment:
          f.openForEnrollment === '' ? undefined : f.openForEnrollment === 'true',
        coursePublicId: f.coursePublicId || undefined,
        subjectPublicId: f.subjectPublicId || undefined,
      })
      .subscribe({
        next: (res) => {
          this.classGroups = res.content;
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
    this.filters.reset({
      name: '',
      openForEnrollment: '',
      coursePublicId: '',
      subjectPublicId: '',
    });
    this.page = 0;
    this.loadSubjects();
    this.load();
  }

  onFilterCourseChange(): void {
    this.filters.patchValue({ subjectPublicId: '' });
    const coursePublicId = this.filters.controls.coursePublicId.value;
    this.loadSubjects(coursePublicId || undefined);
  }

  onFormCourseChange(): void {
    this.form.patchValue({ subjectPublicId: '' });
    const coursePublicId = this.form.controls.coursePublicId.value;
    this.syncSubjectControl(!!coursePublicId);
    this.loadFormSubjects(coursePublicId || undefined);
  }

  private syncSubjectControl(enabled: boolean): void {
    const control = this.form.controls.subjectPublicId;
    if (enabled) {
      control.enable({ emitEvent: false });
    } else {
      control.disable({ emitEvent: false });
    }
  }

  onPageChange(page: number): void {
    this.page = page;
    this.load();
  }

  openCreate(): void {
    this.editing = null;
    this.formSubjects = [];
    this.form.reset({
      name: '',
      description: '',
      coursePublicId: '',
      subjectPublicId: '',
      vacancyLimit: 40,
      openForEnrollment: true,
    });
    this.syncSubjectControl(false);
    this.formOpen = true;
  }

  openEdit(group: ClassGroup): void {
    this.editing = group;
    this.form.reset({
      name: group.name,
      description: group.description ?? '',
      coursePublicId: group.subject.course.publicId,
      subjectPublicId: group.subject.publicId,
      vacancyLimit: group.vacancyLimit,
      openForEnrollment: group.openForEnrollment,
    });
    this.syncSubjectControl(true);
    this.loadFormSubjects(group.subject.course.publicId);
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
      this.snackbar.error('Preencha nome, curso, disciplina e limite de vagas. A descrição é opcional.');
      return;
    }
    const value = this.form.getRawValue();
    const payload = {
      name: value.name,
      description: value.description,
      subjectPublicId: value.subjectPublicId,
      vacancyLimit: value.vacancyLimit,
      openForEnrollment: value.openForEnrollment,
    };
    this.saving = true;
    const req$ = this.editing
      ? this.classGroupService.update(this.editing.publicId, payload)
      : this.classGroupService.create(payload);
    req$.subscribe({
      next: () => {
        this.saving = false;
        this.formOpen = false;
        this.snackbar.success(this.editing ? 'Turma atualizada.' : 'Turma criada.');
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.snackbar.error(extractApiError(err));
      },
    });
  }

  requestDelete(group: ClassGroup): void {
    this.deleteTarget = group;
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
    this.classGroupService.delete(this.deleteTarget.publicId).subscribe({
      next: () => {
        this.deleting = false;
        this.deleteTarget = null;
        this.snackbar.success('Turma excluída.');
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
