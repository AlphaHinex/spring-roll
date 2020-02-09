package io.github.springroll.export.excel

import io.github.springroll.export.excel.handler.PaginationHandler
import io.github.springroll.test.AbstractSpringTest
import io.github.springroll.utils.JsonUtil
import io.github.springroll.web.controller.BaseController
import io.github.springroll.web.model.DataTrunk
import org.junit.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

class ExportExcelControllerTest extends AbstractSpringTest {

    @Test
    void testExportAll() {
        def title = URLEncoder.encode('中文','utf-8')
        def cols = URLEncoder.encode(JsonUtil.toJsonIgnoreException([new ColumnDef("名称", "name")]), 'UTF-8')
        def url = URLEncoder.encode('/test/query', 'UTF-8')
        get("/export/excel/all/$title?cols=$cols&url=$url", HttpStatus.OK)
    }

}

@RestController
@RequestMapping('/test/query')
class Controller extends BaseController {

    @GetMapping
    ResponseEntity<DataTrunk<Planet>> query() {
        responseOfGet(new DataTrunk<>([new Planet('水星'), new Planet('金星')]))
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
