package com.dscid.filesystemanalyzer.utils;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<int[]> {
  Map<int[], Double> base;

  public ValueComparator(Map<int[], Double> base) {
    this.base = base;
  }

  // Note: this comparator imposes orderings that are inconsistent with
  // equals.
  public int compare(int[] a, int[] b) {
    if (base.get(a) >= base.get(b)) {
      return -1;
    } else {
      return 1;
    } // returning 0 would merge keys
  }
}
