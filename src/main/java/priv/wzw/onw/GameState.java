package priv.wzw.onw;

public enum GameState {
    INIT(GamePhase.PREPARE),
    STARTED(GamePhase.GAME_START),
    WEREWOLF_TURN(GamePhase.WEREWOLF_TURN),
    WEREWOLF_DONE(GamePhase.WEREWOLF_TURN),
    MINION_TURN(GamePhase.MINION_TURN),
    MINION_DONE(GamePhase.MINION_TURN),
    SEER_TURN(GamePhase.SEER_TURN),
    SEER_DONE(GamePhase.SEER_TURN),
    ROBBER_TURN(GamePhase.ROBBER_TURN),
    ROBBER_DONE(GamePhase.ROBBER_TURN),
    TROUBLEMAKER_TURN(GamePhase.TROUBLEMAKER_TURN),
    TROUBLEMAKER_DONE(GamePhase.TROUBLEMAKER_TURN),
    DRUNK_TURN(GamePhase.DRUNK_TURN),
    DRUNK_DONE(GamePhase.DRUNK_TURN),
    INSOMNIAC_TURN(GamePhase.INSOMNIAC_TURN),
    INSOMNIAC_DONE(GamePhase.INSOMNIAC_TURN),
    VOTING(GamePhase.VOTE_TURN),
    END(GamePhase.GAME_OVER),
    ;

    private final GamePhase gamePhase;

    GameState(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }
}
