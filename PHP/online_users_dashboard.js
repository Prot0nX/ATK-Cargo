// متغیرهای سراسری
let autoRefreshInterval;
let isAutoRefreshActive = false;
let currentFilter = 'all';
let lastUpdatedTime = new Date();

// تنظیمات تم
function initializeTheme() {
    const savedTheme = localStorage.getItem('dashboard-theme') || 'dark';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeIcon(savedTheme);
}

// تغییر تم
function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('dashboard-theme', newTheme);
    updateThemeIcon(newTheme);
}

// به‌روزرسانی آیکون تم
function updateThemeIcon(theme) {
    const themeIcon = document.querySelector('.theme-toggle-btn i');
    if (themeIcon) {
        themeIcon.className = theme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
    }
}

// بارگذاری اولیه داده‌ها
document.addEventListener('DOMContentLoaded', function() {
    initializeTheme();
    loadData();
    startAutoRefresh();
});

async function loadData() {
    try {
        await Promise.all([
            loadStats(),
            loadOnlineUsers()
        ]);
        updateLastUpdatedTime();
    } catch (error) {
        console.error('خطا در بارگذاری داده‌ها:', error);
    }
}

async function loadStats() {
    try {
        const response = await fetch('session_management_api.php?action=getSessionStats');
        const data = await response.json();
        
        if (data.success) {
            displayStats(data.data);
        } else {
            console.error('خطا در دریافت آمار:', data.message);
        }
    } catch (error) {
        console.error('خطا در درخواست آمار:', error);
    }
}

async function loadOnlineUsers() {
    const container = document.getElementById('usersContainer');
    container.innerHTML = '<div class="loading">در حال بارگذاری...</div>';

    try {
        const response = await fetch('session_management_api.php?action=getOnlineUsers');
        const data = await response.json();
        
        if (data.success) {
            displayUsers(data.data);
        } else {
            container.innerHTML = `<div class="error">خطا: ${data.message}</div>`;
        }
    } catch (error) {
        container.innerHTML = '<div class="error">خطا در اتصال به سرور</div>';
        console.error('خطا در درخواست کاربران:', error);
    }
}

function displayStats(stats) {
    const statsGrid = document.getElementById('statsGrid');
    statsGrid.innerHTML = `
        <div class="stat-card p-6 text-center">
            <div class="stat-icon">
                <i class="fas fa-desktop"></i>
            </div>
            <div class="stat-content">
                <div class="stat-number">${stats.total_active_sessions}</div>
                <div class="stat-label">جلسات فعال</div>
            </div>
        </div>
        <div class="stat-card p-6 text-center">
            <div class="stat-icon">
                <i class="fas fa-users"></i>
            </div>
            <div class="stat-content">
                <div class="stat-number">${stats.unique_users_online}</div>
                <div class="stat-label">کاربران آنلاین</div>
            </div>
        </div>
        <div class="stat-card p-6 text-center">
            <div class="stat-icon">
                <i class="fas fa-sign-in-alt"></i>
            </div>
            <div class="stat-content">
                <div class="stat-number">${stats.today_logins}</div>
                <div class="stat-label">ورودهای امروز</div>
            </div>
        </div>
        <div class="stat-card p-6 text-center">
            <div class="stat-icon">
                <i class="fas fa-clock"></i>
            </div>
            <div class="stat-content">
                <div class="stat-number">${stats.avg_session_duration_formatted}</div>
                <div class="stat-label">میانگین مدت جلسه</div>
            </div>
        </div>
    `;
}

function displayUsers(users) {
    const container = document.getElementById('usersContainer');
    
    if (users.length === 0) {
        container.innerHTML = `
            <div class="empty-state p-20 text-center">
                <i class="fas fa-users-slash"></i>
                <h3>هیچ کاربری آنلاین نیست</h3>
                <p>در حال حاضر هیچ کاربری در سیستم فعال نمی‌باشد</p>
            </div>
        `;
        return;
    }

    const usersGrid = document.createElement('div');
    usersGrid.className = 'users-grid';
    
    users.forEach(user => {
        const userCard = document.createElement('div');
        userCard.className = `user-card ${user.userType} p-6`;
        userCard.setAttribute('data-user-type', user.userType);
        
        const userInitial = user.username.charAt(0).toUpperCase();
        
        userCard.innerHTML = `
            <div class="user-header mb-6">
                <div class="user-avatar">${userInitial}</div>
                <div class="user-info flex-1">
                    <h3>
                        <i class="fas fa-user"></i>
                        ${user.username}
                    </h3>
                    <span class="user-role role-${user.userType} px-3 py-1">
                        <i class="fas ${getRoleIcon(user.userType)}"></i>
                        ${getUserTypeLabel(user.userType)}
                    </span>
                </div>
            </div>
            
            <div class="user-details p-5 mb-6">
                <div class="detail-item">
                    <span class="detail-label">
                        <i class="fas fa-mobile-alt"></i>
                        مدل دستگاه:
                    </span>
                    <span class="detail-value">${user.device_model || 'نامشخص'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">
                        <i class="fas fa-map-marker-alt"></i>
                        آدرس IP:
                    </span>
                    <span class="detail-value">${user.ip_address}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">
                        <i class="fas fa-sign-in-alt"></i>
                        زمان ورود:
                    </span>
                    <span class="detail-value">${formatDateTime(user.login_time)}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">
                        <i class="fas fa-clock"></i>
                        آخرین فعالیت:
                    </span>
                    <span class="detail-value">${formatDateTime(user.last_activity)}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">
                        <i class="fas fa-hourglass-half"></i>
                        مدت آنلاین:
                    </span>
                    <span class="detail-value status-online">
                        <i class="fas fa-check-circle"></i>
                        ${user.online_duration_formatted}
                    </span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">
                        <i class="fas fa-pause-circle"></i>
                        مدت عدم فعالیت:
                    </span>
                    <span class="detail-value">${user.idle_time_formatted}</span>
                </div>
            </div>
            
            <div class="actions">
                <button class="btn btn-danger px-4 py-2" onclick="forceLogout('${user.username}', '${user.device_id}')">
                    <i class="fas fa-sign-out-alt"></i>
                    خروج اجباری
                </button>
                <button class="btn btn-info px-4 py-2" onclick="viewUserDetails('${user.username}')">
                    <i class="fas fa-info-circle"></i>
                    جزئیات
                </button>
            </div>
        `;
        usersGrid.appendChild(userCard);
    });
    
    container.innerHTML = '';
    container.appendChild(usersGrid);
}

function getUserTypeLabel(userType) {
    const labels = {
        'admin': 'مدیر',
        'operator': 'اپراتور',
        'verifier': 'بازرس'
    };
    return labels[userType] || userType;
}

function getRoleIcon(role) {
    switch(role) {
        case 'admin': return 'fa-user-shield';
        case 'operator': return 'fa-user-cog';
        case 'verifier': return 'fa-user-check';
        default: return 'fa-user';
    }
}

function filterUsers(type) {
    // Remove active class from all filter buttons
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Add active class to clicked button
    event.target.classList.add('active');
    
    // Filter user cards
    const userCards = document.querySelectorAll('.user-card');
    userCards.forEach(card => {
        if (type === 'all' || card.dataset.userType === type) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}

function viewUserDetails(username) {
    alert(`نمایش جزئیات کاربر: ${username}\nاین قابلیت در نسخه‌های آینده اضافه خواهد شد.`);
}

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return 'نامشخص';
    
    const date = new Date(dateTimeString);
    const options = {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    };
    
    return date.toLocaleDateString('fa-IR', options);
}

async function forceLogout(username, deviceId) {
    if (!confirm(`آیا مطمئن هستید که می‌خواهید کاربر "${username}" را از سیستم خارج کنید؟`)) {
        return;
    }

    try {
        const response = await fetch(`session_management_api.php?action=forceLogout&username=${encodeURIComponent(username)}&deviceId=${encodeURIComponent(deviceId)}`);
        const data = await response.json();
        
        if (data.success) {
            alert('کاربر با موفقیت از سیستم خارج شد');
            loadData(); // بروزرسانی داده‌ها
        } else {
            alert(`خطا: ${data.message}`);
        }
    } catch (error) {
        alert('خطا در اتصال به سرور');
        console.error('خطا در خروج اجباری:', error);
    }
}

function updateLastUpdatedTime() {
    const now = new Date();
    const timeString = now.toLocaleString('fa-IR');
    document.getElementById('lastUpdated').textContent = `آخرین بروزرسانی: ${timeString}`;
}

function startAutoRefresh() {
    // بروزرسانی خودکار هر 30 ثانیه
    autoRefreshInterval = setInterval(loadData, 30000);
}

function stopAutoRefresh() {
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
    }
}

// توقف بروزرسانی خودکار هنگام خروج از صفحه
window.addEventListener('beforeunload', stopAutoRefresh);