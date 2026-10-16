package com.example.news.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.servlet.http.Part;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public final class CloudinaryImageUtil {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final long MAX_PIXELS = 12_000_000L;

    private CloudinaryImageUtil() {}

    public record UploadedImage(
            String url,
            String publicId
    ) {}

    public static UploadedImage uploadCover(Part part)
            throws IOException {
        return uploadToFolder(part, "newsweb/blog-covers");
    }

    public static UploadedImage uploadEditorialCover(Part part)
            throws IOException {
        return uploadToFolder(part, "newsweb/editorial-covers");
    }

    private static UploadedImage uploadToFolder(Part part, String folder)
            throws IOException {

        // No image selected.
        if (part == null || part.getSize() == 0) {
            return null;
        }

        if (part.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "Cover image must be 5 MB or smaller."
            );
        }

        String mime = part.getContentType();

        if (!"image/jpeg".equalsIgnoreCase(mime)
                && !"image/png".equalsIgnoreCase(mime)) {
            throw new IllegalArgumentException(
                    "Only JPEG and PNG images are allowed."
            );
        }

        byte[] original;

        try (var input = part.getInputStream()) {
            original = input.readNBytes((int) MAX_BYTES + 1);
        }

        if (original.length > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "Cover image is too large."
            );
        }

        BufferedImage image;
        String format;

        // Inspect the real image, not just its file extension.
        try (ImageInputStream stream =
                     ImageIO.createImageInputStream(
                             new ByteArrayInputStream(original))) {

            if (stream == null) {
                throw new IllegalArgumentException(
                        "Invalid image file."
                );
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(stream);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "Unsupported image file."
                );
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(stream);

                format = reader.getFormatName()
                        .toLowerCase(java.util.Locale.ROOT);

                if (!"jpeg".equals(format)
                        && !"png".equals(format)) {
                    throw new IllegalArgumentException(
                            "Only JPEG and PNG files are supported."
                    );
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0
                        || width > 5000 || height > 5000
                        || (long) width * height > MAX_PIXELS) {
                    throw new IllegalArgumentException(
                            "Image dimensions are too large."
                    );
                }

                image = reader.read(0);

            } finally {
                reader.dispose();
            }
        }

        if (image == null) {
            throw new IllegalArgumentException(
                    "Could not decode image."
            );
        }

        // Re-encode to remove uploaded metadata and
        // avoid storing arbitrary original file bytes.
        ByteArrayOutputStream cleanOutput =
                new ByteArrayOutputStream();

        String outputFormat =
                "png".equals(format) ? "png" : "jpg";

        if (!ImageIO.write(
                image, outputFormat, cleanOutput)) {
            throw new IOException(
                    "Unable to process cover image."
            );
        }

        byte[] cleanBytes = cleanOutput.toByteArray();

        if (cleanBytes.length > 10 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Processed image is too large."
            );
        }

        String cloudinaryUrl =
                System.getenv("CLOUDINARY_URL");

        if (cloudinaryUrl == null
                || cloudinaryUrl.isBlank()) {
            throw new IOException(
                    "Cloudinary is not configured."
            );
        }

        Cloudinary cloudinary =
                new Cloudinary(cloudinaryUrl);

        Map<?, ?> result = cloudinary.uploader().upload(
                cleanBytes,
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image"
                )
        );

        String url =
                (String) result.get("secure_url");

        String publicId =
                (String) result.get("public_id");

        if (url == null
                || !url.startsWith("https://res.cloudinary.com/")
                || publicId == null
                || publicId.isBlank()) {

            throw new IOException(
                    "Cloudinary returned an invalid image result."
            );
        }

        return new UploadedImage(url, publicId);
    }

public static void deleteCover(String publicId)
        throws IOException {
    deleteFromFolder(publicId, "newsweb/blog-covers/");
}

public static void deleteEditorialCover(String publicId)
        throws IOException {
    deleteFromFolder(publicId, "newsweb/editorial-covers/");
}

private static void deleteFromFolder(String publicId, String folderPrefix)
        throws IOException {

    if (publicId == null || publicId.isBlank()) {
        return;
    }

    // Never delete an image outside our blog cover folder.
    if (!publicId.startsWith(folderPrefix)) {
        throw new IOException(
                "Unexpected Cloudinary cover ID."
        );
    }

    String cloudinaryUrl = System.getenv("CLOUDINARY_URL");

    if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
        throw new IOException("Cloudinary is not configured.");
    }

    try {
        Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);

        Map<?, ?> result = cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.emptyMap()
        );

        String status = String.valueOf(result.get("result"));

        if (!"ok".equals(status) && !"not found".equals(status)) {
            throw new IOException(
                    "Cloudinary deletion failed: " + status
            );
        }

    } catch (Exception e) {
        throw new IOException(
                "Could not delete Cloudinary cover.", e
        );
    }
}

}
