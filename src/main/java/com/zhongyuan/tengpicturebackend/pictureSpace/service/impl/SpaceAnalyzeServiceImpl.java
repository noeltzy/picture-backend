package com.zhongyuan.tengpicturebackend.pictureSpace.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.pictureSpace.mapper.SpaceMapper;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.space.analyze.*;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Picture;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Space;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.analyze.*;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.PictureService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.SpaceAnalyzeService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.SpaceService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Windows11
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-02-13 09:43:14
 */
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {

    @Resource
    UserService userService;

    @Resource
    SpaceService spaceService;

    @Resource
    PictureService pictureService;


    /**
     * @param spaceUsageAnalyzeRequest 请求参数
     * @param loginUser                当前登录用户 用于鉴权
     * @return 结果
     */
    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyzeResponse(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 分情况权限校验
        Space space = this.checkAnalyzePermission(spaceUsageAnalyzeRequest, loginUser);

        // 构造查询Qw
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 填写查询参数
        this.fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, queryWrapper);
        // 查询分情况
        if (space == null) {
            // 查询的是全部图片||公共图片
            queryWrapper.select("picSize");
            List<Object> list = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long usedSize = list.stream().mapToLong(o -> o instanceof Long ? (Long) o : 0L).sum();
            long usedCount = list.size();
            // 公共图库无其他属性只需要填上述几个
            return new SpaceUsageAnalyzeResponse(usedSize, usedCount);
        }
        return this.spaceToUsageAnalyzeResponse(space);
    }

    /**
     * 各种类别占用情况分析
     *
     * @param spaceCategoryAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyzeResponse(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 2. 权限校验
        checkAnalyzePermission(spaceCategoryAnalyzeRequest, loginUser);

        // 3.QueryWrapper构造
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        // 4.查询 分组查询，每个category的图片数量和图片size
        queryWrapper.select("category AS category", "count(*) as count", "sum(picSize) as totalSize")
                .groupBy("category");
        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream().map(item -> {
                    // 将查询到的map List 转换到对象中保存
                    String category = item.get("category") == null ? "未分类" : item.get("category").toString();
                    Long count = ((Number) item.get("count")).longValue();
                    Long totalSize = ((Number) item.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
                }).collect(Collectors.toList());
    }

    /**
     * 空间标签分析
     *
     * @param spaceTagAnalyzeRequest 请求
     * @param loginUser              登录用户
     * @return 标签分析结果
     */
    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyzeResponse(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 2. 权限校验
        checkAnalyzePermission(spaceTagAnalyzeRequest, loginUser);
        // 3.QueryWrapper构造
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        // 查询各标签的使用情况
        // 1.查询到所有标签字符串
        queryWrapper.select("tags");

        // 查询全部的标签并且合并统计各标签使用次数
        Map<String, Long> collect = pictureService.getBaseMapper()
                .selectObjs(queryWrapper).stream()
                .filter(ObjUtil::isNotNull)
                .map(str -> JSONUtil.toList((String) str, String.class))
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));

        // 4.构造返回结果
        return collect.entrySet().stream()
                // 降序排列
                .sorted(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyzeResponse(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 2. 权限校验
        checkAnalyzePermission(spaceUserAnalyzeRequest, loginUser);
        // 3.QueryWrapper构造
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE_FORMAT(create_time, '%Y-%m-%d') as period", "count(*) as count");
                break;
            case "week":
                queryWrapper.select("DATE_FORMAT(create_time, '%Y-%u') as period", "count(*) as count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(create_time, '%Y-%m') as period", "count(*) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "时间维度错误");
        }
        queryWrapper.groupBy("period").orderByAsc("period");
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(entry -> {
            long l = ((Number) entry.get("count")).longValue();
            String period = (String) entry.get("period");
            return new SpaceUserAnalyzeResponse(period,count());
        }).collect(Collectors.toList());
    }

    /**
     * 返回空间排行TOPN
     * @param spaceRankAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<Space> getSpaceRankAnalyzeResponse(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        userService.isAdmin(loginUser);
        Integer topN = spaceRankAnalyzeRequest.getTopN();
        if(topN==null||topN<=0||topN>30){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"topN错误");
        }
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "useId", "totalSize")
                .orderByDesc("totalSize")
                .last("limit " + topN);
        return spaceService.list(queryWrapper);
    }

    /**
     * 根据space对象构造SpaceUsageAnalyzeResponse
     *
     * @param space 查询到的空间
     * @return space对象转换为spaceUsageAnalyzeResponse对象
     */
    private SpaceUsageAnalyzeResponse spaceToUsageAnalyzeResponse(Space space) {
        Long usedSize = space.getTotalSize();
        Long maxSize = space.getMaxSize();
        double sizeUsageRatio = NumberUtil.round((double) usedSize * 100.0 / maxSize, 3).doubleValue();
        long usedCount = space.getTotalCount();
        long maxCount = space.getMaxCount();
        double countUsageRatio = NumberUtil.round((double) usedCount * 100.0 / maxCount, 3).doubleValue();
        return new SpaceUsageAnalyzeResponse(usedSize, maxSize, sizeUsageRatio, usedCount, maxCount, countUsageRatio);
    }

    /**
     * 根据通用请求类 填写通用请求参数
     *
     * @param spaceAnalyzeRequest
     * @param queryWrapper
     */
    private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean isQueryAll = spaceAnalyzeRequest.isQueryAll();
        boolean isQueryPublic = spaceAnalyzeRequest.isQueryPublic();

        if (isQueryAll) {
            return;
        }
        if (isQueryPublic) {
            queryWrapper.isNull("spaceId");
            return;
        }
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }


    /**
     * 检查分析所需要的全部参数的权限
     *
     * @param spaceAnalyzeRequest
     * @param loginUser
     */
    private Space checkAnalyzePermission(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean isQueryAll = spaceAnalyzeRequest.isQueryAll();
        boolean isQueryPublic = spaceAnalyzeRequest.isQueryPublic();

        // 查询公共和查询全部需要管理员权限
        if (isQueryAll || isQueryPublic) {
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
            return null;
        }
        // 查询具体空间需要本人或者管理员
        if (spaceId != null) {
            //空间是否存在
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
            //本人或者管理员
            spaceService.checkOwnerOrAdmin(loginUser, space);
            return space;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
}




