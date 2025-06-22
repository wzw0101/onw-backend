package priv.wzw.onw.statemachine;

import lombok.Builder;
import lombok.Getter;
import priv.wzw.onw.Room;

@Builder
@Getter
public class GameContext extends StateMachineContext {
    private final Room room;
    private final Integer werewolfCenterCardIndex;
    private final Integer robTargetIndex;
    private final Integer troublemakerIndex1;
    private final Integer troublemakerIndex2;
    private final Integer drunkCenterIndex;

}
