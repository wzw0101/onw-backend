package priv.wzw.onw;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import priv.wzw.onw.event.PlayerLeftEvent;
import priv.wzw.onw.statemachine.GameContext;
import priv.wzw.onw.statemachine.GameStateMachine;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

@Data
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class Room {
    public static final int CENTER_SIZE = 3;

    private final String id = UUID.randomUUID().toString();
    private final Set<Player> players = new HashSet<>();
    private final Set<RoleCard> selectedCards = new HashSet<>();
    private final List<RoleCard> playerInitialCards = new ArrayList<>();
    private final List<RoleCard> playerCards = new ArrayList<>();
    private final List<RoleCard> centerCards = new ArrayList<>();
    private final List<Boolean> readyList = new ArrayList<>();
    private final List<AtomicInteger> votes = new ArrayList<>();
    private final List<String> seats = new ArrayList<>();
    private final Deque<PlayerColor> colorPool = new ArrayDeque<>();
    private final GameStateMachine gameStateMachine;

    private Player hostPlayer;
    @Autowired
    private RoomManager roomManager;

    public boolean accept(Player player) {
        // reject if table is full
        if (players.size() >= selectedCards.size()) {
            return false;
        }

        players.add(player);
        player.setColor(colorPool.removeLast());
        return true;
    }

    public void remove(Player player, PlayerLeftEvent event) {
        if (!players.contains(player)) {
            return;
        }

        int seatNum = seats.indexOf(player.getUserId());
        if (seatNum > 0) {
            seats.set(seatNum, null);
            readyList.set(seatNum, false);
        }

        players.remove(player);
        colorPool.addLast(player.getColor());

        if (players.isEmpty()) {
            roomManager.remove(id);
        } else if (hostPlayer.equals(player)) {
            hostPlayer = new ArrayList<>(players).get(0);
        }
        event.setHostChanged(true);
        event.setCurrentHostId(hostPlayer.getUserId());
    }

    public boolean takeSeat(String userId, int seatNum) {
        if (seats.get(seatNum) != null) {
            log.info("can not take the seat {} cause it is seated by player {}", seatNum, seats.get(seatNum));
            return false;
        }

        int prevSeatNum = seats.indexOf(userId);
        if (prevSeatNum >= 0 && readyList.get(prevSeatNum)) {
            log.info("can not take the seat since user {} is ready", userId);
            return false;
        }

        if (prevSeatNum >= 0) {
            seats.set(prevSeatNum, null);
        }
        seats.set(seatNum, userId);

        return true;
    }

    public void leaveSeat(String userId) {
        int seatNum = seats.indexOf(userId);
        if (seatNum < 0) {
            return;
        }
        seats.set(seatNum, null);
        readyList.set(seatNum, false);
    }

    public boolean updateReadyState(String userId, boolean ready) {
        int seatNum = seats.indexOf(userId);
        if (seatNum < 0 || Boolean.compare(ready, readyList.get(seatNum)) == 0) {
            return false;
        }
        readyList.set(seatNum, ready);
        return true;
    }

    public void start() {
        // if table is not full
        if (players.size() != playerCards.size()) {
            log.info("need more players to start game, current {}, need {}", players.size(), playerCards.size());
            return;
        }
        if (readyList.stream().anyMatch(ready -> !ready)) {
            log.info("can not start the game because not all players are ready");
            return;
        }
        gameStateMachine.sendEvent(GameEvent.START, GameContext.builder().room(this).build());
    }

    public void reset() {
        shuffleRoleCards();

        readyList.clear();
        votes.clear();
        for (int i = 0; i < playerCards.size(); i += 1) {
            readyList.add(false);
            votes.add(new AtomicInteger(0));
        }
    }

    public void shuffleRoleCards() {
        ArrayList<RoleCard> shuffled = new ArrayList<>(selectedCards);
        Collections.shuffle(shuffled);
        centerCards.clear();
        centerCards.addAll(shuffled.subList(shuffled.size() - CENTER_SIZE, shuffled.size()));
        playerCards.clear();
        playerCards.addAll(shuffled.subList(0, shuffled.size() - CENTER_SIZE));
        playerInitialCards.clear();
        playerInitialCards.addAll(playerCards);
    }

}
