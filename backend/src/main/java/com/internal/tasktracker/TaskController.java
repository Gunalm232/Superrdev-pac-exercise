package com.internal.tasktracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// CORS annotation removed: the Vite dev server proxies /api/*, so the browser
// sees same-origin requests and a hard-coded localhost origin is unnecessary.
@RestController
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_QUERY_LENGTH = 100;

    /** Typed response shape (replaces ResponseEntity<?> + untyped Map). */
    public record TaskPage(List<Task> items, int total, int page, int pageSize) {}

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<TaskPage> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // Normalize query input and cap its length
        String query = q == null ? "" : q.trim();
        if (query.length() > MAX_QUERY_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Query too long (max " + MAX_QUERY_LENGTH + " characters)");
        }
        String searchTerm = "%" + escapeLike(query.toLowerCase()) + "%";

        // Parse status filter: invalid values are a client error (400), not a 500
        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                normalizedStatus = TaskStatus.valueOf(status.trim().toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid status. Allowed: " + Arrays.toString(TaskStatus.values()));
            }
        }

        // Clamp paging inputs so bad values can't cause negative subList indexes
        int safePage = Math.max(1, page);
        int safePageSize = Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);

        // Strip CR/LF from user input before logging (log injection)
        log.info("search q=\"{}\" status={} page={} pageSize={}",
                query.replaceAll("[\\r\\n]", " "), normalizedStatus, safePage, safePageSize);

        List<Task> allResults = taskRepository.searchTasks(searchTerm, normalizedStatus);

        // long math avoids int overflow for very large page numbers
        long start = (long) (safePage - 1) * safePageSize;
        List<Task> pageResults = Collections.emptyList();
        if (start < allResults.size()) {
            int end = (int) Math.min(start + safePageSize, allResults.size());
            pageResults = allResults.subList((int) start, end);
        }

        return ResponseEntity.ok(new TaskPage(pageResults, allResults.size(), safePage, safePageSize));
    }

    /** Escape LIKE wildcards so user-typed % and _ are matched literally. */
    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
