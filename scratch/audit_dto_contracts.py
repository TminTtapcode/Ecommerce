import os
import re
import json

backend_dto_dir = r"c:\Users\Dell\IdeaProjects\ecommerce\src\main\java\com\tmt\ecommerce"
frontend_types_dir = r"c:\Users\Dell\IdeaProjects\ecommerce\frontend\src\api\types"

print("Auditing DTO files...")
fe_type_files = os.listdir(frontend_types_dir)
print("Frontend type files:", fe_type_files)

# Let's inspect frontend types
fe_interfaces = {}
for tf in fe_type_files:
    if tf.endswith(".ts"):
        with open(os.path.join(frontend_types_dir, tf), "r", encoding="utf-8") as f:
            content = f.read()
        
        # Match interface or type
        for m in re.finditer(r'export\s+interface\s+([A-Za-z0-9_]+)\s*\{([^}]+)\}', content):
            name = m.group(1)
            body = m.group(2)
            fields = []
            for line in body.split("\n"):
                line = line.strip()
                if line and not line.startswith("//"):
                    fm = re.match(r'([A-Za-z0-9_]+)\??\s*:\s*([^;]+);?', line)
                    if fm:
                        fields.append({"name": fm.group(1), "type": fm.group(2).strip()})
            fe_interfaces[name] = {"file": tf, "fields": fields}

print(f"Extracted {len(fe_interfaces)} Frontend interfaces/types.")

# Let's look for matching Java records/DTOs in backend
be_dtos = {}
for root, dirs, files in os.walk(backend_dto_dir):
    for f in files:
        if f.endswith(".java") and ("dto" in root.lower() or "dto" in f.lower()):
            path = os.path.join(root, f)
            with open(path, "r", encoding="utf-8") as file:
                c = file.read()
            
            # Check for record: public record Foo(...)
            record_match = re.search(r'public\s+record\s+([A-Za-z0-9_]+)\s*\(([^)]*)\)', c)
            if record_match:
                rname = record_match.group(1)
                rparams = record_match.group(2)
                fields = []
                for p in rparams.split(","):
                    p = p.strip()
                    if p:
                        parts = p.split()
                        if len(parts) >= 2:
                            fields.append({"name": parts[-1], "type": parts[-2]})
                be_dtos[rname] = {"file": f, "is_record": True, "fields": fields}

            # Check for class: public class Foo { ... }
            class_match = re.search(r'public\s+class\s+([A-Za-z0-9_]+)', c)
            if class_match and not record_match:
                cname = class_match.group(1)
                # find fields
                fields = []
                for fm in re.finditer(r'private\s+([A-Za-z0-9_<>,.\[\]]+)\s+([A-Za-z0-9_]+);', c):
                    fields.append({"name": fm.group(2), "type": fm.group(1)})
                be_dtos[cname] = {"file": f, "is_record": False, "fields": fields}

print(f"Extracted {len(be_dtos)} Backend DTOs/records.")

# Now check key business contracts:
# 1. ProductResponse
# 2. OrderResponse
# 3. ShopResponse
# 4. VoucherResponse
# 5. ReviewResponse
# 6. CartResponse / CartItemResponse
# 7. CheckoutRequest / CheckoutResponse

def compare(fe_name, be_name):
    print(f"\n--- COMPARING {fe_name} (FE) vs {be_name} (BE) ---")
    fe = fe_interfaces.get(fe_name)
    be = be_dtos.get(be_name)
    if not fe:
        print(f"  FE interface {fe_name} not found!")
        return
    if not be:
        print(f"  BE DTO {be_name} not found!")
        return

    fe_field_names = set(f["name"] for f in fe["fields"])
    be_field_names = set(f["name"] for f in be["fields"])

    missing_in_fe = be_field_names - fe_field_names
    missing_in_be = fe_field_names - be_field_names
    common = fe_field_names & be_field_names

    print(f"  Common fields ({len(common)}): {sorted(list(common))}")
    if missing_in_fe:
        print(f"  In BE but missing in FE ({len(missing_in_fe)}): {sorted(list(missing_in_fe))}")
    if missing_in_be:
        print(f"  In FE but NOT in BE ({len(missing_in_be)}): {sorted(list(missing_in_be))}")

compare("ShopResponse", "ShopResponse")
compare("OrderResponse", "OrderResponse")
compare("VoucherResponse", "VoucherResponse")
compare("ProductResponse", "ProductResponse")
compare("CartResponse", "CartResponse")
compare("ReviewResponse", "ReviewResponse")
compare("CheckoutRequest", "CheckoutRequest")

