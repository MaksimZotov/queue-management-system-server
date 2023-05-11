package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/queues")
@EqualsAndHashCode(callSuper = true)
public class QueueController extends BaseController {

    private final QueueService queueService;

    public QueueController(MessageSource messageSource, QueueService queueService) {
        super(messageSource);
        this.queueService = queueService;
    }

    @GetMapping()
    public ResponseEntity<?> getQueues(
            HttpServletRequest request,
            @RequestParam(name = "location_id") Long locationId
    ) {
        return ResponseEntity.ok().body(queueService.getQueues(getLocalizer(request), getToken(request), locationId));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createQueue(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestBody CreateQueueRequest createQueueRequest
    ) {
        try {
            return ResponseEntity.ok().body(queueService.createQueue(getLocalizer(request), getToken(request), locationId, createQueueRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{queue_id}/delete")
    public ResponseEntity<?> deleteQueue(
            HttpServletRequest request,
            @PathVariable(name = "queue_id") Long queueId
    ) {
        try {
            queueService.deleteQueue(getLocalizer(request), getToken(request), queueId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/{queue_id}/state")
    public ResponseEntity<?> getQueueState(
            HttpServletRequest request,
            @PathVariable(name = "queue_id") Long queueId
    ) {
        try {
            return ResponseEntity.ok().body(queueService.getQueueState(getLocalizer(request), getToken(request), queueId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
