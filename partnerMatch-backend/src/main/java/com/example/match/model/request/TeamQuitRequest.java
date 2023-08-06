package com.example.match.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户退出队伍请求体
 *
 * @author yupi
 */
@Data
public class TeamQuitRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 5240626907774747463L;
    /**
     * id
     */
    private Long teamId;

}

