// =====================================================
// متغیرهای سراسری
// =====================================================

// متغیرهای اصلی سیستم
let currentKotazh = null;                    // شماره کوتاژ فعلی
let updateInterval = null;                   // شناسه تایمر به‌روزرسانی خودکار
let allCargoInfo = [];                       // تمام اطلاعات بار
let filteredCargoInfo = [];                  // اطلاعات بار فیلتر شده

// متغیرهای مدیریت بروزرسانی خودکار
let updateIntervalTime = 30000;              // مدت زمان بروزرسانی (میلی‌ثانیه)
let isAutoUpdateActive = false;              // وضعیت بروزرسانی خودکار
let progressInterval = null;                 // شناسه تایمر نوار پیشرفت
let progressStartTime = null;                // زمان شروع نوار پیشرفت
let longPressTimer = null;                   // تایمر کلیک طولانی
let isLongPress = false;                     // وضعیت کلیک طولانی

// متغیرهای فیلتر و جستجو
let currentFilter = 'all';                   // فیلتر فعلی
let currentSortColumn = null;                // ستون مرتب‌سازی فعلی
let currentSortOrder = 'asc';                // ترتیب مرتب‌سازی
let isDateFilterActive = false;              // وضعیت فیلتر تاریخ
let dateFilterStart = null;                  // تاریخ شروع فیلتر
let dateFilterEnd = null;                    // تاریخ پایان فیلتر

// المان‌های DOM
let voucherSearchInput, receiptSearchInput;
let searchModal, floatingSearchButton, closeSearchModal, advancedSearchForm;
let startDateElement, endDateElement, applyDateFilterButton;
let scrollToTopBtn = document.getElementById("scrollToTopBtn");

// =====================================================
// توابع احراز هویت و امنیت
// =====================================================

/**
 * بررسی وضعیت احراز هویت کاربر
 * @returns {boolean} - true اگر کاربر احراز هویت شده باشد
 */
function checkAuthentication() {
    const isAuthenticated = localStorage.getItem('isAuthenticated');
    const loginTime = localStorage.getItem('loginTime');
    
    if (!isAuthenticated || !loginTime) {
        window.location.href = '../login/login.html';
        return false;
    }
    
    const currentTime = new Date().getTime();
    const timeDiff = currentTime - parseInt(loginTime);
    const hoursDiff = timeDiff / (1000 * 60 * 60);
    
    // بررسی انقضای جلسه (24 ساعت)
    if (hoursDiff >= 24) {
        localStorage.removeItem('isAuthenticated');
        localStorage.removeItem('authToken');
        localStorage.removeItem('loginTime');
        localStorage.removeItem('lastDestination');
        window.location.href = '../login/login.html';
        return false;
    }
    
    return true;
}

/**
 * خروج کاربر از سیستم و پاک کردن اطلاعات احراز هویت
 */
function logout() {
    localStorage.removeItem('isAuthenticated');
    localStorage.removeItem('authToken');
    localStorage.removeItem('loginTime');
    localStorage.removeItem('lastDestination');
    window.location.href = '../login/login.html';
}

// =====================================================
// توابع راه‌اندازی اولیه
// =====================================================

/**
 * راه‌اندازی اولیه برنامه
 */
const initializeApp = () => {
    if (!checkAuthentication()) {
        return;
    }
    
    checkDarkModeOnLoad();
    initializeElements();
    setupEventListeners();
    setupSortableColumns();
    setupFilterEvents();
};

/**
 * مقداردهی اولیه المان‌های DOM
 */
const initializeElements = () => {
    startDateElement = document.getElementById('startDate');
    endDateElement = document.getElementById('endDate');
    applyDateFilterButton = document.getElementById('filterapplydatefilter');
    voucherSearchInput = document.getElementById('voucherSearch');
    receiptSearchInput = document.getElementById('receiptSearch');
    searchModal = document.getElementById('searchModal');
    floatingSearchButton = document.getElementById('floatingSearchButton');
    closeSearchModal = document.getElementById('closeSearchModal');
    advancedSearchForm = document.getElementById('advancedSearchForm');
};

/**
 * تنظیم event listener های اصلی
 */
const setupEventListeners = () => {
    setupKotazhForm();
    setupSearch();
    setupAdvancedSearch();
    setupScrollToTop();
    
    // دکمه حالت تاریک
    document.getElementById('darkModeToggle').addEventListener('click', toggleDarkMode);
    
    // دکمه تنظیم مدت زمان بروزرسانی
    setupUpdateIntervalButton();
    
    // دکمه خروج
    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', logout);
    }
    
    // مدیریت تغییر اندازه پنجره
    window.addEventListener('resize', handleResize);
    window.addEventListener('beforeunload', stopAutoUpdate);
};

/**
 * تنظیم دکمه اسکرول به بالا
 */
const setupScrollToTop = () => {
    window.onscroll = function() {
        if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {
            scrollToTopBtn.style.display = "block";
        } else {
            scrollToTopBtn.style.display = "none";
        }
    };
    
    scrollToTopBtn.addEventListener("click", function(){
        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    });
};

/**
 * مدیریت تغییر اندازه پنجره
 */
const handleResize = () => {
    if (window.myChart) {
        window.myChart.resize();
    }
    
    if (filteredCargoInfo && filteredCargoInfo.length > 0) {
        updateCargoInfo(filteredCargoInfo);
    }
};

// =====================================================
// توابع مدیریت فرم کوتاژ
// =====================================================

/**
 * تنظیم فرم ورود شماره کوتاژ
 */
const setupKotazhForm = () => {
    const kotazhForm = document.getElementById('kotazhForm');
    if (kotazhForm) {
        kotazhForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const kotazhInput = document.getElementById('kotazhNumber');
            if (kotazhInput && validateKotazh(kotazhInput.value)) {
                fetchData(kotazhInput.value);
            }
        });
    }
};

/**
 * اعتبارسنجی شماره کوتاژ
 * @param {string} kotazh - شماره کوتاژ
 * @returns {boolean} - نتیجه اعتبارسنجی
 */
const validateKotazh = (kotazh) => {
    const kotazhPattern = /^\d{8}$/;
    if (!kotazhPattern.test(kotazh)) {
        showNotification('شماره کوتاژ باید شامل 8 رقم باشد.', 'warning');
        return false;
    }
    return true;
};

// =====================================================
// توابع دریافت و به‌روزرسانی داده‌ها
// =====================================================

/**
 * دریافت اطلاعات کوتاژ از سرور
 * @param {string} kotazh - شماره کوتاژ
 */
const fetchData = (kotazh) => {
    if (!validateKotazh(kotazh)) return Promise.reject(new Error('کوتاژ نامعتبر است'));
    
    currentKotazh = kotazh;
    showLoading();
    
    return fetch(`api/api.php?kotazh=${kotazh}`)
        .then(response => {
            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error('کوتاژ مورد نظر یافت نشد');
                }
                throw new Error(`خطای سرور: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            hideLoading();
            if (data.error) {
                throw new Error(data.error);
            }
            
            updateKotazhInfo(data.kotazhInfo);
            allCargoInfo = data.cargoInfo;
            applyFilters();
            showElement('kotazhInfo');
            showElement('cargoInfoTable');
            updateExitDates(allCargoInfo);
            showNotification('اطلاعات با موفقیت دریافت شد!', 'success');
            startAutoUpdate();
            // شروع نوار پیشرفت بعد از جستجوی موفقیت‌آمیز
            showProgressBar();
            return data;
        })
        .catch(error => {
            hideLoading();
            handleFetchError(error);
            throw error;
        });
};

/**
 * به‌روزرسانی خودکار تمام داده‌ها
 */
const updateAllData = () => {
    if (!currentKotazh) return;

    showLoading();
    
    Promise.all([
        fetch(`api/api.php?kotazh=${currentKotazh}`)
    ])
    .then(responses => Promise.all(responses.map(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })))
    .then(([mainData]) => {
        hideLoading();
        
        if (mainData.error) {
            showNotification(mainData.error, 'error');
            hideElement('kotazhInfo');
            hideElement('cargoInfoTable');
        } else {
            allCargoInfo = mainData.cargoInfo;
            updateKotazhInfo(mainData.kotazhInfo);
            applyFilters();
            showElement('kotazhInfo');
            showElement('cargoInfoTable');
            updateExitDates(allCargoInfo);
            showNotification('بروزرسانی انجام شد!', 'info');
        }
    })
    .catch(error => {
        hideLoading();
        handleFetchError(error);
    });
};

/**
 * شروع به‌روزرسانی خودکار
 */
const startAutoUpdate = () => {
    if (updateInterval) {
        clearInterval(updateInterval);
    }
    
    isAutoUpdateActive = true;
    
    updateInterval = setInterval(() => {
        if (currentKotazh) {
            updateAllData();
            showProgressBar();
        }
        updateTime();
    }, updateIntervalTime);

    updateTime();
    // نوار پیشرفت فقط بعد از جستجوی موفقیت‌آمیز کوتاژ نمایش داده می‌شود
};

/**
 * توقف به‌روزرسانی خودکار
 */
const stopAutoUpdate = () => {
    if (updateInterval) {
        clearInterval(updateInterval);
        updateInterval = null;
    }
    
    isAutoUpdateActive = false;
    hideProgressBar();
};

/**
 * به‌روزرسانی زمان آخرین بروزرسانی
 */
const updateTime = () => {
    const updateTimeElement = document.getElementById('updateTime');
    if (updateTimeElement) {
        const now = new Date();
        const timeString = now.toLocaleTimeString('en-US');
        updateTimeElement.textContent = `آخرین به‌روزرسانی: ${timeString}`;
    }
};

// =====================================================
// توابع فیلترها
// =====================================================

/**
 * تنظیم event listener های فیلترها
 */
const setupFilterEvents = () => {
    document.querySelectorAll('.filter-btn:not(#filterapplydatefilter)').forEach(button => {
        button.addEventListener('click', () => {
            const filterType = button.id.replace('filter', '').toLowerCase();
            currentFilter = filterType;
            isDateFilterActive = false;
            dateFilterStart = null;
            dateFilterEnd = null;
            applyFilters();
        });
    });

    setupDateFilter();
};

/**
 * اعمال فیلترهای انتخاب شده
 */
const applyFilters = () => {
    filteredCargoInfo = allCargoInfo.filter(cargo => {
        let passMainFilter = applyMainFilter(cargo);
        let passDateFilter = true;

        if (isDateFilterActive) {
            passDateFilter = applyDateFilterToSingleCargo(cargo);
        }

        return passMainFilter && passDateFilter;
    });

    updateCargoInfo(filteredCargoInfo);
    updateFilterButtonsUI(currentFilter);
    updateDateFilterUI();

    if (currentFilter === 'all' && !isDateFilterActive) {
        startAutoUpdate();
    } else {
        stopAutoUpdate();
    }

    performSearch();
    showNotification(`فیلتر "${getFilterTypeDisplayName(currentFilter)}" اعمال شد.`);
};

/**
 * اعمال فیلتر اصلی
 * @param {Object} cargo - اطلاعات بار
 * @returns {boolean} - نتیجه فیلتر
 */
const applyMainFilter = (cargo) => {
    switch (currentFilter) {
        case 'all':
            return true;
        case 'enter':
            return cargo.status === 'ورود';
        case 'exit24h':
            return applyExit24hFilter(cargo);
        case 'exitall':
            return cargo.status === 'خروج';
        default:
            return true;
    }
};

/**
 * فیلتر خروجی 24 ساعت گذشته
 * @param {Object} cargo - اطلاعات بار
 * @returns {boolean} - نتیجه فیلتر
 */
const applyExit24hFilter = (cargo) => {
    if (cargo.status !== 'خروج') return false;

    const now = moment();
    const todayJalali = now.format('jYYYY/jMM/jDD');
    const today7_30AM = moment(`${todayJalali} 07:30`, 'jYYYY/jMM/jDD HH:mm');
    const cargoExitDateTime = moment(`${cargo.exitDate} ${cargo.exitTime}`, 'jYYYY/jMM/jDD HH:mm');

    const timeDiff = cargoExitDateTime.diff(today7_30AM, 'hours');
    
    return timeDiff >= -24 && timeDiff < 0;
};

/**
 * تنظیم فیلتر تاریخ
 */
const setupDateFilter = () => {
    startDateElement.addEventListener('change', function() {
        populateEndDateOptions(this.value);
        endDateElement.disabled = false;
        applyDateFilterButton.disabled = false;
    });

    endDateElement.addEventListener('change', function() {
        applyDateFilterButton.disabled = !(startDateElement.value && endDateElement.value);
    });

    applyDateFilterButton.addEventListener('click', applyDateFilterAction);
};

/**
 * اعمال فیلتر تاریخ
 */
const applyDateFilterAction = () => {
    const startDate = startDateElement.value;
    const endDate = endDateElement.value;

    if (!validateDate(startDate) || !validateDate(endDate)) {
        return;
    }

    dateFilterStart = `${startDate} 07:30`;
    
    if (startDate === endDate) {
        dateFilterEnd = moment(endDate, 'jYYYY/jMM/jDD').add(1, 'day').format('jYYYY/jMM/jDD') + ' 07:30';
    } else {
        dateFilterEnd = `${endDate} 07:30`;
    }
    
    isDateFilterActive = true;
    currentFilter = 'all';
    applyFilters();
};

/**
 * اعتبارسنجی تاریخ
 * @param {string} date - تاریخ
 * @returns {boolean} - نتیجه اعتبارسنجی
 */
const validateDate = (date) => {
    if (!date || date.trim() === '') {
        showNotification('لطفاً یک تاریخ انتخاب کنید.', 'warning');
        return false;
    }
    return true;
};

/**
 * اعمال فیلتر تاریخ به یک بار
 * @param {Object} cargo - اطلاعات بار
 * @returns {boolean} - نتیجه فیلتر
 */
const applyDateFilterToSingleCargo = (cargo) => {
    if (!cargo.exitDate || !cargo.exitTime) return false;

    const cargoExitDateTime = moment(`${cargo.exitDate} ${cargo.exitTime}`, 'jYYYY/jMM/jDD HH:mm');
    const filterStartDateTime = moment(dateFilterStart, 'jYYYY/jMM/jDD HH:mm');
    const filterEndDateTime = moment(dateFilterEnd, 'jYYYY/jMM/jDD HH:mm');

    return cargoExitDateTime.isBetween(filterStartDateTime, filterEndDateTime, null, '[]');
};

/**
 * به‌روزرسانی رابط کاربری فیلترها
 * @param {string} activeFilter - فیلتر فعال
 */
const updateFilterButtonsUI = (activeFilter) => {
    document.querySelectorAll('.filter-btn').forEach(btn => {
        const filterType = btn.id.replace('filter', '').toLowerCase();
        const isActive = filterType === activeFilter;
        btn.classList.toggle('bg-blue-500', isActive);
        btn.classList.toggle('text-white', isActive);
        btn.classList.toggle('bg-white', !isActive);
        btn.classList.toggle('text-gray-700', !isActive);
        btn.setAttribute('aria-pressed', isActive);
    });
};

/**
 * به‌روزرسانی رابط کاربری فیلتر تاریخ
 */
const updateDateFilterUI = () => {
    applyDateFilterButton.classList.toggle('bg-blue-500', isDateFilterActive);
    applyDateFilterButton.classList.toggle('text-white', isDateFilterActive);
    applyDateFilterButton.classList.toggle('bg-white', !isDateFilterActive);
    applyDateFilterButton.classList.toggle('text-gray-700', !isDateFilterActive);
    applyDateFilterButton.setAttribute('aria-pressed', isDateFilterActive);

    if (!isDateFilterActive) {
        startDateElement.value = '';
        endDateElement.value = '';
    }
};

/**
 * به‌روزرسانی گزینه‌های تاریخ خروج
 * @param {Array} cargoInfo - اطلاعات بارها
 */
const updateExitDates = (cargoInfo) => {
    const validDates = [...new Set(cargoInfo
        .map(cargo => cargo.exitDate)
        .filter(date => date && date.trim() !== '' && date !== '-'))]
        .sort((a, b) => moment(a, 'jYYYY/jMM/jDD').diff(moment(b, 'jYYYY/jMM/jDD')));
    
    [startDateElement, endDateElement].forEach(element => {
        element.innerHTML = '<option value="">از تاریخ</option>';
        validDates.forEach(date => {
            const option = document.createElement('option');
            option.value = date;
            option.textContent = date;
            element.appendChild(option);
        });
    });
};

/**
 * پر کردن گزینه‌های تاریخ پایان
 * @param {string} startDate - تاریخ شروع
 */
const populateEndDateOptions = (startDate) => {
    const validDates = allCargoInfo
        .map(cargo => cargo.exitDate)
        .filter(date => date && date.trim() !== '' && date >= startDate)
        .sort();

    endDateElement.innerHTML = '<option value="">تا تاریخ</option>';
    [...new Set(validDates)].forEach(date => {
        const option = document.createElement('option');
        option.value = date;
        option.textContent = date;
        endDateElement.appendChild(option);
    });
};

// =====================================================
// توابع جستجو
// =====================================================

/**
 * تنظیم جستجوی ساده
 */
const setupSearch = () => {
    voucherSearchInput.addEventListener('input', performSearch);
    receiptSearchInput.addEventListener('input', performSearch);
};

/**
 * انجام جستجو در داده‌ها
 */
const performSearch = () => {
    const voucherSearchTerm = voucherSearchInput.value.toLowerCase();
    const receiptSearchTerm = receiptSearchInput.value.toLowerCase();

    const searchedCargoInfo = filteredCargoInfo.filter(cargo => {
        const matchVoucher = cargo.trackingNumber.toLowerCase().includes(voucherSearchTerm);
        const matchReceipt = cargo.scaleReceiptNumber.toLowerCase().includes(receiptSearchTerm);
        return matchVoucher && matchReceipt;
    });

    renderVisibleRows(searchedCargoInfo);
    updateStats(searchedCargoInfo);
    updateDailyExitChart();
};

/**
 * تنظیم جستجوی پیشرفته
 */
const setupAdvancedSearch = () => {
    floatingSearchButton.addEventListener('click', openSearchModal);
    closeSearchModal.addEventListener('click', closeSearchModalFunction);
    advancedSearchForm.addEventListener('submit', performAdvancedSearch);
};

/**
 * باز کردن مودال جستجو
 */
const openSearchModal = () => {
    searchModal.classList.remove('hidden');
};

/**
 * بستن مودال جستجو
 */
const closeSearchModalFunction = () => {
    searchModal.classList.add('hidden');
};

/**
 * انجام جستجوی پیشرفته
 * @param {Event} e - رویداد فرم
 */
const performAdvancedSearch = async (e) => {
    e.preventDefault();
    const receiptNumber = document.getElementById('searchReceipt').value;
    
    if (!receiptNumber) {
        showNotification('لطفاً شماره قبض باسکول را وارد کنید!', 'warning');
        return;
    }
    
    showLoading();
    
    try {
        const response = await fetch(`api/advancedsearch.php?action=advancedSearch&receipt=${encodeURIComponent(receiptNumber)}`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const responseText = await response.text();
        let data;
        
        try {
            data = JSON.parse(responseText);
        } catch (parseError) {
            throw new Error('Invalid JSON response');
        }
        
        if (data.error) {
            showNotification(data.error, 'error');
        } else if (data.cargoInfo) {
            displaySearchResult(data.cargoInfo);
        } else {
            showNotification('هیچ نتیجه‌ای یافت نشد!', 'error');
        }
    } catch (error) {
        showNotification('خطا در جستجو. لطفاً دوباره تلاش کنید.', 'error');
    } finally {
        hideLoading();
        closeSearchModalFunction();
    }
};

/**
 * نمایش نتیجه جستجو
 * @param {Object} cargoInfo - اطلاعات بار
 */
const displaySearchResult = (cargoInfo) => {
    const resultModal = createDialog(`
        <div class="p-6 bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-4xl w-full">
            <h2 class="text-2xl font-bold mb-6 text-gray-900 dark:text-white text-center">نتیجه جستجو</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                ${createInfoSection('اطلاعات اصلی', [
                    { label: 'شماره حواله', value: cargoInfo.trackingNumber },
                    { label: 'قبض باسکول', value: cargoInfo.scaleReceiptNumber },
                    { label: 'شماره کوتاژ', value: cargoInfo.loadingQuotaNumber }
                ], 'blue', 'clipboard-list')}
                
                ${createInfoSection('اطلاعات وزن', [
                    { label: 'وزن خالص', value: formatNumber(cargoInfo.netWeight)},
                    { label: 'وزن کسری', value: formatNumber(cargoInfo.shortageWeight)},
                    { label: 'وزن اضافی', value: formatNumber(cargoInfo.excessWeight)}
                ], 'green', 'scale')}
                
                ${createInfoSection('زمان‌بندی', [
                    { label: 'ساعت ورود', value: cargoInfo.entryTime },
                    { label: 'ساعت خروج', value: cargoInfo.exitTime || '-' },
                    { label: 'تاریخ خروج', value: cargoInfo.exitDate || '-' }
                ], 'yellow', 'clock')}
                
                ${createInfoSection('اطلاعات تکمیلی', [
                    { label: 'وضعیت', value: cargoInfo.status, isStatus: true },
                    { label: 'کشتی', value: cargoInfo.shipName },
                    { label: 'انبار بارگیری', value: cargoInfo.loadingWarehouse },
                    { label: 'نوع کالا', value: cargoInfo.cargoType },
                    { label: 'شرکت بارگیری', value: cargoInfo.shippingCompany }
                ], 'purple', 'information-circle')}
            </div>
            <div class="mt-6 text-center">
                <button onclick="this.closest('.fixed').remove()" class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors">بستن</button>
            </div>
        </div>
    `);
    
    // اضافه کردن انیمیشن
    setTimeout(() => {
        resultModal.querySelectorAll('.info-section').forEach((section, index) => {
            section.style.animation = `fadeInUp 0.5s ease-out ${index * 0.1}s forwards`;
        });
    }, 100);
};

// =====================================================
// توابع کمکی رابط کاربری
// =====================================================

/**
 * ایجاد دیالوگ
 * @param {string} content - محتوای دیالوگ
 * @returns {HTMLElement} - المان دیالوگ
 */
const createDialog = (content) => {
    const dialog = document.createElement('div');
    dialog.className = 'fixed inset-0 z-50 overflow-auto bg-black bg-opacity-50 flex items-center justify-center';
    dialog.innerHTML = `
        <div class="bg-white dark:bg-gray-800 w-full max-w-md mx-auto rounded-lg shadow-lg overflow-hidden transform transition-all" dir="rtl">
            ${content}
        </div>
    `;
    document.body.appendChild(dialog);
    return dialog;
};

/**
 * ایجاد بخش اطلاعات
 * @param {string} title - عنوان بخش
 * @param {Array} items - آیتم‌های اطلاعات
 * @param {string} color - رنگ بخش
 * @param {string} icon - آیکون بخش
 * @returns {string} - HTML بخش اطلاعات
 */
const createInfoSection = (title, items, color, icon) => {
    return `
        <div class="info-section bg-${color}-50 dark:bg-${color}-900 rounded-lg p-4 shadow-md opacity-0">
            <h3 class="text-lg font-semibold mb-3 text-${color}-700 dark:text-${color}-200 flex items-center">
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="${getIconPath(icon)}"></path>
                </svg>
                ${title}
            </h3>
            ${items.map(item => `
                <div class="mb-2">
                    <span class="font-medium text-${color}-600 dark:text-${color}-300">${item.label}:</span>
                    ${item.isStatus 
                        ? `<span class="px-2 py-1 rounded ${item.value === 'خروج' ? 'bg-green-500 text-white' : 'bg-yellow-500 text-black'}">${item.value}</span>`
                        : `<span class="text-gray-800 dark:text-gray-200">${item.value || '-'}</span>`
                    }
                </div>
            `).join('')}
        </div>
    `;
};

/**
 * دریافت مسیر آیکون
 * @param {string} icon - نام آیکون
 * @returns {string} - مسیر SVG آیکون
 */
const getIconPath = (icon) => {
    const icons = {
        'clipboard-list': 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01',
        'scale': 'M3 6l3 1m0 0l-3 9a5.002 5.002 0 006.001 0M6 7l3 9M6 7l6-2m6 2l3-1m-3 1l-3 9a5.002 5.002 0 006.001 0M18 7l3 9m-3-9l-6-2m0-2v2m0 16V5m0 16H9m3 0h3',
        'clock': 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z',
        'information-circle': 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z'
    };
    return icons[icon] || '';
};

// =====================================================
// توابع به‌روزرسانی رابط کاربری
// =====================================================

/**
 * به‌روزرسانی اطلاعات کوتاژ
 * @param {Object} data - اطلاعات کوتاژ
 */
const updateKotazhInfo = (data) => {
    ['shipName', 'loadingWarehouse', 'cargoType', 'shippingCompany'].forEach(id => {
        updateElementText(id, data[id]);
    });
    
    // نمایش تناژ کل (مقدار اصلی از سرور)
    const totalCargoWeight = parseFloat(data.cargoWeight) || 0;
    updateElementText('cargoWeight', formatNumber(totalCargoWeight));

    const additionalInfo = calculateAdditionalKotazhInfo(data, allCargoInfo);
    updateElementText('remainingTonnage', formatNumber(additionalInfo.remainingTonnage));
    updateElementText('remainingVouchers', formatNumber(additionalInfo.remainingVouchers));
    
    // محاسبه تناژ بارگیری شده (تناژ کل - تناژ مانده)
    const remainingTonnage = additionalInfo.remainingTonnage || 0;
    const loadedTonnage = totalCargoWeight - remainingTonnage;
    updateElementText('loadedTonnage', formatNumber(loadedTonnage));
    
    currentKotazh = data.loadingQuotaNumber;
};

/**
 * به‌روزرسانی جدول اطلاعات بار
 * @param {Array} cargoInfo - اطلاعات بارها
 */
const updateCargoInfo = (cargoInfo) => {
    const tableBody = document.getElementById('cargoInfoBody');
    if (!tableBody) return;
    
    tableBody.innerHTML = '';
    cargoInfo.forEach(cargo => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap text-sm">${cargo.trackingNumber || ''}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${cargo.entryTime || ''}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${parseFloat(cargo.netWeight) > 0 ? formatNumber(parseFloat(cargo.netWeight)) : ''}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${cargo.scaleReceiptNumber || ''}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
                ${renderWeightDifference(parseFloat(cargo.shortageWeight), 'shortage')}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
                ${renderWeightDifference(parseFloat(cargo.excessWeight), 'excess')}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${cargo.exitTime || '-'}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${cargo.exitDate || '-'}</td>
            <td class="px-6 py-4 whitespace-nowrap">
                ${renderCargoStatus(cargo.status)}
            </td>
        `;
        tableBody.appendChild(row);
    });
};

/**
 * رندر ردیف‌های قابل مشاهده جدول
 * @param {Array} cargoInfo - اطلاعات بارها
 */
const renderVisibleRows = (cargoInfo) => {
    const tableBody = document.getElementById('cargoInfoBody');
    if (!tableBody) return;
    
    const fragment = document.createDocumentFragment();
    cargoInfo.forEach(cargo => {
        const row = document.createElement('tr');
        row.className = 'bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700';
        row.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap text-sm">${cargo.trackingNumber || ''}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${cargo.entryTime || ''}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${parseFloat(cargo.netWeight) > 0 ? formatNumber(parseFloat(cargo.netWeight)) : ''}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${cargo.scaleReceiptNumber || ''}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
                ${renderWeightDifference(parseFloat(cargo.shortageWeight), 'shortage')}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
                ${renderWeightDifference(parseFloat(cargo.excessWeight), 'excess')}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${cargo.exitTime || '-'}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${cargo.exitDate || '-'}</td>
            <td class="px-6 py-4 whitespace-nowrap">
                ${renderCargoStatus(cargo.status)}
            </td>
        `;
        fragment.appendChild(row);
    });
    
    tableBody.innerHTML = '';
    tableBody.appendChild(fragment);
};

/**
 * رندر تفاوت وزن
 * @param {number} weight - مقدار وزن
 * @param {string} type - نوع تفاوت (shortage/excess)
 * @returns {string} - HTML تفاوت وزن
 */
const renderWeightDifference = (weight, type) => {
    if (isNaN(weight) || weight <= 0) return '';
    
    const iconSvg = type === 'shortage' 
        ? '<svg class="weight-difference-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14M5 12l7-7 7 7"/></svg>'
        : '<svg class="weight-difference-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 19V5M5 12l7 7 7-7"/></svg>';
    
    const colorClass = type === 'shortage' ? 'text-red-600 dark:text-red-400' : 'text-green-600 dark:text-green-400';
    
    return `
        <span class="weight-difference ${colorClass}">
            ${formatNumber(weight)}
            ${iconSvg}
        </span>
    `;
};

/**
 * رندر وضعیت بار
 * @param {string} status - وضعیت بار
 * @returns {string} - HTML وضعیت بار
 */
const renderCargoStatus = (status) => {
    const isExit = status === 'خروج';
    const colorClass = isExit 
        ? 'bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100' 
        : 'bg-yellow-100 text-yellow-800 dark:bg-yellow-800 dark:text-yellow-100';
    
    const iconSvg = isExit
        ? '<svg class="cargo-status-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 19V5M5 12l7 7 7-7"/></svg>'
        : '<svg class="cargo-status-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14M5 12l7-7 7 7"/></svg>';
    
    return `
        <span class="cargo-status ${colorClass} px-3 py-1 inline-flex items-center text-xs leading-5 font-semibold rounded-full">
            ${iconSvg}
            ${status}
        </span>
    `;
};

// =====================================================
// توابع مرتب‌سازی جدول
// =====================================================

/**
 * تنظیم ستون‌های قابل مرتب‌سازی
 */
const setupSortableColumns = () => {
    document.querySelectorAll('th.sortable').forEach(header => {
        const ascIcon = header.querySelector('.sort-icon.sort-asc');
        const descIcon = header.querySelector('.sort-icon.sort-desc');
        
        if (ascIcon && descIcon) {
            ascIcon.addEventListener('click', () => sortTable(header.dataset.sort, 'asc'));
            descIcon.addEventListener('click', () => sortTable(header.dataset.sort, 'desc'));
        }
    });
};

/**
 * مرتب‌سازی جدول
 * @param {string} column - نام ستون
 * @param {string} order - ترتیب مرتب‌سازی
 */
const sortTable = (column, order) => {
    currentSortColumn = column;
    currentSortOrder = order;

    filteredCargoInfo.sort((a, b) => {
        let valueA = a[column];
        let valueB = b[column];

        if (column === 'exitDate') {
            valueA = jalaliToComparable(valueA);
            valueB = jalaliToComparable(valueB);
        } else if (column === 'status') {
            valueA = valueA === 'ورود' ? 0 : 1;
            valueB = valueB === 'ورود' ? 0 : 1;
        }

        return order === 'asc' ? valueA - valueB : valueB - valueA;
    });

    updateCargoInfo(filteredCargoInfo);
    updateSortIndicators();
};

/**
 * به‌روزرسانی نشانگرهای مرتب‌سازی
 */
const updateSortIndicators = () => {
    document.querySelectorAll('th.sortable').forEach(header => {
        const ascIcon = header.querySelector('.sort-icon.sort-asc');
        const descIcon = header.querySelector('.sort-icon.sort-desc');
        
        if (ascIcon && descIcon) {
            const isActive = header.dataset.sort === currentSortColumn;
            
            ascIcon.classList.toggle('active', isActive && currentSortOrder === 'asc');
            descIcon.classList.toggle('active', isActive && currentSortOrder === 'desc');
        }
    });
};

// =====================================================
// توابع محاسبه آمار
// =====================================================

/**
 * محاسبه آمار بارها
 * @param {Array} cargoInfo - اطلاعات بارها
 * @returns {Object} - آمار محاسبه شده
 */
const calculateStats = (cargoInfo) => {
    return cargoInfo.reduce((stats, cargo) => {
        const netWeight = parseFloat(cargo.netWeight) || 0;
        stats.totalVouchers++;
        stats.totalNetWeight += netWeight;

        if (cargo.exitTime && cargo.exitDate) {
            const exitDateTime = moment(`${cargo.exitDate} ${cargo.exitTime}`, 'jYYYY/jMM/jDD HH:mm');
            const exitHour = exitDateTime.hour();
            const exitMinutes = exitDateTime.minute();

            if ((exitHour > 7 || (exitHour === 7 && exitMinutes >= 30)) && 
                (exitHour < 18 || (exitHour === 18 && exitMinutes <= 30))) {
                stats.dayShiftVouchers++;
                stats.dayShiftWeight += netWeight;
            } else {
                stats.nightShiftVouchers++;
                stats.nightShiftWeight += netWeight;
            }
        }

        return stats;
    }, {
        totalVouchers: 0,
        totalNetWeight: 0,
        dayShiftVouchers: 0,
        dayShiftWeight: 0,
        nightShiftVouchers: 0,
        nightShiftWeight: 0
    });
};

/**
 * انیمیشن تغییر مقدار
 * @param {HTMLElement} element - المان هدف
 * @param {number} start - مقدار شروع
 * @param {number} end - مقدار پایان
 * @param {number} duration - مدت زمان انیمیشن
 */
const animateValue = (element, start, end, duration) => {
    // بررسی وجود المان قبل از انیمیشن
    if (!element) {
        console.warn('المان برای انیمیشن یافت نشد');
        return;
    }
    
    let startTimestamp = null;
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        element.textContent = Math.floor(progress * (end - start) + start).toLocaleString();
        if (progress < 1) {
            window.requestAnimationFrame(step);
        }
    };
    window.requestAnimationFrame(step);
};

/**
 * به‌روزرسانی آمار نمایشی
 * @param {Array} cargoInfo - اطلاعات بارها
 */
const updateStats = (cargoInfo) => {
    const stats = calculateStats(cargoInfo);
    
    // لیست المان‌های آماری برای به‌روزرسانی
    const statElements = [
        { id: 'totalNetWeight', value: stats.totalNetWeight },
        { id: 'dayShiftVouchers', value: stats.dayShiftVouchers },
        { id: 'dayShiftTonnage', value: stats.dayShiftWeight / 1000 },
        { id: 'nightShiftVouchers', value: stats.nightShiftVouchers },
        { id: 'nightShiftTonnage', value: stats.nightShiftWeight / 1000 },
        { id: 'totalVouchersStats', value: stats.dayShiftVouchers + stats.nightShiftVouchers }
    ];
    
    // به‌روزرسانی هر المان با بررسی وجود
    statElements.forEach(({ id, value }) => {
        const element = document.getElementById(id);
        if (element) {
            animateValue(element, 0, value, 1000);
        } else {
            console.warn(`المان با شناسه '${id}' یافت نشد`);
        }
    });
};

/**
 * محاسبه اطلاعات تکمیلی کوتاژ
 * @param {Object} kotazhInfo - اطلاعات کوتاژ
 * @param {Array} cargoInfo - اطلاعات بارها
 * @returns {Object} - اطلاعات محاسبه شده
 */
const calculateAdditionalKotazhInfo = (kotazhInfo, cargoInfo) => {
    let totalExitWeight = 0;
    let exitCount = 0;

    cargoInfo.forEach(cargo => {
        if (cargo.status === 'خروج' && cargo.netWeight) {
            totalExitWeight += parseFloat(cargo.netWeight);
            exitCount++;
        }
    });

    const remainingTonnage = parseFloat(kotazhInfo.cargoWeight) - totalExitWeight;
    const averageExitWeight = exitCount > 0 ? totalExitWeight / exitCount : 0;
    const remainingVouchers = averageExitWeight > 0 ? Math.ceil(remainingTonnage / averageExitWeight) : 0;

    return { remainingTonnage, remainingVouchers, totalExitWeight };
};

// =====================================================
// توابع نمودار
// =====================================================

/**
 * به‌روزرسانی نمودار خروج روزانه
 */
const updateDailyExitChart = () => {
    const dailyExits = {};
    
    const tableRows = document.querySelectorAll('#cargoInfoBody tr');
    
    tableRows.forEach(row => {
        const cells = row.cells;
        const exitDate = cells[7].textContent.trim();
        const exitTime = cells[6].textContent.trim();
        const netWeight = parseFloat(cells[2].textContent.replace(/,/g, '')) || 0;
        const status = cells[8].querySelector('span').textContent.trim();

        if (status === 'خروج' && exitDate !== '-') {
            const adjustedDate = adjustExitDate(exitDate, exitTime);
            if (adjustedDate) {
                const dateKey = adjustedDate.format('YYYY-MM-DD');
                if (!dailyExits[dateKey]) {
                    dailyExits[dateKey] = { count: 0, weight: 0 };
                }
                dailyExits[dateKey].count++;
                dailyExits[dateKey].weight += netWeight;
            }
        }
    });

    const chartData = Object.keys(dailyExits).map(date => ({
        date: date,
        count: dailyExits[date].count,
        weight: dailyExits[date].weight / 1000 // تبدیل به تن
    })).sort((a, b) => moment(a.date).diff(moment(b.date)));

    createExitChart(chartData);
};

/**
 * تنظیم تاریخ خروج
 * @param {string} exitDate - تاریخ خروج
 * @param {string} exitTime - زمان خروج
 * @returns {moment|null} - تاریخ تنظیم شده
 */
const adjustExitDate = (exitDate, exitTime) => {
    if (!exitDate || !exitTime) return null;

    const exitDateTime = moment(`${exitDate} ${exitTime}`, 'jYYYY/jMM/jDD HH:mm');
    
    if (exitDateTime.hour() < 7 || (exitDateTime.hour() === 7 && exitDateTime.minute() < 30)) {
        exitDateTime.subtract(1, 'day');
    }
    
    return exitDateTime;
};

/**
 * بررسی حالت تاریک
 * @returns {boolean} - true اگر حالت تاریک فعال باشد
 */
const isDarkMode = () => {
    return document.documentElement.classList.contains('dark');
};

/**
 * ایجاد نمودار خروج
 * @param {Array} chartData - داده‌های نمودار
 */
const createExitChart = (chartData) => {
    const ctx = document.getElementById('exitChartCanvas');
    if (!ctx) return;

    // حذف نمودار قبلی
    if (window.myChart instanceof Chart) {
        window.myChart.destroy();
    }

    // آماده‌سازی داده‌ها
    const dates = chartData.map(item => moment(item.date, 'YYYY-MM-DD').format('jYYYY/jMM/jDD'));
    const counts = chartData.map(item => item.count);
    const weights = chartData.map(item => item.weight);

    // ایجاد نمودار
    window.myChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: dates,
            datasets: [{
                label: 'تعداد حواله',
                data: counts,
                backgroundColor: 'rgba(54, 162, 235, 0.5)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1,
                yAxisID: 'y-axis-1'
            }, {
                label: 'تناژ خالص (تن)',
                data: weights,
                type: 'line',
                fill: false,
                borderColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 2,
                yAxisID: 'y-axis-2'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            rtl: true,
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        font: { family: 'Vazirmatn' },
                        color: isDarkMode() ? '#ffffff' : '#666666'
                    }
                },
                title: {
                    display: true,
                    text: 'نمودار خروج روزانه',
                    font: {
                        size: 18,
                        family: 'Vazirmatn',
                        weight: 'bold'
                    },
                    color: isDarkMode() ? '#ffffff' : '#333333'
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    backgroundColor: isDarkMode() ? 'rgba(50, 50, 50, 0.8)' : 'rgba(255, 255, 255, 0.8)',
                    titleColor: isDarkMode() ? '#ffffff' : '#333333',
                    bodyColor: isDarkMode() ? '#e0e0e0' : '#666666',
                    borderColor: isDarkMode() ? '#555555' : '#cccccc',
                    borderWidth: 1,
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) {
                                label += ': ';
                            }
                            if (context.parsed.y !== null) {
                                label += context.parsed.y.toFixed(2);
                            }
                            return label;
                        }
                    }
                }
            },
            scales: {
                x: {
                    ticks: {
                        font: { family: 'Vazirmatn' },
                        color: isDarkMode() ? '#e0e0e0' : '#666666'
                    },
                    grid: {
                        color: isDarkMode() ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)'
                    }
                },
                'y-axis-1': {
                    type: 'linear',
                    display: true,
                    position: 'left',
                    title: {
                        display: true,
                        text: 'تعداد حواله',
                        font: { family: 'Vazirmatn' },
                        color: isDarkMode() ? '#e0e0e0' : '#666666'
                    },
                    ticks: {
                        font: { family: 'Vazirmatn' },
                        color: isDarkMode() ? '#e0e0e0' : '#666666'
                    },
                    grid: {
                        color: isDarkMode() ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)'
                    }
                },
                'y-axis-2': {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    title: {
                        display: true,
                        text: 'تناژ خالص (تن)',
                        font: { family: 'Vazirmatn' },
                        color: isDarkMode() ? '#e0e0e0' : '#666666'
                    },
                    ticks: {
                        font: { family: 'Vazirmatn' },
                        color: isDarkMode() ? '#e0e0e0' : '#666666'
                    },
                    grid: {
                        color: isDarkMode() ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)'
                    }
                }
            }
        }
    });
};

/**
  * به‌روزرسانی تم نمودار
  */
 const updateChartTheme = () => {
     if (!window.myChart) return;
 
     const textColor = isDarkMode() ? '#ffffff' : '#666666';
     const gridColor = isDarkMode() ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)';
 
     window.myChart.options.plugins.legend.labels.color = textColor;
     window.myChart.options.plugins.title.color = textColor;
     window.myChart.options.scales.x.ticks.color = textColor;
     window.myChart.options.scales['y-axis-1'].ticks.color = textColor;
     window.myChart.options.scales['y-axis-2'].ticks.color = textColor;
     window.myChart.options.scales.x.grid.color = gridColor;
     window.myChart.options.scales['y-axis-1'].grid.color = gridColor;
     window.myChart.options.scales['y-axis-2'].grid.color = gridColor;
 
     window.myChart.update();
 };
 
 // =====================================================
 // توابع کمکی عمومی
 // =====================================================
 
 /**
  * به‌روزرسانی متن المان
  * @param {string} id - شناسه المان
  * @param {string} text - متن جدید
  */
 const updateElementText = (id, text) => {
     const element = document.getElementById(id);
     if (element) element.textContent = text;
 };
 
 /**
  * مخفی کردن المان
  * @param {string} id - شناسه المان
  */
 const hideElement = (id) => {
     const element = document.getElementById(id);
     if (element) element.classList.add('hidden');
 };
 
 /**
  * نمایش المان
  * @param {string} id - شناسه المان
  */
 const showElement = (id) => {
     const element = document.getElementById(id);
     if (element) element.classList.remove('hidden');
 };
 
 /**
  * فرمت کردن اعداد
  * @param {number} number - عدد برای فرمت
  * @returns {string} - عدد فرمت شده
  */
 const formatNumber = (number) => {
     return new Intl.NumberFormat('en-US').format(number);
 };
 
 /**
  * تبدیل تاریخ جلالی به قابل مقایسه
  * @param {string} jalaliDate - تاریخ جلالی
  * @returns {number} - عدد قابل مقایسه
  */
 const jalaliToComparable = (jalaliDate) => {
     if (!jalaliDate) return 0;
     const [year, month, day] = jalaliDate.split('/').map(Number);
     return (year * 10000) + (month * 100) + day;
 };
 
 /**
  * دریافت نام نمایشی فیلتر
  * @param {string} filterType - نوع فیلتر
  * @returns {string} - نام نمایشی
  */
 const getFilterTypeDisplayName = (filterType) => {
     const filterNames = {
         'all': 'همه',
         'enter': 'ورود',
         'exit24h': 'خروجی 24 ساعت',
         'exitall': 'خروجی کلی'
     };
     return filterNames[filterType] || filterType;
 };
 
 // =====================================================
 // توابع مدیریت اعلان‌ها
 // =====================================================
 
 /**
  * نمایش اعلان
  * @param {string} message - پیام اعلان
  * @param {string} notificationType - نوع اعلان
  */
 const showNotification = (message, notificationType = 'success') => {
     const validTypes = ['success', 'error', 'warning', 'info'];
     if (!validTypes.includes(notificationType)) {
         console.warn(`نوع اعلان نامعتبر: ${notificationType}. از 'success' استفاده می‌شود.`);
         notificationType = 'success';
     }
 
     const notification = document.getElementById('notification');
     const notificationText = notification.querySelector('p');
     const notificationIcon = notification.querySelector('svg');
 
     let bgColor, textColor, iconPath;
 
     switch (notificationType) {
         case 'error':
             bgColor = 'bg-red-500';
             textColor = 'text-white';
             iconPath = '<path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M6 18L18 6M6 6l12 12" />';
             break;
         case 'warning':
             bgColor = 'bg-yellow-500';
             textColor = 'text-gray-800';
             iconPath = '<path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />';
             break;
         case 'info':
             bgColor = 'bg-blue-500';
             textColor = 'text-white';
             iconPath = '<path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />';
             break;
         default: // success
             bgColor = 'bg-green-500';
             textColor = 'text-white';
             iconPath = '<path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />';
     }
 
     notification.className = `fixed bottom-0 left-1/2 transform -translate-x-1/2 mb-4 p-4 rounded-lg shadow-lg flex items-center transition-all duration-300 ease-in-out ${bgColor} ${textColor}`;
     notificationText.textContent = message;
     notificationIcon.setAttribute('width', '24');
     notificationIcon.setAttribute('height', '24');
     notificationIcon.setAttribute('viewBox', '0 0 24 24');
     notificationIcon.setAttribute('stroke-width', '3');
     notificationIcon.setAttribute('stroke', 'currentColor');
     notificationIcon.setAttribute('fill', 'none');
     notificationIcon.innerHTML = iconPath;
 
     notification.classList.remove('translate-y-full', 'opacity-0');
     notification.setAttribute('aria-hidden', 'false');
 
     setTimeout(hideNotification, 5000);
 };
 
 /**
  * مخفی کردن اعلان
  */
 const hideNotification = () => {
     const notification = document.getElementById('notification');
     notification.classList.add('translate-y-full', 'opacity-0');
     notification.setAttribute('aria-hidden', 'true');
 };
 
 /**
  * نمایش لودینگ
  */
 const showLoading = () => {
     const loadingOverlay = document.getElementById('loadingOverlay');
     if (loadingOverlay) {
         loadingOverlay.classList.remove('hidden');
     }
 };
 
 /**
  * مخفی کردن لودینگ
  */
 const hideLoading = () => {
     const loadingOverlay = document.getElementById('loadingOverlay');
     if (loadingOverlay) {
         loadingOverlay.classList.add('hidden');
     }
 };
 
 /**
  * مدیریت خطاهای دریافت داده
  * @param {Error} error - خطای رخ داده
  */
 const handleFetchError = (error) => {
     hideElement('kotazhInfo');
     hideElement('cargoInfoTable');
 
     if (error.message === 'کوتاژ مورد نظر یافت نشد') {
         showNotification('کوتاژ مورد نظر یافت نشد', 'error');
     } else if (error.message.includes('خطای سرور')) {
         showNotification('خطا در ارتباط با سرور. لطفاً بعداً دوباره تلاش کنید.', 'error');
     } else if (error.name === 'TypeError' && error.message.includes('Failed to fetch')) {
         showNotification('خطا در ارتباط با سرور. لطفاً اتصال اینترنت خود را بررسی کنید.', 'warning');
     } else {
         showNotification('خطای ناشناخته. لطفاً صفحه را رفرش کنید.', 'error');
     }
 
     console.error('خطا در دریافت اطلاعات:', error);
 };
 
 // =====================================================
// توابع مدیریت بروزرسانی خودکار و نوار پیشرفت
// =====================================================

/**
 * تنظیم دکمه مدت زمان بروزرسانی
 */
const setupUpdateIntervalButton = () => {
    const updateButton = document.getElementById('updateIntervalButton');
    const intervalDisplay = document.getElementById('intervalDisplay');
    
    if (!updateButton || !intervalDisplay) return;
    
    // نمایش مقدار اولیه
    intervalDisplay.textContent = Math.floor(updateIntervalTime / 1000);
    
    // رویداد کلیک
    updateButton.addEventListener('click', handleUpdateIntervalClick);
    
    // رویداد شروع کلیک طولانی
    updateButton.addEventListener('mousedown', startLongPress);
    updateButton.addEventListener('touchstart', startLongPress);
    
    // رویداد پایان کلیک طولانی
    updateButton.addEventListener('mouseup', endLongPress);
    updateButton.addEventListener('mouseleave', endLongPress);
    updateButton.addEventListener('touchend', endLongPress);
};

/**
 * مدیریت کلیک روی دکمه مدت زمان بروزرسانی
 */
const handleUpdateIntervalClick = () => {
    if (isLongPress) return;
    
    // تغییر مدت زمان بروزرسانی
    const intervals = [10000, 15000, 30000, 60000, 120000, 300000]; // 10s, 15s, 30s, 1m, 2m, 5m
    const currentIndex = intervals.indexOf(updateIntervalTime);
    const nextIndex = (currentIndex + 1) % intervals.length;
    
    updateIntervalTime = intervals[nextIndex];
    
    // به‌روزرسانی نمایش
    const intervalDisplay = document.getElementById('intervalDisplay');
    intervalDisplay.textContent = Math.floor(updateIntervalTime / 1000);
    
    // راه‌اندازی مجدد بروزرسانی خودکار
    if (isAutoUpdateActive) {
        restartAutoUpdate();
    }
    
    showNotification(`مدت زمان بروزرسانی به ${Math.floor(updateIntervalTime / 1000)} ثانیه تغییر یافت`, 'info');
};

/**
 * شروع کلیک طولانی
 */
const startLongPress = (e) => {
    e.preventDefault();
    isLongPress = false;
    
    // تغییر آیکن به pause
    showPauseIcon();
    
    longPressTimer = setTimeout(() => {
        isLongPress = true;
        toggleAutoUpdate();
    }, 1000); // 1 ثانیه برای کلیک طولانی
};

/**
 * پایان کلیک طولانی
 */
const endLongPress = () => {
    if (longPressTimer) {
        clearTimeout(longPressTimer);
        longPressTimer = null;
    }
};

/**
 * تغییر وضعیت بروزرسانی خودکار
 */
const toggleAutoUpdate = () => {
    if (isAutoUpdateActive) {
        stopAutoUpdate();
        hideProgressBar();
        showPauseIcon(); // نمایش آیکن pause هنگام توقف
        showNotification('بروزرسانی خودکار متوقف شد', 'warning');
    } else {
        if (currentKotazh) {
            startAutoUpdate();
            showProgressBar();
            showClockIcon(); // نمایش آیکن ساعت هنگام شروع
            showNotification('بروزرسانی خودکار شروع شد', 'success');
        } else {
            showNotification('ابتدا یک کوتاژ وارد کنید', 'warning');
        }
    }
};

/**
 * راه‌اندازی مجدد بروزرسانی خودکار
 */
const restartAutoUpdate = () => {
    if (isAutoUpdateActive) {
        stopAutoUpdate();
        startAutoUpdate();
        showProgressBar();
    }
};

/**
 * نمایش نوار پیشرفت
 */
const showProgressBar = () => {
    const progressBar = document.getElementById('updateProgressBar');
    if (!progressBar) return;
    
    progressBar.classList.remove('hidden');
    progressBar.style.transform = 'scaleX(1)';
    
    progressStartTime = Date.now();
    
    // انیمیشن نوار پیشرفت از راست به چپ (از 100% به 0%)
    progressInterval = setInterval(() => {
        const elapsed = Date.now() - progressStartTime;
        const progress = Math.max(1 - (elapsed / updateIntervalTime), 0);
        
        progressBar.style.transform = `scaleX(${progress})`;
        
        if (progress <= 0) {
            clearInterval(progressInterval);
            progressInterval = null;
            progressBar.classList.add('hidden');
        }
    }, 50);
};

/**
 * مخفی کردن نوار پیشرفت
 */
const hideProgressBar = () => {
    const progressBar = document.getElementById('updateProgressBar');
    if (progressBar) {
        progressBar.classList.add('hidden');
        progressBar.style.transform = 'scaleX(0)';
    }
    
    if (progressInterval) {
        clearInterval(progressInterval);
        progressInterval = null;
    }
};

/**
 * نمایش آیکن pause
 */
const showPauseIcon = () => {
    const clockIcon = document.getElementById('clockIcon');
    const pauseIcon = document.getElementById('pauseIcon');
    
    if (clockIcon && pauseIcon) {
        clockIcon.classList.add('hidden');
        pauseIcon.classList.remove('hidden');
    }
};

/**
 * نمایش آیکن ساعت
 */
const showClockIcon = () => {
    const clockIcon = document.getElementById('clockIcon');
    const pauseIcon = document.getElementById('pauseIcon');
    
    if (clockIcon && pauseIcon) {
        clockIcon.classList.remove('hidden');
        pauseIcon.classList.add('hidden');
    }
};

// =====================================================
// توابع مدیریت حالت تاریک
// =====================================================
 
 /**
  * تغییر حالت تاریک
  */
 const toggleDarkMode = () => {
     document.documentElement.classList.toggle('dark');
     const isDarkMode = document.documentElement.classList.contains('dark');
     localStorage.setItem('darkMode', isDarkMode);
     updateDarkModeButtonText();
     updateChartTheme();
 };
 
 /**
  * به‌روزرسانی متن دکمه حالت تاریک
  */
 const updateDarkModeButtonText = () => {
     const darkModeToggle = document.getElementById('darkModeToggle');
     darkModeToggle.textContent = document.documentElement.classList.contains('dark') ? 'روشن' : 'تیره';
 };
 
 /**
  * بررسی حالت تاریک در بارگذاری صفحه
  */
 const checkDarkModeOnLoad = () => {
     if (localStorage.getItem('darkMode') === 'true') {
         document.documentElement.classList.add('dark');
     } else {
         document.documentElement.classList.remove('dark');
     }
     updateDarkModeButtonText();
 };
 
 // =====================================================
 // راه‌اندازی اولیه برنامه
 // =====================================================
 
// =====================================================
// توابع اکسپورت داده‌ها
// =====================================================

/**
 * اکسپورت داده‌های جدول به فرمت‌های مختلف
 * @param {string} format - فرمت خروجی (csv, excel, pdf)
 */
const exportTableData = (format) => {
    if (!filteredCargoInfo || filteredCargoInfo.length === 0) {
        showNotification('هیچ داده‌ای برای اکسپورت وجود ندارد!', 'warning');
        return;
    }

    const currentDate = new Date().toISOString().split('T')[0];
    const fileName = `cargo_data_${currentKotazh}_${currentDate}`;

    switch (format) {
        case 'csv':
            exportToCSV(fileName);
            break;
        case 'excel':
            exportToExcel(fileName);
            break;
        case 'pdf':
            exportToPDF(fileName);
            break;
        default:
            showNotification('فرمت نامعتبر!', 'error');
    }
};

/**
 * اکسپورت به فرمت CSV
 * @param {string} fileName - نام فایل
 */
const exportToCSV = (fileName) => {
    const headers = [
        'شماره حواله',
        'ساعت ورود',
        'وزن خالص',
        'قبض باسکول',
        'کسری بار',
        'اضافه بار',
        'ساعت خروج',
        'تاریخ خروج',
        'وضعیت حواله'
    ];

    const csvContent = "\uFEFF" + headers.join(',') + '\n' +
        filteredCargoInfo.map(cargo => [
            cargo.trackingNumber || '-',
            cargo.entryTime || '-',
            cargo.netWeight || '0',
            cargo.scaleReceiptNumber || '-',
            cargo.shortageWeight || '0',
            cargo.excessWeight || '0',
            cargo.exitTime || '-',
            cargo.exitDate || '-',
            cargo.status || '-'
        ].join(',')).join('\n');

    downloadFile(csvContent, `${fileName}.csv`, 'text/csv;charset=utf-8');
    showNotification('فایل CSV با موفقیت ایجاد شد!', 'success');
};

/**
 * اکسپورت به فرمت Excel
 * @param {string} fileName - نام فایل
 */
const exportToExcel = (fileName) => {
    const headers = [
        'شماره حواله',
        'ساعت ورود',
        'وزن خالص',
        'قبض باسکول',
        'کسری بار',
        'اضافه بار',
        'ساعت خروج',
        'تاریخ خروج',
        'وضعیت حواله'
    ];

    let excelContent = '<table border="1"><tr>';
    headers.forEach(header => {
        excelContent += `<th>${header}</th>`;
    });
    excelContent += '</tr>';

    filteredCargoInfo.forEach(cargo => {
        excelContent += '<tr>';
        excelContent += `<td>${cargo.trackingNumber || '-'}</td>`;
        excelContent += `<td>${cargo.entryTime || '-'}</td>`;
        excelContent += `<td>${formatNumber(cargo.netWeight) || '0'}</td>`;
        excelContent += `<td>${cargo.scaleReceiptNumber || '-'}</td>`;
        excelContent += `<td>${formatNumber(cargo.shortageWeight) || '0'}</td>`;
        excelContent += `<td>${formatNumber(cargo.excessWeight) || '0'}</td>`;
        excelContent += `<td>${cargo.exitTime || '-'}</td>`;
        excelContent += `<td>${cargo.exitDate || '-'}</td>`;
        excelContent += `<td>${cargo.status || '-'}</td>`;
        excelContent += '</tr>';
    });
    excelContent += '</table>';

    const blob = new Blob(["\uFEFF" + excelContent], {
        type: 'application/vnd.ms-excel;charset=utf-8'
    });
    
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${fileName}.xls`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    showNotification('فایل Excel با موفقیت ایجاد شد!', 'success');
};

/**
 * اکسپورت به فرمت PDF
 * @param {string} fileName - نام فایل
 */
const exportToPDF = (fileName) => {
    const htmlContent = `
        <!DOCTYPE html>
        <html dir="rtl" lang="fa">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>گزارش حواله‌های کوتاژ ${currentKotazh}</title>
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Vazirmatn:wght@300;400;500;600;700;800&display=swap');
                
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                
                body {
                    font-family: 'Vazirmatn', 'Tahoma', sans-serif;
                    direction: rtl;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: #2d3748;
                    line-height: 1.6;
                    padding: 20px;
                    min-height: 100vh;
                }
                
                .container {
                    max-width: 1200px;
                    margin: 0 auto;
                    background: white;
                    border-radius: 20px;
                    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
                    overflow: hidden;
                }
                
                .header {
                    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
                    color: white;
                    padding: 40px 30px;
                    text-align: center;
                    position: relative;
                    overflow: hidden;
                }
                
                .header::before {
                    content: '';
                    position: absolute;
                    top: -50%;
                    right: -50%;
                    width: 200%;
                    height: 200%;
                    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
                    animation: float 6s ease-in-out infinite;
                }
                
                @keyframes float {
                    0%, 100% { transform: translateY(0px) rotate(0deg); }
                    50% { transform: translateY(-20px) rotate(180deg); }
                }
                
                .header h1 {
                    font-size: 32px;
                    font-weight: 800;
                    margin-bottom: 15px;
                    text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
                    position: relative;
                    z-index: 1;
                }
                
                .header-info {
                     margin-top: 30px;
                     position: relative;
                     z-index: 1;
                 }
                 
                 .stats-grid {
                     display: grid;
                     grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
                     gap: 20px;
                     margin-bottom: 25px;
                 }
                 
                 .stats-section {
                     background: rgba(255, 255, 255, 0.15);
                     backdrop-filter: blur(15px);
                     border-radius: 20px;
                     padding: 25px;
                     border: 2px solid rgba(255, 255, 255, 0.2);
                     box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
                 }
                 
                 .stats-section h2 {
                     font-size: 18px;
                     font-weight: 700;
                     margin-bottom: 15px;
                     text-align: center;
                     color: rgba(255, 255, 255, 0.95);
                     border-bottom: 2px solid rgba(255, 255, 255, 0.3);
                     padding-bottom: 10px;
                 }
                 
                 .stats-row {
                     display: grid;
                     grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
                     gap: 15px;
                 }
                 
                 .info-card {
                     background: rgba(255, 255, 255, 0.25);
                     backdrop-filter: blur(10px);
                     border-radius: 12px;
                     padding: 15px;
                     border: 1px solid rgba(255, 255, 255, 0.4);
                     text-align: center;
                     transition: all 0.3s ease;
                 }
                 
                 .info-card:hover {
                     transform: translateY(-2px);
                     box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
                 }
                 
                 .info-card h3 {
                     font-size: 12px;
                     opacity: 0.9;
                     margin-bottom: 8px;
                     line-height: 1.3;
                 }
                 
                 .info-card p {
                     font-size: 16px;
                     font-weight: 700;
                     margin: 0;
                 }
                
                .content {
                    padding: 30px;
                }
                
                .table-container {
                    background: #f8fafc;
                    border-radius: 15px;
                    padding: 20px;
                    box-shadow: inset 0 2px 4px rgba(0,0,0,0.06);
                }
                
                table {
                    width: 100%;
                    border-collapse: separate;
                    border-spacing: 0;
                    background: white;
                    border-radius: 12px;
                    overflow: hidden;
                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
                }
                
                th {
                    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
                    color: white;
                    padding: 15px 12px;
                    font-weight: 600;
                    font-size: 13px;
                    text-align: center;
                    border-bottom: 3px solid #0ea5e9;
                }
                
                th:first-child {
                    border-top-right-radius: 12px;
                }
                
                th:last-child {
                    border-top-left-radius: 12px;
                }
                
                td {
                    padding: 12px;
                    text-align: center;
                    font-size: 12px;
                    border-bottom: 1px solid #e2e8f0;
                    transition: background-color 0.2s;
                }
                
                tr:nth-child(even) {
                    background: linear-gradient(90deg, #f1f5f9 0%, #f8fafc 100%);
                }
                
                tr:hover {
                    background: linear-gradient(90deg, #e0f2fe 0%, #f0f9ff 100%);
                }
                
                tr:last-child td:first-child {
                    border-bottom-right-radius: 12px;
                }
                
                tr:last-child td:last-child {
                    border-bottom-left-radius: 12px;
                }
                
                .status-badge {
                    padding: 6px 12px;
                    border-radius: 20px;
                    font-size: 11px;
                    font-weight: 600;
                    text-align: center;
                }
                
                .status-enter {
                     background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
                     color: white;
                 }
                 
                 .status-exit {
                     background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                     color: white;
                 }
                
                .footer {
                    background: linear-gradient(135deg, #1f2937 0%, #374151 100%);
                    color: white;
                    text-align: center;
                    padding: 25px;
                    font-size: 13px;
                }
                
                .footer-content {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    flex-wrap: wrap;
                }
                
                .logo {
                    font-weight: 700;
                    font-size: 16px;
                }
                
                @media print {
                    body {
                        background: white;
                        padding: 0;
                    }
                    
                    .container {
                        box-shadow: none;
                        border-radius: 0;
                    }
                    
                    .header::before {
                        display: none;
                    }
                }
                
                @page {
                    size: A4 landscape;
                    margin: 1cm;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>📊 گزارش حواله‌های کوتاژ ${currentKotazh}</h1>
                    <div class="header-info">
                         <div class="stats-grid">
                             <!-- بخش اطلاعات کلی -->
                             <div class="stats-section">
                                 <h2>📋 اطلاعات کلی گزارش</h2>
                                 <div class="stats-row">
                                     <div class="info-card">
                                         <h3>📅 تاریخ تولید</h3>
                                         <p>${new Date().toLocaleDateString('fa-IR')}</p>
                                     </div>
                                     <div class="info-card">
                                         <h3>📦 تعداد حواله‌ها</h3>
                                         <p>${filteredCargoInfo.length}</p>
                                     </div>
                                 </div>
                             </div>
                             
                             <!-- بخش آمار وزنی -->
                             <div class="stats-section">
                                 <h2>⚖️ آمار وزنی</h2>
                                 <div class="stats-row">
                                     <div class="info-card">
                                         <h3>📊 مجموع وزن</h3>
                                         <p>${formatNumber(filteredCargoInfo.filter(cargo => cargo.status === 'خروج').reduce((sum, cargo) => sum + (parseFloat(cargo.netWeight) || 0), 0))}</p>
                                     </div>
                                     <div class="info-card">
                                         <h3>📈 میانگین وزن</h3>
                                         <p>${(() => {
                                             const exitCargos = filteredCargoInfo.filter(cargo => cargo.status === 'خروج');
                                             return formatNumber(Math.round(exitCargos.length > 0 ? exitCargos.reduce((sum, cargo) => sum + (parseFloat(cargo.netWeight) || 0), 0) / exitCargos.length : 0));
                                         })()}</p>
                                     </div>
                                 </div>
                             </div>
                             
                             <!-- بخش تقسیم‌بندی بر اساس وزن -->
                             <div class="stats-section">
                                 <h2>📏 تقسیم‌بندی وزنی</h2>
                                 <div class="stats-row">
                                     <div class="info-card">
                                         <h3>🔺 بالای 20 تن</h3>
                                         <p>${filteredCargoInfo.filter(cargo => cargo.status === 'خروج' && (parseFloat(cargo.netWeight) || 0) > 20000).length} حواله</p>
                                     </div>
                                     <div class="info-card">
                                         <h3>🔻 زیر 20 تن</h3>
                                         <p>${filteredCargoInfo.filter(cargo => cargo.status === 'خروج' && (parseFloat(cargo.netWeight) || 0) <= 20000).length} حواله</p>
                                     </div>
                                 </div>
                             </div>

                         </div>
                     </div>
                </div>
                
                <div class="content">
                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>🏷️ شماره حواله</th>
                                    <th>🕐 ساعت ورود</th>
                                    <th>⚖️ وزن خالص</th>
                                    <th>📋 قبض باسکول</th>
                                    <th>📉 کسری بار</th>
                                    <th>📈 اضافه بار</th>
                                    <th>🕐 ساعت خروج</th>
                                    <th>📅 تاریخ خروج</th>
                                    <th>📊 وضعیت</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${filteredCargoInfo.map(cargo => `
                                    <tr>
                                        <td><strong>${cargo.trackingNumber || '-'}</strong></td>
                                        <td>${cargo.entryTime || '-'}</td>
                                        <td><strong>${formatNumber(cargo.netWeight) || '0'}</strong></td>
                                        <td>${cargo.scaleReceiptNumber || '-'}</td>
                                        <td>${formatNumber(cargo.shortageWeight) || '0'}</td>
                                        <td>${formatNumber(cargo.excessWeight) || '0'}</td>
                                        <td>${cargo.exitTime || '-'}</td>
                                        <td>${cargo.exitDate || '-'}</td>
                                        <td>
                                            <span class="status-badge ${cargo.status === 'ورود' ? 'status-enter' : 'status-exit'}">
                                                ${cargo.status || '-'}
                                            </span>
                                        </td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>
                
                <div class="footer">
                    <div class="footer-content">
                        <div class="logo">🚢 سیستم مدیریت بارگیری (شرکت امین تجار خوزرستان)</div>
                        <div>تولید شده در ${new Date().toLocaleString('fa-IR')} | کوتاژ: ${currentKotazh}</div>
                    </div>
                </div>
            </div>
        </body>
        </html>
    `;
    
    // ایجاد blob و دانلود فایل
    const blob = new Blob([htmlContent], { type: 'text/html;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${fileName}.html`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    showNotification('فایل PDF (HTML) با موفقیت ذخیره شد!', 'success');
};

/**
 * دانلود فایل
 * @param {string} content - محتوای فایل
 * @param {string} fileName - نام فایل
 * @param {string} mimeType - نوع فایل
 */
const downloadFile = (content, fileName, mimeType) => {
    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
};

// =====================================================
 // اجرای اولیه برنامه
 initializeApp();
 
 // راه‌اندازی اولیه
 document.addEventListener('DOMContentLoaded', () => {
     initializeApp();
     startAutoUpdate();
 });
 
 document.getElementById('darkModeToggle').addEventListener('click', updateChartTheme);
