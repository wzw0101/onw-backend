package priv.wzw.onw.event;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractRoomEvent {
    private final Type eventType;

    public String getEventType() {
        return eventType.toString();
    }

    public enum Type {
        ROOM_STATE_CHANGED,
        PHASE_CHANGED,
        ;
    }
}
