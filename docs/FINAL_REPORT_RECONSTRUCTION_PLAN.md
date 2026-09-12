# FINAL_REPORT_RECONSTRUCTION_PLAN.md
# Final Report Reconstruction Plan

This plan maps the Official Report Template to the verified current system. It dictates the writing strategy for the final graduation report.

## CHAPTER 1: TỔNG QUAN ĐỀ TÀI (INTRODUCTION)
| Section | Content to Write | Source Evidence | Resources Req. | Status | Priority |
|---|---|---|---|---|---|
| 1.1 Intro | Overview of Multi-Vendor E-Commerce | `PROJECT_CONTEXT.md` | - | Ready to Write | High |
| 1.2 Problem | Buyer, Vendor, Admin needs | `PROJECT_CONTEXT.md` | - | Ready to Write | High |
| 1.3 Objectives | Build a full marketplace, Modular Monolith | `ARCHITECTURE.md` | - | Ready to Write | High |
| 1.4 Scope | In-scope: Auth, Order, Payment, Chat. Out: Mobile, Redis, Docker | `REPORT_RECONSTRUCTION_AUDIT.md` | - | Ready to Write | High |
| 1.5 Methodology | Agile, Iterative phases 1-9 | `MODULE_DEVELOPMENT_WORKFLOW.md` | - | Ready to Write | High |
| 1.6 Structure | Summary of Chapters 1-4 | N/A | - | Write Last | Low |

## CHAPTER 2: CƠ SỞ LÝ THUYẾT (THEORETICAL FOUNDATION)
| Section | Content to Write | Source Evidence | Resources Req. | Status | Priority |
|---|---|---|---|---|---|
| 2.1 Concepts | Modular Monolith, JWT, REST, STOMP | Source architecture | - | Ready to Write | Medium |
| 2.2 Methods | State Machine, Proportional Split, Idempotency | `OrderStatus`, `VoucherService`, `PaymentRepo` | - | Ready to Write | Medium |
| 2.3 Tech | Spring Boot, React, Vite, MySQL, VNPay, Cloudinary | `pom.xml`, `package.json` | - | Ready to Write | Medium |

## CHAPTER 3: HỆ THỐNG (SYSTEM IMPLEMENTATION)
| Section | Content to Write | Source Evidence | Resources Req. | Status | Priority |
|---|---|---|---|---|---|
| 3.1 Intro | System overview and actors | `PROJECT_CONTEXT.md` | - | Ready to Write | High |
| 3.2 Architecture | Modular Monolith layout | Modules: `order`, `product`, `shop`, etc. | DIAGRAM-01 | Ready to Write | High |
| 3.3 Use Case | Buyer, Vendor, Admin capabilities | Controllers & Frontend routing | DIAGRAM-02 | Ready to Write | High |
| 3.4.1 Class Dia. | Domain Entities mapping | `src/.../entity/*.java` | DIAGRAM-03 (ERD) | Ready to Write | High |
| 3.4.2 Sequence | 1. Checkout + VNPay IPN, 2. Chat STOMP | `OrderServiceImpl`, `VnPayIpnController` | DIAGRAM-04, 05 | Ready to Write | High |
| 3.4.4 State | Order Lifecycle | `OrderStatus` enum | DIAGRAM-06 | Ready to Write | High |
| 3.5 Database | Exact 22 tables mapping | `REPORT_DATABASE_AUDIT.md` | - | Ready to Write | High |
| 3.6 UI Design | Screen-by-screen breakdown | `frontend/src/pages/` | SCREENSHOTS 01-20 | Blocked by UI capture | High |
| 3.7 Deployment | Local dev setup (No Docker) | `application.yml`, Vite configs | - | Ready to Write | Medium |

## CHAPTER 4: KẾT QUẢ, ĐÁNH GIÁ VÀ KẾT LUẬN (CONCLUSION)
| Section | Content to Write | Source Evidence | Resources Req. | Status | Priority |
|---|---|---|---|---|---|
| 4.1 Results | Verified implemented features | `REPORT_RECONSTRUCTION_AUDIT.md` | - | Ready to Write | High |
| 4.2 Limitations| No Redis, No Refund automation, No Docker | `REPORT_RECONSTRUCTION_AUDIT.md` | - | Ready to Write | High |
| 4.3 Future | Add Mobile App, Caching, K8s Deployment | `PROJECT_CONTEXT.md` Phase 9 | - | Ready to Write | Low |

---

## SCREENSHOT CHECKLIST
*Note: Only features verified as IMPLEMENTED are listed.*
- **IMG-01:** Home / Product list (`/products`)
- **IMG-02:** Product detail & gallery (`/products/:id`)
- **IMG-03:** Cart multi-vendor items (`/cart`)
- **IMG-04:** Checkout page (Order splitting visible)
- **IMG-05:** VNPay Sandbox gateway redirect
- **IMG-06:** Payment Result CONFIRMED (`/payment-result`)
- **IMG-07:** Order history & Status tracking (`/orders`)
- **IMG-08:** Product Review form (After DELIVERED)
- **IMG-09:** Vendor Dashboard (Analytics)
- **IMG-10:** Vendor Product Management (Cloudinary upload)
- **IMG-11:** Vendor Order Management (Status mutation)
- **IMG-12:** Vendor Voucher Management
- **IMG-13:** Admin Shop Approval list (PENDING -> ACTIVE)
- **IMG-14:** Realtime Chat Widget (Buyer & Vendor communication)
- **IMG-15:** Notification Dropdown

## DIAGRAM CHECKLIST
- **DIAGRAM-01:** System Architecture (Client -> Spring Boot -> MySQL/Cloudinary/VNPay)
- **DIAGRAM-02:** Use Case Diagram (Actors: Buyer, Vendor, Admin)
- **DIAGRAM-03:** Entity Relationship Diagram (ERD) - 22 Tables
- **DIAGRAM-04:** Sequence: Multi-Vendor Checkout to VNPay Payment
- **DIAGRAM-05:** Sequence: VNPay IPN Webhook and Pessimistic Locking
- **DIAGRAM-06:** Sequence: Realtime STOMP Chat messaging
- **DIAGRAM-07:** State Machine: Order Lifecycle

## MISSING EVIDENCE / ACTION REQUIRED
1. **Screenshots:** The AI cannot render React components or open localhost visually. The user MUST provide the 15 screenshots above.
2. **Diagrams:** UML Diagrams need to be generated (e.g., using Mermaid.js or visual tools) to place into the Word document.
3. **F10 VNPay Refund (Sandbox):** Backend code exists, but E2E verification evidence (Logs of a successful refund) is missing. This remains UNVERIFIED.

## RECOMMENDED WRITING ORDER
1. Generate **Chapter 1 & 2** (Text heavy, requires no visual evidence).
2. Generate **Chapter 3: 3.1 - 3.5** (Architecture, Database, Use Cases). *Generate Mermaid syntax for diagrams during this step.*
3. User manually captures **Screenshots 01-15** while running the application.
4. Generate **Chapter 3.6 - 3.7** (UI & Results) embedding the user's screenshots.
5. Generate **Chapter 4** (Conclusion & Limitations).
