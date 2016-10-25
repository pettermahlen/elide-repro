package com.spotify.repro.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * TODO: document!
 */
@Entity
public class Team {
  @Id
  public String id;

  public String teamType;
}
