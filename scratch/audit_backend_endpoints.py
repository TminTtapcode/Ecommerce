import os
import re
import json

base_dir = r"c:\Users\Dell\IdeaProjects\ecommerce\src\main\java\com\tmt\ecommerce"

def remove_comments(text):
    # Remove block comments
    text = re.sub(r'/\*.*?\*/', '', text, flags=re.DOTALL)
    # Remove line comments
    text = re.sub(r'//.*', '', text)
    return text

controllers = []

for root, dirs, files in os.walk(base_dir):
    for f in files:
        if f.endswith("Controller.java"):
            path = os.path.join(root, f)
            with open(path, "r", encoding="utf-8") as file:
                raw_content = file.read()

            content = remove_comments(raw_content)

            # Check if RestController or Controller
            if "@RestController" not in content and "@Controller" not in content:
                continue

            # Class mapping
            base_match = re.search(r'@RequestMapping\s*\(\s*(?:(?:value|path)\s*=\s*)?["\']([^"\']+)["\']', content)
            base_mapping = base_match.group(1) if base_match else ""

            # Class PreAuthorize
            class_preauth = re.search(r'@PreAuthorize\s*\(\s*["\']([^"\']+)["\']', content)
            class_auth = class_preauth.group(1) if class_preauth else ""

            # Find all methods
            # Split methods or scan for @(Get|Post|Put|Delete|Patch|Message)Mapping
            method_chunks = re.finditer(r'@(Get|Post|Put|Delete|Patch|Message|Request)Mapping\s*(\([^)]*\))?', content)
            
            methods = []
            for m in method_chunks:
                verb = m.group(1).upper()
                args = m.group(2) or ""
                
                # Extract path
                path_match = re.search(r'(?:value\s*=\s*|path\s*=\s*)?["\']([^"\']*)["\']', args)
                sub_path = path_match.group(1) if path_match else ""

                full_path = (base_mapping.rstrip("/") + "/" + sub_path.lstrip("/")).rstrip("/")
                if not full_path.startswith("/"):
                    full_path = "/" + full_path

                # Find method declaration following this mapping
                after = content[m.end():m.end()+600]
                
                # PreAuthorize
                preauth_m = re.search(r'@PreAuthorize\s*\(\s*["\']([^"\']+)["\']', after)
                auth_rule = preauth_m.group(1) if preauth_m else class_auth

                # Method name and params
                decl_m = re.search(r'public\s+(?:<[^>]+>\s+)?([A-Za-z0-9_<>,.?\[\]\s]+?)\s+([A-Za-z0-9_]+)\s*\(([^)]*)\)', after)
                if decl_m:
                    ret_type = decl_m.group(1).strip()
                    method_name = decl_m.group(2).strip()
                    params = " ".join(decl_m.group(3).split())
                else:
                    ret_type = "unknown"
                    method_name = "unknown"
                    params = ""

                methods.append({
                    "method_name": method_name,
                    "http_method": "ALL" if verb == "REQUEST" else verb,
                    "path": full_path,
                    "params": params,
                    "return_type": ret_type,
                    "auth_rule": auth_rule
                })

            controllers.append({
                "file": f,
                "path": path,
                "base_mapping": base_mapping,
                "methods": methods
            })

with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\backend_endpoints.json", "w", encoding="utf-8") as out:
    json.dump(controllers, out, indent=2, ensure_ascii=False)

total_endpoints = sum(len(c["methods"]) for c in controllers)
print(f"Audited {len(controllers)} controllers, total endpoints: {total_endpoints}")
