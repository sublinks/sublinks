package com.sublinks.sublinksapi.api.sublinks.v1.utils;

import org.springframework.lang.Nullable;

public class PaginationControllerUtils {

  public static int getAbsoluteMinNumber(@Nullable Integer number,
      @Nullable Integer defaultNumber)
  {

    if (number == null && defaultNumber == null) {
      return 1;
    }

    if (defaultNumber == null) {
      defaultNumber = number;
    }

    if (number == null) {
      return Math.abs(defaultNumber);
    }

    return Math.abs(Math.min(number, defaultNumber));
  }

}
