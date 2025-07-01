// بررسی احراز هویت
function checkAuthentication() {
    const isAuthenticated = localStorage.getItem('isAuthenticated');
    const loginTime = localStorage.getItem('loginTime');
    
    if (!isAuthenticated || !loginTime) {
        // کاربر وارد نشده است
        window.location.href = 'login.html';
        return false;
    }
    
    const currentTime = new Date().getTime();
    const timeDiff = currentTime - parseInt(loginTime);
    const hoursDiff = timeDiff / (1000 * 60 * 60);
    
    // بررسی انقضای جلسه (24 ساعت)
    if (hoursDiff >= 24) {
        // جلسه منقضی شده است
        localStorage.removeItem('isAuthenticated');
        localStorage.removeItem('authToken');
        localStorage.removeItem('loginTime');
        localStorage.removeItem('lastDestination');
        window.location.href = 'login.html';
        return false;
    }
    
    return true;
}

// تابع خروج از سیستم
function logout() {
    // پاک کردن تمام اطلاعات احراز هویت
    localStorage.removeItem('isAuthenticated');
    localStorage.removeItem('authToken');
    localStorage.removeItem('loginTime');
    localStorage.removeItem('lastDestination');
    
    // هدایت به صفحه ورود
    window.location.href = 'login.html';
}

// متغیرهای سراسری
let currentKotazh = null;
let updateInterval = null;
let allCargoInfo = [];

let filteredCargoInfo = [];
let currentFilter = 'all';
let currentSortColumn = null;
let currentSortOrder = 'asc';
let isDateFilterActive = false;
let dateFilterStart = null;
let dateFilterEnd = null;
let voucherSearchInput, receiptSearchInput;
let searchModal, floatingSearchButton, closeSearchModal, advancedSearchForm;
let startDateElement, endDateElement, applyDateFilterButton;
var scrollToTopBtn = document.getElementById("scrollToTopBtn");
window.onscroll = function() {scrollFunction()};

// توابع اصلی
const initializeApp = () => {
    // بررسی احراز هویت قبل از هر چیز
    if (!checkAuthentication()) {
        return;
    }
    
    checkDarkModeOnLoad();
    initializeElements();
    setupEventListeners();

    setupSortableColumns();

    setupFilterEvents();

};

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

const setupEventListeners = () => {
    setupKotazhForm();
    setupSearch();
    setupAdvancedSearch();
    document.getElementById('darkModeToggle').addEventListener('click', toggleDarkMode);
    
    // دکمه خروج
    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', logout);
    }
    
    window.addEventListener('resize', handleResize);
    window.addEventListener('beforeunload', stopAutoUpdate);
};

// فرم کوتاژ
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

const validateKotazh = (kotazh) => {
    const kotazhPattern = /^\d{8}$/;
    if (!kotazhPattern.test(kotazh)) {
        showNotification('شماره کوتاژ باید شامل 8 رقم باشد.', 'warning');
        return false;
    }
    return true;
};

const fetchData = (kotazh) => {
    if (validateKotazh(kotazh)) {
        currentKotazh = kotazh;
        showLoading();
        fetch(`api.php?kotazh=${kotazh}`)
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
            })
            .catch(error => {
                hideLoading();
                handleFetchError(error);
            });
    }
};

// به‌روزرسانی داده‌ها
const updateAllData = () => {
    if (!currentKotazh) {
        return;
    }

    showLoading();
    Promise.all([
        fetch(`api.php?kotazh=${currentKotazh}`)
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
        handleError(error);
    });
};

const startAutoUpdate = () => {
    if (updateInterval) {
        clearInterval(updateInterval);
    }
    updateInterval = setInterval(() => {
        if (currentKotazh) {
            fetchData(currentKotazh).catch(error => {
                console.error('خطا در به‌روزرسانی خودکار:', error);
            });
        }

        updateTime(); // اضافه کردن به‌روزرسانی زمان
    }, 30000); // هر 30 ثانیه

    // به‌روزرسانی اولیه زمان
    updateTime();
};

const stopAutoUpdate = () => {
    if (updateInterval) {
        clearInterval(updateInterval);
        updateInterval = null;
    }
};

const updateLastUpdateTime = () => {
    const updateTimeElement = document.getElementById('lastUpdateTime');
    if (updateTimeElement) {
        const now = new Date();
        const timeString = now.toLocaleTimeString();
        updateTimeElement.textContent = `آخرین به‌روزرسانی: ${timeString}`;
    }
};

// فیلترها
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

const applyExit24hFilter = (cargo) => {
    if (cargo.status !== 'خروج') return false;

    const now = moment();
    const todayJalali = now.format('jYYYY/jMM/jDD');
    const today7_30AM = moment(`${todayJalali} 07:30`, 'jYYYY/jMM/jDD HH:mm');
    const cargoExitDateTime = moment(`${cargo.exitDate} ${cargo.exitTime}`, 'jYYYY/jMM/jDD HH:mm');

    const timeDiff = cargoExitDateTime.diff(today7_30AM, 'hours');
    
    return timeDiff >= -24 && timeDiff < 0;
};

const adjustExitDate = (exitDate, exitTime) => {
    if (!exitDate || !exitTime) return null;

    const exitDateTime = moment(`${exitDate} ${exitTime}`, 'jYYYY/jMM/jDD HH:mm');
    
    if (exitDateTime.hour() < 7 || (exitDateTime.hour() === 7 && exitDateTime.minute() < 30)) {
        exitDateTime.subtract(1, 'day');
    }
    
    return exitDateTime;
};

const applyDateFilter = (cargo) => {
    if (!cargo.exitDate || !cargo.exitTime) return false;

    const cargoExitDateTime = moment(`${cargo.exitDate} ${cargo.exitTime}`, 'jYYYY/jMM/jDD HH:mm');
    const filterStartDateTime = moment(dateFilterStart, 'jYYYY/jMM/jDD HH:mm');
    const filterEndDateTime = moment(dateFilterEnd, 'jYYYY/jMM/jDD HH:mm');

    return cargoExitDateTime.isBetween(filterStartDateTime, filterEndDateTime, null, '[]');
};

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

const applyDateFilterAction = () => {
    const startDate = startDateElement.value;
    const endDate = endDateElement.value;

    if (!validateDate(startDate) || !validateDate(endDate)) {
        return;
    }

    dateFilterStart = `${startDate} 07:30`;
    
    // اگر تاریخ شروع و پایان یکسان باشند، تاریخ پایان را یک روز جلو می‌بریم
    if (startDate === endDate) {
        dateFilterEnd = moment(endDate, 'jYYYY/jMM/jDD').add(1, 'day').format('jYYYY/jMM/jDD') + ' 07:30';
    } else {
        // در غیر این صورت، از تاریخ پایان انتخاب شده استفاده می‌کنیم
        dateFilterEnd = `${endDate} 07:30`;
    }
    
    isDateFilterActive = true;
    currentFilter = 'all';

    applyFilters();
};

const validateDate = (date) => {
    if (!date || date.trim() === '') {
        showNotification('لطفاً یک تاریخ انتخاب کنید.', 'warning');
        return false;
    }
    return true;
};

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

const updateExitDates = (cargoInfo) => {
    const validDates = [...new Set(cargoInfo
        .map(cargo => cargo.exitDate)
        .filter(date => date && date.trim() !== '' && date !== '-'))].sort((a, b) => moment(a, 'jYYYY/jMM/jDD').diff(moment(b, 'jYYYY/jMM/jDD')));
    
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

const applyDateFilterToSingleCargo = (cargo) => {
    if (!cargo.exitDate || !cargo.exitTime) return false;

    const cargoExitDateTime = moment(`${cargo.exitDate} ${cargo.exitTime}`, 'jYYYY/jMM/jDD HH:mm');
    const filterStartDateTime = moment(dateFilterStart, 'jYYYY/jMM/jDD HH:mm');
    const filterEndDateTime = moment(dateFilterEnd, 'jYYYY/jMM/jDD HH:mm');

    return cargoExitDateTime.isBetween(filterStartDateTime, filterEndDateTime, null, '[]');
};

// جستجو
const setupSearch = () => {
    voucherSearchInput.addEventListener('input', performSearch);
    receiptSearchInput.addEventListener('input', performSearch);
};

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

// جستجوی پیشرفته
const setupAdvancedSearch = () => {
    floatingSearchButton.addEventListener('click', openSearchModal);
    closeSearchModal.addEventListener('click', closeSearchModalFunction);
    advancedSearchForm.addEventListener('submit', performAdvancedSearch);
};

const openSearchModal = () => {
    searchModal.classList.remove('hidden');
};

const closeSearchModalFunction = () => {
    searchModal.classList.add('hidden');
};

const performAdvancedSearch = async (e) => {
    e.preventDefault();
    const receiptNumber = document.getElementById('searchReceipt').value;
    if (!receiptNumber) {
        showNotification('لطفاً شماره قبض باسکول را وارد کنید!', 'warning');
        return;
    }
    showLoading();
    try {
        const response = await fetch(`advancedsearch.php?action=advancedSearch&receipt=${encodeURIComponent(receiptNumber)}`);
        
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
    
    // اضافه کردن انیمیشن به المان‌ها پس از رندر
    setTimeout(() => {
        resultModal.querySelectorAll('.info-section').forEach((section, index) => {
            section.style.animation = `fadeInUp 0.5s ease-out ${index * 0.1}s forwards`;
        });
    }, 100);
};

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

const getIconPath = (icon) => {
    const icons = {
        'clipboard-list': 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01',
        'scale': 'M3 6l3 1m0 0l-3 9a5.002 5.002 0 006.001 0M6 7l3 9M6 7l6-2m6 2l3-1m-3 1l-3 9a5.002 5.002 0 006.001 0M18 7l3 9m-3-9l-6-2m0-2v2m0 16V5m0 16H9m3 0h3',
        'clock': 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z',
        'information-circle': 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z'
    };
    return icons[icon] || '';
};





// به‌روزرسانی رابط کاربری
const updateKotazhInfo = (data) => {
    ['shipName', 'loadingWarehouse', 'cargoType', 'shippingCompany'].forEach(id => {
        updateElementText(id, data[id]);
    });
    updateElementText('cargoWeight', formatNumber(parseFloat(data.cargoWeight)));

    const additionalInfo = calculateAdditionalKotazhInfo(data, allCargoInfo);
    updateElementText('remainingTonnage', formatNumber(additionalInfo.remainingTonnage));
    updateElementText('remainingVouchers', formatNumber(additionalInfo.remainingVouchers));
    
    currentKotazh = data.loadingQuotaNumber;
};



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

const renderCargoStatus = (status) => {
    const isExit = status === 'خروج';
    const colorClass = isExit ? 'bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100' : 'bg-yellow-100 text-yellow-800 dark:bg-yellow-800 dark:text-yellow-100';
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



// مرتب‌سازی اطلاعات جدول
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

// آمار
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

const animateValue = (element, start, end, duration) => {
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

const updateStats = (cargoInfo) => {
    const stats = calculateStats(cargoInfo);
    
    animateValue(document.getElementById('totalVouchers'), 0, stats.totalVouchers, 1000);
    animateValue(document.getElementById('totalNetWeight'), 0, stats.totalNetWeight, 1000);
    animateValue(document.getElementById('dayShiftVouchers'), 0, stats.dayShiftVouchers, 1000);
    animateValue(document.getElementById('dayShiftTonnage'), 0, stats.dayShiftWeight / 1000, 1000);
    animateValue(document.getElementById('nightShiftVouchers'), 0, stats.nightShiftVouchers, 1000);
    animateValue(document.getElementById('nightShiftTonnage'), 0, stats.nightShiftWeight / 1000, 1000);
};

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

// نمودار
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
        weight: dailyExits[date].weight / 1000 // Convert to tons
    })).sort((a, b) => moment(a.date).diff(moment(b.date)));

    createExitChart(chartData);
};

const isDarkMode = () => {
    return document.documentElement.classList.contains('dark');
};

const createExitChart = (chartData) => {
    const ctx = document.getElementById('exitChartCanvas');
    if (!ctx) {
        return;
    }

    // Destroy existing chart if it exists
    if (window.myChart instanceof Chart) {
        window.myChart.destroy();
    }

    // Prepare the data
    const dates = chartData.map(item => moment(item.date, 'YYYY-MM-DD').format('jYYYY/jMM/jDD'));
    const counts = chartData.map(item => item.count);
    const weights = chartData.map(item => item.weight);

    // Create the chart
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
                        font: {
                            family: 'Vazirmatn'
                        },
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
                        font: {
                            family: 'Vazirmatn'
                        },
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
                        font: {
                            family: 'Vazirmatn'
                        },
                        color: isDarkMode() ? '#e0e0e0' : '#666666'
                    },
                    ticks: {
                        font: {
                            family: 'Vazirmatn'
                        },
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
                        font: {
                            family: 'Vazirmatn'
                        },
                        color: isDarkMode() ? '#e0e0e0' : '#666666'
                    },
                    ticks: {
                        font: {
                            family: 'Vazirmatn'
                        },
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



const calculateProgress = (entry, exit) => {
    const total = entry + exit;
    if (total === 0) return 0;
    return Math.round((exit / total) * 100
	);
};

// گرفتن خروجی
const exportTableData = (format) => {
    const tableData = getTableData();
    switch (format) {
        case 'excel':
            exportToExcel(tableData);
            break;
        case 'csv':
            exportToCSV(tableData);
            break;
        case 'pdf':
            exportToPDF(tableData);
            break;
    }
};

const getTableData = () => {
    const table = document.getElementById('cargoInfoTable');
    const headers = Array.from(table.querySelectorAll('thead th'))
        .slice(0, -1) // حذف ستون عملیات
        .map(th => th.textContent.trim());
    
    const rows = Array.from(table.querySelectorAll('tbody tr')).map(row => {
        return Array.from(row.querySelectorAll('td'))
            .slice(0, -1) // حذف ستون عملیات
            .map(td => {
                // برای ستون‌های وضعیت حواله و تفاوت وزن، متن داخل span را استخراج می‌کنیم
                const span = td.querySelector('span');
                return span ? span.textContent.trim() : td.textContent.trim();
            });
    });

    return { headers, rows };
};

const exportToExcel = async (data) => {
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet('گزارش بارگیری');
    
    // تنظیم عنوان گزارش
    worksheet.mergeCells('A1:I1');
    const titleCell = worksheet.getCell('A1');
    titleCell.value = 'گزارش بارگیری ';
    titleCell.font = { name: 'Tahoma', size: 16, bold: true };
    titleCell.alignment = { vertical: 'middle', horizontal: 'center' };
    titleCell.fill = {
        type: 'pattern',
        pattern: 'solid',
        fgColor: { argb: 'FFD9D9D9' }
    };

    // اضافه کردن سرستون‌ها
    const headerRow = worksheet.addRow(data.headers);
    headerRow.eachCell((cell) => {
        cell.fill = {
            type: 'pattern',
            pattern: 'solid',
            fgColor: { argb: 'FF4472C4' }
        };
        cell.font = { name: 'Tahoma', size: 12, bold: true, color: { argb: 'FFFFFFFF' } };
        cell.alignment = { vertical: 'middle', horizontal: 'center' };
    });

    // اضافه کردن داده‌ها
    data.rows.forEach(row => {
        const dataRow = worksheet.addRow(row);
        dataRow.eachCell((cell) => {
            cell.font = { name: 'Tahoma', size: 11 };
            cell.alignment = { vertical: 'middle', horizontal: 'center' };
        });
    });

    // تنظیم عرض ستون‌ها
    worksheet.columns.forEach(column => {
        column.width = 15;
    });

    // اضافه کردن خلاصه آمار
    const summary = calculateSummary(data);
    worksheet.addRow([]);
    const summaryTitleRow = worksheet.addRow(['خلاصه آمار']);
    summaryTitleRow.font = { name: 'Tahoma', size: 14, bold: true };
    summaryTitleRow.alignment = { vertical: 'middle', horizontal: 'center' };
    worksheet.mergeCells(`A${summaryTitleRow.number}:B${summaryTitleRow.number}`);

    const summaryRows = [
        ['تعداد  حواله‌ها', summary.totalVouchers],
        ['جمع وزن خالص', summary.totalNetWeight]
    ];
    summaryRows.forEach(row => {
        const summaryRow = worksheet.addRow(row);
        summaryRow.getCell(1).font = { name: 'Tahoma', size: 12 };
        summaryRow.getCell(1).alignment = { horizontal: 'right' };
        summaryRow.getCell(2).font = { name: 'Tahoma', size: 12 };
        summaryRow.getCell(2).alignment = { horizontal: 'left' };
    });

    // اضافه کردن خطوط جدول
    worksheet.eachRow((row) => {
        row.eachCell((cell) => {
            cell.border = {
                top: { style: 'thin' },
                left: { style: 'thin' },
                bottom: { style: 'thin' },
                right: { style: 'thin' }
            };
        });
    });

    // تنظیم جهت راست به چپ برای ورک‌شیت
    worksheet.views = [{ rightToLeft: true }];

    const buffer = await workbook.xlsx.writeBuffer();
    saveAs(new Blob([buffer]), 'گزارش_بارگیری.xlsx');
};

const exportToCSV = (data) => {
    const csv = Papa.unparse({
        fields: data.headers,
        data: data.rows
    }, {
        encoding: 'UTF-8'
    });
    
    const blob = new Blob(["\ufeff" + csv], { type: 'text/csv;charset=utf-8;' });
    saveAs(blob, 'cargo_data.csv');
};

const exportToPDF = (data) => {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF({
        orientation: 'l',
        unit: 'mm',
        format: 'a4',
        putOnlyUsedFonts: true
    });

    // اضافه کردن فونت فارسی
    doc.addFileToVFS('iran-sans.ttf', iranSansFontBase64);
    doc.addFont('iran-sans.ttf', 'IranSans', 'normal');
    doc.setFont('IranSans');

    // افزودن عنوان
    doc.setFontSize(18);
    doc.setTextColor(66, 135, 245);
    doc.text('امین تجار خوزستان', doc.internal.pageSize.width / 2, 15, { align: 'center' });

    // اضافه کردن تاریخ و زمان گزارش
    const now = new Date();
    doc.setFontSize(10);
    doc.setTextColor(100);
    doc.text(`تاریخ گزارش: ${now.toLocaleDateString('en-US')}`, doc.internal.pageSize.width - 15, 25, { align: 'right' });

    // معکوس کردن ترتیب هدرها و داده‌های هر ردیف
    const reversedHeaders = [...data.headers.reverse(), 'ردیف'];
    const reversedRows = data.rows.map((row, index) => [...row.reverse(), index + 1]);

    // محاسبه عرض ستون‌ها با مقیاس‌بندی خودکار
    const columnWidths = calculateColumnWidths(reversedHeaders, reversedRows, doc);

    // محاسبه بازه زمانی داده‌ها
    const dateRange = getDateRange(reversedRows, reversedHeaders);

    // پیدا کردن ایندکس ستون‌های مورد نیاز
    const netWeightIndex = getColumnIndex(reversedHeaders, 'وزن خالص');
    const statusIndex = getColumnIndex(reversedHeaders, 'وضعیت');

    // اضافه کردن جدول اصلی
    doc.autoTable({
        head: [reversedHeaders],
        body: reversedRows,
        startY: 30,
        styles: {
            font: 'IranSans',
            fontSize: 9,
            cellPadding: 2,
            halign: 'center',
            valign: 'middle',
            lineWidth: 0.1,
        },
        headStyles: {
            fillColor: [66, 135, 245],
            textColor: 255,
            fontSize: 10,
            fontStyle: 'bold',
            halign: 'center',
            valign: 'middle',
            cellPadding: 3,
        },
        columnStyles: columnWidths,
        alternateRowStyles: {
            fillColor: [240, 240, 240]
        },
        theme: 'grid',
        tableWidth: 'auto',
        margin: { top: 30, right: 7, bottom: 20, left: 7 },
        tableLineColor: [75, 75, 75],
        tableLineWidth: 0.1,
        didParseCell: function(data) {            
            // تنظیم رنگ برای ستون وضعیت حواله
            if (data.section === 'body' && data.column.dataKey === statusIndex) {
                if (data.cell.raw === 'خروج') {
                    data.cell.styles.textColor = [0, 128, 0]; // سبز
                } else if (data.cell.raw === 'ورود') {
                    data.cell.styles.textColor = [255, 191, 0]; // زرد
                }
            }
        }
    });

    // اضافه کردن جدول خلاصه
    const summary = calculateSummary(data);
    const summaryData = [
        ['عنوان', 'محاسبات'],
        ['تعداد حواله‌ها', summary.totalVouchers],
        ['جمع خالص', summary.totalNetWeight]
    ];

    doc.autoTable({
        body: summaryData,
        startY: doc.lastAutoTable.finalY + 15,
        theme: 'striped',
        styles: {
            font: 'IranSans',
            fontSize: 12,
            halign: 'right',
            cellPadding: 3,
        },
        headStyles: {
            fillColor: [66, 135, 245],
            textColor: [255, 255, 255],
            fontStyle: 'bold',
        },
        columnStyles: {
            0: { cellWidth: 40, fontStyle: 'bold' },
            1: { cellWidth: 50, halign: 'center' }
        },
        alternateRowStyles: {
            fillColor: [230, 240, 255]
        },
    });

    // محاسبه تعداد کل صفحات
    const totalPages = doc.internal.getNumberOfPages();

    // اضافه کردن پاورقی به هر صفحه
    for (let i = 1; i <= totalPages; i++) {
        doc.setPage(i);
        const pageHeight = doc.internal.pageSize.height;
        doc.setFontSize(8);
        doc.setTextColor(100);
        
        // شماره کوتاژ
        doc.text(`شماره کوتاژ: ${currentKotazh}`, doc.internal.pageSize.width / 2, pageHeight - 10, { align: 'center' });
        
        // تاریخ داده‌ها
        doc.text(`بازه زمانی: ${dateRange}`, 15, pageHeight - 10, { align: 'left' });
        
        // شماره صفحه
        doc.text(`صفحه ${i} از ${totalPages}`, doc.internal.pageSize.width - 15, pageHeight - 10, { align: 'right' });
    }

    doc.save('گزارش_بارگیری.pdf');
};

const calculateColumnWidths = (headers, rows, doc) => {
    const pageWidth = doc.internal.pageSize.getWidth() - 14; 
    const minWidth = 22; 
    
    // محاسبه حداکثر طول محتوا در هر ستون
    const maxLengths = headers.map((header, index) => {
        const columnValues = [header, ...rows.map(row => row[index])];
        return Math.max(...columnValues.map(value => doc.getStringUnitWidth(value.toString()) * doc.internal.getFontSize()));
    });

    // محاسبه کل عرض مورد نیاز
    const totalRequiredWidth = maxLengths.reduce((sum, length) => sum + Math.max(length, minWidth), 0);

    // اگر عرض مورد نیاز بیشتر از عرض صفحه است، مقیاس‌بندی انجام دهید
    const scaleFactor = totalRequiredWidth > pageWidth ? pageWidth / totalRequiredWidth : 1;

    return maxLengths.reduce((acc, length, index) => {
        acc[index] = {
            cellWidth: Math.max(length, minWidth) * scaleFactor,
            fontStyle: index === headers.length - 2 ? 'bold' : 'normal',
        };
        return acc;
    }, {});
};

const getDateRange = (rows, headers) => {
    const exitDateIndex = getColumnIndex(headers, 'تاریخ خروج');
    if (exitDateIndex === -1) return 'نامشخص';

    const dates = rows.map(row => row[exitDateIndex]).filter(date => date && date !== '-');
    if (dates.length === 0) return 'نامشخص';
    
    const sortedDates = dates.sort();
    return `${sortedDates[0]} تا ${sortedDates[sortedDates.length - 1]}`;
};

const getColumnIndex = (headers, columnName) => {
    return headers.findIndex(header => header.includes(columnName));
};

function calculateSummary(data) {
    let totalVouchers = data.rows.length;
    const netWeightIndex = getColumnIndex(data.headers, 'وزن خالص');
    let totalNetWeight = data.rows.reduce((sum, row) => {
        let netWeight = parseInt(row[netWeightIndex].replace(/,/g, '')) || 0;
        return sum + netWeight;
    }, 0);
    
    return {
        totalVouchers: formatNumber(totalVouchers),
        totalNetWeight: formatNumber(totalNetWeight)
    };
}

const saveAs = (blob, filename) => {
    if (window.navigator.msSaveOrOpenBlob) {
        window.navigator.msSaveOrOpenBlob(blob, filename);
    } else {
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.click();
        URL.revokeObjectURL(link.href);
    }
};

// توابع کمکی
const updateElementText = (id, text) => {
    const element = document.getElementById(id);
    if (element) element.textContent = text;
};

const hideElement = (id) => {
    const element = document.getElementById(id);
    if (element) element.classList.add('hidden');
};

const showElement = (id) => {
    const element = document.getElementById(id);
    if (element) element.classList.remove('hidden');
};

const formatNumber = (number) => {
    return new Intl.NumberFormat('en-US').format(number);
};

const showNotification = (message, notificationType = 'success') => {
    // بررسی اعتبار نوع اعلان
    const validTypes = ['success', 'error', 'warning', 'info'];
    if (!validTypes.includes(notificationType)) {
        console.warn(`نوع اعلان نامعتبر: ${notificationType}. از 'success' استفاده می‌شود.`);
        notificationType = 'success';
    }

    // دریافت المان‌های اعلان
    const notification = document.getElementById('notification');
    const notificationText = notification.querySelector('p');
    const notificationIcon = notification.querySelector('svg');

    // تنظیمات پیش‌فرض
    let bgColor, textColor, iconPath;

    // تنظیمات بر اساس نوع اعلان
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

    // اعمال کلاس‌ها و آیکون
    notification.className = `fixed bottom-0 left-1/2 transform -translate-x-1/2 mb-4 p-4 rounded-lg shadow-lg flex items-center transition-all duration-300 ease-in-out ${bgColor} ${textColor}`;
    notificationText.textContent = message;
    notificationIcon.setAttribute('width', '24');
    notificationIcon.setAttribute('height', '24');
    notificationIcon.setAttribute('viewBox', '0 0 24 24');
    notificationIcon.setAttribute('stroke-width', '3');
    notificationIcon.setAttribute('stroke', 'currentColor');
    notificationIcon.setAttribute('fill', 'none');
    notificationIcon.innerHTML = iconPath;

    // نمایش اعلان
    notification.classList.remove('translate-y-full', 'opacity-0');
    notification.setAttribute('aria-hidden', 'false');

    // تنظیم تایمر برای مخفی کردن اعلان
    setTimeout(hideNotification, 5000);
};

const hideNotification = () => {
    const notification = document.getElementById('notification');
    notification.classList.add('translate-y-full', 'opacity-0');
    notification.setAttribute('aria-hidden', 'true');
};

const showLoading = () => {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.classList.remove('hidden');
    }
};

const hideLoading = () => {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.classList.add('hidden');
    }
};

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

const jalaliToComparable = (jalaliDate) => {
    if (!jalaliDate) return 0;
    const [year, month, day] = jalaliDate.split('/').map(Number);
    return (year * 10000) + (month * 100) + day;
};

const getFilterTypeDisplayName = (filterType) => {
    const filterNames = {
        'all': 'همه',
        'enter': 'ورود',
        'exit24h': 'خروجی 24 ساعت',
        'exitall': 'خروجی کلی'
    };
    return filterNames[filterType] || filterType;
};

const updateTime = () => {
    const updateTimeElement = document.getElementById('updateTime');
    if (updateTimeElement) {
        const now = new Date();
        const timeString = now.toLocaleTimeString('en-US');
        updateTimeElement.textContent = `آخرین به‌روزرسانی: ${timeString}`;
    }
};

function scrollFunction() {
	if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {
		scrollToTopBtn.style.display = "block";
	} else {
		scrollToTopBtn.style.display = "none";
	}
}

scrollToTopBtn.addEventListener("click", function(){
	window.scrollTo({
		top: 0,
		behavior: "smooth"
	});
});

// حالت تاریک
const toggleDarkMode = () => {
    document.documentElement.classList.toggle('dark');
    const isDarkMode = document.documentElement.classList.contains('dark');
    localStorage.setItem('darkMode', isDarkMode);
    updateDarkModeButtonText();
    updateChartTheme();
};

const updateDarkModeButtonText = () => {
    const darkModeToggle = document.getElementById('darkModeToggle');
    darkModeToggle.textContent = document.documentElement.classList.contains('dark') ? 'روشن' : 'تیره';
};

const checkDarkModeOnLoad = () => {
    if (localStorage.getItem('darkMode') === 'true') {
        document.documentElement.classList.add('dark');
    } else {
        document.documentElement.classList.remove('dark');
    }
    updateDarkModeButtonText();
};

// اجرای اولیه برنامه
initializeApp();

// راه‌اندازی اولیه
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();

	startAutoUpdate();
    
    const editForm = document.getElementById('editForm');
    const inputs = editForm.querySelectorAll('input');

    inputs.forEach(input => {
        input.addEventListener('input', () => validateInput(input));
    });
});



document.getElementById('darkModeToggle').addEventListener('click', updateChartTheme);

const iranSansFontBase64 = "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DVMUJ4SEOW4nKhx+Gi0ohjJfYQD//wAo/wgDHAGgACIARQAAAAcCVAFR/9z//wAo/wcDuwF9ACIARwAAAAcCVAFn/9v////r/wsBWwFOACIASgAAAAYCVOnfAAD////w/wwBWgFRACIATAAAAAYCVO7gAAD////7/wwBWwFOACIASgAAAAYCVCXgAAD////7/w0BlQFRACIATQAAAAYCVDDhAAD////4/wwBGgGgACIATgAAAAYCVPbgAAD////7/w0BGgGgACIATgAAAAYCVCfhAAD//wAo/sMDHAGgACIARQAAAAcCWAD9/9H//wAo/sYDuwF9ACIARwAAAAcCWAET/9T///+5/sUBkQFOACIASwAAAAYCWLrTAAD////J/sUBlQFRACYCWMrTAAIATQAAAAD////7/sUBkQFOACIASwAAAAYCWALTAAD////7/sUBlQFRACYCWALTAAIATQAAAAD///++/sYBPAGgACIATwAAAAYCWL/UAAAABf/4/sUBWQGgAA4AFgAeACQAKQAANzMyNjUmJic3FhYVFCEjFzcXBwYxJicnNxcHBjEmJxcXFjEHLwI3MxUYKmlWASgNUxQn/vg52BdIGDAjJYkYSBgwIyWkFStAP20GBiFuJyocfhotKIYywFwYSBgwJSMwGEgYMCUjLxUrQED7NzduAAAA//8AKP/1AxwBugAiAEUAAAAHAlUA/wAw//8AKP/1A7sBvAAiAEcAAAAHAlUBHQAy////+wAAAZECLgAiAEsAAAAHAlUADwCk////+wAAAZUCMAAiAE0AAAAHAlUAJgCmAAX/+AAAAbICMAAQABoAIAAmACsAACUXByMiJicmJyc3FxYXFhYzBgYjIzUzMjY3FwM3FwYHJzc3FwYHJwMnNzMVAawGBgU9VRAFEBVRBRMOCiohbIqSBwZtbQhI9S9JJCVIyTBJJCVI7QYGIW43Nz09EkphGhReLiIhG1NuJykPAVEwSCUkSRgwSCUkSf4YNzduAAAE//gAAAGuAi4AHAAiACkALgAANzMyNjY3NjU0Jic3FhUWFjMzFwcjIiYnFwYGIyMTNxcGByc3NjEXBgcnAyc3MxUYCE5WJwcECgFTDgUrJQcGBgcxSxIWFnZ9CCcwSSQlSMgwSSQlSNkGBiFuDhsZDRgbRQgRQ18dITc3OCwENSsB/jBIJSRJGDBI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BUwFhAWkBdYGBAYyBkwGigasBsoG3Ab+BxgHOgdQB4AHmAewB9IH8gf+CAoIFggiCC4IOghGCFIIjgiuCPAJEgk2CWAJjgm6CegKBAogCiwKOApEClAKXApoCnQKgAqMCpgKpAqwCrwKyArUCxgLJAswCzwLSAuSC9wL6AwgDCwMOAxEDFAMqg0CDQ4NVA3CDjYOQg5ODloOZg5yDn4Oig72D3YPgg+OD5YP8BA2EIAQuBDEENAQ3BDoERARQBFMEVgRZBFwEYoRoBHMEdgR5BHwEfwSCBIUEiASLBKeEsoTShNcE84T+hReFHwUpBTOFNoU5hTyFP4VVBW2FgAWEhZMFlgWZBZwFnwWuhcIF04XiBeUF6AXrBe4GAwYZBieGNIY3hjqGPYZAhkOGRoZJhkyGT4ZShlWGWIZvhoYGlQakhrOGw4bWBuiG64buhvGG9IcKByWHJ4cxhzwHSAdaB3CHfweBB5SHrAe9h8kH3of3iBIIMAhDiFqIaYh7iH2IiIiXiKOIqwiuCLEItAi3CMoI3wjvCP2JAIkDiQaJCYkMiReJJwkzCUGJVQlpCXmJjAmcCZ8JogmkCa4JvAnLCc4J0QnjifiKDgoQChMKFgoYChsKKYo3ijqKPYpAikOKRopJikyKT4pjCnIKggqFCogKiwqOCpEKlAqXCpoKqAqrCq4KsQq0CrcKugq9CsAKwwrGCskKzArPCtIK1QrXCtkK2wrdCt8K4QrjCuUK5wrziv2LB4sRixuLJYsoiyuLLosxizOLNYs5CzyLSItZi1yLX4tii2WLaItri26LcYuGC5+LtYvQi/iL+4v+jAGMBQwIjAwMEowfDDGMQgxODFgMYYxqjHgMgwyFDIcMiQydDK2MvIy+jMCMwozEjMaMyIzKjM0MzwzRDNMM1QzXDNkM2wzdDOyM9g0AjQUND40fDSaNMg1AjUWNV41mjWiNao1sjW6NcI1yjXSNdo14jXqNfI1+jYCNgo2EjYaNiI2KjYyNjo2YDZwNpg2zjboNxI3QjdUN5Q3xDfMN9Q33DfkN+w39Df8OAQ4DDgUOB44KDgyODw4RjhQOFo4ZDhuOHg4gjiMOJY4oDiqOLQ4vjjIONI43DjqOPo5CjkaORo5GjkaORo5GjkaORo5GjkaOSI5QjlOOZI56joGOiI6ODpMOlg6ZDp0OpI6yjrmOwQ7NDtCO1I7XDtqO3g7gDuIO7A72DvqO/w8CDwoPDY8SjxUPHw8pDy6PNA83DzsPW49dj36Pt4/lkA4QMZA6kFYQapB/kJcQn5CskK6QsJCzkLiQy5DRENSQ2xDmkOuQ9BD5EP2RBBEKkRGRI5EwETQRORFNkVaRW5FjEWgRcBGAEZWRmZGoEaqRrxG0EbgRxpHOkdKR15HdEd+R6hHske+R8pH1kfiR+5H+kgGSBJIVkhgSG5IokisSOpI9kkCSRBJHEkoSTRJgEmmScpJ9EoeSjxKWkpsSnZKgkqQSqBKskrmAAAAAQAAAAE4UWOx2FJfDzz1AAMD6AAAAADczT9ZAAAAANzNP4//uf4OCNEEDQAAAAcAAgAAAAAAAAD/ACYAAAAAAP8AAAE0AAACkgALAnMATQKMADUCjgBNAjYATQIoAE0CqQA3AscATQEWAFQCKgAYAnYATQIbAE0DagBNAscATQKwADUCewBNArAAMQJvAEwCYQArAlwAFwKLAEECgQALA3MAGgJ1ABcCXgAFAlkAKAIdAC8CMQBAAgoAKQI0ACoCFQAqAV4AGAI0ACsCKgA/APkAQAD1/9sCAwBBAPkASANpAEACKgA/AjgAJwIxAEACNwAqAVoAQAIDACgBSgAEAioAPQHrAA0C6gATAfQAEQHiAAgB9AApAaMAJwDyAD4A8QA9ARIAPgEPAD0A8QAOAQ8AIwDyAA8BEgAjAQv/xgEP/88A9//NAQ//7wNEACgBoP/5A68AKAGkACgCDP/8AVD/+wGG//sBT//7AYr/+wFD//sBZv/7A0QAKAOvACgBUP/rAU//8AFQ//sBiv/7AUP/+AFD//sDRAAoA68AKAGG/7kBiv/JAYb/+wGK//sBZv++AYP/+ANEACgDrwAoAYb/+wGK//sBp//4AaP/+AFm//sBgv/4A0QAKAOvACgBhv/7AYr/+wGn//gBo//4AWb/+wGC//gDRAAoA68AKAGG//sBiv/7AWb/+wK7ACgC2gAoAvX/+gK4//oCvAAoAtoAKAL1//oCuP/6ArwAKALaACgCvAAoAvX/+gK4//oCvAAoAtoAKAL1//oCuP/6Af0AHAJCABwB/QAcAkIAHAH9ABwCQgAcAWYAAAGAANwBgAAAAWYAAAGAAAABZgAAAYAAAAFmAAABgAAAAZQAAAGAAAAEqwAqAoIAKgTnACoAWf/FAyj/9wD+//8C7P/3AMP//wEOAAABHP/3BKsAKgTnACoDKP/3Auz/9wT7ACoFAgAqA2b/+gDF/88DY//6BPsAKgUCACoDZv/6A2P/+gMDABgDBwAYArb/+wK4//sDAwAYAwcAGAK2//sCuP/7ArcAJQI+ACkCKv/7Ag7/+wK3ACUCPgApAir/+wIO//sDaAAoA3cAKAHb//gBxP/6A2gAKAN3ACgB2//4AcT/+gNoACgDeAAoAdv/+AHU//cBxP/6AcT/+ALJACoCvgAqAskAKgK+ACoB2//4AcT/+gMUACgDLgAoAhT/+wHE//sBxP/1AcP/9QNnACgDtQAoAhT/+wHE//sDZwAoA6gAKAII//sBw//7BMoALATKACwE1wAsBNcALAOV//sDlf/7A47/+wOO//sClwApApcAKQLXACkBKv/7AQn/+wKXACkC1wApASr/+wEJ//sCPwAnAlsAKQJH//sCJP/7AsMAKgLaACoBUP/7AU//+wFD//sCwwAqAtoAKgG/ACkB7wAqAosAJQH2//wB0P/8Aov//QG///sBvwApAe8AKgG/ACkCJgAtAef/+wGC//gBvwApAiYALQLIACcCPwAtApT//QKL//0BvwApAe8AKgG/ACkCJgAtAdYAHwHJAB8B1gAfAckAHwHWAB8ByQAfAdYAHwHJAB8B1gAfAckAHwLqACQC4gAkAqkAKALqACQC4gAkAqkAKAGG/74Biv/TAYb/+wGK//sBZv+9AYD/+ALqACQC4gAkAqoAKAFQ//sBT//7AUP/+wLqACQC4gAkAqkAKAGG/74Biv/TAYb/+wGK//sBZv+9AYD/+ALqACQC4gAkAqkAKAGG/74Biv/TAYb/+wGK//sBZv+9AYD/+AO9ADYBrQA3AbQANwF+ADMBrQA3AZgAKAO9ADYBrQA3AbQANwF+ADMBvwApAe8AKgDG//sAHf/4AjAAMAJ1ADACMAAQAnUAEAIwADACdQAwAjAABwJ1AAcCMAAFAnUAAwRWACkEpgApBFYAKQSmACkDwwAmAjAAMAJ1ADAEdwAmAW0AJQDXABcBggBOAWYAPQIZADgCsAA4AdIAKwIFACsCMwAfAm8AGwJvABsB/AAkAcwANwFmAD0CGQA4ArAAOAKZAD0CbQAnAh8AJAJvABsCbwAbAfwAJAKUACsClAAxApQAWgKUAJwClADHApQAcwKUACgClAAqApQAMQKUAFsClAAlApQAJAKUAGYCQAAoAgMAHQI1ADUCNQBTAjUAKgI1ACkCNQAbAjUARAI1ADsCNQAjAjUANAI1AC8BzAA3AWYAPQIZADgCsAA4ApkAPQJtACcCHwAkAm8AGwJvABsB/AAkApQAnAKUAMcClABzApQAKAKUACoClAAxApQAWwKUACUClAAkApQAZgFxACYBcQA+AXEAIAFxABwBcQAaAXEAKQFxACgBcQAbAXEAJgFxACMBcQAmAXEAPgFxACABcQAcAXEAGgFxACkBcQAoAXEAGwFxACYBcQAjAXEAJgFxAD4BcQAgAXEAHAFxABoBcQApAXEAKAFxABsBcQAmAXEAIwFxACYBcQA+AXEAIAFxABwBcQAaAXEAKQFxACgBcQAbAXEAJgFxACMBxAAZAzkALQMEAC0DKAAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAADIAAABoAAAAAAAAATYAVgEfADoBHwBGAe4ANAK2ADQBigAeAYoANAE2AFYA3gAQARUARQE8AD8DUgBXARcATQHfACEB2wBmAbYADgJhADEBkAABAZAAFQAAADQBNwAdAcEAAgGKAB4BigA0AU8AHQFEAAsBDABDAQwABQGSABEBiABAAXgACADRADAA0QAIAYIAIwGCADIA6gAjAOoAMgEkADQAswAwBYQAMQV6ADEFDwAyCQMAMQdkADIGbQAyBXsAMgH/ADADkQA0AngALQMSACwDEgAsAnMANAGGAEUArwAwAUIAQgD2AFUA8wBFAjUAMwIwACMCNgBPAhQAKAI7ACICLABIAiwASAIIAD8B/AAgAgIAPAIAACACFwAuAjcAMQKeADwCKQA+AagAHAQKADABCv/XArUAUgJWACACSQAeAkUASQI7ADIC3gAyAlgAgwAA//YAAgABAAUABv/3AAb/8f/+AAkAEQAXABYAGf/vAAT/+v/6AAL/+f/8/+L//P/8AAX//P/e/+//5//l/+j/5v/i/+P/5//0AAX/8gABAAEAAgACAAEAAP//////8AABAAAD6P4MAAAJA/+5/oII0QABAAAAAAAAAAAAAAAAAAACLAAEAioB9AAFAAgCigJYAAAASwKKAlgAAAFeADIA0gAAAAAAAAAAAAAAAIAAIAOAAABJAAAACAAAAABVS1dOAMAADf7/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AGwAZQBtACAARQBiAHIAYQBoAGkAbQBpACkALgAgAEEAbABsACAAcgBpAGcAaAB0AHMAIAByAGUAcwBlAHIAdgBlAGQALgBJAFIAQQBOAFMAYQBuAHMAWAAgAE0AZQBkAGkAdQBtAFIAZQBnAHUAbABhAHIAMQAuADIAMgAwADsAVQBLAFcATgA7AEkAUgBBAE4AUwBhAG4AcwBYAC0ATQBlAGQAaQB1AG0AVgBlAHIAcwBpAG8AbgAgADEALgAyADIAMABJAFIAQQBOAFMAYQBuAHMAWAAtAE0AZQBkAGkAdQBtAEkAUgBBAE4AUwBhAG4AcwBYACAAaQBzACAAYQAgAHQAcgBhAGQAZQBtAGEAcgBrACAAbwBmACAAdwB3AHcALgBmAG8AbgB0AGkAcgBhAG4ALgBjAG8AbQAgACgATQBvAHMAbABlAG0AIABFAGIAcgBhAGgAaQBtAGkAKQAuAEYAbwBuAHQAaQByAGEAbgBNAG8AcwBsAGUAbQAgAEUAYgByAGEAaABpAG0AaQBmAG8AbgB0AGkAcgBhAG4ALgBjAG8AbQBtAG8AcwBsAGUAbQBlAGIAcgBhAGgAaQBtAGkALgBjAG8AbQBUAG8AIAB1AHMAZQAgAHQAaABpAHMAIABmAG8AbgB0ACwAIABpAHQAIABpAHMAIABuAGUAYwBlAHMAcwBhAHIAeQAgAHQAbwAgAG8AYgB0AGEAaQBuACAAdABoAGUAIABsAGkAYwBlAG4AcwBlACAAZgByAG8AbQAgAHcAdwB3AC4AZgBvAG4AdABpAHIAYQBuAC4AYwBvAG0AaAB0AHQAcABzADoALwAvAGYAbwBuAHQAaQByAGEAbgAuAGMAbwBtAC8AbABpAGMAZQBuAHMAZQBzAEkAUgBBAE4AUwBhAG4AcwBYAE0AZQBkAGkAdQBtAAAAAgAAAAAAAP8rABgAAAAAAAAAAAAAAAAAAAAAAAAAAAJaAAAAAQACAAMAJAAlACYAJwAoACkAKgArACwALQAuAC8AMAAxADIAMwA0ADUANgA3ADgAOQA6ADsAPAA9AEQARQBGAEcASABJAEoASwBMAE0ATgBPAFAAUQBSAFMAVABVAFYAVwBYAFkAWgBbAFwAXQECAQMBBAEFAQYBBwEIAQkBCgELAQwBDQEOAQ8BEAERARIBEwEUARUBFgEXARgBGQEaARsBHAEdAR4BHwEgASEBIgEjASQBJQEmAScBKAEpASoBKwEsAS0BLgEvATABMQEyATMBNAE1ATYBNwE4ATkBOgE7ATwBPQE+AT8BQAFBAUIBQwFEAUUBRgFHAUgBSQFKAUsBTAFNAU4BTwFQAVEBUgFTAVQBVQFWAVcBWAFZAVoBWwFcAV0BXgFfAWABYQFiAWMBZAFlAWYBZwFoAWkBagFrAWwBbQFuAW8BcAFxAXIBcwF0AXUBdgF3AXgBeQF6AXsBfAF9AX4BfwGAAYEBggGDAYQBhQGGAYcBiAGJAYoBiwGMAY0BjgGPAZABkQGSAZMBlAGVAZYBlwGYAZkBmgGbAZwBnQGeAZ8BoAGhAaIBowGkAaUBpgGnAagBqQGqAasBrAGtAa4BrwGwAbEBsgGzAbQBtQG2AbcBuAG5AboBuwG8Ab0BvgG/AcABwQHCAcMBxAHFAcYBxwHIAckBygHLAcwBzQHOAc8B0AHRAdIB0wHUAdUB1gHXAdgB2QHaAdsB3AHdAd4B3wHgAeEB4gHjAeQB5QHmAecB6AHpAeoB6wHsAe0B7gHvAfAB8QHyAfMB9AH1AfYB9wH4AfkB+gH7AfwB/QH+Af8CAAIBAgICAwIEAgUCBgIHAggCCQIKAgsCDAINAg4CDwIQAhECEgITAhQCFQIWAhcCGAIZAhoCGwIcAh0CHgIfAiACIQIiAiMCJAIlAiYCJwIoAikCKgIrAiwCLQIuAi8CMAIxAjICMwI0AjUCNgI3AjgCOQI6AjsCPAI9Aj4CPwJAAkECQgJDAkQCRQJGAkcCSAJJAkoCSwJMAk0CTgJPABMAFAAVABYAFwAYABkAGgAbABwCUAJRAlICUwJUAlUCVgJXAlgCWQJaAlsCXAJdAl4CXwJgAmECYgJjAmQCZQJmAmcCaAJpAmoCawJsAm0CbgJvAnACcQJyAnMCdAJ1AnYCdwJ4AnkCegJ7AnwCfQJ+An8CgAKBAoICgwKEAoUChgKHAogCiQKKAosAvAD0APUA9gKMAo0CjgKPApACkQKSApMClAKVApYClwKYApkCmgKbABEADwAdAB4AqwAEACIAhwANAAYAEgA/ApwAEABCAAsADABeAGAAPgBAAMUAtAC1ALYAtwCpAKoAvgC/AAUACgKdAp4CnwKgAqECogKjAqQAIwAJAIsAigCMAIMCpQKmAF8A6AAHAA4A7wDwALgAIACPACEAHwCVAJQAkwCnAGEApABBAJIAnACaAJkApQKnAJgACAKoAqkCqgKrAqwCrQKuAq8CsAKxArICswK0ArUCtgK3ArgCuQK6ArsCvAK9Ar4CvwLAAsECwgLDAsQCxQLGAscCyALJAsoCywLMAs0CzgLPAtAC0QLSAtMC1ALVAtYC1wd1bmkwNjIxB3VuaTA2MjcLdW5pMDYyNy4wMDEMdW5pMDYyNy5maW5hEHVuaTA2MjcuZmluYS4wMDEHdW5pMDYyMwx1bmkwNjIzLmZpbmEHdW5pMDYyNQx1bmkwNjI1LmZpbmEHdW5pMDYyMgx1bmkwNjIyLmZpbmEHdW5pMDY3MQx1bmkwNjcxLmZpbmEHdW5pMDY2RQt1bmkwNjZFLjAwMQx1bmkwNjZFLmZpbmEQdW5pMDY2RS5maW5hLjAwMRB1bmkwNjZFLmZpbmEuMDAyDHVuaTA2NkUubWVkaRB1bmkwNjZFLm1lZGkuMDAxEHVuaTA2NkUubWVkaS4wMDIQdW5pMDY2RS5tZWRpLjAwMwx1bmkwNjZFLmluaXQQdW5pMDY2RS5pbml0LjAwMQd1bmkwNjI4DHVuaTA2MjguZmluYQx1bmkwNjI4Lm1lZGkQdW5pMDYyOC5tZWRpLjAwMRB1bmkwNjI4Lm1lZGkuYWx0FHVuaTA2MjgubWVkaS5hbHQuMDAxDHVuaTA2MjguaW5pdBB1bmkwNjI4LmluaXQuYWx0B3VuaTA2N0UMdW5pMDY3RS5maW5hDHVuaTA2N0UubWVkaRB1bmkwNjdFLm1lZGkuMDAxEHVuaTA2N0UubWVkaS5hbHQUdW5pMDY3RS5tZWRpLmFsdC4wMDEMdW5pMDY3RS5pbml0EHVuaTA2N0UuaW5pdC5hbHQHdW5pMDYyQQx1bmkwNjJBLmZpbmEMdW5pMDYyQS5tZWRpEHVuaTA2MkEubWVkaS4wMDEQdW5pMDYyQS5tZWRpLjAwMhB1bmkwNjJBLm1lZGkuYWx0DHVuaTA2MkEuaW5pdBB1bmkwNjJBLmluaXQuYWx0B3VuaTA2MkIMdW5pMDYyQi5maW5hDHVuaTA2MkIubWVkaRB1bmkwNjJCLm1lZGkuMDAxEHVuaTA2MkIubWVkaS4wMDIQdW5pMDYyQi5tZWRpLmFsdAx1bmkwNjJCLmluaXQQdW5pMDYyQi5pbml0LmFsdAd1bmkwNjc5DHVuaTA2NzkuZmluYQx1bmkwNjc5Lm1lZGkQdW5pMDY3OS5tZWRpLjAwMQx1bmkwNjc5LmluaXQHdW5pMDYyQwx1bmkwNjJDLmZpbmEMdW5pMDYyQy5tZWRpDHVuaTA2MkMuaW5pdAd1bmkwNjg2DHVuaTA2ODYuZmluYQx1bmkwNjg2Lm1lZGkMdW5pMDY4Ni5pbml0B3VuaTA2MkQMdW5pMDYyRC5maW5hEHVuaTA2MkQuZmluYS4wMDEMdW5pMDYyRC5tZWRpDHVuaTA2MkQuaW5pdAd1bmkwNjJFDHVuaTA2MkUuZmluYQx1bmkwNjJFLm1lZGkMdW5pMDYyRS5pbml0B3VuaTA2MkYMdW5pMDYyRi5maW5hB3VuaTA2MzAMdW5pMDYzMC5maW5hB3VuaTA2ODgMdW5pMDY4OC5maW5hB3VuaTA2MzELdW5pMDYzMS4wMDMMdW5pMDYzMS5maW5hB3VuaTA2MzIMdW5pMDYzMi5maW5hB3VuaTA2OTEMdW5pMDY5MS5maW5hB3VuaTA2OTUMdW5pMDY5NS5maW5hB3VuaTA2OTgMdW5pMDY5OC5maW5hB3VuaTA2MzMLdW5pMDYzMy4wMDEMdW5pMDYzMy5maW5hEHVuaTA2MzMuZmluYS4wMDEMdW5pMDYzMy5tZWRpEHVuaTA2MzMubWVkaS4wMDIMdW5pMDYzMy5pbml0EHVuaTA2MzMuaW5pdC4wMDEQdW5pMDYzMy5pbml0LjAwMhB1bmkwNjMzLmluaXQuMDAzB3VuaTA2MzQMdW5pMDYzNC5maW5hDHVuaTA2MzQubWVkaQx1bmkwNjM0LmluaXQHdW5pMDYzNQx1bmkwNjM1LmZpbmEMdW5pMDYzNS5tZWRpEHVuaTA2MzUubWVkaS4wMDEMdW5pMDYzNS5pbml0B3VuaTA2MzYMdW5pMDYzNi5maW5hDHVuaTA2MzYubWVkaQx1bmkwNjM2LmluaXQHdW5pMDYzNwx1bmkwNjM3LmZpbmEMdW5pMDYzNy5tZWRpDHVuaTA2MzcuaW5pdAd1bmkwNjM4DHVuaTA2MzguZmluYQx1bmkwNjM4Lm1lZGkMdW5pMDYzOC5pbml0B3VuaTA2MzkMdW5pMDYzOS5maW5hDHVuaTA2MzkubWVkaQx1bmkwNjM5LmluaXQHdW5pMDYzQQx1bmkwNjNBLmZpbmEMdW5pMDYzQS5tZWRpDHVuaTA2M0EuaW5pdAd1bmkwNjQxDHVuaTA2NDEuZmluYQx1bmkwNjQxLm1lZGkMdW5pMDY0MS5pbml0B3VuaTA2QTQMdW5pMDZBNC5maW5hDHVuaTA2QTQubWVkaQx1bmkwNkE0LmluaXQHdW5pMDZBMQx1bmkwNkExLmZpbmEMdW5pMDZBMS5tZWRpEHVuaTA2QTEubWVkaS4wMDEMdW5pMDZBMS5pbml0EHVuaTA2QTEuaW5pdC4wMDEHdW5pMDY2Rgx1bmkwNjZGLmZpbmEHdW5pMDY0Mgx1bmkwNjQyLmZpbmEMdW5pMDY0Mi5tZWRpDHVuaTA2NDIuaW5pdAd1bmkwNjQzDHVuaTA2NDMuZmluYQx1bmkwNjQzLm1lZGkMdW5pMDY0My5pbml0EHVuaTA2NDMuaW5pdC5hbHQRdW5pMDY0My5pbml0LmFsdDMHdW5pMDZBOQx1bmkwNkE5LmZpbmEMdW5pMDZBOS5tZWRpDHVuaTA2QTkuaW5pdAd1bmkwNkFGDHVuaTA2QUYuZmluYQx1bmkwNkFGLm1lZGkMdW5pMDZBRi5pbml0B3VuaTA2QUELdW5pMDZBQS4wMDEMdW5pMDZBQS5maW5hEHVuaTA2QUEuZmluYS4wMDEMdW5pMDZBQS5tZWRpEHVuaTA2QUEubWVkaS4wMDEMdW5pMDZBQS5pbml0EHVuaTA2QUEuaW5pdC4wMDEHdW5pMDY0NAt1bmkwNjQ0LjAwMQx1bmkwNjQ0LmZpbmEMdW5pMDY0NC5tZWRpDHVuaTA2NDQuaW5pdAd1bmkwNkI1DHVuaTA2QjUuZmluYQx1bmkwNkI1Lm1lZGkMdW5pMDZCNS5pbml0B3VuaTA2NDUMdW5pMDY0NS5maW5hDHVuaTA2NDUubWVkaQx1bmkwNjQ1LmluaXQHdW5pMDY0Ngx1bmkwNjQ2LmZpbmEMdW5pMDY0Ni5tZWRpEHVuaTA2NDYubWVkaS4wMDEMdW5pMDY0Ni5pbml0B3VuaTA2QkEMdW5pMDZCQS5maW5hB3VuaTA2NDcMdW5pMDY0Ny5maW5hEHVuaTA2NDcuZmluYS5hbHQMdW5pMDY0Ny5tZWRpEHVuaTA2NDcubWVkaS5hbHQMdW5pMDY0Ny5pbml0EHVuaTA2NDcuaW5pdC5hbHQHdW5pMDZDMAx1bmkwNkMwLmZpbmEHdW5pMDZDMQx1bmkwNkMxLmZpbmEMdW5pMDZDMS5tZWRpDHVuaTA2QzEuaW5pdAd1bmkwNkMyDHVuaTA2QzIuZmluYQd1bmkwNkJFDHVuaTA2QkUuZmluYQx1bmkwNkJFLm1lZGkMdW5pMDZCRS5pbml0B3VuaTA2MjkMdW5pMDYyOS5maW5hB3VuaTA2QzMMdW5pMDZDMy5maW5hB3VuaTA2NDgMdW5pMDY0OC5maW5hB3VuaTA2MjQMdW5pMDYyNC5maW5hB3VuaTA2QzYMdW5pMDZDNi5maW5hB3VuaTA2QzcMdW5pMDZDNy5maW5hB3VuaTA2QzkMdW5pMDZDOS5maW5hB3VuaTA2NDkMdW5pMDY0OS5maW5hEHVuaTA2NDkuZmluYS4wMDEHdW5pMDY0QQx1bmkwNjRBLmZpbmEQdW5pMDY0QS5maW5hLjAwMQx1bmkwNjRBLm1lZGkQdW5pMDY0QS5tZWRpLjAwMRB1bmkwNjRBLm1lZGkuYWx0FHVuaTA2NEEubWVkaS5hbHQuMDAxDHVuaTA2NEEuaW5pdBB1bmkwNjRBLmluaXQuYWx0B3VuaTA2MjYMdW5pMDYyNi5maW5hEHVuaTA2MjYuZmluYS4wMDEMdW5pMDYyNi5tZWRpEHVuaTA2MjYubWVkaS4wMDEMdW5pMDYyNi5pbml0B3VuaTA2Q0UMdW5pMDZDRS5maW5hEHVuaTA2Q0UuZmluYS4wMDEMdW5pMDZDRS5tZWRpEHVuaTA2Q0UubWVkaS4wMDEQdW5pMDZDRS5tZWRpLmFsdBR1bmkwNkNFLm1lZGkuYWx0LjAwMQx1bmkwNkNFLmluaXQQdW5pMDZDRS5pbml0LmFsdAd1bmkwNkNDDHVuaTA2Q0MuZmluYRB1bmkwNkNDLmZpbmEuMDAxDHVuaTA2Q0MubWVkaRB1bmkwNkNDLm1lZGkuMDAxEHVuaTA2Q0MubWVkaS5hbHQUdW5pMDZDQy5tZWRpLmFsdC4wMDEMdW5pMDZDQy5pbml0EHVuaTA2Q0MuaW5pdC5hbHQHdW5pMDZEMgx1bmkwNkQyLmZpbmEQdW5pMDZEMi5maW5hLmFsdBF1bmkwNkQyLmZpbmEuYWx0MhF1bmkwNkQyLmZpbmEuYWx0MxF1bmkwNkQyLmZpbmEuYWx0NAd1bmkwNkQzDHVuaTA2RDMuZmluYRB1bmkwNkQzLmZpbmEuYWx0EXVuaTA2RDMuZmluYS5hbHQyB3VuaTA2RDUMdW5pMDZENS5maW5hB3VuaTA2NDALdW5pMDY0MC4wMDELdW5pMDY0NDA2MjcQdW5pMDY0NDA2MjcuZmluYQt1bmkwNjQ0MDYyMxB1bmkwNjQ0MDYyMy5maW5hC3VuaTA2NDQwNjI1EHVuaTA2NDQwNjI1LmZpbmELdW5pMDY0NDA2MjIQdW5pMDY0NDA2MjIuZmluYQt1bmkwNjQ0MDY3MRB1bmkwNjQ0MDY3MS5maW5hC3VuaTA2QTkwNjQ0EHVuaTA2QTkwNjQ0LmZpbmELdW5pMDZBRjA2NDQQdW5pMDZBRjA2NDQuZmluYQ91bmkwNjQ0MDY0NDA2NDcLdW5pMDZCNTA2MjcQdW5pMDZCNTA2MjcuZmluYQd1bmlGREYyB3VuaTA2NkIHdW5pMDY2Qwd1bmkwNjYwB3VuaTA2NjEHdW5pMDY2Mgd1bmkwNjYzB3VuaTA2NjQHdW5pMDY2NQd1bmkwNjY2B3VuaTA2NjcHdW5pMDY2OAd1bmkwNjY5B3VuaTA2RjAHdW5pMDZGMQd1bmkwNkYyB3VuaTA2RjMHdW5pMDZGNAd1bmkwNkY1B3VuaTA2RjYHdW5pMDZGNwd1bmkwNkY4B3VuaTA2RjkMdW5pMDY2NC5zczAzDHVuaTA2NjUuc3MwMwx1bmkwNjY2LnNzMDMMdW5pMDZGMC5zczAzDHVuaTA2RjEuc3MwMwx1bmkwNkYyLnNzMDMMdW5pMDZGMy5zczAzDHVuaTA2RjQuc3MwMwx1bmkwNkY1LnNzMDMMdW5pMDZGNi5zczAzDHVuaTA2Rjcuc3MwMwx1bmkwNkY4LnNzMDMMdW5pMDZGOS5zczAzDHVuaTA2RjQudXJkdQx1bmkwNkY3LnVyZHUJemVyby5zczAyCG9uZS5zczAyCHR3by5zczAyCnRocmVlLnNzMDIJZm91ci5zczAyCWZpdmUuc3MwMghzaXguc3MwMgpzZXZlbi5zczAyCmVpZ2h0LnNzMDIJbmluZS5zczAyCXplcm8uc3MwMwhvbmUuc3MwMwh0d28uc3MwMwp0aHJlZS5zczAzCWZvdXIuc3MwMwlmaXZlLnNzMDMIc2l4LnNzMDMKc2V2ZW4uc3MwMwplaWdodC5zczAzCW5pbmUuc3MwMwl6ZXJvLmRub20Ib25lLmRub20IdHdvLmRub20KdGhyZWUuZG5vbQlmb3VyLmRub20JZml2ZS5kbm9tCHNpeC5kbm9tCnNldmVuLmRub20KZWlnaHQuZG5vbQluaW5lLmRub20HdW5pMjA4MAd1bmkyMDgxB3VuaTIwODIHdW5pMjA4Mwd1bmkyMDg0B3VuaTIwODUHdW5pMjA4Ngd1bmkyMDg3B3VuaTIwODgHdW5pMjA4OQl6ZXJvLm51bXIIb25lLm51bXIIdHdvLm51bXIKdGhyZWUubnVtcglmb3VyLm51bXIJZml2ZS5udW1yCHNpeC5udW1yCnNldmVuLm51bXIKZWlnaHQubnVtcgluaW5lLm51bXIHdW5pMjA3MAd1bmkwMEI5B3VuaTAwQjIHdW5pMDBCMwd1bmkyMDc0B3VuaTIwNzUHdW5pMjA3Ngd1bmkyMDc3B3VuaTIwNzgHdW5pMjA3OQd1bmkyMDBFB3VuaTIwMEYHdW5pRkVGRgd1bmkyMDBEB3VuaTIwMEMHdW5pMjAwQQd1bmkwMEEwB3VuaTIwMDkHdW5pMjAwQgd1bmkwNkQ0B3VuaTA2MEMHdW5pMDYxQgd1bmkwNjFGB3VuaTA2NkQHdW5pRkQzRQd1bmlGRDNGCnBlcmlvZC4wMDEHdW5pRkRGQwt1bmlGREZDLjAwMQt1bmlGREZDLjAwMgZ0b21hYW4KdG9tYWFuLjAwMQp0b21hYW4uMDAyCnRvbWFhbi4wMDMHdW5pMDY2QQZtaW51dGUGc2Vjb25kB3VuaTAwQjUHdW5pMjIxNQd1bmkwNjE1DGRvdGNlbnRlci1hch10d29kb3RzaG9yaXpvbnRhbGFib3ZlLWFyLjAwMRl0aHJlZWRvdHNkb3duYmVsb3ctYXIuMDAxF3RocmVlZG90c3VwYWJvdmUtYXIuMDAyCHdhc2xhLWFyDG1pbmlLZWhlaC1hchJnYWZzYXJrYXNoYWJvdmUtYXIWZ2Fmc2Fya2FzaGFib3ZlLWFyLjAwMQd1bmkwNjcwB3VuaTA2NTYHdW5pMDY1NAd1bmkwNjU1C3VuaTA2NTQwNjRGC3VuaTA2NTQwNjRDC3VuaTA2NTQwNjRFC3VuaTA2NTQwNjRCC3Vu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AAD/+AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA//b/2wAAAAAAAAAA/8UAAAAAAAD/4wAAAAD/6AAA/9YAAAACAA0ABAAIAAAACgAKAAUADQAPAAYAEgATAAkAFgAgAAsAIgAiABYAJQAlABcAKgAtABgALwAwABwAMwA3AB4B/wH/ACMCCgILACQCDgIPACYAAgAjAAUABQABAAYABgACAAcABwADAAgACAAEAAoACgACAA0ADQAFAA4ADgAGAA8ADwAHABIAEgADABMAEwAIABYAFgACABcAFwAJABgAGAAFABkAGQAKABoAGgALABsAGwAMABwAHAANAB0AHQAOAB4AHgAPAB8AHwAQACAAIAARACIAIgASACUAJQATACoAKwATACwALAAUAC0ALQAQAC8ALwAVADAAMAAPADMANAAWADUANQAXADYANgAWADcANwAYAf8B/wAZAgoCCwADAg4CDwAZAAIAIQAEAAQADQAGAAYAAQAKAAoAAQANAA0AEwASABIAAQAUABQAAQAWABYAFQAXABcAAgAYABgAAwAZABkABAAaABoABQAbABsADgAcABwABgAdAB0ADwAeAB4AFAAgACIAEQAkACQAEQAqACsABwAsACwACAAtAC0ABwAuAC4AEQAwADAAFgAyADIACgAzADMACwA1ADUAFwA2ADYACwA3ADcADAHgAeEAEAHkAeQAEAHtAe0AEgH/Af8ACQIKAgsAAQIOAg8ACQACAAgABAAOAkgH1AvwAAEAPAAFAAAAGQEiAHIAegDWAN4A5gD6AQ4BIgEqATIB4AG4AdIB0gHSAdIB4AHgAfQB9AH8AhACJAIyAAEAGQA5ADsAPQA+AD8AQQBCAEMARABuAIYAjACOAI8AkACRAJIAkwCUAJUAlgDaANsA3gDfAAEAAwAEAAQADwA9ADIAMgBBABIAEgBDAA4ADgC2/5b/lgC6/5b/lgDVACIAIgDWACIAIgDXACIAIgDYACIAIgDbACIAIgDcACIAIgDfACIAIgFPABwAHAFZACIAIgFbACIAIgABAF7/+P/4AAEAPwALAAsAAwBBADIAMgBDABIAEgFPACIAIgADAEEAGwAbAI8AGAAYAU8ABAAEAAMAQQAOAA4AQwAPAA8BTwAEAAQAAQFPAAQABAABAJUAAAAAABYApf/8//wApv/8//wAp//8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AAEAEgABACYAAAABAAAAKQABAAgAVgBmAHcAeAB7AHwA7AD5AAEAAQFIAAMAAQASAAEGCAAAAAEAAAAqAAEACgCbAJ0AowCkAKcAqQCsAK0A5ADmAAMAAQASAAEF3gAAAAEAAAArAAEAFwCAAIEAhACFALAAsQC0ALUAuAC5ALwAvQDAAMEAxADFANAA0QDaAN4A8wD0AQEABgAJAAICwAAKAAMAAAABAsgAAQLIAAEAAAArAAYACQACArwACgADAAAAAQLEAAECxAABAAAAKwAGAAkAAgK4AAoAAwAAAAECwAABAsAAAQAAACsABgAJAAICzAAKAAMAAAABAtQAAQLUAAEAAAArAAYACQACApgACgADAAAAAQKgAAECoAABAAAAKwAGAAkAAQAIAAMAAAABABIAAQKkAAEAAAArAAEAAQBTAAYACQABAAgAAwAAAAEAEgABAoQAAQAAACsAAQABATwABgAJAAEACAADAAAAAQASAAECZAABAAAAKwABAAEBJAAGAAkAAQAIAAMAAAABABIAAQJEAAEAAAArAAEAAQEzAAYACQABAAgAAwAAAAEAEgABAiQAAQAAACsAAQABAFsABgAJAAEACAADAAAAAQASAAECBAABAAAAKwABAAEAXgAGAAkAAQAIAAMAAAABABIAAQHkAAEAAAArAAEAAQEnAAYACQABAAgAAwAAAAEAEgABAcQAAQAAACsAAQABATYABgAJAAEACAADAAAAAQASAAEBpAABAAAAKwABAAEAVgAGAAkAAQAIAAMAAAABABIAAQGEAAEAAAArAAEAAQE/AAYACQAJABgAMABIAGAA9AEMASQBPAFUAAMAAQBaAAEAEgAAAAEAAAArAAEAAQE5AAMAAQBCAAEAEgAAAAEAAAArAAEAAQEhAAMAAQAqAAEAEgAAAAEAAAArAAEAAQEqAAMAAQASAAEAjgAAAAEAAAArAAEAPABiAGYAagBuAHIAdAB3AHgAewB8AIAAgQCEAIUAmwCdAKMApACnAKkArACtALAAsQC0ALUAuAC5ALwAvQDAAMEAxADFAMgAygDQANEA1ADVANoA2wDeAN8A5ADmAOsA7ADvAPAA8wD0APcA+QD/AQEBBwEIAQ0BDgABAAEBMAADAAAAAQASAAEAeAABAAAAKwABAAEBOwADAAAAAQASAAEAYAABAAAAKwABAAEBIwADAAAAAQASAAEASAABAAAAKwABAAEBMgADAAAAAQASAAEAMAABAAAAKwABAAEAUgADAAAAAQASAAEAGAABAAAAKwABAAEAWgABACQAQABUAFUAXABdAHYAegB+AIMAjgCQAJYAtwC7AM8A9gD7AP8BBwEMARQBFgEeASEBJQEmASoBMAE0ATUBOQE9AT4BQgFDAUgABgAJAAMADAAkADwAAwAAAAEAEgABAJwAAQAAACsAAQABAGYAAwAAAAEAEgABAIQAAQAAACsAAQABAGIAAwAAAAEAEgABAGwAAQAAACsAAQABAGMABgAJAAMADAAkADwAAwAAAAEAEgABAEgAAQAAACsAAQABAG4AAwAAAAEAEgABADAAAQAAACsAAQABAGoAAwAAAAEAEgABABgAAQAAACsAAQABAGsAAQALAGIAYwBkAGUAagBrAHIAcwCSAJYA0AAGAAkAAQAIAAMAAQAUAAEB2gABAGoAAQAAACwAAQApAFIAVABWAFcAWgBcAF4AXwBiAGYAagBuAHIAdACZAJsAnQCiAKMApACnAKkArACtAPcA+QEIASMBJQEnASgBLAEuATIBNAE2ATcBOwE9AT8BQAABABUARwBRAFIAVABZAFoAXABhAGIAaQBqAHEAcgD3ASMBJQEsATIBNAE7AT0AAQAAAAEACAABAAYACgACAAEBhgGPAAAAAQAAAAEACAACADQAFwF3AXgBeQF6AXsBfAF9AX4BfwGAAYEBggGDAZoBmwGcAZ0BngGfAaABoQGiAaMAAgADAWcBaQAAAW0BdgADAYYBjwANAAEAAAABAAgAAQAG/+wAAgABAbgBwQAAAAEACQABAAgAAQAUAAEAAQAJAAEACAABAAYAAgABAAIBQgFIAAEACQABAAgAAQAGAAMAAQABAUIAAQAJAAEACAACADoAGgBUAFUAVwBcAF0AXwBlAGQAZwBtAGwAbwEiASUBJgEoASsBMQE0ATUBNwE6AT0BPgFAAUYAAQAaAFIAUwBWAFoAWwBeAGIAYwBmAGoAawBuASEBIwEkAScBKgEwATIBMwE2ATkBOwE8AT8BQgABAAkAAQAIAAEABgABAAEADwBSAFQAWgBcAGIAagByAPcBIwElASwBMgE0ATsBPQAAAAAAAQAAAAA=";
