import os
import re

frontend_src = r"c:\Users\Dell\IdeaProjects\ecommerce\frontend\src"

# Read App.tsx
with open(os.path.join(frontend_src, "App.tsx"), "r", encoding="utf-8") as f:
    app_text = f.read()

# Routes to resolve accurately
# 1. Top-level public routes
# /login, /register, /, /products, /products/:id, /cart, /checkout, /payment-result, /orders, /orders/:id, /profile, /chat-test
# 2. Vendor routes
# /vendor/products, /vendor/products/new, /vendor/products/:id/edit, /vendor/orders, /vendor/vouchers, /vendor (redirect), /seller (redirect)
# 3. Admin routes (nested under /admin)
# /admin (index), /admin/products, /admin/products/create, /admin/products/:id/edit, /admin/shop, /admin/orders, /admin/orders/:id, /admin/vouchers

routes_map = {
    "/login": {"role": "PUBLIC", "element": "LoginPage"},
    "/register": {"role": "PUBLIC", "element": "RegisterPage"},
    "/": {"role": "PUBLIC", "element": "Redirect /products"},
    "/products": {"role": "PUBLIC", "element": "ProductListPage"},
    "/products/:id": {"role": "PUBLIC", "element": "ProductDetailPage"},
    "/cart": {"role": "CUSTOMER", "element": "CartPage"},
    "/checkout": {"role": "CUSTOMER", "element": "CheckoutPage"},
    "/payment-result": {"role": "CUSTOMER", "element": "PaymentResultPage"},
    "/orders": {"role": "CUSTOMER", "element": "OrderHistoryPage"},
    "/orders/:id": {"role": "CUSTOMER", "element": "OrderDetailPage"},
    "/profile": {"role": "CUSTOMER", "element": "ProfilePage"},
    "/chat-test": {"role": "CUSTOMER", "element": "ChatTestPage"},
    "/vendor/products": {"role": "VENDOR", "element": "VendorProductListPage"},
    "/vendor/products/new": {"role": "VENDOR", "element": "VendorProductFormPage"},
    "/vendor/products/:id/edit": {"role": "VENDOR", "element": "VendorProductFormPage"},
    "/vendor/orders": {"role": "VENDOR", "element": "VendorOrderListPage"},
    "/vendor/vouchers": {"role": "VENDOR", "element": "VendorVoucherListPage"},
    "/vendor": {"role": "VENDOR", "element": "Redirect /vendor/products"},
    "/seller": {"role": "VENDOR", "element": "Redirect /vendor/products"},
    "/admin": {"role": "ADMIN", "element": "Dashboard Overview"},
    "/admin/products": {"role": "ADMIN", "element": "AdminProductListPage"},
    "/admin/products/create": {"role": "ADMIN", "element": "AdminProductFormPage"},
    "/admin/products/:id/edit": {"role": "ADMIN", "element": "AdminProductFormPage"},
    "/admin/shop": {"role": "ADMIN", "element": "AdminShopListPage"},
    "/admin/orders": {"role": "ADMIN", "element": "AdminOrderListPage"},
    "/admin/orders/:id": {"role": "ADMIN", "element": "AdminOrderDetailPage"},
    "/admin/vouchers": {"role": "ADMIN", "element": "AdminVoucherListPage"},
}

# Scan all navigation references in frontend
links_found = []
for root, dirs, files in os.walk(frontend_src):
    for file in files:
        if file.endswith((".tsx", ".ts")):
            fpath = os.path.join(root, file)
            with open(fpath, "r", encoding="utf-8") as f:
                content = f.read()
                # <Link to="..." or <NavLink to="..."
                for m in re.finditer(r'to=(?:["\']([^"\']+)["\']|\{`([^`]+)`\})', content):
                    t = m.group(1) or m.group(2)
                    links_found.append({"target": t, "file": file, "path": fpath, "type": "Link/NavLink"})
                # navigate(...)
                for m in re.finditer(r'navigate\((?:`([^`]+)`|["\']([^"\']+)["\'])', content):
                    t = m.group(1) or m.group(2)
                    links_found.append({"target": t, "file": file, "path": fpath, "type": "navigate"})

print(f"=== ANALYZING {len(links_found)} NAVIGATION ACTIONS ===")

# Match links to routes
def matches_route(target, route_pattern):
    # Remove query string & hash
    clean = target.split("?")[0].split("#")[0]
    # Replace parameter patterns like :id or ${id} or ${order.id}
    reg = "^" + re.sub(r':\w+', r'[^/]+', route_pattern) + "$"
    # If target has template expression like /products/${item.id} -> turn ${...} to [^/]+
    target_regex = "^" + re.sub(r'\$\{[^}]+\}', r'[^/]+', clean) + "$"
    if re.match(reg, clean):
        return True
    if re.match(target_regex, route_pattern):
        return True
    # If clean is like /admin/orders/${order.id} and route_pattern is /admin/orders/:id
    t_norm = re.sub(r'\$\{[^}]+\}', r':param', clean)
    r_norm = re.sub(r':\w+', r':param', route_pattern)
    return t_norm == r_norm

unmatched_links = []
matched_routes = set()

for lf in links_found:
    t = lf["target"]
    if t.startswith("http") or t == "#":
        continue
    # Special: navigate(-1) etc handled if string
    matched = False
    for r in routes_map:
        if matches_route(t, r):
            matched = True
            matched_routes.add(r)
            break
    if not matched:
        unmatched_links.append(lf)

print(f"\n--- UNMATCHED / DANGLING LINKS (404 RISK): {len(unmatched_links)} ---")
for ul in unmatched_links:
    print(f"  Target: '{ul['target']}' in {ul['file']} ({ul['type']})")

print(f"\n--- ORPHAN ROUTES (Routes without direct incoming links): ---")
for r in routes_map:
    if r not in matched_routes and r not in ["/", "/vendor", "/seller", "/payment-result"]:
        print(f"  Route '{r}' -> {routes_map[r]['element']} (Role: {routes_map[r]['role']})")

