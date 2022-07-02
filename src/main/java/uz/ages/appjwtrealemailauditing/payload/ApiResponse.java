package uz.ages.appjwtrealemailauditing.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    private String messages;
    private boolean success;
    private Object object;

    public ApiResponse(String messages, boolean success) {
        this.messages = messages;
        this.success = success;
    }
}
