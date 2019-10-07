package io.github.springroll.utils

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class DateUtilSpec extends Specification {

    def "Check utils in DateUtil"() {
        given:
        Date d = DateUtil.toDate(LocalDateTime.of(2016, 6, 15, 15, 31, 24))

        String ds = '2016-06-15'

        expect:
        DateUtil.getCurrentYear() == new Date().format('YYYY').toInteger()
        DateUtil.getTimestamp().substring(0, 13) == new Date().format(DateUtil.FORMAT_DATETIME).substring(0, 13)
        DateUtil.toTimestamp(LocalDateTime.of(2016, 6, 15, 15, 31, 24), true) == '2016-06-15 15:31:24.000'
        DateUtil.toTimestamp(LocalDateTime.of(2016, 6, 15, 15, 31, 24)) == '2016-06-15 15:31:24'
        DateUtil.toLocalDateString(LocalDateTime.of(2016, 6, 15, 15, 31, 24)) == ds
        DateUtil.toLocalDate(ds) == LocalDate.of(2016,6,15)
        DateUtil.toLocalDate('2016-06-15 15:31:24', DateUtil.FORMAT_DATETIME) == LocalDate.of(2016, 06,15)
        DateUtil.toLocalDateTime(d) == LocalDateTime.of(2016, 6, 15, 15, 31, 24)
        DateUtil.toLocalDateTime('2016-06-15 15:31:24') == LocalDateTime.of(2016, 6, 15, 15, 31, 24)
        DateUtil.safeClone(d) == d
        DateUtil.safeClone(null) == null
    }

    @Unroll
    def "The margin between #start and #end is #result"() {
        expect:
        result == Period.between(DateUtil.toLocalDate(start), DateUtil.toLocalDate(end)).getYears()

        where:
        start        | end          | result
        '1993-10-01' | '2013-09-13' | 19
        '2010-01-01' | '2013-09-13' | 3
        '2010-01-15' | '2013-09-13' | 3
        '1993-10-15' | '2013-09-13' | 19
        '1993-10-15' | '2013-10-15' | 20
        '1993-10-15' | '2013-09-15' | 19
        '2013-09-15' | '2013-10-15' | 0
    }

    def "Timestamp with millisecond is unique"() {
        given:
        def timestamp = DateUtil.getTimestamp(true)
        sleep(1)

        expect:
        timestamp != DateUtil.getTimestamp(true)
    }

    def "Check addDay in DateUtil"() {
        given:
        LocalDateTime localDateTime = LocalDateTime.of(2016, 6, 15, 15, 31, 24)

        expect:
        DateUtil.addDay(localDateTime, 0) == localDateTime
    }

    def "Check addMinute in DateUtil"() {
        given:
        LocalDateTime localDateTime = LocalDateTime.of(2016, 6, 15, 15, 31, 24)

        expect:
        DateUtil.addMinute(localDateTime, 0) == localDateTime
    }

    def "Check parse ISO date"() {
        expect:
        assert "2018-07-25 01:42:17" == DateUtil.parseIsoDateTimestamp("2018-07-25T01:42:17.582Z").format(DateTimeFormatter.ofPattern
                (DateUtil.FORMAT_DATETIME))
    }

    def "Check addWeek in DateUtil"() {
        given:
        LocalDateTime localDateTime = LocalDateTime.of(2018, 7, 19, 15, 31, 24)

        expect:
        DateUtil.addWeek(localDateTime, 0) == localDateTime
    }

    def "Check addMonth in DateUtil"() {
        given:
        LocalDateTime localDateTime = LocalDateTime.of(2018, 7, 19, 15, 31, 24)

        expect:
        DateUtil.addMonth(localDateTime, 0) == localDateTime
    }

    def "Check getDayOfWeek in DateUtil"() {
        given:
        LocalDateTime localDateTime = LocalDateTime.of(2018, 7, 15, 15, 31, 24)

        expect:
        DateUtil.getDayOfWeek(localDateTime, 7) == LocalDateTime.of(2018, 7, 15, 15, 31, 24)
    }

    def "Check getBeginningOfYear in DateUtil"() {
        given:
        LocalDateTime localDateTime = LocalDateTime.of(2018, 7, 15, 15, 31, 24)

        expect:
        DateUtil.getBeginningOfYear(localDateTime) == LocalDateTime.of(2018, 1, 1, 15, 31, 24)
    }

    def "Check exception"() {
        when:
        DateUtil.addDay(null, 1)
        then:
        thrown(IllegalArgumentException)

        when:
        DateUtil.toLocalDateTime((Date)null)
        then:
        thrown(IllegalArgumentException)
    }

    // 更多对日期的操作可以直接使用 java time 提供的方法

    @Unroll
    def "Get #year year #month month 1st date: #result"() {
        expect:
        result.equals LocalDate.of(year, month, 1).toString()

        where:
        year | month | result
        2012 | 12    | '2012-12-01'
    }

    @Unroll
    def "Get #year year #month month last date: #result"() {
        expect:
        result.equals LocalDate.of(year, month, 1).with(TemporalAdjusters.lastDayOfMonth()).toString()

        where:
        year | month | result
        2012 | 12    | '2012-12-31'
        2013 | 2     | '2013-02-28'
    }

}
