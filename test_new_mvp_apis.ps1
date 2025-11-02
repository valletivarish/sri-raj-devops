# Test New MVP APIs
$ErrorActionPreference = 'Continue'
$baseUrl = "http://localhost:8080"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Testing NEW MVP APIs" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Start-Sleep -Seconds 5

# Login to get tokens
Write-Host "[Setup] Logging in..." -ForegroundColor Yellow
$userLogin = @{
    usernameOrEmail = "tarun@example.com"
    password = "pass123"
} | ConvertTo-Json

$userResp = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/login" -ContentType "application/json" -Body $userLogin
$userToken = $userResp.accessToken

$adminLogin = @{
    usernameOrEmail = "admin@lostfound.com"
    password = "admin123"
} | ConvertTo-Json

$adminResp = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/login" -ContentType "application/json" -Body $adminLogin
$adminToken = $adminResp.accessToken

Write-Host "Tokens obtained successfully`n" -ForegroundColor Green

# Test 1: GET /api/users/me
Write-Host "[1] GET /api/users/me - Get current user profile" -ForegroundColor Yellow
try {
    $me = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/users/me" -Headers @{ Authorization = "Bearer $userToken" }
    Write-Host "[PASS] Current user: $($me.name) ($($me.email))" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] $_" -ForegroundColor Red
}

# Test 2: PUT /api/users/me
Write-Host "`n[2] PUT /api/users/me - Update current user profile" -ForegroundColor Yellow
try {
    $updateBody = @{
        name = "Tarun Updated"
    } | ConvertTo-Json
    $updated = Invoke-RestMethod -Method Put -Uri "$baseUrl/api/users/me" -Headers @{ Authorization = "Bearer $userToken" } -ContentType "application/json" -Body $updateBody
    Write-Host "[PASS] Profile updated: $($updated.name)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] $_" -ForegroundColor Red
}

# Test 3: PUT /api/users/me/password
Write-Host "`n[3] PUT /api/users/me/password - Change password" -ForegroundColor Yellow
try {
    $pwdBody = @{
        currentPassword = "pass123"
        newPassword = "newpass123"
    } | ConvertTo-Json
    $pwdResp = Invoke-RestMethod -Method Put -Uri "$baseUrl/api/users/me/password" -Headers @{ Authorization = "Bearer $userToken" } -ContentType "application/json" -Body $pwdBody
    Write-Host "[PASS] $pwdResp" -ForegroundColor Green
    
    # Change it back
    $pwdBackBody = @{
        currentPassword = "newpass123"
        newPassword = "pass123"
    } | ConvertTo-Json
    Invoke-RestMethod -Method Put -Uri "$baseUrl/api/users/me/password" -Headers @{ Authorization = "Bearer $userToken" } -ContentType "application/json" -Body $pwdBackBody | Out-Null
    Write-Host "  (Password restored to original)" -ForegroundColor Gray
} catch {
    Write-Host "[FAIL] $_" -ForegroundColor Red
}

# Test 4: POST /api/items (create item for testing)
Write-Host "`n[4] Creating test item for MY items test..." -ForegroundColor Yellow
$itemBody = @{
    title = "Test MVP Item"
    description = "For testing my items endpoint"
    type = "LOST"
    tags = "test,mvp"
    location = "Office"
} | ConvertTo-Json

try {
    $item = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/items" -Headers @{ Authorization = "Bearer $userToken" } -ContentType "application/json" -Body $itemBody
    $itemId = $item.id
    Write-Host "[PASS] Test item created with ID: $itemId" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] $_" -ForegroundColor Red
}

# Test 5: GET /api/items/my
Write-Host "`n[5] GET /api/items/my - Get my posted items" -ForegroundColor Yellow
try {
    $myItems = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/items/my" -Headers @{ Authorization = "Bearer $userToken" }
    Write-Host "[PASS] Retrieved $($myItems.content.Count) items posted by current user" -ForegroundColor Green
    $myItems.content | Select-Object -First 3 | ForEach-Object { Write-Host "  - $($_.title) (Status: $($_.status))" -ForegroundColor Gray }
} catch {
    Write-Host "[FAIL] $_" -ForegroundColor Red
}

# Test 6: PATCH /api/items/{id}/status
Write-Host "`n[6] PATCH /api/items/$itemId/status - Update item status" -ForegroundColor Yellow
try {
    $statusBody = @{
        status = "CLAIMED"
    } | ConvertTo-Json
    $statusUpdated = Invoke-RestMethod -Method Patch -Uri "$baseUrl/api/items/$itemId/status" -Headers @{ Authorization = "Bearer $userToken" } -ContentType "application/json" -Body $statusBody
    Write-Host "[PASS] Item status updated to: $($statusUpdated.status)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] $_" -ForegroundColor Red
}

# Test 7: GET /api/dashboard/stats
Write-Host "`n[7] GET /api/dashboard/stats - Dashboard statistics (admin)" -ForegroundColor Yellow
try {
    $stats = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/dashboard/stats" -Headers @{ Authorization = "Bearer $adminToken" }
    Write-Host "[PASS] Dashboard stats retrieved:" -ForegroundColor Green
    Write-Host "  - Total Users: $($stats.totalUsers)" -ForegroundColor Gray
    Write-Host "  - Total Items: $($stats.totalItems)" -ForegroundColor Gray
    Write-Host "  - Total Reports: $($stats.totalReports)" -ForegroundColor Gray
} catch {
    Write-Host "[FAIL] $_" -ForegroundColor Red
}

# Test 8: POST /api/images/upload & DELETE /api/images/{filename}
Write-Host "`n[8] POST /api/images/upload + DELETE /api/images/{filename}" -ForegroundColor Yellow
$tempFile = [System.IO.Path]::GetTempFileName()
"Test MVP image content" | Out-File -FilePath $tempFile -Encoding ASCII
try {
    $uploadResult = & curl.exe -s -X POST "$baseUrl/api/images/upload" -H "Authorization: Bearer $userToken" -F "file=@$tempFile"
    if ($uploadResult -like "*/api/images/*") {
        Write-Host "[PASS] Image uploaded: $uploadResult" -ForegroundColor Green
        $imageFilename = $uploadResult -replace "/api/images/", ""
        
        # Test DELETE
        try {
            $deleteResp = Invoke-RestMethod -Method Delete -Uri "$baseUrl/api/images/$imageFilename" -Headers @{ Authorization = "Bearer $userToken" }
            Write-Host "[PASS] Image deleted: $deleteResp" -ForegroundColor Green
        } catch {
            Write-Host "[FAIL] Delete failed: $_" -ForegroundColor Red
        }
    } else {
        Write-Host "[FAIL] Upload returned unexpected: $uploadResult" -ForegroundColor Red
    }
} catch {
    Write-Host "[FAIL] $_" -ForegroundColor Red
}
Remove-Item $tempFile -ErrorAction SilentlyContinue

# Cleanup test item
Write-Host "`n[Cleanup] Deleting test item..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Method Delete -Uri "$baseUrl/api/items/$itemId" -Headers @{ Authorization = "Bearer $userToken" } | Out-Null
    Write-Host "Test item deleted" -ForegroundColor Gray
} catch {
    Write-Host "Cleanup failed (acceptable)" -ForegroundColor Gray
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "NEW MVP API Testing Complete" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

