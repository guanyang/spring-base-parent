package org.gy.framework.core.dto;

import java.io.Serializable;
import org.gy.framework.core.trace.TraceUtils;

/**
 * 功能描述：Data Transfer object
 *
 * @author gy
 * @version 1.0.0
 */
public abstract class DTO implements Serializable {

    private static final long serialVersionUID = -3215896069583612323L;

    private String requestId;

    public DTO() {
        requestId = TraceUtils.computeIfAbsent();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
