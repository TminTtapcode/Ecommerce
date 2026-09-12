import axios from 'axios';

export interface ApiErrorDetails {
  status?: number;
  errorCode?: string;
  message: string;
  fieldErrors?: Record<string, string>;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

export function getApiError(error: unknown, fallback = 'Không thể thực hiện yêu cầu.'): ApiErrorDetails {
  if (!axios.isAxiosError(error)) return { message: fallback };
  const body: unknown = error.response?.data;
  if (!isRecord(body)) return { status: error.response?.status, message: fallback };

  const fields = body.fieldErrors ?? (error.response?.status === 400 ? body.data : undefined);
  const fieldErrors = isRecord(fields)
    ? Object.fromEntries(Object.entries(fields).filter((entry): entry is [string, string] => typeof entry[1] === 'string'))
    : undefined;
  return {
    status: error.response?.status,
    errorCode: typeof body.errorCode === 'string' ? body.errorCode : undefined,
    message: typeof body.message === 'string' && body.message ? body.message : fallback,
    fieldErrors: fieldErrors && Object.keys(fieldErrors).length > 0 ? fieldErrors : undefined,
  };
}
