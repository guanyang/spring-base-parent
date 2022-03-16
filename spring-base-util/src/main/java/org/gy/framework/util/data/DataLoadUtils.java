package org.gy.framework.util.data;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述：数据加载工具类
 *
 * @author gy
 * @version 1.0.0
 */
public class DataLoadUtils {

    /**
     * 功能描述：分批加载处理数据
     *
     * @param req 分批参数
     * @param queryFuntion 查询函数
     * @param idMapper id映射
     * @param dataConsumer 处理函数
     * @author gy
     * @version 1.0.0
     */
    public static <T extends DataLoadContext, R> void execute(T req, Function<T, List<R>> queryFuntion,
        Function<R, Long> idMapper, Consumer<List<R>> dataConsumer) {
        List<R> list;
        do {
            list = queryFuntion.apply(req);
            if (isEmpty(list)) {
                break;
            }
            dataConsumer.accept(list);
            Long id = idMapper.apply(list.get(list.size() - 1));
            req.setIdValue(id);
        } while (!isEmpty(list) && list.size() == req.getPageSize());
    }

    private static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataLoadContext {

        public static final int PAGE_SIZE = 200;

        public static final String ID_NAME = "id";
        /**
         * id字段名称
         */
        private String idName = ID_NAME;

        private Long idValue = 0L;

        private Long startTime;

        private Long endTime;

        private Integer pageSize = PAGE_SIZE;

    }

}
