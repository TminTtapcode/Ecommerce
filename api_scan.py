import os
import re

BE_DIR = r"c:\Users\Dell\IdeaProjects\ecommerce\src\main\java\com\tmt\ecommerce"
FE_DIR = r"c:\Users\Dell\IdeaProjects\ecommerce\frontend\src\api"

be_endpoints = []

# Scan BE
for root, _, files in os.walk(BE_DIR):
    for file in files:
        if file.endswith("Controller.java"):
            filepath = os.path.join(root, file)
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()
                
            base_mapping = ""
            base_match = re.search(r'@RequestMapping\("([^"]+)"\)', content)
            if base_match:
                base_mapping = base_match.group(1)
                
            methods = re.findall(r'@(Get|Post|Put|Patch|Delete)Mapping(?:\("([^"]+)"\))?', content)
            for m in methods:
                method = m[0].upper()
                path = m[1] if m[1] else ""
                full_path = (base_mapping + path).replace("//", "/")
                # Normalize path variables {id} -> {var}
                full_path = re.sub(r'\{.+?\}', '{var}', full_path)
                if not full_path.endswith('/'):
                    full_path += '/'
                be_endpoints.append((method, full_path, file))

# Scan FE
fe_endpoints = []
for root, _, files in os.walk(FE_DIR):
    for file in files:
        if file.endswith(".ts") or file.endswith(".tsx"):
            filepath = os.path.join(root, file)
            with open(filepath, "r", encoding="utf-8") as f:
                lines = f.readlines()
                for line in lines:
                    match = re.search(r'apiClient\.(get|post|put|patch|delete)[<a-zA-Z\[\],>\s]*\(([`\'"])(.+?)\2', line)
                    if match:
                        method = match.group(1).upper()
                        path = match.group(3)
                        # Normalize variables like ${id} -> {var}
                        path = re.sub(r'\$\{.+?\}', '{var}', path)
                        
                        # Handle query params attached to the path
                        path = path.split('?')[0]
                        
                        if not path.endswith('/'):
                            path += '/'
                        fe_endpoints.append((method, path, file))

print("=== API AUDIT ===")

print("\n--- BE Endpoints NOT in FE ---")
fe_keys = set(f"{m} {p}" for m, p, _ in fe_endpoints)
for m, p, f in sorted(be_endpoints):
    if f"{m} {p}" not in fe_keys:
        print(f"{m} {p} (BE: {f})")

print("\n--- FE Endpoints NOT in BE ---")
be_keys = set(f"{m} {p}" for m, p, _ in be_endpoints)
for m, p, f in sorted(fe_endpoints):
    if f"{m} {p}" not in be_keys:
        print(f"{m} {p} (FE: {f})")
