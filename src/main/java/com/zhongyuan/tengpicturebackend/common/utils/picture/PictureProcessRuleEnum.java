package com.zhongyuan.tengpicturebackend.common.utils.picture;


import lombok.Getter;

@Getter
public enum PictureProcessRuleEnum {

    COMPRESS("webp"),
    THUMBNAIL("thumbnail");

    private final String value;
    PictureProcessRuleEnum( String value) {
        this.value = value;
    }
    /**
     * 根据value 获取枚举
     * @param value 枚举值的value
     * @return 枚举值
     */
   public static PictureProcessRuleEnum getEnumByValue(String value) {
        if(value==null) return null;
        for(PictureProcessRuleEnum e : PictureProcessRuleEnum.values()) {
            if(e.value.equals(value)) return e;
        }
        return null;
   }
}
