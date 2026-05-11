package com.aichallenge.agents.gitdiffrfc.application;

import com.aichallenge.agents.gitdiffrfc.domain.ChangeSet;
import com.aichallenge.agents.gitdiffrfc.domain.GitDiffReader;
import com.aichallenge.agents.gitdiffrfc.domain.RfcWriter;
import com.aichallenge.agents.gitdiffrfc.infrastructure.ai.BedrockRfcWriter;
import com.aichallenge.agents.gitdiffrfc.infrastructure.output.MarkdownRfcRenderer;
import org.springframework.stereotype.Service;

@Service
public class GitDiffRfcGenerator {

    private final GitDiffReader gitDiffReader;
    private final RfcWriter localRfcWriter;
    private final BedrockRfcWriter bedrockRfcWriter;
    private final MarkdownRfcRenderer renderer;

    public GitDiffRfcGenerator(
            GitDiffReader gitDiffReader,
            RfcWriter localRfcWriter,
            BedrockRfcWriter bedrockRfcWriter,
            MarkdownRfcRenderer renderer
    ) {
        this.gitDiffReader = gitDiffReader;
        this.localRfcWriter = localRfcWriter;
        this.bedrockRfcWriter = bedrockRfcWriter;
        this.renderer = renderer;
    }

    public String generate(GenerateRfcRequest request) {
        ChangeSet changeSet = gitDiffReader.read(request);
        RfcWriter writer = request.ai() ? bedrockRfcWriter : localRfcWriter;
        return renderer.render(writer.write(changeSet));
    }
}
