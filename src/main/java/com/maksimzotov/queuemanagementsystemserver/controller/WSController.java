package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.model.stub.Message;
import com.maksimzotov.queuemanagementsystemserver.service.WSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WSController {

    @Autowired
    private WSService service;

    @PostMapping("/send-message/{id}")
    public void sendMessage(@RequestBody Message message, @PathVariable Integer id) {
        service.notifyFrontend(message.getMessageContent(), id);
    }
}