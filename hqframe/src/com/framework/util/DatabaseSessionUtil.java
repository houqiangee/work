package com.framework.util;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.alibaba.druid.pool.DruidDataSource;

public class DatabaseSessionUtil
{
  public static final int DBTYPE_ORACLE = 0;
  public static final int DBTYPE_POSTGRESQL = 1;
  public static final String DEFAULT_DATASOURCE_NAME = "dataSource";
  private static HashMap<String, DataSource> dsMap = new HashMap();

  private static HashMap<String, DataSourceTransactionManager> tmMap = new HashMap();

  private static ApplicationContext ctx = null;

  public static JdbcTemplate getCurrentSession(){
    return getCurrentSession("dataSource");
  }

  public static String getJdbcUrl() {
    DataSource ds = (DataSource)dsMap.get("dataSource");
    if (ds instanceof DruidDataSource) {
      DruidDataSource druidDs = (DruidDataSource)ds;
      return druidDs.getRawJdbcUrl();
    }
    return null;
  }

  public static String getPassword()
  {
    return getPassword("dataSource"); }

  public static String getPassword(String dbName) {
    DataSource ds = (DataSource)dsMap.get(dbName);
    if (ds instanceof DruidDataSource) {
      DruidDataSource druidDs = (DruidDataSource)ds;
      return druidDs.getPassword();
    }
    return null;
  }

  public static String getUsername()
  {
    return getUsername("dataSource"); }

  public static String getUsername(String dbName) {
    DataSource ds = (DataSource)dsMap.get(dbName);
    if (ds instanceof DruidDataSource) {
      DruidDataSource druidDs = (DruidDataSource)ds;
      return druidDs.getUsername();
    }
    return null;
  }

  public static JdbcTemplate getCurrentSession(String dbName) 
  {
    if ((dbName == null) || (dbName.equals(""))) {
      dbName = "dataSource";
    }
    DataSource dataSource = null;
    if (!(dsMap.containsKey(dbName))) {
      dataSource = getDataSource(dbName);
      dsMap.put(dbName, dataSource);
    } else {
      dataSource = (DataSource)dsMap.get(dbName);
    }
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    return jdbcTemplate;
  }

  public static DataSource getDataSource() {
	    return getDataSource("dataSource");
	  }

  public static DataSource getDataSource(String dbName) {
    ApplicationContext ctx = getCtx();
    Object bean = ctx.getBean(dbName);
    if (bean instanceof DataSource) {
      return ((DataSource)bean);
    }
    throw new RuntimeException("暂不支持【" + bean.getClass().getName() + "】类型数据源配置，请检查!");
  }
  
  public static int getDBType()
    
  {
    return getDBType("dataSource");
  }

  public static int getDBType(String dbName) 
  {
    DataSource ds = null;
    if (!(dsMap.containsKey(dbName))) {
      ds = (DataSource)getCtx().getBean(dbName, DataSource.class);
      dsMap.put(dbName, ds);
    } else {
      ds = (DataSource)dsMap.get(dbName);
    }

    if (ds instanceof DruidDataSource) {
      DruidDataSource druidDs = (DruidDataSource)ds;
      String jdbcUrl = druidDs.getRawJdbcUrl();

      Pattern regx = Pattern.compile("(?<=jdbc:).*?(?=:)");
      Matcher match = regx.matcher(jdbcUrl);
      if (!(match.find())) {
      }
      String dbType = match.group();
      if ("POSTGRESQL".equalsIgnoreCase(dbType))
        return 1;
      if ("ORACLE".equalsIgnoreCase(dbType))
        return 0;
      if ("INSPUR".equalsIgnoreCase(dbType)) {
        return 0;
      }
    }
    return 1;
  }

  public static DataSourceTransactionManager getCurrentTM() 
  {
    return getCurrentTM("dataSource");
  }

  public static DataSourceTransactionManager getCurrentTM(String dbName)
    
  {
    DataSourceTransactionManager txManager = null;
    if (!(tmMap.containsKey(dbName))) {
      DruidDataSource dataSource = (DruidDataSource)getCtx().getBean(dbName, DruidDataSource.class);
      txManager = new DataSourceTransactionManager(dataSource);
      tmMap.put(dbName, txManager);
    } else {
      txManager = (DataSourceTransactionManager)tmMap.get(dbName);
    }
    return txManager;
  }

  public static void initTransactionManager() {
    ApplicationContext ctx = getCtx();

    DataSource dataSource = (DataSource)ctx.getBean("dataSource", DataSource.class);
    dsMap.put("dataSource", dataSource);

    DataSourceTransactionManager txManager = new DataSourceTransactionManager(dataSource);
    tmMap.put("dataSource", txManager);
  }

	public static ApplicationContext getCtx() {
		if (ctx == null) {
			ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		}
		return ctx;
	}

  public static void setCtx(ApplicationContext application) {
    ctx = application;
  }
}