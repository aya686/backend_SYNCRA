package org.example.backend_pi.dto;
import lombok.Data;

@Data
public class ReviewDTO {
    private Long id;
    private Integer rating;
    private String comment;
    private Long userId;
    private String userName;
    private String userAvatarUrl;
    private Long machineId;
    private Long serviceId;
}