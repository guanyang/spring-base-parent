package org.gy.framework.sign.aspect;

import javax.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.gy.framework.sign.config.ClientAppConfiguration;
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
        SignedReq req = (SignedReq) joinpoint.getArgs()[0];
        int appId = req.getAppId();

        if (!clientAppConfiguration.checkAppId(appId)) {
            throw new SignInvalidException("app_id not exists");
        }

        String key = clientAppConfiguration.getAppKey(req.getAppId());
        ParamSignUtils.checkSign((SignedReq) joinpoint.getArgs()[0], key);

        return joinpoint.proceed();
    }

}
