package com.aichallenge.agents.gitdiffrfc.domain;

public interface RfcWriter {

    RfcDocument write(ChangeSet changeSet);
}
