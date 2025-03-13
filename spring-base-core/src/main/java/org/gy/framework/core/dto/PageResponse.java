package org.gy.framework.core.dto;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.Setter;
import org.gy.framework.core.exception.CommonErrorCode;
import org.gy.framework.core.exception.CommonException;
import org.gy.framework.core.exception.ErrorCodeI;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Getter
@Setter
public class PageResponse<T> extends BaseResponse {

    private static final long serialVersionUID = -3414014589767966265L;

    private Collection<T> data = Collections.EMPTY_LIST;
    private long total;

    public static <T> PageResponse<T> asSuccess(Collection<T> data, long total) {
        PageResponse<T> response = new PageResponse<>();
        response.wrapResponse(SUCCESS_CODE, SUCCESS_MSG);
        response.setData(data);
        response.setTotal(total);
        return response;
    }

    public static <T> PageResponse<T> asError(int error, String msg) {
        PageResponse<T> response = new PageResponse<>();
        response.wrapResponse(error, msg);
        return response;
    }

    public static <T> PageResponse<T> asError(ErrorCodeI bizCode) {
        return asError(bizCode.getError(), bizCode.getMsg());
    }

    public static <T> PageResponse<T> asError(CommonException e) {
        return asError(e.getError(), e.getMsg());
    }


}
