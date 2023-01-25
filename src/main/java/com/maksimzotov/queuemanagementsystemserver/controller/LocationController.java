package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.services.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.services.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.type.CreateQueueTypeRequest;
import com.maksimzotov.queuemanagementsystemserver.service.LocationService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueTypeService;
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
    private final QueueTypeService queueTypeService;

    public LocationController(
            MessageSource messageSource,
            LocationService locationService,
            ServiceService serviceService,
            QueueTypeService queueTypeService
    ) {
        super(messageSource);
        this.locationService = locationService;
        this.serviceService = serviceService;
        this.queueTypeService = queueTypeService;
    }

    @GetMapping()
    public ResponseEntity<?> getLocations(
            HttpServletRequest request,
            @RequestParam String username
    ) {
        try {
            return ResponseEntity.ok().body(locationService.getLocations(getLocalizer(request), getToken(request), username));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkHasRights(
            HttpServletRequest request,
            @RequestParam String username
    ) {
        return ResponseEntity.ok().body(locationService.checkHasRights(getToken(request), username));
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

    @PostMapping("/{location_id}/change")
    public ResponseEntity<?> changeMaxColumns(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId,
            @RequestParam("max_columns") Integer maxColumns
    ) {
        try {
            return ResponseEntity.ok().body(locationService.changeMaxColumns(getLocalizer(request), getToken(request), locationId, maxColumns));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
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

    @GetMapping("/{location_id}/service")
    public ResponseEntity<?> getServicesInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.getServicesInLocation(getLocalizer(request), getToken(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{location_id}/service/create")
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

    @DeleteMapping("/{location_id}/service/{service_id}/delete")
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

    @GetMapping("/{location_id}/sequence")
    public ResponseEntity<?> getServicesSequencesInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.getServicesSequencesInLocation(getLocalizer(request), getToken(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{location_id}/sequence/create")
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

    @DeleteMapping("/{location_id}/sequence/{services_sequence_id}/delete")
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

    @GetMapping("/{location_id}/type")
    public ResponseEntity<?> getQueueTypesInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(queueTypeService.getQueueTypesInLocation(getLocalizer(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{location_id}/type/create")
    public ResponseEntity<?> createQueueTypeInLocation(
            HttpServletRequest request,
            @RequestBody CreateQueueTypeRequest createQueueTypeRequest,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(queueTypeService.createQueueTypeInLocation(getLocalizer(request), getToken(request), locationId, createQueueTypeRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{location_id}/type/{queue_type_id}/delete")
    public ResponseEntity<?> deleteQueueTypeInLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId,
            @PathVariable("queue_type_id") Long queueTypeId
    ) {
        try {
            queueTypeService.deleteQueueTypeInLocation(getLocalizer(request), getToken(request), locationId, queueTypeId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
