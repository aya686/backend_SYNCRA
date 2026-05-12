package tn.esprit.pifirst.event;

import org.springframework.context.ApplicationEvent;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.service.AuthService.ApiResponse;

public class BlockedAccountEvent extends ApplicationEvent {
    private final User user;
    private final ApiResponse apiResponse;

    public BlockedAccountEvent(Object source, User user, ApiResponse apiResponse) {
        super(source);
        this.user = user;
        this.apiResponse = apiResponse;
    }

    public User getUser() { return user; }
    public ApiResponse getApiResponse() { return apiResponse; }
}