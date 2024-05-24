package com.sublinks.sublinksapi.person.models;

import com.sublinks.sublinksapi.comment.dto.Comment;
import com.sublinks.sublinksapi.community.dto.Community;
import com.sublinks.sublinksapi.person.dto.Person;
import com.sublinks.sublinksapi.person.dto.PersonAggregate;
import com.sublinks.sublinksapi.post.dto.Post;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PersonContext {

  private Person person;
  private Collection<Post> posts;
  private Collection<Comment> comments;
  private PersonAggregate personAggregate;
  private Collection<Integer> discussLanguages;
  private Collection<Community> moderates;
  private Collection<Community> follows;
  private Collection<Community> communityBlocks;
  private Collection<Person> personBlocks;
}
