package com.example.match.model.enums;

import com.example.match.model.domain.Team;
import com.example.match.service.TeamService;

/**
 * @description: 队伍状态枚举
 * @date: 2023/5/10 18:50
 */
public enum TeamStatusEnum {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private int value;
    private String text;

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * @description: 根据value值获取枚举类
     * @param value
     * @return com.example.match.model.enums.TeamStatusEnum
     * @date: 2023/5/10 18:57
     */
    public static TeamStatusEnum getEnumByValue(Integer value){
        if (value == null) {
            return null;
        }
        TeamStatusEnum[] teamStatusEnums = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : teamStatusEnums) {
            if(teamStatusEnum.getValue() == value){
                return teamStatusEnum;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
