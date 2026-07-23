import { HttpErrorResponse } from '@angular/common/http';
import {
  enrollmentStatusLabel,
  extractApiError,
  roleLabel,
  translateErrorCode,
  translateFieldError,
} from './api-error';

describe('extractApiError', () => {
  it('translates known error codes to Portuguese', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: {
        code: 'EMAIL_ALREADY_REGISTERED',
        message: 'Email already registered: a@b.com',
      },
    });
    expect(extractApiError(error)).toBe('E-mail já cadastrado: a@b.com');
  });

  it('translates validation detail messages from English', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: {
        code: 'VALIDATION_ERROR',
        message: 'One or more fields are invalid',
        details: [
          { field: 'email', message: 'must be a well-formed email address' },
          { field: 'password', message: 'size must be between 6 and 255' },
        ],
      },
    });
    expect(extractApiError(error)).toBe(
      'Informe um e-mail válido. O campo senha deve ter entre 6 e 255 caracteres.',
    );
  });

  it('returns connection message when status is 0', () => {
    const error = new HttpErrorResponse({ status: 0, error: null });
    expect(extractApiError(error)).toBe('Não foi possível conectar ao servidor.');
  });

  it('returns fallback for unknown errors', () => {
    expect(extractApiError({})).toBe('Ocorreu um erro inesperado.');
  });
});

describe('translateErrorCode', () => {
  it('maps business codes', () => {
    expect(translateErrorCode('PASSWORD_MISMATCH')).toBe(
      'Senha e confirmação de senha não conferem.',
    );
    expect(translateErrorCode('CLASS_GROUP_FULL')).toBe('A turma não possui vagas disponíveis.');
  });

  it('returns null for unknown codes', () => {
    expect(translateErrorCode('UNKNOWN_CODE')).toBeNull();
  });
});

describe('translateFieldError', () => {
  it('translates required field messages', () => {
    expect(translateFieldError('name', 'must not be blank')).toBe('O campo nome é obrigatório.');
  });
});

describe('enrollmentStatusLabel', () => {
  it('maps known statuses to Portuguese', () => {
    expect(enrollmentStatusLabel('PENDING')).toBe('Pendente');
    expect(enrollmentStatusLabel('CONFIRMED')).toBe('Confirmada');
    expect(enrollmentStatusLabel('CANCELLED')).toBe('Cancelada');
  });
});

describe('roleLabel', () => {
  it('maps known roles to Portuguese', () => {
    expect(roleLabel('ADMIN')).toBe('Administrador');
    expect(roleLabel('STUDENT')).toBe('Aluno');
  });
});
