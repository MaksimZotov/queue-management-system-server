package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;

    @GetMapping()
    public ResponseEntity<?> getQueueStateForClient(
            @RequestParam("location_id") Long locationId
    ) {
        return ResponseEntity.ok().body(boardService.updateLocation(locationId));
    }
}
