package lk.ijse.eca.productservice.service;

import com.google.cloud.storage.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class GcsStorageService {

    @Value("${gcp.storage.bucket-name}")
    private String bucketName;

    @Value("${gcp.storage.project-id}")
    private String projectId;

    private Storage storage;

    @PostConstruct
    public void init() {
        this.storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .build()
                .getService();
        log.info("GCS Storage initialized for project: {} bucket: {}", projectId, bucketName);
    }

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String objectName = folder + "/" + UUID.randomUUID() + extension;

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        String publicUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);
        log.info("File uploaded to GCS: {}", publicUrl);
        return publicUrl;
    }

    public void deleteFile(String gcsPath) {
        if (gcsPath != null && !gcsPath.isEmpty()) {
            BlobId blobId = BlobId.of(bucketName, gcsPath);
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                log.info("File deleted from GCS: {}", gcsPath);
            } else {
                log.warn("File not found in GCS: {}", gcsPath);
            }
        }
    }

    public String getBucketName() {
        return bucketName;
    }
}