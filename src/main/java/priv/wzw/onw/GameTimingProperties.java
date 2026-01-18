package priv.wzw.onw;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 游戏时间配置属性
 * 用于参数化游戏各个阶段的持续时间
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "game.timing")
public class GameTimingProperties {
    
    /**
     * 游戏开始阶段的延迟时间（秒）
     */
    private int gameStartDelaySeconds = 3;
    
    /**
     * 默认回合持续时间（秒）
     * 用于所有角色回合（狼人、爪牙、预言家、盗贼、捣蛋鬼、酒鬼、失眠者）
     */
    private int defaultTurnDurationSeconds = 20;
}
