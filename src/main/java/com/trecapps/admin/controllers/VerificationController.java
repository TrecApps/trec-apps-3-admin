package com.trecapps.admin.controllers;

import com.azure.core.annotation.QueryParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.trecapps.admin.models.VerificationRequest;
import com.trecapps.admin.services.VerificationService;
import com.trecapps.auth.models.TcUser;
import com.trecapps.auth.models.TrecAuthentication;
import com.trecapps.auth.services.TrecAccountService;
import com.trecapps.auth.services.UserStorageService;
import com.trecapps.pictures.models.Picture;
import com.trecapps.pictures.repos.PictureRepo;
import com.trecapps.pictures.services.PictureManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/Verify")
@Slf4j
public class VerificationController {

    @Value("${trecauth.verify.permission:ADMIN_VERIFIED}")
    String verifyPermission;

    @Autowired
    PictureManager pictureManager;

    @Autowired
    VerificationService verificationService;

    @Autowired
    PictureRepo pictureRepo;

    @Autowired
    UserStorageService userStorageService;

    private static final List<String> imageTypes = Arrays.asList(
            "apng",
            "avif",
            "gif",
            "jpeg",
            "jpg",
            "png",
            "svg",
            "webp"
    );

    // Calls for the User

    @GetMapping(value = "/isVerified", consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> isVerified(Authentication authentication)
    {
        log.info("Made it to 'isVerified'");
        TrecAuthentication tAuth = (TrecAuthentication) authentication;

        for(GrantedAuthority authority :tAuth.getAuthorities())
        {
            if(authority.getAuthority().equals(verifyPermission))
                return new ResponseEntity<>(Boolean.TRUE.toString(), HttpStatus.OK);
        }
        return new ResponseEntity<>(Boolean.FALSE.toString(), HttpStatus.OK);
    }
    @GetMapping(value = "/hasVerification", consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> hasVerificationRequest(Authentication authentication)
    {
        TrecAuthentication tAuth = (TrecAuthentication) authentication;

        return new ResponseEntity<>(Boolean.valueOf(verificationService
                .hasVerification(tAuth.getAccount().getId())).toString(), HttpStatus.OK);
    }

    @GetMapping(value = "/requestVerification", consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> requestVerification(Authentication authentication)
    {
        TrecAuthentication tAuth = (TrecAuthentication) authentication;
        String id = tAuth.getAccount().getId();
        if(verificationService.hasVerification(id))
            return new ResponseEntity<>("You Already Have a Request in Place!", HttpStatus.CONFLICT);
        else
        {
            String res = verificationService.requestVerification(id);
            String[] result = res.split("[:]", 2);
            return new ResponseEntity<>(result[1], HttpStatus.valueOf(Integer.parseInt(result[0])));
        }
    }

    @PostMapping(value = "/AddEvidence", consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> setPic(RequestEntity<String> request)
    {
        String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        if(null == contentType)
            return new ResponseEntity<>("Requires Content-Type header to be provided!", HttpStatus.BAD_REQUEST);
        String type = null;
        contentType = contentType.toLowerCase(Locale.ROOT);
        for(String imageType: imageTypes)
        {
            if(contentType.contains(imageType))
            {
                type = imageType;
                break;
            }
        }

        if(null == type)
            return new ResponseEntity<>("Requires Content-Type header to have 'apng' 'avif' 'gif' 'jpeg' 'png' 'svg' 'webp' ''!", HttpStatus.BAD_REQUEST);

        TrecAuthentication trecAuth = (TrecAuthentication) SecurityContextHolder.getContext().getAuthentication();
        String id = pictureManager.addNewPicture(
                trecAuth.getAccount().getId(),                              // Id if the user adding the picture
                request.getHeaders().getFirst("Picture-Name"),   // Name fo he picture (if provided)
                type,                                                       // Type of image we're dealing with
                request.getBody(),                                          // The image data in base64 format
                false);                                              // It's the profile pic so might as well be public


            String[] results = verificationService.addEvidence(trecAuth.getAccount().getId(),
                    pictureManager.retrievePictureObject(id)).split("[:]", 2);

            HttpStatus status = HttpStatus.valueOf(Integer.parseInt(results[0]));
            String resultBody = results[1];


        return new ResponseEntity<>(resultBody, status);
    }

    // Calls for the Admin

    boolean isUserAdmin(TrecAuthentication trecAuthentication)
    {
        for(GrantedAuthority authority : trecAuthentication.getAuthorities())
        {
            if(authority.getAuthority().equals("USER_ADMIN"))
                return true;
        }
        return false;
    }

    @GetMapping("/admin/listRequesters")
    ResponseEntity<List<String>> getRequesterList()
    {
        return new ResponseEntity<>(verificationService.getRequesters(),HttpStatus.OK);
    }

    @GetMapping(value="/RequesterProfileType", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> getProfileType(@RequestParam String user)
    {
        String extension = pictureManager.getProfilePicName(user);
        if (extension == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Content-type", "text/plain");
        return new ResponseEntity<>(extension, header, HttpStatus.OK);
    }

    @GetMapping("/RequesterProfile/{userId}")
    ResponseEntity<byte[]> getPictureOfRequester(@PathVariable String userId, @RequestParam String ext)
    {
        byte[] data = pictureManager.getProfilePic(userId, ext);
        if(data == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Content-type", String.format("image/%s", ext.toLowerCase(Locale.ROOT)));
        return new ResponseEntity<>(data, header, HttpStatus.OK);
    }

    @GetMapping("/admin/RequesterInfo")
    ResponseEntity<TcUser> getRequesterDetails(@RequestParam String userId)
    {
        try {
            TcUser user = userStorageService.retrieveUser(userId);
            user.setCurrentCode(null);
            user.setBrands(null);
            user.setUserProfile(null);
            user.setCodeExpiration(null);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/admin/listPic")
    ResponseEntity<List<String>> getPictureIds(Authentication authentication, @RequestParam("userId")String userId)
    {
        VerificationRequest request = verificationService.getRequest(userId);
        if(request == null)
        {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        List<String> ret = new ArrayList<>();
        for(String picId: request.getEvidence())
        {
            Picture pic = pictureManager.retrievePictureObject(picId);
            if(pic == null)
                continue;
            ret.add(String.format("%s.%s", picId, pic.getExtension()));
        }
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("/admin/pic/{fileName}")
    ResponseEntity<byte[]> getProfilePicture(@PathVariable("fileName")String fileName)
    {
        String[] filePieces = fileName.split("[.]");
        String name = filePieces[0];
        for(int rust = 1; rust < (filePieces.length - 1); rust++)
            name = "." + filePieces[rust];

        byte[] data = pictureManager.getPic(name, filePieces[filePieces.length -1]);
        if(data == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Content-type", String.format("image/%s", filePieces[filePieces.length - 1].toLowerCase(Locale.ROOT)));
        return new ResponseEntity<>(data, header, HttpStatus.OK);
    }

    @GetMapping("/admin/verify")
    ResponseEntity<String> verify(@QueryParam("userId") String userId, Authentication authentication)
    {
        TrecAuthentication trecAuthentication = (TrecAuthentication) authentication;

        String[] res = verificationService.doApproval(userId, trecAuthentication.getAccount().getId()).split("[:]",2);

        return new ResponseEntity<>(res[1], HttpStatus.valueOf(Integer.parseInt(res[0])));
    }
}
