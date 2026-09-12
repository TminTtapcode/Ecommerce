import json

with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\frontend_scan.json", "r", encoding="utf-8") as f:
    data = json.load(f)

print("=== ALL FRONTEND FILES ===")
for f in sorted(data["files"]):
    print(f)

print("\n=== TODOS ===")
for t in data["todos"]:
    print(f"{t['file']}:{t['line']} - {t['content']}")

print("\n=== ALERTS ===")
for a in data["alerts"]:
    print(f"{a['file']}:{a['line']} - {a['content']}")

print("\n=== MOCK PATTERNS ===")
for m in data["mock_patterns"]:
    print(f"{m['file']}:{m['line']} - {m['content']}")

print("\n=== EMPTY HANDLERS ===")
for e in data["empty_handlers"]:
    print(f"{e['file']}:{e['line']} - {e['content']}")

print("\n=== API CALLS ===")
for c in data["api_calls"]:
    print(f"{c['file']}:{c['line']} - {c['call']}")
