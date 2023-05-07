package com.maksimzotov.queuemanagementsystemserver.integration;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.integration.base.IntegrationTests;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationModel;
import com.maksimzotov.queuemanagementsystemserver.model.rights.AddRightsRequest;
import com.maksimzotov.queuemanagementsystemserver.model.service.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.LocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RightsRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ServiceRepo;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class RightsScenariosTests extends IntegrationTests {

    @Autowired
    private ServiceService serviceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private RightsService rightsService;

    @Autowired
    private RightsRepo rightsRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private ServiceRepo serviceRepo;
    @Autowired
    private LocationRepo locationRepo;

    @Mock
    private MessageSource messageSource;
    private Localizer localizer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TokensResponse firstTokens;
    private TokensResponse secondTokens;

    @SneakyThrows
    @BeforeEach
    void beforeEach() {
        rightsRepo.deleteAll();
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
        accountRepo.save(
                new AccountEntity(
                        2L,
                        "zotovmaksim1254@gmail.com",
                        "Test",
                        "Test",
                        passwordEncoder.encode("12345678"),
                        new Date()
                )
        );

        firstTokens = accountService.login(
                localizer,
                new LoginRequest(
                        "zotovm256@gmail.com" ,
                        "12345678"
                )
        );
        secondTokens = accountService.login(
                localizer,
                new LoginRequest(
                        "zotovmaksim1254@gmail.com" ,
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
    void testAddRightsByAccountWithoutRights() {
        LocationModel locationModel = locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Локация 1" ,
                        "Описание"
                )
        );
        assertThrows(DescriptionException.class, () -> rightsService.addRights(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        "zotovmaksim1254@gmail.com",
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
    }

    @Test
    @SneakyThrows
    void testCreateServiceByAccountWithoutRights() {
        LocationModel locationModel = locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Локация 1" ,
                        "Описание"
                )
        );
        assertThrows(DescriptionException.class, () -> serviceService.createServiceInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Услуга 1",
                        "Описание"
                )
        ));
    }

    @Test
    @SneakyThrows
    void testCreateServiceByAccountWithRights() {
        LocationModel locationModel = locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                       "Локация 1" ,
                        "Описание"
                )
        );
        rightsService.addRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        "zotovmaksim1254@gmail.com",
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        );
        serviceService.createServiceInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Услуга 1",
                        "Описание"
                )
        );
    }
}
