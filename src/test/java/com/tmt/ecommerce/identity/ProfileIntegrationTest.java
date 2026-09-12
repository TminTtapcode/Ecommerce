package com.tmt.ecommerce.identity;

import com.tmt.ecommerce.identity.entity.*;
import com.tmt.ecommerce.identity.repository.*;
import com.tmt.ecommerce.identity.security.CustomUserDetails;
import com.tmt.ecommerce.identity.service.ProfileService;
import com.tmt.ecommerce.identity.dto.ProfileUpdateRequest;
import com.tmt.ecommerce.common.security.JwtService;
import com.tmt.ecommerce.common.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProfileIntegrationTest {
    @org.springframework.boot.test.web.server.LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired ProfileService profiles;
    @Autowired JwtService jwt;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager manager;
    User user, other;
    String token;
    @BeforeEach void setup() {
        assertTrue(jdbc.queryForObject("select database()", String.class).startsWith("ecommerce_s103_s104_s105"));
        user = fixture("ROLE_USER"); other = fixture("ROLE_USER"); token = jwt.generateToken(new CustomUserDetails(user));
    }
    User fixture(String roleName) {
        var role = roles.findByName(roleName).orElseGet(() -> roles.save(Role.builder().name(roleName).build()));
        return users.save(User.builder().email("profile-" + UUID.randomUUID() + "@example.test").fullName("Old name")
                .phone("0123456789").passwordHash("fixture-hash").status("ACTIVE").roles(Set.of(role)).build());
    }
    <T> T authenticated(Callable<T> action) {
        var context = SecurityContextHolder.getContext(); var before = context.getAuthentication();
        var principal = new CustomUserDetails(user);
        context.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        try { return action.call(); } catch (RuntimeException e) { throw e; } catch (Exception e) { throw new RuntimeException(e); }
        finally { context.setAuthentication(before); }
    }
    ProfileUpdateRequest request(String name, String phone) {
        var r = new ProfileUpdateRequest(); r.setFullName(name); r.setPhone(phone); return r;
    }
    java.net.http.HttpResponse<String> http(String method, String bearer, String body) throws Exception {
        var r = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://127.0.0.1:" + port + "/api/v1/users/me"))
                .header("Content-Type", "application/json");
        if (bearer != null) r.header("Authorization", "Bearer " + bearer);
        return java.net.http.HttpClient.newHttpClient().send(r.method(method, body == null ? java.net.http.HttpRequest.BodyPublishers.noBody()
                : java.net.http.HttpRequest.BodyPublishers.ofString(body)).build(), java.net.http.HttpResponse.BodyHandlers.ofString());
    }
    tools.jackson.databind.JsonNode json(String text) { return tools.jackson.databind.json.JsonMapper.builder().build().readTree(text); }
    void error(java.net.http.HttpResponse<String> r, int status, String code) {
        assertEquals(status, r.statusCode(), r.body()); var value = json(r.body());
        assertEquals(code, value.get("errorCode").asText()); assertFalse(value.get("success").asBoolean()); assertTrue(value.get("data").isNull());
    }
    @ParameterizedTest @ValueSource(strings = {"ROLE_USER", "ROLE_VENDOR", "ROLE_ADMIN"})
    void eachRoleCanReadAndUpdateOnlySelf(String role) throws Exception {
        user = fixture(role); token = jwt.generateToken(new CustomUserDetails(user));
        var result = http("PUT", token, "{\"fullName\":\"  Nguyễn  Thị Ánh  \",\"phone\":\" +84 123  \"}");
        assertEquals(200, result.statusCode(), result.body()); var value = json(result.body()).get("data");
        assertEquals(4, value.size()); assertEquals(user.getId().longValue(), value.get("id").asLong());
        assertEquals("Nguyễn  Thị Ánh", value.get("fullName").asText()); assertEquals("+84 123", value.get("phone").asText());
        assertEquals("Nguyễn  Thị Ánh", json(http("GET", token, null).body()).get("data").get("fullName").asText());
        var stored = users.findById(user.getId()).orElseThrow();
        assertEquals(user.getEmail(), stored.getEmail()); assertEquals("fixture-hash", stored.getPasswordHash()); assertEquals("ACTIVE", stored.getStatus());
        assertEquals(Set.of(role), stored.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()));
        assertEquals("Old name", users.findById(other.getId()).orElseThrow().getFullName());
    }
    @ParameterizedTest @ValueSource(strings = {"userId", "id", "email", "roles", "status", "password", "passwordHash", "avatar", "unknown"})
    void rejectsEveryUnknownFieldWithoutChangingDb(String field) throws Exception {
        error(http("PUT", token, "{\"fullName\":\"Changed\",\"" + field + "\":\"attacker\"}"), 400, "INVALID_INPUT");
        assertEquals("Old name", users.findById(user.getId()).orElseThrow().getFullName());
    }
    @ParameterizedTest @ValueSource(strings = {"{}", "{\"fullName\":null}", "{\"fullName\":\"   \"}",
            "{\"fullName\":1}", "{\"fullName\":[]}", "{\"fullName\":{}}", "{\"fullName\":true}",
            "{\"fullName\":\"Name\",\"phone\":123}", "[]", "null", "{bad"})
    void rejectsInvalidBodyAndTypes(String body) throws Exception { error(http("PUT", token, body), 400, "INVALID_INPUT"); }
    @Test void validatesLengthAfterTrimWithFieldErrors() throws Exception {
        var valid = "{\"fullName\":\" " + "a".repeat(100) + " \",\"phone\":\" " + "0".repeat(20) + " \"}";
        assertEquals(200, http("PUT", token, valid).statusCode());
        var badName = http("PUT", token, "{\"fullName\":\"" + "a".repeat(101) + "\"}");
        error(badName, 400, "INVALID_INPUT"); assertTrue(json(badName.body()).get("fieldErrors").has("fullName"));
        var badPhone = http("PUT", token, "{\"fullName\":\"Name\",\"phone\":\"" + "0".repeat(21) + "\"}");
        error(badPhone, 400, "INVALID_INPUT"); assertTrue(json(badPhone.body()).get("fieldErrors").has("phone"));
    }
    @ParameterizedTest @ValueSource(strings = {"", ",\"phone\":null", ",\"phone\":\"  \""})
    void missingNullOrBlankPhoneClearsIt(String phone) throws Exception {
        assertEquals(200, http("PUT", token, "{\"fullName\":\"Name\"" + phone + "}").statusCode());
        assertNull(users.findById(user.getId()).orElseThrow().getPhone());
    }
    @Test void duplicatePhonesAndUnverifiedTextRemainAllowed() throws Exception {
        assertEquals(200, http("PUT", token, "{\"fullName\":\"Name\",\"phone\":\"0123456789\"}").statusCode());
        assertEquals(other.getPhone(), users.findById(user.getId()).orElseThrow().getPhone());
        assertEquals(200, http("PUT", token, "{\"fullName\":\"Name\",\"phone\":\"contact text\"}").statusCode());
    }
    @Test void unauthenticatedHttpAndDirectServiceAreRejected() throws Exception {
        error(http("GET", null, null), 401, "UNAUTHORIZED"); error(http("PUT", "invalid", "{}"), 401, "UNAUTHORIZED");
        assertThrows(org.springframework.security.core.AuthenticationException.class, () -> profiles.get());
    }
    @Test void callerRollbackReturnsOriginalProfile() {
        new TransactionTemplate(manager).executeWithoutResult(s -> {
            authenticated(() -> profiles.update(request("Changed", null))); s.setRollbackOnly();
        });
        assertEquals("Old name", users.findById(user.getId()).orElseThrow().getFullName());
        assertEquals("0123456789", users.findById(user.getId()).orElseThrow().getPhone());
    }
    @Test void staleManagedUserDoesNotOverwriteConcurrentSensitiveColumns() {
        new TransactionTemplate(manager).executeWithoutResult(s -> {
            users.findById(user.getId()).orElseThrow();
            jdbc.update("update users set password_hash=?,status=? where id=?", "new-hash", "INACTIVE", user.getId());
            var result = authenticated(() -> profiles.update(request("Changed", null)));
            assertEquals("Changed", result.fullName());
        });
        var stored = users.findById(user.getId()).orElseThrow();
        assertEquals("new-hash", stored.getPasswordHash()); assertEquals("INACTIVE", stored.getStatus());
        assertEquals("Changed", stored.getFullName());
    }
    @Test void deletedUserCannotReportSuccessfulUpdate() {
        users.deleteById(user.getId());
        assertEquals(ErrorCode.USER_NOT_FOUND, assertThrows(AppException.class,
                () -> authenticated(() -> profiles.update(request("Name", null)))).getErrorCode());
    }
    @Test void concurrentUpdatesCommitWholePairsLastWriterWins() throws Exception {
        var pool = Executors.newFixedThreadPool(2); var changed = new CountDownLatch(1); var release = new CountDownLatch(1); var started = new CountDownLatch(1);
        try {
            var first = pool.submit(() -> new TransactionTemplate(manager).executeWithoutResult(s -> {
                authenticated(() -> profiles.update(request("First", "111"))); changed.countDown();
                try { assertTrue(release.await(10, TimeUnit.SECONDS)); } catch (InterruptedException e) { throw new AssertionError(e); }
            }));
            assertTrue(changed.await(10, TimeUnit.SECONDS));
            var second = pool.submit(() -> { started.countDown(); return authenticated(() -> profiles.update(request("Second", "222"))); });
            assertTrue(started.await(10, TimeUnit.SECONDS)); assertThrows(TimeoutException.class, () -> second.get(300, TimeUnit.MILLISECONDS));
            release.countDown(); first.get(10, TimeUnit.SECONDS); second.get(10, TimeUnit.SECONDS);
            var stored = users.findById(user.getId()).orElseThrow(); assertEquals("Second", stored.getFullName()); assertEquals("222", stored.getPhone());
        } finally { release.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(15, TimeUnit.SECONDS)); }
    }
}
