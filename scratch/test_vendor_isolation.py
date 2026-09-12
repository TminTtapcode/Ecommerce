import urllib.request
import json
import sys

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
    with urllib.request.urlopen(req) as response:
        res_body = response.read().decode("utf-8")
        return response.status, json.loads(res_body) if res_body else {}

def test_vendor2_isolation():
    # Login as Vendor 2
    status, res = request("POST", "/api/v1/auth/login", data={"email": "vendor2@fashionhub.vn", "password": "password123"})
    assert status == 200
    token2 = res["data"]["token"]

    # Get Shop 2
    status, res = request("GET", "/api/v1/shops/my-shop", token=token2)
    assert status == 200
    shop = res["data"]
    print(f"Vendor 2 shop: ID={shop['id']}, Name='{shop['name']}'")
    assert "Fashion" in shop["name"]

    # Get Vendor 2 products
    status, res = request("GET", "/api/v1/vendor/products", token=token2)
    assert status == 200
    products = res["data"]["content"]
    print(f"Vendor 2 products count: {len(products)}")
    for p in products:
        assert p["shopId"] == shop["id"], f"Product {p['id']} does not belong to Shop {shop['id']}"

    print("Vendor isolation verified: Vendor 2 only sees their own products!")

if __name__ == "__main__":
    test_vendor2_isolation()
