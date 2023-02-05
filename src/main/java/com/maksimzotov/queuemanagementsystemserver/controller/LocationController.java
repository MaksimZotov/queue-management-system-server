package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.client.AddClientRequst;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.service.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.CreateSpecialistRequest;
import com.maksimzotov.queuemanagementsystemserver.service.ClientService;
import com.maksimzotov.queuemanagementsystemserver.service.LocationService;
import com.maksimzotov.queuemanagementsystemserver.service.SpecialistService;
import com.maksimzotov.queuemanagementsystemserver.service.ServiceService;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/locations")
@EqualsAndHashCode(callSuper = true)
public class LocationController extends BaseController {

    private final LocationService locationService;
    private final ServiceService serviceService;
    private final SpecialistService specialistService;
    private final ClientService clientService;

    public LocationController(
            MessageSource messageSource,
            LocationService locationService,
            ServiceService serviceService,
            SpecialistService specialistService,
            ClientService clientService
    ) {
        super(messageSource);
        this.locationService = locationService;
        this.serviceService = serviceService;
        this.specialistService = specialistService;
        this.clientService = clientService;
    }

    @GetMapping()
    public ResponseEntity<?> getLocations(
            HttpServletRequest request,
            @RequestParam String email
    ) {
        try {
            return ResponseEntity.ok().body(locationService.getLocations(getLocalizer(request), getToken(request), email));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkIsOwner(
            HttpServletRequest request,
            @RequestParam String email
    ) {
        return ResponseEntity.ok().body(locationService.checkIsOwner(getToken(request), email));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createLocation(
            HttpServletRequest request,
            @RequestBody CreateLocationRequest createLocationRequest
    ) {
        try {
            return ResponseEntity.ok().body(locationService.createLocation(getLocalizer(request), getToken(request), createLocationRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{location_id}/delete")
    public ResponseEntity<?> deleteLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            locationService.deleteLocation(getLocalizer(request), getToken(request), locationId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/{location_id}")
    public ResponseEntity<?> getLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(locationService.getLocation(getLocalizer(request), getToken(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/{location_id}/board")
    public ResponseEntity<?> getLocationBoard(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(locationService.getLocationBoard(getLocalizer(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{location_id}/enable")
    public ResponseEntity<?> enable(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            locationService.changeEnabledStateInLocation(getLocalizer(request), getToken(request), locationId, true);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{location_id}/disable")
    public ResponseEntity<?> disable(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            locationService.changeEnabledStateInLocation(getLocalizer(request), getToken(request), locationId, false);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/{location_id}/services")
    public ResponseEntity<?> getServicesInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        return ResponseEntity.ok().body(serviceService.getServicesInLocation(getLocalizer(request), locationId));
    }

    @PostMapping("/{location_id}/services/create")
    public ResponseEntity<?> createServiceInLocation(
            HttpServletRequest request,
            @RequestBody CreateServiceRequest createServiceRequest,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.createServiceInLocation(getLocalizer(request), getToken(request), locationId, createServiceRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{location_id}/services/{service_id}/delete")
    public ResponseEntity<?> deleteServiceInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId,
            @PathVariable("service_id") Long serviceId
    ) {
        try {
            serviceService.deleteServiceInLocation(getLocalizer(request), getToken(request), locationId, serviceId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/{location_id}/sequences")
    public ResponseEntity<?> getServicesSequencesInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        return ResponseEntity.ok().body(serviceService.getServicesSequencesInLocation(getLocalizer(request), locationId));
    }

    @PostMapping("/{location_id}/sequences/create")
    public ResponseEntity<?> createServicesSequenceInLocation(
            HttpServletRequest request,
            @RequestBody CreateServicesSequenceRequest createServicesSequenceRequest,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.createServicesSequenceInLocation(getLocalizer(request), getToken(request), locationId, createServicesSequenceRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{location_id}/sequences/{services_sequence_id}/delete")
    public ResponseEntity<?> deleteServicesSequenceInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId,
            @PathVariable("services_sequence_id") Long servicesSequenceId
    ) {
        try {
            serviceService.deleteServicesSequenceInLocation(getLocalizer(request), getToken(request), locationId, servicesSequenceId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/{location_id}/types")
    public ResponseEntity<?> getQueueTypesInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(specialistService.getQueueTypesInLocation(getLocalizer(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{location_id}/types/create")
    public ResponseEntity<?> createQueueTypeInLocation(
            HttpServletRequest request,
            @RequestBody CreateSpecialistRequest createSpecialistRequest,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(specialistService.createQueueTypeInLocation(getLocalizer(request), getToken(request), locationId, createSpecialistRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{location_id}/types/{queue_type_id}/delete")
    public ResponseEntity<?> deleteQueueTypeInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId,
            @PathVariable("queue_type_id") Long queueTypeId
    ) {
        try {
            specialistService.deleteQueueTypeInLocation(getLocalizer(request), getToken(request), locationId, queueTypeId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{location_id}/clients/add")
    public ResponseEntity<?> addClient(
            HttpServletRequest request,
            @RequestBody AddClientRequst addClientRequst,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            clientService.addClient(getLocalizer(request), locationId, addClientRequst);
            return ResponseEntity.ok().build();
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
