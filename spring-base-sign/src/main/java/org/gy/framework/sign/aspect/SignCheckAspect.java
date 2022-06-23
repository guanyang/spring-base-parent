package org.gy.framework.sign.aspect;

import java.util.stream.Stream;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.gy.framework.sign.config.ClientAppConfiguration;
import org.gy.framework.sign.config.ClientAppProperties.AppItem;
import org.gy.framework.sign.dto.SignedReq;
import org.gy.framework.sign.exception.SignInvalidException;
import org.gy.framework.sign.util.ParamSignUtils;
import org.springframework.stereotype.Component;

/**
 * @author gy
 */
@Aspect
@Component
public class SignCheckAspect {

    @Resource
    private ClientAppConfiguration clientAppConfiguration;

    @Around("@annotation(org.gy.framework.sign.annotation.SignCheck)")
    public Object doCheckSign(ProceedingJoinPoint joinpoint) throws Throwable {
        SignedReq req = getSignedReq(joinpoint);
        AppItem appItem = clientAppConfiguration.getAppItem(req.getAppId());
        if (appItem == null || StringUtils.isBlank(appItem.getAppKey())) {
            throw new SignInvalidException("app_id/app_key not exists");
        }
        //校验时间戳偏移，默认不能超过30s，可以自定义配置
        checkTime(req, appItem);

        ParamSignUtils.checkSign(req, appItem.getAppKey());

        return joinpoint.proceed();
    }

    private static <T extends SignedReq> void checkTime(T req, AppItem appItem) {
        int timeSpan = Math.abs((int) (System.currentTimeMillis() - req.getTimestamp()) / 1000);
        int clockSkew = appItem.getClockSkew();
        if (clockSkew > 0 && clockSkew < timeSpan) {
            throw new SignInvalidException("SignedReq timestamp invalid");
        }
    }

    private static <T extends SignedReq> T getSignedReq(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        if (args == null || args.length == 0) {
            throw new SignInvalidException("SignedReq not exists");
        }
        return (T) Stream.of(args).filter(p -> p instanceof SignedReq).findFirst()
            .orElseThrow(() -> new SignInvalidException("SignedReq not exists"));
    }

}
