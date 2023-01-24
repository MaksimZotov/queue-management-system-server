package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.services.AddOrRemoveServicesRequest;
import com.maksimzotov.queuemanagementsystemserver.model.services.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.services.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.service.ServiceService;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/service")
@EqualsAndHashCode(callSuper = true)
public class ServiceController extends BaseController {

    private final ServiceService serviceService;

    public ServiceController(MessageSource messageSource, ServiceService serviceService) {
        super(messageSource);
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<?> getServicesInLocation(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.getServicesInLocation(getLocalizer(request), getToken(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createServiceInLocation(
            HttpServletRequest request,
            @RequestBody CreateServiceRequest createServiceRequest,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.createServiceInLocation(getLocalizer(request), getToken(request), locationId, createServiceRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteServiceInLocation(
            HttpServletRequest request,
            @RequestParam("service_id") Long serviceId,
            @RequestParam("location_id") Long locationId
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

    @PostMapping("/add")
    public ResponseEntity<?> addServicesToQueue(
            HttpServletRequest request,
            @RequestBody AddOrRemoveServicesRequest addOrRemoveServicesRequest,
            @RequestParam("queue_id") Long queueId
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.addServicesToQueue(getLocalizer(request), getToken(request), queueId, addOrRemoveServicesRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeServicesFromQueue(
            HttpServletRequest request,
            @RequestBody AddOrRemoveServicesRequest addOrRemoveServicesRequest,
            @RequestParam("queue_id") Long queueId
    ) {
        try {
            serviceService.removeServicesFromQueue(getLocalizer(request), getToken(request), queueId, addOrRemoveServicesRequest);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/sequence")
    public ResponseEntity<?> getServicesSequencesInLocation(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.getServicesSequencesInLocation(getLocalizer(request), getToken(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/sequence/create")
    public ResponseEntity<?> createServicesSequenceInLocation(
            HttpServletRequest request,
            @RequestBody CreateServicesSequenceRequest createServicesSequenceRequest,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.createServicesSequenceInLocation(getLocalizer(request), getToken(request), locationId, createServicesSequenceRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/sequence/delete")
    public ResponseEntity<?> deleteServicesSequenceInLocation(
            HttpServletRequest request,
            @RequestParam("services_sequence_id") Long servicesSequenceId,
            @RequestParam("location_id") Long locationId
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
}
