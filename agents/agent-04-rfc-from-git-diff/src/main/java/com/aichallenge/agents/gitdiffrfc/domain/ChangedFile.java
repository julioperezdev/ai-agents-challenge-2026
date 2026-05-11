package com.aichallenge.agents.gitdiffrfc.domain;

import java.util.Optional;

public record ChangedFile(ChangeType type, String path, Optional<String> previousPath) {
}
