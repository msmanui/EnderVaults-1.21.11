import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SafeAntiDupeManager {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final AtomicInteger clickCount = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final long CLICK_THRESHOLD = 20; // 20 clicks per second

    public SafeAntiDupeManager() {
        startClickMonitoring();
    }

    private void startClickMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            int currentClicks = clickCount.getAndSet(0);
            if (currentClicks > CLICK_THRESHOLD) {
                System.out.println("Click spam detected: " + currentClicks + " clicks in the last second.");
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void onVaultOperation(Runnable operation) {
        lock.writeLock().lock();
        try {
            operation.run(); // Execute the vault operation
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void onUserClick() {
        clickCount.incrementAndGet();
    }

    public void createCheckpointAsync() {
        lock.readLock().lock();
        try {
            Executors.newSingleThreadExecutor().submit(() -> {
                // Code to create checkpoint
                validateDataIntegrity();
            });
        } finally {
            lock.readLock().unlock();
        }
    }

    private void validateDataIntegrity() {
        // Code for data integrity validation
        // Implement checks that ensure no false positives occur.
    }
}