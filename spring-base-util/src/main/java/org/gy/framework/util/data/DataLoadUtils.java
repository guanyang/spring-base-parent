package org.gy.framework.util.data;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 功能描述：数据加载工具类
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class DataLoadUtils {


    /**
     * 功能描述：批量获取，避免超过单次查询限制
     *
     * @param params 方法入参
     * @param executeFun 执行函数
     * @param batchSize 批量大小
     * @author gy
     * @version 1.0.0
     */
    public static <T, R> List<R> batchExecute(List<T> params, Function<List<T>, List<R>> executeFun, int batchSize) {
        return batchExecute(params, executeFun, batchSize, true);
    }

    /**
     * 功能描述：批量获取，避免超过单次查询限制
     *
     * @param params 方法入参
     * @param executeFun 执行函数
     * @param batchSize 批量大小
     * @param distinct 参数是否去重，true是，false否
     * @author gy
     * @version 1.0.0
     */
    public static <T, R> List<R> batchExecute(List<T> params, Function<List<T>, List<R>> executeFun, int batchSize,
        boolean distinct) {
        List<R> result = Lists.newArrayList();
        if (isEmpty(params)) {
            log.warn("[batchExecute]批量获取参数为空:batchSize={}", batchSize);
            return result;
        }
        //去重处理
        List<T> reqList = distinct ? params.stream().distinct().collect(Collectors.toList()) : params;
        //分批获取，避免超过批量限制
        List<List<T>> partition = Lists.partition(reqList, batchSize);
        partition.forEach(items -> {
            List<R> resList = executeFun.apply(items);
            result.addAll(resList);
        });
        return result;
    }

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
