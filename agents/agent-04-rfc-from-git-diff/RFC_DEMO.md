# RFC: Add WebCheck Agent, Extend AgentStrategy, Update Token Limits, and AWS Parameter Store Defaults

## Summary
This change introduces a new **WebCheck** agent that performs web reputation checks using SerpAPI and HTTP GET requests. It adds a new `WEB_CHECK` strategy type, updates token usage limits, and adjusts AWS Parameter Store defaults. Documentation for the strategy system is also added.

## Change Scope
- **New Features**: WebCheck agent, WebCheck persistence, and API endpoints.
- **Strategy Enhancements**: Add `WEB_CHECK` to `AgentStrategy.AgentType`.
- **Token Usage**: Increase default token limit from 10 000 to 1 000 000.
- **AWS Parameter Store**: Switch default parameter name from `/dev/isaac` to `/dev/argentis`.
- **Documentation**: Add `agent_strategy_implementation.md`.
- **Exception Handling**: Add `INSUFFICIENT_TOKENS` mapping to `TokenLimitException`.

## Technical Changes
| Area | Change |
|------|--------|
| **Domain** | `AgentStrategy` now includes `WEB_CHECK` enum value. |
| **Application** | `WebCheckAgent` created; initializes default strategy on startup; runs web checks via SerpAPI and HTTP GET. |
| **Infrastructure** | `WebCheckEntity`, `WebCheckJpaBaseRepository`, `WebCheckJpaRepository` added for persistence. |
| **Controller** | `ChatsController` now exposes `/webcheck` POST/GET endpoints and integrates `WebCheckAgent`, `WebCheckSaver`, `WebCheckFinder`. |
| **Repository** | `WebCheckRepository` interface added; implementations use JPA. |
| **Token Usage** | `TokenUsageTracker.validateOrThrow` now uses limit `1_000_000L`. |
| **Exception Mapping** | `SupportedExceptions` now includes `INSUFFICIENT_TOKENS` for `TokenLimitException`. |
| **AWS Config** | `AWSParameterStoreEnvironmentPostProcessor` default parameter changed to `/dev/argentis`. |
| **Documentation** | New markdown file explaining strategy system. |

## Functional Impact
- **New API**: Clients can request a web reputation report via `POST /chats/{chatId}/webcheck` and retrieve stored reports via `GET /chats/{chatId}/webcheck` or `GET /chats/{chatId}/webcheck/{webcheckId}`.
- **Strategy Management**: Existing agents (Summary, Checklist, TitleGenerator) continue to use their strategies; new `WEB_CHECK` strategy can be created/activated via back‑office endpoints.
- **Token Limits**: Users now have a higher token allowance; `TokenLimitException` will be thrown only when exceeding 1 000 000 tokens.
- **AWS Parameter Store**: Configuration now loads from `/dev/argentis`; old `/dev/isaac` is no longer used.

## Risks & Considerations
- **SerpAPI Key Exposure**: `serpapi.api.key` is injected via Spring property; ensure it is secured in production.
- **HTTP GET Requests**: The agent performs external HTTP GETs; potential for blocking or slow responses. Timeout is set to 20 s.
- **Token Limit Increase**: Raising the limit may increase cost; monitor usage metrics.
- **Database Schema**: New `web_check` table required; migrations must be applied.
- **Exception Mapping**: `INSUFFICIENT_TOKENS` returns HTTP 402; clients must handle this status.

## Open Questions
- Should the WebCheck agent support pagination or multiple entities per request?
- Is there a need to cache SerpAPI results to reduce API calls?
- How will the new strategy type be exposed in the back‑office UI?
- Are there any compliance concerns with scraping external sites via HTTP GET?

## Review Checklist
- [ ] Verify `WEB_CHECK` strategy creation and activation flow.
- [ ] Confirm SerpAPI integration works with provided key.
- [ ] Ensure `TokenUsageTracker` uses the new limit and throws `TokenLimitException` correctly.
- [ ] Test new API endpoints for creating and retrieving WebCheck reports.
- [ ] Run database migrations to create `web_check` table.
- [ ] Validate AWS Parameter Store loads from `/dev/argentis`.
- [ ] Check exception handling for `INSUFFICIENT_TOKENS` returns 402.
- [ ] Update documentation and ensure it compiles in the docs build.
