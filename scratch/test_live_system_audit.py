import urllib.request
import urllib.parse
import json
import ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

BASE_URL = "http://localhost:8080/api/v1"

def api_call(method, path, data=None, token=None):
    url = BASE_URL + path
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    
    req_data = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(url, data=req_data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            res_body = response.read().decode("utf-8")
            return response.getcode(), json.loads(res_body) if res_body else {}
    except urllib.error.HTTPError as e:
        err_body = e.read().decode("utf-8")
        try:
            parsed = json.loads(err_body)
        except:
            parsed = {"raw": err_body}
        return e.code, parsed
    except Exception as e:
        return 0, {"error": str(e)}

print("=== 1. TESTING LOGIN & AUTH TOKENS ===")
code, admin_res = api_call("POST", "/auth/login", {"email": "admin@ecommerce.com", "password": "password123"})
admin_token = admin_res.get("data", {}).get("token")
print(f"Admin login: HTTP {code}, Token present: {bool(admin_token)}")

code, v1_res = api_call("POST", "/auth/login", {"email": "vendor1@techstore.com", "password": "password123"})
v1_token = v1_res.get("data", {}).get("token")
print(f"Vendor 1 login: HTTP {code}, Token present: {bool(v1_token)}")

code, v2_res = api_call("POST", "/auth/login", {"email": "vendor2@fashionhub.com", "password": "password123"})
v2_token = v2_res.get("data", {}).get("token")
print(f"Vendor 2 login: HTTP {code}, Token present: {bool(v2_token)}")

code, c1_res = api_call("POST", "/auth/login", {"email": "customer1@gmail.com", "password": "password123"})
c1_token = c1_res.get("data", {}).get("token")
print(f"Customer 1 login: HTTP {code}, Token present: {bool(c1_token)}")

code, c2_res = api_call("POST", "/auth/login", {"email": "customer2@gmail.com", "password": "password123"})
c2_token = c2_res.get("data", {}).get("token")
print(f"Customer 2 login: HTTP {code}, Token present: {bool(c2_token)}")

print("\n=== 2. TESTING ROLE PERMISSIONS (RBAC) ===")
# Customer tries Admin endpoint
code, res = api_call("GET", "/admin/shops", token=c1_token)
print(f"Customer -> /admin/shops: HTTP {code} (Expect 403)")

# Customer tries Vendor endpoint
code, res = api_call("GET", "/vendor/products", token=c1_token)
print(f"Customer -> /vendor/products: HTTP {code} (Expect 403)")

# Vendor tries Admin endpoint
code, res = api_call("GET", "/admin/shops", token=v1_token)
print(f"Vendor1 -> /admin/shops: HTTP {code} (Expect 403)")

# Admin accesses Admin endpoint
code, res = api_call("GET", "/admin/shops", token=admin_token)
print(f"Admin -> /admin/shops: HTTP {code} (Expect 200)")

# Admin accesses Admin orders
code, res = api_call("GET", "/admin/orders", token=admin_token)
print(f"Admin -> /admin/orders: HTTP {code} (Expect 200)")

# Admin accesses Admin vouchers
code, res = api_call("GET", "/admin/vouchers", token=admin_token)
print(f"Admin -> /admin/vouchers: HTTP {code} (Expect 200)")

# Vendor1 accesses Vendor products
code, res = api_call("GET", "/vendor/products", token=v1_token)
print(f"Vendor1 -> /vendor/products: HTTP {code} (Expect 200)")

# Vendor1 accesses Vendor vouchers
code, res = api_call("GET", "/vouchers/my-shop", token=v1_token)
print(f"Vendor1 -> /vouchers/my-shop: HTTP {code} (Expect 200)")

print("\n=== 3. TESTING VENDOR ISOLATION & IDOR ===")
# List Vendor2 products
code, v2_prods = api_call("GET", "/vendor/products", token=v2_token)
v2_items = v2_prods.get("data", {}).get("content", [])
print(f"Vendor2 product count: {len(v2_items)}")

if v2_items:
    target_id = v2_items[0]["id"]
    print(f"Vendor1 trying to update Vendor2's product #{target_id}...")
    code, update_res = api_call("PUT", f"/vendor/products/{target_id}", {
        "name": "Hacked by Vendor1",
        "description": "Hacked",
        "price": 1000,
        "stockQuantity": 100
    }, token=v1_token)
    print(f"Vendor1 update Vendor2 product: HTTP {code}, Message: {update_res.get('message')}")

    print(f"Vendor1 trying to delete Vendor2's product #{target_id}...")
    code, del_res = api_call("DELETE", f"/vendor/products/{target_id}", token=v1_token)
    print(f"Vendor1 delete Vendor2 product: HTTP {code}, Message: {del_res.get('message')}")

print("\n=== 4. TESTING VOUCHER SCOPE SECURITY (VOUCHER IDOR/PRIVILEGE ESCALATION) ===")
# Can Vendor1 create a SYSTEM voucher?
code, sys_v_res = api_call("POST", "/vouchers", {
    "code": "VENDOR1_SYS_HACK",
    "scope": "SYSTEM",
    "type": "FIXED_AMOUNT",
    "discountValue": 50000,
    "minOrderValue": 100000,
    "startDate": "2026-09-01T00:00:00",
    "endDate": "2026-12-31T23:59:59",
    "usageLimit": 100
}, token=v1_token)
print(f"Vendor1 creating SYSTEM voucher: HTTP {code}, Result: {sys_v_res}")

print("\n=== 5. TESTING CUSTOMER ORDER IDOR ===")
# Get Customer 1's orders
code, c1_orders = api_call("GET", "/orders", token=c1_token)
c1_order_list = c1_orders.get("data", {}).get("content", [])
print(f"Customer 1 order count: {len(c1_order_list)}")

if c1_order_list:
    target_order_id = c1_order_list[0]["id"]
    print(f"Customer 2 trying to view Customer 1's order #{target_order_id}...")
    code, view_res = api_call("GET", f"/orders/{target_order_id}", token=c2_token)
    print(f"Customer 2 view Customer 1 order: HTTP {code}, Message: {view_res.get('message')}")

