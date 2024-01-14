package api.userservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleToUserDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Role name is required")
    private String roleName;
}
