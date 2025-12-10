package com.thentrees.gymhealthtech.util;

/**
 * Utility class for formatting file sizes in human-readable format.
 */
public final class FileSizeFormatter {

  private FileSizeFormatter() {
    // Utility class - prevent instantiation
  }

  /**
   * Formats a file size in bytes to a human-readable string.
   * Examples: 1024 bytes → "1.0 KB", 1048576 bytes → "1.0 MB"
   *
   * @param bytes the file size in bytes
   * @return formatted file size string
   */
  public static String format(long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    String pre = "KMGTPE".charAt(exp - 1) + "";
    return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
  }
}

