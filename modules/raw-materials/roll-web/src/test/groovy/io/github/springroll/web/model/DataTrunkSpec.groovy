package io.github.springroll.web.model

import spock.lang.Specification

class DataTrunkSpec extends Specification {

    def "Check size and total with page no and size"() {
        DataTrunk dt = new DataTrunk<>(allData, pageNo, pageSize)

        expect:
        dt.data.size() == size
        dt.total == total

        where:
        pageNo | pageSize | size | total | allData
        1      | 10       | 3    | 3     | ['a', 'b', 'c']
        2      | 2        | 2    | 4     | ['a', 'b', 'c', 'd']
        1      | 2        | 2    | 5     | ['a', 'b', 'c', 'd', 'e']
        2      | 2        | 2    | 5     | ['a', 'b', 'c', 'd', 'e']
        3      | 2        | 1    | 5     | ['a', 'b', 'c', 'd', 'e']
        4      | 2        | 0    | 5     | ['a', 'b', 'c', 'd', 'e']
        2      | 2        | 0    | 0     | []
        2      | 2        | 0    | 0     | null
    }

    def "Check constructors"() {
        def data = ['a','b']
        DataTrunk dt1 = new DataTrunk()
        dt1.setData(data)
        dt1.setTotal(data.size())

        DataTrunk dt2 = new DataTrunk(data)

        DataTrunk dt3 = new DataTrunk(data, data.size())

        DataTrunk dt4 = new DataTrunk(null)

        expect:
        dt1 == dt2
        dt2 == dt3
        dt1 == dt3
        dt1.hashCode() == dt2.hashCode()
        dt1.hashCode() == dt3.hashCode()
        dt4.getTotal() == 0
        dt1 != data

        dt3.setTotal(10)
        dt1 != dt3

        dt2.setData(null)
        dt1 != dt2
    }

    def "Check assert"() {
        when:
        new DataTrunk<>(col, pn, ps)

        then:
        thrown(IllegalArgumentException)

        where:
        col     | pn    | ps
        ['a']   | -1    | 2
        ['a']   | 1     | -2
        ['a']   | -1    | -2
    }

}
