package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/queues")
@RequiredArgsConstructor
@Slf4j
public class ClientController extends BaseController {

    private final ClientService clientService;

    @GetMapping("/{queue_id}/client")
    public ResponseEntity<?> getQueueStateForClient(
            @PathVariable("queue_id") Long queueId,
            @RequestParam String email,
            @RequestParam("access_key") String accessKey
    ) {
        return ResponseEntity.ok().body(clientService.getQueueStateForClient(queueId, email, accessKey));
    }

    @PostMapping("/{queue_id}/client/join")
    public ResponseEntity<?> joinQueue(
            HttpServletRequest request,
            @PathVariable("queue_id") Long queueId,
            @RequestBody JoinQueueRequest joinQueueRequest
    ) {
        try {
            return ResponseEntity.ok().body(clientService.joinQueue(getLocalizer(request), queueId, joinQueueRequest));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{queue_id}/client/rejoin")
    public ResponseEntity<?> rejoinQueue(
            HttpServletRequest request,
            @PathVariable("queue_id") Long queueId,
            @RequestParam String email
    ) {
        try {
            return ResponseEntity.ok().body(clientService.rejoinQueue(getLocalizer(request), queueId, email));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{queue_id}/client/confirm")
    public ResponseEntity<?> confirmCode(
            HttpServletRequest request,
            @PathVariable("queue_id") Long queueId,
            @RequestParam String email,
            @RequestParam String code
    ) {
        try {
            return ResponseEntity.ok().body(clientService.confirmCode(getLocalizer(request), queueId, email, code));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{queue_id}/client/leave")
    public ResponseEntity<?> leaveQueue(
            HttpServletRequest request,
            @PathVariable("queue_id") Long queueId,
            @RequestParam String email,
            @RequestParam("access_key") String accessKey
    ) {
        try {
            return ResponseEntity.ok().body(clientService.leaveQueue(getLocalizer(request), queueId, email, accessKey));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
