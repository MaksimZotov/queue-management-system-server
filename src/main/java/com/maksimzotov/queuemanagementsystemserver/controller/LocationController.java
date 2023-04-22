package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.client.AddClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.client.ChangeClientRequest;
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
            @RequestParam("account_id") Long accountId
    ) {
        try {
            return ResponseEntity.ok().body(locationService.getLocations(getLocalizer(request), getToken(request), accountId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkIsOwner(
            HttpServletRequest request,
            @RequestParam("account_id") Long accountId
    ) {
        try {
            return ResponseEntity.ok().body(locationService.checkIsOwner(getLocalizer(request), getToken(request), accountId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
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

    @GetMapping("/{location_id}/state")
    public ResponseEntity<?> getLocationState(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(locationService.getLocationState(getLocalizer(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/{location_id}/services")
    public ResponseEntity<?> getServicesInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.getServicesInLocation(getLocalizer(request), locationId));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
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
        try {
            return ResponseEntity.ok().body(serviceService.getServicesSequencesInLocation(getLocalizer(request), locationId));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
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

    @GetMapping("/{location_id}/specialists")
    public ResponseEntity<?> getSpecialistsInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        return ResponseEntity.ok().body(specialistService.getSpecialistsInLocation(getLocalizer(request), locationId));
    }

    @PostMapping("/{location_id}/specialists/create")
    public ResponseEntity<?> createSpecialistInLocation(
            HttpServletRequest request,
            @RequestBody CreateSpecialistRequest createSpecialistRequest,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(specialistService.createSpecialistInLocation(getLocalizer(request), getToken(request), locationId, createSpecialistRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{location_id}/specialists/{queue_type_id}/delete")
    public ResponseEntity<?> deleteSpecialistInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId,
            @PathVariable("queue_type_id") Long specialistId
    ) {
        try {
            specialistService.deleteSpecialistInLocation(getLocalizer(request), getToken(request), locationId, specialistId);
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
            @RequestBody AddClientRequest addClientRequest,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(clientService.addClient(getLocalizer(request), getToken(request), locationId, addClientRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{location_id}/clients/change")
    public ResponseEntity<?> changeClient(
            HttpServletRequest request,
            @RequestBody ChangeClientRequest changeClientRequest,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            clientService.changeClient(getLocalizer(request), getToken(request), locationId, changeClientRequest);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
