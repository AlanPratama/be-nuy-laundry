package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.FileUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileUseCase fileUseCase;

    @GetMapping("/view")
    public ResponseEntity<byte[]> viewFileByParam(@RequestParam("path") String path) {
        return serveFile(path);
    }

    @GetMapping("/**")
    public ResponseEntity<byte[]> viewFileByPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        String prefix = "/api/v1/files/";
        String finalPath;
        if (uri.startsWith(prefix)) {
            finalPath = uri.substring(prefix.length());
        } else {
            Object bestMatchPatternObj = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            if (bestMatchPatternObj != null && path != null) {
                AntPathMatcher apm = new AntPathMatcher();
                finalPath = apm.extractPathWithinPattern(bestMatchPatternObj.toString(), path);
            } else {
                finalPath = path;
            }
        }

        if (finalPath != null) {
            finalPath = finalPath.replaceFirst("^/+", "");
            try {
                finalPath = java.net.URLDecoder.decode(finalPath, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ignored) {
            }
        }

        return serveFile(finalPath);
    }

    private ResponseEntity<byte[]> serveFile(String filePath) {
        if (!ValueUtil.hasValueString(filePath)) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] data = fileUseCase.download(filePath);
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            String lower = filePath.toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if (lower.endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (lower.endsWith(".webp")) {
                mediaType = MediaType.parseMediaType("image/webp");
            } else if (lower.endsWith(".pdf")) {
                mediaType = MediaType.APPLICATION_PDF;
            }
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
