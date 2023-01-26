package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.client.AddClientRequst;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.CodeGenerator;
import com.maksimzotov.queuemanagementsystemserver.util.EmailChecker;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private final ServiceInServicesSequenceRepo serviceInServicesSequenceRepo;
    private final ServicesSequenceInLocationRepo servicesSequenceInLocationRepo;
    private final ServiceInLocationRepo serviceInLocationRepo;
    private final ServiceInQueueTypeRepo serviceInQueueTypeRepo;
    private final ClientInQueueToChosenServiceRepo clientInQueueToChosenServiceRepo;

    public ClientServiceImpl(
            AccountService accountService,
            RightsService rightsService,
            MailService mailService,
            QueueService queueService,
            ClientInQueueRepo clientInQueueRepo,
            ClientRepo clientRepo,
            QueueRepo queueRepo,
            ServiceInServicesSequenceRepo serviceInServicesSequenceRepo,
            ServicesSequenceInLocationRepo servicesSequenceInLocationRepo,
            ServiceInLocationRepo serviceInLocationRepo,
            ServiceInQueueTypeRepo serviceInQueueTypeRepo,
            ClientInQueueToChosenServiceRepo clientInQueueToChosenServiceRepo
    ) {
        this.accountService = accountService;
        this.rightsService = rightsService;
        this.mailService = mailService;
        this.queueService = queueService;
        this.clientInQueueRepo = clientInQueueRepo;
        this.clientRepo = clientRepo;
        this.queueRepo = queueRepo;
        this.serviceInServicesSequenceRepo = serviceInServicesSequenceRepo;
        this.servicesSequenceInLocationRepo = servicesSequenceInLocationRepo;
        this.serviceInLocationRepo = serviceInLocationRepo;
        this.serviceInQueueTypeRepo = serviceInQueueTypeRepo;
        this.clientInQueueToChosenServiceRepo = clientInQueueToChosenServiceRepo;
    }

    @Override
    public QueueStateForClient joinByClient(Localizer localizer, Long locationId, AddClientRequst addClientRequest) throws DescriptionException {
        if (!EmailChecker.emailMatches(addClientRequest.getEmail())) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_EMAIL));
        }
        Map<Long, Integer> serviceIdsToOrderNumbers = checkAddClientRequest(localizer, locationId, addClientRequest);
        ClientEntity clientEntity = clientRepo.save(getClientToAdd(localizer, locationId, addClientRequest, ClientStatusEntity.Status.RESERVED));
        distributeClient(localizer, clientEntity, serviceIdsToOrderNumbers);
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
    public void addClientByEmployee(Localizer localizer, String accessToken, Long locationId, AddClientRequst addClientRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        rightsService.checkRightsInLocation(accountService.getUsername(accessToken), locationId);
        Map<Long, Integer> serviceIdsToOrderNumbers = checkAddClientRequest(localizer, locationId, addClientRequest);
        ClientEntity clientEntity = clientRepo.save(getClientToAdd(localizer, locationId, addClientRequest, ClientStatusEntity.Status.CONFIRMED));
        distributeClient(localizer, clientEntity, serviceIdsToOrderNumbers);
    }

    private ClientEntity getClientToAdd(Localizer localizer, Long locationId, AddClientRequst addClientRequest, ClientStatusEntity.Status status) throws DescriptionException {
        if (addClientRequest.getEmail() != null && clientRepo.findByEmail(addClientRequest.getEmail()).isPresent()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_WITH_THIS_EMAIL_ALREADY_EXIST));
        }
        return new ClientEntity(
                null,
                locationId,
                addClientRequest.getEmail(),
                addClientRequest.getFirstName(),
                addClientRequest.getLastName(),
                CodeGenerator.generate(),
                status.name()
        );
    }

    private void distributeClient(Localizer localizer, ClientEntity clientEntity, Map<Long, Integer> serviceIdsToOrderNumbers) throws DescriptionException {
        int minOrderNumber = Integer.MAX_VALUE;
        Collection<Integer> orderNumbers = serviceIdsToOrderNumbers.values();
        for (Integer orderNumber : orderNumbers) {
            if (orderNumber < minOrderNumber) {
                minOrderNumber = orderNumber;
            }
        }
        List<Long> serviceIds = new ArrayList<>();
        for (Map.Entry<Long, Integer> serviceIdTpOrderNumber : serviceIdsToOrderNumbers.entrySet()) {
            if (serviceIdTpOrderNumber.getValue() == minOrderNumber) {
                serviceIds.add(serviceIdTpOrderNumber.getKey());
            }
        }
        QueueEntity curQueueEntity = getCurrentQueue(localizer, serviceIds);
        List<ServiceInQueueTypeEntity> serviceInQueueTypeEntities = serviceInQueueTypeRepo.findAllByQueueTypeId(curQueueEntity.getQueueTypeId());
        List<Long> serviceIdsInQueueType = serviceInQueueTypeEntities.stream().map(ServiceInQueueTypeEntity::getServiceId).toList();

        List<Long> servicesWithKnownQueue = serviceIds.stream()
                .distinct()
                .filter(serviceIdsInQueueType::contains)
                .toList();

        List<Long> servicesWithUnknownQueue = serviceIds;
        servicesWithUnknownQueue.removeAll(servicesWithKnownQueue);


    }

    private QueueEntity getCurrentQueue(Localizer localizer, List<Long> serviceIds) throws DescriptionException {
        Set<ServiceInQueueTypeEntity> serviceInQueueTypeEntities = new HashSet<>();
        for (Long serviceId : serviceIds) {
            serviceInQueueTypeEntities.addAll(serviceInQueueTypeRepo.findAllByServiceId(serviceId));
        }
        List<QueueEntity> queueEntities = new ArrayList<>();
        for (ServiceInQueueTypeEntity serviceInQueueTypeEntity : serviceInQueueTypeEntities) {
            queueEntities.addAll(queueRepo.findAllByQueueTypeId(serviceInQueueTypeEntity.getQueueTypeId()));
        }
        queueEntities = queueEntities.stream().filter(queue -> !queue.getPaused()).toList();

        QueueEntity chosenQueueEntity = null;
        Long minWaitTime = Long.MAX_VALUE;
        for (QueueEntity queueEntity : queueEntities) {
            Long curWaitTime = estimateQueueWaitTime(queueEntity);
            if (curWaitTime < minWaitTime) {
                chosenQueueEntity = queueEntity;
                minWaitTime = curWaitTime;
            }
        }

        return chosenQueueEntity;
    }

    private Long estimateQueueWaitTime(QueueEntity queueEntity) {
        List<ClientInQueueToChosenServiceEntity> clientInQueueToChosenServiceEntities =
                clientInQueueToChosenServiceRepo.findAllByQueueId(queueEntity.getId());
        long waitTime = 0L;
        for (ClientInQueueToChosenServiceEntity clientInQueueToChosenServiceEntity : clientInQueueToChosenServiceEntities) {
            waitTime += estimateServiceDuration(clientInQueueToChosenServiceEntity.getServiceId());
        }
        return waitTime;
    }

    private Long estimateServiceDuration(Long serviceId) {
        // TODO
        return 1000L;
    }

    private void switchToNextQueue() {
        // TODO
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
        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        ClientEntity clientEntity = client.get();
        if (Objects.equals(clientEntity.getStatus(), ClientStatusEntity.Status.RESERVED.name())) {
            throw new DescriptionException(localizer.getMessage(Message.WAIT_FOR_CONFIRMATION_OF_CODE_BY_CLIENT));
        }
        if (late) {
            clientEntity.setStatus(ClientStatusEntity.Status.LATE.name());
        } else {
            clientEntity.setStatus(ClientStatusEntity.Status.CONFIRMED.name());
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

    private Map<Long, Integer> checkAddClientRequest(Localizer localizer, Long locationId, AddClientRequst addClientRequest) throws DescriptionException {
        boolean servicesChosen = addClientRequest.getServiceIds() != null && !addClientRequest.getServiceIds().isEmpty();
        boolean servicesSequenceChosen = addClientRequest.getServicesSequenceId() != null;
        if (servicesChosen == servicesSequenceChosen) {
            throw new DescriptionException(localizer.getMessage(Message.INCORRECT_REQUEST));
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

        Map<Long, Integer> serviceIdsToOrderNumbers = new HashMap<>();
        if (servicesChosen) {
            List<Long> serviceIds = addClientRequest.getServiceIds();
            for (Long serviceId: serviceIds) {
                if (serviceInLocationRepo.existsById(new ServiceInLocationEntity(serviceId, locationId))) {
                    throw new DescriptionException(localizer.getMessage(Message.ONE_OR_MORE_OF_CHOSEN_SERVICES_DO_NOT_EXIST_IN_LOCATION));
                }
                serviceIdsToOrderNumbers.put(serviceId, 1);
            }
        } else {
            Long servicesSequenceId = addClientRequest.getServicesSequenceId();
            if (!servicesSequenceInLocationRepo.existsById(new ServicesSequenceInLocationEntity(servicesSequenceId, locationId))) {
                throw new DescriptionException(localizer.getMessage(Message.CHOSEN_SERVICES_SEQUENCE_DOES_NOT_EXIST_IN_LOCATION));
            }
            List<ServicesInServicesSequenceEntity> servicesInServicesSequenceEntities =
                    serviceInServicesSequenceRepo.findAllByPrimaryKeyServicesSequenceIdOrderByOrderNumberAsc(
                            servicesSequenceId
                    );
            for (ServicesInServicesSequenceEntity servicesSequenceEntity: servicesInServicesSequenceEntities) {
                serviceIdsToOrderNumbers.put(servicesSequenceEntity.getPrimaryKey().getServiceId(), servicesSequenceEntity.getOrderNumber());
            }
        }

        return serviceIdsToOrderNumbers;
    }
}
