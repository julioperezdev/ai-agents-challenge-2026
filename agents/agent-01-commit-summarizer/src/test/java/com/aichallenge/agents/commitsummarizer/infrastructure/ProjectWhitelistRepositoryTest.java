package com.aichallenge.agents.commitsummarizer.infrastructure;

import com.aichallenge.agents.commitsummarizer.domain.model.ProjectWhitelistEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectWhitelistRepositoryTest {
    private final ProjectWhitelistRepository repository = new ProjectWhitelistRepository();

    @Test
    void loadsProjectsFromWhitelist(@TempDir Path tempDir) throws Exception {
        Path whitelist = tempDir.resolve("project-whitelist.csv");
        Files.writeString(
            whitelist,
            """
            # alias,path,enabled,notes
            demo-one,/tmp/demo-one,true,Proyecto principal
            demo-two,/tmp/demo-two,false,Proyecto pausado
            """,
            StandardCharsets.UTF_8
        );

        List<ProjectWhitelistEntry> entries = repository.load(whitelist);

        assertEquals(2, entries.size());
        assertEquals("demo-one", entries.get(0).alias());
        assertEquals(true, entries.get(0).enabled());
    }

    @Test
    void findsOnlyEnabledAlias(@TempDir Path tempDir) throws Exception {
        Path whitelist = tempDir.resolve("project-whitelist.csv");
        Files.writeString(
            whitelist,
            """
            active-project,/tmp/active-project,true,Activo
            inactive-project,/tmp/inactive-project,false,Inactivo
            """,
            StandardCharsets.UTF_8
        );

        ProjectWhitelistEntry entry = repository.findEnabledByAlias(whitelist, "active-project");

        assertEquals("active-project", entry.alias());
    }
}
