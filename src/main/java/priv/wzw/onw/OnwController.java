package priv.wzw.onw;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;

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

    @GetMapping("/player/{userId}/werewolf-turn/center-card/{cardIndex}")
    public String werewolfTurnGetCenterCard(@PathVariable("userId") String userId, @PathVariable("cardIndex") int cardIndex) {
        Player player = playerManager.getOrCreate(userId);
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            log.info("room {} not exist", player.getRoomId());
            return null;
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.WEREWOLF_TURN) {
            log.info("player {} game is not in werewolf turn", userId);
            return null;
        }
        if (cardIndex < 0 || cardIndex >= room.getCenterCards().size()) {
            log.info("card index {} out of range", cardIndex);
            return null;
        }
        return room.getCenterCards().get(cardIndex).name();
    }

    @GetMapping("/player/{userId}/seer-turn/player-card/{cardIndex}")
    public String seerTurnGetPlayerCard(@PathVariable("userId") String userId, @PathVariable("cardIndex") int cardIndex) {
        Player player = playerManager.getOrCreate(userId);
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            log.info("room {} not exist", player.getRoomId());
            return null;
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.SEER_TURN) {
            log.info("player {} game is not in seer turn", userId);
            return null;
        }
        if (cardIndex < 0 || cardIndex >= room.getPlayerCards().size()) {
            log.info("card index {} out of range", cardIndex);
            return null;
        }
        return room.getPlayerCards().get(cardIndex).name();
    }

    @PutMapping("/player/{userId}/robber-turn/player-card/{cardIndex}")
    public String robberTurnRobPlayerCard(@PathVariable("userId") String userId, @PathVariable("cardIndex") int cardIndex) {
        Player player = playerManager.getOrCreate(userId);
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            log.info("room {} not exist", player.getRoomId());
            return null;
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.ROBBER_TURN) {
            log.info("player {} game is not in robber turn", userId);
            return null;
        }
        if (cardIndex < 0 || cardIndex >= room.getPlayerCards().size()) {
            log.info("card index {} out of range", cardIndex);
            return null;
        }
        if (userId.equals(room.getSeats().get(cardIndex))) {
            log.info("can not rob yourself");
            return null;
        }
        String robCardName = room.getPlayerCards().get(cardIndex).name();
        Collections.swap(room.getPlayerCards(), cardIndex, room.getPlayerCards().indexOf(RoleCard.ROBBER));
        return robCardName;
    }

    @PutMapping("/player/{userId}/troublemaker-turn/player-cards/{cardIndices}")
    public void troublemakerTurnSwapPlayerCards(@PathVariable("userId") String userId, @PathVariable("cardIndices") int[] cardIndices) {
        Player player = playerManager.getOrCreate(userId);
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            log.info("room {} not exist", player.getRoomId());
            return;
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.TROUBLEMAKER_TURN) {
            log.info("player {} game is not in troublemaker turn", userId);
            return;
        }
        if (cardIndices.length != 2) {
            log.info("card indices length must be 2");
            return;
        }
        if (cardIndices[0] < 0 || cardIndices[0] >= room.getPlayerCards().size()) {
            log.info("card index {} out of range", cardIndices[0]);
            return;
        }
        if (cardIndices[1] < 0 || cardIndices[1] >= room.getPlayerCards().size()) {
            log.info("card index {} out of range", cardIndices[1]);
            return;
        }
        Collections.swap(room.getPlayerCards(), cardIndices[0], cardIndices[1]);
    }

    @PutMapping("/player/{userId}/drunk-turn/center-card/{cardIndex}")
    public void drunkTurnSwapCenterCard(@PathVariable("userId") String userId, @PathVariable("cardIndex") int cardIndex) {
        Player player = playerManager.getOrCreate(userId);
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            log.info("room {} not exist", player.getRoomId());
            return;
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.DRUNK_TURN) {
            log.info("player {} game is not in drunk turn", userId);
            return;
        }
        if (cardIndex < 0 || cardIndex >= room.getCenterCards().size()) {
            log.info("card index {} out of range", cardIndex);
            return;
        }
        RoleCard toSwap = room.getCenterCards().get(cardIndex);
        room.getCenterCards().set(cardIndex, RoleCard.DRUNK);
        int playerSeatNum = room.getSeats().indexOf(userId);
        room.getPlayerCards().set(playerSeatNum, toSwap);
    }

    @GetMapping("/player/{userId}/insomniac-turn/player-card")
    public String insomniacTurnGetPlayerCard(@PathVariable("userId") String userId) {
        Player player = playerManager.getOrCreate(userId);
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            log.info("room {} not exist", player.getRoomId());
            return null;
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.INSOMNIAC_TURN) {
            log.info("player {} game is not in insomniac turn", userId);
            return null;
        }
        int playerSeatNum = room.getSeats().indexOf(userId);
        return room.getPlayerCards().get(playerSeatNum).name();
    }

    @PostMapping("/player/{userId}/vote/{targetPlayerId}")
    public void vote(@PathVariable("userId") String userId, @PathVariable("targetPlayerId") int targetPlayerIndex) {
        Player voter = playerManager.getOrCreate(userId);
        Room room = roomManager.lookup(voter.getRoomId());
        if (room == null) {
            log.info("room {} not exist", voter.getRoomId());
            return;
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.VOTING) {
            log.info("player {} game is not in voting turn", userId);
            return;
        }
        if (targetPlayerIndex < 0 || targetPlayerIndex >= room.getSelectedCards().size()) {
            log.info("voted target index {} invalid", targetPlayerIndex);
            return;
        }
        room.getVotes().get(targetPlayerIndex).incrementAndGet();
    }

}
