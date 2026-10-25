
package com.jobscheduler.repository;

import com.jobscheduler.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;

public interface JobRepository extends JpaRepository<Job, UUID> {

    @Query("""
    SELECT j FROM Job j
    WHERE j.status = 'SCHEDULED'
      AND COALESCE(j.nextAttemptAt, j.scheduledAt) <= :now
    ORDER BY j.priority DESC,
             COALESCE(j.nextAttemptAt, j.scheduledAt) ASC
    """)
    List<Job> findDueJobs(@Param("now") Instant now);

    List<Job> findByStatus(String status, Sort sort);

    Page<Job> findAll(Pageable pageable);

    @Query(value = """
    SELECT *
    FROM jobs
    WHERE status = 'SCHEDULED'
      AND COALESCE(next_attempt_at, scheduled_at) <= :now
    ORDER BY priority DESC,
             COALESCE(next_attempt_at, scheduled_at) ASC
    LIMIT 1
    FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    Optional<Job> claimNextDueJob(@Param("now") Instant now);

    @Query("""
    SELECT j FROM Job j
    WHERE j.status = 'RUNNING'
      AND j.claimedAt < :cutoff
    """)
    List<Job> findStaleRunningJobs(
            @Param("cutoff") Instant cutoff);

    @Modifying
    @Query(value = """
    UPDATE jobs
    SET status = CASE
            WHEN attempts >= max_attempts
            THEN 'FAILED'
            ELSE 'SCHEDULED'
        END,
        next_attempt_at = CASE
            WHEN attempts >= max_attempts
            THEN NULL
            ELSE CURRENT_TIMESTAMP + INTERVAL '5 seconds'
        END,
        claimed_at = NULL,
        last_error = 'Worker lease expired; execution outcome unknown'
    WHERE status = 'RUNNING'
      AND claimed_at IS NOT NULL
      AND claimed_at < :cutoff
    """, nativeQuery = true)
    int recoverStaleRunningJobs(@Param("cutoff") Instant cutoff);
}
