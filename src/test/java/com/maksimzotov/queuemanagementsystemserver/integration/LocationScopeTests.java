package com.maksimzotov.queuemanagementsystemserver.integration;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.integration.extension.PostgreSQLExtension;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.model.client.ClientModel;
import com.maksimzotov.queuemanagementsystemserver.model.client.CreateClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationModel;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueModel;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueStateModel;
import com.maksimzotov.queuemanagementsystemserver.model.rights.AddRightsRequest;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.ServicesSequenceModel;
import com.maksimzotov.queuemanagementsystemserver.model.service.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.service.ServiceModel;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.CreateSpecialistRequest;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.SpecialistModel;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(PostgreSQLExtension.class)
@TestPropertySource(
        properties = {"spring.config.location=classpath:application-tests.yml"}
)
@DirtiesContext
public class LocationScopeTests {

    private static final String PASSWORD = "12345678";
    private static final String EMAIL = "zotovm256@gmail.com";
    private static final String RIGHTS_EMAIL = "zotovmaksim1254@gmail.com";
    private static final Long NON_EXISTING_ID = 1000L;

    @Autowired
    private ServiceService serviceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private RightsService rightsService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private SpecialistService specialistService;
    @Autowired
    private QueueService queueService;
    @Autowired
    private ServicesSequenceService servicesSequenceService;

    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private ServiceRepo serviceRepo;
    @Autowired
    private LocationRepo locationRepo;
    @Autowired
    private QueueRepo queueRepo;
    @Autowired
    private SpecialistRepo specialistRepo;
    @Autowired
    private ServiceInSpecialistRepo serviceInSpecialistRepo;
    @Autowired
    private ClientRepo clientRepo;
    @Autowired
    private ClientToChosenServiceRepo clientToChosenServiceRepo;
    @Autowired
    private ServicesSequenceRepo servicesSequenceRepo;
    @Autowired
    private ServiceInServicesSequenceRepo serviceInServicesSequenceRepo;

    @Mock
    private MessageSource messageSource;
    private Localizer localizer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TokensResponse tokens;
    private LocationModel locationModel;
    private ServiceModel firstServiceModel;
    private ServiceModel secondServiceModel;
    private ServicesSequenceModel servicesSequenceModel;
    private SpecialistModel firstSpecialistModel;
    private SpecialistModel secondSpecialistModel;
    private QueueModel firstQueueModel;
    private QueueModel secondQueueModel;
    private ClientModel confirmedClientModel;
    private ClientModel nonConfirmedClientModel;
    private AccountEntity accountEntity;

    @BeforeEach
    void beforeEach() {
        clientToChosenServiceRepo.deleteAll();
        clientRepo.deleteAll();
        queueRepo.deleteAll();
        serviceInSpecialistRepo.deleteAll();
        specialistRepo.deleteAll();
        serviceInServicesSequenceRepo.deleteAll();
        servicesSequenceRepo.deleteAll();
        serviceRepo.deleteAll();
        locationRepo.deleteAll();
        accountRepo.deleteAll();

        localizer = new Localizer(
                new Locale("ru"),
                messageSource
        );

        accountEntity = accountRepo.save(
                new AccountEntity(
                        1L,
                        EMAIL,
                        "Test",
                        "Test",
                        passwordEncoder.encode(PASSWORD),
                        new Date()
                )
        );

        tokens = assertDoesNotThrow(() -> accountService.login(
                localizer,
                new LoginRequest(
                        EMAIL,
                        PASSWORD
                )
        ));

        locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                tokens.getAccess(),
                new CreateLocationRequest(
                        "Location 1",
                        "Description"
                )
        ));

        firstServiceModel = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 1",
                        null
                )
        ));
        secondServiceModel = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 2",
                        null
                )
        ));

        servicesSequenceModel = assertDoesNotThrow(() -> servicesSequenceService.createServicesSequenceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServicesSequenceRequest(
                        "Services sequence",
                        "Description",
                        Map.ofEntries(
                                entry(firstServiceModel.getId(), 1),
                                entry(secondServiceModel.getId(), 1)
                        )
                )
        ));

        confirmedClientModel = assertDoesNotThrow(() -> clientService.createClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateClientRequest(
                        null,
                        List.of(firstServiceModel.getId(), secondServiceModel.getId()),
                        null,
                        false
                )
        ));

        nonConfirmedClientModel = assertDoesNotThrow(() -> clientService.createClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateClientRequest(
                        "+79193058732",
                        List.of(firstServiceModel.getId(), secondServiceModel.getId()),
                        null,
                        true
                )
        ));

        firstSpecialistModel = assertDoesNotThrow(() -> specialistService.createSpecialistInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateSpecialistRequest(
                        "Specialist 1",
                        null,
                        List.of(firstServiceModel.getId())
                )
        ));
        secondSpecialistModel = assertDoesNotThrow(() -> specialistService.createSpecialistInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateSpecialistRequest(
                        "Specialist 2",
                        null,
                        List.of(secondServiceModel.getId())
                )
        ));

        firstQueueModel = assertDoesNotThrow(() -> queueService.createQueue(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateQueueRequest(
                        firstSpecialistModel.getId(),
                        "Queue 1",
                        null
                )
        ));
        secondQueueModel = assertDoesNotThrow(() -> queueService.createQueue(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateQueueRequest(
                        secondSpecialistModel.getId(),
                        "Queue 2",
                        null
                )
        ));
    }

    @Test
    void testCreateLocationWithBlankName() {
        assertThrows(DescriptionException.class, () -> locationService.createLocation(
                localizer,
                tokens.getAccess(),
                new CreateLocationRequest(
                      "",
                      null
                )
        ));
    }

    @Test
    void testGetLocation() {
        assertThrows(DescriptionException.class, () -> locationService.getLocation(
                localizer,
                NON_EXISTING_ID
        ));
        LocationModel localLocationModel = assertDoesNotThrow(() -> locationService.getLocation(
                localizer,
                locationModel.getId()
        ));
        assertEquals(locationModel, localLocationModel);
    }

    @Test
    void testGetLocations() {
        assertThrows(DescriptionException.class, () -> locationService.getLocations(
                localizer,
                NON_EXISTING_ID
        ));
        List<LocationModel> locations = assertDoesNotThrow(() -> locationService.getLocations(
                localizer,
                accountEntity.getId()
        )).getResults();
        assertEquals(List.of(locationModel), locations);
    }

    @Test
    void testDeleteLocation() {
        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        RIGHTS_EMAIL,
                        RightsStatusEntity.Status.ADMINISTRATOR.name()
                )
        ));

        assertThrows(DescriptionException.class, () -> locationService.deleteLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId()
        ));

        assertFalse(assertDoesNotThrow(() -> clientRepo.findAllByLocationId(locationModel.getId())).isEmpty());
        assertDoesNotThrow(() -> clientService.deleteClientInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                confirmedClientModel.getId()
        ));
        assertDoesNotThrow(() -> clientService.deleteClientInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                nonConfirmedClientModel.getId()
        ));
        assertTrue(assertDoesNotThrow(() -> clientRepo.findAllByLocationId(locationModel.getId())).isEmpty());

        assertFalse(
                assertDoesNotThrow(() -> rightsService.getRights(
                    localizer,
                    tokens.getAccess(),
                    locationModel.getId()
                )).getResults().isEmpty()
        );
        assertFalse(
                assertDoesNotThrow(() -> queueService.getQueues(
                        localizer,
                        locationModel.getId()
                )).getResults().isEmpty()
        );
        assertFalse(
                assertDoesNotThrow(() -> specialistService.getSpecialistsInLocation(
                        localizer,
                        locationModel.getId()
                )).getResults().isEmpty()
        );
        assertFalse(
                assertDoesNotThrow(() -> servicesSequenceService.getServicesSequencesInLocation(
                        localizer,
                        locationModel.getId()
                )).getResults().isEmpty()
        );
        assertFalse(
                assertDoesNotThrow(() -> serviceService.getServicesInLocation(
                        localizer,
                        locationModel.getId()
                )).getResults().isEmpty()
        );

        assertDoesNotThrow(() -> locationService.deleteLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId()
        ));

        assertThrows(DescriptionException.class, () -> rightsService.getRights(
                localizer,
                tokens.getAccess(),
                locationModel.getId()
        ));
        assertThrows(DescriptionException.class, () -> queueService.getQueues(
                localizer,
                locationModel.getId()
        ));
        assertThrows(DescriptionException.class, () -> specialistService.getSpecialistsInLocation(
                localizer,
                locationModel.getId()
        ));
        assertThrows(DescriptionException.class, () -> servicesSequenceService.getServicesSequencesInLocation(
                localizer,
                locationModel.getId()
        ));
        assertThrows(DescriptionException.class, () -> serviceService.getServicesInLocation(
                localizer,
                locationModel.getId()
        ));
    }

    @Test
    void testDeleteServiceInLocation() {
        assertThrows(DescriptionException.class, () -> serviceService.deleteServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                firstServiceModel.getId()
        ));

        assertFalse(clientRepo.findAllByLocationId(locationModel.getId()).isEmpty());
        assertDoesNotThrow(() -> clientService.deleteClientInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                confirmedClientModel.getId()
        ));
        assertDoesNotThrow(() -> clientService.deleteClientInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                nonConfirmedClientModel.getId()
        ));
        assertTrue(clientRepo.findAllByLocationId(locationModel.getId()).isEmpty());

        assertTrue(
                assertDoesNotThrow(
                        () -> specialistService.getSpecialistsInLocation(
                                localizer,
                                locationModel.getId()
                        )
                ).getResults().contains(firstSpecialistModel)
        );
        assertTrue(
                assertDoesNotThrow(
                        () -> queueService.getQueues(
                                localizer,
                                locationModel.getId()
                        )
                ).getResults().contains(firstQueueModel)
        );

        assertThrows(DescriptionException.class, () -> specialistService.deleteSpecialistInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                firstSpecialistModel.getId()
        ));
        assertDoesNotThrow(() -> queueService.deleteQueue(
                localizer,
                tokens.getAccess(),
                firstQueueModel.getId()
        ));

        assertDoesNotThrow(() -> specialistService.deleteSpecialistInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                firstSpecialistModel.getId()
        ));

        assertFalse(
                assertDoesNotThrow(
                        () -> specialistService.getSpecialistsInLocation(
                                localizer,
                                locationModel.getId()
                        )
                ).getResults().contains(firstSpecialistModel)
        );
        assertFalse(
                assertDoesNotThrow(
                        () -> queueService.getQueues(
                                localizer,
                                locationModel.getId()
                        )
                ).getResults().contains(firstQueueModel)
        );

        assertThrows(DescriptionException.class, () -> serviceService.deleteServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                firstServiceModel.getId()
        ));

        assertTrue(
                assertDoesNotThrow(
                        () -> servicesSequenceService.getServicesSequencesInLocation(
                                localizer,
                                locationModel.getId()
                        )
                ).getResults().contains(servicesSequenceModel)
        );

        assertDoesNotThrow(() -> servicesSequenceService.deleteServicesSequenceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                servicesSequenceModel.getId()
        ));

        assertFalse(
                assertDoesNotThrow(
                        () -> servicesSequenceService.getServicesSequencesInLocation(
                                localizer,
                                locationModel.getId()
                        )
                ).getResults().contains(servicesSequenceModel)
        );

        assertTrue(
                assertDoesNotThrow(
                        () -> serviceService.getServicesInLocation(
                                localizer,
                                locationModel.getId()
                        )
                ).getResults().contains(firstServiceModel)
        );

        assertDoesNotThrow(() -> serviceService.deleteServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                firstServiceModel.getId()
        ));

        assertFalse(
                assertDoesNotThrow(
                        () -> serviceService.getServicesInLocation(
                                localizer,
                                locationModel.getId()
                        )
                ).getResults().contains(firstServiceModel)
        );
    }

    @Test
    void testGetQueueState() {
        assertThrows(DescriptionException.class, () -> queueService.getQueueState(
                localizer,
                tokens.getAccess(),
                NON_EXISTING_ID
        ));
        QueueStateModel actual = assertDoesNotThrow(() -> queueService.getQueueState(
                localizer,
                tokens.getAccess(),
                firstQueueModel.getId()
        ));
        QueueStateModel expected = new QueueStateModel(
                firstQueueModel.getId(),
                locationModel.getId(),
                firstQueueModel.getName(),
                firstQueueModel.getDescription(),
                EMAIL,
                List.of(firstServiceModel.getId())
        );
        assertEquals(expected, actual);
    }
}
