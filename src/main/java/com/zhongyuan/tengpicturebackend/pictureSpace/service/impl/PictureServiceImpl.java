package com.zhongyuan.tengpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zhongyuan.tengpicturebackend.common.config.CosConfig;
import com.zhongyuan.tengpicturebackend.common.message.PictureProcessMessage;
import com.zhongyuan.tengpicturebackend.common.utils.picture.PictureProcessRuleEnum;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.api.aliyunai.model.common.CreateTaskResponse;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.api.aliyunai.model.genPicture.GenPictureRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.api.aliyunai.model.genPicture.ImageGenerationResponse;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.api.aliyunai.model.outPainting.CreateOutPaintingTaskRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.api.aliyunai.model.outPainting.GetOutPaintingTaskResponse;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.api.aliyunai.service.AliYunApiService;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.cos.CosManager;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.cos.upload.FilePictureUpload;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.cos.upload.PictureUploadTemplate;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.cos.upload.UrlPictureUpload;
import com.zhongyuan.tengpicturebackend.pictureSpace.mapper.PictureMapper;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.file.PictureUploadResult;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.picture.*;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Picture;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Space;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.enums.PictureReviewStatusEnum;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.enums.SpaceRoleEnum;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.enums.UserRoleEnum;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.PictureVo;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.UserVo;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.FileInfoService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.PictureService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.SpaceService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Windows11
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-20 14:12:33
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    FilePictureUpload filePictureUpload;
    @Resource
    UrlPictureUpload urlPictureUpload;
    @Resource
    SpaceService spaceService;
    @Resource
    UserService userService;
    @Resource
    TransactionTemplate transactionTemplate;
    @Resource
    AliYunApiService aliYunApiService;

    @Resource
    FileInfoService fileInfoService;

    @Resource
    private CosConfig cosConfig;
    @Resource
    private CosManager cosManager;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public PictureVo uploadPicture(Object inputSource, PictureUploadRequest uploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断是上传还是更新
        Long picId = uploadRequest.getId();
        Long spaceId = uploadRequest.getSpaceId();

        // 非公共图库上传
        if (spaceId != null) {
            Space space = spaceService.lambdaQuery().eq(Space::getId, spaceId).one();
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 如果空间存在,校验权限
            ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "没有权限");
            // 校验空间容量
            spaceService.checkVolume(space);
        }

        // 如果是更新，id需要存在
        if (picId != null) {
            Picture oldPic = this.getById(picId);
            ThrowUtils.throwIf(oldPic == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //本人或者管理员可继续更新图片
            if (!Objects.equals(oldPic.getUserId(), loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 校验更改图片时候spaceId前后是否一至
            //如果没传递spaceId，则使用旧spaceId
            if (spaceId == null) {
                spaceId = oldPic.getSpaceId();
            } else {
                //如果传递了spaceId，则校验spaceId是否合法
                ThrowUtils.throwIf(!Objects.equals(spaceId, oldPic.getSpaceId()), ErrorCode.PARAMS_ERROR, "图片空间不匹配");
            }
        }
        //正常上传图片
        String filePrefix;
        PictureUploadTemplate fileManager = filePictureUpload;
        if (spaceId == null) {
            //公共图库
            filePrefix = String.format("public/%s", loginUser.getId());
        } else {
            //空间
            filePrefix = String.format("space/%s", spaceId);
        }


        // 根据 inputSource 判断上传方式
        if (inputSource instanceof String) {
            fileManager = urlPictureUpload;
        }
        PictureUploadResult pictureUploadResult = fileManager.uploadPicture(inputSource, filePrefix);


        Picture picture = PictureUploadResult.toPicture(pictureUploadResult, loginUser.getId());

        picture.setSpaceId(spaceId);

        // 仅限批量抓取图片更新：
        String batchFetchDefaultName = uploadRequest.getBatchFetchDefaultName();
        String batchFetchCategory = uploadRequest.getCategory();
        if (StrUtil.isNotBlank(batchFetchDefaultName)) {
            picture.setName(batchFetchDefaultName);
        }
        if (StrUtil.isNotBlank(batchFetchCategory)) {
            picture.setCategory(batchFetchCategory);
        }
        // end

        // 更新图片 需要添加其他字段
        if (picId != null) {
            picture.setId(picId);
            picture.setEditTime(new Date());
        }//end
        //更新审核参数
        this.setReviewParam(picture, loginUser);

        Long finalSpaceId = spaceId;
        //事务
        transactionTemplate.execute(status -> {
            boolean res = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!res, ErrorCode.SYSTEM_ERROR, "保存失败");
            //非公共图库更新容量
            if (finalSpaceId != null) {
                spaceService.updateVolume(finalSpaceId, picture.getPicSize());
            }
            return picture;
        });

        return PictureVo.obj2Vo(picture, UserVo.obj2Vo(loginUser));
    }

    @Override
    public PictureVo uploadPictureMq(Object inputSource, PictureUploadRequest uploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断是上传还是更新
        Long picId = uploadRequest.getId();
        Long spaceId = uploadRequest.getSpaceId();
        // 非公共图库上传
        if (spaceId != null) {
            Space space = spaceService.lambdaQuery().eq(Space::getId, spaceId).one();
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 如果空间存在,校验权限
            ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "没有权限");
            // 校验空间容量
            spaceService.checkVolume(space);
        }

        // 如果是更新，id需要存在
        if (picId != null) {
            // 无需更新
            Picture oldPic = this.getById(picId);
            ThrowUtils.throwIf(oldPic == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //本人或者管理员可继续更新图片
            if (!Objects.equals(oldPic.getUserId(), loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 校验更改图片时候spaceId前后是否一至
            //如果没传递spaceId，则使用旧spaceId
            if (spaceId == null) {
                spaceId = oldPic.getSpaceId();
            } else {
                //如果传递了spaceId，则校验spaceId是否合法
                ThrowUtils.throwIf(!Objects.equals(spaceId, oldPic.getSpaceId()), ErrorCode.PARAMS_ERROR, "图片空间不匹配");
            }
        }
        //正常上传图片
        String filePrefix;
        PictureUploadTemplate fileManager = filePictureUpload;
        if (spaceId == null) {
            //公共图库
            filePrefix = String.format("public/%s", loginUser.getId());
        } else {
            //空间
            filePrefix = String.format("space/%s", spaceId);
        }


        // 根据 inputSource 判断上传方式
        if (inputSource instanceof String) {
            fileManager = urlPictureUpload;
        }
        PictureUploadResult pictureUploadResult = fileManager.uploadPictureMq(inputSource, filePrefix);
        Picture picture = PictureUploadResult.toPicture(pictureUploadResult, loginUser.getId());
        picture.setSpaceId(spaceId);
        // 仅限批量抓取图片更新：
        String batchFetchDefaultName = uploadRequest.getBatchFetchDefaultName();
        String batchFetchCategory = uploadRequest.getCategory();
        if (StrUtil.isNotBlank(batchFetchDefaultName)) {
            picture.setName(batchFetchDefaultName);
        }
        if (StrUtil.isNotBlank(batchFetchCategory)) {
            picture.setCategory(batchFetchCategory);
        }
        // end

        // 更新图片 需要添加其他字段
        if (picId != null) {
            picture.setId(picId);
            picture.setEditTime(new Date());
        }//end
        //更新审核参数
        this.setReviewParam(picture, loginUser);

        Long finalSpaceId = spaceId;
        //事务
        transactionTemplate.execute(status -> {
            boolean res = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!res, ErrorCode.SYSTEM_ERROR, "保存失败");
            //非公共图库更新容量
            if (finalSpaceId != null) {
                spaceService.updateVolume(finalSpaceId, picture.getPicSize());
            }
            return picture;
        });
        // 构建消息
        List<PictureProcessRuleEnum> rules = new ArrayList<>();
        rules.add(PictureProcessRuleEnum.COMPRESS);
        rules.add(PictureProcessRuleEnum.THUMBNAIL);
        PictureProcessMessage message = new PictureProcessMessage();
        message.setPictureId(picture.getId());
        message.setOriginPictureKey(picture.getOriginUrl());
        message.setProcessRules(rules);
        // 发送消息
        rabbitTemplate.convertAndSend("picture.process.topic", "picture.process", message);
        return PictureVo.obj2Vo(picture, UserVo.obj2Vo(loginUser));
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        ThrowUtils.throwIf(picture.getId() == null || picture.getId() <= 0, ErrorCode.PARAMS_ERROR);
        if (StrUtil.isNotBlank(picture.getUrl())) {
            ThrowUtils.throwIf(picture.getUrl().length() > 1024, ErrorCode.PARAMS_ERROR, "URL长度不能超过1024");
        }
    }

    @Override
    public LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Long spaceId = pictureQueryRequest.getSpaceId();


        LambdaQueryWrapper<Picture> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                lambdaQueryWrapper.like(Picture::getTags, String.format("\"%s\"", tag));
            }

        }
        if (StrUtil.isNotBlank(searchText)) {
            lambdaQueryWrapper.and(wrapper -> wrapper.like(Picture::getName, searchText)
                    .or()
                    .like(Picture::getIntroduction, searchText));
        }
        //查公共图库
        if (spaceId == null) {
            lambdaQueryWrapper.isNull(Picture::getSpaceId);
            lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(userId), Picture::getUserId, userId);
        } else {
            //查私有图库
            lambdaQueryWrapper.eq(Picture::getSpaceId, spaceId);
        }
//
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(id), Picture::getId, id);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), Picture::getReviewerId, reviewerId);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picWidth), Picture::getPicWidth, picWidth);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picHeight), Picture::getPicHeight, picHeight);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picSize), Picture::getPicSize, picSize);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), Picture::getReviewStatus, reviewStatus);
        lambdaQueryWrapper.like(ObjUtil.isNotEmpty(picFormat), Picture::getPicFormat, picFormat);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picScale), Picture::getPicScale, picScale);
        lambdaQueryWrapper.eq(StrUtil.isNotBlank(category), Picture::getCategory, category);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(introduction), Picture::getIntroduction, introduction);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(name), Picture::getName, name);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(reviewMessage), Picture::getReviewMessage, reviewMessage);
        lambdaQueryWrapper.orderByDesc(Picture::getEditTime);
        return lambdaQueryWrapper;
    }


    @Override
    public void reviewPicture(PictureReviewRequest pictureReviewRequest, User user) {
        //校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum statusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        ThrowUtils.throwIf(statusEnum == null || id == null, ErrorCode.PARAMS_ERROR);
        //判断图片是否存在
        Picture picture = getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        //判断当前用户是否为管理员
        ThrowUtils.throwIf(!UserRoleEnum.ADMIN.getValue().equals(user.getUserRole()), ErrorCode.NO_AUTH_ERROR);
        //审核状态不能重复
        ThrowUtils.throwIf(reviewStatus.equals(picture.getReviewStatus()) ||
                PictureReviewStatusEnum.REVIEWING.equals(statusEnum), ErrorCode.PARAMS_ERROR);
        //更新图片审核状态
        Picture pictureUpdate = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, pictureUpdate);
        //添加相关
        pictureUpdate.setReviewerId(user.getId());
        pictureUpdate.setReviewTime(new Date());
        boolean b = this.updateById(pictureUpdate);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void setReviewParam(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewTime(new Date());
            picture.setReviewerId(loginUser.getId());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 参数校验
        String prefixName = pictureUploadByBatchRequest.getPrefixName();
        String searchText = pictureUploadByBatchRequest.getSearchText();
        String category = pictureUploadByBatchRequest.getCategory();

        if (StrUtil.isBlank(prefixName)) {
            prefixName = "默认" + searchText;
        }

        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(ObjUtil.isNull(count) || count > 30, ErrorCode.PARAMS_ERROR, "抓取数量过多");
        // 拼接搜索引擎URL
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        // 爬虫抓取图片网页
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片抓取失败");
        }
        // 获取图片链接
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "解析元素失败");
        }

        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        if (imgElementList.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "抓取失败");
        }
        for (Element imgElement : imgElementList) {
            String imgUrl = imgElement.attr("src");
            if (StrUtil.isBlank(imgUrl)) {
                log.info("当前连接为空,跳过,{}", fetchUrl);
                continue;
            }
            //处理文件地址,防止出现转义错误去除请求文件地址的查询参数
            int queryIndex = imgUrl.indexOf("?");
            if (queryIndex > -1) {
                imgUrl = imgUrl.substring(0, queryIndex);
            }
            // 批量上传
            try {
                PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                pictureUploadRequest.setBatchFetchDefaultName(prefixName + "-" + (uploadCount + 1));
                if (StrUtil.isNotBlank(category)) {
                    pictureUploadRequest.setCategory(category);
                }
                this.uploadPicture(imgUrl, pictureUploadRequest, loginUser);
                log.info("上传图片成功: {}", imgUrl);
                uploadCount++;
            } catch (Exception e) {
                log.error("上传图片失败: {}", imgUrl, e);
                continue;
            }
            // 早停
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }


    @Override
    public Integer uploadPictureByBatchByAsync(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 参数校验部分保持不变
        String prefixName = pictureUploadByBatchRequest.getPrefixName();
        String searchText = pictureUploadByBatchRequest.getSearchText();
        String category = pictureUploadByBatchRequest.getCategory();

        if (StrUtil.isBlank(prefixName)) {
            prefixName = "默认" + searchText;
        }
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(ObjUtil.isNull(count) || count > 30, ErrorCode.PARAMS_ERROR, "抓取数量过多");
        // 拼接搜索引擎URL
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);

        // 爬虫抓取图片网页 - 这部分还是需要同步执行
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片抓取失败");
        }

        // 获取图片链接
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "解析元素失败");
        }

        Elements imgElementList = div.select("img.mimg");
        if (imgElementList.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "抓取失败");
        }
        // 使用原子变量跟踪上传计数
        AtomicInteger uploadCount = new AtomicInteger(0);
        final String finalPrefixName = prefixName;
        // 创建并发任务列表
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 处理每个图片元素
        for (Element imgElement : imgElementList) {
            String imgUrl = imgElement.attr("src");
            if (StrUtil.isBlank(imgUrl)) {
                log.info("当前连接为空,跳过,{}", fetchUrl);
                continue;
            }
            // 处理文件地址,防止出现转义错误去除请求文件地址的查询参数
            int queryIndex = imgUrl.indexOf("?");
            if (queryIndex > -1) {
                imgUrl = imgUrl.substring(0, queryIndex);
            }
            // 如果已经达到了需要的数量，不再创建新任务
            if (uploadCount.get() >= count) {
                break;
            }
            // 创建异步任务
            final String finalImgUrl = imgUrl;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // 当前上传的是第几个图片（原子操作自增）
                    int currentCount = uploadCount.incrementAndGet();
                    // 如果已经超出了需要的数量，不再处理
                    if (currentCount > count) {
                        return;
                    }
                    PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                    pictureUploadRequest.setBatchFetchDefaultName(finalPrefixName + "-" + currentCount);
                    if (StrUtil.isNotBlank(category)) {
                        pictureUploadRequest.setCategory(category);
                    }
                    // 调用上传图片的方法
                    uploadPicture(finalImgUrl, pictureUploadRequest, loginUser);
                    log.info("上传图片成功: {},by Thread:{}", finalImgUrl,Thread.currentThread().getName());
                } catch (Exception e) {
                    log.error("上传图片失败: {}", finalImgUrl, e);
                    // 失败时递减计数
                    uploadCount.decrementAndGet();
                }
            },pictureUploadThreadPool());

            futures.add(future);
        }
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 返回成功上传的数量
        return Math.min(uploadCount.get(), count);
    }
    private ExecutorService pictureUploadThreadPool() {
        // 创建一个适合IO密集型任务的线程池
        // 核心线程数可以设置为CPU核心数 * 2
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        // 最大线程数设置为核心线程数的2倍
        int maximumPoolSize = corePoolSize * 2;
        // 空闲线程的存活时间
        long keepAliveTime = 60L;
        // 使用有界队列，防止任务堆积导致内存溢出
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(500);

        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("picture-upload-pool-" + threadNumber.getAndIncrement());
                thread.setDaemon(false);
                return thread;
            }
        };

        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由提交任务的线程执行
        );
    }


    @Override
    public void deletePicture(Long id, User loginUser) {
        // 查询是否存在
        Picture oldPic = this.getById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(oldPic), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 本人或者管理员
        this.checkPictureOptionAuth(oldPic, loginUser, SpaceRoleEnum.ADMIN);
        // 数据库删除
        transactionTemplate.execute(status -> {
            boolean result = this.removeById(id);
            ThrowUtils.throwIf(!result, ErrorCode.NOT_FOUND_ERROR, "删除图片失败");
            Long spaceId = oldPic.getSpaceId();

            if (spaceId!=null) {
                spaceService.updateVolume(spaceId, -oldPic.getPicSize());
            }
            tryClearPictureFile(oldPic);
            return 1;
        });
    }

    @Async
    @Override
    public void tryClearPictureFile(Picture picture) {

        Boolean cosRemove = transactionTemplate.execute(status -> removeRecord(picture));
        if(ObjUtil.isNull(cosRemove)||!cosRemove){
            return;
        }
        String key = this.url2Key(picture.getUrl());
        String originKey = this.url2Key(picture.getOriginUrl());
        String thumbnailKey = this.url2Key(picture.getThumbnailUrl());
        //执行清理
        if (StrUtil.isNotBlank(key)) {
            cosManager.deleteObject(key);
        }
        if (StrUtil.isNotBlank(originKey)) {
            cosManager.deleteObject(originKey);
        }
        if (StrUtil.isNotBlank(thumbnailKey)) {
            cosManager.deleteObject(thumbnailKey);
        }
    }

    public boolean removeRecord(Picture picture) {
        // 查看是否存在当前图片
        String url = picture.getUrl();
        Long count = this.lambdaQuery().eq(Picture::getUrl, url).count();
        // 如果只有一条记录用可以删除，超过一条说明有其他记录使用则不适用
        if (count == null || count > 1) {
            return false;
        }
        String fileHash = picture.getFileHash();
        if(fileHash!=null){
            fileInfoService.remove(fileHash);
        }
        return  true;
    }

    @Override
    public CreateTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 获取图片信息
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        Picture picture = Optional.ofNullable(this.getById(pictureId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        CreateOutPaintingTaskRequest taskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        taskRequest.setInput(input);
        BeanUtil.copyProperties(createPictureOutPaintingTaskRequest, taskRequest);
        // 创建任务
        return aliYunApiService.createOutPaintingTask(taskRequest);
    }

    @Override
    public CreateTaskResponse createGenPictureTask(GenPictureRequest request, User loginUser) {
        return aliYunApiService.createGenPictureTask(request);
    }

    @Override
    public GetOutPaintingTaskResponse getOutPaintingResult(String taskId) {
        return aliYunApiService.getOutPaintingTask(taskId);
    }

    @Override
    public ImageGenerationResponse getGenerationResult(String taskId) {
        return aliYunApiService.getGenPictureTaskResult(taskId);
    }

    @Override
    public PictureVo getPictureVoById(long id, HttpServletRequest request) {
        // 必须存在且审核通过 || 公共空间必须是审核通过的图片
        Picture picture = this.lambdaQuery().eq(Picture::getId, id).one();
        ThrowUtils.throwIf(ObjUtil.isNull(picture), ErrorCode.NOT_FOUND_ERROR, "非发现资源");

        // 公共空间 查询的结果一定是需要审核通过的
        if (ObjUtil.isNull(picture.getSpaceId())) {
            PictureReviewStatusEnum reviewStatus = PictureReviewStatusEnum.getEnumByValue(picture.getReviewStatus());
            ThrowUtils.throwIf(!PictureReviewStatusEnum.PASS.equals(reviewStatus), ErrorCode.NOT_FOUND_ERROR);
        }

        // 统一的权限校验
        User loginUser = userService.getLoginUser(request);
        this.checkPictureOptionAuth(picture, loginUser, SpaceRoleEnum.VIEWER);

        // 构建返回
        User pictureUser = userService.getById(picture.getUserId());
        UserVo pictureUserVo = UserVo.obj2Vo(pictureUser);
        return PictureVo.obj2Vo(picture, pictureUserVo);
    }

    @Override
    public Page<PictureVo> listPictureVoPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        // 构建查询请求
        Long spaceId = pictureQueryRequest.getSpaceId();
        User loginUser = userService.getLoginUser(request);
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureQueryRequest, picture);
        // 如果有空间Id 查询就不需要审核完毕
        if (spaceId == null) {
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        }
        // 根据统一的查询请求校验参数
        this.checkPictureOptionAuth(picture, loginUser, SpaceRoleEnum.VIEWER);

        // 查询结果并封装返回
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        LambdaQueryWrapper<Picture> queryWrapper = this.getQueryWrapper(pictureQueryRequest);
        Page<Picture> page = this.page(new Page<>(current, size), queryWrapper);
        Page<PictureVo> pictureVoPage = new Page<>(current, size, page.getTotal());
        // 查list的时候返回id即可
        pictureVoPage.setRecords(PictureVo.toVoList(page.getRecords()));
        return pictureVoPage;
    }

    @Override
    public boolean editPicture(PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        Picture picture = pictureEditRequest.toObj();
        picture.setEditTime(new Date());
        // 复杂的参数校验也交由 service层处理
        this.validPicture(picture);
        Long picId = picture.getId();
        // 必须存在
        Picture oldPic = this.getById(picId);
        ThrowUtils.throwIf(ObjUtil.isNull(oldPic), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 本人或者管理员
        User loginUser = userService.getLoginUser(request);
        this.checkPictureOptionAuth(oldPic, loginUser, SpaceRoleEnum.EDITOR);
        // 数据库操作
        this.setReviewParam(picture, loginUser);

        return this.updateById(picture);
    }

    @Override
    public String downloadPicture(Long id, HttpServletRequest request) throws MalformedURLException {

        Picture picture = this.getById(id);
        User loginUser = userService.getLoginUser(request);

        //TODO VIP拥有其他额外权力,暂时不添加
        this.checkPictureOptionAuth(picture, loginUser, SpaceRoleEnum.VIEWER);
        return picture.getOriginUrl() == null ? picture.getUrl() : picture.getOriginUrl();
    }


    /**
     * url转换成key
     *
     * @param url 图片url
     * @return key string
     */
    private String url2Key(String url) {
        return url.replaceFirst(cosConfig.getHost(), "");
    }

    @Override
    public void checkPictureOptionAuth(Picture picture, User loginUser, SpaceRoleEnum requestRole) {
        Long spaceId = picture.getSpaceId();
        Long pictureOwnUserId = picture.getUserId();
        Long currentUserId = loginUser.getId();

        // 公共图片的判断逻辑主要是图片的所有权来进行判断
        if (spaceId == null) {
            // 公共空间 任何人都可以执行读操作 哪怕是批量查询
            if (SpaceRoleEnum.VIEWER.equals(requestRole)) {
                return;
            }
            //公共空间，允许其他操作只准许是本人和管理员
            if (!Objects.equals(pictureOwnUserId, currentUserId) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "公共空间，允许操作是本人和管理员");
            }
        } else {
            spaceService.checkSpaceOptionAuth(spaceId, loginUser, requestRole);
        }
    }


    @Override
    public Picture getById(Serializable id) {
        // redis 缓存版本
        return super.getById(id);
    }
}




