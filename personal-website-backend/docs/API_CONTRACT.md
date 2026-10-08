# Bashmobarmeg API contract

## Connection and conventions

- Local base URL: `http://localhost:8080/api`.
- Send JSON unless an endpoint specifies `multipart/form-data`.
- Supply `Authorization: Bearer <token>` for authenticated endpoints.
- IDs are UUID strings. Dates are `YYYY-MM-DD`; timestamps are ISO-8601 strings.
- Roles are `ADMIN` (Ryan only) and `STUDENT`.
- `204 No Content` responses have no response body.

| Status | Meaning |
| --- | --- |
| `400` | Validation or business-rule error |
| `401` | Missing or invalid JWT |
| `403` | Authenticated but not permitted |
| `404` | Requested resource does not exist |
| `409` | Duplicate or conflicting resource |

## Authentication

Public registration creates only student accounts. Admin accounts are provisioned through the configured bootstrap administrator, not an HTTP registration endpoint.

| Method | Path | Access | Request | Response |
| --- | --- | --- | --- | --- |
| `POST` | `/auth/students` | Public | `AuthenticationRequest` | `200 AuthenticationResponse` |
| `POST` | `/auth/login` | Public | `LoginRequest` | `200 AuthenticationResponse` |
| `GET` | `/auth/me` | Signed in | — | `200 AuthenticatedUser` |

```json
// AuthenticationRequest
{ "name": "Jane Doe", "email": "jane@example.com", "password": "at-least-8-characters" }

// LoginRequest
{ "email": "jane@example.com", "password": "at-least-8-characters" }

// AuthenticationResponse
{ "token": "jwt", "role": "ADMIN" }

// AuthenticatedUser
{ "email": "ryan@example.com", "role": "ADMIN" }
```

`name` accepts letters and spaces only. Passwords require at least eight characters.

## Ryan profile

The public about profile belongs to Ryan alone. It is not a member resource.

`AdminProfile`: `{ "id", "name", "headline", "biography", "photoUrl" }`

| Method | Path | Access | Request | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/profile` | Public | — | `200 AdminProfile` |
| `GET` | `/admin/profile` | `ADMIN` | — | `200 AdminProfile` |
| `PUT` | `/admin/profile` | `ADMIN` | `UpdateAdminProfile` | `200 AdminProfile` |
| `POST` | `/admin/profile/photo` | `ADMIN` | Multipart field `photo` | `200 AdminProfile` |

```json
{ "name": "Ryan Abdulaziz", "headline": "optional", "biography": "optional", "photoUrl": "optional" }
```

Photo uploads accept JPG, PNG, or WebP files up to 5 MB. The returned `photoUrl` is publicly readable below `/uploads/admin-profile/`.

## Courses, lessons, and sections

All course content belongs to the site. There is no member owner, `memberIds`, or `membersName` filter.

`Course`: `{ "id", "title", "description", "createTime", "thumbnailLink", "price", "enrollments": ["Enrollment"], "lessons": ["Lesson"] }`

| Method | Path | Access | Request / query | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/courses` | Public | Optional `title`, `minPrice`, `maxPrice` | `200 Course[]` |
| `GET` | `/courses/featured` | Public | — | `200 Course[]` |
| `GET` | `/courses/search/{title}` | Public | — | `200 Course[]` |
| `GET` | `/courses/{courseId}` | Public | — | `200 Course` |
| `POST` | `/courses` | `ADMIN` | `CourseRequest` | `201 Course` |
| `PUT` | `/courses/{id}` | `ADMIN` | `CourseRequest` | `200 Course` |
| `DELETE` | `/courses/{courseId}` | `ADMIN` | — | `204` |
| `GET` | `/lessons/course/{courseId}` | Public | — | `200 Lesson[]` |
| `GET` | `/lessons/course/{courseId}/search` | Public | Required `title` | `200 Lesson[]` |
| `POST` | `/lessons` | `ADMIN` | `CreateLesson` | `201 Lesson` |
| `PUT` | `/lessons/{id}` | `ADMIN` | `UpdateLesson` | `200 Lesson` |
| `DELETE` | `/lessons/{id}` | `ADMIN` | — | `204` |

```json
// CourseRequest
{ "price": 0, "title": "Java", "description": "required", "thumbnailLink": "optional" }

// CreateLesson
{ "courseId": "uuid", "title": "required", "videoLink": "optional" }

// UpdateLesson
{ "title": "required", "videoLink": "optional" }

// Lesson
{ "id": "uuid", "courseId": "uuid", "title": "...", "videoLink": "...", "createTime": "timestamp" }
```

`price` is required and must be zero or more.

### Sections

`Section`: `{ "id", "title", "description", "orderIndex", "createTime", "courseId" }`.

| Method | Path | Access | Request / query | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/sections/course/{courseId}` | Public | — | `Section[]` |
| `GET` | `/sections/{id}` | Public | — | `Section` |
| `GET` | `/sections/{id}/course/{courseId}` | Public | — | `Section` |
| `GET` | `/sections/{id}/exists` | Public | — | `boolean` |
| `GET` | `/sections/{id}/course/{courseId}/exists` | Public | — | `boolean` |
| `GET` | `/sections/course/{courseId}/count` | Public | — | `number` |
| `POST` | `/sections/course/{courseId}` | `ADMIN` | `SectionRequest` | `201 Section` |
| `PUT` | `/sections/{id}` | `ADMIN` | `SectionRequest` | `200 Section` |
| `PATCH` | `/sections/{id}/order` | `ADMIN` | Required `orderIndex` query parameter | `200 Section` |
| `PUT` | `/sections/course/{courseId}/reorder` | `ADMIN` | UUID array in display order | `200 Section[]` |
| `PATCH` | `/sections/{id}/course/{courseId}/move-up` | `ADMIN` | — | `200 Section` |
| `PATCH` | `/sections/{id}/course/{courseId}/move-down` | `ADMIN` | — | `200 Section` |
| `DELETE` | `/sections/{id}` | `ADMIN` | — | `204` |
| `DELETE` | `/sections/{id}/course/{courseId}` | `ADMIN` | — | `204` |
| `DELETE` | `/sections/course/{courseId}` | `ADMIN` | — | `204` |

```json
{ "title": "required, 2-255 characters", "description": "optional, max 5000 characters", "orderIndex": 1 }
```

## Enrollment, progress, and reviews

`Enrollment`: `{ "id", "studentId", "studentName", "courseId", "courseTitle", "paymentStatus", "enrolledAt" }`.

`Progress`: `{ "id", "studentId", "studentName", "lessonId", "lessonTitle", "isComplete" }`.

`Review`: `{ "id", "studentId", "studentName", "courseId", "courseTitle", "rating", "comment", "createTime" }`.

| Method | Path | Access | Request | Response |
| --- | --- | --- | --- | --- |
| `POST` | `/enrollments` | Signed in | `{ "studentId", "courseId" }` | `200 Enrollment` |
| `POST` | `/enrollments/courses/{courseId}/me` | `STUDENT` | — | `200 Enrollment` |
| `GET` | `/enrollments/students/{studentId}` | Signed in | — | `200 Enrollment[]` |
| `GET` | `/enrollments/courses/{courseId}` | Signed in | — | `200 Enrollment[]` |
| `GET` | `/enrollments/courses/{courseId}/me` | Signed in | — | `200 boolean` |
| `DELETE` | `/enrollments/{id}` | Signed in | — | `200` empty body |
| `GET` | `/progress/student/{studentId}` | Signed in | — | `200 Progress[]` |
| `GET` | `/progress/lesson/{lessonId}` | Signed in | — | `200 Progress[]` |
| `POST` | `/progress` | Signed in | `{ "studentId", "lessonId" }` | `201 Progress` |
| `PUT` | `/progress/{id}` | Signed in | `{ "isComplete": true }` | `200 Progress` |
| `DELETE` | `/progress/{id}` | Signed in | — | `204` |
| `GET` | `/reviews/course/{courseId}` | Public | — | `200 Review[]` |
| `GET` | `/reviews/student/{studentId}` | Signed in | — | `200 Review[]` |
| `POST` | `/reviews` | `STUDENT` | `{ "studentId", "courseId", "rating", "comment" }` | `201 Review` |
| `DELETE` | `/reviews/{id}` | `ADMIN` | — | `204` |

Review ratings must be integers from 1 through 5. IDs in create requests are required.

## Articles, tags, and portfolio content

All items in this section are Ryan's global site content. They are not owned by members.

### Articles and tags

`Article`: `{ "id", "title", "content", "createTime", "tags": ["Java"] }`.

| Method | Path | Access | Request / query | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/articles` | Public | Optional `query`, repeated `tags`, `from`, `to` | `200 Article[]` |
| `GET` | `/articles/{id}` | Public | — | `200 Article` |
| `GET` | `/articles/tag/{tagName}` | Public | — | `200 Article[]` |
| `POST` | `/admin/articles` | `ADMIN` | `ArticleRequest` | `201 Article` |
| `PUT` | `/admin/articles/{id}` | `ADMIN` | `ArticleRequest` | `200 Article` |
| `DELETE` | `/admin/articles/{id}` | `ADMIN` | — | `204` |
| `GET` | `/tags` | Public | — | `200 Tag[]` |
| `GET` | `/tags/{id}` | Public | — | `200 Tag` |
| `POST` | `/tags` | `ADMIN` | `{ "name": "required" }` | `201 Tag` |
| `DELETE` | `/tags/{id}` | `ADMIN` | — | `204` |

```json
{ "title": "required", "content": "required", "tags": ["Java", "Spring"] }
```

Pass multiple article-tag filters as repeated query parameters, for example `?tags=Java&tags=Spring`.

### Projects, work experience, and certificates

| Method | Path | Access | Request | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/projects` | Public | — | `200 Project[]` |
| `POST` | `/projects` | `ADMIN` | `ProjectRequest` | `201 Project` |
| `PUT` | `/projects/{id}` | `ADMIN` | `ProjectRequest` | `200 Project` |
| `DELETE` | `/projects/{id}` | `ADMIN` | — | `204` |
| `GET` | `/work-experiences` | Public | — | `200 WorkExperience[]` |
| `POST` | `/work-experiences` | `ADMIN` | `WorkExperienceRequest` | `201 WorkExperience` |
| `PUT` | `/work-experiences/{id}` | `ADMIN` | `WorkExperienceRequest` | `200 WorkExperience` |
| `DELETE` | `/work-experiences/{id}` | `ADMIN` | — | `204` |
| `GET` | `/certificates` | Public | — | `200 Certificate[]` |
| `POST` | `/certificates` | `ADMIN` | `CertificateRequest` | `201 Certificate` |
| `PUT` | `/certificates/{id}` | `ADMIN` | `CertificateRequest` | `200 Certificate` |
| `DELETE` | `/certificates/{id}` | `ADMIN` | — | `204` |
| `GET` | `/admin/about` | `ADMIN` | — | `200` empty body |

```json
// ProjectRequest and Project
{ "title": "required", "description": "optional", "imagePath": "optional", "projectUrl": "required", "displayOrder": 1 }

// WorkExperienceRequest; response additionally includes id
{ "company": "required", "role": "required", "startDate": "YYYY-MM-DD", "endDate": null, "summary": "required", "highlights": ["..."], "displayOrder": 1 }

// CertificateRequest; response additionally includes id
{ "title": "required", "issuer": "optional", "issuedDate": "YYYY-MM-DD", "imagePath": "required", "displayOrder": 1 }
```

## Student management

| Method | Path | Access | Request | Response |
| --- | --- | --- | --- | --- |
| `POST` | `/students` | `ADMIN` | `{ "name", "email", "password" }` | `200 Student` |
| `GET` | `/students` | `ADMIN` | — | `200 Student[]` |
| `GET` | `/students/{id}` | Signed in | — | `200 Student` |
| `PUT` | `/students/{id}` | Signed in | `{ "name", "email" }` | `200 Student` |
| `POST` | `/students/{id}` | Signed in | `{ "currentPassword", "newPassword" }` | `200` empty body |
| `DELETE` | `/students/{id}` | Signed in | — | `200` empty body |

`Student` is `{ "id", "name", "email" }`. Student name and password validation match registration.

## LinkedIn administration

Every LinkedIn route except the callback requires `ADMIN`. The callback is public so LinkedIn can redirect the browser after consent.

| Method | Path | Access | Response |
| --- | --- | --- | --- |
| `GET` | `/linkedin/connection` | `ADMIN` | `{ "configured", "connected", "personUrn", "expiresAt" }` |
| `POST` | `/linkedin/authorization-url` | `ADMIN` | `{ "authorizationUrl": "https://..." }` |
| `GET` | `/linkedin/callback?code=...&state=...` | Public | `204`; `400` for denied/invalid callback, `502` for token exchange failure |
| `GET` | `/linkedin/articles/{articleId}` | `ADMIN` | `{ "articleId", "status", "linkedInPostUrn", "failureMessage", "publishedAt" }` |
| `POST` | `/linkedin/articles/{articleId}/publish` | `ADMIN` | Publication response |

## Operational endpoints and assets

| Method | Path | Access | Response |
| --- | --- | --- | --- |
| `GET` | `/actuator/health` | Public | Spring Boot health object |
| `GET` | `/uploads/**` | Public | Uploaded asset |

The configured frontend origin is the only cross-origin browser client accepted by the backend. The server accepts `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, and `OPTIONS` methods.
