package net.roaringmind.multisleep.gui;

public enum ClickTypes {
  YES, NO, PHANTOMYES, PHANTOMNO, PERMAYES, PERMANO;

  public int toInt() {
    switch (this) {
      case YES:
        return 0;
      case NO:
        return 1;
      case PHANTOMYES:
        return 2;
      case PHANTOMNO:
        return 3;
      case PERMAYES:
        return 4;
      case PERMANO:
        return 5;
      default:
        return -1;
    }
  }

  public static ClickTypes fromInt(int n) {
    if (n == 0) {
      return YES;
    }
    if (n == 1) {
      return NO;
    }
    if (n == 2) {
      return PHANTOMYES;
    }
    if (n == 3) {
      return PHANTOMNO;
    }
    if (n == 4) {
      return PERMAYES;
    }
    if (n == 5) {
      return PERMANO;
    }
    return null;
  }
}
