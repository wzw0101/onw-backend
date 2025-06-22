package priv.wzw.onw;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import priv.wzw.onw.event.*;

@EqualsAndHashCode(of = "userId")
@Data
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NoArgsConstructor
public class Player {

    private String userId;
    private String roomId;
    private PlayerColor color;
    private RoleCard initialRole;

    @Autowired
    @JsonIgnore
    private RoomManager roomManager;
    @Autowired
    @JsonIgnore
    private SimpMessagingTemplate template;
    @Autowired
    @JsonIgnore
    private JacksonUtils jacksonUtils;

    public void joinRoom(String targetRoomId) {
        Room targetRoom = roomManager.lookup(targetRoomId);
        if (targetRoom == null || !targetRoom.accept(this)) {
            return;
        }

        exitRoom();
        roomId = targetRoomId;

        PlayerJoinEvent event = new PlayerJoinEvent(userId, color);
        String eventJson = jacksonUtils.toJson(event);
        if (eventJson != null) {
            template.convertAndSend("/topic/room/" + roomId, eventJson);
        }
    }

    public void exitRoom() {
        if (this.roomId == null) {
            return;
        }
        PlayerLeftEvent event = new PlayerLeftEvent(userId);
        Room prevRoom = roomManager.lookup(roomId);
        if (prevRoom == null) {
            log.warn("user is in a non existing room {}", roomId);
            return;
        }
        prevRoom.remove(this, event);
        color = null;
        initialRole = null;

        String eventJson = jacksonUtils.toJson(event);
        if (eventJson != null) {
            template.convertAndSend("/topic/room/" + roomId, eventJson);
        }

        roomId = null;
    }

    public void takeSeat(int seatNum) {
        Room room = roomManager.lookup(roomId);
        if (room == null) {
            log.info("can not take the seat cause room {} not exist ", roomId);
            return;
        }
        if (!room.takeSeat(userId, seatNum)) {
            return;
        }
        initialRole = room.getPlayerCards().get(seatNum);

        PlayerTakeSeatEvent event = PlayerTakeSeatEvent.builder().seatNum(seatNum).userId(userId).build();
        template.convertAndSend("/topic/room/" + roomId, jacksonUtils.toJson(event));
    }

    public void leaveSeat() {
        Room room = roomManager.lookup(roomId);
        if (room == null) {
            return;
        }
        room.leaveSeat(userId);
        PlayerLeaveSeatEvent event = PlayerLeaveSeatEvent.builder().userId(userId).build();
        template.convertAndSend("/topic/room/" + roomId, jacksonUtils.toJson(event));
    }

    public void updateReadyState(boolean ready) {
        Room room = roomManager.lookup(roomId);
        if (room == null) {
            return;
        }
        boolean updated = room.updateReadyState(userId, ready);
        if (updated) {
            PlayerStateUpdateEvent event = PlayerStateUpdateEvent.builder().ready(ready).userId(userId).build();
            template.convertAndSend("/topic/room/" + roomId, jacksonUtils.toJson(event));
        }
    }

    public void startGame() {
        Room room = roomManager.lookup(roomId);
        if (room == null) {
            return;
        }
        if (!room.getHostPlayer().equals(this)) {
            log.warn("player {} is not host of room {}", userId, roomId);
            return;
        }
        room.start();
    }

}
