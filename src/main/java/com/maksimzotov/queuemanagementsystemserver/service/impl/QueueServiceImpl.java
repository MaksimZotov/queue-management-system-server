package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.queue.*;
import com.maksimzotov.queuemanagementsystemserver.model.queue.Queue;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.BoardService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@Slf4j
public class QueueServiceImpl implements QueueService {

    private final RightsService rightsService;
    private final BoardService boardService;
    private final SimpMessagingTemplate messagingTemplate;
    private final LocationRepo locationRepo;
    private final QueueRepo queueRepo;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientCodeRepo clientCodeRepo;
    private final JavaMailSender mailSender;
    private final String emailUsernameSender;

    public QueueServiceImpl(
            SimpMessagingTemplate messagingTemplate,
            AccountRepo accountRepo,
            RightsService rightsService,
            BoardService boardService,
            LocationRepo locationRepo,
            QueueRepo queueRepo,
            ClientInQueueRepo clientInQueueRepo,
            ClientCodeRepo clientCodeRepo,
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String emailUsernameSender
    ) {
        this.messagingTemplate = messagingTemplate;
        this.rightsService = rightsService;
        this.boardService = boardService;
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
        boardService.updateLocation(entity.getLocationId());
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
            clientInQueueRepo.deleteByQueueId(queueId);
            queueRepo.deleteById(queueId);
            boardService.updateLocation(location.get().getId());
        } else {
            throw new DescriptionException("У вас нет прав на удаление очереди");
        }
    }

    @Override
    public ContainerForList<Queue> getQueues(Long locationId, String username) throws DescriptionException {
        Optional<List<QueueEntity>> queuesEntities = queueRepo.findAllByLocationId(locationId);
        if (queuesEntities.isEmpty()) {
            throw new DescriptionException("Локации не существует");
        }
        return new ContainerForList<>(
                queuesEntities.get().stream()
                        .map((item) -> Queue.toModel(item, rightsService.checkRightsInLocation(username, locationId)))
                        .toList()
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

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
        if (clients.isEmpty()) {
            throw new IllegalStateException("Queue with id " + queueId + "exists in QueueRepo but does not exist in ClientInQueueRepo");
        }
        List<ClientInQueueEntity> clientsEntities = clients.get();

        return new QueueState(
                queueId,
                queueEntity.getLocationId(),
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
    public void serveClientInQueue(String username, Long queueId, Long clientId) throws DescriptionException {
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(clientId);
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException("Клиент не стоит в очереди");
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        clientInQueueRepo.updateClientsOrderNumberInQueue(clientInQueueEntity.getOrderNumber());
        clientInQueueRepo.deleteById(clientId);
        QueueState curQueueState = getQueueState(queueId);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, curQueueState);
        boardService.updateLocation(curQueueState.getLocationId());
    }

    @Override
    public void notifyClientInQueue(String username, Long queueId, Long clientId) throws DescriptionException {
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(clientId);
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException("Клиент с почтой не стоит в очереди");
        }
        String email = clientInQueue.get().getEmail();
        if (email == null) {
            throw new DescriptionException("У клиента не указан email");
        }
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailUsernameSender);
        mailMessage.setTo(email);
        mailMessage.setSubject("Очередь");
        mailMessage.setText("Пожалуйста, подойдите к месту оказания услуги");
        mailSender.send(mailMessage);
    }

    @Override
    public ClientInQueue addClient(Long queueId, AddClientRequest addClientRequest) throws DescriptionException {
        if (addClientRequest.getFirstName().isEmpty()) {
            throw new DescriptionException("Имя не может быть пустым");
        }
        if (addClientRequest.getFirstName().length() > 64) {
            throw new DescriptionException("Имя должно содержать меньше 64 символов");
        }
        if (addClientRequest.getLastName().isEmpty()) {
            throw new DescriptionException("Фамилия не может быть пустой");
        }
        if (addClientRequest.getLastName().length() > 64) {
            throw new DescriptionException("Фамилия должна содержать меньше 64 символов");
        }

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
        if (clients.isEmpty()) {
            throw new DescriptionException("Очередь не существует");
        }
        List<ClientInQueueEntity> clientsEntities = clients.get();

        Optional<Integer> maxOrderNumber = clientsEntities.stream()
                .map(ClientInQueueEntity::getOrderNumber)
                .max(Integer::compare);

        Integer curOrderNumber = maxOrderNumber.isEmpty() ? 1 : maxOrderNumber.get() + 1;

        String code = Integer.toString(new Random().nextInt(9000) + 1000);

        int publicCode;
        List<Integer> publicCodes = clientsEntities.stream().map(ClientInQueueEntity::getPublicCode).toList();
        Optional<Integer> minOptional = publicCodes.stream().min(Integer::compare);
        if (minOptional.isEmpty()) {
            publicCode = 1;
        } else {
            int min = minOptional.get();
            if (min > 1) {
                publicCode = min - 1;
            } else {
                publicCode = publicCodes.stream().max(Integer::compare).get() + 1;
            }
        }

        ClientInQueueEntity clientInQueueEntity = new ClientInQueueEntity(
                null,
                queueId,
                null,
                addClientRequest.getFirstName(),
                addClientRequest.getLastName(),
                curOrderNumber,
                publicCode,
                code,
                ClientInQueueStatusEntity.Status.CONFIRMED.name()
        );
        clientInQueueRepo.save(clientInQueueEntity);

        QueueState curQueueState = getQueueState(queueId);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, curQueueState);
        boardService.updateLocation(curQueueState.getLocationId());

        return ClientInQueue.toModel(clientInQueueEntity);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public QueueState getQueueStateWithoutTransaction(Long queueId) throws DescriptionException {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException("Очередь не существует");
        }

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
        if (clients.isEmpty()) {
            throw new IllegalStateException("Queue with id " + queueId + "exists in QueueRepo but does not exist in ClientInQueueRepo");
        }

        QueueEntity queueEntity = queue.get();
        List<ClientInQueueEntity> clientsEntities = clients.get();

        return new QueueState(
                queueId,
                queueEntity.getLocationId(),
                queueEntity.getName(),
                queueEntity.getDescription(),
                clientsEntities.stream()
                        .map(ClientInQueue::toModel)
                        .sorted(Comparator.comparingInt(ClientInQueue::getOrderNumber))
                        .toList(),
                null
        );
    }
}
