import test from 'node:test';
import assert from 'node:assert/strict';
import vm from 'node:vm';
import ts from 'typescript';
import { readFileSync } from 'node:fs';

// Execute the actual transport with a controllable STOMP client; no network or timing sleeps.
function harness() {
  let generation=0, stored=''; const clients=[], timers=[];
  class Client {
    constructor(options) { this.options=options; this.connected=false; this.active=false; this.subscriptions=new Map(); clients.push(this); }
    activate() { this.active=true; }
    async deactivate() { this.active=false; this.connected=false; }
    subscribe(destination, cb) { this.subscriptions.set(destination,cb); return { unsubscribe() {} }; }
    publish(frame) { this.sent=frame; }
    open() { this.connected=true; this.options.onConnect(); }
    push(destination, value) { this.subscriptions.get(destination)?.({ body:JSON.stringify(value) }); }
  }
  const exports={};
  const source=readFileSync(new URL('../src/services/websocket.ts',import.meta.url),'utf8').replaceAll('import.meta.env','({})');
  const js=ts.transpileModule(source,{ compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2022} }).outputText;
  const context={exports,require: name => name==='@stomp/stompjs' ? {Client} : {authSessionGuard:{ accepts:g=>g===generation }},
    localStorage:{getItem:()=>stored},atob,Date,console,setTimeout:cb=>{timers.push(cb);return timers.length;},clearTimeout(){} };
  vm.runInNewContext(js,context);
  const token = expiry => `x.${Buffer.from(JSON.stringify({exp:expiry})).toString('base64url')}.x`;
  return { service:exports.websocketService, clients, timers, token,
    session: (value=token(Math.floor(Date.now()/1000)+3600)) => { stored=value; return {token:value,generation:++generation}; },
    flush: async()=>{for(let i=0;i<10;i++) await Promise.resolve();} };
}
test('one connection has independent chat and notification subscriptions, no credential debug logger',async()=>{
  const h=harness(); h.service.setSession(h.session()); await h.flush(); const c=h.clients[0]; c.open();
  assert.equal(c.subscriptions.size,2); assert.equal(c.options.debug,undefined);
  let chats=0, notifications=0; const remove=h.service.subscribeNotifications(()=>notifications++); h.service.subscribe(()=>chats++);
  c.push('/user/queue/notifications',{id:1}); remove(); c.push('/user/queue/notifications',{id:2}); c.push('/user/queue/messages',{id:3});
  assert.equal(notifications,1);assert.equal(chats,1);assert.equal(c.active,true);
});
test('old callbacks and close cannot affect new account connection',async()=>{
  const h=harness(); h.service.setSession(h.session()); await h.flush(); const old=h.clients[0]; old.open();
  let received=0; h.service.subscribeNotifications(()=>received++);
  h.service.setSession(h.session()); await h.flush(); const current=h.clients[1]; current.open();
  old.push('/user/queue/notifications',{id:1});old.options.onWebSocketClose();
  assert.equal(received,0);assert.equal(h.service.isConnected(),true);assert.equal(old.active,false);
  current.push('/user/queue/notifications',{id:2});assert.equal(received,1);
});
test('same token re-login still replaces connection and logout ignores late messages',async()=>{
  const h=harness();const s=h.session();h.service.setSession(s);await h.flush();const old=h.clients[0];old.open();
  h.service.setSession(h.session(s.token));await h.flush();assert.equal(h.clients.length,2);
  let received=0;h.service.subscribe(()=>received++);h.service.setSession(null);await h.flush();old.push('/user/queue/messages',{id:1});
  assert.equal(received,0);assert.equal(h.service.isConnected(),false);
});
test('reconnect subscribes both destinations before connected status',async()=>{
  const h=harness();h.service.setSession(h.session());await h.flush();const c=h.clients[0];
  h.service.onStatusChange(connected=>{if(connected)assert.equal(c.subscriptions.size,2);});c.open();
  c.options.onWebSocketClose();c.subscriptions.clear();c.open();assert.equal(h.service.isConnected(),true);
});
test('expired or malformed token does not connect; expiry stops current session',async()=>{
  const h=harness();h.service.setSession(h.session(h.token(1)));await h.flush();assert.equal(h.clients.length,0);
  h.service.setSession(h.session('bad'));await h.flush();assert.equal(h.clients.length,0);
  h.service.setSession(h.session());await h.flush();h.clients[0].open();h.timers.at(-1)();await h.flush();assert.equal(h.clients[0].active,false);
});
test('queued account changes only activate latest session',async()=>{
  const h=harness();h.service.setSession(h.session());h.service.setSession(h.session());h.service.setSession(h.session());await h.flush();
  assert.equal(h.clients.length,1);
});
