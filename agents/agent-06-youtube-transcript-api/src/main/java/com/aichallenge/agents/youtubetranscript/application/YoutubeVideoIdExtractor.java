package com.aichallenge.agents.youtubetranscript.application;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

public class YoutubeVideoIdExtractor {

    private static final Pattern VIDEO_ID = Pattern.compile("^[A-Za-z0-9_-]{11}$");

    public Optional<String> extract(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return Optional.empty();
        }

        try {
            URI uri = URI.create(rawUrl.trim());
            String host = uri.getHost();
            String path = uri.getPath();

            if (host == null || path == null) {
                return Optional.empty();
            }

            host = host.toLowerCase();
            if (host.equals("youtu.be")) {
                return firstPathSegment(path);
            }

            if (host.equals("youtube.com") || host.equals("www.youtube.com") || host.equals("m.youtube.com")) {
                if (path.equals("/watch")) {
                    return queryParam(uri.getRawQuery(), "v").filter(this::isValidVideoId);
                }

                if (path.startsWith("/shorts/") || path.startsWith("/embed/")) {
                    return pathSegment(path, 1);
                }
            }

            return Optional.empty();
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private Optional<String> firstPathSegment(String path) {
        return pathSegment(path, 0);
    }

    private Optional<String> pathSegment(String path, int index) {
        String[] segments = path.split("/");
        int seen = 0;
        for (String segment : segments) {
            if (segment.isBlank()) {
                continue;
            }
            if (seen == index && isValidVideoId(segment)) {
                return Optional.of(segment);
            }
            seen++;
        }
        return Optional.empty();
    }

    private Optional<String> queryParam(String rawQuery, String name) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Optional.empty();
        }

        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && parts[0].equals(name)) {
                return Optional.of(parts[1]);
            }
        }

        return Optional.empty();
    }

    private boolean isValidVideoId(String value) {
        return value != null && VIDEO_ID.matcher(value).matches();
    }
}
