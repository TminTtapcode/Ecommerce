import { test } from 'node:test';
import assert from 'node:assert/strict';
import { decodeUser, createSessionGuard } from '../src/contexts/authSession.ts';
const token = payload => `header.${Buffer.from(JSON.stringify(payload)).toString('base64url')}.signature`;
test('decodes Vietnamese UTF-8 display name without modifying roles', () => {
  assert.deepEqual(decodeUser(token({ userId: 12, sub: 'a@test', fullName: 'Nguyễn Thị Ánh', roles: ['ROLE_USER'] })),
    { userId: 12, sub: 'a@test', fullName: 'Nguyễn Thị Ánh', roles: ['ROLE_USER'] });
});
test('rejects malformed tokens and missing identity', () => {
  for (const value of [null, '', 'bad', 'a.???.c', token({ roles: ['ROLE_ADMIN'] }), token({ userId: -1, sub: 'a' })]) assert.equal(decodeUser(value), null);
});
test('old responses are invalid after logout or same-user re-login', () => {
  const guard = createSessionGuard(); const before = guard.capture();
  guard.advance(); assert.equal(guard.accepts(before), false);
  const loggedOut = guard.capture(); guard.advance(); assert.equal(guard.accepts(loggedOut), false);
  assert.equal(guard.accepts(guard.capture()), true);
});
