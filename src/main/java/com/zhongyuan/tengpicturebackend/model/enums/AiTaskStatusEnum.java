package com.zhongyuan.tengpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum AiTaskStatusEnum {

    Running("执行中", "Running"),
    FAILED("失败", "FAILED"),
    SUCCEED("成功", "SUCCEED");
    private final String text;
    private final String value;
    AiTaskStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的 value
     * @return 枚举值
     */
    public static AiTaskStatusEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (AiTaskStatusEnum anEnum : AiTaskStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    /**
     * 获取所有枚举的文本列表
     *
     * @return 文本列表
     */
    public static List<String> getAllTexts() {
        return Arrays.stream(AiTaskStatusEnum.values())
                .map(AiTaskStatusEnum::getText)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有枚举的值列表
     *
     * @return 值列表
     */
    public static List<String> getAllValues() {
        return Arrays.stream(AiTaskStatusEnum.values())
                .map(AiTaskStatusEnum::getValue)
                .collect(Collectors.toList());
    }
}
