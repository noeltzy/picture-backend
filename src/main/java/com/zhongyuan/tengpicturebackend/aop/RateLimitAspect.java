package com.zhongyuan.tengpicturebackend.aop;


import com.zhongyuan.tengpicturebackend.annotation.RequestLimit;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@Order(2)
public class RateLimitAspect {

    @Resource
    RedissonClient redissonClient;
    @Resource
    private UserService userService;


    @Around("@annotation(requestLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RequestLimit requestLimit) throws Throwable {
        //TODO 业务扩展点，可以根据key的不同,在获取用户信息之后，根据用户是否是VIP给与请求并发数的增加,如果是VIP准许一秒上传20张
        if(requestLimit!=null){
            String key = requestLimit.key();
            log.info(key);
            int times = requestLimit.times();
            int duration = requestLimit.duration();
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            ThrowUtils.throwIf(requestAttributes==null, ErrorCode.SYSTEM_ERROR);
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            // 这个就是必须登录
            User user = userService.getLoginUser(request);
            Long currentRequestUserId = user.getId();
            String limitKey = String.format("tengPicture:rateLimit:%s:%d", key,currentRequestUserId);
            log.info(limitKey);
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(limitKey);
            if (!rateLimiter.isExists()) {
                rateLimiter.trySetRate(RateType.OVERALL, times, Duration.ofSeconds(duration));
                //限流器设置十分钟期限
                rateLimiter.expire(Duration.ofMinutes(10));
            }
            if(!rateLimiter.tryAcquire()){
                //TODO 这里可以设置 Banner 如果访问过于频繁 可以让用户十分钟后访问
                log.info("limit");
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS_ERROR);
            }
            // 异步刷新限流器的持续时间
            CompletableFuture.runAsync(() -> {
                try {
                    Long ttl =rateLimiter.remainTimeToLive();
                    if(ttl!=null && ttl< TimeUnit.MINUTES.toMillis(3)){
                        rateLimiter.expire(Duration.ofMinutes(10));
                    }
                }catch (Exception e){
                    log.info("限流器刷新异常");
                }
            });
        }
        log.info("pass");
        return  joinPoint.proceed(); //直接放行
    }
}
