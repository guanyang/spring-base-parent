package org.gy.framework.core.filter;

import java.util.HashMap;
import java.util.Map;
import org.gy.framework.core.dto.DTO;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class BaseFilterDTO extends DTO {

    private static final long serialVersionUID = -6778039657924078129L;

    private Map<String, Object> attachments = new HashMap<>();

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public BaseFilterDTO setAttachments(Map<String, Object> map) {
        if (map != null && map.size() > 0) {
            attachments.putAll(map);
        }
        return this;
    }

    public Object getAttachment(String key) {
        return attachments.get(key);
    }

    public Object getAttachment(String key, Object defaultValue) {
        Object result = attachments.get(key);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    public BaseFilterDTO setAttachment(String key, Object value) {
        attachments.put(key, value);
        return this;
    }

}
