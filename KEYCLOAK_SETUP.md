# Keycloak Configuration Guide

## Login
- URL: http://localhost:8090
- Username: `admin`
- Password: `admin`

---

## Step 1: Create Realm "finki-services"

1. Click **Manage realms** (top-left dropdown)
2. Click **Create realm**
3. Name: `finki-services`
4. Click **Create**

You should now be in the finki-services realm.

---

## Step 2: Create Realm Roles

Go to: **Realm roles** (left menu)

### Role 1: service.user
- Click **Create role**
- Role name: `service.user`
- Description: Regular user
- Click **Save**

### Role 2: service.admin
- Click **Create role**
- Role name: `service.admin`
- Description: Administrator
- Click **Save**

---

## Step 3: Create Users and Assign Roles

Go to: **Users** (left menu)

### User 1: ben

1. Click **Create new user**
2. Username: `ben`
3. Email: `ben@springframework.org`
4. Toggle **Email verified: ON**
5. Click **Create**

**Set Password:**
- Go to **Credentials** tab
- Click **Set password**
- Password: `benspassword`
- Toggle **Temporary: OFF**
- Click **Set password**

**Assign Role:**
- Go to **Role mapping** tab
- Click **Assign role**
- Filter: select "Realm roles"
- Check **service.admin**
- Click **Assign**

### User 2: bob

1. Click **Create new user**
2. Username: `bob`
3. Email: `bob@springframework.org`
4. Email verified: ON
5. Click **Create**

**Set Password:**
- Go to **Credentials** tab
- Password: `bobspassword`
- Temporary: OFF
- Click **Set password**

**Assign Role:**
- Go to **Role mapping** tab
- Assign role: **service.user**

### User 3: test

1. Click **Create new user**
2. Username: `test`
3. Email: `test@springframework.org`
4. Email verified: ON
5. Click **Create**

**Set Password:**
- Password: `testpassword`
- Temporary: OFF
- Click **Set password**

**Assign Role:**
- Assign role: **service.user**

---

## Step 4: Register Client "gateway-tester"

Go to: **Clients** (left menu)

1. Click **Create client**
2. Client type: **OpenID Connect**
3. Client ID: `gateway-tester`
4. Click **Next**

**Capability config:**
- Client authentication: **ON** (confidential)
- Authentication flow:
  - Check: **Direct access grants**
  - Check: **Standard flow**
- Click **Next**

**Login settings:**
- Valid redirect URIs: `http://localhost:*`
- Click **Save**

**Copy Client Secret:**
- Go to **Credentials** tab
- Copy the **Client secret** (you'll need this for testing)

---

## Step 5: Get a Token (Testing)

```bash
curl -s -X POST \
  -d "client_id=gateway-tester" \
  -d "client_secret=<PASTE_SECRET_HERE>" \
  -d "grant_type=password" \
  -d "username=ben" \
  -d "password=benspassword" \
  http://localhost:8090/realms/finki-services/protocol/openid-connect/token | jq .
```

You should get back an `access_token`. Copy it!

---

## Step 6: Verify Token

```bash
# Replace TOKEN with the access_token from above
echo "<TOKEN>" | cut -d. -f2 | base64 -d 2>/dev/null | jq .
```

You should see claims like:
- `iss`: http://localhost:8090/realms/finki-services
- `sub`: user ID
- `realm_access.roles`: ["service.admin"]

---

## LDAP Federation (Optional - Part 5)

Once realm is set up:

1. Go to **User federation** (left menu)
2. Click **Add Ldap providers**

### Connection:
- UI display name: `ldap`
- Vendor: `Other`
- Connection URL: `ldap://openldap:389`
- Bind type: `simple`
- Bind DN: `cn=admin,dc=springframework,dc=org`
- Bind credentials: `
- 
- 
- 
- 
- 
- 
- 
- 
- 
- 
- `

Click **Test connection** and **Test authentication** (both must pass)

### LDAP Config:
- Edit mode: `READ_ONLY`
- Users DN: `ou=people,dc=springframework,dc=org`
- Username LDAP attribute: `uid`
- RDN LDAP attribute: `cn`
- UUID LDAP attribute: `uid`
- User object classes: `inetOrgPerson, organizationalPerson, person`
- Search scope: `One Level`

### Sync:
- Import users: ON
- Sync registrations: ON
- Periodic full sync: OFF

Click **Save**, then **Sync all users**

Now LDAP users (ben, bob, test) can log in with their LDAP passwords!

---

## Quick Checklist

- [ ] Created realm: finki-services
- [ ] Created roles: service.user, service.admin
- [ ] Created user: ben (service.admin role)
- [ ] Created user: bob (service.user role)
- [ ] Created user: test (service.user role)
- [ ] Created client: gateway-tester
- [ ] Copied client secret
- [ ] Got access token
- [ ] Verified token claims

Once done, tell me and we'll create the API Gateway!