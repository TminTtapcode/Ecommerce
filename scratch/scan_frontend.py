import os
import re
import json

src_dir = r"c:\Users\Dell\IdeaProjects\ecommerce\frontend\src"

all_files = []
for root, dirs, files in os.walk(src_dir):
    for f in files:
        if f.endswith(('.ts', '.tsx', '.js', '.jsx')):
            all_files.append(os.path.join(root, f))

scan_result = {
    "files_count": len(all_files),
    "files": [],
    "todos": [],
    "fixmes": [],
    "alerts": [],
    "console_logs": [],
    "mock_patterns": [],
    "empty_handlers": [],
    "href_hash": [],
    "api_calls": []
}

for filepath in all_files:
    rel_path = os.path.relpath(filepath, src_dir)
    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
        lines = content.splitlines()

    scan_result["files"].append(rel_path)

    for i, line in enumerate(lines, 1):
        line_strip = line.strip()
        if "TODO" in line:
            scan_result["todos"].append({"file": rel_path, "line": i, "content": line_strip})
        if "FIXME" in line:
            scan_result["fixmes"].append({"file": rel_path, "line": i, "content": line_strip})
        if re.search(r'\balert\(', line):
            scan_result["alerts"].append({"file": rel_path, "line": i, "content": line_strip})
        if "console.log" in line:
            scan_result["console_logs"].append({"file": rel_path, "line": i, "content": line_strip})
        if re.search(r'\b(mock|dummy|fake)\b', line, re.IGNORECASE):
            scan_result["mock_patterns"].append({"file": rel_path, "line": i, "content": line_strip})
        if re.search(r'href\s*=\s*["\']#["\']', line):
            scan_result["href_hash"].append({"file": rel_path, "line": i, "content": line_strip})
        if re.search(r'onClick\s*=\s*\{\s*\(\s*\)\s*=>\s*\{\s*\}\s*\}', line):
            scan_result["empty_handlers"].append({"file": rel_path, "line": i, "content": line_strip})
        
        # API calls
        api_match = re.search(r'(apiClient|axios|\.get|\.post|\.put|\.delete|\.patch)\s*\(\s*[`"\']([^`"\']+)[`"\']', line)
        if api_match:
            scan_result["api_calls"].append({"file": rel_path, "line": i, "call": api_match.group(0), "endpoint": api_match.group(2)})

out_path = r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\frontend_scan.json"
with open(out_path, 'w', encoding='utf-8') as f:
    json.dump(scan_result, f, indent=2, ensure_ascii=False)

print(f"Scanned {len(all_files)} files.")
