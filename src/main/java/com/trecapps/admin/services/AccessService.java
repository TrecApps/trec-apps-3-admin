package com.trecapps.admin.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trecapps.auth.models.TcUser;
import com.trecapps.auth.repos.primary.TrecAccountRepo;
import com.trecapps.auth.services.UserStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccessService {

    @Autowired
    UserStorageService userStorageService;

    @Autowired
    TrecAccountRepo trecAccountRepo;

    @Value("#{'${trecauth.permissions}'.split(',')}")
    List<String> permissions;

    public String grant(String userId, String permission)
    {
        if(!trecAccountRepo.existsById(userId))
            return "404: User Not found!";

        if(!permissions.contains(permission))
            return "400: Permission not supported!";
        try {
            TcUser user = userStorageService.retrieveUser(userId);
            List<String> roles = new ArrayList<String>(List.of(user.getAuthRoles()));

            if(roles.contains(permission))
                return "412: Permission already granted!";

            roles.add(permission);

            user.setAuthRoles(roles.toArray(new String[0]));
            userStorageService.saveUser(user);
            return "200: Success!";
        } catch (JsonProcessingException e) {
            return "500: Issue managing User Data";
        }
    }

    public String revoke(String userId, String permission)
    {
        if(!trecAccountRepo.existsById(userId))
            return "404: User Not found!";

        if(!permissions.contains(permission))
            return "400: Permission not supported!";

        try
        {
            TcUser user = userStorageService.retrieveUser(userId);

            List<String> roles = new ArrayList<String>(List.of(user.getAuthRoles()));

            if(!roles.contains(permission))
                return "412: Permission not present!";

            roles = roles.stream().filter((String role) -> !role.equals(permission)).collect(Collectors.toList());
            user.setAuthRoles(roles.toArray(new String[0]));
            userStorageService.saveUser(user);
            return "200: Success!";
        } catch(JsonProcessingException e)
        {
            return "500: Issue managing User Data";
        }
    }
}
