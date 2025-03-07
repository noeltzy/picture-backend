package com.zhongyuan.tengpicturebackend.api.aliyunai.model.outPainting;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.common.ApiRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class CreateOutPaintingTaskRequest implements Serializable, ApiRequest {

    /**
     * 模型，例如 "image-out-painting"
     */
    private String model = "image-out-painting";

    /**
     * 输入图像信息
     */
    private Input input;

    /**
     * 图像处理参数
     */
    private Parameters parameters;

    @Data
    public static class Input {
        /**
         * 必选，图像 URL
         */
        @Alias("image_url")
        private String imageUrl;
    }

    @Data
    public static class Parameters implements Serializable {
        /**
         * 可选，逆时针旋转角度，默认值 0，取值范围 [0, 359]
         */
        private Integer angle=0;

        /**
         * 可选，输出图像的宽高比，默认空字符串，不设置宽高比
         * 可选值：["", "1:1", "3:4", "4:3", "9:16", "16:9"]
         */
        @Alias("output_ratio")
        private String outputRatio;

        /**
         * 可选，图像居中，在水平方向上按比例扩展，默认值 1.0，范围 [1.0, 3.0]
         */
        @Alias("x_scale")
        @JsonProperty("xScale")
        private Float xScale;

        /**
         * 可选，图像居中，在垂直方向上按比例扩展，默认值 1.0，范围 [1.0, 3.0]
         */
        @Alias("y_scale")
        @JsonProperty("yScale")
        private Float yScale;

        /**
         * 可选，在图像上方添加像素，默认值 0
         */
        @Alias("top_offset")
        private Integer topOffset;

        /**
         * 可选，在图像下方添加像素，默认值 0
         */
        @Alias("bottom_offset")
        private Integer bottomOffset;

        /**
         * 可选，在图像左侧添加像素，默认值 0
         */
        @Alias("left_offset")
        private Integer leftOffset;

        /**
         * 可选，在图像右侧添加像素，默认值 0
         */
        @Alias("right_offset")
        private Integer rightOffset;

        /**
         * 可选，开启图像最佳质量模式，默认值 false
         * 若为 true，耗时会成倍增加
         */
        @Alias("best_quality")
        private Boolean bestQuality;

        /**
         * 可选，限制模型生成的图像文件大小，默认值 true
         * - 单边长度 <= 10000：输出图像文件大小限制为 5MB 以下
         * - 单边长度 > 10000：输出图像文件大小限制为 10MB 以下
         */
        @Alias("limit_image_size")
        private Boolean limitImageSize;

        /**
         * 可选，添加 "Generated by AI" 水印，默认值 true
         */
        @Alias("add_watermark")
        private Boolean addWatermark = false;
    }
}
