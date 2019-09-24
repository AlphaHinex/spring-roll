package io.github.springroll.webmvc.model;

import io.github.springroll.Version;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * 数据仓库实体
 * 用来存放结果集数据及集合相关基本信息
 *
 * @param <T> 存放的数据类型
 */
public class DataTrunk<T> implements Serializable {

    public static final long serialVersionUID = Version.HASH;

    /**
     * 结果集数据
     */
    private Collection<T> data;

    /**
     * 结果集数据总数
     */
    private long count;

    public DataTrunk() {
    }

    public DataTrunk(Collection<T> data) {
        this.data = data;
        this.count = null == data ? 0 : data.size();
    }

    public DataTrunk(Collection<T> data, long count) {
        this.data = data;
        this.count = count;
    }

    @SuppressWarnings("unchecked")
    public DataTrunk(Collection<T> allData, int pageNo, int pageSize) {
        if (CollectionUtils.isEmpty(allData)) {
            this.data = Collections.EMPTY_LIST;
            this.count = 0;
        } else {
            Assert.isTrue(pageNo > 0, "Page Number SHOULD FROM 1!");
            int from = (pageNo - 1) * pageSize;
            int to = from + pageSize;
            if (to > allData.size()) {
                to = allData.size();
            }
            if (from > to) {
                this.data = Collections.EMPTY_LIST;
                this.count = allData.size();
            } else {
                this.data = Arrays.asList((T[]) Arrays.copyOfRange(allData.toArray(), from, to));
                this.count = allData.size();
            }
        }
    }

    public Collection<T> getData() {
        return data;
    }

    public void setData(Collection<T> data) {
        this.data = data;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

}
