# REVE Genie AI Teacher — Python FastAPI Backend

This is the dedicated Python FastAPI backend server for **REVE Genie AI Teacher**. It acts as a secure intermediary between the Android Jetpack Compose client app and the OpenAI API, ensuring that API keys and proprietary system prompts are never exposed on client devices.

---

## 1. Project Directory Structure

```text
python_backend/
│
├── main.py              # FastAPI server, endpoints (/health, /chat), error handling & OpenAI logic
├── requirements.txt     # Python dependencies (FastAPI, Uvicorn, OpenAI, etc.)
├── .env                 # Local environment variables (OPENAI_API_KEY, OPENAI_MODEL) [DO NOT COMMIT]
├── .env.example         # Template for environment configuration
├── .gitignore           # Ignores .env, virtual environment, and cache files
└── README.md            # Comprehensive documentation & testing guide
```

---

## 2. Prerequisites

- **Python 3.10+** (Python 3.10, 3.11, or 3.12 recommended)
- **pip** package manager
- Valid **OpenAI API Key** (starts with `sk-...`)
- Android Studio with Android Emulator (or a physical device on the same local network)

---

## 3. Installation & Setup Instructions

### Step 1: Navigate to the `python_backend` directory
Open your terminal or Windows PowerShell:
```powershell
cd python_backend
```

### Step 2: Create a Python Virtual Environment
Creating a virtual environment isolates your dependencies:

**On Windows (PowerShell):**
```powershell
python -m venv venv
.\venv\Scripts\Activate.ps1
```
*(If PowerShell restricts script execution, run: `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`)*

**On macOS / Linux:**
```bash
python3 -m venv venv
source venv/bin/activate
```

### Step 3: Install Required Dependencies
Install the pinned packages from `requirements.txt`:
```bash
pip install -r requirements.txt
```

### Step 4: Configure the OpenAI API Key in `.env`
Copy the template file to `.env`:

**On Windows (PowerShell):**
```powershell
Copy-Item .env.example .env
```

**On macOS / Linux:**
```bash
cp .env.example .env
```

Open `.env` in any text editor and replace `YOUR_OPENAI_API_KEY_HERE` with your actual secret key:
```env
OPENAI_API_KEY=sk-proj-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
OPENAI_MODEL=gpt-4o-mini
HOST=0.0.0.0
PORT=8000
```

> **Security Note:** Never commit `.env` to Git or share your secret key. The `.gitignore` file is already set up to exclude `.env`.

---

## 4. Starting the Server

Run the server with Uvicorn:

```bash
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

Alternatively, run directly with Python:
```bash
python main.py
```

Output should indicate:
```text
INFO:     Uvicorn running on http://0.0.0.0:8000 (Press CTRL+C to quit)
INFO:     Started reloader process [...]
INFO:     Application startup complete.
```

- Local host access: `http://127.0.0.1:8000`
- Android Emulator access: `http://10.0.2.2:8000`

---

## 5. Testing the Backend

### Test 1: Verify Health Status (`GET /health`)

**Using Windows PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/health" -Method Get
```

**Using cURL:**
```bash
curl -X GET http://127.0.0.1:8000/health
```

**Expected Response:**
```json
{
  "status": "ok",
  "service": "REVE Genie AI Teacher"
}
```

---

### Test 2: Verify AI Chat (`POST /chat`)

#### Scenario A: Simple Question (English)
**PowerShell:**
```powershell
$body = @{
    message = "What is Newton's First Law of Motion?"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://127.0.0.1:8000/chat" -Method Post -ContentType "application/json" -Body $body
```

#### Scenario B: Bengali Question with Educational Context (SSC NCTB Biology)
**PowerShell:**
```powershell
$body = @{
    message = "শালোকসংশ্লেষণ কি এবং এর প্রয়োজনীয় উপাদানগুলো কি কি?"
    language = "Bengali"
    context = "Education Level: SSC`nCurriculum: NCTB`nSubject: Biology`nChapter: Bioenergetics`nTopic: Photosynthesis"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://127.0.0.1:8000/chat" -Method Post -ContentType "application/json; charset=utf-8" -Body $body
```

**Using cURL (JSON):**
```bash
curl -X POST http://127.0.0.1:8000/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Explain photosynthesis in simple Bengali.",
    "language": "Bengali",
    "context": "SSC Biology, Chapter: Photosynthesis"
  }'
```

**Expected Response Format:**
```json
{
  "success": true,
  "reply": "শালোকসংশ্লেষণ (Photosynthesis) হলো উদ্ভিদের খাদ্য তৈরির জৈব-রাসায়নিক প্রক্রিয়া...",
  "error": null
}
```

---

## 6. Connecting to the Android App & Emulator

### Why `10.0.2.2`?
In the standard Android Emulator:
- `127.0.0.1` inside the emulator refers to the emulator itself (loopback).
- `10.0.2.2` is a special virtual alias routed by the Android Emulator directly to the host machine's `127.0.0.1` (your Windows PC).

### Android Network Setup
1. Ensure `python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload` is running on your host machine.
2. In your Android app, configure the base URL as:
   ```kotlin
   const val BASE_URL = "http://10.0.2.2:8000"
   ```
3. The Android client sends requests to:
   ```text
   POST http://10.0.2.2:8000/chat
   ```
4. Verify that `AndroidManifest.xml` includes:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   ```
   If testing on Android 9+ (API 28+) over plain HTTP (`http://`), ensure `android:usesCleartextTraffic="true"` is enabled in `<application>` within `AndroidManifest.xml` for development.

---

## 7. Error Handling Specification

All responses adhere to a consistent contract so the Android client never encounters unhandled crashes:

| Condition | `success` | `reply` | `error` |
|---|---|---|---|
| Successful AI Response | `true` | `<AI response text>` | `null` |
| Empty User Message | `false` | `""` | `"Please enter a question or topic for the AI Teacher."` |
| Missing/Invalid API Key | `false` | `""` | `"AI service is not configured..."` or `"AI authentication failed..."` |
| OpenAI Rate Limit / Quota | `false` | `""` | `"AI service is currently busy due to high traffic..."` |
| Network / Timeout Error | `false` | `""` | `"Unable to connect to AI service..."` |
| Unexpected Internal Error | `false` | `""` | `"An unexpected server error occurred..."` |
