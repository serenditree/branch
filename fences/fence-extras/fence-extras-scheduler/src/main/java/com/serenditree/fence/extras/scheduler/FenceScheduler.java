package com.serenditree.fence.extras.scheduler;

import com.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FenceScheduler {

    private AuthorizationRepositoryApi authorizationRepository;

    @Scheduled(cron = "{serenditree.fence.cronjobs.cleanup}")
    void fenceRecordCleanupJob(ScheduledExecution execution) {
        int count = this.authorizationRepository.deleteFenceRecordsByExpiration();
        Log.infov("Deleted {0} expired FenceRecords.", count);
        Log.infov("Next execution: {0}", execution.getScheduledFireTime());
    }

    @PostConstruct
    void postConstruct() {
        Log.infov("Started {0}", FenceScheduler.class.getSimpleName());
    }

    @Inject
    public void setAuthorizationRepository(AuthorizationRepositoryApi authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }
}
