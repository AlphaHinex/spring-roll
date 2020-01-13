package io.github.springroll.base;

import java.io.Serializable;

public interface Historical extends Serializable {

    /**
     * 获取id
     * @return id
     */
    String getId();

    /**
     * 赋值id
     * @param id id
     */
    void setId(String id);

    /**
     * 获取创建人id
     * @return createUserId
     */
    String getCreateUserId();

    /**
     * 赋值创建人id
     * @param createUserId createUserId
     */
    void setCreateUserId(String createUserId);

    /**
     * 获取创建时间
     * @return createTime
     */
    String getCreateTime();

    /**
     * 赋值创建时间
     * @param createTime createTime
     */
    void setCreateTime(String createTime);

    /**
     * 获取最后修改人的id
     * @return lastModifyUserId
     */
    String getLastModifyUserId();

    /**
     * 赋值最后修改人的id
     * @param lastModifyUserId lastModifyUserId
     */
    void setLastModifyUserId(String lastModifyUserId);

    /**
     * 获取最后修改时间
     * @return lastModifyTime
     */
    String getLastModifyTime();

    /**
     * 赋值最后修改时间
     * @param lastModifyTime lastModifyTime
     */
    void setLastModifyTime(String lastModifyTime);

}
