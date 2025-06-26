package priv.wzw.onw.event;

import lombok.Getter;

@Getter
public class GameOverEvent extends AbstractRoomEvent {
    private final String mostVotedPlayer;

    public GameOverEvent(String mostVotedPlayer) {
        super(Type.GAME_OVER);
        this.mostVotedPlayer = mostVotedPlayer;
    }
}
