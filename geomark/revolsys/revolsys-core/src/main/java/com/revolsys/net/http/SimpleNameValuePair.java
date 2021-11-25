
package com.revolsys.net.http;

import org.apache.http.NameValuePair;
import org.apache.http.util.Args;
import org.apache.http.util.LangUtils;

import com.revolsys.util.Strings;

public class SimpleNameValuePair implements NameValuePair {

  private String name;

  private String value;

  public SimpleNameValuePair(final String name, final String value) {
    this.name = Args.notNull(name, "Name");
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
    int hash = LangUtils.HASH_SEED;
    hash = LangUtils.hashCode(hash, this.name);
    hash = LangUtils.hashCode(hash, this.value);
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
