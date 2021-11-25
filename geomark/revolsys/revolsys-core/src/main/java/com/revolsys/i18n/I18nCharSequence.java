package com.revolsys.i18n;

public class I18nCharSequence implements CharSequence {
  private final I18n i18n;

  private final String key;

  public I18nCharSequence(final I18n i18n, final String key) {
    this.i18n = i18n;
    this.key = key;
  }

  /**
   * Get the character at the specified index.
   *
   * @param index The index.
   * @return The character.
   */
  @Override
  public char charAt(final int index) {
    return toString().charAt(index);
  }

  public I18n getI18n() {
    return this.i18n;

  }

  /**
   * Get the length of the char sequence.
   *
   * @return The length;
   */
  @Override
  public int length() {
    return toString().length();
  }

  @Override
  public CharSequence subSequence(final int start, final int end) {
    return toString().subSequence(start, end);
  }

  /**
   * Get the
   *
   * @return
   */
  @Override
  public String toString() {
    return this.i18n.getString(this.key);
  }

}
