package io.github.springroll.export.excel

import io.github.springroll.test.AbstractSpringTest
import io.github.springroll.utils.JsonUtil
import org.junit.Test
import org.springframework.http.HttpStatus

class ExportExcelControllerTest extends AbstractSpringTest {

    @Test
    void testExportAll() {
        def title = URLEncoder.encode('中文','utf-8')
        def cols = URLEncoder.encode(JsonUtil.toJsonIgnoreException([new ColumnDef("名称", "name")]), 'UTF-8')
        def url = URLEncoder.encode('/', 'UTF-8')
        get("/export/excel/all/$title?cols=$cols&url=$url", HttpStatus.OK)
    }

}