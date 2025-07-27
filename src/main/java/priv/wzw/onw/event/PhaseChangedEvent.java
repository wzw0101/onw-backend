package priv.wzw.onw.event;

import lombok.Builder;
import lombok.Getter;
import priv.wzw.onw.GamePhase;

@Getter
@Builder
public class PhaseChangedEvent extends AbstractRoomEvent {
    private final GamePhase gamePhase;

    public PhaseChangedEvent(GamePhase gamePhase) {
        super(Type.PHASE_CHANGED);
        this.gamePhase = gamePhase;
    }
}
