package org.hy.common.berkeley.junit;

import java.io.Serializable;





public class ChildBean implements Serializable
{
    
    private static final long serialVersionUID = 3791900618307573634L;
    

    private BeanInfo bean;
    
    private Integer  count;

    
    /**
     * 获取：
     */
    public BeanInfo getBean()
    {
        return bean;
    }

    
    /**
     * 设置：
     * 
     * @param bean 
     */
    public void setBean(BeanInfo bean)
    {
        this.bean = bean;
    }

    
    /**
     * 获取：
     */
    public Integer getCount()
    {
        return count;
    }

    
    /**
     * 设置：
     * 
     * @param count 
     */
    public void setCount(Integer count)
    {
        this.count = count;
    }
    
}
