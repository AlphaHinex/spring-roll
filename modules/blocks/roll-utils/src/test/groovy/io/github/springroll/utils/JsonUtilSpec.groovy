package io.github.springroll.utils

import com.fasterxml.jackson.core.type.TypeReference
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class JsonUtilSpec extends Specification {

    static BaseEntity entity = new BaseEntity()

    def setupSpec() {
        entity.setId('abcdefg')
        entity.setCreateUserId('999')
    }

    def "Object #obj to JSON string is #result"() {
        expect:
        result == JsonUtil.toJson(obj)
        result == JsonUtil.toJsonIgnoreException(obj)

        where:
        obj                                          | result
        [a: 'a', b: 'b']                             | '{"a":"a","b":"b"}'
        [[a1: 'a1', a2: 'a2'], [b1: 'b1', b2: 'b2']] | '[{"a1":"a1","a2":"a2"},{"b1":"b1","b2":"b2"}]'
        [id: 123, text: '中文']                        | '{"id":123,"text":"中文"}'
        ['a', 'b', 'c']                              | '["a","b","c"]'
    }

    def "Entity to JSON string"() {
        given:
        def result = JsonUtil.toJson(obj)
        println "Entity to JSON string result is: $result"

        expect:
        result ==~ regEx

        where:
        obj              | regEx
        entity           | /\{(".*":"?.*"?,?)+\}/
        [entity, entity] | /\[\{.*\},\{.*\}\]/
    }

    def "Parse JSON string to container object"() {
        given:
        def result1 = JsonUtil.parse(json, Map.class)
        def result2 = JsonUtil.parseIgnoreException(json, Map.class)

        expect:
        keys.split('\\.').each { key ->
            result1 = result1.get(key)
        }
        result1.toString() == value

        keys.split('\\.').each { key ->
            result2 = result2.get(key)
        }
        result2.toString() == value

        where:
        keys       | value    | json
        'email'    | 'aaaa'   | '{"firstName":"Brett","lastName":"McLaughlin","email":"aaaa"}'
        'child.id' | '113000' | """
{
  id: '100000',
  text: 'parent',
  child: {
          id: '113000',
          text: 'child',
          leaf: true
        }
}
"""
    }

    def "Parse JSON string to collection"() {
        given:
        def result1 = JsonUtil.parse(str, List.class)
        def result2 = JsonUtil.parse(str.bytes, List.class)
        def result3 = JsonUtil.parseIgnoreException(str.bytes, List.class)

        expect:
        result1[idx][key] == value
        result2[idx][key] == value
        result3[idx][key] == value

        where:
        idx | key  | value | str
        1   | 'b2' | 'b2'  | '[{"a1":"a1","a2":"a2","a3":"a3"},{"b1":"b1","b2":"b2","b3":"b3"}]'
    }

    def "Parse JSON string to entity array or collection"() {
        given:
        def result1 = JsonUtil.getMapper().readValue(str, new TypeReference<BaseEntity[]>() {})
        def result2 = JsonUtil.getMapper().readValue(str, new TypeReference<List<BaseEntity>>() {})
        def result3 = JsonUtil.getMapper().readValue(str, new TypeReference<BaseEntity[]>() {})
        def result4 = JsonUtil.getMapper().readValue(str, new TypeReference<List<BaseEntity>>() {})

        expect:
        result1[idx] instanceof BaseEntity
        result2[idx] instanceof BaseEntity
        result3[idx] instanceof BaseEntity
        result4[idx] instanceof BaseEntity

        result1[idx][key] == value
        result2[idx][key] == value
        result3[idx][key] == value
        result4[idx][key] == value

        where:
        idx | key  | value | str
        1   | 'id' | '2'   | '[{"id":"1","createUserId":"a"},{"id":"2","createUserId":"b"}]'
        0   | 'id' | '1'   | '[{"id":"1","createUserId":"a"},{"id":"2","createUserId":"b"}]'
    }

    def "parse jsonattr"() {
        List<BaseEntity> bs = new ArrayList<>()
        BaseEntity baseEntity = new BaseEntity()
        baseEntity.setId("123")
        bs.add(baseEntity)
        expect:
        assert JsonUtil.parse(JsonUtil.toJson(bs), new TypeReference<List<BaseEntity>>() {}).get(0).id == "123"
        assert JsonUtil.parseIgnoreException(JsonUtil.toJson(bs), new TypeReference<List<BaseEntity>>() {}).get(0).id == "123"
    }

    static class BaseEntity {
        def id, createUserId
    }

    def 'Parse having exception'() {
        expect:
        JsonUtil.parseIgnoreException('abc', String.class) == null
        JsonUtil.parseIgnoreException('abc'.bytes, String.class) == null
        JsonUtil.parseIgnoreException('abc', new TypeReference<String>() {}) == null
        JsonUtil.toJsonIgnoreException('zzz') == '""'
    }

}
