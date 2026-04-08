# ✅ Email Field Update - Complete Testing Guide

## 🎉 What's New

Users now **enter their actual email** during registration instead of having one auto-generated from their username!

---

## 📍 Access the Updated Application

| Component | URL | Status |
|-----------|-----|--------|
| **Frontend** | http://localhost:3002 | ✅ Running |
| **Backend** | http://localhost:8085 | ✅ Running |

---

## 📝 Registration Form - New Layout

### Before ❌
```
┌────────────────────────────────────┐
│        Create Your Account         │
├────────────────────────────────────┤
│ Username: [enter username]         │
│ Password: [enter password]         │
│ Role: [select role]                │
└────────────────────────────────────┘
Email generated: username@kluniversity.in
```

### After ✅
```
┌────────────────────────────────────┐
│        Create Your Account         │
├────────────────────────────────────┤
│ Username: [enter username]         │
│ Email: [enter email] ← NEW!        │
│ Password: [enter password]         │
│ Role: [select role]                │
└────────────────────────────────────┘
Email sent to: user-provided@email.com
```

---

## 🧪 Step-by-Step Test Guide

### Test 1: Basic Registration with Real Email

**Objective**: Register with your actual email and receive OTP

#### Step 1: Open Registration
1. Go to http://localhost:3002
2. Click **"Create account"** link
3. You'll see the signup form

#### Step 2: Fill Registration Form
```
Field           | Example Value
─────────────────────────────────────
Username        | john_doe
Email           | john.doe@gmail.com  ← ENTER YOUR REAL EMAIL!
Password        | Password@123
Role            | Student
```

#### Step 3: Create Account
1. Fill in all fields with YOUR actual email
2. Click **"Create Account"** button

#### Expected Result ✅
```
Alert Message: "Registration successful! Please verify your email."
↓
Redirect to OTP verification screen
```

#### Step 4: Check Your Email
1. Open your email inbox
2. Look for email from: `koushiknaga42@gmail.com`
3. Subject: `Your ERP Verification OTP Code`
4. Copy the 6-digit OTP code (e.g., `582941`)

---

### Test 2: OTP Verification with Received Code

**Objective**: Verify the OTP sent to your email

#### Step 1: Enter OTP on Verification Screen
```
Verification screen shows:
"We've sent a 6-digit code to john.doe@gmail.com"
```

#### Step 2: Paste OTP
1. Paste the 6-digit code from your email
2. Example: If email says `OTP: 582941` → paste `582941`
3. Make sure there are NO spaces

#### Step 3: Click "Verify Account"

#### Expected Result ✅
```
Success Message: "Email verified successfully. You can now login."
↓
Redirect to Login page
```

---

### Test 3: Login with Verified Account

**Objective**: Login with verified email account

#### Step 1: Fill Login Form
```
Field           | Value
─────────────────────────────────────
Username        | john_doe
Password        | Password@123
Role            | Student
CAPTCHA         | [enter displayed code]
```

#### Step 2: Submit Login
1. Enter all credentials
2. Click **"Login"** button

#### Expected Result ✅
```
✓ Dashboard loads
✓ User info appears in top-right
✓ Full system access granted
```

---

### Test 4: Wrong Email Format Validation

**Objective**: Verify email validation works

#### Step 1: Try Invalid Email Format
1. Go to registration
2. Fill form with:
   ```
   Username: test_user
   Email: invalidemail      ← NO @ symbol
   Password: password123
   ```
3. Click "Create Account"

#### Expected Result ❌
```
Alert: "Please enter a valid email address"
↓
Stays on registration form (doesn't submit)
```

#### Step 2: Try Another Invalid Format
1. Fill with:
   ```
   Email: test@gmail        ← NO .com
   ```
2. Click "Create Account"

#### Expected Result ❌
```
Alert: "Please enter a valid email address"
```

#### Step 3: Now Try Valid Format
1. Fill with:
   ```
   Email: test@gmail.com    ← VALID ✓
   ```
2. Click "Create Account"

#### Expected Result ✅
```
Proceeds to submit and sends OTP to the email ✓
```

---

### Test 5: Username Validation

**Objective**: Verify username format validation

#### Step 1: Invalid Username (with spaces)
1. Go to registration
2. Username: `john doe` (with space)
3. Click "Create Account"

#### Expected Result ❌
```
Alert: "Username can only contain letters, numbers, and underscores"
```

#### Step 2: Invalid Username (with special chars)
1. Username: `john@doe` (with @)
2. Click "Create Account"

#### Expected Result ❌
```
Same error message
```

#### Step 3: Valid Username
1. Username: `john_doe_123` (letters, numbers, underscore only)
2. Click "Create Account"

#### Expected Result ✅
```
Proceeds normally ✓
```

---

### Test 6: Password Validation

**Objective**: Verify password length validation

#### Step 1: Short Password
```
Username: testuser
Email: test@gmail.com
Password: abc       ← Less than 6 chars
```
Click "Create Account"

#### Expected Result ❌
```
Alert: "Password must be at least 6 characters long"
```

#### Step 2: Valid Password
```
Password: password123   ← 6+ characters
```
Click "Create Account"

#### Expected Result ✅
```
Proceeds normally ✓
```

---

### Test 7: Multiple Users (Different Emails)

**Objective**: Verify system handles multiple registrations

#### Create User 1
```
Username: user1
Email: user1@gmail.com
Password: password123
Role: Student
```
→ Verify with OTP from user1@gmail.com

#### Create User 2
```
Username: user2
Email: user2@yahoo.com
Password: password456
Role: Teacher
```
→ Verify with OTP from user2@yahoo.com

#### Create User 3
```
Username: user3
Email: user3@outlook.com
Password: password789
Role: Admin
```
→ Verify with OTP from user3@outlook.com

#### Expected Result ✅
```
✓ Each user receives OTP to their own email
✓ Each user verifies successfully
✓ All three can login independently
```

---

### Test 8: Unverified User Cannot Login

**Objective**: Verify unverified users are blocked

#### Step 1: Register but Don't Verify
1. Register: `unverified_user`
2. Do NOT complete OTP verification
3. Go back to login page manually

#### Step 2: Try to Login
```
Username: unverified_user
Password: [your password]
Role: Student
```
Click "Login"

#### Expected Result ❌
```
Alert: "Account not verified. Please verify your email."
↓
Redirect to OTP verification screen
```

#### Step 3: Complete Verification
1. Check email for OTP
2. Enter OTP and verify
3. Go back to login

#### Step 4: Try Login Again
```
Same credentials as before
```
Click "Login"

#### Expected Result ✅ (Now Works!)
```
✓ Login successful
✓ Dashboard loads
```

---

### Test 9: Resend OTP While Unverified

**Objective**: Verify resend email reaches user's email

#### Step 1: Register and Go to OTP Screen
```
Username: resend_test
Email: resend_test@gmail.com
Password: password123
```

#### Step 2: Try Resend Immediately
Click "Resend Code" button

#### Expected Result ❌
```
Button disabled: "Resend code in 30s"
Countdown timer visible
```

#### Step 3: Wait 30 Seconds
Watch the timer count down to 0

#### Step 4: Click Resend Again
Once timer reaches 0, button becomes clickable

#### Expected Result ✅
```
New email sent to resend_test@gmail.com
Check inbox for new OTP (may be newer email)
New OTP code should be different from first one
```

---

### Test 10: OTP Expiry Timeline

**Objective**: Verify OTP expires after 5 minutes

#### Step 1: Register
```
Username: expiry_test
Email: expiry_test@gmail.com
Password: password123
```

#### Step 2: Get OTP from Email
Copy the OTP code: `123456`
**Note the time**: e.g., 2:00 PM

#### Step 3: Wait 5 Minutes
Set a timer for exactly 5 minutes
Let it run while doing something else

#### Step 4: (After 5+ minutes) Try Old OTP
Enter the OTP code you copied earlier
Click "Verify Account"

#### Expected Result ❌
```
Alert: "Invalid or expired OTP"
```

#### Step 5: Resend and Use New OTP
Click "Resend Code"
Check email for new OTP
Enter new OTP

#### Expected Result ✅ (Works!)
```
✓ New OTP works
✓ Verification succeeds
```

---

## ✅ Validation Checklist

Fill out as you test:

```
[_] Email field appears in signup form
[_] Can enter email address
[_] Invalid email format shows error
[_] Valid email format accepts input
[_] OTP email arrives at user's email address
[_] OTP is 6 digits
[_] Correct OTP allows verification
[_] Wrong OTP shows error
[_] Verified user can login
[_] Unverified user cannot login
[_] Unverified user redirects to OTP screen
[_] Resend button works after cooldown
[_] OTP expires after 5 minutes
[_] New OTP works after resend
[_] Multiple users receive separate emails
[_] Password validation works
[_] Username validation works
[_] All required fields validated
```

---

## 🐛 Troubleshooting

### Issue 1: Email Field Not Showing

**Solution:**
1. Hard refresh page: `Ctrl+Shift+R` (Windows) or `Cmd+Shift+R` (Mac)
2. Clear browser cache
3. Open in incognito/private window
4. Check browser console for errors: `F12` → Console tab

### Issue 2: OTP Email Not Received

**Solutions:**
1. Check spam/promotions folder
2. Verify Gmail account is set up correctly
3. Check backend logs for email sending errors
4. Try registering again to resend
5. Verify SMTP credentials in `application.properties`

### Issue 3: "Please enter a valid email address" Error

**Check:**
- Email must have `@` symbol
- Email must have domain: `@gmail.com`, `@yahoo.com`, etc.
- No spaces in email
- Examples of VALID emails:
  - ✅ john@gmail.com
  - ✅ user.name@company.co.uk
  - ✅ test123@outlook.com
- Examples of INVALID:
  - ❌ john@gmail (missing .com)
  - ❌ john gmail.com (missing @)
  - ❌ john@.com (missing domain name)

### Issue 4: Username Validation Error

**Check:**
- Use only: `a-z`, `A-Z`, `0-9`, `_`
- NO spaces, NO special characters (@#$%^&*)
- Examples:
  - ✅ john_doe
  - ✅ user123
  - ✅ admin_2026
  - ❌ john doe (space)
  - ❌ john@doe (@)

### Issue 5: Password Error "Must be at least 6 characters"

**Solution:**
- Make password 6 or more characters
- Examples:
  - ✅ password (8 chars)
  - ✅ pass123 (7 chars)
  - ✅ 123456 (6 chars)
  - ❌ pass1 (5 chars)

---

## 📊 Expected User Journey

```
Registration Page
      ↓
[Enter: username, EMAIL, password, role]
      ↓
Click "Create Account"
      ↓
Backend: Validates all fields
      ↓
Backend: Creates user + generates OTP
      ↓
Backend: Sends OTP to USER'S EMAIL (not generated!)
      ↓
Frontend: Shows "Check your email for OTP"
      ↓
OTP Verification Page
      ↓
User: Gets email with OTP to their inbox
      ↓
[Enter 6-digit OTP]
      ↓
Click "Verify Account"
      ↓
Backend: Validates OTP
      ↓
Verified ✅
      ↓
Frontend: "Success! You can now login"
      ↓
Login Page
      ↓
[Enter: username, password, role]
      ↓
Backend: Check isVerified = true
      ↓
✅ Login Success → Dashboard
```

---

## 📞 Common Questions

### Q: Why can't I use my real university email?
**A:** You can! Use any valid email format (gmail, outlook, yahoo, university email, etc.). Just make sure it has the format: `something@domain.com`

### Q: What if I don't have a Gmail account?
**A:** Use any email provider:
- Gmail: `user@gmail.com`
- Outlook: `user@outlook.com`
- Yahoo: `user@yahoo.com`
- University: `user@university.edu`
- Company: `user@company.com`

### Q: How long is the OTP valid?
**A:** 5 minutes from when it was sent

### Q: Can I resend the OTP?
**A:** Yes! Wait 30 seconds after the first attempt, then click "Resend Code" to get a new OTP

### Q: What if I forget my OTP?
**A:** Resend it! Click the "Resend Code" button (after cooldown) to get a new OTP in your email

### Q: Can I use the same email for multiple accounts?
**A:** No, each email can only register once. But you can register different emails!

---

## 🎯 Quick Test (3 minutes)

If you only have 3 minutes:

1. **Register** with YOUR real email
   - Username: `quicktest`
   - Email: YOUR email
   - Password: `test123`

2. **Check email** for OTP code

3. **Verify** the OTP

4. **Login** with username + password

5. ✅ If you see dashboard = **WORKING!**

---

## 🚀 You're All Set!

The system now works with real user emails:
- ✅ Users enter their own email
- ✅ OTP sent to their email
- ✅ Perfect for production use

**Start testing at**: http://localhost:3002

---

**Status**: ✅ Ready for testing  
**Frontend URL**: http://localhost:3002  
**Backend URL**: http://localhost:8085  
**Date**: 2026-04-09
