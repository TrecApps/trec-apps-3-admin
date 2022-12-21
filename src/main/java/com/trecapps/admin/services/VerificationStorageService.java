package com.trecapps.admin.services;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.TaggedBlobItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.trecapps.admin.models.VerificationRequest;
import com.trecapps.auth.models.TcUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VerificationStorageService {

    BlobServiceClient client;

    ObjectMapper objectMapper;

    @Autowired
    VerificationStorageService(@Value("${picture.storage.account-name}") String name,
                          @Value("${picture.storage.account-key}") String key,
                          @Value("${picture.storage.blob-endpoint}") String endpoint,
                          Jackson2ObjectMapperBuilder objectMapperBuilder)
    {
        AzureNamedKeyCredential credential = new AzureNamedKeyCredential(name, key);
        client = new BlobServiceClientBuilder().credential(credential).endpoint(endpoint).buildClient();
        objectMapper = objectMapperBuilder.createXmlMapper(false).build();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    boolean hasVerificationRequest(String id)
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-pictures");
        BlobClient client = containerClient.getBlobClient(String.format("%s_verify.json", id));
        return client.exists();
    }

    VerificationRequest getVerificationRequest(String id) throws JsonProcessingException {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-pictures");
        BlobClient client = containerClient.getBlobClient(String.format("%s_verify.json", id));
        BinaryData bData = client.downloadContent();
        String data = new String(bData.toBytes(), StandardCharsets.UTF_8);
        return this.objectMapper.readValue(data, VerificationRequest.class);
    }

    void saveVerificationRequest(String id, VerificationRequest request)
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-pictures");
        BlobClient client = containerClient.getBlobClient(String.format("%s_verify.json", id));
        client.upload(BinaryData.fromObject(request), true);
        client.setTags(Map.of("Purpose",request.isApproved() ? "AppVerify":"IdVerify"));

    }

    List<String> RetrieveRequests()
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-pictures");
        return containerClient.findBlobsByTags("where=Purpose=IdVerify").stream()
                .map(TaggedBlobItem::getName).collect(Collectors.toList());
    }
}
