package com.revolsys.jdbc.io;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class JdbcDataSourceFactoryBean extends AbstractFactoryBean<DataSource>
  implements ApplicationContextAware {

  private Map<String, Object> config = new HashMap<>();

  private JdbcDatabaseFactory databaseFactory;

  private String password;

  private String url;

  private String username;

  @Override
  protected DataSource createInstance() throws Exception {
    final Map<String, Object> config = new HashMap<>(this.config);
    config.put("url", this.url);
    config.put("user", this.username);
    config.put("password", this.password);
    this.databaseFactory = JdbcDatabaseFactory.databaseFactory(config);
    final DataSource dataSource = this.databaseFactory.newDataSource(config);
    return dataSource;
  }

  @Override
  protected void destroyInstance(final DataSource dataSource) throws Exception {
    try {
      JdbcDatabaseFactory.closeDataSource(dataSource);
    } finally {
      this.config = null;
      this.databaseFactory = null;
      this.password = null;
      this.url = null;
      this.username = null;
    }
  }

  public Map<String, Object> getConfig() {
    return this.config;
  }

  @Override
  public Class<?> getObjectType() {
    return DataSource.class;
  }

  public String getPassword() {
    return this.password;
  }

  public String getUrl() {
    return this.url;
  }

  public String getUsername() {
    return this.username;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext)
    throws BeansException {
  }

  public void setConfig(final Map<String, Object> config) {
    this.config = config;
  }

  @Required
  public void setPassword(final String password) {
    this.password = password;
  }

  @Required
  public void setUrl(final String url) {
    this.url = url;
  }

  @Required
  public void setUsername(final String username) {
    this.username = username;
  }
}
