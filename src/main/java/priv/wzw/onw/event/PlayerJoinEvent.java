package priv.wzw.onw.event;

import lombok.Builder;
import lombok.Getter;
import priv.wzw.onw.PlayerColor;

@Builder
@Getter
public class PlayerJoinEvent extends AbstractRoomEvent {
    private final String userId;
    private final PlayerColor playerColor;

    public PlayerJoinEvent(String userId, PlayerColor playerColor) {
        super(Type.PLAYER_JOIN);
        this.userId = userId;
        this.playerColor = playerColor;
    }
}
