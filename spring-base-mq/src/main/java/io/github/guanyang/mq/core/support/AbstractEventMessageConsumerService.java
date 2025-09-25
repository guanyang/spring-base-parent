package io.github.guanyang.mq.core.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.mq.core.EventMessageConsumerService;
import io.github.guanyang.mq.model.EventMessage;
import org.springframework.util.Assert;

import java.lang.reflect.Type;


/**
 * @author gy
 */
@Slf4j
public abstract class AbstractEventMessageConsumerService<T, R> implements EventMessageConsumerService<T, R> {

    protected abstract Class<T> getDataType();

    /**
     * 获取数据类型（扩展嵌套泛型）
     */
    protected Type getDataTypeReference() {
        return null;
    }

    protected abstract R internalExecute(T data);

    public T convert(EventMessage<?> event) {
        if (event == null || event.getData() == null) {
            log.warn("[EventMessageService]消息数据为空");
            return null;
        }
        Object data = event.getData();
        Type typeReference = getDataTypeReference();
        try {
            if (typeReference != null) {
                return TypeUtils.cast(data, typeReference);
            }
            return convert(data, getDataType());
        } catch (Exception e) {
            log.error("[EventMessageService]消息数据转换异常, event={}.", event, e);
            return null;
        }
    }

    protected T convert(Object object, Class<T> dataType) {
        Assert.notNull(dataType, () -> "[EventMessageService]消息数据类型为空");
        if (dataType.isInstance(object)) {
            return (T) object;
        }
        return JSON.to(dataType, object);
    }

    protected R internalExecuteWithContext(T data) {
        return doWithContext(data, this::internalExecute);
    }

    @Override
    public R execute(EventMessage<T> eventMessage) {
        T data = convert(eventMessage);
        return internalExecuteWithContext(data);
    }

}
