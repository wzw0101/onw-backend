package priv.wzw.onw.event;

import lombok.Getter;
import priv.wzw.onw.Room;

@Getter
public class RoomStateChangeEvent extends AbstractRoomEvent {

    private final Room data;

    public RoomStateChangeEvent(Room room) {
        super(Type.ROOM_STATE_CHANGED);
        this.data = room;
    }
}
