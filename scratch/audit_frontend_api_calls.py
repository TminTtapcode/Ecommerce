import os
import re
import json

fe_root = r"c:\Users\Dell\IdeaProjects\ecommerce\frontend\src"

api_calls = []

for root, dirs, files in os.walk(fe_root):
    for f in files:
        if f.endswith((".ts", ".tsx")):
            path = os.path.join(root, f)
            with open(path, "r", encoding="utf-8") as file:
                content = file.read()
                
            # Match apiClient.(get|post|put|delete|patch)
            # Find all positions of apiClient.
            for m in re.finditer(r'apiClient\.(get|post|put|delete|patch)', content):
                verb = m.group(1).upper()
                start_pos = m.end()
                
                # We need to find the matching opening parenthesis '(' of the method call
                # If there are generics like <...>, skip them by counting '<' and '>'
                pos = start_pos
                while pos < len(content) and content[pos] in " \t\r\n":
                    pos += 1
                
                if pos < len(content) and content[pos] == '<':
                    depth = 0
                    while pos < len(content):
                        if content[pos] == '<':
                            depth += 1
                        elif content[pos] == '>':
                            depth -= 1
                            if depth == 0:
                                pos += 1
                                break
                        pos += 1
                
                # Now find '('
                while pos < len(content) and content[pos] in " \t\r\n":
                    pos += 1
                
                if pos < len(content) and content[pos] == '(':
                    # Extract the first argument up to ',' or ')'
                    arg_start = pos + 1
                    # Skip whitespace
                    while arg_start < len(content) and content[arg_start] in " \t\r\n":
                        arg_start += 1
                    
                    # Find closing quote or comma or end of string
                    # Check string quote
                    quote_char = content[arg_start] if content[arg_start] in ("'", '"', '`') else None
                    if quote_char:
                        arg_end = arg_start + 1
                        while arg_end < len(content):
                            if content[arg_end] == quote_char and content[arg_end-1] != '\\':
                                arg_str = content[arg_start:arg_end+1]
                                break
                            arg_end += 1
                    else:
                        # Maybe an expression or variable
                        arg_end = arg_start
                        while arg_end < len(content) and content[arg_end] not in (',', ')'):
                            arg_end += 1
                        arg_str = content[arg_start:arg_end].strip()
                    
                    # Calculate line number
                    line_num = content[:start_pos].count('\n') + 1
                    
                    api_calls.append({
                        "file": f,
                        "line": line_num,
                        "verb": verb,
                        "raw_call": arg_str.strip("`'\"")
                    })

with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\frontend_api_calls.json", "w", encoding="utf-8") as out:
    json.dump(api_calls, out, indent=2)

print(f"Extracted {len(api_calls)} frontend API calls.")
for c in sorted(api_calls, key=lambda x: (x['file'], x['line'])):
    print(f"{c['file']:<25} L{c['line']:<3} [{c['verb']:<6}] {c['raw_call']}")
