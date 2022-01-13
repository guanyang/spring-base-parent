package org.gy.framework.core.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.gy.framework.core.trace.TraceUtils;
import org.gy.framework.core.util.JsonUtils;

/**
 * 功能描述：Data Transfer object
 *
 * @author gy
 * @version 1.0.0
 */
@Getter
@Setter
public abstract class DTO implements Serializable {

    private static final long serialVersionUID = -3215896069583612323L;

    private String requestId;

    public DTO() {
        requestId = TraceUtils.computeIfAbsent();
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
