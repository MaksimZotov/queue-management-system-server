package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.Constants;
import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.client.AddClientRequst;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueStateModel;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.CodeGenerator;
import com.maksimzotov.queuemanagementsystemserver.util.EmailChecker;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final AccountService accountService;
    private final RightsService rightsService;
    private final MailService mailService;
    private final QueueService queueService;
    private final DelayedJobService delayedJobService;
    private final CleanerService cleanerService;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientRepo clientRepo;
    private final QueueRepo queueRepo;
    private final LocationRepo locationRepo;
    private final ServiceRepo serviceRepo;
    private final ServicesSequenceRepo servicesSequenceRepo;
    private final HistoryItemRepo historyItemRepo;
    private final ServiceInHistoryItemRepo serviceInHistoryItemRepo;
    private final ServiceInServicesSequenceRepo serviceInServicesSequenceRepo;
    private final ServiceInSpecialistRepo serviceInSpecialistRepo;
    private final ClientInQueueToChosenServiceRepo clientInQueueToChosenServiceRepo;
    private final ClientToChosenServiceRepo clientToChosenServiceRepo;
    private final Integer confirmationTimeInSeconds;

    public ClientServiceImpl(
            AccountService accountService,
            RightsService rightsService,
            MailService mailService,
            @Lazy QueueService queueService,
            DelayedJobService delayedJobService,
            CleanerService cleanerService,
            ClientInQueueRepo clientInQueueRepo,
            ClientRepo clientRepo,
            QueueRepo queueRepo,
            LocationRepo locationRepo,
            ServiceRepo serviceRepo,
            ServicesSequenceRepo servicesSequenceRepo,
            HistoryItemRepo historyItemRepo,
            ServiceInHistoryItemRepo serviceInHistoryItemRepo,
            ServiceInServicesSequenceRepo serviceInServicesSequenceRepo,
            ServiceInSpecialistRepo serviceInSpecialistRepo,
            ClientInQueueToChosenServiceRepo clientInQueueToChosenServiceRepo,
            ClientToChosenServiceRepo clientToChosenServiceRepo,
            @Value("${app.registration.confirmationtime.join}")  Integer confirmationTimeInSeconds
    ) {
        this.accountService = accountService;
        this.rightsService = rightsService;
        this.mailService = mailService;
        this.queueService = queueService;
        this.delayedJobService = delayedJobService;
        this.cleanerService = cleanerService;
        this.clientInQueueRepo = clientInQueueRepo;
        this.clientRepo = clientRepo;
        this.queueRepo = queueRepo;
        this.locationRepo = locationRepo;
        this.serviceRepo = serviceRepo;
        this.servicesSequenceRepo = servicesSequenceRepo;
        this.historyItemRepo = historyItemRepo;
        this.serviceInHistoryItemRepo = serviceInHistoryItemRepo;
        this.serviceInServicesSequenceRepo = serviceInServicesSequenceRepo;
        this.serviceInSpecialistRepo = serviceInSpecialistRepo;
        this.clientInQueueToChosenServiceRepo = clientInQueueToChosenServiceRepo;
        this.clientToChosenServiceRepo = clientToChosenServiceRepo;
        this.confirmationTimeInSeconds = confirmationTimeInSeconds;
    }

    @Override
    public void addClient(Localizer localizer, Long locationId, AddClientRequst addClientRequest) throws DescriptionException {
        Map<Long, Integer> serviceIdsToOrderNumbers = checkAddClientRequest(localizer, locationId, addClientRequest);
        createClient(localizer, locationId, addClientRequest, serviceIdsToOrderNumbers);
    }

    @Override
    public QueueStateForClient getQueueStateForClient(Localizer localizer, Long clientId, String accessKey) throws DescriptionException {
        ClientEntity clientEntity = checkAccessKey(localizer, clientId, accessKey);
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByClientId(clientId);
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_STAND_IN_QUEUE));
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        QueueStateModel queueStateModel = queueService.getCurrentQueueState(clientInQueueEntity.getQueueId());
        return QueueStateForClient.toModel(queueStateModel, clientInQueueEntity, clientEntity);
    }

    @Override
    public QueueStateForClient confirmAccessKeyByClient(Localizer localizer, Long clientId, String accessKey) throws DescriptionException {
        ClientEntity clientEntity = checkAccessKey(localizer, clientId, accessKey);
        if (Objects.equals(clientEntity.getStatus(), ClientStatusEntity.Status.CONFIRMED.name())) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_ALREADY_CONFIRMED));
        }
        clientEntity.setStatus(ClientStatusEntity.Status.CONFIRMED.name());
        clientRepo.save(clientEntity);
        distributeClient(localizer, clientEntity, clientEntity.getLocationId(), getServiceIdsToOrderNumbersForClient(clientId));
        return getQueueStateForClient(localizer, clientId, accessKey);
    }

    @Override
    public QueueStateForClient leaveByClient(Localizer localizer, Long clientId, String accessKey) throws DescriptionException {
        checkAccessKey(localizer, clientId, accessKey);
        Optional<ClientInQueueEntity> client = clientInQueueRepo.findByClientId(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        ClientInQueueEntity clientInQueueEntity = client.get();
        clientInQueueRepo.updateClientsOrderNumberInQueue(clientId, clientInQueueEntity.getOrderNumber());
        clientInQueueRepo.deleteByClientId(clientId);
        clientToChosenServiceRepo.deleteByPrimaryKeyClientId(clientId);
        clientInQueueToChosenServiceRepo.deleteAllByClientId(clientId);
        clientRepo.deleteById(clientId);
        queueService.updateCurrentQueueState(clientId);
        return getQueueStateForClient(localizer, clientId, accessKey);
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

        switchToNextQueue(localizer, clientId);

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

    private void checkRightsInQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountEmail = accountService.getEmail(accessToken);
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();
        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountEmail, queueEntity.getLocationId())) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
    }

    private Map<Long, Integer> checkAddClientRequest(Localizer localizer, Long locationId, AddClientRequst addClientRequest) throws DescriptionException {
        boolean servicesChosen = addClientRequest.getServiceIds() != null && !addClientRequest.getServiceIds().isEmpty();
        boolean servicesSequenceChosen = addClientRequest.getServicesSequenceId() != null;
        if (servicesChosen == servicesSequenceChosen) {
            throw new DescriptionException(localizer.getMessage(Message.INCORRECT_REQUEST));
        }

        if (!EmailChecker.emailMatches(addClientRequest.getEmail())) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_EMAIL));
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
            for (Long serviceId : serviceIds) {
                if (!serviceRepo.existsByIdAndLocationId(serviceId, locationId)) {
                    throw new DescriptionException(localizer.getMessage(Message.ONE_OR_MORE_OF_CHOSEN_SERVICES_DO_NOT_EXIST_IN_LOCATION));
                }
                serviceIdsToOrderNumbers.put(serviceId, 1);
            }
        } else {
            Long servicesSequenceId = addClientRequest.getServicesSequenceId();
            if (!servicesSequenceRepo.existsByIdAndLocationId(servicesSequenceId, locationId)) {
                throw new DescriptionException(localizer.getMessage(Message.CHOSEN_SERVICES_SEQUENCE_DOES_NOT_EXIST_IN_LOCATION));
            }
            List<ServiceInServicesSequenceEntity> servicesInServicesSequenceEntities =
                    serviceInServicesSequenceRepo.findAllByPrimaryKeyServicesSequenceIdOrderByOrderNumberAsc(
                            servicesSequenceId
                    );
            for (ServiceInServicesSequenceEntity servicesSequenceEntity : servicesInServicesSequenceEntities) {
                serviceIdsToOrderNumbers.put(servicesSequenceEntity.getPrimaryKey().getServiceId(), servicesSequenceEntity.getOrderNumber());
            }
        }

        return serviceIdsToOrderNumbers;
    }

    private ClientEntity checkAccessKey(Localizer localizer, Long clientId, String accessKey) throws DescriptionException {
        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        ClientEntity clientEntity = client.get();
        if (!Objects.equals(clientEntity.getAccessKey(), accessKey)) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_ACCESS_KEY));
        }
        return clientEntity;
    }

    private String getLinkForClient(Localizer localizer, ClientEntity clientEntity, Long locationId) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        return new StringBuilder()
                .append(Constants.CLIENT_URL)
                .append("/locations/")
                .append(locationId)
                .append("/client?client_id=")
                .append(clientEntity.getId())
                .append("&access_key=")
                .append(clientEntity.getAccessKey())
                .toString();
    }

    private void createClient(Localizer localizer, Long locationId, AddClientRequst addClientRequest, Map<Long, Integer> serviceIdsToOrderNumbers) throws DescriptionException {
        if (addClientRequest.getEmail() != null && clientRepo.findByEmail(addClientRequest.getEmail()).isPresent()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_WITH_THIS_EMAIL_ALREADY_EXIST));
        }

        ClientEntity clientEntity = clientRepo.save(
                new ClientEntity(
                        null,
                        locationId,
                        addClientRequest.getEmail(),
                        addClientRequest.getFirstName(),
                        addClientRequest.getLastName(),
                        CodeGenerator.generate(),
                        ClientStatusEntity.Status.RESERVED.name()
                )
        );

        for (Map.Entry<Long, Integer> serviceIdToOrderNumber : serviceIdsToOrderNumbers.entrySet()) {
            clientToChosenServiceRepo.save(
                    new ClientToChosenServiceEntity(
                            new ClientToChosenServiceEntity.PrimaryKey(
                                    clientEntity.getId(),
                                    serviceIdToOrderNumber.getKey()
                            ),
                            serviceIdToOrderNumber.getValue()
                    )
            );
        }

        delayedJobService.schedule(
                () -> cleanerService.deleteNonConfirmedClient(clientEntity.getId(), addClientRequest.getEmail()),
                confirmationTimeInSeconds,
                TimeUnit.SECONDS
        );

        mailService.send(
                addClientRequest.getEmail(),
                localizer.getMessage(Message.CONFIRMATION_OF_CONNECTION),
                localizer.getMessageForClientConfirmation(getLinkForClient(localizer, clientEntity, locationId))
        );
    }

    private void distributeClient(Localizer localizer, ClientEntity clientEntity, Long locationId, Map<Long, Integer> serviceIdsToOrderNumbers) throws DescriptionException {
        int minOrderNumber = Integer.MAX_VALUE;
        Collection<Integer> orderNumbers = serviceIdsToOrderNumbers.values();
        for (Integer orderNumber : orderNumbers) {
            if (orderNumber < minOrderNumber) {
                minOrderNumber = orderNumber;
            }
        }
        List<Long> serviceIds = new ArrayList<>();
        for (Map.Entry<Long, Integer> serviceIdToOrderNumber : serviceIdsToOrderNumbers.entrySet()) {
            if (serviceIdToOrderNumber.getValue() == minOrderNumber) {
                serviceIds.add(serviceIdToOrderNumber.getKey());
            }
        }

        QueueEntity queueToAssign = getQueueToAssign(serviceIds, locationId);
        if (queueToAssign == null) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_NOT_FOUND_FOR_CLIENT));
        }

        List<ClientInQueueEntity> clientsEntities = clientInQueueRepo.findAllByQueueId(queueToAssign.getId());
        Optional<Integer> maxOrderNumber = clientsEntities.stream().map(ClientInQueueEntity::getOrderNumber).max(Integer::compare);
        Integer orderNumber = maxOrderNumber.isEmpty() ? 1 : maxOrderNumber.get() + 1;
        Integer publicCode = CodeGenerator.generate(clientsEntities.stream().map(ClientInQueueEntity::getPublicCode).toList());
        clientInQueueRepo.save(
                new ClientInQueueEntity(
                        clientEntity.getId(),
                        queueToAssign.getId(),
                        orderNumber,
                        publicCode
                )
        );

        HistoryItemEntity historyItemEntity = historyItemRepo.save(
                new HistoryItemEntity(
                        null,
                        clientEntity.getId(),
                        new Date(),
                        null
                )
        );
        List<ServiceInSpecialistEntity> serviceInQueueTypeEntities = serviceInSpecialistRepo.findAllBySpecialistId(queueToAssign.getSpecialistId());
        List<Long> serviceIdsInQueueType = serviceInQueueTypeEntities.stream().map(ServiceInSpecialistEntity::getServiceId).toList();
        List<Long> serviceIdsWithKnownQueue = serviceIds.stream().distinct().filter(serviceIdsInQueueType::contains).toList();
        for (Long serviceId : serviceIdsWithKnownQueue) {
            clientInQueueToChosenServiceRepo.save(
                    new ClientInQueueToChosenServiceEntity(
                            clientEntity.getId(),
                            serviceId,
                            queueToAssign.getId()
                    )
            );
            serviceInHistoryItemRepo.save(
                    new ServiceInHistoryItemEntity(
                            null,
                            historyItemEntity.getId(),
                            serviceId
                    )
            );
        }

        mailService.send(
                clientEntity.getEmail(),
                localizer.getMessage(Message.YOUR_STATUS_IN_QUEUE),
                localizer.getMessageForClientCheckStatus(
                        queueToAssign.getName(),
                        publicCode.toString(),
                        getLinkForClient(localizer, clientEntity, locationId)
                )
        );

        queueService.updateCurrentQueueState(queueToAssign.getId());
    }

    private QueueEntity getQueueToAssign(List<Long> serviceIds, Long locationId) {
        Set<ServiceInSpecialistEntity> serviceInQueueTypeEntities = new HashSet<>();
        for (Long serviceId : serviceIds) {
            serviceInQueueTypeEntities.addAll(serviceInSpecialistRepo.findAllByServiceId(serviceId));
        }
        List<QueueEntity> queueEntities = new ArrayList<>();
        for (ServiceInSpecialistEntity serviceInSpecialistEntity : serviceInQueueTypeEntities) {
            queueEntities.addAll(
                    queueRepo.findAllBySpecialistIdAndLocationId(
                            serviceInSpecialistEntity.getSpecialistId(),
                            locationId
                    )
            );
        }
        queueEntities = queueEntities.stream().filter(QueueEntity::getEnabled).toList();

        QueueEntity queueToAssign = null;
        long minWaitTime = Long.MAX_VALUE;
        for (QueueEntity queueEntity : queueEntities) {
            Long curWaitTime = estimateQueueWaitTime(queueEntity);
            if (curWaitTime < minWaitTime) {
                queueToAssign = queueEntity;
                minWaitTime = curWaitTime;
            }
        }

        return queueToAssign;
    }

    private Long estimateQueueWaitTime(QueueEntity queueEntity) {
        List<ClientInQueueToChosenServiceEntity> clientInQueueToChosenServiceEntities = clientInQueueToChosenServiceRepo.findAllByQueueId(queueEntity.getId());
        return estimateServicesDuration(clientInQueueToChosenServiceEntities.stream().map(ClientInQueueToChosenServiceEntity::getServiceId).collect(Collectors.toSet()));
    }

    private Long estimateServicesDuration(Set<Long> serviceIds) {
        Set<HistoryItemEntity> historyItemEntities = historyItemRepo.findAll()
                .stream()
                .filter(historyItem -> historyItem.getEndTime() != null)
                .collect(Collectors.toSet());

        List<ServiceInHistoryItemEntity> servicesInHistoryItemEntities = serviceInHistoryItemRepo.findAll();

        long maxDuration = serviceRepo.findAllById(serviceIds)
                .stream()
                .map(ServiceEntity::getMaxDuration)
                .reduce(0L, Long::sum);

        List<Long> durations = new ArrayList<>();

        for (HistoryItemEntity historyItemEntity : historyItemEntities) {
            Set<Long> serviceIdsInHistory = servicesInHistoryItemEntities
                    .stream()
                    .filter(serviceInHistory -> Objects.equals(serviceInHistory.getHistoryItemId(), historyItemEntity.getId()))
                    .map(ServiceInHistoryItemEntity::getServiceId)
                    .collect(Collectors.toSet());

            if (serviceIdsInHistory.equals(serviceIds)) {
                long duration = historyItemEntity.getEndTime().getTime() - historyItemEntity.getStartTime().getTime();
                if (duration < maxDuration) {
                    durations.add(duration);
                }
            }
        }

        if (durations.isEmpty()) {
            return serviceRepo.findAllById(serviceIds)
                    .stream()
                    .map(ServiceEntity::getSupposedDuration)
                    .reduce(0L, Long::sum);
        } else {
            return durations
                    .stream()
                    .sorted()
                    .toList()
                    .get(durations.size() / 2);
        }
    }

    private void switchToNextQueue(Localizer localizer, Long clientId) throws DescriptionException {
        List<ClientInQueueToChosenServiceEntity> services = clientInQueueToChosenServiceRepo.findAllByClientId(clientId);
        for (ClientInQueueToChosenServiceEntity service : services) {
            clientToChosenServiceRepo.deleteByPrimaryKeyServiceId(service.getServiceId());
        }
        clientInQueueToChosenServiceRepo.deleteAllByClientId(clientId);
        clientInQueueRepo.deleteByClientId(clientId);

        HistoryItemEntity historyItemEntity = historyItemRepo.findByClientIdAndEndTimeIsNull(clientId).get();
        historyItemEntity.setEndTime(new Date());
        historyItemRepo.save(historyItemEntity);

        ClientEntity clientEntity = clientRepo.findById(clientId).get();
        if (clientToChosenServiceRepo.existsByPrimaryKeyClientId(clientId)) {
            Map<Long, Integer> serviceIdsToOrderNumbers = getServiceIdsToOrderNumbersForClient(clientId);
            distributeClient(localizer, clientEntity, clientEntity.getLocationId(), serviceIdsToOrderNumbers);
        } else {
            clientRepo.delete(clientEntity);
        }
    }

    private Map<Long, Integer> getServiceIdsToOrderNumbersForClient(Long clientId) {
        Map<Long, Integer> serviceIdsToOrderNumbers = new HashMap<>();
        List<ClientToChosenServiceEntity> services = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(clientId);
        for (ClientToChosenServiceEntity service : services) {
            serviceIdsToOrderNumbers.put(service.getPrimaryKey().getServiceId(), service.getOrderNumber());
        }
        return serviceIdsToOrderNumbers;
    }
}
