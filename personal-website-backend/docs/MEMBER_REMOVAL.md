# Member model removal

The backend now represents a personal site owned by one administrator, Ryan, plus regular student accounts.

Flyway migration `V35__replace_members_with_single_admin.sql` copies Ryan's legacy member profile to the admin user, migrates Ryan's member experiences to the existing global work-experience table, grants that user `ROLE_ADMIN`, and drops the member tables and ownership columns. Legacy member accounts and `ROLE_MEMBER` authorities are removed.

Content records (courses, projects, articles, and certificates) are retained as global Ryan-owned site content. The API no longer has `/members` or `/auth/members` endpoints, member fields, or member ownership checks. The replacement profile endpoints are `GET /profile` and the admin-only `/admin/profile` endpoints described in `API_CONTRACT.md`.
