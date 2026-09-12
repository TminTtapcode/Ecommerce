package com.tmt.ecommerce.voucher;

import com.tmt.ecommerce.voucher.api.VoucherInternalService;
import com.tmt.ecommerce.voucher.entity.*;
import com.tmt.ecommerce.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VoucherUsageIntegrationTest {
    @Autowired VoucherRepository vouchers;
    @Autowired VoucherInternalService service;
    @Autowired PlatformTransactionManager manager;
    Long fixture() {
        return vouchers.save(Voucher.builder().code("usage-" + UUID.randomUUID()).scope(VoucherScope.SYSTEM)
                .type(VoucherType.FIXED_AMOUNT).discountValue(BigDecimal.ONE)
                .startDate(LocalDateTime.now().minusDays(1)).endDate(LocalDateTime.now().plusDays(1))
                .usageLimit(1).usedCount(0).build()).getId();
    }
    @Test void onlyOneConcurrentReservationCanConsumeLastUse() throws Exception {
        Long id = fixture(); var pool = Executors.newFixedThreadPool(2);
        var reserved = new CountDownLatch(1); var release = new CountDownLatch(1); var started = new CountDownLatch(1);
        try {
            var first = pool.submit(() -> new TransactionTemplate(manager).execute(s -> {
                assertTrue(service.incrementUsage(id)); reserved.countDown();
                try { assertTrue(release.await(10, TimeUnit.SECONDS)); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
                return true;
            }));
            assertTrue(reserved.await(10, TimeUnit.SECONDS));
            var second = pool.submit(() -> { started.countDown(); return service.incrementUsage(id); });
            assertTrue(started.await(10, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> second.get(300, TimeUnit.MILLISECONDS));
            release.countDown(); assertTrue(first.get(10, TimeUnit.SECONDS)); assertFalse(second.get(10, TimeUnit.SECONDS));
            assertEquals(1, vouchers.findById(id).orElseThrow().getUsedCount());
        } finally { release.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(15, TimeUnit.SECONDS)); }
    }
    @Test void callerRollbackReturnsReservedUse() {
        Long id = fixture();
        new TransactionTemplate(manager).executeWithoutResult(s -> { assertTrue(service.incrementUsage(id)); s.setRollbackOnly(); });
        assertEquals(0, vouchers.findById(id).orElseThrow().getUsedCount());
        assertTrue(service.incrementUsage(id)); assertFalse(service.incrementUsage(id));
    }
}
