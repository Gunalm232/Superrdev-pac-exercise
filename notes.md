# Patch Exercise Notes

## Summary of Changes

I reviewed the frontend, backend, and SQL query logic and focused on high-impact correctness, reliability, and performance issues.

1. **Search/filter precedence:** Fixed the SQL `WHERE` clause by grouping title/description search conditions before applying archived and status filters. This prevents archived tasks and incorrectly filtered statuses from appearing.
2. **Backend delay:** Removed the artificial `Thread.sleep()` from `TaskController`, which caused slow responses and could result in stale search results while typing.
3. **Pagination validation:** Added bounds for `page` and `pageSize`, including a maximum page size of 100, preventing invalid `subList()` ranges and excessive data retrieval.
4. **Invalid status:** Changed invalid status handling from an unhandled `IllegalArgumentException`/500 response to a proper 400 Bad Request.
5. **Stable ordering:** Added `id DESC` as a tie-breaker to `created_at DESC`, making pagination deterministic when timestamps are equal.
6. **createdAt:** Added automatic timestamp initialization for newly persisted tasks.
7. **Smaller robustness improvements:** Escaped `%` and `_` in search input, added null-safe status rendering, handled missing priority display, and added query-length validation and accessibility labels.

## What I Chose Not to Change

I avoided rewriting the application or making broad architectural changes because the exercise prioritizes focused, high-value patches. I also left low-impact cosmetic behavior unchanged where it did not affect correctness.

## Biggest Remaining Risk

The application still uses in-memory H2 and performs pagination after retrieving filtered results, which may not scale well with a large production dataset.

## Tools/AI Used

I used ChatGPT to help inspect the code, identify possible failure cases, understand root causes, and review potential fixes. I verified the important issues using API requests and adjusted the suggested changes based on the actual application behavior.
