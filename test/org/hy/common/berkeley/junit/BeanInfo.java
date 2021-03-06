package org.hy.common.berkeley.junit;

import java.io.Serializable;

import org.hy.common.Date;





public class BeanInfo implements Serializable
{
    
    private static final long serialVersionUID = -6957962061553124622L;

    
    private String name;
    
    private Date   createTime;
    
    /** 大数据信息 */
    private String bigInfo;

    
    
    /**
     * 获取：
     */
    public String getName()
    {
        return name;
    }

    
    /**
     * 设置：
     * 
     * @param name 
     */
    public void setName(String name)
    {
        this.name = name;
    }

    
    /**
     * 获取：
     */
    public Date getCreateTime()
    {
        return createTime;
    }

    
    /**
     * 设置：
     * 
     * @param createTime 
     */
    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    
    /**
     * 获取：大数据信息
     */
    public String getBigInfo()
    {
        return bigInfo;
    }

    
    /**
     * 设置：大数据信息
     * 
     * @param bigInfo 
     */
    public void setBigInfo(String bigInfo)
    {
        this.bigInfo = bigInfo;
    }
    
}
