package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.service.ServicesSequenceService;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/services_sequences")
@EqualsAndHashCode(callSuper = true)
public class ServiceSequenceController extends BaseController {

    private final ServicesSequenceService servicesSequenceService;

    public ServiceSequenceController(MessageSource messageSource, ServicesSequenceService servicesSequenceService) {
        super(messageSource);
        this.servicesSequenceService = servicesSequenceService;
    }

    @GetMapping
    public ResponseEntity<?> getServicesSequencesInLocation(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(servicesSequenceService.getServicesSequencesInLocation(getLocalizer(request), locationId));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createServicesSequenceInLocation(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestBody CreateServicesSequenceRequest createServicesSequenceRequest
    ) {
        try {
            return ResponseEntity.ok().body(servicesSequenceService.createServicesSequenceInLocation(getLocalizer(request), getToken(request), locationId, createServicesSequenceRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{services_sequence_id}/delete")
    public ResponseEntity<?> deleteServicesSequenceInLocation(
            HttpServletRequest request,
            @PathVariable("services_sequence_id") Long servicesSequenceId,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            servicesSequenceService.deleteServicesSequenceInLocation(getLocalizer(request), getToken(request), locationId, servicesSequenceId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
