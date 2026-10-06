# Notes

## Summary of changes
One commit per fix, highest impact first:

- **Search query precedence** (repository + both SQL files): un-bracketed `AND`/`OR` let title matches skip the status filter and description matches skip `archived`.
- **Removed the fake `Thread.sleep`** that made empty/short searches take up to 1s.
- **Validation:** bad `status`, `page < 1`, `pageSize` outside 1–100 now return 400 with a message, not 500.
- **LIKE escaping:** user-typed `%` and `_` match literally.
- **DB-side pagination** (`Pageable` + count query) instead of slicing every match in Java; `id` tie-breaker for stable pages.
- **Frontend:** abort stale requests, stop the UI hanging on "Loading…" after an error, reset to page 1 on filter change, 300ms debounce, show server error messages.
- Same fixes mirrored in the Oracle package; MockMvc regression tests added.

## Not changed
- `status` stored as String instead of the enum (touches schema and seed data).
- H2 console / `show-sql` left on (local dev setup).
- Loading skeleton / keeping old rows while loading: UX polish, not a bug.
- Oracle ROWNUM kept; the file targets pre-12c.

## Biggest remaining risk
`LIKE '%term%'` on `LOWER(...)` can't use an index, so search becomes a full table scan as data grows; it needs full-text search eventually. The API also has no auth or rate limiting.

## Tools / AI used
Claude Code: to read the codebase, reproduce each bug against the running app (curl and a Playwright script: 2/16 checks passed before, 16/16 after), and draft fixes and tests, which I reviewed. I corrected it where needed: one Playwright check passed vacuously, and Windows CRLF endings would have broken `mvnw` on macOS/Linux.
