package com.trecapps.admin.models;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PermissionRequest {
    @NotNull
    String userId;
    @NotNull
    String permission;
}
