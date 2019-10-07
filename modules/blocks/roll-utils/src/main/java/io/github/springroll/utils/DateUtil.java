package io.github.springroll.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    /**
     * 默认日期表示格式
     */
    public static final String FORMAT_DATE = "yyyy-MM-dd";

    /**
     * 默认日期时间表示格式
     */
    public static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期时间表示格式，含毫秒
     */
    public static final String FORMAT_TIMESTAMP = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * ISODate 时间表示格式
     */
    public static final String FORMAT_ISODATE = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * 私有化工具类的构造函数，避免对工具类的实例化
     */
    private DateUtil() {
    }

    /**
     * 获得当前年份数值表示，例如：2016
     *
     * @return 当前年份数值
     */
    public static int getCurrentYear() {
        return Year.now().getValue();
    }

    /**
     * 获得当前时间戳，不含毫秒
     *
     * @return 时间戳字符串，使用默认时间日期格式
     */
    public static String getTimestamp() {
        return getTimestamp(false);
    }

    /**
     * 获得当前时间戳
     *
     * @param millisecond 是否含毫秒
     * @return 时间戳字符串，传入 true 时可包含毫秒
     */
    public static String getTimestamp(boolean millisecond) {
        return toTimestamp(LocalDateTime.now(), millisecond);
    }

    /**
     * 按默认时间戳格式转换日期为时间戳，不含毫秒
     *
     * @param localDateTime 日期对象
     * @return 时间戳
     */
    public static String toTimestamp(LocalDateTime localDateTime) {
        return toTimestamp(localDateTime, false);
    }

    /**
     * 按默认时间戳格式转换日期为时间戳
     *
     * @param date        日期对象
     * @param millisecond 是否包含毫秒
     * @return 时间戳
     */
    public static String toTimestamp(LocalDateTime date, boolean millisecond) {
        return toString(date, millisecond ? FORMAT_TIMESTAMP : FORMAT_DATETIME);
    }

    /**
     * 按照提供的日期格式转换日期对象为字符串
     *
     * @param date   日期对象
     * @param format 日期字符串格式
     * @return 日期字符串
     */
    public static String toString(LocalDateTime date, String format) {
        assureNotNull(date);
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    private static void assureNotNull(LocalDateTime date) {
        if (date == null) {
            throw new IllegalArgumentException("The date must NOT be null!");
        }
    }

    /**
     * localDateTime 转换成 Date 日期对象
     *
     * @param localDateTime localDateTime 时间
     * @return Date 日期对象
     */
    public static Date toDate(LocalDateTime localDateTime) {
        assureNotNull(localDateTime);
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        return Date.from(zdt.toInstant());
    }

    /**
     * 按照默认日期格式转换字符串为日期对象
     *
     * @param dateStr 日期字符串
     * @return 日期对象
     */
    public static LocalDate toLocalDate(String dateStr) {
        return toLocalDate(dateStr, FORMAT_DATE);
    }

    /**
     * 按照提供的日期格式转换字符串为日期对象
     *
     * @param dateStr 日期字符串
     * @param format  日期字符串格式
     * @return 日期对象
     */
    public static LocalDate toLocalDate(String dateStr, String format) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
    }

    /**
     * 按照默认时间戳格式转换字符串为日期对象
     *
     * @param dateTimeStr 日期时间字符串
     * @return 日期对象
     */
    public static LocalDateTime toLocalDateTime(String dateTimeStr) {
        return toLocalDateTime(dateTimeStr, FORMAT_DATETIME);
    }

    /**
     * 按照给定时间戳格式转换字符串为日期对象
     *
     * @param dateTimeStr 日期时间字符串
     * @param format      时间格式
     * @return 日期对象
     */
    public static LocalDateTime toLocalDateTime(String dateTimeStr, String format) {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(format));
    }

    /**
     * Date 转换成 LocalDateTime
     *
     * @param date Date 日期
     * @return LocalDateTime
     * @throws IllegalArgumentException date 为空时
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("The date must NOT be null!");
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 按默认日期格式转换日期为字符串
     *
     * @param date 日期对象
     * @return 日期字符串
     */
    public static String toLocalDateString(LocalDateTime date) {
        return toString(date, FORMAT_DATE);
    }

    /**
     * 安全 clone 日期对象
     * 常用在 Java Bean 中的 Date 类型属性的 getter 和 setter 方法中
     * 防止将 Bean 中的私有属性直接暴露出来
     *
     * @param date 日期对象
     * @return null 或者克隆出的日期对象
     */
    public static Date safeClone(Date date) {
        return date == null ? null : (Date) date.clone();
    }

    /**
     * 解析 ISODate 格式时间戳至 LocalDateTime 类型
     *
     * @param isoDateTimestamp ISODate 格式时间戳
     * @return 时间
     */
    public static LocalDateTime parseIsoDateTimestamp(String isoDateTimestamp) {
        return LocalDateTime.parse(isoDateTimestamp, DateTimeFormatter.ofPattern(FORMAT_ISODATE).withZone(ZoneId.of("GMT")));
    }

    /**
     * 获取本周的周几 ，周一、周日为一周的开始和结束
     *
     * @param date      日期
     * @param dayOfWeek 周几 1～7代表周一～周日
     * @return 本周几Date对象
     */
    public static LocalDateTime getDayOfWeek(LocalDateTime date, int dayOfWeek) {
        assureNotNull(date);
        return date.with(ChronoField.DAY_OF_WEEK, dayOfWeek);
    }

    /**
     * 获取年初日期
     *
     * @param date 要获得年初的日期
     * @return 该年度1月1日的Date对象
     */
    public static LocalDateTime getBeginningOfYear(LocalDateTime date) {
        assureNotNull(date);
        return date.withDayOfYear(1);
    }

    /**
     * 日期添加指定分钟数
     *
     * @param date   要添加天数的日期,如果为负数，则为减少的分钟数
     * @param minute 添加的天数
     * @return 添加指定分钟数的新的Date对象
     */
    public static LocalDateTime addMinute(LocalDateTime date, int minute) {
        return addWithUnit(date, minute, ChronoUnit.MINUTES);
    }

    private static LocalDateTime addWithUnit(LocalDateTime date, long amountToAdd, TemporalUnit unit) {
        assureNotNull(date);
        return date.plus(amountToAdd, unit);
    }

    /**
     * 日期添加指定天数
     *
     * @param date 要添加天数的日期,如果为负数，则为减少的天数
     * @param day  添加的天数
     * @return 添加指定分钟数的新的Date对象
     */
    public static LocalDateTime addDay(LocalDateTime date, int day) {
        return addWithUnit(date, day, ChronoUnit.DAYS);
    }

    /**
     * 日期添加指定星期
     *
     * @param date 要添加星期的日期
     * @param week 添加的星期,如果为负数，则为减少的星期
     * @return 添加指定星期数的新的Date对象
     */
    public static LocalDateTime addWeek(LocalDateTime date, int week) {
        return addWithUnit(date, week, ChronoUnit.WEEKS);
    }

    /**
     * 日期添加指定月份
     *
     * @param date  要添加天数的日期,如果为负数，则为减少的月份
     * @param month 添加的月份
     * @return 添加指定月份的新的Date对象
     */
    public static LocalDateTime addMonth(LocalDateTime date, int month) {
        return addWithUnit(date, month, ChronoUnit.MONTHS);
    }

}
