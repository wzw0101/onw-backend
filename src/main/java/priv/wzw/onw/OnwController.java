package priv.wzw.onw;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OnwController {

    private final RoomManager roomManager;
    private final PlayerManager playerManager;

    @GetMapping("/room/{roomId}")
    public Room getRoom(@PathVariable("roomId") String roomId) {
        return roomManager.lookup(roomId);
    }

    @PostMapping("/player/{userId}/room")
    public String createRoom(@PathVariable("userId") String userId) {
        Player player = playerManager.getOrCreate(userId);
        Room room = roomManager.createRoom(player, Arrays.asList(RoleCard.values()));
        return room.getId();
    }

    @PostMapping("/player/{userId}/room/{roomId}")
    public void enterRoom(@PathVariable("roomId") String roomId, @PathVariable("userId") String userId) {
        if (roomManager.lookup(roomId) == null) {
            return;
        }
        Player player = playerManager.getOrCreate(userId);
        player.joinRoom(roomId);
    }

    @DeleteMapping("/player/{userId}/room")
    public void exitRoom(@PathVariable("userId") String userId) {
        playerManager.getOrCreate(userId).exitRoom();
    }

    @PostMapping("/player/{userId}/seat/{seatNum}")
    public void takeSeat(@PathVariable("userId") String userId, @PathVariable("seatNum") int seatNum) {
        Player player = playerManager.get(userId);
        if (player == null) {
            return;
        }
        player.takeSeat(seatNum);
    }

    @DeleteMapping("/player/{userId}/seat")
    public void leaveSeat(@PathVariable("userId") String userId) {
        Player player = playerManager.get(userId);
        if (player == null) {
            return;
        }
        player.leaveSeat();
    }

    @PutMapping("/player/{userId}/ready/{ready}")
    public void updateReadyState(@PathVariable("userId") String userId, @PathVariable("ready") boolean ready) {
        Player player = playerManager.get(userId);
        if (player == null) {
            return;
        }
        player.updateReadyState(ready);
    }

    @PostMapping("/player/{userId}/game-start")
    public void gameStart(@PathVariable("userId") String userId) {
        playerManager.getOrCreate(userId).startGame();
    }
}
