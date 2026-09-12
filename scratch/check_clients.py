import json
import re

# Read api_summary.txt
with open(r"c:\Users\Dell\IdeaProjects\ecommerce\scratch\api_summary.txt", "r", encoding="utf-8") as f:
    text = f.read()

# Also let's inspect apiClient.ts
print("=== apiClient.ts ===")
match = re.search(r'==================== apiClient\.ts ====================\n(.*?)(?=\n====================)', text, re.DOTALL)
if match:
    print(match.group(1))

print("=== authApi.ts ===")
match = re.search(r'==================== authApi\.ts ====================\n(.*?)(?=\n====================)', text, re.DOTALL)
if match:
    print(match.group(1))

print("=== cartApi.ts ===")
match = re.search(r'==================== cartApi\.ts ====================\n(.*?)(?=\n====================)', text, re.DOTALL)
if match:
    print(match.group(1))

print("=== orderApi.ts ===")
match = re.search(r'==================== orderApi\.ts ====================\n(.*?)(?=\n====================)', text, re.DOTALL)
if match:
    print(match.group(1))

print("=== paymentApi.ts ===")
match = re.search(r'==================== paymentApi\.ts ====================\n(.*?)(?=\n====================)', text, re.DOTALL)
if match:
    print(match.group(1))
