package com.zhongyuan.tengpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceAddRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceQueryRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.analyze.SpaceCategoryAnalyzeRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.analyze.SpaceTagAnalyzeRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.analyze.SpaceUsageAnalyzeRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.analyze.SpaceUserAnalyzeRequest;
import com.zhongyuan.tengpicturebackend.model.entity.Space;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.vo.LoginUserVo;
import com.zhongyuan.tengpicturebackend.model.vo.SpaceVO;
import com.zhongyuan.tengpicturebackend.model.vo.analyze.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Windows11
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-02-13 09:43:14
*/
public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 查询空间使用情况
     * @param spaceUsageAnalyzeRequest 请求参数
     * @param loginUser 当前登录用户 用于鉴权
     * @return 空间使用情况
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyzeResponse(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     *  类别分析
     * @param spaceCategoryAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyzeResponse(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 标签分析
     * @param spaceTagAnalyzeRequest
     * @param loginUser
     * @return
     *
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyzeResponse(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest,User loginUser);

    /**
     * 用户分析
     * @param spaceUserAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyzeResponse(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 空间排名分析
     * @param spaceRankAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<Space> getSpaceRankAnalyzeResponse(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);

}
