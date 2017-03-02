package org.hy.common.berkeley;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Help;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;





/**
 * 嵌入式(文件)数据库Berkeley
 * 
 * 1. 同时创建两个数据库。
 * 2. 一个是存储真实数据的数据库。即普通的数据库key/value。
 * 3. 一个存储可序列化对象的结构的数据库。
 * 4. 当存储一个可序列化对象时，先在真实数据的数据库中存储key及对象的序列化值。
 * 5. 当存储一个可序列化对象时，后在可序列化的数据库中存储同样的key及Java对象的元类的全名称。
 *
 * @author      ZhengWei(HY)
 * @createDate  2015-01-09
 * @version     v1.0
 *              v2.0  2016-02-17  添加：获取所有简单记录的方法
 *                                添加：获取所有对象记录的方法
 *                                添加：提交数据的方法
 *                                添加：删除所有记录的方法
 */
public class Berkeley
{
    
    /** 
     * 数据库环境配置
     * 
     * 1. setAllowCreate()     如果设置了true则表示当数据库环境不存在时候重新创建一个数据库环境，默认为false.
     * 2. setReadOnly()        以只读方式打开，默认为false.
     * 3. setTransactional()   事务支持,如果为true，则表示当前环境支持事务处理，默认为false，不支持事务处理。
     * 4. setLocking()         是否有锁。默认为true
     * 
     * 其父类的方法
     * 1. setCachePercent()    设置当前环境能够使用的RAM占整个JVM内存的百分比。
     * 2. setCacheSize()       设置当前环境能够使用的最大RAM。单位BYTE 
     * 3. setTxnNoSync()       当提交事务的时候是否把缓存中的内容同步到磁盘中去。true 表示不同步，也就是说不写磁盘
     * 4. setTxnWriteNoSync()  当提交事务的时候，是否把缓冲的log写到磁盘上。true 表示不同步，也就是说不写磁盘  
     */
    private EnvironmentConfig          environmentConfig;
    
    /** 数据库环境的路径 */
    private String                     environmentHome;
    
    /** 数据库环境的实例对象 */
    private Environment                environment;
    
    
    
    /** 
     * 数据库配置
     * 
     * 1. setAllowCreate()          如果是true的话，则当不存在此数据库的时候创建一个。
     * 2. setBtreeComparator()      设置用于Btree比较的比较器，通常是用来排序 
     * 3. setDuplicateComparator()  设置用来比较一个key有两个不同值的时候的大小比较器。
     * 4. setSortedDuplicates()     设置一个key是否允许存储多个值，true代表允许，默认false.
     * 5. setExclusiveCreate()      以独占的方式打开，也就是说同一个时间只能有一实例打开这个database。
     * 6. setReadOnly()             以只读方式打开database,默认是false.
     * 7. setTransactional()        如果设置为true,则支持事务处理，默认是false，不支持事务。
     * 8. setDeferredWrite()        设置延迟写入选项。
     */
    private DatabaseConfig             databaseConfig;
    
    /** 数据库名称 */
    private String                     databaseName;

    /** 数据库的实例对象 */
    private Database                   database;
    
    /** 
     * 可序列化对象的专有数据库
     */
    private ClassBerkeley              classBerkeley;
    
    
    
    /** 数据库格式。默认为: UTF-8 */
    private String                     dataEnCode;
    
    /** 
     * 是否自动提交。默认为：ture自动提交
     * 
     *  进行了写操作的时候，你的修改不一定马上就能生效，有的时候他仅仅是缓存在RAM中，
     *  如果想让你的修改立即生效，则可以使用Environment.sync()方法来把数据同步到磁盘中去
     */
    private boolean                    autoCommit;
    
    
    
    public Berkeley()
    {
        this.dataEnCode = "UTF-8";
        this.autoCommit = true;
    }
    
    
    
    /**
     * 打开数据库环境及数据库本身
     * 
     * 写在open()之后的方法，都必须调用open()方法之后才能正常使用
     */
    public void open()
    {
        try
        {
            this.environment   = new Environment(new File(this.environmentHome) ,this.environmentConfig);
            
            this.database      = this.environment.openDatabase(null ,this.databaseName ,this.databaseConfig);
            
            this.classBerkeley = new ClassBerkeley(this ,this.databaseName + "_ClassCatalog");
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
    }
    
    
    
    /**
     * 1. 通过调用Database.close()方法来关闭数据库，但要注意，在关闭数据库前必须得先把游标先关闭
     * 
     * 2. 可以通过Environment.close()这个方法来关闭数据库环境。
     *    当你完成数据库操作后一定要关闭数据库环境
     */
    public void close()
    {
        if ( this.environment != null )
        {
            this.commit();
        }
        
        try
        {
            if ( this.database != null )
            {
                this.database.close();
            }
            
            this.database = null;
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        try
        {
            if ( this.environment != null )
            {
                this.environment.close();
            }
            
            this.environment = null;
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
    }
    
    
    
    /**
     * 获取当前环境下的数据库名称列表
     * 
     * @return
     */
    public List<String> getDatabaseNames()
    {
        return this.environment.getDatabaseNames();
    }
    
    
    
    /**
     * 数据库中的记录总数
     * 
     * @return
     */
    public long getCount()
    {
        return this.database.count();
    }
    
    
    
    /**
     * 数据库中的可序列化对象的总数
     * 
     * @return
     */
    public long getObjectCount()
    {
        return this.classBerkeley.getObjectCount();
    }
    
    
    
    /**
     * 将字符串转为Berkeley数据库实体对象
     * 
     * @param i_String
     * @return
     * @throws UnsupportedEncodingException
     */
    public DatabaseEntry toDBEntry(String i_String) throws UnsupportedEncodingException
    {
        return new DatabaseEntry(i_String.getBytes(this.dataEnCode)); 
    }
    
    
    
    /**
     * 提交数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-18
     * @version     v1.0
     *
     */
    public void commit()
    {
        this.database.sync();
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
        return this.classBerkeley.put(i_Key ,i_ObjValue);
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
        return this.classBerkeley.putNoOverwrite(i_Key ,i_ObjValue);
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
        return this.classBerkeley.putNoDupData(i_Key ,i_ObjValue);
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
    public boolean put(String i_Key ,String i_Value)
    {
        try
        {
            return this.put(this.toDBEntry(i_Key) ,this.toDBEntry(Help.NVL(i_Value)));
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
    public boolean putNoOverwrite(String i_Key ,String i_Value)
    {
        try
        {
            return this.putNoOverwrite(this.toDBEntry(i_Key) ,this.toDBEntry(Help.NVL(i_Value)));
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
    public boolean putNoDupData(String i_Key ,String i_Value)
    {
        try
        {
            return this.putNoDupData(this.toDBEntry(i_Key) ,this.toDBEntry(Help.NVL(i_Value)));
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
     * 如果数据库不支持一个key对应多个data或当前database中已经存在该key了，
     * 则使用此方法将使用新的值覆盖旧的值。
     *
     * @param i_Key
     * @param i_Value
     * @return
     */
    public boolean put(DatabaseEntry i_Key ,DatabaseEntry i_Value)
    {
        OperationStatus v_OperationStatus = this.database.put(null ,i_Key ,i_Value);
        
        if ( v_OperationStatus == OperationStatus.SUCCESS )
        {
            if ( this.autoCommit )
            {
                this.commit();
            }
            
            return true;
        }
        else
        {
            return false;
        }
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
    public boolean putNoOverwrite(DatabaseEntry i_Key ,DatabaseEntry i_Value)
    {
        OperationStatus v_OperationStatus = this.database.putNoOverwrite(null ,i_Key ,i_Value);
        
        if ( v_OperationStatus == OperationStatus.SUCCESS )
        {
            if ( this.autoCommit )
            {
                this.commit();
            }
            
            return true;
        }
        else if ( v_OperationStatus == OperationStatus.KEYEXIST )
        {
            return true;
        }
        else
        {
            return false;
        }
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
    public boolean putNoDupData(DatabaseEntry i_Key ,DatabaseEntry i_Value)
    {
        OperationStatus v_OperationStatus = this.database.putNoDupData(null ,i_Key ,i_Value);
        
        if ( v_OperationStatus == OperationStatus.SUCCESS )
        {
            if ( this.autoCommit )
            {
                this.commit();
            }
            
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
    
    /**
     * 获取记录的方法，通过key的方式来匹配，如果没有改记录则返回OperationStatus.NOTFOUND
     * 
     * @param i_Key
     * @return
     */
    public Object getObject(String i_Key)
    {
        return this.classBerkeley.getObject(i_Key);
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
        return this.classBerkeley.getObjects();
    }
    
    
    
    /**
     * 获取记录的方法，通过key的方式来匹配，如果没有改记录则返回OperationStatus.NOTFOUND
     * 
     * @param i_Key
     * @return
     */
    public String get(String i_Key)
    {
        DatabaseEntry v_Value = this.getDBEntry(i_Key);
        
        if ( v_Value != null )
        {
            try
            {
                return new String(v_Value.getData() ,this.dataEnCode);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        
        return null;
    }
    
    
    
    /**
     * 获取所有记录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-17
     * @version     v1.0
     *
     * @return
     */
    public Map<String ,String> gets()
    {
        Map<String ,String> v_Ret    = new HashMap<String ,String>();
        Cursor              v_Cursor = null;
        DatabaseEntry       v_Key    = new DatabaseEntry();  
        DatabaseEntry       v_Value  = new DatabaseEntry();
        
        try
        {
            v_Cursor = this.database.openCursor(null ,null);
            
            while ( v_Cursor.getNext(v_Key ,v_Value ,LockMode.DEFAULT) == OperationStatus.SUCCESS )
            {
                v_Ret.put(new String(v_Key.getData() ,this.dataEnCode) ,new String(v_Value.getData() ,this.dataEnCode));
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 获取记录的方法，通过key的方式来匹配，如果没有改记录则返回OperationStatus.NOTFOUND
     * 
     * @param i_Key
     * @return
     */
    public DatabaseEntry getDBEntry(String i_Key)
    {
        DatabaseEntry   v_Value           = new DatabaseEntry();
        OperationStatus v_OperationStatus = null;
        
        try
        {
            v_OperationStatus = this.database.get(null ,this.toDBEntry(i_Key) ,v_Value ,LockMode.DEFAULT);
            
            if ( v_OperationStatus != null && v_OperationStatus == OperationStatus.SUCCESS )
            {
                return v_Value;
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return null;
    }
    
    
    
    /**
     * 获取记录的方法，通过key的方式来匹配，如果没有改记录则返回OperationStatus.NOTFOUND
     * 
     * @param i_Key
     * @return
     */
    public DatabaseEntry getDBEntry(DatabaseEntry i_Key)
    {
        DatabaseEntry   v_Value           = new DatabaseEntry();
        OperationStatus v_OperationStatus = this.database.get(null ,i_Key ,v_Value ,LockMode.DEFAULT);
        
        if ( v_OperationStatus != null && v_OperationStatus == OperationStatus.SUCCESS )
        {
            return v_Value;
        }
        else
        {
            return null;
        }
    }
    
    
    
    public Map<String ,DatabaseEntry> getDBEntrys()
    {
        Map<String ,DatabaseEntry> v_Ret    = new HashMap<String ,DatabaseEntry>();
        Cursor                     v_Cursor = null;
        DatabaseEntry              v_Key    = new DatabaseEntry();  
        DatabaseEntry              v_Value  = new DatabaseEntry();
        
        try
        {
            v_Cursor = this.database.openCursor(null ,null);
            
            while ( v_Cursor.getNext(v_Key ,v_Value ,LockMode.DEFAULT) == OperationStatus.SUCCESS )
            {
                v_Ret.put(new String(v_Key.getData() ,this.dataEnCode) ,v_Value);
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 来删除记录。
     * 
     * 如果你的database支持多重记录，则当前key下的所有记录都会被删除，
     * 如果只想删除多重记录中的一条则可以使用游标来删除
     * 
     * @param i_Key
     * @return
     */
    public boolean delete(String i_Key)
    {
        OperationStatus v_OperationStatus = null;
        
        try
        {
            v_OperationStatus = this.database.delete(null ,this.toDBEntry(i_Key));
            
            if ( v_OperationStatus == OperationStatus.SUCCESS )
            {
                if ( this.autoCommit )
                {
                    this.commit();
                }
                
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return false;
    }
    
    
    
    /**
     * 删除所有记录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-17
     * @version     v1.0
     *
     */
    public void deletes()
    {
        Map<String ,String> v_Datas = this.gets();
        
        if ( !Help.isNull(v_Datas) )
        {
            for (String v_Key : v_Datas.keySet())
            {
                this.delete(v_Key);
            }
            
            this.environment.sync();
        }
    }
    
    
    
    /**
     * 清空数据库所有记录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-17
     * @version     v1.0
     *
     */
    public long truncateDB()
    {
        if ( this.getCount() > 0 )
        {
            return this.environment.truncateDatabase(null ,this.database.getDatabaseName() ,true);
        }
        else
        {
            return 0;
        }
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
        return this.classBerkeley.truncateObjectDB();
    }

    
    
    /**
     * 获取：数据库环境配置
     */
    public EnvironmentConfig getEnvironmentConfig()
    {
        return environmentConfig;
    }
    
    
    
    /**
     * 设置：数据库环境配置
     * 
     * @param environmentConfig 
     */
    public void setEnvironmentConfig(EnvironmentConfig environmentConfig)
    {
        this.environmentConfig = environmentConfig;
    }
    
    
    
    /**
     * 获取：数据库环境的路径
     */
    public String getEnvironmentHome()
    {
        return environmentHome;
    }

    
    
    /**
     * 设置：数据库环境的路径
     * 
     * @param environmentHome 
     */
    public void setEnvironmentHome(String environmentHome)
    {
        this.environmentHome = environmentHome;
    }
    

    
    /**
     * 获取：数据库环境的实例对象
     */
    public Environment getEnvironment()
    {
        return environment;
    }

    
    
    /**
     * 设置：数据库环境的实例对象
     * 
     * @param environment 
     */
    public void setEnvironment(Environment environment)
    {
        this.environment = environment;
    }
    

    
    /**
     * 获取：数据库配置
     */
    public DatabaseConfig getDatabaseConfig()
    {
        return databaseConfig;
    }
    
    
    
    /**
     * 设置：数据库配置
     * 
     * @param databaseConfig 
     */
    public void setDatabaseConfig(DatabaseConfig databaseConfig)
    {
        this.databaseConfig = databaseConfig;
    }

    
    
    /**
     * 获取：数据库名称
     */
    public String getDatabaseName()
    {
        return databaseName;
    }

    
    
    /**
     * 设置：数据库名称
     * 
     * @param databaseName 
     */
    public void setDatabaseName(String databaseName)
    {
        this.databaseName = databaseName;
    }
    

    
    /**
     * 获取：数据库的实例对象
     */
    public Database getDatabase()
    {
        return database;
    }
    

    
    /**
     * 设置：数据库的实例对象
     * 
     * @param database 
     */
    public void setDatabase(Database database)
    {
        this.database = database;
    }
    

    
    /**
     * 获取：数据库格式。默认为: UTF-8
     */
    public String getDataEnCode()
    {
        return dataEnCode;
    }
    

    
    /**
     * 设置：数据库格式。默认为: UTF-8
     * 
     * @param dataEnCode 
     */
    public void setDataEnCode(String dataEnCode)
    {
        this.dataEnCode = dataEnCode;
    }
    

    
    /**
     * 获取：是否自动提交。默认为：ture自动提交
     * 
     *  进行了写操作的时候，你的修改不一定马上就能生效，有的时候他仅仅是缓存在RAM中，
     *  如果想让你的修改立即生效，则可以使用Environment.sync()方法来把数据同步到磁盘中去
     */
    public boolean isAutoCommit()
    {
        return autoCommit;
    }
    

    
    /**
     * 设置：是否自动提交。默认为：ture自动提交
     * 
     *  进行了写操作的时候，你的修改不一定马上就能生效，有的时候他仅仅是缓存在RAM中，
     *  如果想让你的修改立即生效，则可以使用Environment.sync()方法来把数据同步到磁盘中去
     * 
     * @param autoCommit 
     */
    public void setAutoCommit(boolean autoCommit)
    {
        this.autoCommit = autoCommit;
    }



    protected void finalize()
    {
        this.close();
        this.classBerkeley.getCatalogDB().close();
    }
    
}
