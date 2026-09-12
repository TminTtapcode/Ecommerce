import json
import re

with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\backend_endpoints.json", "r", encoding="utf-8") as f:
    be_data = f.read()

with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\frontend_api_calls.json", "r", encoding="utf-8") as f:
    fe_data = f.read()

be_controllers = json.loads(be_data)
fe_calls = json.loads(fe_data)

# Flatten backend endpoints
be_endpoints = []
for c in be_controllers:
    cfile = c["file"]
    for m in c["methods"]:
        be_endpoints.append({
            "controller": cfile,
            "method_name": m["method_name"],
            "http_method": m["http_method"],
            "path": m["path"],
            "params": m["params"],
            "auth_rule": m.get("auth_rule", "")
        })

def normalize_path(p):
    # Strip query
    p = p.split("?")[0]
    # Replace ${...}
    p = re.sub(r'\$\{[^}]+\}', '{id}', p)
    # Replace {xxx}
    p = re.sub(r'\{[^}]+\}', '{id}', p)
    # Normalize slashes
    p = re.sub(r'/+', '/', p).rstrip("/")
    return p

matches = []
unmatched_fe = []

for fe in fe_calls:
    fe_norm = normalize_path(fe["raw_call"])
    found = None
    for be in be_endpoints:
        be_norm = normalize_path(be["path"])
        if fe["verb"] == be["http_method"] and fe_norm == be_norm:
            found = be
            break
    if found:
        matches.append({"fe": fe, "be": found})
    else:
        # Try finding if path exists with different verb
        other_verb = [be for be in be_endpoints if normalize_path(be["path"]) == fe_norm]
        unmatched_fe.append({"fe": fe, "fe_norm": fe_norm, "other_verbs": other_verb})

print(f"Total FE calls: {len(fe_calls)}")
print(f"Matched FE calls: {len(matches)}")
print(f"Unmatched FE calls: {len(unmatched_fe)}")

if unmatched_fe:
    print("\n=== UNMATCHED FRONTEND ENDPOINTS ===")
    for u in unmatched_fe:
        fe = u["fe"]
        print(f"[{fe['verb']}] {fe['raw_call']} ({fe['file']}:L{fe['line']})")
        if u["other_verbs"]:
            print(f"   -> Path exists in BE but with verbs: {[v['http_method'] for v in u['other_verbs']]}")
        else:
            print(f"   -> Path does NOT exist in backend at all!")

# Also check backend endpoints that are NOT called by frontend (unused or backend-only)
called_be_paths = set(normalize_path(m["be"]["path"]) + ":" + m["be"]["http_method"] for m in matches)
unused_be = []
for be in be_endpoints:
    key = normalize_path(be["path"]) + ":" + be["http_method"]
    if key not in called_be_paths:
        unused_be.append(be)

print(f"\nBackend endpoints not directly called in frontend api/*.ts: {len(unused_be)}")
for u in unused_be:
    print(f"  [{u['http_method']:<6}] {u['path']:<45} ({u['controller']}) [Auth: {u['auth_rule']}]")
