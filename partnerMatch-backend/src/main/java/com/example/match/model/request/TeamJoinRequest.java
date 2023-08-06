package com.example.match.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户加入队伍请求体
 *
 * @author yupi
 */
@Data
public class TeamJoinRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -3746393069884526379L;
    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}

