package com.example.match.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用删除请求
 */
@Data
public class DeleteRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -2286314039327430628L;
    private long id;
}
