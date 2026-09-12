import os

types_dir = r"c:\Users\Dell\IdeaProjects\ecommerce\frontend\src\api\types"
for f in os.listdir(types_dir):
    if f.endswith(".ts"):
        print(f"=== {f} ===")
        with open(os.path.join(types_dir, f), "r", encoding="utf-8") as file:
            print(file.read())
