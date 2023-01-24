package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.template.CreateQueueClassRequest;
import com.maksimzotov.queuemanagementsystemserver.service.QueueClassService;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/class")
@EqualsAndHashCode(callSuper = true)
public class QueueClassController extends BaseController {

    private final QueueClassService queueClassService;

    public QueueClassController(MessageSource messageSource, QueueClassService queueClassService) {
        super(messageSource);
        this.queueClassService = queueClassService;
    }

    @GetMapping
    public ResponseEntity<?> getQueueClassesInLocation(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(queueClassService.getQueueClassesInLocation(getLocalizer(request), getToken(request), locationId));
        }  catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createQueueClassInLocation(
            HttpServletRequest request,
            @RequestBody CreateQueueClassRequest createQueueClassRequest,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(queueClassService.createQueueClassesInLocation(getLocalizer(request), getToken(request), locationId, createQueueClassRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteQueueClassInLocation(
            HttpServletRequest request,
            @RequestParam("queue_class_id") Long queueClassId,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            queueClassService.deleteQueueClassesInLocation(getLocalizer(request), getToken(request), locationId, queueClassId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}