package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.queue.*;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueStatusRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.LocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.QueueRepo;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class QueueServiceImpl implements QueueService {
    private final LocationRepo locationRepo;
    private final QueueRepo queueRepo;
    private final ClientInQueueStatusRepo clientInQueueStatusRepo;

    @Override
    public Queue createQueue(String username, Long locationId, CreateQueueRequest createQueueRequest) {
        QueueEntity entity = queueRepo.save(
                new QueueEntity(
                        createQueueRequest.getName(),
                        createQueueRequest.getDescription(),
                        locationRepo.findById(locationId).get()
                )
        );
        return Queue.toModel(entity);
    }

    @Override
    public Long deleteQueue(String username, Long id) {
        QueueEntity entity = queueRepo.findById(id).get();
        if (Objects.equals(entity.getLocation().getOwner().getUsername(), username)) {
            queueRepo.delete(entity);
            return id;
        } else {
            return null;
        }
    }

    @Override
    public ContainerForList<Queue> getQueues(Long locationId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<QueueEntity> pageResult = queueRepo.findByLocationIdContaining(locationId, pageable);
        return new ContainerForList<>(
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                pageResult.getContent().stream().map(Queue::toModel).toList()
        );
    }

    @Override
    public QueueState getQueueState(Long id) {
        List<ClientInQueueStatusEntity> clients = clientInQueueStatusRepo.findByQueueIdContaining(id);
        QueueEntity queue = queueRepo.findById(id).get();
        return new QueueState(
                id,
                queue.getName(),
                queue.getDescription(),
                clients.stream().map(ClientInQueue::toModel).toList()
        );
    }

    @Override
    public QueueState joinQueue(Long id, JoinQueueRequest joinQueueRequest) {
        return null;
    }

    @Override
    public Long serveClientInQueue(String username, Long id, Long clientId) {
        return clientId;
    }

    @Override
    public Long notifyClientInQueue(String username, Long id, Long clientId) {
        return clientId;
    }
}
