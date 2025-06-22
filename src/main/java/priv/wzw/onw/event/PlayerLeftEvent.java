package priv.wzw.onw.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class PlayerLeftEvent extends AbstractRoomEvent {
    private final String leftUserId;
    private boolean hostChanged;
    private String currentHostId;

    public PlayerLeftEvent(String leftUserId) {
        super(Type.PLAYER_LEFT);
        this.leftUserId = leftUserId;
    }

}
