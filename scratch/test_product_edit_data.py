import urllib.request
import json
import sys

if sys.stdout.encoding != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

BASE_URL = "http://localhost:8080"

def test_product_edit_data():
    print("=== Testing Product Edit Form Data Resolution ===")
    
    # 1. Fetch categories
    req = urllib.request.Request(f"{BASE_URL}/api/v1/categories")
    with urllib.request.urlopen(req) as res:
        categories = json.loads(res.read().decode("utf-8"))["data"]
    print(f"1. Categories loaded: {len(categories)} categories found.")

    # 2. Fetch Product 1 (iPhone 15 Pro Max)
    req = urllib.request.Request(f"{BASE_URL}/api/v1/products/1")
    with urllib.request.urlopen(req) as res:
        product = json.loads(res.read().decode("utf-8"))["data"]

    print("\n2. Product 1 data inspection:")
    print(f"   - Name: {product.get('name')}")
    print(f"   - Description: {product.get('description')}")
    print(f"   - Price: {product.get('price')}")
    print(f"   - Stock: {product.get('stockQuantity')}")
    print(f"   - CategoryId: {product.get('categoryId')}")
    print(f"   - CategoryName: {product.get('categoryName')}")
    print(f"   - Status: {product.get('status')}")
    print(f"   - Variants: {len(product.get('variants', []))} variants")
    print(f"   - Images: {len(product.get('imageResponses', []))} images")

    # Assertions
    assert product.get("name") == "iPhone 15 Pro Max 256GB", "Name mismatch!"
    assert product.get("categoryId") is not None, "categoryId is missing!"
    assert product.get("categoryId") == 1, f"Expected categoryId 1, got {product.get('categoryId')}"
    assert len(product.get("variants", [])) > 0, "Variants missing!"
    assert len(product.get("imageResponses", [])) > 0, "Images missing!"
    assert product.get("status") == "ACTIVE", "Status mismatch!"

    # 3. Test matching category with loaded categories
    matched_cat = next((c for c in categories if c["id"] == product["categoryId"]), None)
    assert matched_cat is not None, "Product categoryId not found in categories list!"
    print(f"\n3. Category successfully resolved: [{matched_cat['id']}] {matched_cat['name']}")

    print("\nSUCCESS: All product information is completely present and ready for form population!")

if __name__ == "__main__":
    test_product_edit_data()
