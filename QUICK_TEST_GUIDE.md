# 🧪 Email OTP Verification - Quick Test Guide

## 🚀 QUICK START (5 minutes)

### Prerequisites
- ✅ Backend running on `http://localhost:8085`
- ✅ Frontend running on `http://localhost:3001`
- ✅ MySQL running with `erp_management` database
- ✅ Gmail account with App Password configured

### Test URLs
- **Frontend**: http://localhost:3001
- **Backend Health Check**: http://localhost:8085/api/auth/login (should show error without credentials)

---

## 🧪 Test Scenario 1: Complete Registration → OTP Verification → Login

### Step 1: Register New User
1. Open http://localhost:3001
2. Click **"Create account"** link
3. Fill in:
   - Username: `testuser123`
   - Password: `Password@123`
   - Role: **Student**
4. Click **"Sign Up"**

### Expected Result
- ✅ Message: "Registration successful. Please verify your email."
- ✅ Redirected to OTP Verification screen
- ✅ Shows: "We've sent a 6-digit code to testuser123@kluniversity.in"
- ✅ Email received in Gmail inbox with subject: "Your ERP Verification OTP Code"

### Step 2: Enter OTP
1. Check email inbox for OTP (e.g., `582941`)
2. Enter the 6-digit code in verification field
3. Click **"Verify Account"**

### Expected Result
- ✅ Success message: "Email verified successfully. You can now login."
- ✅ Redirected to Login page

### Step 3: Login with Verified Account
1. Enter:
   - Username: `testuser123`
   - Password: `Password@123`
   - Role: **Student**
   - CAPTCHA: (enter displayed code)
2. Click **"Login"**

### Expected Result
- ✅ Redirected to Dashboard
- ✅ User info shown in top-right corner

---

## 🧪 Test Scenario 2: Invalid OTP Handling

### Step 1: Register Another User
1. Create new account (username: `testuser456`)
2. Proceed to OTP verification

### Step 2: Enter Wrong OTP
1. Deliberately enter wrong code: `123456`
2. Click **"Verify Account"**

### Expected Result
- ❌ Error message: "Invalid or expired OTP"
- ✅ Stays on OTP verification page
- ✅ Can try again

### Step 3: Enter Correct OTP
1. Check email for correct OTP
2. Enter correct code
3. Click **"Verify Account"**

### Expected Result
- ✅ Success message
- ✅ Redirected to Login

---

## 🧪 Test Scenario 3: OTP Resend

### Step 1: Register and Go to OTP Screen
1. Register new account: `testuser789`
2. Proceed to OTP verification

### Step 2: Test Resend Button
1. Click **"Resend Code"** button
2. Wait 30 seconds (initial cooldown)

### Expected Result
- ❌ Button disabled initially: "Resend code in 30s"
- ✅ After 30s, button becomes clickable
- ✅ Button text changes to: "Resend Code"
- ✅ New email received

### Step 3: Verify with New OTP
1. Check email for new OTP
2. Enter new OTP code
3. Click **"Verify Account"**

### Expected Result
- ✅ Success message
- ✅ Both old and new OTP should work (only latest is valid)

---

## 🧪 Test Scenario 4: OTP Expiry (5 minutes)

### Step 1: Register and Don't Verify Immediately
1. Register: `expiry_test@kluniversity.in`
2. Note the OTP from email
3. **Wait 5 minutes** (system expiry time)

### Step 2: After 5+ Minutes, Try Old OTP
1. Enter the original OTP code
2. Click **"Verify Account"**

### Expected Result
- ❌ Error message: "Invalid or expired OTP"

### Step 3: Resend and Use New OTP
1. Click **"Resend Code"**
2. Check email for new OTP
3. Enter new OTP
4. Click **"Verify Account"**

### Expected Result
- ✅ Success! New OTP works

---

## 🧪 Test Scenario 5: Login Blocked for Unverified Account

### Step 1: Register Without Verification
1. Register: `unverified_user`
2. Bypass OTP verification by going back to login page
   - Click "Back to Login" or navigate directly

### Step 2: Try to Login with Unverified Account
1. Enter credentials:
   - Username: `unverified_user`
   - Password: (your password)
   - Role: **Student**
2. Click **"Login"**

### Expected Result
- ❌ Status 403 error
- ✅ Message: "Account not verified. Please verify your email."
- ✅ Redirected to OTP verification screen automatically
- ✅ Can now complete verification

### Step 3: Verify and Login
1. Complete OTP verification
2. Login again

### Expected Result
- ✅ Login successful!
- ✅ Redirected to Dashboard

---

## ✅ Validation Checklist

### Database Verification
```sql
-- Check if user table has is_verified column
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME='users' AND COLUMN_NAME='is_verified';

-- Check if otps table exists
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA='erp_management' AND TABLE_NAME='otps';

-- List all verified users
SELECT username, email, is_verified FROM users;

-- Check active OTPs
SELECT email, otp_code, expiry_time FROM otps;
```

### API Testing with Postman

#### 1. Register Endpoint
```
POST http://localhost:8085/api/auth/register
Content-Type: application/json

{
  "username": "postman_test",
  "email": "postman_test@kluniversity.in",
  "password": "TestPassword@123",
  "firstName": "Postman",
  "lastName": "Test",
  "role": "student"
}

Expected Response (200):
{
  "success": true,
  "user": {
    "id": 5,
    "username": "postman_test",
    "email": "postman_test@kluniversity.in",
    "isVerified": false,
    ...
  },
  "message": "User registered successfully..."
}
```

#### 2. Verify OTP Endpoint
```
POST http://localhost:8085/api/auth/verify-otp
Content-Type: application/json

{
  "email": "postman_test@kluniversity.in",
  "otp": "123456"
}

Expected Response (200):
{
  "success": true,
  "message": "Email verified successfully. You can now login."
}
```

#### 3. Resend OTP Endpoint
```
POST http://localhost:8085/api/auth/resend-otp
Content-Type: application/json

{
  "email": "postman_test@kluniversity.in"
}

Expected Response (200):
{
  "success": true,
  "message": "OTP resent successfully"
}
```

#### 4. Login Verified User
```
POST http://localhost:8085/api/auth/login
Content-Type: application/json

{
  "username": "postman_test",
  "password": "TestPassword@123"
}

Expected Response (200):
{
  "success": true,
  "user": {
    "id": 5,
    "username": "postman_test",
    "isVerified": true,
    ...
  },
  "token": "..."
}
```

#### 5. Login Unverified User
```
POST http://localhost:8085/api/auth/login
Content-Type: application/json

{
  "username": "unverified_user",
  "password": "Password@123"
}

Expected Response (403):
{
  "success": false,
  "notVerified": true,
  "email": "unverified_user@kluniversity.in",
  "message": "Account not verified. Please verify your email."
}
```

---

## 🐛 Common Issues & Fixes

### Issue 1: Email Not Received
**Solution:**
1. Check if backend logs show email sending errors
2. Verify Gmail account has 2FA enabled
3. Check if App Password is correct
4. Check Spam/Promotions folder in Gmail
5. Try resending OTP
6. Check backend logs for SMTP errors:
```
grep -i "mail\|smtp\|email" <backend-logs>
```

### Issue 2: OTP Always Shows as Invalid
**Solution:**
1. Copy OTP without spaces: `582941` NOT `582 941`
2. Check if OTP has expired (5 minutes)
3. Verify OTP matches what's in backend logs
4. In database, check:
```sql
SELECT * FROM otps WHERE email = 'your-email@kluniversity.in';
```

### Issue 3: Login Still Blocked After Verification
**Solution:**
1. Check database:
```sql
SELECT username, is_verified FROM users WHERE username = 'your_username';
```
2. If `is_verified` is 0, manually update:
```sql
UPDATE users SET is_verified = 1 WHERE username = 'your_username';
```
3. Clear localStorage and reload:
```javascript
// In browser console
localStorage.clear();
location.reload();
```

### Issue 4: CORS Errors in Browser Console
**Solution:**
Already fixed in backend. Check if `application.properties` has:
```properties
cors.allowed-origins=http://localhost:3000,http://localhost:8080,http://localhost:3001
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
```

---

## 📊 Expected Email Content

### OTP Email Example
```
From: koushiknaga42@gmail.com
To: testuser@kluniversity.in
Subject: Your ERP Verification OTP Code

Welcome to ERP Management System!

Your verification code is: 582941

This code will expire in 5 minutes.

If you did not request this, please ignore this email.
```

---

## 🏁 Success Indicators

✅ **All tests pass when:**
1. New users receive OTP emails within seconds
2. OTP code is exactly 6 digits
3. Correct OTP allows verification
4. Wrong OTP shows error message
5. OTP expires after 5 minutes
6. Resend generates new OTP
7. Verified users can login
8. Unverified users are blocked
9. No CORS errors in console
10. Dashboard shows after successful login

---

## 🎯 Performance Metrics

| Metric | Target | Status |
|--------|--------|--------|
| Email sending time | < 5 seconds | ✅ Expected |
| OTP validation | < 500ms | ✅ Expected |
| Login response | < 1 second | ✅ Expected |
| OTP expiry check | < 100ms | ✅ Expected |
| Page load time | < 2 seconds | ✅ Expected |

---

## 📞 Debugging Commands

### Backend Logs
```bash
# View real-time logs
tail -f ERP-Backend/boot_log.txt

# Search for email errors
grep -i "mail\|error\|failed" ERP-Backend/boot_log.txt

# Check OTP creation
grep -i "generateOtp\|sendOtp" ERP-Backend/boot_log.txt
```

### Database Queries
```bash
# Login to MySQL
mysql -h localhost -u root -p2007 erp_management

# Check recent users
SELECT id, username, email, is_verified, created_at FROM users ORDER BY id DESC LIMIT 5;

# Check OTP table
SELECT * FROM otps ORDER BY id DESC LIMIT 3;

# Monitor in real-time (if supported)
WATCH "SELECT COUNT(*) as total_users, SUM(is_verified) as verified FROM users;";
```

### Frontend Console (Browser)
```javascript
// Check localStorage
JSON.parse(localStorage.getItem('uniERPUser'));

// Check last login token
localStorage.getItem('token');

// Clear all storage
localStorage.clear();

// Check verification status
localStorage.getItem('verificationEmail');
```

---

## 🎓 Learning Resources

### Backend Implementation Reference
- Spring Boot Mail: https://spring.io/guides/gs/sending-email/
- OTP Best Practices: https://owasp.org/www-community/attacks/Denial_of_Service
- JWT Auth: https://jwt.io/

### Frontend Implementation Reference
- React Hooks: https://react.dev/reference/react
- Vite Documentation: https://vitejs.dev/guide/
- Fetch API: https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API

---

## 📝 Notes

- **OTP Format**: Always 6 digits, numeric only
- **Email Expiry**: 5 minutes from generation
- **Resend Cooldown**: 30 seconds initially, 60 seconds after each resend
- **Failed Attempts**: No limit (can be added later)
- **Account Status**: Unverified users can't login but can request OTP resend
- **Database**: Uses MySQL with Hibernate auto-schema creation

---

**Created**: 2026-04-08  
**Status**: ✅ Ready for Testing  
**Version**: 1.0  
