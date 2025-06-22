package priv.wzw.onw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdatePlayerStateRequest {
    @NotBlank
    private String userId;
    @NotNull
    private Boolean ready;
}
