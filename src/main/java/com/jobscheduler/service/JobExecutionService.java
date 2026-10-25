package com.jobscheduler.service;

import com.jobscheduler.entity.Job;
import com.jobscheduler.repository.JobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class JobExecutionService {

    private final JobClaimService jobClaimService;
    private final JobRepository jobRepository;

    public JobExecutionService(JobClaimService jobClaimService, JobRepository jobRepository) {
        this.jobClaimService = jobClaimService;
        this.jobRepository = jobRepository;
    }

    @Scheduled(fixedDelay = 5000)
    public void executeDueJobs() {
        while (true) {
            Optional<Job> claimedJob =
                    jobClaimService.claimNextDueJob();

            if (claimedJob.isEmpty()) {
                break;
            }

            executeJob(claimedJob.get());
        }
    }

    public void executeJob(Job job) {
        job.setAttempts(job.getAttempts() + 1);
        job.setNextAttemptAt(null);

        try {
            performJob(job);

            job.setStatus("COMPLETED");
            job.setLastError(null);

            System.out.println(
                    "Job completed: " + job.getId());

        } catch (Exception e) {
            job.setLastError(e.getMessage());

            if (job.getAttempts() < job.getMaxAttempts()) {
                long delaySeconds =
                        5L * (1L << (job.getAttempts() - 1));

                job.setStatus("SCHEDULED");
                job.setNextAttemptAt(
                        Instant.now().plusSeconds(delaySeconds));

                System.out.println(
                        "Job failed; retrying in "
                                + delaySeconds + " seconds: "
                                + job.getId());

            } else {
                job.setStatus("FAILED");

                System.out.println(
                        "Job permanently failed: "
                                + job.getId());
            }
        }

        // Persist the execution result.
        // The claim service already committed RUNNING.
        // Repository save operations manage their own transactions.
        job.setClaimedAt(null);
        jobRepository.save(job);
    }

    private void performJob(Job job) {
        System.out.println("Executing job: " + job.getId());

        if (job.getPayload() != null
                && job.getPayload().contains(
                "\"simulateFailure\":true")) {
            throw new RuntimeException(
                    "Simulated failure for retry testing");
        }

        System.out.println(
                "Processing job type: " + job.getJobType());
    }
}