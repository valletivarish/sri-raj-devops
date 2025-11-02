# Backend API Verification Script - Fixed Version
$ErrorActionPreference = 'Continue'
$baseUrl = "http://localhost:8080"
$passed = 0
$failed = 0

Write-Host "===== Backend API Verification Started =====" -ForegroundColor Cyan

# Admin Login
Write-Host "`n[1] Admin Login..." -ForegroundColor Yellow
$adminLogin = @{
    usernameOrEmail = "admin@lostfound.com"
    password = "admin123"
} | ConvertTo-Json

try {
    $adminResp = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/login" -ContentType "application/json" -Body $adminLogin
    $adminToken = $adminResp.accessToken
    Write-Host "[PASS] Admin logged in successfully" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] Admin login failed: $_" -ForegroundColor Red
    $failed++
    exit 1
}

# Register New User
Write-Host "`n[2] Register new user (Tarun)..." -ForegroundColor Yellow
$registerBody = @{
    name = "Tarun"
    username = "tarun"
    email = "tarun@example.com"
    password = "pass123"
} | ConvertTo-Json

try {
    $regResp = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/register" -ContentType "application/json" -Body $registerBody
    Write-Host "[PASS] User registered" -ForegroundColor Green
    $passed++
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "[PASS] User already exists (acceptable)" -ForegroundColor Green
        $passed++
    } else {
        Write-Host "[FAIL] Registration failed: $_" -ForegroundColor Red
        $failed++
    }
}

# User Login
Write-Host "`n[3] User Login (Tarun)..." -ForegroundColor Yellow
$userLogin = @{
    usernameOrEmail = "tarun@example.com"
    password = "pass123"
} | ConvertTo-Json

try {
    $userResp = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/login" -ContentType "application/json" -Body $userLogin
    $userToken = $userResp.accessToken
    Write-Host "[PASS] User logged in successfully" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] User login failed: $_" -ForegroundColor Red
    $failed++
    exit 1
}

# Get All Users (Admin)
Write-Host "`n[4] Get all users (as admin)..." -ForegroundColor Yellow
try {
    $users = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/users" -Headers @{ Authorization = "Bearer $adminToken" }
    Write-Host "[PASS] Retrieved $($users.Count) users" -ForegroundColor Green
    $firstUserId = $users[0].id
    $passed++
} catch {
    Write-Host "[FAIL] Get users failed: $_" -ForegroundColor Red
    $failed++
}

# Get User by ID (using actual user ID)
Write-Host "`n[5] Get user by ID..." -ForegroundColor Yellow
try {
    $user = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/users/$firstUserId" -Headers @{ Authorization = "Bearer $adminToken" }
    Write-Host "[PASS] Retrieved user: $($user.name)" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] Get user by ID failed: $_" -ForegroundColor Red
    $failed++
}

# Create Item (as user)
Write-Host "`n[6] Create item (as Tarun)..." -ForegroundColor Yellow
$itemBody = @{
    title = "Lost Bag"
    description = "Blue bag near reception"
    type = "LOST"
    tags = "bag,blue,school"
    location = "Reception"
} | ConvertTo-Json

try {
    $item = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/items" -Headers @{ Authorization = "Bearer $userToken" } -ContentType "application/json" -Body $itemBody
    $itemId = $item.id
    Write-Host "[PASS] Item created with ID: $itemId" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] Create item failed: $_" -ForegroundColor Red
    $failed++
}

# Get All Items (public)
Write-Host "`n[7] Get all items (public access)..." -ForegroundColor Yellow
try {
    $items = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/items"
    Write-Host "[PASS] Retrieved $($items.content.Count) items" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] Get items failed: $_" -ForegroundColor Red
    $failed++
}

# Get Item by ID
Write-Host "`n[8] Get item by ID..." -ForegroundColor Yellow
try {
    $singleItem = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/items/$itemId"
    Write-Host "[PASS] Retrieved item: $($singleItem.title)" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] Get item by ID failed: $_" -ForegroundColor Red
    $failed++
}

# Update Item
Write-Host "`n[9] Update item..." -ForegroundColor Yellow
$updateBody = @{
    description = "Blue bag near reception - UPDATED"
    status = "OPEN"
} | ConvertTo-Json

try {
    $updated = Invoke-RestMethod -Method Put -Uri "$baseUrl/api/items/$itemId" -Headers @{ Authorization = "Bearer $userToken" } -ContentType "application/json" -Body $updateBody
    Write-Host "[PASS] Item updated successfully" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] Update item failed: $_" -ForegroundColor Red
    $failed++
}

# Create Report
Write-Host "`n[10] Create report for item..." -ForegroundColor Yellow
$reportBody = @{
    reporterContact = "reporter@example.com"
    reason = "Found the owner"
} | ConvertTo-Json

try {
    $report = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/items/$itemId/report" -Headers @{ Authorization = "Bearer $userToken" } -ContentType "application/json" -Body $reportBody
    Write-Host "[PASS] Report created with ID: $($report.id)" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] Create report failed: $_" -ForegroundColor Red
    $failed++
}

# Get All Reports (Admin)
Write-Host "`n[11] Get all reports (as admin)..." -ForegroundColor Yellow
try {
    $reports = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/reports" -Headers @{ Authorization = "Bearer $adminToken" }
    Write-Host "[PASS] Retrieved $($reports.content.Count) reports" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] Get reports failed: $_" -ForegroundColor Red
    $failed++
}

# Claim Request Flow
Write-Host "`n[11b] Create claim request for item..." -ForegroundColor Yellow
$claimBody = @{ message = "I am the owner" } | ConvertTo-Json
try {
    $claimReq = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/items/$itemId/claim-requests" -Headers @{ Authorization = "Bearer $userToken" } -ContentType "application/json" -Body $claimBody
    $claimReqId = $claimReq.id
    Write-Host "[PASS] Claim request created: $claimReqId" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] Create claim request failed: $_" -ForegroundColor Red
    $failed++
}

Write-Host "`n[11c] List claim requests for item (admin/owner)..." -ForegroundColor Yellow
try {
    $claimList = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/items/$itemId/claim-requests" -Headers @{ Authorization = "Bearer $adminToken" }
    Write-Host "[PASS] Claim requests listed: $($claimList.content.Count)" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] List claim requests failed: $_" -ForegroundColor Red
    $failed++
}

Write-Host "`n[11d] Approve claim request (admin)..." -ForegroundColor Yellow
try {
    $approved = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/items/$itemId/claim-requests/$claimReqId/approve" -Headers @{ Authorization = "Bearer $adminToken" }
    Write-Host "[PASS] Claim approved; item should be CLAIMED" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] Approve claim failed: $_" -ForegroundColor Red
    $failed++
}

# Upload Image (use curl)
Write-Host "`n[12] Upload image..." -ForegroundColor Yellow
$tempFile = [System.IO.Path]::GetTempFileName()
"Test image content" | Out-File -FilePath $tempFile -Encoding ASCII
try {
    $curlResult = & curl.exe -s -X POST "$baseUrl/api/images/upload" -H "Authorization: Bearer $userToken" -F "file=@$tempFile"
    if ($curlResult -like "*/api/images/*") {
        Write-Host "[PASS] Image uploaded: $curlResult" -ForegroundColor Green
        $imageFilename = $curlResult -replace "/api/images/", ""
        $passed++
    } else {
        Write-Host "[FAIL] Image upload returned unexpected: $curlResult" -ForegroundColor Red
        $failed++
    }
} catch {
    Write-Host "[FAIL] Image upload failed: $_" -ForegroundColor Red
    $failed++
}
Remove-Item $tempFile -ErrorAction SilentlyContinue

# Get Image
Write-Host "`n[13] Get uploaded image..." -ForegroundColor Yellow
try {
    $imageResp = Invoke-WebRequest -Method Get -Uri "$baseUrl/api/images/$imageFilename" -UseBasicParsing
    Write-Host "[PASS] Image retrieved successfully" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] Get image failed: $_" -ForegroundColor Red
    $failed++
}

# Delete Item
Write-Host "`n[14] Delete item..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Method Delete -Uri "$baseUrl/api/items/$itemId" -Headers @{ Authorization = "Bearer $userToken" }
    Write-Host "[PASS] Item deleted successfully" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "[FAIL] Delete item failed: $_" -ForegroundColor Red
    $failed++
}

# Security Tests
Write-Host "`n[15] Security: Access protected endpoint without token..." -ForegroundColor Yellow
try {
    $attempt = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/users" -ErrorAction Stop
    Write-Host "[FAIL] Security issue: unauthorized access allowed!" -ForegroundColor Red
    $failed++
} catch {
    Write-Host "[PASS] Correctly blocked unauthorized access" -ForegroundColor Green
    $passed++
}

Write-Host "`n===== Backend API Verification Completed =====" -ForegroundColor Cyan
Write-Host "Passed: $passed | Failed: $failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Yellow" })

