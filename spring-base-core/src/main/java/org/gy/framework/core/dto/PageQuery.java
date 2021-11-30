package org.gy.framework.core.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Data
public class PageQuery extends Query {

    private static final long serialVersionUID = -1828037735162653632L;

    private int pageNum = 1;
    private int pageSize = 10;
    private boolean needTotalCount = true;
    private List<OrderDesc> orderDescs;

    public void addOrderDesc(OrderDesc orderDesc) {
        if (null == orderDescs) {
            orderDescs = new ArrayList<>();
        }
        orderDescs.add(orderDesc);
    }

    public int getOffset() {
        return pageNum > 0 ? (pageNum - 1) * pageSize : 0;
    }


}
