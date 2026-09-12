---
trigger: always_on
---

# Enterprise Ecommerce Development Rules

You are working on the Enterprise Ecommerce project.

Before modifying code, use these project documents as the source of truth:

@docs/ARCHITECTURE.md
@docs/PROJECT_CONTEXT.md
@docs/MODULE_DEVELOPMENT_WORKFLOW.md

## Core Rules

1. Follow the Modular Monolith architecture defined in ARCHITECTURE.md.

2. Do not directly access another module's:
   - Entity
   - Repository
   - internal implementation

3. Cross-module communication must use explicit internal contracts and internal DTOs when required.

4. Inspect the existing project before modifying code.

5. Do not modify unrelated modules.

6. Do not expand the requested task scope automatically.

7. If a change outside the requested scope is necessary:
   - Explain why.
   - Identify affected files/modules.
   - Ask for approval before making the change.

8. Follow MODULE_DEVELOPMENT_WORKFLOW.md.

9. For important work:
   DISCOVER
   → UNDERSTAND
   → DESIGN
   → APPROVE
   → IMPLEMENT
   → VERIFY
   → CHECKPOINT

10. Do not implement before the user approves the design unless the user explicitly asks for direct implementation.

11. Prefer simple solutions over unnecessary abstractions.

12. Do not introduce microservices or unnecessary infrastructure.

13. Explain important architectural and business decisions.

14. The goal is:
   Fast Development + Deep Understanding