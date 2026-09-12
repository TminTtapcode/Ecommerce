import os
import json
import re

frontend_dir = r"c:\Users\Dell\IdeaProjects\ecommerce\frontend\src"

def analyze_file(path):
    with open(path, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
    
    # find functions, exports, handlers, hooks, api calls
    exports = re.findall(r'export\s+(?:const|function|class|interface|type)\s+([A-Za-z0-9_]+)', content)
    apis = re.findall(r'(?:apiClient|axios)\.(get|post|put|patch|delete)\s*(?:<[^>]+>)?\s*\(\s*[`"\']([^`"\']+)[`"\']', content)
    alerts = re.findall(r'alert\s*\(\s*[`"\']([^`"\']+)[`"\']\s*\)', content)
    todos = [line.strip() for line in content.splitlines() if "TODO" in line or "FIXME" in line]
    empties = [line.strip() for line in content.splitlines() if re.search(r'onClick\s*=\s*\{\s*\(\s*\)\s*=>\s*\{\s*\}', line)]
    disabled = [line.strip() for line in content.splitlines() if "disabled" in line and "true" in line]
    
    return {
        "exports": exports,
        "api_calls": apis,
        "alerts": alerts,
        "todos": todos,
        "empty_handlers": empties,
        "disabled_true": disabled,
        "size_lines": len(content.splitlines())
    }

report = {}
for root, dirs, files in os.walk(frontend_dir):
    for f in sorted(files):
        if f.endswith(('.ts', '.tsx')):
            p = os.path.join(root, f)
            rel = os.path.relpath(p, frontend_dir)
            report[rel] = analyze_file(p)

with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\deep_frontend_analysis.json", "w", encoding="utf-8") as f:
    json.dump(report, f, indent=2, ensure_ascii=False)

print("Analyzed", len(report), "files.")
