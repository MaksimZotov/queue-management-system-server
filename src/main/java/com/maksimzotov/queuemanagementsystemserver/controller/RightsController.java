package com.maksimzotov.queuemanagementsystemserver.controller;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.service.CurrentAccountService;
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
public class RightsController {

    private final CurrentAccountService currentAccountService;

    private final RightsService rightsService;


    @GetMapping
    public ResponseEntity<?> getRights(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(
                    currentAccountService.handleRequestFromCurrentAccount(
                            request,
                            username -> rightsService.getRights(username, locationId)
                    )
            );
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Аккаунт не авторизован"));
        }  catch (TokenExpiredException ex) {
            return ResponseEntity.status(401).body(new ErrorResult("Время действия токена истекло"));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/add")
    public ResponseEntity<?> addRights(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestParam("email") String email
    ) {
        try {
            currentAccountService.handleRequestFromCurrentAccountNoReturn(
                    request,
                    username -> rightsService.addRights(username, locationId, email)
            );
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Аккаунт не авторизован"));
        }  catch (TokenExpiredException ex) {
            return ResponseEntity.status(401).body(new ErrorResult("Время действия токена истекло"));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRights(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestParam("email") String email
    ) {
        try {
            currentAccountService.handleRequestFromCurrentAccountNoReturn(
                    request,
                    username -> rightsService.deleteRights(username, locationId, email)
            );
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Аккаунт не авторизован"));
        }  catch (TokenExpiredException ex) {
            return ResponseEntity.status(401).body(new ErrorResult("Время действия токена истекло"));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
