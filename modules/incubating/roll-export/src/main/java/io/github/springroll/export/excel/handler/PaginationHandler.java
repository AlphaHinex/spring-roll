package io.github.springroll.export.excel.handler;

import java.util.List;
import java.util.Optional;

/**
 * 业务接口返回的一般都是分页对象，而从分页对象中获得具体数据的方式各异
 * 通过实现此接口，可注册根据不同方式获取具体数据的实现，以解耦通用导出方法与具体实现的关联
 */
public interface PaginationHandler {

    /**
     * 提供统一接口方法，由各个实现负责从原始对象中获得所需数据集合
     * 当某具体实现无法从 rawObject 中提取所需数据集合时，返回 Optional.empty()
     *
     * @param  rawObject 原始对象
     * @return 数据集合
     */
    Optional<List> getPaginationData(Object rawObject);

}
