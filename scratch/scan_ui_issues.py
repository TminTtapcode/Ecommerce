import os
import re
import json

frontend_src = r"c:\Users\Dell\IdeaProjects\ecommerce\frontend\src"

results = {
    "alerts": [],
    "todos": [],
    "href_hash": [],
    "empty_on_click": [],
    "mock_or_dummy": []
}

for root, dirs, files in os.walk(frontend_src):
    for f in sorted(files):
        if not f.endswith(('.ts', '.tsx')):
            continue
        p = os.path.join(root, f)
        rel = os.path.relpath(p, frontend_src)
        with open(p, 'r', encoding='utf-8', errors='ignore') as file:
            lines = file.readlines()
        
        for i, line in enumerate(lines, 1):
            s = line.strip()
            if "alert(" in line:
                results["alerts"].append({"file": rel, "line": i, "content": s})
            if "TODO" in line or "FIXME" in line:
                results["todos"].append({"file": rel, "line": i, "content": s})
            if 'href="#"' in line or "href='#'" in line:
                results["href_hash"].append({"file": rel, "line": i, "content": s})
            if 'onClick={() => {}}' in line or 'onClick={() => { }}' in line:
                results["empty_on_click"].append({"file": rel, "line": i, "content": s})
            if re.search(r'\b(mock|dummy|fake|static)\b', line, re.IGNORECASE):
                results["mock_or_dummy"].append({"file": rel, "line": i, "content": s})

with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\scan_ui_issues.json", "w", encoding="utf-8") as f:
    json.dump(results, f, indent=2, ensure_ascii=False)

print(f"Done! Alerts: {len(results['alerts'])}, Todos: {len(results['todos'])}, Hash links: {len(results['href_hash'])}, Empty onClick: {len(results['empty_on_click'])}, Mock: {len(results['mock_or_dummy'])}")
