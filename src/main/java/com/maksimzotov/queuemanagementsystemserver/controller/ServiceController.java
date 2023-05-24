package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.service.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.service.ServiceService;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/services")
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
            @RequestParam(value = "location_id", required = false) Long locationId,
            @RequestParam(value = "queue_id", required = false) Long queueId,
            @RequestParam(value = "specialist_id", required = false) Long specialistId,
            @RequestParam(value = "services_sequence_id", required = false) Long servicesSequenceId
    ) {
        try {
            int count = 0;
            count += locationId != null ? 1 : 0;
            count += queueId != null ? 1 : 0;
            count += specialistId != null ? 1 : 0;
            count += servicesSequenceId != null ? 1 : 0;
            if (count != 1) {
                return ResponseEntity.badRequest().body(new ErrorResult(getLocalizer(request).getMessage(Message.INCORRECT_REQUEST)));
            }
            if (locationId != null) {
                return ResponseEntity.ok().body(serviceService.getServicesInLocation(getLocalizer(request), locationId));
            }
            if (queueId != null) {
                return ResponseEntity.ok().body(serviceService.getServicesInQueue(getLocalizer(request), queueId));
            }
            if (specialistId != null) {
                return ResponseEntity.ok().body(serviceService.getServicesInSpecialist(getLocalizer(request), specialistId));
            }
            return ResponseEntity.ok().body(serviceService.getServicesInServicesSequence(getLocalizer(request), servicesSequenceId));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createServiceInLocation(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestBody CreateServiceRequest createServiceRequest
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.createServiceInLocation(getLocalizer(request), getToken(request), locationId, createServiceRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{service_id}/delete")
    public ResponseEntity<?> deleteService(
            HttpServletRequest request,
            @PathVariable("service_id") Long serviceId,
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
}