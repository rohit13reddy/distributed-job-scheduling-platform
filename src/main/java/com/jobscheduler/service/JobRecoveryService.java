package com.jobscheduler.service;

import com.jobscheduler.repository.JobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class JobRecoveryService {

    private final JobRepository jobRepository;

    public JobRecoveryService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void recoverStaleJobs() {
        Instant cutoff = Instant.now().minusSeconds(120);

        int recovered =
                jobRepository.recoverStaleRunningJobs(cutoff);

        if (recovered > 0) {
            System.out.println(
                    "Recovered " + recovered + " stale jobs");
        }
    }
}