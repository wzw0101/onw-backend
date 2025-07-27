package priv.wzw.onw;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import priv.wzw.onw.statemachine.GameContext;
import priv.wzw.onw.statemachine.GameStateMachine;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
@AllArgsConstructor
public class Room {
    public static final int CENTER_SIZE = 3;

    // generate id of 4 random characters
    private final String id = RandomStringUtils.randomAlphanumeric(4);
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
    private final Map<String, PlayerColor> playerColorMap = new HashMap<>();

    private Player hostPlayer;
    @Autowired
    private RoomManager roomManager;

    public boolean accept(Player player) {
        // reject if table is full
        if (players.size() >= selectedCards.size()) {
            return false;
        }

        players.add(player);
        PlayerColor color = colorPool.removeLast();
        player.setColor(color);
        playerColorMap.put(player.getUserId(), color);
        return true;
    }

    public void remove(Player player) {
        if (!players.contains(player)) {
            return;
        }

        leaveSeat(player.getUserId());

        players.remove(player);
        colorPool.addLast(player.getColor());
        playerColorMap.remove(player.getUserId());

        if (players.isEmpty()) {
            roomManager.remove(id);
        } else if (hostPlayer.equals(player)) {
            hostPlayer = new ArrayList<>(players).get(0);
        }
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
            votes.add(new AtomicInteger(-1));
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

    public int getMostVotedTarget() {
        Map<Integer, Integer> voteCount = new HashMap<>();
        int maxCount = -1;
        int maxCountTarget = -1;
        for (int i = 0; i < votes.size(); i += 1) {
            int target = votes.get(i).get();
            if (target < 0) {
                continue;
            }
            int count = voteCount.getOrDefault(target, 0) + 1;
            voteCount.put(target, count);
            if (count > maxCount) {
                maxCount = count;
                maxCountTarget = target;
            }
        }
        return maxCountTarget;
    }
}
