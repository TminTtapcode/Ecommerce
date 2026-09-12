import os

frontend_api_dir = r"c:\Users\Dell\IdeaProjects\ecommerce\frontend\src\api"

with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\api_summary.txt", "w", encoding="utf-8") as out:
    for root, dirs, files in os.walk(frontend_api_dir):
        for f in sorted(files):
            p = os.path.join(root, f)
            rel = os.path.relpath(p, frontend_api_dir)
            out.write(f"\n==================== {rel} ====================\n")
            with open(p, "r", encoding="utf-8", errors="ignore") as file:
                out.write(file.read())

print("API and types extracted to scratch/api_summary.txt")
