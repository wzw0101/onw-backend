package priv.wzw.onw;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import priv.wzw.onw.dto.RoomDTO;

@Mapper(componentModel = "spring")
public interface Converters {
    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "gamePhase", source = "room.gameStateMachine.currentState")
    RoomDTO toDTO(Room room);

    default String toPlayerId(Player player) {
        return player.getUserId();
    }

    default String toGamePhase(GameState gameState) {
        return gameState.getGamePhase().name();
    }
}
