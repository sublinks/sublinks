package com.sublinks.sublinksapi.utils;

import jakarta.persistence.TypedQuery;
import org.springframework.lang.Nullable;

public class PaginationUtils {

  /**
   * Calculates the offset for pagination query.
   *
   * @param page The page number.
   * @param size The number of items per page.
   * @return The starting index for the given page.
   */
  public static int getOffset(int page, int size) {

    return (page - 1) * size;
  }

  /**
   * Applies pagination to a {@link TypedQuery}.
   *
   * @param <T>   The type of the query result.
   * @param query The TypedQuery to which pagination will be applied.
   * @param page  The page number to retrieve.
   * @param size  The number of records per page.
   */
  public static <T> void applyPagination(TypedQuery<T> query, @Nullable Integer page, Integer size)
  {

    if (page != null) {
      query.setFirstResult(getOffset(page, size));
    }
    query.setMaxResults(Math.abs(size));
  }

  public static <T extends Integer> int Clamp(T value, T min, T max) {

    return Math.min(Math.max(value, min), max);
  }

  public static <T extends Integer> int getPage(T value) {

    return Clamp(value, 1, Integer.MAX_VALUE);
  }

  public static <T extends Integer> int getPerPage(T value, T min, T max) {

    return Clamp(value, min, max);
  }

  public static <T extends Integer> int getPerPage(T value, T max) {

    return getPerPage(value, 1, max);
  }

  public static <T extends Integer> int getPerPage(T value) {

    return getPerPage(value, 1, 20);
  }

}
