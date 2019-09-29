package io.github.springroll.web.model

import spock.lang.Specification

class DataTrunkSpec extends Specification {

    def "Sub collection"() {
        DataTrunk dt = new DataTrunk<>(allData, pageNo, pageSize)

        expect:
        dt.data.size() == size
        dt.count == count

        where:
        pageNo | pageSize | size | count | allData
        1      | 10       | 3    | 3     | ['a', 'b', 'c']
        2      | 2        | 2    | 4     | ['a', 'b', 'c', 'd']
        1      | 2        | 2    | 5     | ['a', 'b', 'c', 'd', 'e']
        2      | 2        | 2    | 5     | ['a', 'b', 'c', 'd', 'e']
        3      | 2        | 1    | 5     | ['a', 'b', 'c', 'd', 'e']
        4      | 2        | 0    | 5     | ['a', 'b', 'c', 'd', 'e']
        2      | 2        | 0    | 0     | []
        2      | 2        | 0    | 0     | null
    }

}
