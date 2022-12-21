package com.trecapps.admin.controllers;

import com.trecapps.admin.models.PermissionRequest;
import com.trecapps.admin.services.AccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Access")
public class AccessController {

    @Autowired
    AccessService accessService;

    private ResponseEntity<String> getResponse(String response)
    {
        String[] pieces = response.split("[:]");
        return new ResponseEntity<>(pieces[1], HttpStatus.valueOf(Integer.valueOf(pieces[0])));
    }

    @PostMapping(value = "/Grant", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> grant(RequestEntity<PermissionRequest> request)
    {
        PermissionRequest permissionRequest = request.getBody();
        return getResponse(accessService.grant(permissionRequest.getUserId(), permissionRequest.getPermission()));
    }

    @PostMapping(value = "/Revoke", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> revoke(RequestEntity<PermissionRequest> request)
    {
        PermissionRequest permissionRequest = request.getBody();
        return getResponse(accessService.revoke(permissionRequest.getUserId(), permissionRequest.getPermission()));
    }
}
