document.addEventListener('DOMContentLoaded', () => {
    initializeEventListeners();
    checkPreviousLogin();
});

function initializeEventListeners() {
    // نمایش/مخفی کردن رمز عبور
    const togglePassword = document.getElementById('togglePassword');
    const password = document.getElementById('password');
    
    togglePassword.addEventListener('click', function() {
        const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
        password.setAttribute('type', type);
        
        const icon = this.querySelector('i');
        icon.classList.toggle('fa-eye');
        icon.classList.toggle('fa-eye-slash');
    });

    // فرم لاگین
    const loginForm = document.getElementById('loginForm');
    
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const password = document.getElementById('password').value;
        const submitButton = loginForm.querySelector('button[type="submit"]');
        const originalButtonText = submitButton.innerHTML;
        
        // غیرفعال کردن دکمه در حین ارسال درخواست
        submitButton.disabled = true;
        submitButton.innerHTML = '<i class="fas fa-circle-notch fa-spin"></i> در حال بررسی...';
        
        try {
            const response = await fetch('login.php', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ password })
            });
            
            const data = await response.json();
            
            if (data.success) {
                // ذخیره توکن در localStorage
                localStorage.setItem('authToken', data.token);
                localStorage.setItem('isAuthenticated', 'true');
                
                // نمایش پیام موفقیت
                Swal.fire({
                    icon: 'success',
                    title: 'ورود موفقیت‌آمیز',
                    text: 'در حال انتقال به پنل مدیریت...',
                    timer: 2000,
                    showConfirmButton: false
                }).then(() => {
                    window.location.href = 'license-manager.html';
                });
            } else {
                // نمایش پیام خطا
                Swal.fire({
                    icon: 'error',
                    title: 'خطا',
                    text: data.message || 'خطا در ورود به سیستم',
                    confirmButtonText: 'تلاش مجدد'
                });
                
                // پاک کردن فیلد رمز عبور
                document.getElementById('password').value = '';
            }
        } catch (error) {
            console.error('Error:', error);
            Swal.fire({
                icon: 'error',
                title: 'خطا',
                text: 'خطا در برقراری ارتباط با سرور',
                confirmButtonText: 'تلاش مجدد'
            });
        } finally {
            // فعال کردن مجدد دکمه
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonText;
        }
    });
}

// بررسی لاگین قبلی
function checkPreviousLogin() {
    const isAuthenticated = localStorage.getItem('isAuthenticated');
    const loginTime = localStorage.getItem('loginTime');
    
    if (isAuthenticated && loginTime) {
        const currentTime = new Date().getTime();
        const timeDiff = currentTime - parseInt(loginTime);
        const hoursDiff = timeDiff / (1000 * 60 * 60);
        
        // اگر کمتر از 24 ساعت از آخرین ورود گذشته باشد
        if (hoursDiff < 24) {
            window.location.href = 'license-manager.html';
        } else {
            // پاک کردن اطلاعات لاگین منقضی شده
            localStorage.removeItem('isAuthenticated');
            localStorage.removeItem('authToken');
            localStorage.removeItem('loginTime');
        }
    }
}

// تولید توکن تصادفی
function generateToken() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let token = '';
    for (let i = 0; i < 32; i++) {
        token += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return token;
}

// نمایش نوتیفیکیشن
function showNotification(message, type = 'success') {
    Swal.fire({
        text: message,
        icon: type,
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
        customClass: {
            popup: 'rounded-xl shadow-lg'
        }
    });
} 