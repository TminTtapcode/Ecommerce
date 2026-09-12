package com.tmt.ecommerce.notification;

import com.tmt.ecommerce.identity.entity.*;
import com.tmt.ecommerce.identity.repository.*;
import com.tmt.ecommerce.identity.security.CustomUserDetails;
import com.tmt.ecommerce.common.security.JwtService;
import com.tmt.ecommerce.notification.entity.*;
import com.tmt.ecommerce.notification.repository.NotificationRepository;
import com.tmt.ecommerce.notification.service.NotificationWriter;
import com.tmt.ecommerce.order.api.event.*;
import com.tmt.ecommerce.review.api.event.ReviewCreatedEvent;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import com.tmt.ecommerce.chat.entity.ChatRoom;
import com.tmt.ecommerce.chat.repository.ChatRoomRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotificationPushIntegrationTest {
    @org.springframework.boot.test.web.server.LocalServerPort int port;
    @Autowired UserRepository users; @Autowired RoleRepository roles; @Autowired ShopRepository shops;
    @Autowired ChatRoomRepository rooms; @Autowired JwtService jwt; @Autowired JdbcTemplate jdbc;
    @Autowired NotificationRepository notifications; @Autowired NotificationWriter writer;
    @Autowired PlatformTransactionManager manager; @Autowired ApplicationEventPublisher events;
    @Autowired SimpUserRegistry registry;
    @Autowired @Qualifier("brokerChannel") org.springframework.messaging.support.AbstractMessageChannel brokerChannel;
    @Value("${application.security.jwt.secret-key}") String secret;
    User buyer, vendor; Shop shop;
    List<Wire> wires = new ArrayList<>();
    @BeforeEach void setup() {
        assertTrue(jdbc.queryForObject("select database()", String.class).startsWith("ecommerce_s103_s104_s105"));
        buyer = user("ROLE_USER"); vendor = user("ROLE_VENDOR");
        shop = shops.save(Shop.builder().userId(vendor.getId()).name("push-" + UUID.randomUUID()).status(ShopStatus.BANNED).build());
    }
    @AfterEach void cleanup() { wires.forEach(Wire::close); }
    User user(String role) {
        var r = roles.findByName(role).orElseGet(() -> roles.save(Role.builder().name(role).build()));
        return users.save(User.builder().email("push-" + UUID.randomUUID() + "@example.test").fullName("Push user")
            .passwordHash("fixture").status("ACTIVE").roles(Set.of(r)).build());
    }
    String token(User u) { return jwt.generateToken(new CustomUserDetails(u)); }
    String special(Long id, long expiry) {
        var b = io.jsonwebtoken.Jwts.builder().subject(buyer.getEmail()).expiration(new Date(expiry));
        if (id != null) b.claim("userId", id);
        return b.signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secret))).compact();
    }
    void tx(Runnable action) { new TransactionTemplate(manager).executeWithoutResult(s -> action.run()); }
    Notification draft(Long userId) { return Notification.builder().userId(userId).type(NotificationType.ORDER_SHIPPED)
        .title("Push fixture").message("Message").referenceId(UUID.randomUUID().toString()).build(); }
    long create(Long id) { var n = draft(id); tx(() -> writer.create(n)); return n.getId(); }
    static void until(BooleanSupplier predicate) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!predicate.getAsBoolean()) { if (System.nanoTime() >= end) fail("Timed out awaiting server state"); Thread.sleep(10); }
    }
    class Wire implements WebSocket.Listener {
        final BlockingQueue<String> frames = new LinkedBlockingQueue<>();
        final StringBuilder buffer = new StringBuilder(); WebSocket socket;
        volatile boolean closed;
        Wire(String origin) throws Exception {
            var builder = HttpClient.newHttpClient().newWebSocketBuilder();
            if (origin != null) builder.header("Origin", origin);
            socket = builder.buildAsync(URI.create("ws://127.0.0.1:" + port + "/ws"), this).get(5, TimeUnit.SECONDS);
            wires.add(this);
        }
        public void onOpen(WebSocket ws) { ws.request(1); }
        public CompletionStage<?> onText(WebSocket ws, CharSequence value, boolean last) {
            buffer.append(value);
            int end;
            while ((end = buffer.indexOf("\0")) >= 0) { frames.add(buffer.substring(0, end).stripLeading()); buffer.delete(0, end + 1); }
            ws.request(1); return CompletableFuture.completedFuture(null);
        }
        public CompletionStage<?> onClose(WebSocket ws, int status, String reason) { closed = true; return CompletableFuture.completedFuture(null); }
        void send(String value) { socket.sendText(value + "\0", true).join(); }
        void connect(String bearer) { send("CONNECT\naccept-version:1.2\nhost:localhost\n" + (bearer == null ? "" : "Authorization:Bearer " + bearer + "\n") + "\n"); }
        String take(String command) throws Exception { String frame = frames.poll(5, TimeUnit.SECONDS); assertNotNull(frame, "Missing " + command); assertTrue(frame.startsWith(command + "\n"), frame); return frame; }
        void subscribe(String destination) throws Exception {
            String id = UUID.randomUUID().toString(); send("SUBSCRIBE\nid:" + id + "\ndestination:" + destination + "\n\n");
            until(() -> !registry.findSubscriptions(s -> id.equals(s.getId())).isEmpty());
        }
        void none() throws Exception { assertNull(frames.poll(200, TimeUnit.MILLISECONDS)); }
        void close() { if (socket != null) socket.abort(); }
    }
    Wire connected(User u) throws Exception { var w = new Wire(null); w.connect(token(u)); w.take("CONNECTED"); return w; }
    tools.jackson.databind.JsonNode body(String frame) { return tools.jackson.databind.json.JsonMapper.builder().build().readTree(frame.substring(frame.indexOf("\n\n") + 2)); }
    HttpResponse<String> http(String path, String bearer, String method) throws Exception {
        var r = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/notifications" + path));
        if (bearer != null) r.header("Authorization", "Bearer " + bearer);
        return HttpClient.newHttpClient().send(r.method(method, HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void committedNotificationRoutesToEveryRecipientSessionOnly() throws Exception {
        var a = connected(buyer); var a2 = connected(buyer); var b = connected(vendor);
        for (var w : List.of(a,a2,b)) w.subscribe("/user/queue/notifications");
        long id = create(buyer.getId());
        for (var w : List.of(a,a2)) { var payload = body(w.take("MESSAGE")); assertEquals(id, payload.get("id").asLong()); assertEquals(7, payload.size()); assertTrue(notifications.existsById(id)); }
        b.none();
    }
    @Test void noPushBeforeCommitAndRollbackNeverPushes() throws Exception {
        var a = connected(buyer); a.subscribe("/user/queue/notifications");
        var n = draft(buyer.getId());
        new TransactionTemplate(manager).executeWithoutResult(s -> { writer.create(n); assertTrue(a.frames.isEmpty()); s.setRollbackOnly(); });
        assertFalse(notifications.existsById(n.getId())); a.none();
        assertThrows(org.springframework.transaction.IllegalTransactionStateException.class, () -> writer.create(draft(buyer.getId())));
    }
    @Test void heldTransactionCannotDeliverUntilIndependentCommit() throws Exception {
        var a=connected(buyer); a.subscribe("/user/queue/notifications");
        var saved=new CountDownLatch(1); var release=new CountDownLatch(1); var pool=Executors.newSingleThreadExecutor();
        var n=draft(buyer.getId());
        try {
            var worker=pool.submit(() -> tx(() -> { writer.create(n); saved.countDown(); try { assertTrue(release.await(5,TimeUnit.SECONDS)); } catch(InterruptedException e) { throw new RuntimeException(e); } }));
            assertTrue(saved.await(5,TimeUnit.SECONDS)); a.none(); assertFalse(notifications.existsById(n.getId()));
            release.countDown(); worker.get(5,TimeUnit.SECONDS); assertEquals(n.getId().longValue(),body(a.take("MESSAGE")).get("id").asLong());
            assertTrue(notifications.existsById(n.getId()));
        } finally { release.countDown(); pool.shutdownNow(); }
    }
    @Test void failedBrokerSendLeavesCommittedNotificationRecoverableThroughHttp() throws Exception {
        var attempted=new java.util.concurrent.atomic.AtomicBoolean();
        var failure=new org.springframework.messaging.support.ChannelInterceptor() {
            @Override public org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message, org.springframework.messaging.MessageChannel channel) {
                String destination=org.springframework.messaging.simp.SimpMessageHeaderAccessor.getDestination(message.getHeaders());
                if (("/user/"+buyer.getId()+"/queue/notifications").equals(destination)) {
                    attempted.set(true); throw new org.springframework.messaging.MessageDeliveryException("Injected broker failure");
                }
                return message;
            }
        };
        brokerChannel.addInterceptor(0,failure);
        long id;
        try { id=create(buyer.getId()); } finally { brokerChannel.removeInterceptor(failure); }
        assertTrue(attempted.get()); assertTrue(notifications.existsById(id));
        assertTrue(http("",token(buyer),"GET").body().contains("\"id\":"+id));
    }
    @Test void actualResolvedQueueCannotBeSubscribedByAnotherUser() throws Exception {
        var a=connected(buyer); a.subscribe("/user/queue/notifications");
        var serverSession=registry.getUser(buyer.getId().toString()).getSessions().iterator().next().getId();
        var b=connected(vendor); b.send("SUBSCRIBE\nid:attack\ndestination:/queue/notifications-user"+serverSession+"\n\n");
        b.take("ERROR"); until(() -> b.closed); create(buyer.getId()); a.take("MESSAGE"); b.none();
    }
    @ParameterizedTest @ValueSource(strings={"BEGIN","COMMIT","ABORT","ACK","NACK"})
    void unusedTransactionAndAckFramesAreRejected(String command) throws Exception {
        var w=connected(buyer); w.send(command+"\ntransaction:x\nid:x\n\n"); w.take("ERROR"); until(() -> w.closed);
    }
    @Test void listUsesStableIdTieBreakerAndReadRollback() throws Exception {
        long first=create(buyer.getId()), second=create(buyer.getId());
        jdbc.update("update notifications set created_at='2026-01-01 00:00:00' where id in (?,?)",first,second);
        var json=tools.jackson.databind.json.JsonMapper.builder().build().readTree(http("?size=1",token(buyer),"GET").body());
        assertEquals(second,json.get("data").get("content").get(0).get("id").asLong());
        new TransactionTemplate(manager).executeWithoutResult(s -> { notifications.markAllAsReadByUserId(buyer.getId()); s.setRollbackOnly(); });
        assertFalse(notifications.findById(first).orElseThrow().isRead());
    }
    @Test void realDatabaseFailureRollsBackAllCreatedNotifications() throws Exception {
        var a = connected(buyer); a.subscribe("/user/queue/notifications"); var n = draft(buyer.getId());
        assertThrows(RuntimeException.class, () -> tx(() -> { writer.create(n); var bad = draft(buyer.getId()); bad.setTitle("x".repeat(256)); writer.create(bad); }));
        assertFalse(notifications.existsById(n.getId())); a.none();
    }
    @Test void producersAndDomainRollbackPreserveExistingSemantics() throws Exception {
        var a = connected(buyer); var b = connected(vendor); a.subscribe("/user/queue/notifications"); b.subscribe("/user/queue/notifications");
        long order = System.nanoTime();
        tx(() -> events.publishEvent(new OrderConfirmedEvent(order, buyer.getId(), shop.getId(), "COD")));
        assertEquals("ORDER_CONFIRMED", body(a.take("MESSAGE")).get("type").asText()); assertEquals("NEW_ORDER", body(b.take("MESSAGE")).get("type").asText());
        tx(() -> events.publishEvent(new OrderConfirmedEvent(order, buyer.getId(), shop.getId(), "COD"))); a.none(); b.none();
        tx(() -> events.publishEvent(new OrderShippedEvent(order, buyer.getId()))); assertEquals("ORDER_SHIPPED", body(a.take("MESSAGE")).get("type").asText());
        tx(() -> events.publishEvent(new OrderDeliveredEvent(order, buyer.getId()))); assertEquals("ORDER_DELIVERED", body(a.take("MESSAGE")).get("type").asText());
        tx(() -> events.publishEvent(new ReviewCreatedEvent(order, buyer.getId(), shop.getId(), 1L, 5))); assertEquals("NEW_REVIEW", body(b.take("MESSAGE")).get("type").asText());
        tx(() -> events.publishEvent(new OrderCancelledEvent(order, buyer.getId(), shop.getId()))); a.none(); b.none();
        long before = notifications.count();
        new TransactionTemplate(manager).executeWithoutResult(s -> { events.publishEvent(new OrderShippedEvent(order, buyer.getId())); s.setRollbackOnly(); });
        assertEquals(before, notifications.count()); a.none();
    }
    @ParameterizedTest @ValueSource(strings={"missing","malformed","expired","missing-id","wrong-id"})
    void invalidConnectIsErrorAndClosed(String kind) throws Exception {
        var w = new Wire(null);
        String bearer = switch(kind) { case "missing" -> null; case "malformed" -> "invalid"; case "expired" -> special(buyer.getId(), System.currentTimeMillis()-1000); case "missing-id" -> special(null,System.currentTimeMillis()+60000); default -> special(vendor.getId(),System.currentTimeMillis()+60000); };
        w.connect(bearer); assertTrue(w.take("ERROR").contains("UNAUTHORIZED")); until(() -> w.closed);
    }
    @ParameterizedTest @ValueSource(strings={"/queue/notifications","/queue/messages-userKnownSession","/topic/test","/user/1/queue/notifications","/user/queue/*","/user/queue/notifications/extra"})
    void subscriptionAllowlistRejectsRawAndForeignDestinations(String destination) throws Exception {
        var w = connected(buyer); w.send("SUBSCRIBE\nid:bad\ndestination:"+destination+"\n\n"); assertTrue(w.take("ERROR").contains("FORBIDDEN")); until(() -> w.closed);
    }
    @ParameterizedTest @ValueSource(strings={"/queue/notifications","/user/queue/notifications","/topic/test","/app/notifications","/user/1/queue/messages"})
    void sendCannotForgeNotificationOrBrokerDelivery(String destination) throws Exception {
        var w = connected(buyer); w.send("SEND\ndestination:"+destination+"\n\n{}"); w.take("ERROR"); until(() -> w.closed);
    }
    @Test void adminAlsoCannotSubscribeRawAndAnonymousCannotSubscribe() throws Exception {
        var admin = connected(user("ROLE_ADMIN")); admin.send("SUBSCRIBE\nid:bad\ndestination:/queue/notifications\n\n"); admin.take("ERROR");
        var anon = new Wire(null); anon.send("SUBSCRIBE\nid:bad\ndestination:/user/queue/notifications\n\n"); assertTrue(anon.take("ERROR").contains("UNAUTHORIZED"));
    }
    @Test void expiryClosesIdleSubscriberAndNewTokenReconnects() throws Exception {
        var w = new Wire(null); String shortToken = special(buyer.getId(),System.currentTimeMillis()+3000);
        w.connect(shortToken); w.take("CONNECTED"); w.subscribe("/user/queue/notifications");
        create(buyer.getId()); w.take("MESSAGE"); until(() -> w.closed);
        long missed = create(buyer.getId()); w.none();
        var expired = new Wire(null); expired.connect(shortToken); expired.take("ERROR");
        var fresh = connected(buyer); fresh.subscribe("/user/queue/notifications"); create(buyer.getId()); fresh.take("MESSAGE");
        assertTrue(http("",token(buyer),"GET").body().contains("\"id\":"+missed));
    }
    @Test void originAllowlistAndRepeatedConnect() throws Exception {
        var w = new Wire("http://localhost:5173"); w.connect(token(buyer)); w.take("CONNECTED"); w.connect(token(vendor)); w.take("ERROR");
        assertThrows(ExecutionException.class, () -> new Wire("https://untrusted.example"));
    }
    @Test void chatSendEchoAndRoomOwnershipStillWork() throws Exception {
        var a=connected(buyer); var b=connected(vendor); a.subscribe("/user/queue/messages"); b.subscribe("/user/queue/messages");
        var room = rooms.save(ChatRoom.builder().buyerId(buyer.getId()).shopId(shop.getId()).build());
        String payload = "{\"roomId\":"+room.getId()+",\"content\":\"hello\"}";
        a.send("SEND\ndestination:/app/chat.send\ncontent-type:application/json\n\n"+payload);
        assertEquals("hello",body(a.take("MESSAGE")).get("content").asText()); assertEquals(buyer.getId().longValue(),body(b.take("MESSAGE")).get("senderId").asLong());
        var stranger=connected(user("ROLE_USER")); stranger.send("SEND\ndestination:/app/chat.send\ncontent-type:application/json\n\n"+payload);
        a.none(); b.none(); assertEquals(1,jdbc.queryForObject("select count(*) from chat_messages where room_id=?",Integer.class,room.getId()));
    }
    @Test void restOwnershipPagingAndReadReconcile() throws Exception {
        long a=create(buyer.getId()), b=create(vendor.getId());
        assertEquals(401,http("",null,"GET").statusCode()); assertEquals(400,http("?size=101",token(buyer),"GET").statusCode());
        assertEquals(400,http("?page=-1",token(buyer),"GET").statusCode());
        var denied=http("/"+b+"/read",token(buyer),"PUT"); assertEquals(404,denied.statusCode()); assertTrue(denied.body().contains("NOT_FOUND"));
        assertEquals(200,http("/"+a+"/read",token(buyer),"PUT").statusCode());
        assertTrue(http("/unread-count",token(buyer),"GET").body().contains("\"unreadCount\":0"));
        create(buyer.getId()); assertEquals(200,http("/read-all",token(buyer),"PUT").statusCode()); assertTrue(http("/unread-count",token(buyer),"GET").body().contains("\"unreadCount\":0"));
        assertFalse(notifications.findById(b).orElseThrow().isRead());
    }
}
