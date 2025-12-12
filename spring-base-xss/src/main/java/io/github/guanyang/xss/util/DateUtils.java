package io.github.guanyang.xss.util;

import cn.hutool.core.date.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author guanyang
 */
public class DateUtils {

    public static LocalDateTime toLocalDateTime(String source) {
        return convert(source, DateUtils::toLocalDateTime);
    }

    public static LocalDate toLocalDate(String source) {
        return convert(source, DateUtils::toLocalDate);
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return Optional.ofNullable(date).map(d -> LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault())).orElse(null);
    }

    public static LocalDate toLocalDate(Date date) {
        return Optional.ofNullable(date).map(d -> LocalDate.ofInstant(d.toInstant(), ZoneId.systemDefault())).orElse(null);
    }

    public static <T> T convert(String source, Function<Date, T> convertor) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        Date date = DateUtil.parse(StringUtils.trim(source));
        if (date == null) {
            throw new IllegalArgumentException("Unable to parse the date: " + source);
        }
        return convertor.apply(date);
    }

    private DateUtils() {
    }
}
