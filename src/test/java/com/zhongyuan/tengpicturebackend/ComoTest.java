package com.zhongyuan.tengpicturebackend;


import cn.hutool.core.io.file.FileNameUtil;
import org.junit.jupiter.api.Test;

public class ComoTest {
    @Test
    public void test(){
        String url ="https://i0.hdslb.com/bfs/archive/a14553a95fa79a0a5a65aa1feb05626e4384cfa6";
        String name = FileNameUtil.getSuffix(url);
        String extName = FileNameUtil.extName(url);
        System.out.println(name+"\n"+extName);
    }
}
