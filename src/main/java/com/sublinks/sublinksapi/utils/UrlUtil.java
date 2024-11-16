package com.sublinks.sublinksapi.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UrlUtil {

  /**
   * Normalizes a provided URL by removing tracking parameters. This method processes the URL,
   * removes any known tracking parameters from its query string, and reconstructs the URL. It
   * retains the original URL's protocol, authority, path, and fragment. If the URL is malformed or
   * encounters a URI syntax issue, the original URL is returned.
   *
   * @param providedUrl The URL string to be normalized.
   * @return The normalized URL string without tracking parameters. Returns the original URL if any
   * exceptions are encountered.
   */
  public String normalizeUrl(final String providedUrl) {

    try {
      final URL url = new URL(providedUrl);
      final String normalizedQueryString = removeTrackingParameters(url.getQuery());
      // @Todo verify protocol is http, https, magnet
      final URI uri = new URI(url.getProtocol(), url.getAuthority(), url.getPath(),
          normalizedQueryString, url.getRef());
      return uri.toString();
    } catch (URISyntaxException | MalformedURLException e) {
      return providedUrl;
    }
  }

  private String removeTrackingParameters(final String queryString) {

    if (queryString == null || queryString.isEmpty()) {
      return null;
    }
    Pattern pattern = Pattern.compile("(\\w+)=?([^&]+)?");
    Matcher matcher = pattern.matcher(queryString);
    Collection<String> spyList = List.of("utm_source", "utm_medium", "utm_campaign", "utm_term",
        "utm_content", "gclid", "gclsrc", "dclid", "fbclid");
    HashMap<String, String> parameters = new HashMap<>();
    while ((matcher.find())) {
      if (!spyList.contains(matcher.group(1))) {
        parameters.put(matcher.group(1), matcher.group(2));
      }
    }
    if (parameters.isEmpty()) {
      return null;
    }
    return parameters.entrySet()
        .stream()
        .map(e -> e.getKey() + "=" + e.getValue())
        .collect(Collectors.joining("&"));
  }

  /**
   * Validates the protocol of a provided URL. This method checks if the URL's protocol is one of
   * the accepted types: HTTP, HTTPS, or Magnet. If the protocol is not one of these or the URL is
   * malformed, a RuntimeException is thrown.
   *
   * @param providedUrl The URL string to be validated.
   * @throws RuntimeException If the URL's protocol is not one of the accepted types or if the URL
   *                          is malformed.
   */
  public void checkUrlProtocol(String providedUrl) {

    try {
      final URL url = new URL(providedUrl);
      if (!List.of("http", "https", "magnet")
          .contains(url.getProtocol())) {
        throw new RuntimeException("Invalid URL Scheme");
      }
    } catch (Exception e) {
      throw new RuntimeException("Invalid URL Scheme");
    }
  }

  public String cleanUrlProtocol(String providedUrl) {

    try {
      final URL url = new URL(providedUrl);

      final StringBuilder sb = new StringBuilder();

      sb.append(url.getHost());
      if (url.getPort() != -1) {
        sb.append(":")
            .append(url.getPort());
      }

      return sb.toString();
    } catch (MalformedURLException e) {
      return providedUrl;
    }
  }
}
