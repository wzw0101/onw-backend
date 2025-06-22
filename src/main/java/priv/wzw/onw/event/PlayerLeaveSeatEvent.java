package priv.wzw.onw.event;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PlayerLeaveSeatEvent extends AbstractRoomEvent {
    private String userId;

    public PlayerLeaveSeatEvent(String userId) {
        super(Type.PLAYER_LEAVE_SEAT);
    }
}
