package io.github.guanyang.sign.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author gy
 */
@ConfigurationProperties(prefix = "sign.client")
@Data
public class ClientAppProperties {

    private List<AppItem> apps;

    @Data
    public static class AppItem {

        private int appId;
        private String appKey;

    }
}
