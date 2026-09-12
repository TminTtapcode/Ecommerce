# AI Prompts

> Bộ prompt chuẩn để sử dụng với Gemini / Antigravity cho Enterprise Ecommerce.
>
> Quy tắc:
> - Đây là project EXISTING, không xây lại từ đầu.
> - AI phải đọc context trước khi làm.
> - Không implement ngay khi chưa hiểu và chưa được approve thiết kế.
> - Backend và Frontend được phát triển song song theo từng Vertical Slice.
> - Sau mỗi feature hoàn thành, AI phải đề xuất cập nhật PROJECT_CONTEXT.md.

---

# 0. BASE CONTEXT

Dùng prompt này khi bắt đầu một phiên làm việc mới.

```text
Đọc các file context sau trước khi làm bất kỳ thay đổi nào:

@docs/ARCHITECTURE.md
@docs/PROJECT_CONTEXT.md
@docs/MODULE_DEVELOPMENT_WORKFLOW.md
@docs/AI_PROMPTS.md
@docs/AI_DEVELOPMENT_WORKFLOW.md

Đây là một EXISTING PROJECT.

Không được tự ý xây lại project.
Không được xóa hoặc viết lại code đang hoạt động nếu chưa có lý do rõ ràng.

Trước khi implement:
1. Đọc architecture.
2. Đọc project context.
3. Inspect code hiện tại.
4. Hiểu các module liên quan.
5. Hiểu backend và frontend hiện tại.
6. Xác định những gì đã tồn tại.
7. Xác định những gì còn thiếu.

Luôn ưu tiên:
Inspect → Reuse → Smallest Safe Change → Verify → Incrementally Improve.

Không implement ngay.
1. CONTEXT CHECK

Dùng khi bắt đầu một feature/module.

Start: CONTEXT CHECK

Module:
[MODULE]

Feature:
[FEATURE]

Use Case:
[USE CASE]

Hãy inspect code hiện tại liên quan đến feature này.

KHÔNG sửa code.

Hãy báo cáo:

1. Project hiện tại đang ở trạng thái nào?
2. Module này hiện đã có những gì?
3. Backend hiện tại đã có những gì?
4. Frontend hiện tại đã có những gì?
5. Database/Entity hiện tại đã có những gì?
6. API hiện tại đã có những gì?
7. Những module nào liên quan?
8. Có cross-module dependency nào không?
9. Những phần nào có thể reuse?
10. Những phần nào còn thiếu?
11. Có technical debt hoặc architecture violation nào liên quan không?
12. Feature này nên tiếp tục từ code hiện tại như thế nào?

Không implement.
2. DISCOVER
Start: DISCOVER

Hãy inspect sâu hơn feature:

[FEATURE]

Tìm và phân tích:

- Entity liên quan
- Repository
- Service
- Controller
- DTO
- Exception
- Security
- Configuration
- Test
- Frontend page
- Component
- API client
- TypeScript type
- State management
- Routing
- Các module liên quan

Mục tiêu:

1. Tìm code có thể reuse.
2. Tìm pattern project đang sử dụng.
3. Tìm dependency hiện tại.
4. Tìm vấn đề có thể ảnh hưởng feature.
5. Tránh tạo duplicate abstraction.

Không sửa code.
3. UNDERSTAND
Start: UNDERSTAND

Trước khi thiết kế, hãy giải thích feature này dựa trên code thực tế.

Feature:
[FEATURE]

Giải thích:

1. Business purpose
2. User flow
3. Frontend flow
4. HTTP request flow
5. Controller xử lý gì?
6. Service xử lý gì?
7. Repository xử lý gì?
8. Database thay đổi gì?
9. Validation
10. Business rules
11. Exception cases
12. Authentication
13. Authorization
14. Cross-module communication
15. Transaction boundary
16. Response trả về frontend như thế nào?

Hãy giải thích sao cho tôi có thể hiểu được toàn bộ request lifecycle.

Không implement.
4. DESIGN BACKEND
Start: DESIGN BACKEND

Hãy thiết kế backend cho:

Module:
[MODULE]

Feature:
[FEATURE]

Dựa trên architecture và code hiện tại.

Đề xuất:

1. API endpoints
2. HTTP methods
3. Request DTO
4. Response DTO
5. Entity changes
6. Repository changes
7. Service changes
8. Validation
9. Business rules
10. Exception
11. Transaction boundary
12. Authentication
13. Authorization
14. Cross-module communication
15. Internal DTO nếu cần
16. Files cần tạo
17. Files cần sửa
18. Files không nên sửa

Với mỗi thay đổi hãy giải thích:

- Tại sao cần?
- Tại sao chọn cách này?
- Có cách đơn giản hơn không?
- Có ảnh hưởng module khác không?

Ưu tiên smallest valid change.

Không implement.
5. DESIGN FRONTEND
Start: DESIGN FRONTEND

Hãy thiết kế frontend cho feature:

[FEATURE]

Dựa trên backend API đã đề xuất.

Đề xuất:

1. Page cần tạo/sửa
2. Component
3. TypeScript types
4. API client
5. Form
6. Form validation
7. Loading state
8. Error state
9. Success state
10. Empty state nếu cần
11. State management
12. Routing
13. Reusable components

Kiểm tra frontend có khớp hoàn toàn với backend API contract không.

Ưu tiên reuse component và pattern hiện tại.

Không implement.
6. DESIGN REVIEW

Đây là bước rất quan trọng trước khi cho AI code.

Start: DESIGN REVIEW

Review toàn bộ design Backend + Frontend của feature:

[FEATURE]

Kiểm tra:

### Architecture
- Module boundary
- Module ownership
- Dependency direction
- Circular dependency
- Cross-module communication

### Backend
- API design
- DTO
- Entity
- Repository
- Service
- Validation
- Exception
- Transaction
- Security

### Frontend
- Component structure
- API integration
- TypeScript types
- Validation
- Loading/error/success states
- UX

### Database
- Relationship
- Constraint
- Data consistency

### Maintainability
- Duplication
- Overengineering
- Unnecessary abstraction
- Unnecessary dependency

Phân loại vấn đề:

MUST FIX
SHOULD IMPROVE
CAN POSTPONE

Đề xuất design cuối cùng.

Không implement.
7. APPROVAL

Sau khi tôi đã xem design, tôi sẽ gửi:

APPROVED.

Proceed with the approved design.

Do not introduce unrelated changes.
Do not change architecture without asking.

AI chỉ được bắt đầu implementation sau bước này.

8. IMPLEMENT BACKEND
Start: IMPLEMENT BACKEND

Implement ONLY backend changes đã được approve.

Feature:
[FEATURE]

Rules:

1. Không rewrite project.
2. Không sửa code unrelated.
3. Reuse code hiện tại khi phù hợp.
4. Follow ARCHITECTURE.md.
5. Respect module boundaries.
6. Không expose Entity trực tiếp qua REST API.
7. Sử dụng Request DTO / Response DTO.
8. Validation đúng boundary.
9. Business exception rõ ràng.
10. Transaction có chủ đích.
11. Không thêm abstraction không cần thiết.
12. Không thêm dependency nếu không cần.
13. Không thay đổi architecture nếu chưa được approve.

Sau khi implement:

1. Liệt kê files đã thay đổi.
2. Giải thích từng thay đổi.
3. API mới/thay đổi.
4. Database thay đổi.
5. Cross-module dependency thay đổi.
6. Có deviation nào so với design không?
7. Nếu có deviation, giải thích lý do.

Không tự ý implement frontend ở bước này.
9. VERIFY BACKEND
Start: VERIFY BACKEND

Hãy verify backend feature vừa implement.

Kiểm tra:

1. Compile
2. Build
3. Application startup
4. API
5. HTTP status
6. Request validation
7. Response
8. Exception handling
9. Authentication
10. Authorization
11. Database persistence
12. Transaction
13. Cross-module communication
14. Regression

Nếu có lỗi:

- Root cause
- File
- Line
- Vì sao lỗi xảy ra
- Smallest safe fix

Phân loại:

PASS
FAIL
WARNING

Không thực hiện broad refactoring.
10. IMPLEMENT FRONTEND

Chỉ thực hiện sau khi backend đã verify.

Start: IMPLEMENT FRONTEND

Implement frontend cho feature:

[FEATURE]

Backend API đã được verify.

Rules:

1. Dùng đúng backend API contract.
2. Không tự đoán API.
3. Reuse component hiện tại.
4. Reuse API client pattern hiện tại.
5. TypeScript types phải khớp backend response.
6. Có loading state.
7. Có error state.
8. Có success state.
9. Validation phù hợp.
10. Không sửa unrelated pages.
11. Không thêm library nếu chưa cần.

Sau implementation:

1. Liệt kê files thay đổi.
2. Giải thích từng thay đổi.
3. API đã sử dụng.
4. Component đã tạo/sửa.
5. Deviation nếu có.
11. VERIFY FRONTEND
Start: VERIFY FRONTEND

Verify frontend feature:

[FEATURE]

Kiểm tra:

1. Build
2. TypeScript
3. API integration
4. Request payload
5. Response mapping
6. Form validation
7. Loading state
8. Error state
9. Success state
10. Routing
11. UI behavior
12. Regression

Report:

PASS
FAIL
WARNING

Nếu FAIL:

- Root cause
- File
- Smallest safe fix

Không broad refactoring.
12. END-TO-END VERIFICATION
Start: END-TO-END VERIFICATION

Hãy kiểm tra toàn bộ feature từ user đến database.

Trace:

User
→ Frontend
→ API Client
→ HTTP Request
→ Controller
→ Service
→ Repository / Internal Contract
→ Database
→ Response
→ Frontend
→ UI

Kiểm tra:

1. Request
2. Authentication
3. Authorization
4. Validation
5. Business logic
6. Database
7. Transaction
8. Response
9. HTTP status
10. Frontend state
11. Error handling
12. User-visible result

Test:

- Happy path
- Validation failure
- Unauthorized
- Forbidden nếu có
- Resource not found
- Business failure
- Database failure nếu phù hợp

Kết luận:

PASS / FAIL / WARNING
13. ARCHITECTURE REVIEW
Start: ARCHITECTURE REVIEW

Review ONLY phần code liên quan feature vừa hoàn thành.

Đối chiếu:

@docs/ARCHITECTURE.md

Kiểm tra:

1. Module ownership
2. Module boundary
3. Cross-module communication
4. Repository coupling
5. Entity coupling
6. DTO usage
7. Dependency direction
8. Circular dependency
9. Transaction
10. Security
11. API consistency
12. Unnecessary abstraction
13. Unnecessary complexity

Phân loại:

FIX NOW
FIX WHEN TOUCHING MODULE
TECHNICAL DEBT
NO ACTION

Không tự động refactor toàn project.

Nếu phát hiện architecture violation nhưng không ảnh hưởng feature hiện tại, hãy ghi nhận thành technical debt thay vì tự ý sửa.
14. CHECKPOINT
Start: CHECKPOINT

Feature:
[FEATURE]

Tạo completion report.

Bao gồm:

1. Feature đã hoàn thành
2. Backend changes
3. Frontend changes
4. Database changes
5. API added/changed
6. Authentication/authorization
7. Cross-module communication
8. Verification result
9. Architecture review
10. Technical debt
11. Known limitations
12. Những gì chưa hoàn thành
13. Feature tiếp theo nên làm

Không sửa code.
15. UPDATE PROJECT_CONTEXT

Đây là prompt quan trọng nhất để AI tự giúp cập nhật context.

Start: UPDATE PROJECT_CONTEXT

Hãy đồng bộ:

@docs/PROJECT_CONTEXT.md

với trạng thái THỰC TẾ đã được verify của project.

Không được đoán.

Chỉ cập nhật những phần thực sự thay đổi.

Kiểm tra:

1. Project status
2. Module status
3. Current development position
4. Completed features
5. Backend progress
6. Frontend progress
7. Database progress
8. Known issues
9. Technical debt
10. Architecture decisions
11. Next steps

Rules:

- Không invent progress.
- Không đánh dấu completed nếu chưa verify.
- Không nói architecture compliant nếu code vẫn vi phạm.
- Nếu có gap giữa ARCHITECTURE.md và code thực tế, ghi rõ gap.
- Không sửa ARCHITECTURE.md.
- Không sửa source code.
- Không thay đổi các phần PROJECT_CONTEXT không liên quan.

TRƯỚC TIÊN KHÔNG ĐƯỢC SỬA FILE.

Hãy hiển thị:

SECTION:
Old:
New:
Reason:

Sau đó chờ tôi APPROVE.

Sau khi AI đưa proposal:

APPROVED.

Apply only the proposed changes to:

@docs/PROJECT_CONTEXT.md

Do not modify:
- source code
- ARCHITECTURE.md
- MODULE_DEVELOPMENT_WORKFLOW.md
- AI_PROMPTS.md
16. START NEXT FEATURE

Sau khi context đã được cập nhật:

Read again:

@docs/ARCHITECTURE.md
@docs/PROJECT_CONTEXT.md
@docs/MODULE_DEVELOPMENT_WORKFLOW.md
@docs/AI_PROMPTS.md

Continue from the CURRENT project state.

Do not rebuild.

Start:

CONTEXT CHECK

Module:
[MODULE]

Feature:
[FEATURE]

Use Case:
[USE CASE]

Do not implement yet.
17. FULL WORKFLOW

Mỗi feature/module nên chạy theo thứ tự:

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
APPROVAL
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
UPDATE PROJECT_CONTEXT
        ↓
APPROVAL
        ↓
NEXT FEATURE
18. ONE-SHOT START PROMPT

Nếu muốn bắt đầu nhanh một feature mới, dùng:

Đọc:

@docs/ARCHITECTURE.md
@docs/PROJECT_CONTEXT.md
@docs/MODULE_DEVELOPMENT_WORKFLOW.md
@docs/AI_PROMPTS.md
@docs/AI_DEVELOPMENT_WORKFLOW.md

Đây là existing project.

Không rebuild.
Không implement ngay.

Feature:
[FEATURE]

Module:
[MODULE]

Use Case:
[USE CASE]

Hãy bắt đầu bằng:

CONTEXT CHECK

Sau đó thực hiện:

DISCOVER
→ UNDERSTAND
→ DESIGN BACKEND
→ DESIGN FRONTEND
→ DESIGN REVIEW

Dừng lại để tôi review và approve trước khi implement.
19. GOLDEN RULES
1. Understand before coding.

2. Inspect existing code before designing.

3. Design Backend + Frontend together.

4. Implement one vertical slice at a time.

5. Backend must be verified before frontend integration.

6. Never rebuild working code without a strong reason.

7. Never modify unrelated modules.

8. Prefer reuse over duplication.

9. Prefer simple solutions over overengineering.

10. Architecture violations should be visible, not hidden.

11. PROJECT_CONTEXT describes reality, not the desired future.

12. AI proposes important changes; user approves them.

13. After every completed feature:
   Verify → Review → Checkpoint → Update Context.

14. If uncertain, stop and explain instead of guessing.

### Cách dùng thực tế

Bạn **không cần mỗi lần paste toàn bộ file**. Với Antigravity, chỉ cần để 5 file trong project, sau đó dùng:

```text
Read:
@docs/ARCHITECTURE.md
@docs/PROJECT_CONTEXT.md
@docs/MODULE_DEVELOPMENT_WORKFLOW.md
@docs/AI_PROMPTS.md
@docs/AI_DEVELOPMENT_WORKFLOW.md

Start: CONTEXT CHECK

Module: Product
Feature: Product Image Management
Use Case: Admin upload/update/delete product images

Do not implement yet.

