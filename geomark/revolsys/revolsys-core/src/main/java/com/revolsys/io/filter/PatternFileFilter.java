package com.revolsys.io.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

import com.revolsys.io.FileUtil;

/**
 * The PatternFileNameFilter is a {@link FileFilter} that only returns files if they
 * match the regular expression.
 *
 * @author Paul Austin
 */
public class PatternFileFilter implements FileFilter {
  private boolean ignoreCase;

  /** The regular expression pattern to match file names. */
  private final Pattern pattern;

  /**
   * Construct a new PatternFileNameFilter.
   *
   * @param regex The regular expression.
   */
  public PatternFileFilter(final String regex) {
    this.pattern = Pattern.compile(regex);
  }

  public PatternFileFilter(String regex, final boolean ignoreCase) {
    if (ignoreCase) {
      regex = regex.toLowerCase();
    }
    this.pattern = Pattern.compile(regex);
    this.ignoreCase = ignoreCase;
  }

  /**
   * Check to see if the file should be included in the list of matched files
   *
   * @param file The file.
   * @return True if the file matched, false otherwise.
   */
  @Override
  public boolean accept(final File file) {
    String fileName = FileUtil.getFileName(file);
    if (this.ignoreCase) {
      fileName = fileName.toUpperCase();
    }
    return this.pattern.matcher(fileName).matches();
  }
}
