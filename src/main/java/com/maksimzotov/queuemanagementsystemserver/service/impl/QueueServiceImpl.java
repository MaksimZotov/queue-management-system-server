package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.ClientInQueue;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.Queue;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import com.maksimzotov.queuemanagementsystemserver.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@Slf4j
public class QueueServiceImpl implements QueueService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AccountRepo accountRepo;
    private final LocationRepo locationRepo;
    private final QueueRepo queueRepo;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientCodeRepo clientCodeRepo;
    private final JavaMailSender mailSender;
    private final String emailUsernameSender;

    public QueueServiceImpl(
            SimpMessagingTemplate messagingTemplate,
            AccountRepo accountRepo,
            LocationRepo locationRepo,
            QueueRepo queueRepo,
            ClientInQueueRepo clientInQueueRepo,
            ClientCodeRepo clientCodeRepo,
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String emailUsernameSender
    ) {
        this.messagingTemplate = messagingTemplate;
        this.accountRepo = accountRepo;
        this.locationRepo = locationRepo;
        this.queueRepo = queueRepo;
        this.clientInQueueRepo = clientInQueueRepo;
        this.clientCodeRepo = clientCodeRepo;
        this.mailSender = mailSender;
        this.emailUsernameSender = emailUsernameSender;
    }

    @Override
    public Queue createQueue(String username, Long locationId, CreateQueueRequest createQueueRequest) throws DescriptionException {
        if (createQueueRequest.getName().isEmpty()) {
            throw new DescriptionException("Название очереди не может быть пустым");
        }
        QueueEntity entity = queueRepo.save(
                new QueueEntity(
                        null,
                        locationId,
                        createQueueRequest.getName(),
                        createQueueRequest.getDescription()
                )
        );
        return Queue.toModel(entity, true);
    }

    @Override
    public void deleteQueue(String username, Long queueId) throws DescriptionException {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException("Очереди не существует");
        }
        QueueEntity queueEntity = queue.get();

        Optional<LocationEntity> location = locationRepo.findById(queueEntity.getLocationId());
        if (location.isEmpty()) {
            throw new IllegalStateException("Queue with id " + queueId + "exists without location");
        }
        LocationEntity locationEntity = location.get();

        if (Objects.equals(locationEntity.getOwnerUsername(), username)) {
            clientCodeRepo.deleteByPrimaryKeyQueueId(queueId);
            clientInQueueRepo.deleteByPrimaryKeyQueueId(queueId);
            queueRepo.deleteById(queueId);
        } else {
            throw new DescriptionException("У вас нет прав на удаление очереди");
        }
    }

    @Override
    public ContainerForList<Queue> getQueues(Long locationId, Integer page, Integer pageSize, Boolean hasRules) throws DescriptionException {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").descending());
        Page<QueueEntity> pageResult;
        try {
            pageResult = queueRepo.findByLocationId(locationId, pageable);
        } catch (Exception ex) {
            throw new DescriptionException("Локации не существует");
        }
        return new ContainerForList<>(
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                pageResult.getContent().stream().map((item) -> Queue.toModel(item, hasRules)).toList()
        );
    }

    @Override
    public QueueState getQueueState(Long queueId) throws DescriptionException {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException("Очереди не существует");
        }
        QueueEntity queueEntity = queue.get();

        Optional<LocationEntity> location = locationRepo.findById(queueEntity.getLocationId());
        if (location.isEmpty()) {
            throw new IllegalStateException("Queue with id " + queueId + "exists in QueueRepo without");
        }
        LocationEntity locationEntity = location.get();

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findByPrimaryKeyQueueId(queueId);
        if (clients.isEmpty()) {
            throw new IllegalStateException("Queue with id " + queueId + "exists in QueueRepo but does not exist in ClientInQueueRepo");
        }
        List<ClientInQueueEntity> clientsEntities = clients.get();

        return new QueueState(
                queueId,
                queueEntity.getName(),
                queueEntity.getDescription(),
                clientsEntities.stream()
                        .map(ClientInQueue::toModel)
                        .sorted(Comparator.comparingInt(ClientInQueue::getOrderNumber))
                        .toList(),
                locationEntity.getOwnerUsername()
        );
    }

    @Override
    public void serveClientInQueue(String username, Long queueId, String email) throws DescriptionException {
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(
                new ClientInQueueEntity.PrimaryKey(
                        queueId,
                        email
                )
        );
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException("Клиент с почтой " + email + " не стоит в очереди");
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        clientInQueueRepo.updateClientsOrderNumberInQueue(clientInQueueEntity.getOrderNumber());
        clientInQueueRepo.deleteByPrimaryKeyEmail(email);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, getQueueState(queueId));
    }

    @Override
    public void notifyClientInQueue(String username, Long queueId, String email) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailUsernameSender);
        mailMessage.setTo(email);
        mailMessage.setSubject("Очередь");
        mailMessage.setText("Пожалуйста, подойдите к месту оказания услуги");
        mailSender.send(mailMessage);
    }

    @Override
    public ClientInQueue addClient(Long queueId, JoinQueueRequest joinQueueRequest) throws DescriptionException {
        if (joinQueueRequest.getFirstName().isEmpty()) {
            throw new DescriptionException("Имя не может быть пустым");
        }
        if (joinQueueRequest.getFirstName().length() > 64) {
            throw new DescriptionException("Имя должно содержать меньше 64 символов");
        }
        if (joinQueueRequest.getLastName().isEmpty()) {
            throw new DescriptionException("Фамилия не может быть пустой");
        }
        if (joinQueueRequest.getLastName().length() > 64) {
            throw new DescriptionException("Фамилия должна содержать меньше 64 символов");
        }
        if (!Util.emailMatches(joinQueueRequest.getEmail())) {
            throw new DescriptionException("Некорректная почта");
        }

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findByPrimaryKeyQueueId(queueId);
        if (clients.isEmpty()) {
            throw new DescriptionException("Очередь не существует");
        }
        List<ClientInQueueEntity> clientsEntities = clients.get();

        Optional<Integer> maxOrderNumber = clientsEntities.stream()
                .map(ClientInQueueEntity::getOrderNumber)
                .max(Integer::compare);

        Integer curOrderNumber = maxOrderNumber.isEmpty() ? 1 : maxOrderNumber.get() + 1;

        String code = Integer.toString(new Random().nextInt(9000) + 1000);

        ClientInQueueEntity clientInQueueEntity = new ClientInQueueEntity(
                new ClientInQueueEntity.PrimaryKey(
                        queueId,
                        joinQueueRequest.getEmail()
                ),
                joinQueueRequest.getFirstName(),
                joinQueueRequest.getLastName(),
                curOrderNumber,
                code,
                ClientInQueueStatusEntity.CONFIRMED
        );
        clientInQueueRepo.save(clientInQueueEntity);

        QueueState curQueueState = getQueueState(queueId);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, curQueueState);

        return ClientInQueue.toModel(clientInQueueEntity);
    }
}
