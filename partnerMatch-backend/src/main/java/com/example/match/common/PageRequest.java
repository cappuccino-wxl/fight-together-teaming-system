package com.example.match.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用分页请求
 */
@Data
public class PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 4806016778461317274L;
    // 页面大小
    protected int pageSize = 10;
    // 当前是第几页
    protected int pageNum = 1;
}
