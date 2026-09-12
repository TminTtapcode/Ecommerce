import { test } from 'node:test';
import assert from 'node:assert/strict';
import { AxiosError } from 'axios';
import { getApiError } from '../src/api/apiError.ts';

const error = (data, status = 400) => new AxiosError('HTTP failure', undefined, undefined, undefined, { status, data });

test('uses symbolic code independently of translated message', () => {
  assert.equal(getApiError(error({ errorCode: 'SHOP_NOT_FOUND', message: 'Changed translation', data: null }, 404)).errorCode, 'SHOP_NOT_FOUND');
});
test('reads fieldErrors with null data', () => {
  assert.deepEqual(getApiError(error({ fieldErrors: { name: 'Required' }, data: null })).fieldErrors, { name: 'Required' });
});
test('accepts legacy field validation data during deployment', () => {
  assert.deepEqual(getApiError(error({ data: { name: 'Required' } })).fieldErrors, { name: 'Required' });
});
test('unknown codes keep server message and status', () => {
  assert.deepEqual(getApiError(error({ errorCode: 'FUTURE_CODE', message: 'Try later' }, 409)), { status: 409, errorCode: 'FUTURE_CODE', message: 'Try later', fieldErrors: undefined });
});
test('handles network and non-JSON errors without throwing', () => {
  assert.equal(getApiError(new Error('internal'), 'Fallback').message, 'Fallback');
  assert.equal(getApiError(error('<html>failure</html>', 502), 'Fallback').message, 'Fallback');
});
test('ignores non-string validation details', () => {
  assert.equal(getApiError(error({ fieldErrors: { count: 7 } })).fieldErrors, undefined);
});
