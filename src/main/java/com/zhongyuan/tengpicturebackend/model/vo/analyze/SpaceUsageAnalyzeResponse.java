package com.zhongyuan.tengpicturebackend.model.vo.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceUsageAnalyzeResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 已使用空间大小
     */
    private long usedSize;
    /**
     * 最大空间大小
     */
    private long maxSize;
    /**
     * 空间使用率
     */
    private double sizeUsageRatio;

    /**
     * 已使用数量
     */
    private long usedCount;

    /**
     * 最大数量
     */
    private long maxCount;

    /**
     * 数量使用率
     */
    private double countUsageRatio;


    public SpaceUsageAnalyzeResponse(long usedSize, long usedCount) {
        this.usedSize = usedSize;
        this.usedCount = usedCount;
    }
}
