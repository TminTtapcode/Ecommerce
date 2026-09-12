// Run against the isolated ecommerce_s108_smoke DB, backend :8080 and Edge CDP :9222.
// No production credentials or database are accepted by this harness.
import { createServer } from 'node:http';
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { resolve, extname, sep } from 'node:path';
import { execFileSync } from 'node:child_process';
import assert from 'node:assert/strict';
const dist = resolve('frontend/dist'), evidence = resolve('target/s1-07-08');
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
    const email = `smoke-${role}-${suffix}@example.com`;
    const data = await api('/auth/register', null, { email, password, fullName: `Smoke ${role}` });
    return { email, token: data.token };
  };
  const admin = await register('admin'), vendor = await register('vendor'), buyer = await register('buyer');
  const adminId = JSON.parse(Buffer.from(admin.token.split('.')[1], 'base64url')).userId;
  assert.ok(Number.isSafeInteger(adminId));
  const mysqlArgs = ['--protocol=TCP', '-h127.0.0.1', '-P33317', '-uroot', '--skip-password', '-N', 'ecommerce_s108_smoke', '-e'];
  const mysql = 'C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe';
  const mysqlOptions = { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] };
  assert.equal(execFileSync(mysql, [...mysqlArgs, 'SELECT DATABASE()'], mysqlOptions).trim(), 'ecommerce_s108_smoke');
  execFileSync(mysql, [...mysqlArgs, `INSERT IGNORE INTO roles(name) VALUES('ROLE_ADMIN'); INSERT INTO user_roles(user_id,role_id) SELECT ${adminId},id FROM roles WHERE name='ROLE_ADMIN'`], mysqlOptions);
  admin.token = (await api('/auth/login', null, { email: admin.email, password })).token;
  const shopName = `Smoke-${suffix}`;
  await api('/shops', vendor.token, { name: shopName, description: 'Sprint 1 browser smoke' });
  const shop = await api('/shops/my-shop', vendor.token);
  await api(`/admin/shops/${shop.id}/approve`, admin.token, null, 'PUT');
  vendor.token = (await api('/auth/login', null, { email: vendor.email, password })).token;
  const categories = await api('/categories');
  const product = await api('/vendor/products', vendor.token, { name: `Smoke product ${suffix}`, price: 100, stockQuantity: 10, categoryId: categories[0].id, variants: [{ sku: suffix, price: 100, stockQuantity: 10, attributes: {} }], images: [] });
  const add = () => api('/carts/items', buyer.token, { productVariantId: product.variants[0].id, quantity: 2 });
  await add();
  const checkout = await api('/orders/checkout', buyer.token, { shippingAddress: 'Smoke address', paymentMethod: 'COD' });
  await add();
  steps.push('Real API fixtures: register, approve, product, cart and COD checkout');
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
  const shopButton = label => `[...[...document.querySelectorAll('tr')].find(r=>r.textContent.includes(${JSON.stringify(shopName)})).querySelectorAll('button')].find(b=>b.textContent.trim()===${JSON.stringify(label)})`;
  await go(admin.token, '/admin/shop'); await click(shopButton('Khóa Shop'));
  await wait(async () => (await api('/shops/my-shop', vendor.token)).status === 'BANNED');
  await wait(() => evaluate(`!!(${shopButton('Mở Khóa')})`)); await screenshot('admin-banned');
  steps.push('Admin UI Ban persisted BANNED through real API');
  await go(vendor.token, `/vendor/products/${product.id}/edit`);
  await wait(() => evaluate('document.querySelector("fieldset")?.disabled === true')); await screenshot('vendor-readonly');
  steps.push('Banned vendor can read product detail; form disabled');
  await go(vendor.token, '/vendor/products/new');
  await wait(() => evaluate(`!!document.querySelector('a[href="/vendor/orders"]') && !document.querySelector('fieldset')`));
  steps.push('Banned vendor new product route blocked');
  await go(vendor.token, '/vendor/orders'); await click(button('Hủy đơn'));
  await wait(() => evaluate('!!document.querySelector("textarea")'));
  await evaluate(`(()=>{const t=document.querySelector('textarea');Object.getOwnPropertyDescriptor(HTMLTextAreaElement.prototype,'value').set.call(t,'Smoke cancellation');t.dispatchEvent(new Event('input',{bubbles:true}));})()`);
  await click(button('Xác nhận hủy đơn'));
  await wait(async () => (await api(`/orders/${checkout.orders[0].id}`, buyer.token)).status === 'CANCELLED');
  assert.equal((await api(`/vendor/products/${product.id}`, vendor.token)).stockQuantity, 10);
  steps.push('Banned vendor cancels existing order in UI; stock restored to 10');
  await go(buyer.token, '/cart');
  await wait(() => evaluate(`(${button('+')})?.disabled === true && (${button('-')})?.disabled === false`));
  await screenshot('cart-unavailable'); await click(button('-'));
  await wait(async () => (await api('/carts', buyer.token)).items[0].quantity === 1);
  steps.push('Cart plus disabled; real mouse decrement crosses overlay and persists');
  await go(admin.token, '/admin/shop'); await click(shopButton('Mở Khóa'));
  await wait(async () => (await api('/shops/my-shop', vendor.token)).status === 'ACTIVE');
  assert.equal((await api(`/products/${product.id}`)).id, product.id);
  await screenshot('admin-unbanned');
  steps.push('Admin UI Unban restores ACTIVE and public product');
  writeFileSync(`${evidence}/browser-smoke.json`, JSON.stringify({ status: 'PASS', browser: await send('Browser.getVersion'), steps }, null, 2));
  console.log(JSON.stringify({ status: 'PASS', steps }, null, 2));
} catch (e) { console.error(e); process.exitCode = 1; }
finally { ws?.close(); server.closeAllConnections(); await new Promise(r => server.close(r)); }
