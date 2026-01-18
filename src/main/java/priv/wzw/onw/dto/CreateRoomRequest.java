package priv.wzw.onw.dto;

import lombok.Data;
import priv.wzw.onw.RoleCard;

import java.util.List;

/**
 * 创建房间请求
 */
@Data
public class CreateRoomRequest {
    /**
     * 游戏开始阶段的延迟时间（秒）
     * 如果未提供，使用默认值 3 秒
     */
    private Integer gameStartDelaySeconds;

    /**
     * 默认回合持续时间（秒）
     * 如果未提供，使用默认值 20 秒
     */
    private Integer turnDurationSeconds;

    /**
     * 游戏中选择的角色列表
     * 如果未提供，使用所有可用角色
     */
    private List<RoleCard> roles;
}
