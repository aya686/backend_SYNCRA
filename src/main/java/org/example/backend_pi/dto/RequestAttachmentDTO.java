package org.example.backend_pi.dto;
// dto/RequestAttachmentDTO.java

import lombok.Data;

@Data
public class RequestAttachmentDTO {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
}