package org.hy.common.berkeley.junit;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.Timing;
import org.hy.common.berkeley.Berkeley;
import org.hy.common.xml.XJSON;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.EnvironmentConfig;





/**
 * 测试单元：嵌入式(文件)数据库Berkeley
 *
 * @author      ZhengWei(HY)
 * @createDate  2015-01-09
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_Berkeley
{
    private static boolean $IsInit = false;
    
    private static String  $BigData = "";
    
    
    
    public JU_Berkeley() throws Exception
    {
        if ( !$IsInit )
        {
            $IsInit = true;
            XJava.parserAnnotation("org.hy.common.berkeley");
            
            $BigData = StringHelp.rpad("" ,10000/50 ,"ABCDEFG HIGKLMN OPQRST UVWXYZ 1234567890!@#$%^&*()");
        }
    }
    
    
    
    public static void main_Stop(String [] args) throws Exception
    {
        EnvironmentConfig v_EnvConfig = new EnvironmentConfig();
        DatabaseConfig    v_DBConfig  = new DatabaseConfig();
        Berkeley          v_Berkeley  = new Berkeley();
        
        v_EnvConfig.setAllowCreate(true);
        v_DBConfig.setAllowCreate(true);
        
        v_Berkeley.setEnvironmentConfig(v_EnvConfig);
        v_Berkeley.setDatabaseConfig(   v_DBConfig);
        v_Berkeley.setEnvironmentHome(  "/Users/hy/WSS/WorkSpace_SearchDesktop/hy.common.berkeley/db");
        v_Berkeley.setDatabaseName(     "HY");
        
        v_Berkeley.open();
        
        
        String v_Key = "ZhengWei(HY)";
        
        v_Berkeley.put(v_Key ,Date.getNowTime().getFullMilli());
        System.out.println(v_Berkeley.get(v_Key));
    }
    
    
    
    /**
     * 简单的读写测试
     */
    @Test
    public void test_001() throws Exception
    {
        XJava.parserAnnotation("org.hy.common.berkeley");
        Berkeley v_Berkeley = (Berkeley)XJava.getObject("Berkeley");
        
        
        // 读写普通字符串信息
        String v_Key = "ZhengWei(HY)" + Date.getNowTime().getFullMilli_ID();
        
        v_Berkeley.put(v_Key ,Date.getNowTime().getFullMilli());
        System.out.println(v_Berkeley.get(v_Key));
        
        
        
        // 读写对象信息
        BeanInfo  v_Bean = new BeanInfo();
        ChildBean v_Obj  = new ChildBean();
        ElseInfo  v_Else = new ElseInfo();
        
        v_Bean.setName("ZhengWei(HY)");
        v_Bean.setCreateTime(new Date());
        
        v_Obj.setBean( v_Bean);
        v_Obj.setCount(10);
        
        Thread.sleep(1000);
        
        v_Else.setCreateTime(new Date());
        
        v_Berkeley.put(v_Key + "B" ,v_Bean);
        v_Berkeley.put(v_Key + "C" ,v_Obj);
        v_Berkeley.put(v_Key + "E" ,v_Else);
        
        v_Bean = (BeanInfo) v_Berkeley.getObject(v_Key + "B");
        v_Obj  = (ChildBean)v_Berkeley.getObject(v_Key + "C");
        v_Else = (ElseInfo) v_Berkeley.getObject(v_Key + "E");
        
        System.out.println(v_Bean.getCreateTime().getFullMilli());
        System.out.println(v_Obj.getBean().getCreateTime().getFullMilli());
        System.out.println(v_Else.getCreateTime().getFullMilli());
        
        System.out.println("       Count = " + v_Berkeley.getCount());
        System.out.println("Object Count = " + v_Berkeley.getObjectCount());
        
        
        Help.print(v_Berkeley.gets());
    }
    
    
    
    /**
     * 写数据的压力测试
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-17
     * @version     v1.0
     *
     * @throws Exception
     */
    @Test
    public void test_002() throws Exception
    {
        Berkeley v_Berkeley  = (Berkeley)XJava.getObject("Berkeley");
        XJSON    v_XJSON     = new XJSON();
        Date     v_BeginTime = new Date();
        Date     v_EndTime   = null;
        
        System.out.println("-- 清空老数据共 " + v_Berkeley.getCount() + "条。");
        v_Berkeley.deletes();
        System.out.println("-- 数据库中还有 " + v_Berkeley.getCount() + "条。");
        
        System.out.println("-- " + v_BeginTime.getFullMilli());
        Timing v_Timing = new Timing();
        
        for (int i=0; i<1000; i++)
        {
            BeanInfo v_Bean = new BeanInfo();
            
            v_Bean.setName("ZhengWei(HY)");
            v_Bean.setCreateTime(new Date());
            
            v_Berkeley.put(StringHelp.getUUID() ,v_XJSON.parser(v_Bean).toJSONString());
            v_Timing.timing();
        }
        
        v_EndTime = new Date();
        
        Help.print(v_Timing.getTimings());
        
        System.out.println("-- " + v_EndTime.getFullMilli());
        System.out.println("-- " + (v_EndTime.getTime() - v_BeginTime.getTime()));
        System.out.println("       Count = " + v_Berkeley.getCount());
        System.out.println("Object Count = " + v_Berkeley.getObjectCount());
        System.out.println();
        
        
        // 当你完成数据库操作后一定要关闭数据库环境
        // v_Berkeley.close();
    }
    
    
    
    public void test_003() throws Exception
    {
        for (int i=0; i<=10; i++)
        {
            this.test_002();
        }
        
        
        Berkeley v_Berkeley = (Berkeley)XJava.getObject("Berkeley");
        // 当你完成数据库操作后一定要关闭数据库环境
        v_Berkeley.close();
    }
    
    
    
    /**
     * 大数据写入测试
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-21
     * @version     v1.0
     *
     * @throws Exception
     */
    @Test
    public void test_004_BigData() throws Exception
    {
        Berkeley v_Berkeley = (Berkeley)XJava.getObject("Berkeley");
        
        int  v_DataSize  = 1000;
        Date v_BeginTime = new Date();
        System.out.println("-- " + v_BeginTime.getFullMilli() + "  开始.. ...");
        
        for (int i=0; i<=v_DataSize; i++)
        {
            String v_Key = StringHelp.getUUID();
            
            v_Berkeley.put(v_Key ,$BigData + v_Key);
            v_Berkeley.delete(v_Key);
        }
        
        Date v_EndTime = new Date();
        long v_TimeLen = v_EndTime.getTime() - v_BeginTime.getTime();
        System.out.println("-- " + v_EndTime.getFullMilli() + "  完成... ...");
        System.out.println("-- 写入用时：" + Date.toTimeLen(v_TimeLen));
        System.out.println("-- 平均用时：" + Help.division(v_TimeLen ,v_DataSize));
        
        // 当你完成数据库操作后一定要关闭数据库环境
        v_Berkeley.close();
    }
    
    
    
    public static void main(String [] args) throws Exception
    {
        JU_Berkeley v_JU_Berkeley = new JU_Berkeley();
        v_JU_Berkeley.test_004_BigData();
    }
}
