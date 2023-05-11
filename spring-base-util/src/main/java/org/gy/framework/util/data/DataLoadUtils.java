package org.gy.framework.util.data;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * 功能描述：数据加载工具类
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class DataLoadUtils {

    /**
     * 功能描述：多线程执行function任务，并汇总结果（默认中断未完成的任务）
     *
     * @param executorService 线程池
     * @param requestParams 请求参数集合
     * @param timeoutMillis 超时时间，单位：毫秒
     * @param function function任务定义
     * @return 多任务结果集
     * @author gy
     * @version 1.0.0
     * @date 2020/11/5 10:25
     */
    public static <T, R> List<ConcurrentResult<T, R>> completableExecute(ExecutorService executorService,
        List<T> requestParams, long timeoutMillis, Function<T, R> function) {
        return completableExecute(executorService, requestParams, timeoutMillis, TimeUnit.MILLISECONDS, function, true);
    }


    /**
     * 功能描述：多线程执行function任务，并汇总结果
     *
     * @param executorService 线程池
     * @param requestParams 请求参数集合
     * @param timeout 超时时间，单位：毫秒
     * @param unit 时间单位
     * @param function function任务定义
     * @param mayInterruptIfRunning 是否中断未完成的任务，true是，false否
     * @return 多任务结果集
     * @author gy
     * @version 1.0.0
     * @date 2020/11/5 10:26
     */
    public static <T, R> List<ConcurrentResult<T, R>> completableExecute(ExecutorService executorService,
        List<T> requestParams, long timeout, TimeUnit unit, Function<T, R> function, boolean mayInterruptIfRunning) {
        List<ConcurrentResult<T, R>> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(requestParams)) {
            return result;
        }
        List<CompletableFuture<ConcurrentResult<T, R>>> futureList = Lists.newArrayList();
        for (T param : requestParams) {
            Supplier<ConcurrentResult<T, R>> supplier = getSupplier(function, param);
            futureList.add(CompletableFuture.supplyAsync(supplier, executorService));
        }
        //并行任务合并
        CompletableFuture<Void> allFuture = CompletableFuture
            .allOf(futureList.toArray(new CompletableFuture[futureList.size()]));
        //等待指定时间
        try {
            allFuture.get(timeout, unit);
        } catch (Exception e) {
            log.warn("[completableExecute]future get Exception", e);
        }
        //合并返回结果
        wrapFutureResult(mayInterruptIfRunning, result, futureList);
        return result;
    }

    private static <T, R> void wrapFutureResult(boolean mayInterruptIfRunning, List<ConcurrentResult<T, R>> result,
        List<CompletableFuture<ConcurrentResult<T, R>>> futureList) {
        for (CompletableFuture<ConcurrentResult<T, R>> future : futureList) {
            try {
                ConcurrentResult<T, R> res = future.getNow(null);
                if (Objects.nonNull(res)) {
                    result.add(res);
                } else {
                    future.cancel(mayInterruptIfRunning);
                }
            } catch (Exception e) {
                log.warn("[completableExecute]future wrap exception", e);
            }
        }
    }

    private static <T, R> Supplier<ConcurrentResult<T, R>> getSupplier(Function<T, R> function,
        T param) {
        return () -> {
            ConcurrentResult<T, R> functionResult;
            try {
                R apply = function.apply(param);
                functionResult = new ConcurrentResult<>(param, apply);
            } catch (Exception e) {
                log.warn("[completableExecute]function apply exception: param={}.", param, e);
                functionResult = new ConcurrentResult<>(param, e);
            }
            return functionResult;
        };
    }


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

    @Data
    public static class ConcurrentResult<T, R> {

        private T param;
        private R result;
        private Exception exception;

        public ConcurrentResult(T param, R result) {
            this.param = param;
            this.result = result;
        }

        public ConcurrentResult(T param, Exception exception) {
            this.param = param;
            this.exception = exception;
        }

        public boolean hasException() {
            return exception != null;
        }

    }

}
