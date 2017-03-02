package junit.org.hy.common.berkeley.junit;

import java.io.Serializable;

import org.hy.common.Date;





public class ElseInfo implements Serializable
{
    
    private static final long serialVersionUID = 5509814602223764933L;
    
    
    private Date   createTime;

    
    
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
    
}
