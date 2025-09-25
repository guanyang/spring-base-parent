package io.github.guanyang.core.filter;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class FilterRequest<T> extends BaseFilterDTO {

    private static final long serialVersionUID = -4751648994737819145L;

    private T request;

    public FilterRequest(T request) {
        this.request = request;
    }

    public FilterRequest(T request, Map<String, Object> attachments) {
        this.request = request;
        super.setAttachments(attachments);
    }
}
