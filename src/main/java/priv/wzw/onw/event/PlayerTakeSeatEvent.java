package priv.wzw.onw.event;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PlayerTakeSeatEvent extends AbstractRoomEvent {
    private int seatNum;
    private String userId;

    public PlayerTakeSeatEvent(int seatNum, String userId) {
        super(Type.PLAYER_TAKE_SEAT);
        this.seatNum = seatNum;
        this.userId = userId;
    }
}
