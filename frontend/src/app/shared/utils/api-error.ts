import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorResponse } from '../../models';

const ERROR_CODE_MESSAGES: Record<string, string | ((message: string) => string)> = {
  UNAUTHORIZED: 'Credenciais inválidas ou autenticação necessária.',
  UNAUTHENTICATED: 'Autenticação é obrigatória.',
  ACCESS_DENIED: 'Você não tem permissão para realizar esta ação.',
  FORBIDDEN: 'Você não tem permissão para realizar esta ação.',
  VALIDATION_ERROR: 'Um ou mais campos são inválidos.',
  CONSTRAINT_VIOLATION: 'As restrições da requisição foram violadas.',
  TYPE_MISMATCH: (message) => translateTypeMismatch(message),
  MISSING_PARAMETER: (message) => translateMissingParameter(message),
  MALFORMED_REQUEST: 'O corpo da requisição está ausente ou malformado.',
  INTERNAL_ERROR: 'Ocorreu um erro inesperado. Tente novamente mais tarde.',
  EMAIL_ALREADY_REGISTERED: (message) => {
    const email = extractAfterColon(message);
    return email ? `E-mail já cadastrado: ${email}` : 'E-mail já cadastrado.';
  },
  PASSWORD_MISMATCH: 'Senha e confirmação de senha não conferem.',
  USER_NOT_FOUND: 'Usuário não encontrado.',
  COURSE_NOT_FOUND: 'Curso não encontrado.',
  SUBJECT_NOT_FOUND: 'Disciplina não encontrada.',
  CLASS_GROUP_NOT_FOUND: 'Turma não encontrada.',
  ENROLLMENT_NOT_FOUND: 'Matrícula não encontrada.',
  COURSE_HAS_SUBJECTS: 'Não é possível excluir um curso com disciplinas associadas.',
  SUBJECT_HAS_CLASS_GROUPS: 'Não é possível excluir uma disciplina com turmas associadas.',
  VACANCY_LIMIT_BELOW_ENROLLED: (message) => {
    const count = message.match(/\((\d+)\)/)?.[1];
    return count
      ? `O limite de vagas não pode ser menor que o número de alunos matriculados (${count}).`
      : 'O limite de vagas não pode ser menor que o número de alunos matriculados.';
  },
  CLASS_GROUP_NOT_OPEN: 'A turma não está aberta para matrícula.',
  ENROLLMENT_ALREADY_EXISTS: 'O usuário já possui uma matrícula ativa nesta turma.',
  CLASS_GROUP_FULL: 'A turma não possui vagas disponíveis.',
  INVALID_ENROLLMENT_STATUS: (message) => {
    if (/already cancelled/i.test(message)) {
      return 'A matrícula já está cancelada.';
    }
    if (/Only pending/i.test(message)) {
      const status = message.match(/Current status:\s*(\w+)/i)?.[1];
      return status
        ? `Somente matrículas pendentes podem ser confirmadas. Status atual: ${status}.`
        : 'Somente matrículas pendentes podem ser confirmadas.';
    }
    return 'Status da matrícula inválido para esta operação.';
  },
};

const FIELD_LABELS: Record<string, string> = {
  name: 'nome',
  email: 'e-mail',
  password: 'senha',
  confirmPassword: 'confirmação de senha',
  description: 'descrição',
  active: 'status ativo',
  coursePublicId: 'curso',
  subjectPublicId: 'disciplina',
  userPublicId: 'usuário',
  classGroupPublicId: 'turma',
  vacancyLimit: 'limite de vagas',
  openForEnrollment: 'abertura para matrícula',
  role: 'papel',
};

export function extractApiError(error: unknown, fallback = 'Ocorreu um erro inesperado.'): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as ApiErrorResponse | string | null;
    if (body && typeof body === 'object') {
      if (Array.isArray(body.details) && body.details.length > 0) {
        const detailMessages = body.details
          .map((detail) => translateFieldError(detail.field, detail.message))
          .filter((message): message is string => !!message?.trim());
        if (detailMessages.length > 0) {
          return detailMessages.join(' ');
        }
      }
      if (body.code) {
        const translated = translateErrorCode(body.code, body.message ?? '');
        if (translated) {
          return translated;
        }
      }
      if (body.message) {
        return body.message;
      }
    }
    if (typeof body === 'string' && body.trim()) {
      return body;
    }
    if (error.status === 0) {
      return 'Não foi possível conectar ao servidor.';
    }
    if (error.status === 401) {
      return ERROR_CODE_MESSAGES['UNAUTHORIZED'] as string;
    }
    if (error.status === 403) {
      return ERROR_CODE_MESSAGES['ACCESS_DENIED'] as string;
    }
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export function translateErrorCode(code: string, message = ''): string | null {
  const entry = ERROR_CODE_MESSAGES[code];
  if (!entry) {
    return null;
  }
  return typeof entry === 'function' ? entry(message) : entry;
}

export function translateFieldError(field: string, message: string): string {
  const fieldKey = field.includes('.') ? (field.split('.').pop() ?? field) : field;
  const label = FIELD_LABELS[fieldKey] ?? fieldKey;

  if (/must not be blank|must not be null|must not be empty|is required/i.test(message)) {
    return `O campo ${label} é obrigatório.`;
  }
  if (/must be a well-formed email|must be a valid email/i.test(message)) {
    return 'Informe um e-mail válido.';
  }
  if (/size must be between/i.test(message)) {
    const range = message.match(/between\s+(\d+)\s+and\s+(\d+)/i);
    if (range) {
      return `O campo ${label} deve ter entre ${range[1]} e ${range[2]} caracteres.`;
    }
  }
  if (/size must be less than or equal to|length must be between 0 and/i.test(message)) {
    const max = message.match(/(?:equal to|and)\s+(\d+)/i)?.[1];
    if (max) {
      return `O campo ${label} deve ter no máximo ${max} caracteres.`;
    }
  }
  if (/must be greater than or equal to/i.test(message)) {
    const min = message.match(/equal to\s+(\d+)/i)?.[1];
    if (min) {
      return `O campo ${label} deve ser pelo menos ${min}.`;
    }
  }
  return message;
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

function extractAfterColon(message: string): string {
  const idx = message.indexOf(':');
  if (idx < 0) {
    return '';
  }
  return message.slice(idx + 1).trim();
}

function translateTypeMismatch(message: string): string {
  const match = message.match(/Parameter '([^']+)' must be of type (.+)/i);
  if (match) {
    return `O parâmetro '${match[1]}' deve ser do tipo ${match[2]}.`;
  }
  return 'Um ou mais parâmetros possuem tipo inválido.';
}

function translateMissingParameter(message: string): string {
  const match = message.match(/Required parameter '([^']+)' is missing/i);
  if (match) {
    return `O parâmetro obrigatório '${match[1]}' está ausente.`;
  }
  return 'Um parâmetro obrigatório está ausente.';
}
