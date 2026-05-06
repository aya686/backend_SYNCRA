package org.example.backend_pi.dto;

// dto/ServiceRequestDTO.java


import lombok.Data;
import java.util.List;

@Data
public class ServiceRequestDTO {
    private Long id;
    private String title;
    private String description;
    private Long requesterId;
    private String requesterName;
    private String requesterEmail;
    private String requesterPhone;
    private Long targetProviderId;
    private Boolean isExternalProvider;
    private String externalProviderName;
    private String externalProviderPhone;
    private String externalProviderEmail;
    private String externalProviderCompany;
    private String requestType;
    private Long machineServiceId;
    private String machineServiceName;
    private Integer quantity;
    private String material;
    private String deadline;
    private String specifications;
    private Double budget;
    private String status;
    private List<RequestAttachmentDTO> attachments;

}