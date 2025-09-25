package org.gy.framework.sign.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.gy.framework.sign.SignCommon;
import org.gy.framework.sign.config.ClientAppProperties.AppItem;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author gy
 */
@Configuration
@ConditionalOnProperty(name = "sign.client.apps[0].appId")
@EnableConfigurationProperties(ClientAppProperties.class)
@ComponentScan(basePackageClasses = SignCommon.class)
public class ClientAppConfiguration {

    @Resource
    private ClientAppProperties clientAppProperties;

    private Map<Integer, ClientAppProperties.AppItem> appItemMap;

    @PostConstruct
    public void init() {
        appItemMap = clientAppProperties.getApps().stream()
                .collect(Collectors.toMap(ClientAppProperties.AppItem::getAppId, Function.identity()));
    }

    public boolean checkAppId(int appId) {
        return appItemMap.containsKey(appId);
    }

    public String getAppKey(int appId) {
        return Optional.ofNullable(appItemMap.get(appId)).map(AppItem::getAppKey).orElse(StringUtils.EMPTY);
    }

    public AppItem getAppItem(int appId) {
        return appItemMap.get(appId);
    }

}
