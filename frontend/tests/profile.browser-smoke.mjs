// Run against the isolated ecommerce_p102_smoke DB, backend :8080 and Edge CDP :9222.
// No production credentials or database are accepted by this harness.
import { createServer } from 'node:http';
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { resolve, extname, sep } from 'node:path';
import assert from 'node:assert/strict';
const dist = resolve('frontend/dist'), evidence = resolve('target/p1-02');
mkdirSync(`${evidence}/screenshots`, { recursive: true });
const server = createServer((req, res) => {
  let path = resolve(dist, '.' + new URL(req.url, 'http://localhost').pathname);
  if (!path.startsWith(dist + sep)) path = `${dist}/index.html`;
  let body;
  try { body = readFileSync(path); } catch { path = `${dist}/index.html`; body = readFileSync(path); }
  res.setHeader('Content-Type', ({ '.js': 'text/javascript', '.css': 'text/css', '.html': 'text/html', '.svg': 'image/svg+xml' })[extname(path)] || 'application/octet-stream');
  res.end(body);
});
await new Promise(r => server.listen(5173, '127.0.0.1', r));
let ws;
const steps = [];
const wait = async fn => { for (let i = 0; i < 100; i++) { try { if (await fn()) return; } catch {} await new Promise(r => setTimeout(r, 200)); } throw Error('Timed out: ' + fn.toString()); };
const api = async (path, token, body, method = body ? 'POST' : 'GET') => {
  const r = await fetch('http://localhost:8080/api/v1' + path, { method, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) }, ...(body ? { body: JSON.stringify(body) } : {}) });
  const value = await r.json();
  assert.ok(r.ok, `${method} ${path}: ${r.status} ${JSON.stringify(value)}`);
  return value.data;
};
try {
  const suffix = crypto.randomUUID().slice(0, 8), password = 'smoke-password';
  const register = async role => {
    const email = `profile-${role}-${suffix}@example.com`;
    const data = await api('/auth/register', null, { email, password, fullName: `Original ${role}`, phone: '0123456789' });
    return { email, token: data.token };
  };
  const buyer = await register('buyer'), other = await register('other');
  const pages = await (await fetch('http://127.0.0.1:9222/json')).json();
  ws = new WebSocket(pages.find(p => p.type === 'page').webSocketDebuggerUrl);
  await new Promise(r => ws.addEventListener('open', r, { once: true }));
  let seq = 0; const pending = new Map();
  const send = (method, params = {}) => new Promise((resolve, reject) => { const id = ++seq; pending.set(id, { resolve, reject }); ws.send(JSON.stringify({ id, method, params })); });
  ws.addEventListener('message', e => {
    const m = JSON.parse(e.data);
    if (m.id) {
      const p = pending.get(m.id); pending.delete(m.id);
      if (m.error) p.reject(Error(JSON.stringify(m.error)));
      else p.resolve(m.result);
    }
    if (m.method === 'Page.javascriptDialogOpening') void send('Page.handleJavaScriptDialog', { accept: true, promptText: 'Browser smoke reason' });
  });
  const evaluate = async expression => { const r = await send('Runtime.evaluate', { expression, returnByValue: true, awaitPromise: true }); if (r.exceptionDetails) throw Error(JSON.stringify(r.exceptionDetails)); return r.result.value; };
  await send('Page.enable'); await send('Runtime.enable');
  await send('Emulation.setDeviceMetricsOverride', { width: 1440, height: 1000, deviceScaleFactor: 1, mobile: false });
  await send('Page.navigate', { url: 'http://localhost:5173' });
  await wait(() => evaluate('location.origin === "http://localhost:5173"'));
  const go = async (token, route) => { await evaluate(`localStorage.setItem('token', ${JSON.stringify(token)})`); await send('Page.navigate', { url: 'http://localhost:5173' + route }); };
  const button = label => `[...document.querySelectorAll('button')].find(b => b.textContent.trim() === ${JSON.stringify(label)})`;
  const click = async expression => { await wait(() => evaluate(`!!(${expression}) && !(${expression}).disabled`)); const rect = await evaluate(`(()=>{const r=(${expression}).getBoundingClientRect();return {x:r.x+r.width/2,y:r.y+r.height/2}})()`); await send('Input.dispatchMouseEvent', { type: 'mousePressed', ...rect, button: 'left', clickCount: 1 }); await send('Input.dispatchMouseEvent', { type: 'mouseReleased', ...rect, button: 'left', clickCount: 1 }); };
  const screenshot = async name => { const r = await send('Page.captureScreenshot'); writeFileSync(`${evidence}/screenshots/${name}.png`, Buffer.from(r.data, 'base64')); };
  const fill = async (selector, value, prototype) => evaluate(`(()=>{const t=document.querySelector(${JSON.stringify(selector)});Object.getOwnPropertyDescriptor(${prototype}.prototype,'value').set.call(t,${JSON.stringify(value)});t.dispatchEvent(new Event('${prototype === 'HTMLSelectElement' ? 'change' : 'input'}',{bubbles:true}));})()`);
  const route = path => evaluate(`history.pushState({},'',${JSON.stringify(path)});dispatchEvent(new PopStateEvent('popstate'))`);
  const ready = () => wait(() => evaluate('!!document.querySelector("input[name=fullName]")'));
  const logout = async () => {
    const rect = await evaluate(`(()=>{const r=(${button('Đăng xuất')}).closest('.group').getBoundingClientRect();return {x:r.x+r.width/2,y:r.y+r.height/2}})()`);
    await send('Input.dispatchMouseEvent', { type: 'mouseMoved', ...rect });
    await wait(() => evaluate(`(${button('Đăng xuất')}).getBoundingClientRect().width > 0 && getComputedStyle((${button('Đăng xuất')}).parentElement.parentElement).visibility === 'visible'`));
    await click(button('Đăng xuất')); await wait(() => evaluate('localStorage.getItem("token") === null'));
  };
  const login = async account => {
    await route('/login'); await wait(() => evaluate('!!document.querySelector("input[type=password]")'));
    await fill('input[type=email]', account.email, 'HTMLInputElement'); await fill('input[type=password]', password, 'HTMLInputElement');
    await click(button('Đăng nhập')); await wait(() => evaluate('!!localStorage.getItem("token") && location.pathname !== "/login"'));
    await route('/profile'); await ready();
  };
  await go(buyer.token, '/profile'); await ready();
  await fill('input[name=fullName]', '  Nguyễn  Thị Ánh  ', 'HTMLInputElement');
  await fill('input[name=phone]', ' +84 123 ', 'HTMLInputElement'); await click(button('Lưu hồ sơ'));
  await wait(() => evaluate(`document.querySelector('input[name=fullName]')?.value === 'Nguyễn  Thị Ánh' && document.body.innerText.includes('Đã cập nhật hồ sơ.')`));
  assert.equal(await evaluate('localStorage.getItem("token")'), buyer.token);
  assert.equal((await api('/users/me', buyer.token)).phone, '+84 123');
  await screenshot('profile-saved');
  await send('Page.reload'); await ready();
  await wait(() => evaluate(`document.querySelector('input[name=fullName]')?.value === 'Nguyễn  Thị Ánh'`));
  steps.push('Unicode save and normalized phone persist; reload with unchanged JWT shows server profile');
  await fill('input[name=phone]', '', 'HTMLInputElement'); await click(button('Lưu hồ sơ'));
  await wait(async () => (await api('/users/me', buyer.token)).phone === null);
  await fill('input[name=fullName]', '   ', 'HTMLInputElement');
  assert.equal(await evaluate(`(${button('Lưu hồ sơ')}).disabled`), true);
  await logout(); await login(buyer);
  assert.equal(await evaluate('document.querySelector("input[name=fullName]").value'), 'Nguyễn  Thị Ánh');
  steps.push('Clear phone, blank-name guard and actual logout/login work');
  let mode = '', held = null;
  const errorResponse = (requestId, status) => send('Fetch.fulfillRequest', { requestId, responseCode: status,
    responseHeaders: [{ name: 'Content-Type', value: 'application/json' }, { name: 'Access-Control-Allow-Origin', value: 'http://localhost:5173' }],
    body: Buffer.from(JSON.stringify({ success: false, status, errorCode: status === 401 ? 'UNAUTHORIZED' : 'INTERNAL_SERVER_ERROR', message: 'Smoke injected failure', data: null })).toString('base64') });
  ws.addEventListener('message', e => {
    const m = JSON.parse(e.data); if (m.method !== 'Fetch.requestPaused') return;
    const p = m.params;
    if (mode === 'fail' && p.request.method === 'GET') { void errorResponse(p.requestId, 500); }
    else if (mode === 'hold-put' && p.request.method === 'PUT') { mode = ''; held = p.requestId; }
    else void send('Fetch.continueRequest', { requestId: p.requestId });
  });
  await send('Fetch.enable', { patterns: [{ urlPattern: '*api/v1/users/me', requestStage: 'Response' }] });
  await route('/products'); await wait(() => evaluate('!document.querySelector("input[name=fullName]")'));
  mode = 'fail'; await route('/profile');
  await wait(() => evaluate(`!!(${button('Thử lại')})`)); assert.ok(await evaluate('localStorage.getItem("token")'));
  mode = ''; await click(button('Thử lại')); await ready();
  steps.push('Profile GET failure keeps session, disables editing and retry recovers');
  mode = 'hold-put'; await fill('input[name=fullName]', 'Delayed buyer save', 'HTMLInputElement'); await click(button('Lưu hồ sơ'));
  await wait(() => held !== null);
  await logout(); await login(other);
  const newToken = await evaluate('localStorage.getItem("token")');
  await errorResponse(held, 401); held = null;
  // Wait for the old promise/interceptor to settle, then inspect the new session.
  await new Promise(r => setTimeout(r, 500));
  assert.equal(await evaluate('localStorage.getItem("token")'), newToken);
  assert.equal(await evaluate('document.querySelector("input[name=fullName]").value'), 'Original other');
  steps.push('Delayed old-session PUT/401 cannot log out or overwrite newly logged-in account');
  mode = 'hold-put'; await fill('input[name=fullName]', 'Delayed same-user save', 'HTMLInputElement'); await click(button('Lưu hồ sơ'));
  await wait(() => held !== null); await logout(); await login(other);
  const sameUserNewToken = await evaluate('localStorage.getItem("token")');
  await errorResponse(held, 401); held = null; await new Promise(r => setTimeout(r, 500));
  assert.equal(await evaluate('localStorage.getItem("token")'), sameUserNewToken);
  assert.equal(await evaluate('document.querySelector("input[name=fullName]").value'), 'Delayed same-user save');
  steps.push('Late response also ignored after same-user re-login; committed DB save remains visible');
  mode = 'hold-put'; await fill('input[name=fullName]', 'Old session successful save', 'HTMLInputElement'); await click(button('Lưu hồ sơ'));
  await wait(() => held !== null); await logout(); await login(buyer);
  const buyerSession = await evaluate('localStorage.getItem("token")');
  await send('Fetch.continueRequest', { requestId: held }); held = null;
  await new Promise(r => setTimeout(r, 500));
  assert.equal(await evaluate('localStorage.getItem("token")'), buyerSession);
  assert.equal(await evaluate('document.querySelector("input[name=fullName]").value'), (await api('/users/me', buyerSession)).fullName);
  assert.equal(await evaluate("document.body.innerText.includes('Old session successful save')"), false);
  steps.push('Successful PUT response from old account cannot overwrite new account profile');
  await send('Fetch.disable'); await screenshot('profile-session-safe');
  writeFileSync(`${evidence}/browser-smoke.json`, JSON.stringify({ status: 'PASS', browser: await send('Browser.getVersion'), steps }, null, 2));
  console.log(JSON.stringify({ status: 'PASS', steps }, null, 2));
} catch (e) { console.error(e); process.exitCode = 1; }
finally { ws?.close(); server.closeAllConnections(); await new Promise(r => server.close(r)); }
