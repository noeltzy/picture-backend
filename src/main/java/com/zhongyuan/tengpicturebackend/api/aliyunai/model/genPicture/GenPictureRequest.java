package com.zhongyuan.tengpicturebackend.api.aliyunai.model.genPicture;


import com.zhongyuan.tengpicturebackend.api.aliyunai.model.common.ApiRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class GenPictureRequest implements Serializable, ApiRequest {
    private String model ="wanx2.1-t2i-turbo";
    private Input input = new Input();
    private Parameters parameters = new Parameters();

    @Data
    public static class Input{
        String prompt;
        String negative_prompt;
    }
    @Data
    public static class Parameters implements Serializable {
        String size="1024*1024";
        int n=1;
        boolean prompt_extend=false;
        boolean watermark=false;
    }

    private static final long serialVersionUID = -521528778285570049L;
}
