package io.github.springroll.export.excel

import com.alibaba.excel.EasyExcel
import io.github.springroll.export.excel.handler.PaginationHandler
import io.github.springroll.test.AbstractSpringTest
import io.github.springroll.test.TestResource
import io.github.springroll.utils.JsonUtil
import io.github.springroll.web.controller.BaseController
import io.github.springroll.web.model.DataTrunk
import org.junit.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.NestedServletException

import javax.servlet.http.HttpServletRequest

class ExportExcelControllerTest extends AbstractSpringTest {

    @Test
    void testExportAll() {
        checkExportData('中文', '/test/query', 3)

        def colDef = [new ColumnDef("名称", "name")]
        def col = new ColumnDef('描述', 'des')
        col.setHidden(true)
        colDef << col
        // Add 2 params in ExportExcelController.exportAll
        checkExportData('from request', 'http://localhost:8080/test/query/req?a=1&b=2', 2 + 2, '', colDef)

        col.setShowTitle(true)
        checkExportData('multi', '/test/query/multi?integer=1&str=abc&name=星球&des=', 3, 'ISO_8859_1', colDef)
    }

    void checkExportData(String fileTitle, String queryUrl, int rowCount, encode = 'utf-8', colDef = [new ColumnDef("名称", "name")]) {
        def title = URLEncoder.encode(fileTitle,'utf-8')
        def cols = URLEncoder.encode(JsonUtil.toJsonIgnoreException(colDef), 'UTF-8')
        def url = URLEncoder.encode(queryUrl, 'UTF-8')
        def response = get("/export/excel/all/$title?cols=$cols&url=$url&tomcatUriEncoding=$encode", HttpStatus.OK).getResponse()

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
    void testPostExportAll() {
        def title = URLEncoder.encode('中文','utf-8')
        def model = [
                cols: [["name":"userName","display":"员工姓名"],["name":"userId","display":"员工编号"]],
                url: '/test/query/post',
                bizReqBody: [
                    name: "body name",
                    des: "body des"
                ]
        ]
        post("/export/excel/all/$title", JsonUtil.toJsonIgnoreException(model), HttpStatus.CREATED)
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

    @PostMapping('/post')
    List<Planet> post(@RequestBody Planet planet) {
        [planet]
    }

}

class Planet {
    String name
    String des

    Planet(String name) {
        this. name = name
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
