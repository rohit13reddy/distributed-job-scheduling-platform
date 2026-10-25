
package com.jobscheduler.service;

import com.jobscheduler.entity.Worker;
import com.jobscheduler.repository.WorkerRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PreDestroy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private UUID workerId;

    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    @PostConstruct
    @Transactional
    public void registerWorker() {
        Worker worker = new Worker();
        worker.setId(UUID.randomUUID());
        worker.setWorkerName(
                "worker-" + UUID.randomUUID().toString().substring(0, 8));
        worker.setStatus("ACTIVE");
        worker.setLastHeartbeat(Instant.now());

        workerRepository.save(worker);
        workerId = worker.getId();

        System.out.println("Registered worker: " + workerId);
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void sendHeartbeat() {
        if (workerId == null) {
            return;
        }

        workerRepository.findById(workerId).ifPresent(worker -> {
            worker.setLastHeartbeat(Instant.now());
            workerRepository.save(worker);
        });
    }

    @Transactional(readOnly = true)
    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }


    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void markOfflineWorkers() {
        Instant cutoff = Instant.now().minusSeconds(30);

        List<Worker> workers = workerRepository.findAll();

        for (Worker worker : workers) {
            if ("ACTIVE".equals(worker.getStatus())
                    && worker.getLastHeartbeat() != null
                    && worker.getLastHeartbeat().isBefore(cutoff)) {

                worker.setStatus("OFFLINE");
                workerRepository.save(worker);

                System.out.println(
                        "Worker marked OFFLINE: "
                                + worker.getWorkerName());
            }
        }
    }

    public UUID getWorkerId() {
        return workerId;
    }


    @PreDestroy
    @Transactional
    public void stopWorker() {
        if (workerId == null) {
            return;
        }

        workerRepository.findById(workerId).ifPresent(worker -> {
            worker.setStatus("OFFLINE");
            worker.setLastHeartbeat(Instant.now());
            workerRepository.save(worker);

            System.out.println(
                    "Worker stopped: " + worker.getWorkerName());
        });
    }


}
