package com.zhongyuan.tengpicturebackend.vip.model.entity.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class CreateVipOrderRequest implements Serializable {
    int vipLevel;
}
