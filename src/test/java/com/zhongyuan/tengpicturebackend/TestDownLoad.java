package com.zhongyuan.tengpicturebackend;

import cn.hutool.http.HttpUtil;
import org.junit.jupiter.api.Test;

import java.net.URL;

//普通test类

public class TestDownLoad {

    @Test
    void testDownLoad() {
        String uil ="https://vigen-invi.oss-cn-shanghai.aliyuncs.com/service_dashscope/ImageOutPainting/2025-02-21/public/5c0ac34a-e375-49d3-af0e-9f3b74b7f668/result-96553695-cfb2-458f-98a3-490b42d605bb.jpg?OSSAccessKeyId=LTAI5t7aiMEUzu1F2xPMCdFj&Expires=1740118268&Signature=F4x0K9jlPLwUa6rrJY4VP%2FGuY3g%3D";
        HttpUtil.downloadFile(uil,"./d");
        new Thread();
    }
}
