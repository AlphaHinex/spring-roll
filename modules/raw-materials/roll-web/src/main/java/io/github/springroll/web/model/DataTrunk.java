package io.github.springroll.web.model;

import io.github.springroll.Version;
import io.github.springroll.utils.CollectionUtil;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * 数据仓库对象
 * 用来存放结果集数据及集合相关基本信息
 * 与 Spring Data 提供的 Page 接口定位类似，单独定义以不直接依赖 Spring Data
 * 可在返回集合的方法上，定义 JsonView 以控制数据展示视角
 *
 * @param <T> 存放的数据类型
 */
public class DataTrunk<T> implements Serializable {

    public static final long serialVersionUID = Version.HASH;

    /**
     * 结果集数据，可以是全部数据集合，也可以是部分数据集合
     */
    private Collection<T> data;

    /**
     * 结果集数据总数
     */
    private long total;

    public DataTrunk() {
    }

    public DataTrunk(Collection<T> data) {
        this.data = data;
        this.total = null == data ? 0 : data.size();
    }

    public DataTrunk(Collection<T> data, long total) {
        this.data = data;
        this.total = total;
    }

    @SuppressWarnings("unchecked")
    public DataTrunk(Collection<T> allData, int pageNo, int pageSize) {
        if (CollectionUtils.isEmpty(allData)) {
            this.data = Collections.EMPTY_LIST;
            this.total = 0;
        } else {
            Assert.isTrue(pageNo > 0 && pageSize > 0, "Page number and page size SHOULD FROM 1!");
            int from = (pageNo - 1) * pageSize;
            int to = from + pageSize;
            if (to > allData.size()) {
                to = allData.size();
            }
            if (from > to) {
                this.data = Collections.EMPTY_LIST;
                this.total = allData.size();
            } else {
                this.data = Arrays.asList((T[]) Arrays.copyOfRange(allData.toArray(), from, to));
                this.total = allData.size();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DataTrunk)) {
            return false;
        }
        DataTrunk dt = (DataTrunk) obj;
        return CollectionUtil.isEqualCollection(dt.getData(), data) && dt.getTotal() == total;
    }

    @Override
    public int hashCode() {
        return data.hashCode() + ((Long)total).hashCode();
    }

    public Collection<T> getData() {
        return data;
    }

    public void setData(Collection<T> data) {
        this.data = data;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

}
