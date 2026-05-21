package com.aichallenge.agents.voicecompanion.domain;

import java.util.List;

public record OlderAdultProfile(User user, List<FamilyContact> familyContacts, List<Reminder> reminders) {
  public record User(String name, String preferredName, String language) {}

  public record FamilyContact(String name, String relation, String phone) {}
}
