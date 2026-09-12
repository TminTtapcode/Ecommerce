# AI DEVELOPMENT WORKFLOW
## Enterprise Ecommerce — Existing System → Incremental Vertical Slice

> Mục đích: quy trình chuẩn cho Gemini/Antigravity khi tiếp tục phát triển hệ thống hiện tại.
>
> Nguyên tắc cốt lõi: **Không đập đi xây lại. Inspect → Understand → Design → Approve → Implement Backend + Frontend → Verify → Checkpoint → Update PROJECT_CONTEXT.md.**

---

## 1. SOURCE OF TRUTH

AI phải đọc và tham chiếu:

```text
@docs/ARCHITECTURE.md
@docs/PROJECT_CONTEXT.md
@docs/MODULE_DEVELOPMENT_WORKFLOW.md
@docs/AI_PROMPTS.md
```

| File | Vai trò |
|---|---|
| `ARCHITECTURE.md` | Kiến trúc mục tiêu và nguyên tắc |
| `PROJECT_CONTEXT.md` | Trạng thái THỰC TẾ hiện tại |
| `MODULE_DEVELOPMENT_WORKFLOW.md` | Quy trình xây feature/module |
| `AI_PROMPTS.md` | Bộ prompt/chỉ mục điều khiển AI |

**ARCHITECTURE.md = hệ thống nên như thế nào.**

**PROJECT_CONTEXT.md = hệ thống thực tế đang như thế nào.**

Nếu code hiện tại chưa đúng architecture, phải ghi nhận là architecture gap/technical debt, không được giả vờ đã đúng.

---

## 2. EXISTING SYSTEM RULE

Đây là hệ thống đã được xây dựng trước đó.

AI MUST NOT:

- Rebuild toàn bộ project.
- Xóa code đang hoạt động.
- Rewrite module chỉ vì implementation chưa hoàn hảo.
- Refactor toàn bộ codebase trước khi tiếp tục feature.
- Thay đổi architecture lớn mà chưa được approval.
- Sửa các module không liên quan.
- Đánh dấu feature completed khi chưa verify.

AI MUST:

1. Inspect code hiện tại trước.
2. Hiểu implementation hiện tại.
3. Giữ behavior đang hoạt động.
4. Tìm smallest safe change.
5. Ưu tiên incremental improvement.
6. Refactor sâu chỉ khi có lý do rõ ràng.
7. Bảo vệ backward compatibility khi có thể.
8. Phân biệt bug, architecture gap và technical debt.

---

## 3. WORKFLOW TỔNG THỂ

Mỗi feature được xây theo một **Vertical Slice**:

```text
CURRENT SYSTEM
      ↓
CONTEXT CHECK
      ↓
DISCOVER
      ↓
UNDERSTAND
      ↓
DESIGN BACKEND
      ↓
DESIGN FRONTEND
      ↓
DESIGN REVIEW
      ↓
USER APPROVAL
      ↓
IMPLEMENT BACKEND
      ↓
VERIFY BACKEND
      ↓
IMPLEMENT FRONTEND
      ↓
VERIFY FRONTEND
      ↓
END-TO-END VERIFICATION
      ↓
ARCHITECTURE REVIEW
      ↓
CHECKPOINT
      ↓
UPDATE PROJECT_CONTEXT.md
      ↓
NEXT FEATURE
```

Không bỏ qua Design/Approval với feature có business logic hoặc ảnh hưởng architecture.

---

## 4. INITIAL PROJECT AUDIT

Chỉ dùng khi tiếp quản project hiện tại hoặc context chưa đáng tin cậy.

```text
Read:

@docs/ARCHITECTURE.md
@docs/PROJECT_CONTEXT.md
@docs/MODULE_DEVELOPMENT_WORKFLOW.md
@docs/AI_PROMPTS.md

Inspect the actual existing backend and frontend codebase.

Do NOT modify any files.

Compare PROJECT_CONTEXT.md with the actual implementation.

Report:
1. Existing modules
2. Completed features
3. Partially completed features
4. Backend status
5. Frontend status
6. Database/entity status
7. API status
8. Authentication/security status
9. Cross-module dependencies
10. Architecture gaps
11. Technical debt
12. Critical bugs
13. Current development position
14. Recommended next feature

Classify issues:
A. Compliant
B. Minor improvement
C. Important architecture issue
D. Must fix before continuing
E. Can postpone

Do not rebuild or implement anything.
Show the assessment first.
```

---

## 5. CONTEXT CHECK

```text
Read:

@docs/ARCHITECTURE.md
@docs/PROJECT_CONTEXT.md
@docs/MODULE_DEVELOPMENT_WORKFLOW.md
@docs/AI_PROMPTS.md

This is an EXISTING project.

Module:
[MODULE]

Feature:
[FEATURE]

Use Case:
[USE CASE]

Inspect the current implementation first.

Do not implement yet.

Summarize:
1. Current state
2. Existing related code
3. Existing APIs
4. Existing entities
5. Existing frontend
6. Existing dependencies
7. Constraints
8. Completed work
9. Remaining work
10. Risks
```

AI phải dừng sau bước này.

---

## 6. DISCOVER

Inspect code thực tế liên quan feature.

### Backend

- Entity
- Repository
- Service
- Controller
- DTO
- Validation
- Exception
- Security
- Transaction
- Tests

### Frontend

- Pages
- Components
- API client
- Types
- Forms
- Validation
- State
- Routing
- Error handling

### Cross-module

- Internal service
- Internal DTO
- Module dependency
- Repository/entity coupling

```text
AI_PROMPTS.md → DISCOVER

Inspect the existing implementation related to this feature.

Do not modify files.

Identify reusable code and existing patterns.

Do not create duplicate abstractions if existing ones can be reused.
```

---

## 7. UNDERSTAND

Trước khi code phải hiểu nghiệp vụ và request lifecycle:

```text
User Action
    ↓
Frontend
    ↓
HTTP Request
    ↓
Controller
    ↓
Service
    ↓
Repository / Internal Contract
    ↓
Database
    ↓
Response
    ↓
Frontend State
    ↓
UI
```

```text
AI_PROMPTS.md → UNDERSTAND

Explain:
1. Business purpose
2. User flow
3. Backend request lifecycle
4. Frontend flow
5. Database interaction
6. Validation
7. Business rules
8. Error cases
9. Authorization
10. Cross-module communication
11. Transaction boundary

Use the actual existing implementation.
Do not implement yet.
```

---

## 8. DESIGN BACKEND

Xác định:

- API
- Request DTO
- Response DTO
- Entity changes
- Repository
- Service
- Business rules
- Validation
- Exception
- Transaction
- Security
- Internal contract nếu cross-module

```text
AI_PROMPTS.md → DESIGN BACKEND

Design the backend.

For every proposed change explain:
1. File/class
2. Responsibility
3. Why needed
4. Existing code reused
5. Business rule
6. API contract
7. Validation
8. Exception handling
9. Transaction boundary
10. Security
11. Cross-module dependency
12. Risks

Prefer the smallest valid change.
Do not implement yet.
```

---

## 9. DESIGN FRONTEND

Thiết kế dựa trên API contract/backend behavior.

```text
AI_PROMPTS.md → DESIGN FRONTEND

Design the frontend for the same feature.

Explain:
1. Page/component changes
2. TypeScript types
3. API integration
4. Form/input behavior
5. Validation
6. Loading state
7. Error state
8. Success state
9. State management
10. Routing
11. Reusable existing components

Do not implement yet.
```

---

## 10. DESIGN REVIEW

```text
AI_PROMPTS.md → DESIGN REVIEW

Review the complete feature:

Backend + Frontend + Database + Cross-module communication
+ Security + Validation + Error handling.

Check:
1. Architecture compliance
2. Existing code reuse
3. Coupling
4. Circular dependencies
5. API consistency
6. Data consistency
7. Transaction correctness
8. Security
9. UX states
10. Unnecessary complexity

Classify:
- Must fix
- Should improve
- Can postpone

Do not implement yet.
```

---

## 11. APPROVAL

AI không được tự coi design là approved.

Người dùng kiểm tra flow, API, database, architecture, UI và scope.

Khi đồng ý:

```text
APPROVED.

Proceed with the approved design.

Do not introduce unrelated changes.
```

---

## 12. IMPLEMENT BACKEND

```text
AI_PROMPTS.md → IMPLEMENT BACKEND

Implement only the approved backend design.

Rules:
- Do not rewrite unrelated modules.
- Reuse existing code.
- Follow ARCHITECTURE.md.
- Preserve working behavior.
- Do not add unnecessary abstractions.
- Do not add technologies without justification.
- Keep DTO/entity boundaries clear.
- Respect module boundaries.
- Add validation and exception handling.
- Apply transactions only where needed.

After implementation:
1. List changed files.
2. Explain each change.
3. Report deviations from approved design.
4. Do not silently change architecture.
```

---

## 13. VERIFY BACKEND

```text
AI_PROMPTS.md → VERIFY BACKEND

Verify:
1. Compilation
2. Application startup
3. API behavior
4. Validation
5. Error handling
6. Authentication/authorization
7. Database behavior
8. Transaction behavior
9. Module boundaries
10. Existing functionality regression

Run appropriate tests/build where possible.

Report:
PASS / FAIL / WARNING

For failures:
- root cause
- smallest safe fix

Do not modify unrelated code.
```

Chỉ khi backend ổn mới chuyển sang frontend.

---

## 14. IMPLEMENT FRONTEND

```text
AI_PROMPTS.md → IMPLEMENT FRONTEND

Implement the approved frontend design.

Rules:
- Use the actual backend API contract.
- Reuse existing components and API patterns.
- Do not duplicate logic unnecessarily.
- Handle loading/error/success states.
- Validate user input.
- Keep TypeScript types consistent.
- Do not modify unrelated pages.

After implementation:
1. List changed files.
2. Explain changes.
3. Report deviations.
```

---

## 15. VERIFY FRONTEND

```text
AI_PROMPTS.md → VERIFY FRONTEND

Verify:
1. Build
2. TypeScript errors
3. API integration
4. Form validation
5. Loading state
6. Error state
7. Success state
8. Routing
9. UI behavior
10. Existing page regression

Report PASS / FAIL / WARNING.

Fix only issues related to this feature.
```

---

## 16. END-TO-END VERIFICATION

```text
AI_PROMPTS.md → END-TO-END VERIFICATION

Trace:

User
→ Frontend
→ HTTP
→ Controller
→ Service
→ Repository/Internal Contract
→ Database
→ Response
→ Frontend
→ UI

Verify:
1. Request format
2. Response format
3. HTTP status
4. Authentication
5. Authorization
6. Validation
7. Error handling
8. Database persistence
9. Frontend state
10. User-visible behavior

Check happy path and important failure paths.

Report PASS / FAIL / WARNING.
```

---

## 17. ARCHITECTURE REVIEW

```text
AI_PROMPTS.md → ARCHITECTURE REVIEW

Review only code changed during this feature.

Check against:
@docs/ARCHITECTURE.md

Verify:
- Module ownership
- Module boundaries
- Cross-module communication
- Entity exposure
- DTO usage
- Dependency direction
- Circular dependencies
- Transaction boundaries
- Security
- Exception handling
- Unnecessary complexity

Classify:
FIX NOW
FIX WHEN TOUCHING MODULE
TECHNICAL DEBT
NO ACTION

Do not perform broad refactoring automatically.
```

---

## 18. CHECKPOINT

Feature chỉ hoàn thành khi:

```text
Backend ✓
Frontend ✓
E2E ✓
Architecture Review ✓
```

```text
AI_PROMPTS.md → CHECKPOINT

Report:
1. Feature completed
2. Backend changes
3. Frontend changes
4. Database changes
5. APIs added/changed
6. Verification
7. Architecture decisions
8. Technical debt
9. Known limitations
10. Recommended next feature

Do not modify code.
```

---

## 19. UPDATE PROJECT_CONTEXT

Sau checkpoint, AI phải đồng bộ context với trạng thái ĐÃ VERIFY.

```text
AI_PROMPTS.md → UPDATE PROJECT_CONTEXT

Synchronize:

@docs/PROJECT_CONTEXT.md

with the VERIFIED current state of the project.

Inspect actual implementation.

Only update information that actually changed.

Update when applicable:
1. Project status
2. Module status
3. Current development position
4. Completed features
5. Known issues
6. Technical debt
7. Important architecture decisions
8. Next steps
9. Backend progress
10. Frontend progress
11. Database progress

IMPORTANT:
- Do not invent progress.
- Do not mark unverified work as completed.
- Do not claim architecture compliance if code still violates it.
- Record remaining architecture gaps as issues/technical debt.
- Do not modify ARCHITECTURE.md.
- Do not modify source code.

First show:
## Proposed Context Changes

For each:
- Section
- Old information
- New information
- Reason

Wait for approval.
```

Sau khi duyệt:

```text
Approved.

Apply only the proposed PROJECT_CONTEXT.md changes.

Do not modify source code or other documentation.
```

---

## 20. PROJECT_CONTEXT UPDATE RULES

Không update sau từng dòng code.

### Update Module Status

```text
Not Started → In Progress → Completed
```

### Update Completed Features

Chỉ đánh completed khi:

```text
Backend ✓
Frontend ✓
E2E ✓
```

### Update Current Development Position

Khi:
- đổi module;
- đổi feature;
- chuyển backend → frontend;
- hoàn thành feature.

### Update Architecture Decisions

Chỉ khi có architecture decision quan trọng đã được approve.

### Update Technical Debt

Chỉ ghi issue có ý nghĩa đối với correctness, security, architecture hoặc maintainability.

### Update Next Steps

Sau khi feature hiện tại hoàn thành và xác định feature tiếp theo.

---

## 21. BACKEND + FRONTEND SONG SONG

“Song song” nghĩa là xây **một vertical slice hoàn chỉnh**, không phải code Backend và Frontend hỗn loạn cùng lúc.

```text
Feature
  ↓
Business Understanding
  ↓
Backend API Design
  ↓
Frontend Design
  ↓
Approve
  ↓
Backend Implementation
  ↓
Backend Verification
  ↓
Frontend Implementation
  ↓
Frontend Verification
  ↓
E2E
```

Không cần hoàn thành toàn bộ Backend project trước mới làm Frontend.

Backend đi trước trong từng feature để tạo stable API contract.

---

## 22. SCOPE CONTROL

Mỗi feature phải có:

```text
Module
Feature
Use Case
In Scope
Out of Scope
```

Ví dụ:

```text
Module: Auth
Feature: User Registration
Use Case: Register account

In Scope:
- Registration API
- Password hashing
- Validation
- User persistence
- Registration UI
- API integration

Out of Scope:
- Login
- OAuth
- Refresh token
- Password reset
```

AI không tự mở rộng scope.

Nếu phát hiện việc ngoài scope:

```text
Do not implement.
Record it as a possible next step.
```

---

## 23. KHI NÀO REFACTOR?

### FIX NOW

- Build/compile failure
- Security vulnerability
- Data corruption risk
- Incorrect business logic
- Broken transaction
- Severe circular dependency
- Critical coupling làm feature hiện tại không thể hoạt động

### FIX WHEN TOUCHING MODULE

Ví dụ:

```text
Cart → ProductRepository
```

Nếu chưa chạm integration thì có thể ghi technical debt.

Khi phát triển Cart/Product:

```text
Cart
 ↓
ProductInternalService
 ↓
ProductInternalData
```

thì refactor dần.

### POSTPONE

- Rename nhỏ
- Package cleanup
- Pattern chưa cần
- Generic abstraction
- Optimization chưa có evidence
- Microservices
- Infrastructure complexity

---

## 24. AI BEHAVIOR RULES

AI phải:

- Inspect before modifying.
- Explain before implementing.
- Ask for approval before significant architecture changes.
- Reuse existing code.
- Prefer simplest valid solution.
- Keep changes scoped.
- Test changes.
- Report deviations.
- Keep PROJECT_CONTEXT.md synchronized.
- Preserve existing working behavior.

AI không được:

- Rewrite project.
- Guess missing business requirements.
- Invent APIs.
- Invent completed features.
- Add dependencies without reason.
- Introduce microservices prematurely.
- Add Redis/WebSocket/Docker complexity without requirement.
- Refactor unrelated modules.
- Hide deviations.

---

## 25. DEFINITION OF DONE

```text
[ ] Business flow understood
[ ] Backend design approved
[ ] Frontend design approved
[ ] Backend implemented
[ ] Backend verified
[ ] Frontend implemented
[ ] Frontend verified
[ ] E2E verified
[ ] Architecture reviewed
[ ] Technical debt recorded
[ ] PROJECT_CONTEXT.md synchronized
```

---

## 26. QUICK COMMANDS

### Bắt đầu

```text
Read:
@docs/ARCHITECTURE.md
@docs/PROJECT_CONTEXT.md
@docs/MODULE_DEVELOPMENT_WORKFLOW.md
@docs/AI_PROMPTS.md

Start:
AI_PROMPTS.md → CONTEXT CHECK

Module: [MODULE]
Feature: [FEATURE]
Use Case: [USE CASE]

Existing project.
Do not rebuild.
Do not implement yet.
```

### Tiếp tục

```text
AI_PROMPTS.md → DISCOVER
AI_PROMPTS.md → UNDERSTAND
AI_PROMPTS.md → DESIGN BACKEND
AI_PROMPTS.md → DESIGN FRONTEND
AI_PROMPTS.md → DESIGN REVIEW
```

### Duyệt

```text
APPROVED.
Proceed with the approved design.
```

### Implement + Verify

```text
AI_PROMPTS.md → IMPLEMENT BACKEND
AI_PROMPTS.md → VERIFY BACKEND
AI_PROMPTS.md → IMPLEMENT FRONTEND
AI_PROMPTS.md → VERIFY FRONTEND
AI_PROMPTS.md → END-TO-END VERIFICATION
AI_PROMPTS.md → ARCHITECTURE REVIEW
```

### Kết thúc feature

```text
AI_PROMPTS.md → CHECKPOINT
AI_PROMPTS.md → UPDATE PROJECT_CONTEXT
```

---

## 27. ONE-LINE PRINCIPLE

```text
Không xây lại
→ hiểu code hiện tại
→ thiết kế trước
→ approve
→ xây một vertical slice Backend + Frontend
→ verify
→ review architecture
→ checkpoint
→ đồng bộ PROJECT_CONTEXT
→ lặp lại.
```
