# Test Token - Ben User

**Client Secret:**
```
fxp6CM5F28RKPdXLvqKONTU7kNmpRaA9
```

---

## Access Token

```
eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3bHRIR1NtMzV3Vk9GY0RlQ1JDWC0xeXI1N3FyaGozYzhZd2Z0cUlLZ2s4In0.eyJleHAiOjE3ODQxOTg5ODQsImlhdCI6MTc4NDE5ODY4NCwianRpIjoiM2YwNWZjOTktZGVkMS00NzJkLWI0NWItOGIyN2Q4NDgzODcxIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDkwL3JlYWxtcy9maW5raS1zZXJ2aWNlcyIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJiM2VkYTNhZS01NjVkLTQ5NjYtYTBiNi02MDVlMGZjYzI3ZDIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJnYXRld2F5LXRlc3RlciIsInNpZCI6ImIyNjZkNTI2LTBhYjEtNDcyZS05ZmE0LTVmZWVhMzFlMGM4YyIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDoqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJzZXJ2aWNlLnVzZXIiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy1maW5raS1zZXJ2aWNlcyJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoiQmVuIFNwcmluZyIsInByZWZlcnJlZF91c2VybmFtZSI6ImJlbiIsImdpdmVuX25hbWUiOiJCZW4iLCJmYW1pbHlfbmFtZSI6IlNwcmluZyIsImVtYWlsIjoiYmVuQGVtYWlsLmNvbSJ9.dul5ekTB4jMJHasgyhxZe7MtovgRmd0fewlvG9x2oatrffjCGBOLM56mUjDrj8Y9fQm9v71Sb0p8uixU8-_tJaylvdkuTpiT1HCOjIQh3ay6yA1eU16tgiIMTCW70mNK-4ZcV1vDmjnOZZ4NJsw7fEZB6pXeQuMqHf1btepiZviKBrnh7BtL3ZNLqsPQIfRRvALuFgDqN9cJpJJh8xVdP_Sfwq2n64J01sgDOHsIAG7B1vjGWEailwZ4SR0akX8p62Kefy5qXoaUkKTjIX3iAwNjnIJSiLgYB4K08TRcQqglyR4bkqpGwEmL6kco1fySlITYdC66TSWO_GtDGSpk4A
```

---

## Token Verification ✅

Decoded payload:

```json
{"exp":1784198984,"iat":1784198684,"jti":"3f05fc99-ded1-472d-b45b-8b27d8483871","iss":"http://localhost:8090/realms/finki-services","aud":"account","sub":"b3eda3ae-565d-4966-a0b6-605e0fcc27d2","typ":"Bearer","azp":"gateway-tester","sid":"b266d526-0ab1-472e-9fa4-5feea31e0c8c","acr":"1","allowed-origins":["http://localhost:*"],"realm_access":{"roles":["service.user","offline_access","uma_authorization","default-roles-finki-services"]},"resource_access":{"account":{"roles":["manage-account","manage-account-links","view-profile"]}},"scope":"profile email","email_verified":true,"name":"Ben Spring","preferred_username":"ben","given_name":"Ben","family_name":"Spring","email":"ben@email.com"}
```

Formatted:

---

## Token Claims (Decoded)

```json
{
  "exp": 1784198984,
  "iat": 1784198684,
  "jti": "3f05fc99-ded1-472d-b45b-8b27d8483871",
  "iss": "http://localhost:8090/realms/finki-services",
  "aud": "account",
  "sub": "b3eda3ae-565d-4966-a0b6-605e0fcc27d2",
  "typ": "Bearer",
  "azp": "gateway-tester",
  "sid": "b266d526-0ab1-472e-9fa4-5feea31e0c8c",
  "acr": "1",
  "allowed-origins": [
    "http://localhost:*"
  ],
  "realm_access": {
    "roles": [
      "service.user",
      "offline_access",
      "uma_authorization",
      "default-roles-finki-services"
    ]
  },
  "resource_access": {
    "account": {
      "roles": [
        "manage-account",
        "manage-account-links",
        "view-profile"
      ]
    }
  },
  "scope": "profile email",
  "email_verified": true,
  "name": "Ben Spring",
  "preferred_username": "ben",
  "given_name": "Ben",
  "family_name": "Spring",
  "email": "ben@email.com"
}
```

---

## Keycloak Configuration Summary

**Realm:** finki-services
**Client:** gateway-tester
**Client ID:** gateway-tester
**Client Secret:** fxp6CM5F28RKPdXLvqKONTU7kNmpRaA9

**Users:**
- ben / benpassword (service.admin role)
- bob / bobpassword (service.user role)
- test / testpassword (service.user role)

**Token Endpoints:**
- Get Token: `POST http://localhost:8090/realms/finki-services/protocol/openid-connect/token`
- JWKS (for gateway): `GET http://localhost:8090/realms/finki-services/protocol/openid-connect/certs`

---

## Using the Token

```bash
# With curl
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8000/api/plants

# Replace <TOKEN> with the access_token above
```

---

**Token expires in:** 5 minutes (300 seconds)
**Refresh token expires in:** 30 minutes (1800 seconds)