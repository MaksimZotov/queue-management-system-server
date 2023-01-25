package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.type.CreateQueueTypeRequest;
import com.maksimzotov.queuemanagementsystemserver.service.QueueTypeService;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/type")
@EqualsAndHashCode(callSuper = true)
public class QueueTypeController extends BaseController {

    private final QueueTypeService queueTypeService;

    public QueueTypeController(MessageSource messageSource, QueueTypeService queueTypeService) {
        super(messageSource);
        this.queueTypeService = queueTypeService;
    }

    @GetMapping
    public ResponseEntity<?> getQueueTypesInLocation(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(queueTypeService.getQueueTypesInLocation(getLocalizer(request), getToken(request), locationId));
        }  catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createQueueTypeInLocation(
            HttpServletRequest request,
            @RequestBody CreateQueueTypeRequest createQueueTypeRequest,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(queueTypeService.createQueueTypesInLocation(getLocalizer(request), getToken(request), locationId, createQueueTypeRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteQueueTypeInLocation(
            HttpServletRequest request,
            @RequestParam("queue_type_id") Long queueTypeId,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            queueTypeService.deleteQueueTypesInLocation(getLocalizer(request), getToken(request), locationId, queueTypeId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}