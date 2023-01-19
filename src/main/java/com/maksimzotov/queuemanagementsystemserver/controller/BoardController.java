package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
@Slf4j
public class BoardController extends BaseController {

    private final BoardService boardService;

    @GetMapping()
    public ResponseEntity<?> getLocationBoard(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(boardService.getLocationBoard(getLocalizer(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
