package org.gy.framework.util;

import static org.gy.framework.util.data.DataLoadUtils.completableExecute;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.commons.lang3.RandomUtils;
import org.gy.framework.util.data.DataLoadUtils.ConcurrentResult;
import org.junit.jupiter.api.Test;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 * @date 2023/5/11 11:52
 */
public class DataLoadUtilsTest {


    @Test
    public void completableExecuteTest(){
        ExecutorService service = Executors.newFixedThreadPool(5);

        List<String> requestList = Lists.newArrayList("a", "b", "c", "d", "e");

        Function<String, String> function = param -> {
            int i = RandomUtils.nextInt(130, 180);
            try {
                TimeUnit.MILLISECONDS.sleep(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return param + "-" + System.currentTimeMillis() + "-" + i;
        };
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<ConcurrentResult<String, String>> result = completableExecute(service, requestList, 150,function);
        System.out.println("completableExecute耗时：" + stopwatch.elapsed(TimeUnit.MILLISECONDS));

        result.forEach(System.out::println);

        service.shutdown();
    }

}
