package com.trecapps.admin.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trecapps.admin.models.VerificationRequest;
import com.trecapps.pictures.models.Picture;
import com.trecapps.pictures.models.PictureData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VerificationService {

    @Autowired
    VerificationStorageService verificationStorageService;

    @Autowired
    AccessService accessService;

    @Value("${verification.approval.threshold:10}")
    int threshold;

    public boolean hasVerification(String userId)
    {
        return verificationStorageService.hasVerificationRequest(userId);
    }

    public VerificationRequest getRequest(String userId)
    {
        try
        {
            return verificationStorageService.getVerificationRequest(userId);
        }catch (JsonProcessingException e)
        {
            return null;
        }
    }

    public String requestVerification(String userId)
    {
        try {
            verificationStorageService.getVerificationRequest(userId);
            return "409:Verification Already Requested!";
        } catch (JsonProcessingException e) {
            VerificationRequest newRequest = new VerificationRequest();
            newRequest.setApprover(new ArrayList<>());
            newRequest.setEvidence(new ArrayList<>());
            verificationStorageService.saveVerificationRequest(userId, newRequest);
        }
        return "200:Success!";
    }

    public String addEvidence(String id, Picture picture)
    {
        try{
            VerificationRequest request = verificationStorageService.getVerificationRequest(id);
            List<Picture> evidence = request.getEvidence();
            evidence.add(picture);
            verificationStorageService.saveVerificationRequest(id, request);
            return "200:Added!";
        } catch (JsonProcessingException e) {
            return "404:Request Not Found!";
        }
    }

    public String doApproval(String target, String approver)
    {
        try{
            VerificationRequest request = verificationStorageService.getVerificationRequest(target);
            List<String> evidence = request.getApprover();
            evidence.add(approver);
            verificationStorageService.saveVerificationRequest(target, request);
            return evidence.size() >= threshold ? accessService.grantVerify(target) : "200:Added!";
        } catch (JsonProcessingException e) {
            return "404:Request Not Found!";
        }
    }


}
