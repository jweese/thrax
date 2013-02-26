package edu.jhu.thrax.util;

public class ArrayUtils {

  public static int compareIntArrays(int[] a, int[] b) {
    for (int i = 0; i < Math.min(a.length, b.length); ++i) {
      if (a[i] < b[i]) {
        return -1;
      } else if (a[i] > b[i]) {
        return 1;
      }
    }
    if (a.length < b.length) return -1;
    if (a.length > b.length) return 1;
    return 0;
  }

}
