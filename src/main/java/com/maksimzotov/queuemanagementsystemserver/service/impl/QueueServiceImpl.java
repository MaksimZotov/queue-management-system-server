package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.queue.Queue;
import com.maksimzotov.queuemanagementsystemserver.model.queue.*;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.BoardService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
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
public class QueueServiceImpl implements QueueService {

    private final AccountService accountService;
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
            AccountService accountService,
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
        this.accountService = accountService;
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
    public Queue createQueue(Localizer localizer, String accessToken, Long locationId, CreateQueueRequest createQueueRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountUsername = accountService.getUsername(accessToken);

        if (!rightsService.checkRightsInLocation(accountUsername, locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }

        if (createQueueRequest.getName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_NAME_MUST_NOT_BE_EMPTY));
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
    public void deleteQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountUsername = accountService.getUsername(accessToken);

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();

        if (!rightsService.checkRightsInLocation(accountUsername, queueEntity.getLocationId())) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }

        clientCodeRepo.deleteByPrimaryKeyQueueId(queueId);
        clientInQueueRepo.deleteByQueueId(queueId);
        queueRepo.deleteById(queueId);
        boardService.updateLocation(queueEntity.getLocationId());
    }

    @Override
    public ContainerForList<Queue> getQueues(Localizer localizer, String accessToken, Long locationId) throws DescriptionException {
        Optional<List<QueueEntity>> queuesEntities = queueRepo.findAllByLocationId(locationId);
        if (queuesEntities.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        return new ContainerForList<>(
                queuesEntities.get().stream()
                        .map((item) -> Queue.toModel(item, rightsService.checkRightsInLocation(accountService.getUsernameOrNull(accessToken), locationId)))
                        .toList()
        );
    }

    @Override
    public QueueState getQueueState(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountUsername = accountService.getUsername(accessToken);

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();

        if (!rightsService.checkRightsInLocation(accountUsername, queueEntity.getLocationId())) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_VIEW));
        }

        Optional<LocationEntity> location = locationRepo.findById(queueEntity.getLocationId());
        LocationEntity locationEntity = location.get();

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
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
    public void serveClientInQueue(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountUsername = accountService.getUsername(accessToken);

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();

        if (!rightsService.checkRightsInLocation(accountUsername, queueEntity.getLocationId())) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }

        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(clientId);
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_STAND_IN_QUEUE));
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();

        clientInQueueRepo.updateClientsOrderNumberInQueue(clientInQueueEntity.getOrderNumber());
        clientInQueueRepo.deleteById(clientId);

        QueueState curQueueState = getQueueStateWithoutTransaction(queueId);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, curQueueState);
        boardService.updateLocation(curQueueState.getLocationId());
    }

    @Override
    public void notifyClientInQueue(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountUsername = accountService.getUsername(accessToken);

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();

        if (!rightsService.checkRightsInLocation(accountUsername, queueEntity.getLocationId())) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }

        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(clientId);
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_STAND_IN_QUEUE));
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
    public ClientInQueue addClient(Localizer localizer, String accessToken, Long queueId, AddClientRequest addClientRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountUsername = accountService.getUsername(accessToken);

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();

        if (!rightsService.checkRightsInLocation(accountUsername, queueEntity.getLocationId())) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }

        if (addClientRequest.getFirstName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.FIRST_NAME_MUST_NOT_BE_EMPTY));
        }
        if (addClientRequest.getFirstName().length() > 64) {
            throw new DescriptionException(localizer.getMessage(Message.FIRST_NAME_MUST_CONTAINS_LESS_THAN_64_SYMBOLS));
        }
        if (addClientRequest.getLastName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LAST_NAME_MUST_NOT_BE_EMPTY));
        }
        if (addClientRequest.getLastName().length() > 64) {
            throw new DescriptionException(localizer.getMessage(Message.LAST_NAME_MUST_CONTAINS_LESS_THAN_64_SYMBOLS));
        }

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
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

        QueueState curQueueState = getQueueStateWithoutTransaction(queueId);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, curQueueState);
        boardService.updateLocation(curQueueState.getLocationId());

        return ClientInQueue.toModel(clientInQueueEntity);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public QueueState getQueueStateWithoutTransaction(Long queueId) {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        QueueEntity queueEntity = queue.get();

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
        List<ClientInQueueEntity> clientsEntities = clients.get();

        Optional<LocationEntity> location = locationRepo.findById(queueEntity.getLocationId());
        LocationEntity locationEntity = location.get();

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
}
