import urllib.request
import urllib.parse
import json
import sys

# Ensure UTF-8 output on Windows console
if sys.stdout.encoding != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

BASE_URL = "http://localhost:8080"

def request(method, path, data=None, token=None):
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    body = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as response:
            res_body = response.read().decode("utf-8")
            return response.status, json.loads(res_body) if res_body else {}
    except urllib.error.HTTPError as e:
        err_body = e.read().decode("utf-8")
        print(f"HTTPError {e.code}: {err_body}")
        raise

def test_vendor_product_crud():
    print("=== 1. Login as Vendor ===")
    login_payload = {
        "email": "vendor1@techzone.vn",
        "password": "password123"
    }
    status, res = request("POST", "/api/v1/auth/login", data=login_payload)
    assert status == 200, f"Login failed: {res}"
    token = res["data"]["token"]
    print("Vendor login successful! Token acquired.")

    print("\n=== 2. Get My Shop ===")
    status, res = request("GET", "/api/v1/shops/my-shop", token=token)
    assert status == 200, f"Get my-shop failed: {res}"
    shop_data = res["data"]
    print(f"Shop ID: {shop_data['id']}, Name: {shop_data['name']}, Status: {shop_data['status']}")

    print("\n=== 3. Get Categories ===")
    status, res = request("GET", "/api/v1/categories")
    assert status == 200, f"Get categories failed: {res}"
    categories = res["data"]
    assert len(categories) > 0, "No categories found!"
    cat_id = categories[0]["id"]
    print(f"Loaded {len(categories)} categories. Using Category ID: {cat_id} ({categories[0]['name']})")

    print("\n=== 4. Get Vendor Products List ===")
    status, res = request("GET", "/api/v1/vendor/products?page=0&size=5", token=token)
    assert status == 200, f"Get vendor products failed: {res}"
    products_page = res["data"]
    print(f"Current Vendor Products Count: {products_page['totalElements']}")

    print("\n=== 5. Create New Product (Vendor) ===")
    import time
    ts = int(time.time() * 1000)
    create_payload = {
        "name": f"Tai nghe Gaming RGB Pro X {ts}",
        "description": "Tai nghe chống ồn chủ động chuyên nghiệp cho game thủ",
        "categoryId": cat_id,
        "price": 1250000,
        "stockQuantity": 50,
        "images": [
            {
                "imageUrl": "https://images.unsplash.com/photo-1505740420928-5e560c06d30e",
                "isThumbnail": True,
                "sortOrder": 0
            }
        ],
        "variants": [
            {
                "sku": f"RGB-PRO-BLK-{ts}",
                "price": 1250000,
                "stockQuantity": 30,
                "attributes": {"color": "Đen"}
            },
            {
                "sku": f"RGB-PRO-WHT-{ts}",
                "price": 1290000,
                "stockQuantity": 20,
                "attributes": {"color": "Trắng"}
            }
        ]
    }
    status, res = request("POST", "/api/v1/vendor/products", data=create_payload, token=token)
    assert status in (200, 201), f"Create product failed: {res}"
    created_product = res["data"]
    product_id = created_product["id"]
    print(f"Product created successfully! ID: {product_id}, Name: {created_product['name']}")

    print("\n=== 6. Update Product (Vendor) ===")
    first_variant_id = created_product["variants"][0]["id"] if created_product.get("variants") else None
    update_payload = {
        "name": "Tai nghe Gaming RGB Pro X (Bản Nâng Cấp)",
        "description": "Bản nâng cấp âm thanh vòm 7.1",
        "categoryId": cat_id,
        "price": 1350000,
        "stockQuantity": 45,
        "images": [
            {
                "imageUrl": "https://images.unsplash.com/photo-1505740420928-5e560c06d30e",
                "isThumbnail": True,
                "sortOrder": 0
            }
        ],
        "variants": [
            {
                "id": first_variant_id,
                "sku": f"RGB-PRO-V2-{product_id}",
                "price": 1350000,
                "stockQuantity": 45,
                "attributes": {"color": "Đen"}
            }
        ]
    }
    status, res = request("PUT", f"/api/v1/vendor/products/{product_id}", data=update_payload, token=token)
    assert status == 200, f"Update product failed: {res}"
    updated_product = res["data"]
    print(f"Product updated! New name: {updated_product['name']}, Price: {updated_product['price']}")

    print("\n=== 7. Verify Product in Vendor List ===")
    encoded_kw = urllib.parse.quote("Bản Nâng Cấp")
    status, res = request("GET", f"/api/v1/vendor/products?keyword={encoded_kw}", token=token)
    assert status == 200, f"Filter vendor products failed: {res}"
    matched = res["data"]["content"]
    assert any(p["id"] == product_id for p in matched), "Created product not found in vendor list!"
    print(f"Successfully found product in vendor search list! Found: {len(matched)}")

    print("\n=== 8. Delete / Hide Product (Vendor) ===")
    status, res = request("DELETE", f"/api/v1/vendor/products/{product_id}", token=token)
    assert status == 200, f"Delete product failed: {res}"
    print(f"Product {product_id} deleted/hidden successfully!")

    print("\nALL VENDOR PRODUCT CRUD TESTS PASSED SUCCESSFULLY!")

if __name__ == "__main__":
    test_vendor_product_crud()
