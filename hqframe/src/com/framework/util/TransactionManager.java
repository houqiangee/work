package com.framework.util;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;

public class TransactionManager
{
  public static Transaction getTransaction()
  {
    return getTransaction("dataSource");
  }

  public static Transaction getTransaction(String dbName){
    if ((dbName == null) || (dbName.equals(""))) {
      dbName = "dataSource";
    }
    DataSourceTransactionManager ptm = DatabaseSessionUtil.getCurrentTM(dbName);
    return Transaction.getTransaction(ptm, dbName);
  }
}