package priv.wzw.onw;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerManager {
    // TODO player回收机制
    private static final Map<String, Player> allPlayerMap = new HashMap<>();

    private final ObjectFactory<Player> playerObjectFactory;

    public Player getOrCreate(String userId) {
        Player player = allPlayerMap.get(userId);
        if (player == null) {
            player = playerObjectFactory.getObject();
            player.setUserId(userId);
            allPlayerMap.put(userId, player);
        }
        return player;
    }

    public Player get(String userId) {
        Player player = allPlayerMap.get(userId);
        if (player == null) {
            log.info("player {} not exist", userId);
        }
        return player;
    }
}
