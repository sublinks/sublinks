package com.sublinks.sublinksapi.api.sublinks.v1.utils;

import java.util.List;

public class ActorIdUtils {

  public static boolean isActorIdValid(String actorId) {

    return actorId.contains("@");
  }

  public static List<String> splitActorId(String actorId) {

    if (!actorId.contains("@")) {
      return null;
    }
    return List.of(actorId.split("@"));
  }


  public static String getActorId(String actorId) {

    List<String> parts = splitActorId(actorId);
    if (parts == null) {
      return null;
    }

    return parts.get(0);
  }

  public static String getActorDomain(String actorId) {

    List<String> parts = splitActorId(actorId);
    if (parts == null) {
      return null;
    }

    return parts.get(1);
  }

}
