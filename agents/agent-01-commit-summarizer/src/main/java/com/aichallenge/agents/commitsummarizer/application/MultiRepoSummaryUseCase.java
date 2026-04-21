package com.aichallenge.agents.commitsummarizer.application;

import com.aichallenge.agents.commitsummarizer.domain.model.Commit;
import com.aichallenge.agents.commitsummarizer.domain.model.DailyEntry;
import com.aichallenge.agents.commitsummarizer.domain.model.DistributionMode;
import com.aichallenge.agents.commitsummarizer.domain.model.MeetingEntry;
import com.aichallenge.agents.commitsummarizer.domain.model.MultiRepoSummaryResult;
import com.aichallenge.agents.commitsummarizer.domain.model.RepositoryEntry;
import com.aichallenge.agents.commitsummarizer.domain.service.CommitSummaryService;
import com.aichallenge.agents.commitsummarizer.infrastructure.GitCommitReader;
import com.aichallenge.agents.commitsummarizer.infrastructure.MeetingCsvRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MultiRepoSummaryUseCase {
    private final GitCommitReader gitCommitReader;
    private final CommitSummaryService commitSummaryService;
    private final MeetingCsvRepository meetingCsvRepository;
    private final DailyWorkSummarizer dailyWorkSummarizer;

    public MultiRepoSummaryUseCase(
        GitCommitReader gitCommitReader,
        CommitSummaryService commitSummaryService,
        MeetingCsvRepository meetingCsvRepository,
        DailyWorkSummarizer dailyWorkSummarizer
    ) {
        this.gitCommitReader = gitCommitReader;
        this.commitSummaryService = commitSummaryService;
        this.meetingCsvRepository = meetingCsvRepository;
        this.dailyWorkSummarizer = dailyWorkSummarizer;
    }

    public MultiRepoSummaryResult execute(MultiRepoSummaryRequest request) throws IOException, InterruptedException {
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
        }

        // Build list of workdays in range
        List<LocalDate> workdays = buildWorkdays(request.dateFrom(), request.dateTo());

        // Load meetings if needed
        List<MeetingEntry> meetings = request.includeMeetings() ? meetingCsvRepository.load() : List.of();

        boolean multiRepo = request.repositories().size() > 1;

        if (request.distributionMode() == DistributionMode.STRICT) {
            return buildStrict(request, workdays, meetings, multiRepo);
        } else {
            return buildDistributed(request, workdays, meetings, multiRepo);
        }
    }

    private MultiRepoSummaryResult buildStrict(
        MultiRepoSummaryRequest request,
        List<LocalDate> workdays,
        List<MeetingEntry> meetings,
        boolean multiRepo
    ) throws IOException, InterruptedException {

        // Collect commits per repo, grouped by day
        Map<LocalDate, List<DailyEntry.RepoCommitLine>> commitsByDay = new LinkedHashMap<>();
        for (LocalDate day : workdays) {
            commitsByDay.put(day, new ArrayList<>());
        }

        for (RepositoryEntry repo : request.repositories()) {
            String author = resolveAuthor(repo);
            List<Commit> commits = gitCommitReader.readCommits(
                repo.path(), author, request.dateFrom(), request.dateTo()
            );
            for (Commit commit : commits) {
                LocalDate day = commit.localDay();
                if (isWorkday(day) && commitsByDay.containsKey(day)) {
                    String message = commitSummaryService.cleanCommitMessage(commit.message());
                    commitsByDay.get(day).add(new DailyEntry.RepoCommitLine(repo.name(), message));
                }
            }
        }

        List<DailyEntry> days = buildDailyEntries(workdays, commitsByDay, meetings);
        return new MultiRepoSummaryResult(request.dateFrom(), request.dateTo(), days, multiRepo);
    }

    private MultiRepoSummaryResult buildDistributed(
        MultiRepoSummaryRequest request,
        List<LocalDate> workdays,
        List<MeetingEntry> meetings,
        boolean multiRepo
    ) throws IOException, InterruptedException {

        // Collect ALL commits across all repos in chronological order
        List<DailyEntry.RepoCommitLine> allCommits = new ArrayList<>();
        for (RepositoryEntry repo : request.repositories()) {
            String author = resolveAuthor(repo);
            List<Commit> commits = gitCommitReader.readCommits(
                repo.path(), author, request.dateFrom(), request.dateTo()
            );
            for (Commit commit : commits) {
                String message = commitSummaryService.cleanCommitMessage(commit.message());
                allCommits.add(new DailyEntry.RepoCommitLine(repo.name(), message));
            }
        }

        // Distribute commits across workdays
        Map<LocalDate, List<DailyEntry.RepoCommitLine>> commitsByDay = distributeCommits(allCommits, workdays);

        List<DailyEntry> days = buildDailyEntries(workdays, commitsByDay, meetings);
        return new MultiRepoSummaryResult(request.dateFrom(), request.dateTo(), days, multiRepo);
    }

    /**
     * Distributes commits equitably across workdays.
     * base = total / workdays (integer division)
     * remainder = total % workdays -> first `remainder` days get one extra commit
     */
    private Map<LocalDate, List<DailyEntry.RepoCommitLine>> distributeCommits(
        List<DailyEntry.RepoCommitLine> allCommits,
        List<LocalDate> workdays
    ) {
        Map<LocalDate, List<DailyEntry.RepoCommitLine>> result = new LinkedHashMap<>();
        for (LocalDate day : workdays) {
            result.put(day, new ArrayList<>());
        }

        if (allCommits.isEmpty() || workdays.isEmpty()) {
            return result;
        }

        int total = allCommits.size();
        int numDays = workdays.size();
        int base = total / numDays;
        int remainder = total % numDays;

        int index = 0;
        for (int dayIndex = 0; dayIndex < workdays.size(); dayIndex++) {
            LocalDate day = workdays.get(dayIndex);
            int count = base + (dayIndex < remainder ? 1 : 0);
            List<DailyEntry.RepoCommitLine> dayCommits = result.get(day);
            for (int i = 0; i < count && index < allCommits.size(); i++, index++) {
                dayCommits.add(allCommits.get(index));
            }
        }

        return result;
    }

    private List<DailyEntry> buildDailyEntries(
        List<LocalDate> workdays,
        Map<LocalDate, List<DailyEntry.RepoCommitLine>> commitsByDay,
        List<MeetingEntry> meetings
    ) {
        List<DailyEntry> days = new ArrayList<>();
        for (LocalDate day : workdays) {
            List<String> dayMeetings = getMeetingsForDay(meetings, day.getDayOfWeek());
            List<DailyEntry.RepoCommitLine> dayCommits = commitsByDay.getOrDefault(day, List.of());
            List<String> summaryLines = dayMeetings.isEmpty() && dayCommits.isEmpty()
                ? List.of()
                : summarizeDay(day, dayMeetings, dayCommits);
            DailyEntry entry = new DailyEntry(day, dayMeetings, dayCommits, summaryLines);
            if (entry.hasContent()) {
                days.add(entry);
            }
        }
        return days;
    }

    private List<String> summarizeDay(
        LocalDate day,
        List<String> dayMeetings,
        List<DailyEntry.RepoCommitLine> dayCommits
    ) {
        try {
            return dailyWorkSummarizer.summarize(new DailyWorkSummaryRequest(
                day,
                dayMeetings,
                dayCommits.stream()
                    .map(commitLine -> "[" + commitLine.repoName() + "] " + commitLine.commitMessage())
                    .toList(),
                true
            ));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La generación del resumen con IA fue interrumpida.", exception);
        }
    }

    private List<String> getMeetingsForDay(List<MeetingEntry> meetings, DayOfWeek dayOfWeek) {
        List<String> result = new ArrayList<>();
        for (MeetingEntry meeting : meetings) {
            if (meeting.dayOfWeek() == dayOfWeek) {
                result.add(meeting.name());
            }
        }
        return result;
    }

    private String resolveAuthor(RepositoryEntry repo) throws IOException, InterruptedException {
        String author = gitCommitReader.detectAuthor(repo.path());
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException(
                "No se pudo detectar el autor para el repo '" + repo.name() + "'. Configura git user.name."
            );
        }
        return author;
    }

    private List<LocalDate> buildWorkdays(LocalDate from, LocalDate to) {
        List<LocalDate> workdays = new ArrayList<>();
        LocalDate current = from;
        while (!current.isAfter(to)) {
            if (isWorkday(current)) {
                workdays.add(current);
            }
            current = current.plusDays(1);
        }
        return workdays;
    }

    private boolean isWorkday(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }
}
