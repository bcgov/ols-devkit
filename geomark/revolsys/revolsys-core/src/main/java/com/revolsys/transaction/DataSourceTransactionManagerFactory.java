package com.revolsys.transaction;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

public class DataSourceTransactionManagerFactory
  implements FactoryBean<DataSourceTransactionManager> {

  private DataSource dataSource;

  public DataSource getDataSource() {
    return this.dataSource;
  }

  @Override
  public DataSourceTransactionManager getObject() throws Exception {
    return new DataSourceTransactionManager(this.dataSource);
  }

  @Override
  public Class<?> getObjectType() {
    return DataSourceTransactionManager.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }
}
