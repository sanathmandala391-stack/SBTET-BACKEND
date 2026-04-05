package com.sbtetAttendance.sbtet.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<String, String> uploadBase64(String base64Data, String folder) throws IOException {
        // Accept both "data:image/jpeg;base64,..." and raw base64
        String data = base64Data.contains(",") ? base64Data : "data:image/jpeg;base64," + base64Data;
        @SuppressWarnings("unchecked")
        Map<String, Object> result = cloudinary.uploader().upload(data,
                ObjectUtils.asMap("folder", folder, "resource_type", "image"));
        return Map.of(
                "url", (String) result.get("secure_url"),
                "publicId", (String) result.get("public_id")
        );
    }

    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}

