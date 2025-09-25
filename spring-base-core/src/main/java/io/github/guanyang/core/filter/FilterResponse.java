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
public class FilterResponse<R> extends BaseFilterDTO {

    private static final long serialVersionUID = 1586160273995487939L;

    private R response;

    private boolean endFlag = false;

    public FilterResponse(R response) {
        this.response = response;
    }

    public FilterResponse(R response, Map<String, Object> attachments) {
        this.response = response;
        super.setAttachments(attachments);
    }

    public FilterResponse(R response, boolean endFlag) {
        this.response = response;
        this.endFlag = endFlag;
    }

    public FilterResponse(R response, boolean endFlag, Map<String, Object> attachments) {
        this.response = response;
        this.endFlag = endFlag;
        super.setAttachments(attachments);
    }
}
