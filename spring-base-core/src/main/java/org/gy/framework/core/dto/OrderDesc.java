package org.gy.framework.core.dto;

import java.io.Serializable;
import lombok.Data;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Data
public class OrderDesc implements Serializable {

    private static final long serialVersionUID = -913701937097064332L;

    private String col;
    private boolean asc = true;
}
