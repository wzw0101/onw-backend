package priv.wzw.onw.event;

import lombok.Getter;
import priv.wzw.onw.dto.RoomDTO;

@Getter
public class RoomStateChangeEvent extends AbstractRoomEvent {

    private final RoomDTO data;

    public RoomStateChangeEvent(RoomDTO room) {
        super(Type.ROOM_STATE_CHANGED);
        this.data = room;
    }
}
