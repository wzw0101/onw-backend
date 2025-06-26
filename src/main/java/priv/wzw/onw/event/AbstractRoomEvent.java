package priv.wzw.onw.event;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractRoomEvent {
    private final Type eventType;

    public String getEventType() {
        return eventType.name;
    }

    public enum Type {
        GAME_START("gameStartEvent"),
        PLAYER_JOIN("playerJoinEvent"),
        PLAYER_LEAVE_SEAT("playerLeaveSeatEvent"),
        PLAYER_LEFT("playerLeftEvent"),
        PLAYER_STATE_UPDATE("playerStateUpdateEvent"),
        PLAYER_TAKE_SEAT("playerTakeSeatEvent"),
        WEREWOLF_TURN("werewolfTurnEvent"),
        MINION_TURN("minionTurnEvent"),
        SEER_TURN("seerTurnEvent"),
        ROBBER_TURN("robberTurnEvent"),
        TROUBLEMAKER_TURN("troubleMakerTurnEvent"),
        DRUNK_TURN("drunkTurnEvent"),
        INSOMNIAC_TURN("insomniacTurnEvent"),
        VOTE_TURN("voteTurnEvent"),
        GAME_OVER("gameOverEvent"),
        ;

        private final String name;

        Type(String name) {
            this.name = name;
        }
    }
}
