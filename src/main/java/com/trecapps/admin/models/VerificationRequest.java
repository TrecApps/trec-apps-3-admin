package com.trecapps.admin.models;

import com.trecapps.pictures.models.Picture;
import com.trecapps.pictures.services.PictureManager;
import lombok.Data;

import java.util.List;

@Data
public class VerificationRequest {
    List<Picture> evidence;
    List<String> approver;
}
