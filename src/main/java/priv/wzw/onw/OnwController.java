package priv.wzw.onw;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import priv.wzw.onw.dto.*;
import priv.wzw.onw.statemachine.GameContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OnwController {

    private final Converters converters;
    private final RoomManager roomManager;
    private final PlayerManager playerManager;
    private final SimpMessagingTemplate template;
    private final JacksonUtils jacksonUtils;

    @GetMapping("/room/{roomId}")
    @Deprecated
    public Room getRoom(@PathVariable("roomId") String roomId) {
        return roomManager.lookup(roomId);
    }

    @GetMapping("/player/{playerId}/room")
    public ApiResponse<RoomDTO> getPlayerRoom(@PathVariable("playerId") String playerId) {
        Player player = playerManager.get(playerId);
        if (player == null) {
            return ApiResponse.fail("player not exist");
        }
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            return ApiResponse.fail("room not exist");
        }
        return ApiResponse.success(converters.toDTO(room));
    }

    @PostMapping("/player/{userId}/room")
    public String createRoom(@PathVariable("userId") String userId,
                            @RequestBody(required = false) CreateRoomRequest request) {
        Player player = playerManager.getOrCreate(userId);
        Integer gameStartDelaySeconds = request != null ? request.getGameStartDelaySeconds() : null;
        Integer turnDurationSeconds = request != null ? request.getTurnDurationSeconds() : null;
        List<RoleCard> selectedRoles = (request != null && request.getRoles() != null && !request.getRoles().isEmpty())
                ? request.getRoles()
                : Arrays.asList(RoleCard.values());
        Room room = roomManager.createRoom(player, selectedRoles, 
                                          gameStartDelaySeconds, turnDurationSeconds);
        return room.getId();
    }

    @PostMapping("/player/{userId}/room/{roomId}")
    public ApiResponse<Void> enterRoom(@PathVariable("roomId") String roomId, @PathVariable("userId") String userId) {
        Room room = roomManager.lookup(roomId);
        if (room == null) {
            return ApiResponse.fail("room not exist");
        }
        Player player = playerManager.getOrCreate(userId);
        player.joinRoom(roomId);
        return ApiResponse.success();
    }

    @DeleteMapping("/player/{userId}/room")
    public void exitRoom(@PathVariable("userId") String userId) {
        playerManager.getOrCreate(userId).exitRoom();
    }

    @PostMapping("/player/{userId}/seat/{seatNum}")
    public ApiResponse<SeatData> takeSeat(@PathVariable("userId") String userId, @PathVariable("seatNum") int seatNum) {
        Player player = playerManager.get(userId);
        if (player == null) {
            return ApiResponse.fail("player not exist");
        }
        SeatData seatData = player.takeSeat(seatNum);
        if (seatData == null) {
            log.error("player {} failed to take seat {} ", userId, seatNum);
            return ApiResponse.fail("failed to take seat");
        }
        return ApiResponse.success(seatData);
    }

    @Deprecated
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

    @GetMapping("/player/{userId}/werewolf-turn")
    public ApiResponse<GetWerewolfData> werewolfTurn(
            @PathVariable("userId") String userId,
            @RequestParam(name = "cardIndex", required = false) Integer cardIndex) {
        Player player = playerManager.get(userId);
        if (player == null) {
            return ApiResponse.fail("player not exist");
        }
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            return ApiResponse.fail("room not exist");
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.WEREWOLF_TURN) {
            log.info("player {} game is not in werewolf turn", userId);
            return ApiResponse.fail("game is not in werewolf turn");
        }

        GetWerewolfData.GetWerewolfDataBuilder builder = GetWerewolfData.builder();
        if (cardIndex == null) {
            int playerIndex = room.getSeats().indexOf(userId);
            Integer werewolfIndex = null;
            for (int i = 0; i < room.getPlayerCards().size(); i += 1) {
                if (room.getPlayerCards().get(i) == RoleCard.WEREWOLF && i != playerIndex) {
                    werewolfIndex = i;
                }
            }
            builder.werewolfIndex(werewolfIndex);
        } else {
            if (cardIndex < 0 || cardIndex >= room.getCenterCards().size()) {
                log.info("card index {} out of range", cardIndex);
                return ApiResponse.fail("card index out of range");
            }
            builder.centerCard(room.getCenterCards().get(cardIndex));
        }
        return ApiResponse.success(builder.build());
    }

    @GetMapping("/player/{userId}/minion-turn")
    public ApiResponse<GetMinionData> minionTurn(@PathVariable("userId") String userId) {
        Player player = playerManager.get(userId);
        if (player == null) {
            return ApiResponse.fail("player not exist");
        }
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            return null;
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.MINION_TURN) {
            log.info("player {} game is not in minion turn", userId);
            return null;
        }
        int werewolfIndex = room.getPlayerCards().indexOf(RoleCard.WEREWOLF);
        return ApiResponse.success(GetMinionData.builder().werewolfIndex(werewolfIndex).build());
    }

    @GetMapping("/player/{userId}/seer-turn")
    public ApiResponse<GetSeerData> seerTurn(@PathVariable("userId") String userId,
                                             @RequestParam("cardIndex") int cardIndex) {
        Player player = playerManager.get(userId);
        if (player == null) {
            return ApiResponse.fail("player not exist");
        }
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            return ApiResponse.fail("room not exist");
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.SEER_TURN) {
            log.info("player {} game is not in seer turn", userId);
            return ApiResponse.fail("game is not in seer turn");
        }
        if (cardIndex < 0 || cardIndex >= room.getPlayerCards().size()) {
            log.info("card index {} out of range", cardIndex);
            return ApiResponse.fail("card index out of range");
        }
        GetSeerData data = GetSeerData.builder().roleCard(room.getPlayerCards().get(cardIndex)).build();
        return ApiResponse.success(data);
    }

    @PutMapping("/player/{userId}/robber-turn")
    public ApiResponse<RobberData> robberTurn(@PathVariable("userId") String userId,
                                              @RequestBody RobberRequest request) {
        Player player = playerManager.get(userId);
        if (player == null) {
            return ApiResponse.fail("player not exist");
        }
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            return ApiResponse.fail("room not exist");
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.ROBBER_TURN) {
            log.info("player {} game is not in robber turn", userId);
            return ApiResponse.fail("game is not in robber turn");
        }

        final int cardIndex = request.getCardIndex();
        if (cardIndex < 0 || cardIndex >= room.getPlayerCards().size()) {
            log.info("card index {} out of range", cardIndex);
            return ApiResponse.fail("card index out of range");
        }
        if (userId.equals(room.getSeats().get(cardIndex))) {
            log.info("can not rob yourself");
            return ApiResponse.fail("can not rob yourself");
        }

        // Get the role card that will be robbed before the swap happens
        RoleCard robbedRoleCard = room.getPlayerCards().get(cardIndex);
        
        // Fire event to state machine to handle the swap
        GameContext gameContext = GameContext.builder()
                .room(room)
                .robTargetIndex(cardIndex)
                .build();
        room.getGameStateMachine().sendEvent(GameEvent.ROBBER_ACT, gameContext);
        
        RobberData data = RobberData.builder().roleCard(robbedRoleCard).build();
        return ApiResponse.success(data);
    }

    @PutMapping("/player/{userId}/troublemaker-turn")
    public ApiResponse<Void> troublemakerTurn(@PathVariable("userId") String userId,
                                              @RequestBody TroubleMakerRequest request) {

        Player player = playerManager.get(userId);
        if (player == null) {
            return ApiResponse.fail("player not exist");
        }
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            return ApiResponse.fail("room not exist");
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.TROUBLEMAKER_TURN) {
            log.info("player {} game is not in troublemaker turn", userId);
            return ApiResponse.fail("game is not in troublemaker turn");
        }

        final int[] cardIndices = request.getCardIndices();
        if (cardIndices.length != 2) {
            log.info("card indices length must be 2");
            return ApiResponse.fail("card indices length must be 2");
        }
        if (cardIndices[0] < 0 || cardIndices[0] >= room.getPlayerCards().size()) {
            log.info("card index {} out of range", cardIndices[0]);
            return ApiResponse.fail("card index out of range");
        }
        if (cardIndices[1] < 0 || cardIndices[1] >= room.getPlayerCards().size()) {
            log.info("card index {} out of range", cardIndices[1]);
            return ApiResponse.fail("card index out of range");
        }
        
        // Fire event to state machine to handle the swap
        GameContext gameContext = GameContext.builder()
                .room(room)
                .troublemakerIndex1(cardIndices[0])
                .troublemakerIndex2(cardIndices[1])
                .build();
        room.getGameStateMachine().sendEvent(GameEvent.TROUBLEMAKER_ACT, gameContext);
        
        return ApiResponse.success();
    }

    @PutMapping("/player/{userId}/drunk-turn")
    public ApiResponse<Void> drunkTurn(@PathVariable("userId") String userId,
                                       @RequestBody DrunkRequest request) {
        Player player = playerManager.get(userId);
        if (player == null) {
            return ApiResponse.fail("player not exist");
        }
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            return ApiResponse.fail("room not exist");
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.DRUNK_TURN) {
            log.info("player {} game is not in drunk turn", userId);
            return ApiResponse.fail("game is not in drunk turn");
        }

        final int cardIndex = request.getCardIndex();
        if (cardIndex < 0 || cardIndex >= room.getCenterCards().size()) {
            log.info("card index {} out of range", cardIndex);
            return ApiResponse.fail("card index out of range");
        }
        
        // Fire event to state machine to handle the swap
        GameContext gameContext = GameContext.builder()
                .room(room)
                .drunkCenterIndex(cardIndex)
                .build();
        room.getGameStateMachine().sendEvent(GameEvent.DRUNK_ACT, gameContext);
        
        return ApiResponse.success();
    }

    @GetMapping("/player/{userId}/insomniac-turn")
    public ApiResponse<GetInsomniacData> insomniacTurn(@PathVariable("userId") String userId) {
        Player player = playerManager.get(userId);
        if (player == null) {
            return ApiResponse.fail("player not exist");
        }
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            return ApiResponse.fail("room not exist");
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.INSOMNIAC_TURN) {
            log.info("player {} game is not in insomniac turn", userId);
            return ApiResponse.fail("game is not in insomniac turn");
        }

        int playerSeatNum = room.getSeats().indexOf(userId);
        RoleCard roleCard = room.getPlayerCards().get(playerSeatNum);
        GetInsomniacData data = GetInsomniacData.builder().roleCard(roleCard).build();
        return ApiResponse.success(data);
    }

    @PostMapping("/player/{userId}/vote/{targetPlayerIndex}")
    public void vote(@PathVariable("userId") String userId, @PathVariable("targetPlayerIndex") int targetPlayerIndex) {
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
        if (targetPlayerIndex < -1 || targetPlayerIndex >= room.getSelectedCards().size()) {
            log.info("voted target index {} invalid", targetPlayerIndex);
            return;
        }
        int seatNum = room.getSeats().indexOf(voter.getUserId());
        if (seatNum < 0) {
            log.info("player {} not seated", userId);
            return;
        }
        if (targetPlayerIndex == seatNum) {
            log.info("player {} cannot vote for themselves", userId);
            return;
        }
        room.getVotes().set(seatNum, targetPlayerIndex);
    }

    @PostMapping("/player/{userId}/vote/done")
    public void voteDone(@PathVariable("userId") String userId) {
        Player player = playerManager.getOrCreate(userId);
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            log.info("player {} room not exist", userId);
            return;
        }
        if (!room.getHostPlayer().equals(player)) {
            log.info("non host player cannot end voting");
            return;
        }
        room.getGameStateMachine().sendEvent(GameEvent.VOTE_COMPLETE, GameContext.builder().room(room).build());
    }

    @GetMapping("/player/{userId}/vote/result")
    public ApiResponse<Map<String, Integer>> getVoteResult(@PathVariable("userId") String userId) {
        Player player = playerManager.getOrCreate(userId);
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            log.info("player {} room not exist", userId);
            return ApiResponse.fail("room not exist");
        }
        if (room.getGameStateMachine().getCurrentState() != GameState.END) {
            log.info("Game not ends, cannot get vote result");
            return ApiResponse.fail("game not ended");
        }
        return ApiResponse.success(room.getVoteResult());
    }

    @PostMapping("/player/{userId}/restart")
    public ApiResponse<Void> restart(@PathVariable("userId") String userId) {
        Player player = playerManager.get(userId);
        if (player == null) {
            return ApiResponse.fail("player not exist");
        }
        Room room = roomManager.lookup(player.getRoomId());
        if (room == null) {
            return ApiResponse.fail("room not exist");
        }
        if (!room.getHostPlayer().equals(player)) {
            return ApiResponse.fail("only host player can restart");
        }
        GameContext gameContext = GameContext.builder().room(room).build();
        room.getGameStateMachine().sendEvent(GameEvent.RESTART, gameContext);
        return ApiResponse.success();
    }

}
