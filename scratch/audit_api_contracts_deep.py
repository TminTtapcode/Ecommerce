import os
import re
import json

backend_dir = r"c:\Users\Dell\IdeaProjects\ecommerce\src\main\java\com\tmt\ecommerce"
frontend_dir = r"c:\Users\Dell\IdeaProjects\ecommerce\frontend\src"

# ==================== 1. PARSE BACKEND CONTROLLERS ====================
controllers = []

for root, dirs, files in os.walk(backend_dir):
    for f in files:
        if f.endswith("Controller.java"):
            fpath = os.path.join(root, f)
            with open(fpath, "r", encoding="utf-8") as file:
                content = file.read()

            # Check if it's a RestController
            if "@RestController" not in content and "@Controller" not in content:
                continue

            # Class-level RequestMapping
            class_mapping_match = re.search(r'@RequestMapping\(\s*(?:value\s*=\s*)?["\']([^"\']+)["\']', content)
            base_path = class_mapping_match.group(1) if class_mapping_match else ""

            # Class PreAuthorize
            class_preauth = re.search(r'@PreAuthorize\(\s*["\']([^"\']+)["\']', content)
            class_auth = class_preauth.group(1) if class_preauth else ""

            # Method mappings
            # Matches @GetMapping, @PostMapping, @PutMapping, @DeleteMapping, @PatchMapping, @RequestMapping
            method_pattern = r'(@(Get|Post|Put|Delete|Patch|Request)Mapping(?:\([^)]*\))?)\s*(?:@PreAuthorize\(\s*["\']([^"\']+)["\']\s*\))?\s*(?:public\s+)?([^\s<]+(?:<[^>]+>)?)\s+([a-zA-Z0-9_]+)\s*\(([^)]*)\)'
            
            # Use more robust regex for annotations
            for m in re.finditer(r'(@(Get|Post|Put|Delete|Patch|Request)Mapping(?:\(([^)]*)\))?)\s*(?:@PreAuthorize\(\s*["\']([^"\']+)["\']\s*\))?[\s\S]*?(?:public\s+)([^\s<]+(?:<[^{>]+>)?)\s+([a-zA-Z0-9_]+)\s*\(([^)]*)\)', content):
                full_anno = m.group(1)
                http_verb = m.group(2).upper()
                anno_args = m.group(3) or ""
                preauth = m.group(4) or class_auth
                ret_type = m.group(5)
                method_name = m.group(6)
                params_str = m.group(7)

                # Extract path from anno_args
                path_match = re.search(r'(?:value\s*=\s*|path\s*=\s*)?["\']([^"\']+)["\']', anno_args)
                sub_path = path_match.group(1) if path_match else ""
                
                # Combine base_path + sub_path
                full_path = (base_path.rstrip("/") + "/" + sub_path.lstrip("/")).rstrip("/")
                if not full_path.startswith("/"):
                    full_path = "/" + full_path

                # Request body type
                body_match = re.search(r'@RequestBody\s+(?:@Valid\s+)?([A-Za-z0-9_]+)', params_str)
                body_type = body_match.group(1) if body_match else None

                controllers.append({
                    "file": f,
                    "controller": f.replace(".java", ""),
                    "http_method": "ALL" if http_verb == "REQUEST" else http_verb,
                    "path": full_path,
                    "return_type": ret_type,
                    "method_name": method_name,
                    "preauthorize": preauth,
                    "body_type": body_type
                })

print(f"Parsed {len(controllers)} backend controller endpoints.")

# ==================== 2. PARSE FRONTEND APIS ====================
frontend_apis = []

for root, dirs, files in os.walk(frontend_dir):
    for f in files:
        if f.endswith((".ts", ".tsx")):
            fpath = os.path.join(root, f)
            with open(fpath, "r", encoding="utf-8") as file:
                content = file.read()

            # Find apiClient calls: apiClient.get, apiClient.post, apiClient.put, apiClient.delete, apiClient.patch
            # Also direct axios or fetch if any
            for m in re.finditer(r'apiClient\.(get|post|put|delete|patch)(?:<([^>]+)>)?\(\s*(?:`([^`]+)`|["\']([^"\']+)["\'])', content):
                method = m.group(1).upper()
                generic_type = m.group(2)
                path = m.group(3) or m.group(4)

                frontend_apis.append({
                    "file": f,
                    "path_in_code": path,
                    "http_method": method,
                    "generic_type": generic_type
                })

print(f"Parsed {len(frontend_apis)} frontend API calls.")

# Save both to scratch
with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\backend_endpoints_parsed.json", "w", encoding="utf-8") as f:
    json.dump(controllers, f, indent=2)

with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\frontend_apis_parsed.json", "w", encoding="utf-8") as f:
    json.dump(frontend_apis, f, indent=2)

# ==================== 3. COMPARE FRONTEND -> BACKEND ====================
def normalize_fe_path(p):
    # Remove query string
    p = p.split("?")[0]
    # Replace template expressions: ${id} or ${...} with {var}
    p = re.sub(r'\$\{[^}]+\}', '{id}', p)
    # Remove trailing slash
    return p.rstrip("/")

def normalize_be_path(p):
    # Normalize path variables like {productId} to {id}
    p = re.sub(r'\{[^}]+\}', '{id}', p)
    return p.rstrip("/")

matched = []
unmatched_fe = []

for fe in frontend_apis:
    fe_norm = normalize_fe_path(fe["path_in_code"])
    found = False
    for be in controllers:
        be_norm = normalize_be_path(be["path"])
        if fe["http_method"] == be["http_method"] or be["http_method"] == "ALL":
            if fe_norm == be_norm:
                matched.append({"fe": fe, "be": be})
                found = True
                break
    if not found:
        unmatched_fe.append(fe)

print(f"\n=== MATCHED API CALLS: {len(matched)} ===")
print(f"=== UNMATCHED FRONTEND CALLS: {len(unmatched_fe)} ===")
for u in unmatched_fe:
    print(f"  [{u['http_method']}] {u['path_in_code']} (in {u['file']})")
