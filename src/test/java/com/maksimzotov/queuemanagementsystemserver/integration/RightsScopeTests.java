package com.maksimzotov.queuemanagementsystemserver.integration;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.integration.extension.PostgreSQLExtension;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationModel;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.rights.AddRightsRequest;
import com.maksimzotov.queuemanagementsystemserver.model.rights.RightsModel;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.CreateServicesSequenceRequest;
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
public class RightsScopeTests {
    
    private static final String PASSWORD = "12345678";
    private static final String FIRST_EMAIL = "zotovm256@gmail.com";
    private static final String SECOND_EMAIL = "zotovmaksim1254@gmail.com";
    private static final String THIRD_EMAIL = "test1234567tyiefse4@gmail.com";

    @Autowired
    private ServiceService serviceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private RightsService rightsService;
    @Autowired
    private SpecialistService specialistService;
    @Autowired ServicesSequenceService servicesSequenceService;
    @Autowired QueueService queueService;

    @Autowired
    private RightsRepo rightsRepo;
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
    private ServicesSequenceRepo servicesSequenceRepo;
    @Autowired
    private ServiceInServicesSequenceRepo serviceInServicesSequenceRepo;

    @Mock
    private MessageSource messageSource;
    private Localizer localizer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AccountEntity firstAccountEntity;
    private AccountEntity secondAccountEntity;
    private TokensResponse firstTokens;
    private TokensResponse secondTokens;

    @BeforeEach
    void beforeEach() {
        rightsRepo.deleteAll();
        queueRepo.deleteAll();
        serviceInSpecialistRepo.deleteAll();
        specialistRepo.deleteAll();
        serviceInServicesSequenceRepo.deleteAll();
        servicesSequenceRepo.deleteAll();
        serviceRepo.deleteAll();
        locationRepo.deleteAll();
        accountRepo.deleteAll();

        firstAccountEntity = accountRepo.save(
                new AccountEntity(
                        null,
                        FIRST_EMAIL,
                        "Test",
                        "Test",
                        passwordEncoder.encode(PASSWORD),
                        new Date()
                )
        );
        secondAccountEntity = accountRepo.save(
                new AccountEntity(
                        null,
                        SECOND_EMAIL,
                        "Test",
                        "Test",
                        passwordEncoder.encode(PASSWORD),
                        new Date()
                )
        );

        firstTokens = assertDoesNotThrow(() -> accountService.login(
                localizer,
                new LoginRequest(
                        FIRST_EMAIL,
                        PASSWORD
                )
        ));
        secondTokens = assertDoesNotThrow(() -> accountService.login(
                localizer,
                new LoginRequest(
                        SECOND_EMAIL,
                        PASSWORD
                )
        ));

        localizer = new Localizer(
                new Locale("ru"),
                messageSource
        );
    }

    @Test
    void testAddRightsByAccountWithoutRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Location 1",
                        "Description"
                )
        ));
        assertThrows(DescriptionException.class, () -> rightsService.addRights(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        SECOND_EMAIL,
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
    }

    @Test
    void testPerformOperationsByAccountWithoutRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Location 1",
                        "Description"
                )
        ));
        assertThrows(DescriptionException.class, () -> serviceService.createServiceInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        null,
                        null
                )
        ));
        assertThrows(DescriptionException.class, () -> servicesSequenceService.createServicesSequenceInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateServicesSequenceRequest(
                        null,
                        null,
                        null
                )
        ));
        assertThrows(DescriptionException.class, () -> specialistService.createSpecialistInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateSpecialistRequest(
                        null,
                        null,
                        null
                )
        ));
        assertThrows(DescriptionException.class, () -> queueService.createQueue(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateQueueRequest(
                        null,
                        null,
                        null
                )
        ));
    }

    @Test
    void testPerformOperationsByAccountWithRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                       "Location 1",
                        "Description"
                )
        ));
        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        SECOND_EMAIL,
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
        ServiceModel serviceModel = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 1",
                        "Description"
                )
        ));
        assertDoesNotThrow(() -> servicesSequenceService.createServicesSequenceInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateServicesSequenceRequest(
                        "Services Sequence 1",
                        "Description",
                        Map.ofEntries(
                                entry(serviceModel.getId(), 1)
                        )
                )
        ));
        SpecialistModel specialistModel = assertDoesNotThrow(() -> specialistService.createSpecialistInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateSpecialistRequest(
                        "Specialist 1",
                        "Description",
                        List.of(serviceModel.getId())
                )
        ));
        assertDoesNotThrow(() -> queueService.createQueue(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateQueueRequest(
                        specialistModel.getId(),
                        "Queue 1",
                        "Description"
                )
        ));
    }

    @Test
    void testAddRightsByAccountWithEmployeeRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Location 1",
                        "Description"
                )
        ));
        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        SECOND_EMAIL,
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
        assertThrows(DescriptionException.class, () -> rightsService.addRights(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        THIRD_EMAIL,
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
    }

    @Test
    void testAddRightsByAccountWithAdministratorRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Location 1",
                        "Description"
                )
        ));
        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        SECOND_EMAIL,
                        RightsStatusEntity.Status.ADMINISTRATOR.name()
                )
        ));
        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        THIRD_EMAIL,
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
    }

    @Test
    void testAddRightsByAccountWithRemovedAdministratorRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Location 1",
                        "Description"
                )
        ));
        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        SECOND_EMAIL,
                        RightsStatusEntity.Status.ADMINISTRATOR.name()
                )
        ));

        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        THIRD_EMAIL,
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));

        List<String> rightsModelsBefore = assertDoesNotThrow(() -> rightsService.getRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId()
        )).getResults()
                .stream()
                .map(RightsModel::getEmail)
                .toList();
        assertTrue(rightsModelsBefore.contains(THIRD_EMAIL));
        assertTrue(rightsModelsBefore.contains(SECOND_EMAIL));

        assertDoesNotThrow(() -> rightsService.deleteRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                THIRD_EMAIL
        ));
        assertDoesNotThrow(() -> rightsService.deleteRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                SECOND_EMAIL
        ));

        assertTrue(
                assertDoesNotThrow(() -> rightsService.getRights(
                        localizer,
                        firstTokens.getAccess(),
                        locationModel.getId()
                )).getResults().isEmpty()
        );

        assertThrows(DescriptionException.class, () -> rightsService.addRights(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        THIRD_EMAIL,
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
    }
}
