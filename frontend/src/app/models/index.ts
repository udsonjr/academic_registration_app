export type UserRole = 'STUDENT' | 'ADMIN';

export type EnrollmentStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED';

export interface User {
  publicId: string;
  name: string;
  email: string;
  role: UserRole;
}

export interface Course {
  publicId: string;
  name: string;
  description: string;
  active: boolean;
}

export interface Subject {
  publicId: string;
  name: string;
  description: string;
  course: Course;
}

export interface ClassGroup {
  publicId: string;
  name: string;
  description: string;
  subject: Subject;
  enrolledStudents: number;
  vacancyLimit: number;
  openForEnrollment: boolean;
}

export interface Enrollment {
  publicId: string;
  user: User;
  classGroup: ClassGroup;
  status: EnrollmentStatus;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path: string;
  traceId?: string;
  details?: { field: string; message: string; rejectedValue?: unknown }[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
  role?: UserRole;
}

export interface UpdateUserRequest {
  name?: string;
  email?: string;
}

export interface CreateCourseRequest {
  name: string;
  description?: string;
  active: boolean;
}

export interface UpdateCourseRequest {
  name?: string;
  description?: string;
  active?: boolean;
}

export interface CreateSubjectRequest {
  name: string;
  description?: string;
  coursePublicId: string;
}

export interface UpdateSubjectRequest {
  name?: string;
  description?: string;
  coursePublicId?: string;
}

export interface CreateClassGroupRequest {
  name: string;
  description?: string;
  subjectPublicId: string;
  vacancyLimit: number;
  openForEnrollment: boolean;
}

export interface UpdateClassGroupRequest {
  name?: string;
  description?: string;
  subjectPublicId?: string;
  vacancyLimit?: number;
  openForEnrollment?: boolean;
}

export interface CreateEnrollmentRequest {
  userPublicId: string;
  classGroupPublicId: string;
}

export interface PageParams {
  page?: number;
  size?: number;
}
