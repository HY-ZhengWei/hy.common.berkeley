package org.hy.common.berkeley;

import java.io.Serializable;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.DatabaseEntry;





/**
 * 可序列化对象的翻译信息
 * 
 * 在内存中缓存Berkeley类对应的使用过的可序列化对象的翻译信息。
 * 这样可提高性能。因为同样类型(classInfo)的对象，会被多次访问，
 * 当首次访问时，就记录在内存中，从而减少直接读写磁盘数据的机率。
 *
 * @author      ZhengWei(HY)
 * @createDate  2015-01-09
 * @version     v1.0
 */
public class ClassInfo implements Comparable<ClassInfo>
{
    
    /** 可序列化对象的元类型 */
    private Class<? extends Serializable>        classInfo;
    
    /** 可序列化对象的存储类信息 */
    private StoredClassCatalog                   catalogStored;
    
    /** 可序列化对象的绑定数据和类的绑定对象 */
    private EntryBinding<Object>                 catalogBinding;

    
    
    @SuppressWarnings({"unchecked" ,"rawtypes"})
    public ClassInfo(final Berkeley i_Berkeley ,Class<? extends Serializable> i_ClassInfo)
    {
        this.classInfo = i_ClassInfo;
        
        this.catalogStored  = new StoredClassCatalog(i_Berkeley.getDatabase());
        this.catalogBinding = new SerialBinding(this.catalogStored ,this.classInfo);
    }
    
    
    
    public <T extends Serializable> DatabaseEntry toDBEntry(T i_Obj)
    {
        DatabaseEntry v_Ret = new DatabaseEntry();
        
        this.catalogBinding.objectToEntry(i_Obj, v_Ret);
        
        return v_Ret;
    }
    
    
    
    /**
     * 获取：可序列化对象的元类型
     */
    public Class<? extends Serializable> getClassInfo()
    {
        return classInfo;
    }

    
    
    /**
     * 获取：可序列化对象的存储类信息
     */
    public StoredClassCatalog getCatalogStored()
    {
        return catalogStored;
    }

    
    
    /**
     * 获取：可序列化对象的绑定数据和类的绑定对象
     */
    public EntryBinding<Object> getCatalogBinding()
    {
        return catalogBinding;
    }
    
    
    
    @Override
    public int hashCode()
    {
        return this.classInfo.hashCode();
    }



    @Override
    public boolean equals(Object i_Other)
    {
        if ( i_Other == null )
        {
            return false;
        }
        else if ( i_Other instanceof ClassInfo )
        {
            ClassInfo v_Other = (ClassInfo)i_Other;
            
            return this.classInfo.equals(v_Other.getClassInfo());
        }
        else
        {
            return false;
        }
    }



    @Override
    public int compareTo(ClassInfo i_Other)
    {
        if ( i_Other == null || i_Other.getClassInfo() == null )
        {
            return 1;
        }
        else if ( this.classInfo == i_Other.getClassInfo() )
        {
            return 0;
        }
        else if ( this.classInfo.equals(i_Other.getClassInfo()) )
        {
            return 0;
        }
        else
        {
            return -1;
        }
    }
    
}
