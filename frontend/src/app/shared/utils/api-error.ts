import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorResponse } from '../../models';

export function extractApiError(error: unknown, fallback = 'Ocorreu um erro inesperado.'): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as ApiErrorResponse | string | null;
    if (body && typeof body === 'object' && 'message' in body && body.message) {
      return body.message;
    }
    if (typeof body === 'string' && body.trim()) {
      return body;
    }
    if (error.status === 0) {
      return 'Não foi possível conectar ao servidor.';
    }
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export function enrollmentStatusLabel(status: string): string {
  switch (status) {
    case 'PENDING':
      return 'Pendente';
    case 'CONFIRMED':
      return 'Confirmada';
    case 'CANCELLED':
      return 'Cancelada';
    default:
      return status;
  }
}

export function roleLabel(role: string): string {
  switch (role) {
    case 'ADMIN':
      return 'Administrador';
    case 'STUDENT':
      return 'Aluno';
    default:
      return role;
  }
}
