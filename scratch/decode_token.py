import urllib.request
import json
import base64

req = urllib.request.Request(
    'http://localhost:8080/api/v1/auth/login',
    data=json.dumps({"email": "user1@gmail.com", "password": "password123"}).encode('utf-8'),
    headers={'Content-Type': 'application/json'}
)
with urllib.request.urlopen(req) as resp:
    res = json.loads(resp.read().decode('utf-8'))
    token = res['data']['token']

payload_part = token.split('.')[1]
# Pad base64
payload_part += '=' * (-len(payload_part) % 4)
claims = json.loads(base64.b64decode(payload_part).decode('utf-8'))
print(json.dumps(claims, indent=2))
