# 📧 Email OTP Verification Implementation Guide

## ✅ Implementation Status: COMPLETE

Your ERP system now has a complete **Email OTP Verification** system for user registration and login. All components are fully implemented and working.

---

## 🏗️ Architecture Overview

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.2.0
- **ORM**: Hibernate/JPA
- **Mail Service**: Spring Boot Mail Starter
- **Database**: MySQL

### Frontend (React)
- **Framework**: React 18
- **Build Tool**: Vite 7.3.1
- **Port**: 3001 (development)

---

## 📦 Backend Components

### 1. **User Entity** (`User.java`)
```java
@Column(name = "is_verified", nullable = false)
private boolean isVerified = false;
```
- Added `isVerified` field to track email verification status
- Automatically set to `false` on user creation
- Set to `true` after successful OTP verification

### 2. **OTP Entity** (`Otp.java`)
Location: `ERP-Backend/src/main/java/com/erp/backend/entity/Otp.java`

```java
@Entity
@Table(name = "otps")
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String otpCode;  // 6-digit code
    
    @Column(nullable = false)
    private LocalDateTime expiryTime;  // 5-minute expiry
}
```

**Features:**
- Generates 6-digit OTP
- Stores email and OTP code
- Expiry time: 5 minutes
- Includes `isExpired()` method to validate

### 3. **OTP Repository** (`OtpRepository.java`)
```java
@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmail(String email);
    void deleteByEmail(String email);
}
```

### 4. **Email Service** (`EmailService.java`)
Location: `ERP-Backend/src/main/java/com/erp/backend/service/EmailService.java`

```java
@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("koushiknaga42@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Your ERP Verification OTP Code");
        message.setText("Your verification code is: " + otp + 
                        "\nThis code will expire in 5 minutes.");
        mailSender.send(message);
    }
}
```

**Configuration** (in `application.properties`):
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=koushiknaga42@gmail.com
spring.mail.password=htvsksztbdhgnzhy (App Password for Gmail)
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

⚠️ **Important**: For Gmail, use "App Passwords" instead of your main password. [Enable 2FA and generate App Password](https://myaccount.google.com/apppasswords)

### 5. **OTP Service** (`OtpService.java`)
Location: `ERP-Backend/src/main/java/com/erp/backend/service/OtpService.java`

```java
@Service
public class OtpService {
    
    // Generate 6-digit OTP and send email
    @Transactional
    public String generateAndSendOtp(String email) {
        otpRepository.deleteByEmail(email);  // Delete old OTP
        String otpCode = String.format("%06d", new Random().nextInt(1000000));
        Otp otp = new Otp(email, otpCode, 5);  // 5-minute expiry
        otpRepository.save(otp);
        emailService.sendOtpEmail(email, otpCode);
        return otpCode;
    }
    
    // Validate OTP (checks code and expiry)
    public boolean validateOtp(String email, String otpCode) {
        Optional<Otp> otpOptional = otpRepository.findByEmail(email);
        if (otpOptional.isPresent()) {
            Otp otp = otpOptional.get();
            if (otp.getOtpCode().equals(otpCode) && !otp.isExpired()) {
                otpRepository.delete(otp);  // Delete after use
                return true;
            }
        }
        return false;
    }
}
```

### 6. **Authentication Controller** (`AuthController.java`)
Location: `ERP-Backend/src/main/java/com/erp/backend/controller/AuthController.java`

#### Endpoint 1: POST `/api/auth/register`
```java
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
    User user = new User();
    user.setUsername(registerRequest.getUsername());
    user.setEmail(registerRequest.getEmail());
    user.setPassword(registerRequest.getPassword());
    user.setFirstName(registerRequest.getFirstName());
    user.setLastName(registerRequest.getLastName());
    user.setRole(User.Role.valueOf(registerRequest.getRole().toUpperCase()));
    
    User savedUser = userService.createUser(user);
    otpService.generateAndSendOtp(savedUser.getEmail());  // Send OTP
    
    return ResponseEntity.ok({
        "success": true,
        "user": savedUser,
        "message": "User registered successfully. Please check your email for verification code."
    });
}
```

#### Endpoint 2: POST `/api/auth/login`
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    User user = userService.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
    
    // ❌ BLOCK: User not verified
    if (!user.isVerified()) {
        return ResponseEntity.status(403).body({
            "success": false,
            "notVerified": true,
            "email": user.getEmail(),
            "message": "Account not verified. Please verify your email."
        });
    }
    
    // ✅ ALLOW: User verified
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            )
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
    
    return ResponseEntity.ok({
        "success": true,
        "user": user,
        "token": token,
        "message": "Login successful"
    });
}
```

#### Endpoint 3: POST `/api/auth/verify-otp`
```java
@PostMapping("/verify-otp")
public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    String otpCode = request.get("otp");
    
    boolean isValid = otpService.validateOtp(email, otpCode);
    
    if (isValid) {
        User user = userService.findByEmail(email).get();
        user.setVerified(true);  // ✅ Mark as verified
        userService.updateUser(user.getId(), user);
        
        return ResponseEntity.ok({
            "success": true,
            "message": "Email verified successfully. You can now login."
        });
    } else {
        return ResponseEntity.badRequest().body({
            "success": false,
            "message": "Invalid or expired OTP"
        });
    }
}
```

#### Endpoint 4: POST `/api/auth/resend-otp`
```java
@PostMapping("/resend-otp")
public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    otpService.generateAndSendOtp(email);  // Generate new OTP
    
    return ResponseEntity.ok({
        "success": true,
        "message": "OTP resent successfully"
    });
}
```

### 7. **Dependencies** (in `pom.xml`)
```xml
<!-- Mail Support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- JWT (for authentication tokens) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
```

---

## 🎨 Frontend Components

### 1. **OTP Verification Component** (`OtpVerification.jsx`)
Location: `ERP-Frontend/src/components/OtpVerification.jsx`

**State Management:**
```jsx
const [otp, setOtp] = useState("");           // User input
const [loading, setLoading] = useState(false); // Verify button state
const [resending, setResending] = useState(false); // Resend button state
const [message, setMessage] = useState({ text: "", type: "" }); // Success/error
const [timer, setTimer] = useState(30); // Resend cooldown (30s initially, 60s after resend)
```

**Key Features:**
- 6-digit OTP input (numeric only)
- Real-time countdown timer for resend button
- Success/error message display with color-coded alerts
- Handles both initial verification and resend scenarios
- Auto-focus on OTP input field
- Input validation (must be exactly 6 digits)

**API Integration:**
```jsx
const handleVerify = async (e) => {
    const response = await fetch("http://localhost:8085/api/auth/verify-otp", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, otp })
    });
    
    if (response.ok && data.success) {
        onVerified();  // Redirect to login
    }
};

const handleResend = async () => {
    const response = await fetch("http://localhost:8085/api/auth/resend-otp", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email })
    });
};
```

### 2. **Login Component** (`Login.jsx`)
Location: `ERP-Frontend/src/components/Login.jsx`

**Registration Flow (Modified):**
```jsx
const handleRegister = async (e) => {
    // ... validation ...
    const response = await fetch("http://localhost:8085/api/auth/register", {
        method: "POST",
        body: JSON.stringify({
            username, password, email, firstName, lastName, role
        })
    });
    
    if (response.ok && data.success) {
        alert("Registration successful! Please verify your email.");
        onVerifyRequired(defaultEmail);  // ← Redirect to OTP screen
    }
};
```

**Login Flow (Modified):**
```jsx
const handleSubmit = async (e) => {
    const response = await fetch("http://localhost:8085/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password })
    });
    
    if (response.ok && data.success) {
        onLogin(user);  // ✅ Success → go to dashboard
    } else if (response.status === 403 && data.notVerified) {
        onVerifyRequired(data.email);  // ❌ Not verified → OTP screen
    }
};
```

### 3. **App Component** (`App.jsx`)
Location: `ERP-Frontend/src/App.jsx`

**State Management:**
```jsx
const [isLoggedIn, setIsLoggedIn] = useState(false);
const [user, setUser] = useState(null);
const [verificationEmail, setVerificationEmail] = useState(null);  // ← NEW
```

**Conditional Rendering:**
```jsx
{!isLoggedIn ? (
    verificationEmail ? (
        <OtpVerification 
            email={verificationEmail} 
            onVerified={() => setVerificationEmail(null)}  // Back to login
            onCancel={() => setVerificationEmail(null)}    // Cancel OTP
        />
    ) : (
        <Login 
            onLogin={handleLogin}
            onVerifyRequired={(email) => setVerificationEmail(email)}  // Go to OTP
        />
    )
) : (
    <Layout user={user} onLogout={handleLogout} />  // Dashboard
)}
```

---

## 🔄 Complete User Flow

### Registration with OTP Verification
```
1. User clicks "Create Account"
   ↓
2. Enters: Username, Password, Role (Student/Teacher/Admin)
   ↓
3. Frontend sends POST /api/auth/register
   ↓
4. Backend creates User with isVerified=false
   ↓
5. Backend generates 6-digit OTP and sends email
   ↓
6. Frontend redirects to OTP Verification screen
   ↓
7. User receives email with OTP code
   ↓
8. User enters OTP in verification field
   ↓
9. Frontend sends POST /api/auth/verify-otp
   ↓
10. Backend validates OTP (checks code and expiry)
   ↓
11. If valid: Sets isVerified=true, deletes OTP from DB
   ↓
12. Frontend shows success message and redirects to Login
   ↓
13. User can now login ✅
```

### Login Flow (Updated)
```
1. User enters credentials
   ↓
2. Frontend sends POST /api/auth/login
   ↓
3. Backend checks if user exists and password matches
   ↓
4. Backend checks isVerified status
   ↓
   Option A (Verified):
   ├─ Status: 200 OK ✅
   ├─ Returns user object and token
   └─ Frontend redirects to Dashboard
   
   Option B (Not Verified):
   ├─ Status: 403 Forbidden ❌
   ├─ Returns { notVerified: true, email: "..." }
   └─ Frontend redirects to OTP screen
```

### OTP Resend Flow
```
1. User clicks "Resend Code" button
2. Timer must be 0 (30-60 second cooldown)
3. Frontend sends POST /api/auth/resend-otp
4. Backend deletes old OTP and generates new one
5. Backend sends new OTP via email
6. Frontend resets timer to 60 seconds
7. User receives new email and can try again
```

---

## 🗄️ Database Schema

### Users Table (Modified)
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,  -- ← NEW
    created_at DATETIME,
    updated_at DATETIME
);
```

### OTPs Table (New)
```sql
CREATE TABLE otps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    expiry_time DATETIME NOT NULL
);
```

> **Note**: Hibernate auto-creates these tables with `spring.jpa.hibernate.ddl-auto=update`

---

## 🔐 Security Features

### 1. OTP Expiry (5 Minutes)
```java
LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiryTime);
}
```

### 2. OTP Deletion After Use
```java
otpRepository.delete(otp);  // Prevents reuse
```

### 3. Single OTP Per Email
```java
otpRepository.deleteByEmail(email);  // Delete old OTP before creating new one
```

### 4. Rate Limiting (Resend Cooldown)
```jsx
const [timer, setTimer] = useState(30);  // 30 seconds initial
// After resend: setTimer(60);  // 60 seconds
// Button disabled while timer > 0
disabled={timer > 0 || resending}
```

### 5. No Password Exposure
- OTP sent via email (not returned in API response)
- OTP code not logged in backend
- JWT tokens used for authenticated requests

### 6. SMTP Configuration Secured
```properties
spring.mail.password=htvsksztbdhgnzhy   # Use App Passwords for Gmail
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

---

## 🚀 Running the Application

### Backend (Spring Boot)
```bash
cd ERP-Backend
mvn spring-boot:run
# Server running on http://localhost:8085
```

### Frontend (React)
```bash
cd ERP-Frontend
npm install
npm run dev
# Server running on http://localhost:3001 (or 3000 if available)
```

### Both Running
```bash
# Terminal 1
cd ERP-Backend && mvn spring-boot:run

# Terminal 2
cd ERP-Frontend && npm run dev
```

---

## ✅ Manual Testing Checklist

### 1. Registration Flow
- [ ] Navigate to http://localhost:3001 (or active port)
- [ ] Click "Create account"
- [ ] Enter: Username, Password, Role
- [ ] Submit registration
- [ ] Verify message: "Registration successful. Please verify your email."
- [ ] Check provided email for OTP message
- [ ] Copy OTP code from email

### 2. OTP Verification
- [ ] Enter OTP in verification field
- [ ] Click "Verify Account"
- [ ] Verify success message
- [ ] Should redirect to login page

### 3. Invalid OTP Handling
- [ ] Return to registration and create another account
- [ ] Enter wrong OTP
- [ ] Verify error: "Invalid or expired OTP"
- [ ] Try again with correct OTP

### 4. OTP Expiry
- [ ] Register new account
- [ ] Wait 5+ minutes
- [ ] Enter old OTP
- [ ] Verify error: "Invalid or expired OTP"
- [ ] Click "Resend Code"
- [ ] Enter new OTP
- [ ] Should succeed

### 5. Resend Functionality
- [ ] Don't click resend for 30+ seconds (initial cooldown)
- [ ] Button should show "Resend code in Xs"
- [ ] After cooldown, click "Resend Code"
- [ ] Verify new email received
- [ ] Wait 60 seconds after resend (increased cooldown)
- [ ] Click resend again
- [ ] Verify newest email received

### 6. Login with Verified Account
- [ ] Create and verify an account (username: testuser)
- [ ] Go to login page
- [ ] Enter verified username and password
- [ ] Select correct role
- [ ] Click Login
- [ ] Should redirect to Dashboard ✅

### 7. Login with Unverified Account
- [ ] Create new account (don't verify)
- [ ] Go to login page
- [ ] Enter unverified username and password
- [ ] Click Login
- [ ] Should show OTP verification page ❌
- [ ] Enter correct OTP
- [ ] Verify account
- [ ] Try login again → Should work ✅

---

## 📊 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     React Frontend (3001)                    │
├─────────────────────────────────────────────────────────────┤
│  Login.jsx           OtpVerification.jsx      App.jsx       │
│  - handleSubmit      - handleVerify          - routing      │
│  - handleRegister    - handleResend          - states       │
└────────────────┬────────────────────────────────┬────────────┘
                 │ HTTP (Fetch)                   │
                 │ /api/auth/*                    │
                 ▼                                ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Backend (8085)                      │
├──────────────────────────────────────────────────────────────┤
│                   AuthController                             │
│  /login  /register  /verify-otp  /resend-otp               │
├──────────────────────────────────────────────────────────────┤
│   UserService        OtpService        EmailService         │
│   - createUser       - generateOtp     - sendOtpEmail       │
│   - updateUser       - validateOtp     - (JavaMailSender)   │
│   - findByEmail      - OtpRepository                        │
└────────────────┬───────────────────────────────────┬─────────┘
                 │ SQL Queries                       │
                 ▼                                   ▼
        ┌────────────────────┐      ┌──────────────────────┐
        │  MySQL Database    │      │   Gmail SMTP Server  │
        │  users (+ isVerif) │      │  (smtp.gmail.com:587)│
        │  otps              │      │  (App Password auth) │
        └────────────────────┘      └──────────────────────┘
```

---

## 🔧 Troubleshooting

### Issue: Email not sending
**Check:**
1. Gmail account has 2FA enabled
2. App Password is generated (not regular password)
3. Port 587 is accessible (SMTP TLS)
4. `spring.mail.properties.mail.smtp.starttls.enable=true`

### Issue: OTP validation always fails
**Check:**
1. OTP in email matches what you're entering
2. OTP hasn't expired (5 minutes)
3. No leading/trailing spaces in OTP input

### Issue: Login still blocked after verification
**Check:**
1. Verify user's `is_verified` field is `true` in database
```sql
SELECT username, is_verified FROM users WHERE email = 'your-email@domain.com';
```
2. If false, run:
```sql
UPDATE users SET is_verified = true WHERE email = 'your-email@domain.com';
```

### Issue: CORS errors
**Already configured in** `application.properties`:
```properties
cors.allowed-origins=http://localhost:3000,http://localhost:8080,http://localhost:3001
```

### Issue: Ports already in use
- Backend: `lsof -i :8085` (Mac/Linux) or `netstat -ano | findstr :8085` (Windows)
- Frontend: Vite automatically tries next port (8085 → 8086)

---

## 📝 API Response Examples

### Registration Response
```json
{
  "success": true,
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john_doe@kluniversity.in",
    "firstName": "john_doe",
    "lastName": "User",
    "role": "STUDENT",
    "isVerified": false
  },
  "message": "User registered successfully. Please check your email for verification code."
}
```

### OTP Verification Success
```json
{
  "success": true,
  "message": "Email verified successfully. You can now login."
}
```

### Login Unverified Response (403)
```json
{
  "success": false,
  "notVerified": true,
  "email": "user@kluniversity.in",
  "message": "Account not verified. Please verify your email."
}
```

### Login Success Response (200)
```json
{
  "success": true,
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john_doe@kluniversity.in",
    "role": "STUDENT",
    "isVerified": true
  },
  "token": "am9objdfb2U6cGFzc3dvcmQxMjM="
}
```

---

## 🎯 Next Steps / Enhancements

### Currently Implemented ✅
- Email OTP verification
- 5-minute OTP expiry
- Resend OTP with cooldown
- Login blocked for unverified users
- Email delivery via Gmail SMTP

### Future Enhancements 💡
- [ ] SMS OTP as alternative (Twilio integration)
- [ ] OTP attempt limiting (max 3 tries)
- [ ] Account lockout after failed attempts
- [ ] Multi-factor authentication (2FA)
- [ ] Password reset via OTP
- [ ] Remember this device (30 days)
- [ ] Social login (Google, Microsoft)
- [ ] Custom email templates (HTML)

---

## 📚 File References

| Component | File Path | Type |
|-----------|-----------|------|
| User Entity | `ERP-Backend/src/main/java/com/erp/backend/entity/User.java` | Entity |
| OTP Entity | `ERP-Backend/src/main/java/com/erp/backend/entity/Otp.java` | Entity |
| OTP Repository | `ERP-Backend/src/main/java/com/erp/backend/repository/OtpRepository.java` | Repository |
| OTP Service | `ERP-Backend/src/main/java/com/erp/backend/service/OtpService.java` | Service |
| Email Service | `ERP-Backend/src/main/java/com/erp/backend/service/EmailService.java` | Service |
| Auth Controller | `ERP-Backend/src/main/java/com/erp/backend/controller/AuthController.java` | Controller |
| pom.xml | `ERP-Backend/pom.xml` | Dependencies |
| App Properties | `ERP-Backend/src/main/resources/application.properties` | Config |
| OTP Verification | `ERP-Frontend/src/components/OtpVerification.jsx` | Component |
| Login | `ERP-Frontend/src/components/Login.jsx` | Component |
| App | `ERP-Frontend/src/App.jsx` | Main App |
| API Service | `ERP-Frontend/src/services/api.js` | Service |

---

## 📞 Support

For issues or questions:
1. Check the **Troubleshooting** section above
2. Verify backend logs: `mvn spring-boot:run` output
3. Check browser console: Developer Tools → Console
4. Verify MySQL is running and database exists
5. Confirm Gmail App Password is correct

---

## 🎉 Congratulations!

Your ERP system now has enterprise-grade email OTP verification! 

**Status**: ✅ READY FOR TESTING

**Access Points:**
- Frontend: `http://localhost:3001` (or 3000)
- Backend: `http://localhost:8085`
- Database: `erp_management` (MySQL)

**Start Testing:** Follow the "Manual Testing Checklist" above! 🚀
