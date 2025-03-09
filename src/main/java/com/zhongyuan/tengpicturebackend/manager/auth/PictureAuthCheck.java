package com.zhongyuan.tengpicturebackend.manager.auth;


import org.springframework.stereotype.Component;

@Component
public class PictureAuthCheck {
    private static final String PUBLIC = "public";
    private static final String PRIVATE = "private";
    private static final String TEAM = "team";
    public static void checkPictureRead(PictureSpaceCheckContext pictureSpaceCheckContext){
        if(PUBLIC.equals(pictureSpaceCheckContext.getSpaceType())){
            return;
        }
        if(PRIVATE.equals(pictureSpaceCheckContext.getSpaceType())){

        }
    }
}
