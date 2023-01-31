package com.trecapps.admin.controllers;

import com.trecapps.auth.models.TrecAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class PermissionsController {

    @GetMapping("/Permissions")
    ResponseEntity<List<String>> getPermissions(Authentication authentication)
    {
        TrecAuthentication trecAuth = (TrecAuthentication) authentication;

        List<String> ret = new ArrayList<>();
        for(GrantedAuthority auth: trecAuth.getAuthorities())
        {
            ret.add(auth.getAuthority());
        }
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }
}
