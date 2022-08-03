package com.serenditree.fence.extras.scheduler;

import com.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class FenceScheduler {

    private static final Logger LOGGER = Logger.getLogger(FenceScheduler.class.getName());

    private AuthorizationRepositoryApi authorizationRepository;

    @Scheduled(cron = "{serenditree.fence.cronjobs.cleanup}")
    void fenceRecordCleanupJob(ScheduledExecution execution) {
        int count = this.authorizationRepository.deleteFenceRecordsByExpiration();
        LOGGER.info(() -> "Deleted " + count + " expired FenceRecords.");
        LOGGER.info(() -> "Next execution: " + execution.getScheduledFireTime());
    }

    @PostConstruct
    void postConstruct() {
        LOGGER.info(() -> "Started " + FenceScheduler.class.getSimpleName());
    }

    @Inject
    public void setAuthorizationRepository(AuthorizationRepositoryApi authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }
}
