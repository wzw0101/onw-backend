package priv.wzw.onw;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomManager {
    private static final Map<String, Room> allRoomMap = new HashMap<>();

    private final ObjectFactory<Room> roomObjectFactory;
    private final GameTimingProperties gameTimingProperties;

    public Room createRoom(Player hostPlayer, List<RoleCard> selectedCards, 
                          Integer gameStartDelaySeconds, Integer turnDurationSeconds) {
        int playerSize = selectedCards.size() - Room.CENTER_SIZE;
        Room room = roomObjectFactory.getObject();

        allRoomMap.put(room.getId(), room);
        room.getSelectedCards().addAll(selectedCards);
        room.getSeats().addAll(Arrays.asList(new String[playerSize]));

        List<PlayerColor> colors = Arrays.asList(PlayerColor.values());
        Collections.shuffle(colors);
        room.getColorPool().addAll(colors.subList(0, playerSize));

        // 设置时间配置，如果未提供则使用配置属性中的默认值
        room.setGameStartDelaySeconds(gameStartDelaySeconds != null 
                ? gameStartDelaySeconds 
                : gameTimingProperties.getGameStartDelaySeconds());
        room.setTurnDurationSeconds(turnDurationSeconds != null 
                ? turnDurationSeconds 
                : gameTimingProperties.getDefaultTurnDurationSeconds());

        room.reset();

        hostPlayer.exitRoom();
        room.accept(hostPlayer);
        hostPlayer.setRoomId(room.getId());
        room.setHostPlayer(hostPlayer);
        return room;
    }

    public Room lookup(String roomId) {
        Room room = allRoomMap.get(roomId);
        if (room == null) {
            log.info("room id {} not exist", roomId);
        }
        return room;
    }

    public void remove(String roomId) {
        allRoomMap.remove(roomId);
    }
}
