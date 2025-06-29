// کش کردن داده‌ها
const cache = new Map();
const CACHE_DURATION = 5 * 60 * 1000; // 5 دقیقه

async function fetchWithCache(url, options = {}) {
    const cacheKey = `${url}-${JSON.stringify(options)}`;
    const cachedData = cache.get(cacheKey);
    
    if (cachedData && Date.now() - cachedData.timestamp < CACHE_DURATION) {
        return cachedData.data;
    }
    
    const response = await fetch(url, options);
    const data = await response.json();
    
    cache.set(cacheKey, {
        data,
        timestamp: Date.now()
    });
    
    return data;
}

// لود تنبل برای تصاویر
function initializeLazyLoading() {
    const images = document.querySelectorAll('img[data-src]');
    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.add('animate-fade-in');
                observer.unobserve(img);
            }
        });
    });
    
    images.forEach(img => imageObserver.observe(img));
}

// بهینه‌سازی اسکرول
function initializeInfiniteScroll(container, loadMoreCallback) {
    let isLoading = false;
    const observer = new IntersectionObserver((entries) => {
        const lastEntry = entries[0];
        if (lastEntry.isIntersecting && !isLoading) {
            isLoading = true;
            loadMoreCallback().finally(() => {
                isLoading = false;
            });
        }
    });
    
    observer.observe(container.lastElementChild);
}

// مدیریت عملکرد انیمیشن‌ها
function initializeAnimations() {
    const animatedElements = document.querySelectorAll('.will-animate');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-fade-in');
            }
        });
    }, { threshold: 0.1 });
    
    animatedElements.forEach(el => observer.observe(el));
}

// نمایش اسکلتون لودینگ
function showSkeletonLoading(container, count = 5) {
    const skeleton = `
        <div class="table-skeleton skeleton"></div>
    `.repeat(count);
    
    container.innerHTML = skeleton;
}

// دیباونس برای جستجو
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// بهینه‌سازی رندر جدول
class OptimizedTable {
    constructor(container, options = {}) {
        this.container = container;
        this.options = {
            pageSize: 20,
            virtualScroll: true,
            ...options
        };
        this.data = [];
        this.visibleData = [];
        this.currentPage = 1;
    }
    
    setData(data) {
        this.data = data;
        this.render();
    }
    
    render() {
        const start = (this.currentPage - 1) * this.options.pageSize;
        const end = start + this.options.pageSize;
        this.visibleData = this.data.slice(start, end);
        
        if (this.options.virtualScroll) {
            this.renderVirtual();
        } else {
            this.renderNormal();
        }
    }
    
    renderVirtual() {
        // پیاده‌سازی Virtual Scrolling
        const rowHeight = 50; // ارتفاع هر ردیف
        const totalHeight = this.data.length * rowHeight;
        const visibleHeight = this.container.clientHeight;
        const scrollTop = this.container.scrollTop;
        
        const startIndex = Math.floor(scrollTop / rowHeight);
        const endIndex = Math.min(
            startIndex + Math.ceil(visibleHeight / rowHeight),
            this.data.length
        );
        
        const rows = this.data
            .slice(startIndex, endIndex)
            .map(item => this.renderRow(item))
            .join('');
            
        this.container.innerHTML = `
            <div style="height: ${totalHeight}px; position: relative;">
                <div style="position: absolute; top: ${startIndex * rowHeight}px;">
                    ${rows}
                </div>
            </div>
        `;
    }
    
    renderRow(item) {
        // پیاده‌سازی رندر هر ردیف
        return `<div class="table-row">${JSON.stringify(item)}</div>`;
    }
}

// مدیریت وضعیت برنامه
class AppState {
    constructor() {
        this.subscribers = new Set();
        this.state = {};
    }
    
    subscribe(callback) {
        this.subscribers.add(callback);
        return () => this.subscribers.delete(callback);
    }
    
    setState(newState) {
        this.state = { ...this.state, ...newState };
        this.notify();
    }
    
    notify() {
        this.subscribers.forEach(callback => callback(this.state));
    }
}

// بهینه‌سازی فرم‌ها
function initializeFormOptimization() {
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        // جلوگیری از ارسال‌های تکراری
        let isSubmitting = false;
        
        form.addEventListener('submit', async (e) => {
            if (isSubmitting) {
                e.preventDefault();
                return;
            }
            
            isSubmitting = true;
            const submitButton = form.querySelector('[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
            }
            
            try {
                // ارسال فرم - اجازه دادن به رفتار پیش‌فرض فرم
                // فرم به صورت طبیعی ارسال می‌شود
            } finally {
                isSubmitting = false;
                if (submitButton) {
                    submitButton.disabled = false;
                }
            }
        });
    });
}

// راه‌اندازی بهینه‌سازی‌ها
document.addEventListener('DOMContentLoaded', () => {
    initializeLazyLoading();
    initializeAnimations();
    // initializeFormOptimization(); // Commented out to avoid conflicts with custom form handlers
});

// مدیریت رویدادهای resize
const debouncedResize = debounce(() => {
    // بروزرسانی المان‌های ریسپانسیو
}, 250);

window.addEventListener('resize', debouncedResize);