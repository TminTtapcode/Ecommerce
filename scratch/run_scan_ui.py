import subprocess

res = subprocess.run(["python", r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\scan_ui_issues.py"], capture_output=True, text=True, encoding="utf-8")
with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\scan_ui_issues.txt", "w", encoding="utf-8") as f:
    f.write(res.stdout)
    if res.stderr:
        f.write("\nSTDERR:\n" + res.stderr)

print("Saved output to scratch/scan_ui_issues.txt")
