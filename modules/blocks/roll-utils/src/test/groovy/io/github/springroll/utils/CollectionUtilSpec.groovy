package io.github.springroll.utils

import spock.lang.Specification

class CollectionUtilSpec extends Specification {

    def "Extract to interface collection"() {
        Collection<HashMap<String, String>> collection = [['a': '1'], ['b': '2'], ['c': '3']]
        Collection<Map<String, String>> c = CollectionUtil.convert(collection)

        expect:
        c.size() == collection.size()
        c == collection
    }

}
