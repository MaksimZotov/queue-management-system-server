package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.client.AddClientRequst;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.QueueRepo;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.EmailChecker;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final AccountService accountService;
    private final RightsService rightsService;
    private final MailService mailService;
    private final QueueService queueService;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientRepo clientRepo;
    private final QueueRepo queueRepo;

    public ClientServiceImpl(
            AccountService accountService,
            RightsService rightsService,
            MailService mailService,
            QueueService queueService,
            ClientInQueueRepo clientInQueueRepo,
            ClientRepo clientRepo,
            QueueRepo queueRepo
    ) {
        this.accountService = accountService;
        this.rightsService = rightsService;
        this.mailService = mailService;
        this.queueService = queueService;
        this.clientInQueueRepo = clientInQueueRepo;
        this.clientRepo = clientRepo;
        this.queueRepo = queueRepo;
    }

    @Override
    public QueueStateForClient joinByClient(Localizer localizer, AddClientRequst addClientRequst) throws DescriptionException {
        // TODO
        return null;
    }

    @Override
    public QueueStateForClient getQueueStateForClient(String email, String accessKey) {
        // TODO
        return null;
    }

    @Override
    public QueueStateForClient rejoinByClient(Localizer localizer, String email) throws DescriptionException {
        // TODO
        return null;
    }

    @Override
    public QueueStateForClient confirmCodeByClient(Localizer localizer, String email, String code) throws DescriptionException {
        // TODO
        return null;
    }

    @Override
    public QueueStateForClient leaveByClient(Localizer localizer, String email, String accessKey) throws DescriptionException {
        // TODO
        return null;
    }

    @Override
    public void addClientByEmployee(Localizer localizer, String accessToken, AddClientRequst addClientRequst) throws DescriptionException, AccountIsNotAuthorizedException {
        // TODO
    }

    private List<ClientInQueueEntity> checkJoinByClient(Localizer localizer, Long queueId, AddClientRequst addClientRequst) throws DescriptionException {
        checkAddClientRequest(localizer, addClientRequst);

        if (!EmailChecker.emailMatches(addClientRequst.getEmail())) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_EMAIL));
        }

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
        if (clients.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }

        ClientEntity clientEntity = checkClientByEmail(addClientRequst.getEmail());
        if (clientInQueueRepo.existsByQueueIdAndClientId(queueId, clientEntity.getId())) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.CLIENT_WITH_EMAIL_STAND_IN_QUEUE_START,
                            addClientRequst.getEmail(),
                            Message.CLIENT_WITH_EMAIL_STAND_IN_QUEUE_END
                    )
            );
        }

        return clients.get();
    }

    private ClientEntity checkClientByEmail(String email) {
        Optional<ClientEntity> client = clientRepo.findByEmail(email);
        if (client.isEmpty()) {
            return null;
        }
        return client.get();
    }

    @Override
    public void serveClientInQueueByEmployee(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);

        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(clientId);
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_STAND_IN_QUEUE));
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();

        clientInQueueRepo.updateClientsOrderNumberInQueue(queueId, clientInQueueEntity.getOrderNumber());
        clientInQueueRepo.deleteById(clientId);

        queueService.updateCurrentQueueState(queueId);
    }

    @Override
    public void notifyClientInQueueByEmployee(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);

        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(clientId);
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_STAND_IN_QUEUE));
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        ClientEntity clientEntity = clientRepo.findById(clientInQueueEntity.getClientId()).get();
        if (clientEntity.getEmail() == null) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_HAVE_EMAIL));
        }

        mailService.send(clientEntity.getEmail(), localizer.getMessage(Message.QUEUE), localizer.getMessage(Message.PLEASE_GO_TO_SERVICE));
    }

    @Override
    public void switchClientLateStateByEmployee(Localizer localizer, String accessToken, Long queueId, Long clientId, Boolean late) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(clientId);
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_STAND_IN_QUEUE));
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        if (Objects.equals(clientInQueueEntity.getStatus(), ClientInQueueStatusEntity.Status.RESERVED.name())) {
            throw new DescriptionException(localizer.getMessage(Message.WAIT_FOR_CONFIRMATION_OF_CODE_BY_CLIENT));
        }
        if (late) {
            clientInQueueEntity.setStatus(ClientInQueueStatusEntity.Status.LATE.name());
        } else {
            clientInQueueEntity.setStatus(ClientInQueueStatusEntity.Status.CONFIRMED.name());
        }
        queueService.updateCurrentQueueState(queueId);
    }

    private QueueEntity checkRightsInQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountUsername = accountService.getUsername(accessToken);
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();
        if (!rightsService.checkRightsInLocation(accountUsername, queueEntity.getLocationId())) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
        return queueEntity;
    }

    private void checkAddClientRequest(Localizer localizer, AddClientRequst addClientRequest) throws DescriptionException {
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
    }
}
