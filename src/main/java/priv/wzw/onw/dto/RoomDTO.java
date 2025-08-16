package priv.wzw.onw.dto;

import lombok.Builder;
import lombok.Getter;
import priv.wzw.onw.GamePhase;
import priv.wzw.onw.PlayerColor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Builder
public class RoomDTO {
    private String roomId;
    private Set<String> players;
    private List<Boolean> readyList;
    private List<String> seats;
    private GamePhase gamePhase;
    private Map<String, PlayerColor> playerColorMap;
    private String hostPlayer;
}
