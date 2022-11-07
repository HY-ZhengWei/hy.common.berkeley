package org.hy.common.berkeley;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;





/**
 * 可序列化对象的专有数据库
 * 
 * 专用于存储类信息
 * 
 * 1. 实例化，就打开数据库了
 * 2. 同时操作两个数据库
 *
 * @author      ZhengWei(HY)
 * @createDate  2015-01-09
 * @version     v1.0
 *              v2.0  2016-02-17  添加：获取所有对象记录的方法
 *              v3.0  2017-06-20  修复：游标用完后，及时关闭释放的功能
 */
public class ClassBerkeley
{
    
    /** 真实数据的存储数据库 */
    private Berkeley                             dataDB;
    
    /** 可序列化对象的存储数据库 */
    private Berkeley                             catalogDB;
    
    /** 
     * 可序列化对象的翻译信息
     * 
     *  Map.key  为 ClassInfo.classInfo.getName()，即类的全名称
     */
    private Map<String ,ClassInfo>               catalogClasses;
    
    
    
    public ClassBerkeley(final Berkeley i_Berkeley ,String i_DatabaseName)
    {
        this.dataDB         = i_Berkeley;
        this.catalogClasses = new Hashtable<String ,ClassInfo>();
        this.catalogDB      = new Berkeley();
        
        this.catalogDB.setEnvironmentConfig(this.dataDB.getEnvironmentConfig());
        this.catalogDB.setEnvironmentHome(  this.dataDB.getEnvironmentHome());
        this.catalogDB.setEnvironment(      this.dataDB.getEnvironment());
        this.catalogDB.setDatabaseConfig(   this.dataDB.getDatabaseConfig());
        this.catalogDB.setDatabaseName(     i_DatabaseName);
        this.catalogDB.setAutoCommit(       this.dataDB.isAutoCommit());
        this.catalogDB.setDataEnCode(       this.dataDB.getDataEnCode());
        
        try
        {
            this.catalogDB.setDatabase(this.catalogDB.getEnvironment().openDatabase(null ,this.catalogDB.getDatabaseName() ,this.catalogDB.getDatabaseConfig()));
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
    }
    
    
    
    @SuppressWarnings("unchecked")
    public ClassInfo getClassInfo(String i_ClassName)
    {
        try
        {
            Class<?> v_Class = Class.forName(i_ClassName);
            
            return this.getClassInfo((Class<? extends Serializable>)v_Class);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return null;
    }
    
    
    
    public synchronized ClassInfo getClassInfo(Class<? extends Serializable> i_Class)
    {
        String    v_ClassName = i_Class.getName();
        ClassInfo v_ClassInfo = this.catalogClasses.get(v_ClassName);
        
        if ( v_ClassInfo == null )
        {
            v_ClassInfo = new ClassInfo(this.catalogDB ,i_Class);
            this.catalogClasses.put(v_ClassName ,v_ClassInfo);
        }
        
        return v_ClassInfo;
    }
    
    
    
    /**
     * 向数据库中添加一条记录。
     * 
     * 如果数据库不支持一个key对应多个data或当前database中已经存在该key了，
     * 则使用此方法将使用新的值覆盖旧的值。
     *
     * @param i_Key
     * @param i_Value
     * @return
     */
    public <T extends Serializable> boolean put(String i_Key ,T i_ObjValue)
    {
        ClassInfo v_ClassInfo = this.getClassInfo(i_ObjValue.getClass());
        
        if ( v_ClassInfo == null )
        {
            return false;
        }
        
        try
        {
            boolean v_Ret = this.dataDB.put(this.dataDB.toDBEntry(i_Key) ,v_ClassInfo.toDBEntry(i_ObjValue));
            
            if ( v_Ret )
            {
                v_Ret = this.catalogDB.put(i_Key ,i_ObjValue.getClass().getName());
                
                if ( v_Ret )
                {
                    return true;
                }
                else
                {
                    this.dataDB.delete(i_Key);
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return false;
    }
    
    
    
    /**
     * 向数据库中添加一条记录。
     * 
     * 如果原先已经有了该key，则不覆盖。
     * 不管database是否允许支持多重记录(一个key对应多个value),
     * 只要存在该key就不允许添加，并且返回OperationStatus.KEYEXIST信息。
     *
     * @param i_Key
     * @param i_Value
     * @return
     */
    public <T extends Serializable> boolean putNoOverwrite(String i_Key ,T i_ObjValue)
    {
        ClassInfo v_ClassInfo = this.getClassInfo(i_ObjValue.getClass());
        
        if ( v_ClassInfo == null )
        {
            return false;
        }
        
        try
        {
            boolean v_Ret = this.dataDB.putNoOverwrite(this.dataDB.toDBEntry(i_Key) ,v_ClassInfo.toDBEntry(i_ObjValue));
            
            if ( v_Ret )
            {
                v_Ret = this.catalogDB.putNoOverwrite(i_Key ,i_ObjValue.getClass().getName());
                
                if ( v_Ret )
                {
                    return true;
                }
                else
                {
                    this.dataDB.delete(i_Key);
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return false;
    }
    
    
    
    /**
     * 向数据库中添加一条记录。
     * 
     * 如果原先已经有了该key，则不覆盖。
     * 不管database是否允许支持多重记录(一个key对应多个value),
     * 只要存在该key就不允许添加，并且返回OperationStatus.KEYEXIST信息。
     *
     * @param i_Key
     * @param i_Value
     * @return
     */
    public <T extends Serializable> boolean putNoDupData(String i_Key ,T i_ObjValue)
    {
        ClassInfo v_ClassInfo = this.getClassInfo(i_ObjValue.getClass());
        
        if ( v_ClassInfo == null )
        {
            return false;
        }
        
        try
        {
            boolean v_Ret = this.dataDB.putNoDupData(this.dataDB.toDBEntry(i_Key) ,v_ClassInfo.toDBEntry(i_ObjValue));
            
            if ( v_Ret )
            {
                v_Ret = this.catalogDB.putNoDupData(i_Key ,i_ObjValue.getClass().getName());
                
                if ( v_Ret )
                {
                    return true;
                }
                else
                {
                    this.dataDB.delete(i_Key);
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return false;
    }
    
    
    
    /**
     * 获取记录的方法，通过key的方式来匹配，如果没有改记录则返回OperationStatus.NOTFOUND
     * 
     * @param i_Key
     * @return
     */
    public Object getObject(String i_Key)
    {
        String v_ClassName = this.catalogDB.get(i_Key);
        
        if ( v_ClassName != null )
        {
            ClassInfo v_ClassInfo = this.catalogClasses.get(v_ClassName);
            
            if ( v_ClassInfo != null )
            {
                DatabaseEntry v_DBEntry = this.dataDB.getDBEntry(i_Key);
                
                if ( v_DBEntry != null )
                {
                    return v_ClassInfo.getCatalogBinding().entryToObject(v_DBEntry);
                }
            }
        }
        
        return null;
    }
    
    
    
    /**
     * 获取所有记录对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-17
     * @version     v1.0
     *
     * @return
     */
    public Map<String ,?> getObjects()
    {
        Map<String ,Object>        v_Ret     = new HashMap<String ,Object>();
        Cursor                     v_Cursor  = null;
        DatabaseEntry              v_Key     = new DatabaseEntry();  
        DatabaseEntry              v_Value   = new DatabaseEntry();
        
        try
        {
            v_Cursor  = this.catalogDB.getDatabase().openCursor(null ,null);
            
            while ( v_Cursor.getNext(v_Key ,v_Value ,LockMode.DEFAULT) == OperationStatus.SUCCESS )
            {
                String v_ClassName = new String(v_Value.getData() ,this.dataDB.getDataEnCode());
                if ( null != v_ClassName )
                {
                    ClassInfo v_ClassInfo = this.catalogClasses.get(v_ClassName);
                    
                    if ( v_ClassInfo != null )
                    {
                        DatabaseEntry v_DBEntry = this.dataDB.getDBEntry(v_Key);
                        
                        if ( v_DBEntry != null )
                        {
                            v_Ret.put(new String(v_Key.getData() ,this.dataDB.getDataEnCode()) ,v_ClassInfo.getCatalogBinding().entryToObject(v_DBEntry));
                        }
                    }
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            Berkeley.closeCursor(v_Cursor);
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 数据库中的可序列化对象的总数
     * 
     * @return
     */
    public long getObjectCount()
    {
        return this.catalogDB.getCount();
    }
    
    
    
    /**
     * 获取：可序列化对象的存储数据库
     */
    public Berkeley getCatalogDB()
    {
        return catalogDB;
    }
    
    
    
    /**
     * 清空数据库所有记录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-17
     * @version     v1.0
     *
     */
    public long truncateObjectDB()
    {
        if ( this.getObjectCount() > 0 )
        {
            this.catalogDB.truncateDB();
            return this.dataDB.truncateDB();
        }
        else
        {
            return 0;
        }
    }
    
    
    
    /**
     * 关闭
     * 
     * 1. 不能在此关闭运行环境，environment.close() 。因为 this.dataDB 也在用同一份运行环境
     * 2. 不能在此直接简单的调用 this.catalogDB.close() 。因为会关闭运行环境。 
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-06-20
     * @version     v1.0
     *
     */
    public void close()
    {
        if ( this.catalogDB != null )
        {
            try
            {
                if ( this.catalogDB.getDatabase() != null )
                {
                    this.catalogDB.getDatabase().close();
                }
                
                this.catalogDB.setDatabase(null);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
    }
    
}
