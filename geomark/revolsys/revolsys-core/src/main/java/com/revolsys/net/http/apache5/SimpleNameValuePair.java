
package com.revolsys.net.http.apache5;

import org.apache.hc.core5.http.NameValuePair;

import com.revolsys.util.Strings;

public class SimpleNameValuePair implements NameValuePair {

  private String name;

  private String value;

  public SimpleNameValuePair(final String name, final String value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (object instanceof NameValuePair) {
      final SimpleNameValuePair other = (SimpleNameValuePair)object;
      return this.name.equals(other.name) && Strings.equals(this.value, other.value);
    }
    return false;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getValue() {
    return this.value;
  }

  @Override
  public int hashCode() {
    int hash = 17;
    hash = hash * 17 + this.name.hashCode();
    if (this.value != null) {
      hash = hash * 17 + this.value.hashCode();
    }
    return hash;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder();
    buffer.append(this.name);
    buffer.append("=");
    buffer.append(this.value);
    return buffer.toString();
  }

}
