package com.revolsys.connection;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.beans.PropertyChangeSupportProxy;

public class AbstractConnectionRegistryManager<R extends ConnectionRegistry<C>, C extends Connection>
  implements ConnectionRegistryManager<R>, PropertyChangeSupportProxy {

  private final String name;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private final List<R> registries = new ArrayList<>();

  public AbstractConnectionRegistryManager(final String name) {
    this.name = name;
  }

  @Override
  public void addConnectionRegistry(final R registry) {
    if (registry != null) {
      int index = -1;
      synchronized (this.registries) {
        if (!this.registries.contains(registry)) {
          index = this.registries.size();
          this.registries.add(registry);
          registry.setConnectionManager(this);
        }
      }
      if (index != -1) {
        index = getVisibleConnectionRegistries().indexOf(registry);
        if (index != -1) {
          this.propertyChangeSupport.fireIndexedPropertyChange("registries", index, null, registry);
          this.propertyChangeSupport.fireIndexedPropertyChange("children", index, null, registry);
        }
      }
    }
  }

  protected R findConnectionRegistry(final String name) {
    for (final R registry : this.registries) {
      if (registry.getName().equals(name)) {
        return registry;
      }
    }
    return null;
  }

  @Override
  public List<R> getConnectionRegistries() {
    return new ArrayList<>(this.registries);
  }

  @Override
  public R getConnectionRegistry(final String name) {
    final R connectionRegistry = findConnectionRegistry(name);
    if (connectionRegistry == null) {
      return this.registries.get(0);
    }
    return connectionRegistry;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public R getUserConnectionRegistry() {
    return getConnectionRegistry("User");
  }

  @Override
  public List<R> getVisibleConnectionRegistries() {
    final List<R> registries = new ArrayList<>();
    for (final R registry : this.registries) {
      if (registry != null && registry.isVisible()) {
        registries.add(registry);
      }
    }
    return registries;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    this.propertyChangeSupport.firePropertyChange(event);
  }

  @Override
  public void removeConnectionRegistry(final R registry) {
    if (registry != null) {
      int index;
      synchronized (this.registries) {
        index = this.registries.indexOf(registry);
        if (index != -1) {
          this.registries.remove(registry);
          registry.setConnectionManager(null);
          registry.getPropertyChangeSupport().removePropertyChangeListener(this);
        }
      }
      if (index != -1) {
        this.propertyChangeSupport.fireIndexedPropertyChange("registries", index, registry, null);
      }
    }
  }

  public void removeConnectionRegistry(final String name) {
    final R connectionRegistry = findConnectionRegistry(name);
    removeConnectionRegistry(connectionRegistry);
  }

  @Override
  public String toString() {
    return this.name;
  }
}
