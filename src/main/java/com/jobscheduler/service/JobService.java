
package com.jobscheduler.service;

import com.jobscheduler.dto.CreateJobRequest;
import com.jobscheduler.entity.Job;
import com.jobscheduler.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Sort;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // Replace the existing createJob method with this:
    @Transactional
    public Job createJob(CreateJobRequest request) {
        Job job = new Job();

        job.setJobType(request.getJobType());
        job.setPayload(request.getPayload());
        job.setPriority(request.getPriority());
        job.setMaxAttempts(request.getMaxAttempts());

        if (request.getScheduledAt() == null) {
            job.setScheduledAt(Instant.now());
        } else {
            job.setScheduledAt(request.getScheduledAt());
        }

        job.setStatus("SCHEDULED");

        return jobRepository.save(job);
    }


    public Job getJobById(UUID id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Job not found: " + id));
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Job> getJobsByStatus(String status) {
        return jobRepository.findByStatus(
                status.toUpperCase(),
                Sort.by(
                        Sort.Order.desc("priority"),
                        Sort.Order.asc("createdAt")
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<Job> getJobs(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }
}
