package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        try {
            return ResponseEntity.ok().body(boardService.updateLocation(locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
