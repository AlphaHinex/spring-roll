package io.github.springroll.export.excel

import io.github.springroll.export.excel.handler.PaginationHandler
import io.github.springroll.test.AbstractSpringTest
import io.github.springroll.test.TestResource
import io.github.springroll.utils.JsonUtil
import io.github.springroll.web.controller.BaseController
import io.github.springroll.web.model.DataTrunk
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.junit.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

class ExportExcelControllerTest extends AbstractSpringTest {

    @Test
    void testExportAll() {
        checkExportData('中文', '/test/query', 3)
        // Add 2 params in ExportExcelController.getPageData
        checkExportData('from request', '/test/query/req?a=1&b=2', 2 + 2)
        checkExportData('multi', '/test/query/multi?integer=1&str=abc&name=星球', 3)
    }

    void checkExportData(String fileTitle, String queryUrl, int rowCount) {
        def title = URLEncoder.encode(fileTitle,'utf-8')
        def cols = URLEncoder.encode(JsonUtil.toJsonIgnoreException([new ColumnDef("名称", "name")]), 'UTF-8')
        def url = URLEncoder.encode(queryUrl, 'UTF-8')
        def response = get("/export/excel/all/$title?cols=$cols&url=$url", HttpStatus.OK).getResponse()

        def disposition = response.getHeader('Content-Disposition')
        assert disposition.contains('filename=')
        def filename = disposition.substring(disposition.indexOf('filename=') + 9)

        def bytes = response.getContentAsByteArray()
        assert bytes.length > 0

        def xlsFile = TestResource.getFile(URLDecoder.decode(filename, 'utf-8'))
        xlsFile.withOutputStream { os ->
            os.write(bytes)
        }
        assert filename == "${title}.xls"

        xlsFile.withInputStream { is ->
            Workbook wb = new HSSFWorkbook(is)
            Sheet sheet = wb.getSheetAt(0)
            assert sheet.getRow(0).getCell(0).getStringCellValue() == '名称'
            assert sheet.getLastRowNum() == rowCount
        }
        xlsFile.delete()
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
    ResponseEntity<DataTrunk<Planet>> query(Integer integer, String str, Planet planet) {
        def list = []
        list << new Planet(integer + '')
        list << new Planet(str)
        list << planet
        responseOfGet(new DataTrunk<>(list))
    }

}

class Planet {
    String name

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
        return null
    }

}
