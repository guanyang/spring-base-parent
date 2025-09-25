package io.github.guanyang.core.filter;

import java.util.HashMap;
import java.util.Map;
import io.github.guanyang.core.dto.DTO;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class BaseFilterDTO extends DTO {

    private static final long serialVersionUID = -6778039657924078129L;

    private Map<String, Object> attachments;

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public BaseFilterDTO setAttachments(Map<String, Object> attachments) {
        this.attachments = attachments == null ? new HashMap<>() : attachments;
        return this;
    }

    public Object getAttachment(String key) {
        if (attachments == null) {
            return null;
        }
        return attachments.get(key);
    }

    public Object getAttachment(String key, Object defaultValue) {
        if (attachments == null) {
            return defaultValue;
        }
        Object result = attachments.get(key);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    public BaseFilterDTO setAttachment(String key, Object value) {
        if (attachments == null) {
            attachments = new HashMap<>();
        }
        attachments.put(key, value);
        return this;
    }

    public BaseFilterDTO setAttachmentIfAbsent(String key, Object value) {
        if (attachments == null) {
            attachments = new HashMap<>();
        }
        this.attachments.putIfAbsent(key, value);
        return this;
    }

    public BaseFilterDTO addAttachments(Map<String, Object> attachments) {
        if (attachments == null) {
            return this;
        }
        if (this.attachments == null) {
            this.attachments = new HashMap<>();
        }
        this.attachments.putAll(attachments);
        return this;
    }

    public BaseFilterDTO addAttachmentsIfAbsent(Map<String, Object> attachments) {
        if (attachments == null) {
            return this;
        }
        for (Map.Entry<String, Object> entry : attachments.entrySet()) {
            setAttachmentIfAbsent(entry.getKey(), entry.getValue());
        }
        return this;
    }

}
