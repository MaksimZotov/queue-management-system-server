package com.maksimzotov.queuemanagementsystemserver.controller;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.service.CurrentAccountService;
import com.maksimzotov.queuemanagementsystemserver.service.RulesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/rules")
@RequiredArgsConstructor
@Slf4j
public class RulesController {

    private final CurrentAccountService currentAccountService;

    private final RulesService rulesService;


    @GetMapping
    public ResponseEntity<?> getRules(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(
                    currentAccountService.handleRequestFromCurrentAccount(
                            request,
                            username -> rulesService.getRules(username, locationId)
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
    public ResponseEntity<?> addRules(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestParam("email") String email
    ) {
        try {
            currentAccountService.handleRequestFromCurrentAccountNoReturn(
                    request,
                    username -> rulesService.addRules(username, locationId, email)
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
    public ResponseEntity<?> deleteRules(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestParam("email") String email
    ) {
        try {
            currentAccountService.handleRequestFromCurrentAccountNoReturn(
                    request,
                    username -> rulesService.deleteRules(username, locationId, email)
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
