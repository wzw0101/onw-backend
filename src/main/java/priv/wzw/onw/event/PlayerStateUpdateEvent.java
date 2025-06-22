package priv.wzw.onw.event;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PlayerStateUpdateEvent extends AbstractRoomEvent {
    private String userId;
    private boolean ready;

    public PlayerStateUpdateEvent(String userId, boolean ready) {
        super(Type.PLAYER_STATE_UPDATE);
        this.userId = userId;
        this.ready = ready;
    }
}
