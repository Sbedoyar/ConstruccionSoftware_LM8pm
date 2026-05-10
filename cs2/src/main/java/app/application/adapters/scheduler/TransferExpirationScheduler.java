package app.application.adapters.scheduler;

import app.domain.services.ExpireTransfer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TransferExpirationScheduler {

    private final ExpireTransfer expireTransfer;

    public TransferExpirationScheduler(ExpireTransfer expireTransfer) {
        this.expireTransfer = expireTransfer;
    }

    @Scheduled(fixedRate = 10000)
    public void expirePendingTransfers() {
        expireTransfer.expirePendingTransfers();
    }
}
