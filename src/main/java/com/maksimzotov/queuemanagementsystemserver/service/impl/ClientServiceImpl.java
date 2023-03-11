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

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final AccountService accountService;
    private final LocationService locationService;
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
    private final ServiceInServicesSequenceRepo serviceInServicesSequenceRepo;
    private final ServiceInSpecialistRepo serviceInSpecialistRepo;
    private final ClientInQueueToChosenServiceRepo clientInQueueToChosenServiceRepo;
    private final ClientToChosenServiceRepo clientToChosenServiceRepo;
    private final Integer confirmationTimeInSeconds;

    public ClientServiceImpl(
            AccountService accountService,
            LocationService locationService,
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
            ServiceInServicesSequenceRepo serviceInServicesSequenceRepo,
            ServiceInSpecialistRepo serviceInSpecialistRepo,
            ClientInQueueToChosenServiceRepo clientInQueueToChosenServiceRepo,
            ClientToChosenServiceRepo clientToChosenServiceRepo,
            @Value("${app.registration.confirmationtime.join}")  Integer confirmationTimeInSeconds
    ) {
        this.accountService = accountService;
        this.locationService = locationService;
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
    public void callClient(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }

        QueueEntity queueEntity = queue.get();
        if (queueEntity.getClientId() != null) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_CONTAINS_CLIENTS));
        }

        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }


        ClientEntity clientEntity = client.get();
        Long locationId = queueEntity.getLocationId();

        Map<Long, Integer> serviceIdsToOrderNumbers = getServiceIdsToOrderNumbersForClient(clientId);

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

        List<ServiceInSpecialistEntity> serviceInSpecialistEntities = serviceInSpecialistRepo.findAllBySpecialistId(queueEntity.getSpecialistId());
        List<Long> serviceIdsInSpecialist = serviceInSpecialistEntities.stream().map(ServiceInSpecialistEntity::getServiceId).toList();
        List<Long> serviceIdsWithKnownQueue = serviceIds.stream().distinct().filter(serviceIdsInSpecialist::contains).toList();
        for (Long serviceId : serviceIdsWithKnownQueue) {
            clientInQueueToChosenServiceRepo.save(
                    new ClientInQueueToChosenServiceEntity(
                            clientEntity.getId(),
                            serviceId,
                            queueEntity.getId(),
                            locationId
                    )
            );
        }

        queueEntity.setClientId(clientId);
        queueRepo.save(queueEntity);

        mailService.send(
                clientEntity.getEmail(),
                localizer.getMessage(Message.YOUR_STATUS_IN_QUEUE),
                localizer.getMessageForClientCheckStatus(
                        queueEntity.getName(),
                        clientEntity.toString(),
                        getLinkForClient(localizer, clientEntity, locationId)
                )
        );

        locationService.updateLocationState(locationId);
    }

    @Override
    public void returnClient(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }

        QueueEntity queueEntity = queue.get();
        queueEntity.setClientId(null);
        queueRepo.save(queueEntity);

        clientInQueueToChosenServiceRepo.deleteAllByClientId(clientId);

        locationService.updateLocationState(queueEntity.getLocationId());
    }

    @Override
    public QueueStateForClient getQueueStateForClient(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException {
        ClientEntity clientEntity = checkAccessKey(localizer, clientId, accessKey);
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByClientId(clientId);
        if (clientInQueue.isEmpty()) {
            return QueueStateForClient.toModel(clientEntity);
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        QueueStateModel queueStateModel = queueService.getCurrentQueueState(clientInQueueEntity.getQueueId());
        return QueueStateForClient.toModel(queueStateModel, clientEntity);
    }

    @Override
    public QueueStateForClient confirmAccessKeyByClient(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException {
        ClientEntity clientEntity = checkAccessKey(localizer, clientId, accessKey);
        if (Objects.equals(clientEntity.getStatus(), ClientStatusEntity.Status.CONFIRMED.name())) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_ALREADY_CONFIRMED));
        }

        List<ClientEntity> clientEntities = clientRepo.findAllByLocationId(clientEntity.getLocationId());

        clientEntity.setStatus(ClientStatusEntity.Status.CONFIRMED.name());
        clientEntity.setWaitTimestamp(new Date());
        clientEntity.setCode(CodeGenerator.generateCodeInLocation(clientEntities.stream().map(ClientEntity::getCode).toList()));

        clientRepo.save(clientEntity);
        locationService.updateLocationState(clientEntity.getLocationId());

        return getQueueStateForClient(localizer, clientId, accessKey);
    }

    @Override
    public QueueStateForClient leaveByClient(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException {
        checkAccessKey(localizer, clientId, accessKey);
        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        clientInQueueRepo.deleteByClientId(clientId);
        clientToChosenServiceRepo.deleteByPrimaryKeyClientId(clientId);
        clientInQueueToChosenServiceRepo.deleteAllByClientId(clientId);
        clientRepo.deleteById(clientId);
        locationService.updateLocationState(clientId);
        return getQueueStateForClient(localizer, clientId, accessKey);
    }

    @Override
    public void serveClientInQueueByEmployee(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);

        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(clientId);
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_STAND_IN_QUEUE));
        }

        clientInQueueRepo.deleteById(clientId);

        List<ClientInQueueToChosenServiceEntity> services = clientInQueueToChosenServiceRepo.findAllByClientId(clientId);
        for (ClientInQueueToChosenServiceEntity service : services) {
            clientToChosenServiceRepo.deleteByPrimaryKeyServiceId(service.getServiceId());
        }
        clientInQueueToChosenServiceRepo.deleteAllByClientId(clientId);
        clientInQueueRepo.deleteByClientId(clientId);
        if (!clientToChosenServiceRepo.existsByPrimaryKeyClientId(clientId)) {
            clientRepo.deleteById(clientId);
        } else {
            Optional<ClientEntity> client = clientRepo.findById(clientId);
            if (client.isPresent()) {
                ClientEntity clientEntity = client.get();
                clientEntity.setWaitTimestamp(new Date());
                clientRepo.save(clientEntity);
            }
        }

        locationService.updateLocationState(clientId);
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
    public void deleteClientInLocation(Localizer localizer, String accessToken, Long locationId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId);
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByClientId(clientId);
        if (clientInQueue.isPresent()) {
            ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
            clientInQueueEntity.setClientId(null);
            clientInQueueRepo.save(clientInQueueEntity);
        }
        clientInQueueRepo.deleteByClientId(clientId);
        clientToChosenServiceRepo.deleteByPrimaryKeyClientId(clientId);
        clientInQueueToChosenServiceRepo.deleteAllByClientId(clientId);
        clientRepo.deleteById(clientId);
        locationService.updateLocationState(locationId);
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

    private ClientEntity checkAccessKey(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException {
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
        return Constants.CLIENT_URL +
                "/client?client_id=" +
                clientEntity.getId() +
                "&access_key=" +
                clientEntity.getAccessKey();
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
                        null,
                        CodeGenerator.generateAccessKey(),
                        ClientStatusEntity.Status.RESERVED.name(),
                        null
                )
        );

        for (Map.Entry<Long, Integer> serviceIdToOrderNumber : serviceIdsToOrderNumbers.entrySet()) {
            clientToChosenServiceRepo.save(
                    new ClientToChosenServiceEntity(
                            new ClientToChosenServiceEntity.PrimaryKey(
                                    clientEntity.getId(),
                                    serviceIdToOrderNumber.getKey(),
                                    locationId
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

    private Map<Long, Integer> getServiceIdsToOrderNumbersForClient(Long clientId) {
        Map<Long, Integer> serviceIdsToOrderNumbers = new HashMap<>();
        List<ClientToChosenServiceEntity> services = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(clientId);
        for (ClientToChosenServiceEntity service : services) {
            serviceIdsToOrderNumbers.put(service.getPrimaryKey().getServiceId(), service.getOrderNumber());
        }
        return serviceIdsToOrderNumbers;
    }
}
