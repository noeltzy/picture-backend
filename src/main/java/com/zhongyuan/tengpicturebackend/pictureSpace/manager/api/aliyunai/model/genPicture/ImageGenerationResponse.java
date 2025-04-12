package com.zhongyuan.tengpicturebackend.manager.api.aliyunai.model.genPicture;

import lombok.Data;
import java.util.List;

/**
 * 图像生成API响应对象
 */
@Data
public class ImageGenerationResponse {
    private String request_id;
    private Output output;
    private Usage usage;

    /**
     * 输出信息
     */
    @Data
    public static class Output {
        private String task_id;
        private String task_status;
        private String submit_time;
        private String scheduled_time;
        private String end_time;
        private List<Result> results;
        private TaskMetrics task_metrics;
    }

    /**
     * 生成的图像结果
     */
    @Data
    public static class Result {
        private String orig_prompt;
        private String actual_prompt;
        private String url;
    }

    /**
     * 任务统计信息
     */
    @Data
    public static class TaskMetrics {
        private int TOTAL;
        private int SUCCEEDED;
        private int FAILED;
    }

    /**
     * 使用情况统计
     */
    @Data
    public static class Usage {
        private int image_count;
    }
}