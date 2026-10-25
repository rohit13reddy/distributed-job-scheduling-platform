
package com.jobscheduler.service;

import com.jobscheduler.entity.Job;
import com.jobscheduler.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobClaimService {

    private final JobRepository jobRepository;
    private final WorkerService workerService;

    public JobClaimService(
            JobRepository jobRepository,
            WorkerService workerService) {
        this.jobRepository = jobRepository;
        this.workerService = workerService;
    }

    @Transactional
    public Optional<Job> claimNextDueJob() {
        UUID workerId = workerService.getWorkerId();

        if (workerId == null) {
            return Optional.empty();
        }

        Optional<Job> jobOptional =
                jobRepository.claimNextDueJob(Instant.now());

        if (jobOptional.isEmpty()) {
            return Optional.empty();
        }

        Job job = jobOptional.get();
        job.setStatus("RUNNING");
        job.setClaimedAt(Instant.now());
        job.setClaimedByWorkerId(workerId);

        jobRepository.save(job);

        System.out.println(
                "Worker " + workerId
                        + " claimed job " + job.getId());

        return Optional.of(job);
    }
}
