package com.zhongyuan.tengpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.mapper.SpaceMapper;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceAddRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceQueryRequest;
import com.zhongyuan.tengpicturebackend.model.entity.Space;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.enums.SpaceLevelEnum;
import com.zhongyuan.tengpicturebackend.model.vo.SpaceVO;
import com.zhongyuan.tengpicturebackend.model.vo.UserVo;
import com.zhongyuan.tengpicturebackend.service.SpaceService;
import com.zhongyuan.tengpicturebackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Windows11
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-02-13 09:43:14
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {


    @Resource
    UserService userService;

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);

        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        // 插入时候的校验
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名不能为空");
            ThrowUtils.throwIf(SpaceLevelEnum.getEnumByValue(spaceLevel) == null, ErrorCode.PARAMS_ERROR, "空间等级设置异常");
        }//编辑时候的校验
        ThrowUtils.throwIf(StrUtil.isBlank(spaceName) || spaceName.length() > 30, ErrorCode.PARAMS_ERROR, "空间名称设置有误");
    }

    @Override
    public LambdaQueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        LambdaQueryWrapper<Space> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null, Space::getId, id);
        queryWrapper.eq(userId != null, Space::getUserId, userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), Space::getSpaceName, spaceName);
        queryWrapper.eq(spaceLevel != null, Space::getSpaceLevel, spaceLevel);
        return queryWrapper;
    }

    @Override
    public List<SpaceVO> toVoList(List<Space> records, HttpServletRequest request) {
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }
        Set<Long> userIds = records.stream().map(Space::getUserId).collect(Collectors.toSet());
        List<User> users = userService.listByIds(userIds);
        Map<Long, UserVo> userMap = users.stream().collect(Collectors.toMap(User::getId, UserVo::obj2Vo));
        return records.stream().map(
                space -> SpaceVO.objToVo(space, userMap.get(space.getUserId())
                )).collect(Collectors.toList());
    }

    @Override
    public void fillSpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum == null) return;
        if (space.getMaxSize() == null) {
            space.setMaxSize(spaceLevelEnum.getMaxSize());
        }
        if (space.getMaxCount() == null) {
            space.setMaxCount(spaceLevelEnum.getMaxCount());
        }
    }

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        //填写默认参数
        if (space.getSpaceName() == null) {
            space.setSpaceName("默认空间");

        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        // 填入空间
        this.fillSpaceLevel(space);
        space.setUserId(loginUser.getId());
        // 权限校验
        this.validSpace(space, true);
        //只有管理员才能创建非普通级别的空间
        if (space.getSpaceLevel() != SpaceLevelEnum.COMMON.getValue() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有管理员才能创建非普通级别的空间");
        }
        //TODO 事务添加是否必要
        // 每个用户只能创建一个空间 加锁
        String lock = String.valueOf(loginUser.getId()).intern();
        synchronized (lock) {
            boolean exists = this.lambdaQuery().eq(Space::getUserId, loginUser.getId()).exists();
            ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户只能创建一个空间");
            boolean save = this.save(space);
            ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "创建失败");
            return space.getId();
        }

    }

    @Override
    public void checkVolume(Space space) {
        Long maxSize = space.getMaxSize();
        Long maxCount = space.getMaxCount();
        Long totalSize = space.getTotalSize();
        Long totalCount = space.getTotalCount();
        ThrowUtils.throwIf(totalSize >= maxSize, ErrorCode.OPERATION_ERROR, "空间容量已满");
        ThrowUtils.throwIf(totalCount >= maxCount, ErrorCode.OPERATION_ERROR, "空间容量已满");
    }

    @Override
    public void updateVolume(Long spaceId, Long picSize) {
        boolean update;
        if(picSize>0){
            update=this.lambdaUpdate().eq(Space::getId, spaceId).setSql("totalSize=totalSize+{0},totalCount=totalCount+1", picSize).update();
        }
        else{
            picSize=Math.abs(picSize);
            update = this.lambdaUpdate().eq(Space::getId, spaceId).setSql("totalSize=totalSize-{0},totalCount=totalCount-1", picSize).update();
        }
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新失败");
    }

    @Override
    public void checkOwnerOrAdmin(User loginUser, Space space) {
        ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
    }
}




