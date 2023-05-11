package com.maksimzotov.queuemanagementsystemserver.integration;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientToChosenServiceEntity;
import com.maksimzotov.queuemanagementsystemserver.integration.base.IntegrationTests;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.model.client.ChangeClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.client.ClientModel;
import com.maksimzotov.queuemanagementsystemserver.model.client.CreateClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationModel;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.ServicesSequenceModel;
import com.maksimzotov.queuemanagementsystemserver.model.service.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.service.ServiceModel;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServicesSequenceScenariosTests extends IntegrationTests {

    @Autowired
    private ClientService clientService;
    @Autowired
    private ServiceService serviceService;
    @Autowired
    private ServicesSequenceService servicesSequenceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private LocationService locationService;

    @Autowired
    private ClientRepo clientRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private ServiceRepo serviceRepo;
    @Autowired
    private ClientToChosenServiceRepo clientToChosenServiceRepo;
    @Autowired
    private LocationRepo locationRepo;

    @Mock
    private MessageSource messageSource;
    private Localizer localizer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TokensResponse tokens;

    @SneakyThrows
    @BeforeEach
    void beforeEach() {
        clientToChosenServiceRepo.deleteAll();
        clientRepo.deleteAll();
        serviceRepo.deleteAll();
        locationRepo.deleteAll();
        accountRepo.deleteAll();

        accountRepo.save(
                new AccountEntity(
                        1L,
                        "zotovm256@gmail.com",
                        "Test",
                        "Test",
                        passwordEncoder.encode("12345678"),
                        new Date()
                )
        );

        tokens = accountService.login(
                localizer,
                new LoginRequest(
                        "zotovm256@gmail.com" ,
                        "12345678"
                )
        );

        localizer = new Localizer(
                new Locale("ru"),
                messageSource
        );
    }

    @Test
    @SneakyThrows
    void testClientChosePreparedSequence() {
        LocationModel locationModel = locationService.createLocation(
                localizer,
                tokens.getAccess(),
                new CreateLocationRequest(
                        "Локация 1",
                        "Описание"
                )
        );

        ServiceModel firstServiceOrder1 = serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Услуга 1",
                        "Описание 1"
                )
        );
        ServiceModel secondServiceOrder2 = serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Услуга 2",
                        "Описание 2"
                )
        );
        ServiceModel thirdServiceOrder2 = serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Услуга 3 (без описания)",
                        null
                )
        );
        ServiceModel fourthServiceOrder3 = serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Услуга 4 (без описания)",
                        null
                )
        );

        Map<Long, Integer> serviceIdsToOrderNumbers = Map.ofEntries(
                entry(firstServiceOrder1.getId(), 1),
                entry(secondServiceOrder2.getId(), 2),
                entry(thirdServiceOrder2.getId(), 2),
                entry(fourthServiceOrder3.getId(), 3)
        );

        ServicesSequenceModel servicesSequence = servicesSequenceService.createServicesSequenceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServicesSequenceRequest(
                        "Цепочка",
                        "Описание",
                        serviceIdsToOrderNumbers
                )
        );

        ClientModel client = clientService.createClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateClientRequest(
                        null,
                        null,
                        servicesSequence.getId(),
                        false
                )
        );

        List<ClientToChosenServiceEntity> clientToChosenServiceEntities = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(client.getId());
        HashMap<Long, Integer> actual = new HashMap<>();
        for (ClientToChosenServiceEntity clientToChosenServiceEntity : clientToChosenServiceEntities) {
            actual.put(
                    clientToChosenServiceEntity.getPrimaryKey().getServiceId(),
                    clientToChosenServiceEntity.getOrderNumber()
            );
        }
        assertEquals(serviceIdsToOrderNumbers, actual);
    }

    @Test
    @SneakyThrows
    void testClientMovedToCustomSequence() {
        LocationModel locationModel = locationService.createLocation(
                localizer,
                tokens.getAccess(),
                new CreateLocationRequest(
                        "Локация 1",
                        "Описание"
                )
        );

        ServiceModel firstServiceOrder1 = serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Услуга 1",
                        "Описание 1"
                )
        );
        ServiceModel secondServiceOrder2 = serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Услуга 2",
                        "Описание 2"
                )
        );
        ServiceModel thirdServiceOrder2 = serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Услуга 3 (без описания)",
                        null
                )
        );
        ServiceModel fourthServiceOrder3 = serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Услуга 4 (без описания)",
                        null
                )
        );

        ClientModel client = clientService.createClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateClientRequest(
                        null,
                        List.of(firstServiceOrder1.getId()),
                        null,
                        false
                )
        );

        List<ClientToChosenServiceEntity> clientToChosenServiceEntities = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(client.getId());

        assertEquals(1, clientToChosenServiceEntities.size());
        assertEquals(firstServiceOrder1.getId(), clientToChosenServiceEntities.get(0).getPrimaryKey().getServiceId());
        assertEquals(1, clientToChosenServiceEntities.get(0).getOrderNumber());

        Map<Long, Integer> serviceIdsToOrderNumbers = Map.ofEntries(
                entry(firstServiceOrder1.getId(), 1),
                entry(secondServiceOrder2.getId(), 2),
                entry(thirdServiceOrder2.getId(), 2),
                entry(fourthServiceOrder3.getId(), 3)
        );

        clientService.changeClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                client.getId(),
                new ChangeClientRequest(
                        serviceIdsToOrderNumbers
                )
        );

        clientToChosenServiceEntities = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(client.getId());
        HashMap<Long, Integer> actual = new HashMap<>();
        for (ClientToChosenServiceEntity clientToChosenServiceEntity : clientToChosenServiceEntities) {
            actual.put(
                    clientToChosenServiceEntity.getPrimaryKey().getServiceId(),
                    clientToChosenServiceEntity.getOrderNumber()
            );
        }
        assertEquals(serviceIdsToOrderNumbers, actual);
    }
}
