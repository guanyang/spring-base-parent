package io.github.guanyang.core.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gy
 */
@Slf4j
public class Action {

    @FunctionalInterface
    public interface FunctionAction<T, R> {

        R apply(T t) throws Exception;

        @SneakyThrows
        static <T, R> R apply(T t, FunctionAction<T, R> action) {
            return action.apply(t);
        }

        static <T, R> R applyQuietly(T t, FunctionAction<T, R> action) {
            try {
                return action.apply(t);
            } catch (Throwable e) {
                log.warn("FunctionAction applyQuietly error.", e);
                return null;
            }
        }
    }

    @FunctionalInterface
    public interface BiFunctionAction<T, U, R> {

        R apply(T t, U u) throws Exception;

        @SneakyThrows
        static <T, U, R> R apply(T t, U u, BiFunctionAction<T, U, R> action) {
            return action.apply(t, u);
        }
    }

    @FunctionalInterface
    public interface BiConsumerAction<T, U> {

        void accept(T t, U u) throws Exception;

        @SneakyThrows
        static <T, U> void accept(T t, U u, BiConsumerAction<T, U> action) {
            action.accept(t, u);
        }
    }

    @FunctionalInterface
    public interface ConsumerAction<T> {

        void accept(T t) throws Exception;

        @SneakyThrows
        static <T> void accept(T t, ConsumerAction<T> consumer) {
            consumer.accept(t);
        }

        static <T> void acceptQuietly(T t, ConsumerAction<T> action) {
            try {
                action.accept(t);
            } catch (Throwable e) {
                log.warn("ConsumerAction acceptQuietly error.", e);
            }
        }

    }

}
