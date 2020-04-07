package io.github.springroll.export.excel

import com.alibaba.excel.EasyExcel
import io.github.springroll.export.excel.handler.DecodeHandler
import io.github.springroll.export.excel.handler.PaginationHandler
import io.github.springroll.test.AbstractSpringTest
import io.github.springroll.test.TestResource
import io.github.springroll.utils.JsonUtil
import io.github.springroll.web.controller.BaseController
import io.github.springroll.web.model.DataTrunk
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.NestedServletException

import javax.servlet.http.HttpServletRequest

class ExportExcelControllerTest extends AbstractSpringTest {

    @Autowired
    ExportExcelProperties properties

    @Test
    void test() {
        def cols = '[{"display":"名称","name":"name","showTitle":true,"field":"name","hidden":false,"label":"名称","prop":"name","title":"名称"},{"label":"名称","prop":"name","width":"40"}]'
        cols = URLEncoder.encode(cols, 'UTF-8')
        def url = URLEncoder.encode('/test/query', 'UTF-8')
        get("/export/excel/abc?cols=$cols&url=$url", HttpStatus.OK)
    }

    @Test
    void testExport() {
        checkExportData('中文', '/test/query', 3)

        def colDef = [new ColumnDef("名称", "name")]
        def col = new ColumnDef('描述', 'des')
        col.setHidden(true)
        colDef << col
        checkExportData('from request', 'http://localhost:8080/test/query/req?a=1&b=2', 2 + 2, '', colDef)

        col.setShowTitle(true)
        checkExportData('multi', '/test/query/multi?integer=1&str=abc&name=星球&des=', 3, 'ISO_8859_1', colDef)

        checkExportData('map', '/test/query/map', 2)
        checkExportData('exception', '/test/query/exception', 2)
    }

    void checkExportData(String fileTitle, String queryUrl, int rowCount, encode = 'utf-8', colDef = [new ColumnDef("名称", "name")]) {
        def title = URLEncoder.encode(fileTitle,'utf-8')
        def cols = URLEncoder.encode(JsonUtil.toJsonIgnoreException(colDef), 'UTF-8')
        def url = URLEncoder.encode(queryUrl, 'UTF-8')
        def response = get("/export/excel/$title?cols=$cols&url=$url&tomcatUriEncoding=$encode", HttpStatus.OK).getResponse()
        checkResponse(response, title, rowCount)
    }

    def checkResponse(response, title, rowCount) {
        def disposition = response.getHeader('Content-Disposition')
        assert disposition.contains('filename=')
        def filename = disposition.substring(disposition.indexOf('filename=') + 9)

        def bytes = response.getContentAsByteArray()
        assert bytes.length > 0

        def xlsFile = TestResource.getFile(URLDecoder.decode(filename, 'utf-8'))
        xlsFile.withOutputStream { os ->
            os.write(bytes)
        }
        assert filename == "${title}.xlsx"

        def data = EasyExcel.read(xlsFile).sheet().doReadSync()
        assert data.size() == rowCount
        xlsFile.delete()
        data
    }

    @Test(expected = NestedServletException)
    void invalidMapKeyWouldThrowRuntimeException() {
        checkExportData('null', '/test/query/null?name=pn', 0)
    }

    @Test(expected = NestedServletException)
    void noSuitableHandler() {
        checkExportData('list', '/test/query/list?name=pn', 0)
    }

    @Test
    void testPostExport() {
        def title = URLEncoder.encode('中文post','utf-8')
        def model = [
                cols: [
                        ["prop":"name","label":"名称","decoder":[key: 'not_exist', value: '不会出现这个值']],
                        ["prop":"des","label":"描述","decoder":[key: 'plant_des', value: '翻译后的描述']],
                        ["label":"无prop","other": "props","width":"40"],
                        ["prop":"timestamp","label":"时间戳","decoder":[key: properties.getDateDecoderKey(), value: 'yy-MM-dd HH:mm:ss']]
                ],
                url: "/test/query/post/plant_name/plant_des?${properties.getPageNumber()}=2&${properties.getPageSize()}=10".toString(),
                bizReqBody: [
                    name: "body name",
                    des: "body des"
                ],
                total: '0',
                tomcatUriEncoding: 'utf-8'
        ]
        def response = post("/export/excel/$title", JsonUtil.toJsonIgnoreException(model), HttpStatus.OK).getResponse()
        def data = checkResponse(response, title, 3)
        assert data[0][1] == 'body des'
        assert data[1][1] == '翻译后的描述'
        assert data[2][0] == '2'
        assert data[2][1] == '10'
    }

    @Test
    void testReqWithoutCharset() {
        def model = [
                cols: [["prop":"name","label":"名称"]],
                url: '/test/query/post/plant_name/plant_des',
                bizReqBody: [
                        name: "body name",
                        des: "body des"
                ]
        ]
        post("/export/excel/testReqWithoutCharset", MediaType.APPLICATION_JSON, null, JsonUtil.toJsonIgnoreException(model), HttpStatus.OK)
    }

}

@RestController
@RequestMapping('/test/query')
class Controller extends BaseController {

    @GetMapping
    ResponseEntity<DataTrunk<Planet>> query() {
        responseOfGet(new DataTrunk<>([
                new Planet('水星'),
                new Planet('金星'),
                new Planet('地球')
        ]))
    }

    @GetMapping('/req')
    ResponseEntity<DataTrunk<Planet>> query(HttpServletRequest request) {
        def list = []
        request.getParameterNames().each {
            list << new Planet(it)
        }
        responseOfGet(new DataTrunk<>(list))
    }

    @GetMapping('/multi')
    Map<String, List<Planet>> query(Integer integer, String str, Planet planet) {
        def list = []
        list << new Planet(integer + '')
        list << new Planet(str)
        list << planet
        ['rows': list]
    }

    @GetMapping('/null')
    Map<String, List<Planet>> query(Planet planet) {
        [otherKey: [planet]]
    }

    @GetMapping('/list')
    List<Planet> listQuery(Planet planet) {
        [planet]
    }

    @GetMapping('/map')
    Map map() {
        [rows: [
                [userName: 'Jordan', age: '23'],
                [userName: 'Kobe', age: '8']
        ]]
    }

    @GetMapping('/exception')
    Map exception() {
        [rows: ['Jordan', 'Kobe']]
    }

    @PostMapping('/post/{name}/{des}')
    Map<String, List<Planet>> post(@PathVariable String name, @PathVariable String des, @RequestBody Planet planet, Integer pNo, Integer pSize) {
        def planet2 = new Planet(name)
        planet2.setDes(des)
        def planet3 = new Planet(pNo + '')
        planet3.setDes(pSize + '')
        ['rows': [planet, planet2, planet3]]
    }

}

class Planet {
    String name
    String des
    Date timestamp

    void setTimestamp(Date timestamp) {
        this.timestamp = (Date) timestamp.clone()
    }

    Date getTimestamp() {
        return (Date) timestamp.clone()
    }

    Planet() {
        this.timestamp = new Date()
    }

    Planet(String name) {
        this()
        this.name = name
    }

}

@Component
class DataTrunkPaginationHandler implements PaginationHandler {

    @Override
    Optional<Collection> getPaginationData(Object rawObject) {
        if (rawObject instanceof ResponseEntity) {
            def entity = (ResponseEntity) rawObject
            if (entity.getBody() instanceof DataTrunk) {
                def dataTrunk = (DataTrunk) entity.getBody()
                return Optional.of(dataTrunk.getData())
            }
        }
        return Optional.empty()
    }

}

@Component
class PlantDesDecodeHandler implements DecodeHandler {

    @Override
    String getDecoderKey() {
        return 'plant_des'
    }

    @Override
    String decode(Object obj, String decoderValue) {
        getDecoderKey() == obj ? decoderValue : obj.toString()
    }

}