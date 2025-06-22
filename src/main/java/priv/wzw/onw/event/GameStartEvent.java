package priv.wzw.onw.event;

import lombok.Builder;
import lombok.ToString;

@ToString
@Builder
public class GameStartEvent extends AbstractRoomEvent {
    public GameStartEvent() {
        super(Type.GAME_START);
    }
}
