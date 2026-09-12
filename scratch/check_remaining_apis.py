import re

with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\api_summary.txt", "r", encoding="utf-8") as f:
    text = f.read()

for name in ["productApi.ts", "reviewApi.ts", "notificationApi.ts", "mediaApi.ts", "chatApi.ts"]:
    print(f"=== {name} ===")
    match = re.search(rf'==================== {name} ====================\n(.*?)(?=\n====================)', text, re.DOTALL)
    if match:
        print(match.group(1))
