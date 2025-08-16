package priv.wzw.onw.dto;

import lombok.Builder;
import lombok.Getter;
import priv.wzw.onw.RoleCard;

@Getter
@Builder
public class SeatData {
    private RoleCard initialRole;
}
