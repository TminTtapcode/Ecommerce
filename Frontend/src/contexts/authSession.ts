export interface UserPayload { userId: number; fullName?: string; sub: string; roles: string[] }
export function decodeUser(token: string | null): UserPayload | null {
  try {
    if (!token || token.split('.').length !== 3) return null;
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const value = JSON.parse(new TextDecoder('utf-8', { fatal: true }).decode(Uint8Array.from(atob(payload), c => c.charCodeAt(0))));
    if (!value || !Number.isSafeInteger(value.userId) || value.userId <= 0 || typeof value.sub !== 'string') return null;
    return { userId: value.userId, sub: value.sub, fullName: typeof value.fullName === 'string' ? value.fullName : undefined,
      roles: Array.isArray(value.roles) ? value.roles.filter((role: unknown) => typeof role === 'string') : [] };
  } catch { return null; }
}

export function createSessionGuard() {
  let generation = 0;
  return { advance: () => ++generation, capture: () => generation, accepts: (captured: number) => captured === generation };
}
export const authSessionGuard = createSessionGuard();
