// Built frontend + JAR + isolated ecommerce_p103_smoke only. Edge CDP :9222.
import { createServer } from 'node:http';
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { resolve, extname, sep } from 'node:path';
import { execFileSync } from 'node:child_process';
import assert from 'node:assert/strict';
const dist=resolve('frontend/dist'), evidence=resolve('target/p1-03');
mkdirSync(`${evidence}/screenshots`,{recursive:true});
const server=createServer((req,res)=>{
  let path=resolve(dist,'.'+new URL(req.url,'http://localhost').pathname);
  if(!path.startsWith(dist+sep))path=`${dist}/index.html`;
  let data;try{data=readFileSync(path);}catch{path=`${dist}/index.html`;data=readFileSync(path);}
  res.setHeader('Content-Type',({'.js':'text/javascript','.css':'text/css','.html':'text/html','.svg':'image/svg+xml'})[extname(path)]||'application/octet-stream');res.end(data);
});
await new Promise(r=>server.listen(5173,'127.0.0.1',r));
const wait=async fn=>{for(let i=0;i<120;i++){try{if(await fn())return;}catch{}await new Promise(r=>setTimeout(r,100));}throw Error('Timed out: '+fn.toString());};
const api=async(path,token,body,method=body?'POST':'GET')=>{
  const r=await fetch('http://localhost:8080/api/v1'+path,{method,headers:{'Content-Type':'application/json',...(token?{Authorization:`Bearer ${token}`}:{})},...(body?{body:JSON.stringify(body)}:{})});
  const value=await r.json();assert.ok(r.ok,`${method} ${path}: ${r.status}`);return value.data;
};
class CDP {
  pending=new Map();seq=0;events=[];
  static async connect(url){const c=new CDP();c.ws=new WebSocket(url);await new Promise(r=>c.ws.addEventListener('open',r,{once:true}));
    c.ws.addEventListener('message',e=>{const m=JSON.parse(e.data);if(m.id){const p=c.pending.get(m.id);c.pending.delete(m.id);if(m.error)p.reject(Error(JSON.stringify(m.error)));else p.resolve(m.result);}else c.events.forEach(fn=>fn(m));});return c;}
  send(method,params={}){return new Promise((resolve,reject)=>{const id=++this.seq;this.pending.set(id,{resolve,reject});this.ws.send(JSON.stringify({id,method,params}));});}
  async eval(expression){const r=await this.send('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});if(r.exceptionDetails)throw Error(JSON.stringify(r.exceptionDetails));return r.result.value;}
  async click(expression){await wait(()=>this.eval(`!!(${expression}) && !(${expression}).disabled`));const pos=await this.eval(`(()=>{const r=(${expression}).getBoundingClientRect();return {x:r.x+r.width/2,y:r.y+r.height/2}})()`);await this.send('Input.dispatchMouseEvent',{type:'mousePressed',...pos,button:'left',clickCount:1});await this.send('Input.dispatchMouseEvent',{type:'mouseReleased',...pos,button:'left',clickCount:1});}
  async fill(selector,value){return this.eval(`(()=>{const e=document.querySelector(${JSON.stringify(selector)});Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set.call(e,${JSON.stringify(value)});e.dispatchEvent(new Event('input',{bubbles:true}));})()`);}
  route(path){return this.eval(`history.pushState({},'',${JSON.stringify(path)});dispatchEvent(new PopStateEvent('popstate'))`);}
  async screenshot(name){const r=await this.send('Page.captureScreenshot');writeFileSync(`${evidence}/screenshots/${name}.png`,Buffer.from(r.data,'base64'));}
}
const button=label=>`[...document.querySelectorAll('button')].find(b=>b.textContent.trim()===${JSON.stringify(label)})`;
const bell=`document.querySelector('button[title="Thông báo"]')`;
const badge=c=>c.eval(`Number((${bell})?.querySelector('span')?.textContent || 0)`);
const clients=[],contexts=[],steps=[];let browser;
try{
  const browserInfo=await(await fetch('http://127.0.0.1:9222/json/version')).json();browser=await CDP.connect(browserInfo.webSocketDebuggerUrl);
  const suffix=crypto.randomUUID().slice(0,8),password='smoke-password';
  const register=async role=>{const email=`push-${role}-${suffix}@example.test`;const data=await api('/auth/register',null,{email,password,fullName:`Push ${role}`});return{email,token:data.token};};
  const admin=await register('admin'),vendor=await register('vendor'),buyer=await register('buyer');
  const adminId=JSON.parse(Buffer.from(admin.token.split('.')[1],'base64url')).userId;assert.ok(Number.isSafeInteger(adminId));
  const mysql='C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe';
  const args=['--protocol=TCP','-h127.0.0.1','-P33317','-uroot','--skip-password','-N','ecommerce_p103_smoke','-e'];
  const opts={encoding:'utf8',stdio:['ignore','pipe','pipe']};
  assert.equal(execFileSync(mysql,[...args,'SELECT DATABASE()'],opts).trim(),'ecommerce_p103_smoke');
  execFileSync(mysql,[...args,`INSERT IGNORE INTO roles(name) VALUES('ROLE_ADMIN'); INSERT INTO user_roles(user_id,role_id) SELECT ${adminId},id FROM roles WHERE name='ROLE_ADMIN'`],opts);
  admin.token=(await api('/auth/login',null,{email:admin.email,password})).token;
  await api('/shops',vendor.token,{name:`Push ${suffix}`,description:'F1 smoke'});const shop=await api('/shops/my-shop',vendor.token);
  await api(`/admin/shops/${shop.id}/approve`,admin.token,null,'PUT');vendor.token=(await api('/auth/login',null,{email:vendor.email,password})).token;
  assert.equal((await api('/notifications/unread-count',vendor.token)).unreadCount,0);
  const categories=await api('/categories');
  const product=await api('/vendor/products',vendor.token,{name:`Push product ${suffix}`,price:100,stockQuantity:30,categoryId:categories[0].id,variants:[{sku:suffix,price:100,stockQuantity:30,attributes:{}}],images:[]});
  const room=await api('/chat/rooms',buyer.token,{shopId:shop.id});
  async function page(account){
    const {browserContextId}=await browser.send('Target.createBrowserContext');contexts.push(browserContextId);
    const {targetId}=await browser.send('Target.createTarget',{url:'about:blank',browserContextId});
    const pages=await(await fetch('http://127.0.0.1:9222/json')).json();const c=await CDP.connect(pages.find(p=>p.id===targetId).webSocketDebuggerUrl);clients.push(c);
    await c.send('Page.enable');await c.send('Runtime.enable');await c.send('Network.enable');
    await c.send('Emulation.setDeviceMetricsOverride',{width:1440,height:1000,deviceScaleFactor:1,mobile:false});
    // Observe/inject transport faults without invoking React handlers or storing CONNECT headers.
    await c.send('Page.addScriptToEvaluateOnNewDocument',{source:`window.__sockets=[];window.__received=[];window.__poll=[];const W=window.WebSocket;window.WebSocket=class extends W{constructor(...a){if(window.__blockSocket)throw Error('Smoke blocked reconnect');super(...a);window.__sockets.push(this);this.addEventListener('message',e=>{if(typeof e.data==='string'&&e.data.startsWith('MESSAGE'))window.__received.push(e.data);});}};const I=window.setInterval;window.setInterval=(fn,ms,...a)=>{if(ms===60000)window.__poll.push(fn);return I(fn,ms,...a);};`});
    await c.send('Page.navigate',{url:'http://127.0.0.1:5173/login'});await wait(()=>c.eval('location.origin==="http://127.0.0.1:5173"'));
    await c.eval(`localStorage.setItem('token',${JSON.stringify(account.token)})`);await c.send('Page.navigate',{url:'http://127.0.0.1:5173/chat-test'});
    await wait(()=>c.eval('document.body.innerText.includes("STOMP: Connected")'));await wait(()=>c.eval(`!!(${bell})`));return c;
  }
  const a=await page(buyer), b=await page(vendor);
  const checkout=async()=>{await api('/carts/items',buyer.token,{productVariantId:product.variants[0].id,quantity:1});return(await api('/orders/checkout',buyer.token,{shippingAddress:'F1 smoke address',paymentMethod:'COD'})).orders[0];};
  const order=await checkout();
  await wait(async()=>await badge(a)===1&&await badge(b)===1);
  assert.equal(await a.eval('new Set(__sockets.filter(s=>s.readyState===1)).size'),1);assert.equal(await b.eval('new Set(__sockets.filter(s=>s.readyState===1)).size'),1);
  await a.click(bell);await b.click(bell);
  await wait(()=>a.eval(`document.body.innerText.includes('Đơn hàng đã được xác nhận')`));await wait(()=>b.eval(`document.body.innerText.includes('Bạn có đơn hàng mới')`));
  assert.equal(await a.eval(`document.body.innerText.includes('Bạn có đơn hàng mới')`),false);
  await a.screenshot('notification-buyer');await b.screenshot('notification-vendor');
  steps.push('Real checkout pushes correct buyer/vendor notifications through one socket per browser session; approval created no notification');
  await a.eval(`(()=>{const f=__received.find(f=>f.includes('ORDER_CONFIRMED'));if(!f)throw Error('No notification frame');__sockets.find(s=>s.readyState===1).dispatchEvent(new MessageEvent('message',{data:f}));})()`);
  await new Promise(r=>setTimeout(r,300));assert.equal(await badge(a),1);
  await a.click(button('Đánh dấu đã đọc tất cả'));await wait(async()=>await badge(a)===0);assert.equal((await api('/notifications/unread-count',buyer.token)).unreadCount,0);
  await a.click(bell);await b.click(bell);
  const roomSelector=`[...document.querySelectorAll('span')].find(e=>e.textContent.trim()===${JSON.stringify(`Room #${room.id} (Shop #${shop.id})`)})?.closest('div.cursor-pointer')`;
  for(const c of [a,b])await c.click(roomSelector);
  await a.fill('input[placeholder="Nhập tin nhắn test STOMP..."]','F1 hello buyer');await a.click(button('Gửi'));
  await wait(()=>b.eval(`document.body.innerText.includes('F1 hello buyer')`));
  await b.fill('input[placeholder="Nhập tin nhắn test STOMP..."]','F1 hello vendor');await b.click(button('Gửi'));
  await wait(()=>a.eval(`document.body.innerText.includes('F1 hello vendor')`));
  steps.push('Duplicate notification frame does not increment badge; read-all reconciles; Chat sends and receives both directions while sharing transport');
  // Disconnect WS only; HTTP remains available. Invoke the installed 60-second polling callback as a deterministic timer tick.
  await a.eval('__blockSocket=true;__sockets.forEach(s=>s.close())');await wait(()=>a.eval('document.body.innerText.includes("STOMP: Disconnected")'));
  await api(`/vendor/orders/${order.id}/status`,vendor.token,{status:'SHIPPED'},'PATCH');
  await a.eval('__poll.forEach(fn=>fn())');await wait(async()=>await badge(a)===1);
  await a.eval('__blockSocket=false');await wait(()=>a.eval('document.body.innerText.includes("STOMP: Connected")'));
  await a.click(bell);await wait(()=>a.eval(`document.body.innerText.includes('Đơn hàng đang được giao')`));
  steps.push('WS-only outage uses HTTP polling fallback; reconnect resubscribes and reconciles persisted missed notification');
  // Hold an old GET while a new push + read-all occur. Response revision must not win over newer mutations.
  let hold=false, held=null, fail=false;
  a.events.push(m=>{if(m.method!=='Fetch.requestPaused')return;const p=m.params;
    if(fail){void a.send('Fetch.fulfillRequest',{requestId:p.requestId,responseCode:500,responseHeaders:[{name:'Content-Type',value:'application/json'},{name:'Access-Control-Allow-Origin',value:'http://127.0.0.1:5173'}],body:Buffer.from(JSON.stringify({success:false,status:500,errorCode:'INTERNAL_SERVER_ERROR',data:null})).toString('base64')});}
    else if(hold&&p.request.url.includes('unread-count')){hold=false;held=p.requestId;}else void a.send('Fetch.continueRequest',{requestId:p.requestId});});
  await a.send('Fetch.enable',{patterns:[{urlPattern:'*api/v1/notifications*',requestStage:'Response'}]});
  hold=true;await a.eval('window.dispatchEvent(new Event("focus"))');await wait(()=>held!==null);
  await api(`/vendor/orders/${order.id}/status`,vendor.token,{status:'DELIVERED'},'PATCH');
  await api('/notifications/read-all',buyer.token,null,'PUT');await a.eval('window.dispatchEvent(new Event("focus"))');
  await a.send('Fetch.continueRequest',{requestId:held});held=null;await wait(async()=>await badge(a)===0);
  fail=true;await a.eval('window.dispatchEvent(new Event("focus"))');await wait(()=>a.eval('!!document.querySelector("[role=alert]")'));
  assert.ok(await a.eval('localStorage.getItem("token")'));fail=false;await a.click(button('Retry'));await wait(()=>a.eval('!document.querySelector("[role=alert]")'));
  steps.push('Delayed HTTP count cannot overwrite push/read-all reconciliation; HTTP failure keeps session and retry recovers');
  // An old HTTP 401 arriving after an actual logout/login cannot clear the new session.
  hold=true;await a.eval('window.dispatchEvent(new Event("focus"))');await wait(()=>held!==null);
  await a.click(bell);
  const logout=button('Đăng xuất');const pos=await a.eval(`(()=>{const r=(${logout}).closest('.group').getBoundingClientRect();return{x:r.x+r.width/2,y:r.y+r.height/2}})()`);
  await a.send('Input.dispatchMouseEvent',{type:'mouseMoved',...pos});await wait(()=>a.eval(`(${logout}).getBoundingClientRect().width>0 && getComputedStyle((${logout}).parentElement.parentElement).visibility==='visible'`));await new Promise(r=>setTimeout(r,250));await a.click(logout);await wait(()=>a.eval('localStorage.getItem("token")===null'));
  await a.route('/login');await wait(()=>a.eval('!!document.querySelector("input[type=password]")'));
  await a.fill('input[type=email]',vendor.email);await a.fill('input[type=password]',password);await a.click(button('Đăng nhập'));await wait(()=>a.eval('!!localStorage.getItem("token")&&location.pathname!=="/login"'));
  await a.send('Fetch.fulfillRequest',{requestId:held,responseCode:401,responseHeaders:[{name:'Content-Type',value:'application/json'},{name:'Access-Control-Allow-Origin',value:'http://127.0.0.1:5173'}],body:Buffer.from(JSON.stringify({success:false,status:401,errorCode:'UNAUTHORIZED',data:null})).toString('base64')});held=null;
  await new Promise(r=>setTimeout(r,300));assert.ok(await a.eval('localStorage.getItem("token")'));
  await a.route('/chat-test');await wait(()=>a.eval('document.body.innerText.includes("STOMP: Connected")'));
  assert.equal(await a.eval('new Set(__sockets.filter(s=>s.readyState===1)).size'),1);
  await a.click(bell);await wait(()=>a.eval(`document.body.innerText.includes('Bạn có đơn hàng mới')`));
  assert.equal(await a.eval(`document.body.innerText.includes('Đơn hàng giao thành công')`),false);
  steps.push('Actual account switch closes old socket, clears notification/chat state and ignores delayed old HTTP 401');
  await b.click(bell);await b.click(button('Đánh dấu đã đọc tất cả'));await wait(async()=>await badge(b)===0);
  await a.eval('window.dispatchEvent(new Event("focus"))');await wait(async()=>await badge(a)===0);
  steps.push('Two sessions of the same recipient converge read state when the other session returns to foreground');
  const {targetId:tabId}=await browser.send('Target.createTarget',{url:'about:blank',browserContextId:contexts[1]});
  const tabPages=await(await fetch('http://127.0.0.1:9222/json')).json();const tab=await CDP.connect(tabPages.find(p=>p.id===tabId).webSocketDebuggerUrl);clients.push(tab);
  await tab.send('Page.enable');await tab.send('Runtime.enable');await tab.send('Network.enable');
  let tabSocketClosed=false;tab.events.push(m=>{if(m.method==='Network.webSocketClosed')tabSocketClosed=true;});
  await tab.send('Page.navigate',{url:'http://127.0.0.1:5173/chat-test'});await wait(()=>tab.eval('document.body.innerText.includes("STOMP: Connected")'));
  await b.click(bell);
  const bPos=await b.eval(`(()=>{const r=(${logout}).closest('.group').getBoundingClientRect();return{x:r.x+r.width/2,y:r.y+r.height/2}})()`);
  await b.send('Input.dispatchMouseEvent',{type:'mouseMoved',...bPos});await wait(()=>b.eval(`getComputedStyle((${logout}).parentElement.parentElement).visibility==='visible'`));await new Promise(r=>setTimeout(r,250));await b.click(logout);
  await wait(()=>tab.eval('localStorage.getItem("token")===null&&location.pathname==="/login"'));await wait(()=>tabSocketClosed);
  steps.push('Logout propagates through a real same-origin storage event to another tab, closes its socket and clears the protected Chat view');
  await a.send('Fetch.disable');await a.screenshot('notification-session-safe');
  writeFileSync(`${evidence}/browser-smoke.json`,JSON.stringify({status:'PASS',browser:browserInfo.Browser,steps},null,2));console.log(JSON.stringify({status:'PASS',steps},null,2));
}catch(e){console.error(e);process.exitCode=1;}
finally{for(const c of clients)c.ws.close();if(browser){for(const browserContextId of contexts)await browser.send('Target.disposeBrowserContext',{browserContextId});browser.ws.close();}server.closeAllConnections();await new Promise(r=>server.close(r));}
