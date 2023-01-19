package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/rights")
@RequiredArgsConstructor
@Slf4j
public class RightsController extends BaseController {

    private final RightsService rightsService;

    @GetMapping
    public ResponseEntity<?> getRights(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(rightsService.getRights(getLocalizer(request), getToken(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }


    @PostMapping("/add")
    public ResponseEntity<?> addRights(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestParam("email") String email
    ) {
        try {
            rightsService.addRights(getLocalizer(request), getToken(request), locationId, email);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRights(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestParam("email") String email
    ) {
        try {
            rightsService.deleteRights(getLocalizer(request), getToken(request), locationId, email);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
