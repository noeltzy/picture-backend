package com.zhongyuan.tengpicturebackend.model.enums;


import lombok.Getter;

@Getter
public enum UserRoleEnum {

    USER("用户","user"),
    ADMIN("管理员","admin");

    private final String text;
    private final String value;
    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
    /**
     * 根据value 获取枚举
     * @param value 枚举值的value
     * @return 枚举值
     */
   public static UserRoleEnum getEnumByValue(String value) {
        if(value==null) return null;
        for(UserRoleEnum e : UserRoleEnum.values()) {
            if(e.value.equals(value)) return e;
        }
        return null;
   }

}
