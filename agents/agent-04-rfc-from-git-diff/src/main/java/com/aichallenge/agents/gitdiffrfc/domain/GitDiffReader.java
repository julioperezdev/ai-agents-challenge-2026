package com.aichallenge.agents.gitdiffrfc.domain;

import com.aichallenge.agents.gitdiffrfc.application.GenerateRfcRequest;

public interface GitDiffReader {

    ChangeSet read(GenerateRfcRequest request);
}
