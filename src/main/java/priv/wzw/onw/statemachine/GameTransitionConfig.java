package priv.wzw.onw.statemachine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import priv.wzw.onw.*;
import priv.wzw.onw.event.PhaseChangedEvent;
import priv.wzw.onw.event.RoomStateChangeEvent;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GameTransitionConfig {

    private static final List<RoleCard> ROLE_ORDER = Arrays.asList(
            RoleCard.WEREWOLF,
            RoleCard.MINION,
            RoleCard.SEER,
            RoleCard.ROBBER,
            RoleCard.TROUBLEMAKER,
            RoleCard.DRUNK,
            RoleCard.INSOMNIAC
    );

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private final SimpMessagingTemplate template;
    private final JacksonUtils jacksonUtils;

    @Bean
    Transition<GameState, GameEvent, GameContext> initToStarted() {
        return new Transition<>(GameState.INIT, GameEvent.START, GameState.STARTED,
                acceptAndScheduleNext(GamePhase.GAME_START, GameEvent.START, 
                        gameContext -> gameContext.getRoom().getGameStartDelaySeconds()));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> startedToWerewolfTurn() {
        return new Transition<>(GameState.STARTED, GameEvent.START, GameState.WEREWOLF_TURN,
                acceptAndScheduleTurnEnd(GamePhase.WEREWOLF_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> werewolfAct() {
        return new Transition<>(GameState.WEREWOLF_TURN, GameEvent.WEREWOLF_ACT, GameState.WEREWOLF_DONE,
                gameContext -> {
                    Integer index = gameContext.getWerewolfCenterCardIndex();
                    if (index == null || index >= Room.CENTER_SIZE || index < 0) {
                        return false;
                    }
                    List<RoleCard> playerCards = gameContext.getRoom().getPlayerCards();
                    long wolfCount = playerCards.stream().filter(RoleCard.WEREWOLF::equals).count();
                    return 1 == wolfCount;
                });
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> werewolfToMinionTurn() {
        return new Transition<>(EnumSet.of(GameState.WEREWOLF_TURN, GameState.WEREWOLF_DONE),
                GameEvent.TURN_END, GameState.MINION_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.WEREWOLF, RoleCard.MINION),
                acceptAndScheduleTurnEnd(GamePhase.MINION_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> werewolfToSeerTurn() {
        return new Transition<>(EnumSet.of(GameState.WEREWOLF_TURN, GameState.WEREWOLF_DONE),
                GameEvent.TURN_END, GameState.SEER_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.WEREWOLF, RoleCard.SEER),
                acceptAndScheduleTurnEnd(GamePhase.SEER_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> werewolfToRobberTurn() {
        return new Transition<>(EnumSet.of(GameState.WEREWOLF_TURN, GameState.WEREWOLF_DONE),
                GameEvent.TURN_END, GameState.ROBBER_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.WEREWOLF, RoleCard.ROBBER),
                acceptAndScheduleTurnEnd(GamePhase.ROBBER_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> werewolfToTroublemakerTurn() {
        return new Transition<>(EnumSet.of(GameState.WEREWOLF_TURN, GameState.WEREWOLF_DONE),
                GameEvent.TURN_END, GameState.TROUBLEMAKER_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.WEREWOLF, RoleCard.TROUBLEMAKER),
                acceptAndScheduleTurnEnd(GamePhase.TROUBLEMAKER_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> werewolfToDrunkTurn() {
        return new Transition<>(EnumSet.of(GameState.WEREWOLF_TURN, GameState.WEREWOLF_DONE),
                GameEvent.TURN_END, GameState.DRUNK_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.WEREWOLF, RoleCard.DRUNK),
                acceptAndScheduleTurnEnd(GamePhase.DRUNK_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> werewolfToInsomniacTurn() {
        return new Transition<>(EnumSet.of(GameState.WEREWOLF_TURN, GameState.WEREWOLF_DONE),
                GameEvent.TURN_END, GameState.INSOMNIAC_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.WEREWOLF, RoleCard.INSOMNIAC),
                acceptAndScheduleTurnEnd(GamePhase.INSOMNIAC_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> werewolfToVote() {
        return new Transition<>(EnumSet.of(GameState.WEREWOLF_TURN, GameState.WEREWOLF_DONE),
                GameEvent.TURN_END, GameState.VOTING,
                gameContext -> hasNoSpecialRolesAfter(gameContext, RoleCard.WEREWOLF),
                gameContext -> {
                    PhaseChangedEvent event = PhaseChangedEvent.builder().gamePhase(GamePhase.VOTE_TURN).build();
                    String roomId = gameContext.getRoom().getId();
                    template.convertAndSend("/topic/room/" + roomId, jacksonUtils.toJson(event));
                });
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> minionAct() {
        return new Transition<>(GameState.MINION_TURN, GameEvent.MINION_ACT, GameState.MINION_DONE);
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> minionToSeerTurn() {
        return new Transition<>(EnumSet.of(GameState.MINION_TURN, GameState.MINION_DONE),
                GameEvent.TURN_END, GameState.SEER_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.MINION, RoleCard.SEER),
                acceptAndScheduleTurnEnd(GamePhase.SEER_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> minionToRobberTurn() {
        return new Transition<>(EnumSet.of(GameState.MINION_TURN, GameState.MINION_DONE),
                GameEvent.TURN_END, GameState.ROBBER_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.MINION, RoleCard.ROBBER),
                acceptAndScheduleTurnEnd(GamePhase.ROBBER_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> minionToTroublemakerTurn() {
        return new Transition<>(EnumSet.of(GameState.MINION_TURN, GameState.MINION_DONE),
                GameEvent.TURN_END, GameState.TROUBLEMAKER_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.MINION, RoleCard.TROUBLEMAKER),
                acceptAndScheduleTurnEnd(GamePhase.TROUBLEMAKER_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> minionToDrunkTurn() {
        return new Transition<>(EnumSet.of(GameState.MINION_TURN, GameState.MINION_DONE),
                GameEvent.TURN_END, GameState.DRUNK_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.MINION, RoleCard.DRUNK),
                acceptAndScheduleTurnEnd(GamePhase.DRUNK_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> minionToInsomniacTurn() {
        return new Transition<>(EnumSet.of(GameState.MINION_TURN, GameState.MINION_DONE),
                GameEvent.TURN_END, GameState.INSOMNIAC_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.MINION, RoleCard.INSOMNIAC),
                acceptAndScheduleTurnEnd(GamePhase.INSOMNIAC_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> minionToVote() {
        return new Transition<>(EnumSet.of(GameState.MINION_TURN, GameState.MINION_DONE),
                GameEvent.TURN_END, GameState.VOTING,
                gameContext -> hasNoSpecialRolesAfter(gameContext, RoleCard.MINION),
                gameContext -> {
                    PhaseChangedEvent event = PhaseChangedEvent.builder().gamePhase(GamePhase.VOTE_TURN).build();
                    String roomId = gameContext.getRoom().getId();
                    template.convertAndSend("/topic/room/" + roomId, jacksonUtils.toJson(event));
                });
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> seerAct() {
        return new Transition<>(GameState.SEER_TURN, GameEvent.SEER_ACT, GameState.SEER_DONE);
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> seerToRobberTurn() {
        return new Transition<>(EnumSet.of(GameState.SEER_TURN, GameState.SEER_DONE),
                GameEvent.TURN_END, GameState.ROBBER_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.SEER, RoleCard.ROBBER),
                acceptAndScheduleTurnEnd(GamePhase.ROBBER_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> seerToTroublemakerTurn() {
        return new Transition<>(EnumSet.of(GameState.SEER_TURN, GameState.SEER_DONE),
                GameEvent.TURN_END, GameState.TROUBLEMAKER_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.SEER, RoleCard.TROUBLEMAKER),
                acceptAndScheduleTurnEnd(GamePhase.TROUBLEMAKER_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> seerToDrunkTurn() {
        return new Transition<>(EnumSet.of(GameState.SEER_TURN, GameState.SEER_DONE),
                GameEvent.TURN_END, GameState.DRUNK_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.SEER, RoleCard.DRUNK),
                acceptAndScheduleTurnEnd(GamePhase.DRUNK_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> seerToInsomniacTurn() {
        return new Transition<>(EnumSet.of(GameState.SEER_TURN, GameState.SEER_DONE),
                GameEvent.TURN_END, GameState.INSOMNIAC_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.SEER, RoleCard.INSOMNIAC),
                acceptAndScheduleTurnEnd(GamePhase.INSOMNIAC_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> seerToVote() {
        return new Transition<>(EnumSet.of(GameState.SEER_TURN, GameState.SEER_DONE),
                GameEvent.TURN_END, GameState.VOTING,
                gameContext -> hasNoSpecialRolesAfter(gameContext, RoleCard.SEER),
                gameContext -> {
                    PhaseChangedEvent event = PhaseChangedEvent.builder().gamePhase(GamePhase.VOTE_TURN).build();
                    String roomId = gameContext.getRoom().getId();
                    template.convertAndSend("/topic/room/" + roomId, jacksonUtils.toJson(event));
                });
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> robberAct() {
        return new Transition<>(GameState.ROBBER_TURN, GameEvent.ROBBER_ACT, GameState.ROBBER_DONE,
                gameContext -> {
                    int sourceIndex = gameContext.getRoom().getPlayerCards().indexOf(RoleCard.ROBBER);
                    int targetIndex = gameContext.getRobTargetIndex();
                    Collections.swap(gameContext.getRoom().getPlayerCards(), sourceIndex, targetIndex);
                });
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> robberToTroublemakerTurn() {
        return new Transition<>(EnumSet.of(GameState.ROBBER_TURN, GameState.ROBBER_DONE),
                GameEvent.TURN_END, GameState.TROUBLEMAKER_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.ROBBER, RoleCard.TROUBLEMAKER),
                acceptAndScheduleTurnEnd(GamePhase.TROUBLEMAKER_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> robberToDrunkTurn() {
        return new Transition<>(EnumSet.of(GameState.ROBBER_TURN, GameState.ROBBER_DONE),
                GameEvent.TURN_END, GameState.DRUNK_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.ROBBER, RoleCard.DRUNK),
                acceptAndScheduleTurnEnd(GamePhase.DRUNK_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> robberToInsomniacTurn() {
        return new Transition<>(EnumSet.of(GameState.ROBBER_TURN, GameState.ROBBER_DONE),
                GameEvent.TURN_END, GameState.INSOMNIAC_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.ROBBER, RoleCard.INSOMNIAC),
                acceptAndScheduleTurnEnd(GamePhase.INSOMNIAC_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> robberToVote() {
        return new Transition<>(EnumSet.of(GameState.ROBBER_TURN, GameState.ROBBER_DONE),
                GameEvent.TURN_END, GameState.VOTING,
                gameContext -> hasNoSpecialRolesAfter(gameContext, RoleCard.ROBBER),
                gameContext -> {
                    PhaseChangedEvent event = PhaseChangedEvent.builder().gamePhase(GamePhase.VOTE_TURN).build();
                    String roomId = gameContext.getRoom().getId();
                    template.convertAndSend("/topic/room/" + roomId, jacksonUtils.toJson(event));
                });
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> troublemakerAct() {
        return new Transition<>(GameState.TROUBLEMAKER_TURN, GameEvent.TROUBLEMAKER_ACT, GameState.TROUBLEMAKER_DONE,
                gameContext -> {
                    List<RoleCard> roleCards = gameContext.getRoom().getPlayerCards();
                    int index1 = gameContext.getTroublemakerIndex1();
                    int index2 = gameContext.getTroublemakerIndex2();
                    Collections.swap(roleCards, index1, index2);
                });
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> troublemakerToDrunkTurn() {
        return new Transition<>(EnumSet.of(GameState.TROUBLEMAKER_TURN, GameState.TROUBLEMAKER_DONE),
                GameEvent.TURN_END, GameState.DRUNK_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.TROUBLEMAKER, RoleCard.DRUNK),
                acceptAndScheduleTurnEnd(GamePhase.DRUNK_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> troublemakerToInsomniacTurn() {
        return new Transition<>(EnumSet.of(GameState.TROUBLEMAKER_TURN, GameState.TROUBLEMAKER_DONE),
                GameEvent.TURN_END, GameState.INSOMNIAC_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.TROUBLEMAKER, RoleCard.INSOMNIAC),
                acceptAndScheduleTurnEnd(GamePhase.INSOMNIAC_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> troublemakerToVote() {
        return new Transition<>(EnumSet.of(GameState.TROUBLEMAKER_TURN, GameState.TROUBLEMAKER_DONE),
                GameEvent.TURN_END, GameState.VOTING,
                gameContext -> hasNoSpecialRolesAfter(gameContext, RoleCard.TROUBLEMAKER),
                gameContext -> {
                    PhaseChangedEvent event = PhaseChangedEvent.builder().gamePhase(GamePhase.VOTE_TURN).build();
                    String roomId = gameContext.getRoom().getId();
                    template.convertAndSend("/topic/room/" + roomId, jacksonUtils.toJson(event));
                });
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> drunkAct() {
        return new Transition<>(GameState.DRUNK_TURN, GameEvent.DRUNK_ACT, GameState.DRUNK_DONE,
                gameContext -> {
                    List<RoleCard> centerCards = gameContext.getRoom().getCenterCards();
                    List<RoleCard> playerCards = gameContext.getRoom().getPlayerCards();
                    RoleCard temp = centerCards.get(gameContext.getDrunkCenterIndex());
                    int drunkIndex = playerCards.indexOf(RoleCard.DRUNK);
                    playerCards.set(drunkIndex, temp);
                    centerCards.set(drunkIndex, RoleCard.DRUNK);
                });
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> drunkToInsomniacTurn() {
        return new Transition<>(EnumSet.of(GameState.DRUNK_TURN, GameState.DRUNK_DONE),
                GameEvent.TURN_END, GameState.INSOMNIAC_TURN,
                gameContext -> canTransitionTo(gameContext, RoleCard.DRUNK, RoleCard.INSOMNIAC),
                acceptAndScheduleTurnEnd(GamePhase.INSOMNIAC_TURN));
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> drunkToVote() {
        return new Transition<>(EnumSet.of(GameState.DRUNK_TURN, GameState.DRUNK_DONE),
                GameEvent.TURN_END, GameState.VOTING,
                gameContext -> hasNoSpecialRolesAfter(gameContext, RoleCard.DRUNK),
                gameContext -> {
                    PhaseChangedEvent event = PhaseChangedEvent.builder().gamePhase(GamePhase.VOTE_TURN).build();
                    String roomId = gameContext.getRoom().getId();
                    template.convertAndSend("/topic/room/" + roomId, jacksonUtils.toJson(event));
                });
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> insomniacAct() {
        return new Transition<>(GameState.INSOMNIAC_TURN, GameEvent.INSOMNIAC_ACT, GameState.INSOMNIAC_DONE);
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> insomniacToVote() {
        return new Transition<>(EnumSet.of(GameState.INSOMNIAC_TURN, GameState.INSOMNIAC_DONE),
                GameEvent.TURN_END, GameState.VOTING,
                gameContext -> hasNoSpecialRolesAfter(gameContext, RoleCard.INSOMNIAC),
                gameContext -> {
                    PhaseChangedEvent event = PhaseChangedEvent.builder().gamePhase(GamePhase.VOTE_TURN).build();
                    String roomId = gameContext.getRoom().getId();
                    template.convertAndSend("/topic/room/" + roomId, jacksonUtils.toJson(event));
                });
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> votingToEnd() {
        return new Transition<>(GameState.VOTING, GameEvent.VOTE_COMPLETE, GameState.END,
                gameContext -> {
                    Room room = gameContext.getRoom();
                    PhaseChangedEvent event = PhaseChangedEvent.builder().gamePhase(GamePhase.GAME_OVER).build();
                    template.convertAndSend("/topic/room/" + room.getId(), jacksonUtils.toJson(event));
                });
    }

    @Bean
    Transition<GameState, GameEvent, GameContext> restart(Converters converters) {
        return new Transition<>(GameState.END, GameEvent.RESTART, GameState.INIT,
                gameContext -> {
                    Room room = gameContext.getRoom();
                    PhaseChangedEvent event = PhaseChangedEvent.builder().gamePhase(GamePhase.PREPARE).build();
                    template.convertAndSend("/topic/room/" + room.getId(), jacksonUtils.toJson(event));

                    room.reset();
                    RoomStateChangeEvent roomStateChangeEvent = new RoomStateChangeEvent(
                            converters.toDTO(room));
                    template.convertAndSend("/topic/room/" + room.getId(),
                            jacksonUtils.toJson(roomStateChangeEvent));
                });
    }


    private Consumer<GameContext> acceptAndScheduleTurnEnd(GamePhase gamePhase) {
        return acceptAndScheduleNext(gamePhase, GameEvent.TURN_END, 
                gameContext -> gameContext.getRoom().getTurnDurationSeconds());
    }

    private Consumer<GameContext> acceptAndScheduleNext(GamePhase gamePhase,
                                                        GameEvent nextEvent, 
                                                        java.util.function.Function<GameContext, Integer> delaySecondsProvider) {
        return gameContext -> {
            PhaseChangedEvent event = PhaseChangedEvent.builder().gamePhase(gamePhase).build();
            String roomId = gameContext.getRoom().getId();
            template.convertAndSend("/topic/room/" + roomId, jacksonUtils.toJson(event));
            GameStateMachine gameStateMachine = gameContext.getRoom().getGameStateMachine();
            int delaySeconds = delaySecondsProvider.apply(gameContext);
            scheduler.schedule(
                    () -> gameStateMachine.sendEvent(nextEvent, gameContext),
                    delaySeconds, TimeUnit.SECONDS);
        };
    }

    private boolean canTransitionTo(GameContext gameContext, RoleCard currentRole, RoleCard targetRole) {
        List<RoleCard> playerCards = gameContext.getRoom().getSelectedCards();
        
        if (!playerCards.contains(targetRole)) {
            return false;
        }
        
        int currentIndex = ROLE_ORDER.indexOf(currentRole);
        int targetIndex = ROLE_ORDER.indexOf(targetRole);
        
        for (int i = currentIndex + 1; i < targetIndex; i++) {
            if (playerCards.contains(ROLE_ORDER.get(i))) {
                return false;
            }
        }
        
        return true;
    }

    private boolean hasNoSpecialRolesAfter(GameContext gameContext, RoleCard currentRole) {
        List<RoleCard> playerCards = gameContext.getRoom().getSelectedCards();
        int currentIndex = ROLE_ORDER.indexOf(currentRole);
        
        for (int i = currentIndex + 1; i < ROLE_ORDER.size(); i++) {
            if (playerCards.contains(ROLE_ORDER.get(i))) {
                return false;
            }
        }
        
        return true;
    }

}
