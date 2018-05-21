package com.framework.util;

import java.util.HashMap;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class Transaction
{
  static ThreadLocal<HashMap<String, Transaction>> transLocalMap = new ThreadLocal();
  public static final int PROPAGATION_MANDATORY = 2;
  public static final int PROPAGATION_REQUIRED = 0;
  public static final int PROPAGATION_REQUIRES_NEW = 3;
  private PlatformTransactionManager ptm;
  private TransactionStatus status = null;

  private boolean transactionStarted = false;

  public boolean isUnderTransaction()
  {
    return ((this.status != null) && (!(this.status.isCompleted())));
  }

  public boolean isRollbackOnly() {
    if (this.status == null) {
      return false;
    }
    return this.status.isRollbackOnly();
  }

  protected Transaction()
  {
  }

  protected Transaction(PlatformTransactionManager pPtm) {
    this.ptm = pPtm;
  }

  public static Transaction getTransaction(PlatformTransactionManager pPtm, String dbName)
  {
    HashMap transMap = (HashMap)transLocalMap.get();
    if (null == transMap) {
      transMap = new HashMap();
    }
    Transaction transaction = (Transaction)transMap.get(dbName);
    if (transaction == null) {
      transaction = new Transaction(pPtm);
      transMap.put(dbName, transaction);
    } else if (transaction.isRollbackOnly() == true) {
      transaction.rollback();
      transaction = new Transaction(pPtm);
      transMap.put(dbName, transaction);
    }
    transLocalMap.set(transMap);
    return transaction;
  }

  public PlatformTransactionManager getPtm() {
    return this.ptm;
  }

  public int begin() {
    return begin(0);
  }

  public int begin(int propagation)
  {
    if (this.transactionStarted) {
      return 0;
    }
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setIsolationLevel(2);
    def.setPropagationBehavior(propagation);
    this.status = this.ptm.getTransaction(def);
    this.transactionStarted = true;
    return 1;
  }

  public void commit()
  {
    commitWithoutStart();
    begin();
  }

  public void commitWithoutStart(){
    if (null == this.status)
      System.out.println("当前commit操作，未正常开启事务，请联系开发人员处理。");
    if (this.status.isCompleted()) {
      return;
    }
    if (this.status.isRollbackOnly()) {
      this.ptm.rollback(this.status);
      this.transactionStarted = false;
      System.out.println("当前事务已经被锁定为rollback，请联系开发人员处理。");
    }
    this.ptm.commit(this.status);
    this.transactionStarted = false;
  }

  public void rollback() 
  {
    rollbackWithoutStart();
    begin();
  }

  public void rollbackWithoutStart()  {
    if (null == this.status)
      System.out.println("当前rollback操作，未正常开启事务，请联系开发人员处理。");
    if (this.status.isCompleted()) {
      return;
    }
    this.ptm.rollback(this.status);
    this.transactionStarted = false;
  }
}