// Global variables
let currentFilters = {
    q: '',
    loaiThuId: '',
    danhMucId: [],
    sizeId: '',
    mauSacId: '',
    kieuDangId: '',
    thuongHieuId: [],
    xuatXuId: '',
    priceRange: 10000000
};

let filterLabels = {
    q: 'Tìm kiếm',
    loaiThuId: 'Loại thú',
    danhMucId: 'Danh mục',
    sizeId: 'Kích cỡ',
    mauSacId: 'Màu sắc',
    kieuDangId: 'Kiểu dáng',
    thuongHieuId: 'Thương hiệu',
    xuatXuId: 'Xuất xứ',
    priceRange: 'Giá'
};

// Accordion toggle function
function toggleAccordion(accordionId) {
    const accordion = document.getElementById(accordionId);
    const icon = document.getElementById(accordionId + '-icon');
    
    if (!accordion) return;
    
    // Check current state based on maxHeight
    const isOpen = accordion.style.maxHeight && accordion.style.maxHeight !== '0px';
    
    if (isOpen) {
        // Close accordion
        accordion.style.maxHeight = '0px';
        
        // Rotate icon up (closed state)
        if (icon) {
            icon.style.transform = 'rotate(-180deg)';
            icon.style.transition = 'transform 0.3s ease';
        }
    } else {
        // Open accordion
        accordion.style.maxHeight = accordion.scrollHeight + 'px';
        
        // Rotate icon down (open state)
        if (icon) {
            icon.style.transform = 'rotate(0deg)';
            icon.style.transition = 'transform 0.3s ease';
        }
    }
}

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    initializeFilters();
    setupEventListeners();
    loadInitialFilters();
    
    // Initialize accordion states - default is open
    ['size-accordion', 'color-accordion', 'loaithu-accordion', 'danhmuc-accordion', 'thuonghieu-accordion', 'price-accordion'].forEach(accordionId => {
        const accordion = document.getElementById(accordionId);
        const icon = document.getElementById(accordionId + '-icon');
        
        if (accordion) {
            // Default to open state
            accordion.style.maxHeight = accordion.scrollHeight + 'px';
            
            // Set icon to down position (open state)
            if (icon) {
                icon.style.transform = 'rotate(0deg)';
            }
        }
    });
    
    // Enhance color display
    enhanceColorDisplay();
    
    // Make toggleAccordion available globally for onclick events
    window.toggleAccordion = toggleAccordion;

    // sort dropdown
    const sortSelect = document.querySelector('select');
    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            const val = this.value;
            if (val === 'Giá tăng dần') { window.currentSortBy = 'price'; window.currentSortDir = 'asc'; }
            else if (val === 'Giá giảm dần') { window.currentSortBy = 'price'; window.currentSortDir = 'desc'; }
            else if (val === 'Mới nhất') { window.currentSortBy = 'newest'; window.currentSortDir = 'desc'; }
            else { window.currentSortBy = null; window.currentSortDir = null; }
            applyFilters();
        });
    }

    // Trigger initial load to populate product grid
    if (typeof applyFilters === 'function') {
        applyFilters();
    }
});

// Load initial filter values from URL or server
function loadInitialFilters() {
    const urlParams = new URLSearchParams(window.location.search);
    
    // Update filters from URL
    Object.keys(currentFilters).forEach(key => {
        const value = urlParams.get(key);
        if (value) {
            if (Array.isArray(currentFilters[key])) {
                currentFilters[key] = value.split(',');
            } else {
                currentFilters[key] = value;
            }
            
            if (key === 'q') {
                const searchInput = document.getElementById('searchInput');
                if (searchInput) {
                    searchInput.value = value;
                }
            } else if (key === 'priceRange') {
                const priceSlider = document.getElementById('priceRange');
                if (priceSlider) {
                    priceSlider.value = value;
                    updatePriceDisplay(value);
                }
            } else {
                // Set button states
                updateButtonStates(key, currentFilters[key]);
            }
        }
    });
    
    updateActiveFilters();
}

// Update button states based on filter values
function updateButtonStates(filterType, values) {
    const dataAttr = getDataAttribute(filterType);
    
    if (Array.isArray(values)) {
        // Handle multiple selection (checkboxes)
        values.forEach(value => {
            const button = document.querySelector(`button[${dataAttr}="${value}"]`);
            if (button) {
                button.classList.add('selected');
                updateButtonStyle(button, true);
            }
        });
    } else {
        // Handle single selection (radio buttons)
        // First clear all selections
        document.querySelectorAll(`button[${dataAttr}]`).forEach(btn => {
            btn.classList.remove('selected');
            updateButtonStyle(btn, false);
        });
        
        // Then select the current one
        const button = document.querySelector(`button[${dataAttr}="${values}"]`);
        if (button) {
            button.classList.add('selected');
            updateButtonStyle(button, true);
        }
    }
}

// Convert filter type to data attribute name
function getDataAttribute(filterType) {
    const mapping = {
        'sizeId': 'data-size-id',
        'mauSacId': 'data-color-id',
        'loaiThuId': 'data-loai-thu-id',
        'danhMucId': 'data-danh-muc-id',
        'thuongHieuId': 'data-thuong-hieu-id',
        'priceRange': 'data-price-range'
    };
    return mapping[filterType] || `data-${filterType}`;
}

// Update button visual style
function updateButtonStyle(button, isSelected) {
    if (isSelected) {
        // Selected style
        if (button.hasAttribute('data-size-id')) {
            // Size buttons
            button.classList.remove('tw-border-neutral-300', 'tw-text-neutral-500');
            button.classList.add('tw-border-primary', 'tw-bg-primary', 'tw-text-light');
                 } else if (button.hasAttribute('data-color-id')) {
             // Color buttons
             const colorCircle = button.querySelector('span');
             if (colorCircle) {
                 colorCircle.style.transform = 'scale(1.1)';
                 colorCircle.style.boxShadow = '0 0 0 3px #3b82f6, 0 4px 12px rgba(0,0,0,0.15)';
                 colorCircle.style.borderColor = '#3b82f6';
                 colorCircle.style.borderWidth = '3px';
             }
             const colorText = button.querySelector('p');
             if (colorText) {
                 colorText.classList.remove('tw-text-neutral-500');
                 colorText.classList.add('tw-text-primary-500', 'tw-font-bold');
             }
        } else {
            // Radio/Checkbox buttons
            button.classList.remove('tw-border-neutral-500');
            button.classList.add('tw-border-primary', 'tw-bg-primary');
            // Add checkmark or dot
            if (button.getAttribute('role') === 'checkbox') {
                button.innerHTML = '<svg class="tw-w-3 tw-h-3 tw-text-white" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>';
            } else {
                button.innerHTML = '<div class="tw-w-2 tw-h-2 tw-bg-white tw-rounded-full"></div>';
            }
        }
    } else {
        // Unselected style
        if (button.hasAttribute('data-size-id')) {
            button.classList.remove('tw-border-primary', 'tw-bg-primary', 'tw-text-light');
            button.classList.add('tw-border-neutral-300', 'tw-text-neutral-500');
                 } else if (button.hasAttribute('data-color-id')) {
             const colorCircle = button.querySelector('span');
             if (colorCircle) {
                 colorCircle.style.transform = 'scale(1)';
                 colorCircle.style.boxShadow = '0 2px 4px rgba(0,0,0,0.1)';
                 colorCircle.style.borderWidth = '2px';
                 // Reset border color based on original color
                 const backgroundColor = getComputedStyle(colorCircle).backgroundColor;
                 if (isLightColor(backgroundColor)) {
                     colorCircle.style.borderColor = '#D1D5DB';
                 } else {
                     colorCircle.style.borderColor = backgroundColor;
                 }
             }
             const colorText = button.querySelector('p');
             if (colorText) {
                 colorText.classList.remove('tw-text-primary-500', 'tw-font-bold');
                 colorText.classList.add('tw-text-neutral-500');
             }
        } else {
            button.classList.remove('tw-border-primary', 'tw-bg-primary');
            button.classList.add('tw-border-neutral-500');
            button.innerHTML = '';
        }
    }
}

// Initialize filter functionality
function initializeFilters() {
    // Setup price range slider
    const priceSlider = document.getElementById('priceRange');
    if (priceSlider) {
        updatePriceDisplay(priceSlider.value);
        priceSlider.value = currentFilters.priceRange;
    }
}

// Setup event listeners
function setupEventListeners() {
    
    // Search input with debounce
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                currentFilters.q = this.value;
                applyFilters();
            }, 500);
        });
    }

    // Price range slider
    const priceSlider = document.getElementById('priceRange');
    if (priceSlider) {
        priceSlider.addEventListener('input', function() {
            updatePriceDisplay(this.value);
        });
        
        priceSlider.addEventListener('change', function() {
            currentFilters.priceRange = parseInt(this.value);
            applyFilters();
        });
    }

    // Size filter buttons (radio buttons)
    const sizeButtons = document.querySelectorAll('button[data-size-id]');
    sizeButtons.forEach((button, index) => {
        button.addEventListener('click', function() {
            const sizeId = this.getAttribute('data-size-id');
            currentFilters.sizeId = sizeId;
            
            // Update visual state
            document.querySelectorAll('button[data-size-id]').forEach(btn => {
                btn.classList.remove('selected');
                updateButtonStyle(btn, false);
            });
            this.classList.add('selected');
            updateButtonStyle(this, true);
            
            applyFilters();
        });
    });

    // Color filter buttons (radio buttons)
    const colorButtons = document.querySelectorAll('button[data-color-id]');
    colorButtons.forEach((button, index) => {
        button.addEventListener('click', function() {
            const colorId = this.getAttribute('data-color-id');
            currentFilters.mauSacId = colorId;
            
            // Update visual state
            document.querySelectorAll('button[data-color-id]').forEach(btn => {
                btn.classList.remove('selected');
                updateButtonStyle(btn, false);
            });
            this.classList.add('selected');
            updateButtonStyle(this, true);
            
            applyFilters();
        });
    });

    // Animal type filter buttons (radio buttons)
    document.querySelectorAll('button[data-loai-thu-id]').forEach(button => {
        button.addEventListener('click', function() {
            const loaiThuId = this.getAttribute('data-loai-thu-id');
            currentFilters.loaiThuId = loaiThuId;
            
            // Update visual state
            document.querySelectorAll('button[data-loai-thu-id]').forEach(btn => {
                btn.classList.remove('selected');
                updateButtonStyle(btn, false);
            });
            this.classList.add('selected');
            updateButtonStyle(this, true);
            
            applyFilters();
        });
    });

    // Category filter buttons (checkboxes - multiple selection)
    document.querySelectorAll('button[data-danh-muc-id]').forEach(button => {
        button.addEventListener('click', function() {
            const danhMucId = this.getAttribute('data-danh-muc-id');
            const isSelected = this.classList.contains('selected');
            
            if (isSelected) {
                // Unselect
                this.classList.remove('selected');
                updateButtonStyle(this, false);
                currentFilters.danhMucId = currentFilters.danhMucId.filter(id => id !== danhMucId);
            } else {
                // Select
                this.classList.add('selected');
                updateButtonStyle(this, true);
                if (!currentFilters.danhMucId.includes(danhMucId)) {
                    currentFilters.danhMucId.push(danhMucId);
                }
            }
            
            applyFilters();
        });
    });

    // Brand filter buttons (checkboxes - multiple selection)
    document.querySelectorAll('button[data-thuong-hieu-id]').forEach(button => {
        button.addEventListener('click', function() {
            const thuongHieuId = this.getAttribute('data-thuong-hieu-id');
            const isSelected = this.classList.contains('selected');
            
            if (isSelected) {
                // Unselect
                this.classList.remove('selected');
                updateButtonStyle(this, false);
                currentFilters.thuongHieuId = currentFilters.thuongHieuId.filter(id => id !== thuongHieuId);
            } else {
                // Select
                this.classList.add('selected');
                updateButtonStyle(this, true);
                if (!currentFilters.thuongHieuId.includes(thuongHieuId)) {
                    currentFilters.thuongHieuId.push(thuongHieuId);
                }
            }
            
            applyFilters();
        });
    });

    // Price range checkboxes
    document.querySelectorAll('button[data-price-range]').forEach(button => {
        button.addEventListener('click', function() {
            const priceRange = this.getAttribute('data-price-range');
            const isSelected = this.classList.contains('selected');
            
            // First deselect all price range buttons
            document.querySelectorAll('button[data-price-range]').forEach(btn => {
                btn.classList.remove('selected');
                updateButtonStyle(btn, false);
            });
            
            if (!isSelected) {
                // Select this one
                this.classList.add('selected');
                updateButtonStyle(this, true);
                
                // Set price range based on selection
                if (priceRange === '0-200000') {
                    currentFilters.priceRange = 200000;
                } else if (priceRange === '200000-300000') {
                    currentFilters.priceRange = 300000;
                } else if (priceRange === '300000-500000') {
                    currentFilters.priceRange = 500000;
                } else if (priceRange === '500000+') {
                    currentFilters.priceRange = 10000000;
                }
            } else {
                // Reset to max if deselecting
                currentFilters.priceRange = 10000000;
            }
            
            applyFilters();
        });
    });
}

// Update price display
function updatePriceDisplay(value) {
    const maxPriceElement = document.getElementById('maxPrice');
    if (maxPriceElement) {
        const formattedPrice = new Intl.NumberFormat('vi-VN').format(value);
        maxPriceElement.textContent = formattedPrice + 'đ';
    }
}

// Apply all filters
function applyFilters() {
    console.log('Applying filters:', currentFilters);
    showLoading();
    updateActiveFilters();
    
    // Build query parameters
    const params = new URLSearchParams();
    Object.keys(currentFilters).forEach(key => {
        if (currentFilters[key] && currentFilters[key] !== '') {
            if (Array.isArray(currentFilters[key])) {
                // Handle arrays (multiple values)
                if (currentFilters[key].length > 0) {
                    params.append(key, currentFilters[key].join(','));
                }
            } else {
                // Handle single values
                params.append(key, currentFilters[key]);
            }
        }
    });
    // sort controls
    if (window.currentSortBy) params.append('sortBy', window.currentSortBy);
    if (window.currentSortDir) params.append('sortDir', window.currentSortDir);

    // Make AJAX request
    console.log('Making AJAX request to:', `/san-pham/danh-sach/filter?${params.toString()}`);
    fetch(`/san-pham/danh-sach/filter?${params.toString()}`, {
        method: 'GET',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => {
        console.log('Response status:', response.status);
        return response.text();
    })
    .then(html => {
        console.log('Response HTML length:', html.length);
        console.log('Response HTML preview:', html.substring(0, 500));
        // Update URL without page reload
        const newUrl = window.location.pathname + (params.toString() ? '?' + params.toString() : '');
        window.history.pushState({}, '', newUrl);
        
        // Update product grid
        updateProductGrid(html);
        updateResultCount(html);
        hideLoading();
    })
    .catch(error => {
        console.error('Error applying filters:', error);
        hideLoading();
        showErrorMessage('Có lỗi xảy ra khi lọc sản phẩm. Vui lòng thử lại.');
    });
}

// Update result count
function updateResultCount(html) {
    // Count product cards in the returned HTML
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = html;
    const productCards = tempDiv.querySelectorAll('.tw-bg-white.tw-rounded-lg');
    const count = productCards.length;
    
    const resultCountElement = document.getElementById('filterResultCount');
    if (resultCountElement) {
        resultCountElement.textContent = `${count} kết quả`;
    }
}

// Update active filters display
function updateActiveFilters() {
    const activeFiltersContainer = document.getElementById('activeFilters');
    const filterTagsContainer = document.getElementById('filterTags');
    
    // Clear existing tags
    filterTagsContainer.innerHTML = '';
    
    // Add new tags
    let hasActiveFilters = false;
    Object.keys(currentFilters).forEach(key => {
        if (currentFilters[key] && currentFilters[key] !== '' && key !== 'priceRange') {
            if (Array.isArray(currentFilters[key])) {
                // Handle arrays
                if (currentFilters[key].length > 0) {
                    currentFilters[key].forEach(value => {
                        if (value !== '') {
                            hasActiveFilters = true;
                            const tag = createFilterTag(key, value);
                            filterTagsContainer.appendChild(tag);
                        }
                    });
                }
            } else {
                // Handle single values
                hasActiveFilters = true;
                const tag = createFilterTag(key, currentFilters[key]);
                filterTagsContainer.appendChild(tag);
            }
        }
    });

    // Add price range tag if not at maximum
    if (currentFilters.priceRange && currentFilters.priceRange < 10000000) {
        hasActiveFilters = true;
        const priceTag = createPriceFilterTag(currentFilters.priceRange);
        filterTagsContainer.appendChild(priceTag);
    }

    // Show/hide active filters container
    if (hasActiveFilters) {
        activeFiltersContainer.classList.remove('tw-hidden');
    } else {
        activeFiltersContainer.classList.add('tw-hidden');
    }
}

// Create filter tag element
function createFilterTag(filterType, value) {
    const tag = document.createElement('div');
    tag.className = 'tw-bg-blue-100 tw-text-blue-800 tw-text-sm tw-font-medium tw-px-3 tw-py-1 tw-rounded-full tw-flex tw-items-center tw-gap-2';
    
    let displayText = value;
    
    // Get display text for the value
    if (filterType !== 'q') {
        const dataAttr = getDataAttribute(filterType);
        const button = document.querySelector(`button[${dataAttr}="${value}"]`);
        if (button) {
            if (filterType === 'sizeId') {
                displayText = button.textContent.trim();
            } else if (filterType === 'mauSacId') {
                const colorText = button.querySelector('p');
                if (colorText) {
                    displayText = colorText.textContent.trim();
                }
            } else {
                const label = button.nextElementSibling;
                if (label && label.tagName === 'LABEL') {
                    displayText = label.textContent.trim();
                }
            }
        }
    }
    
    tag.innerHTML = `
        <span>${filterLabels[filterType]}: ${displayText}</span>
        <button onclick="removeFilter('${filterType}', '${value}')" class="tw-text-blue-600 tw-hover:text-blue-800">
            <svg class="tw-w-4 tw-h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
        </button>
    `;
    
    return tag;
}

// Create price filter tag
function createPriceFilterTag(value) {
    const tag = document.createElement('div');
    tag.className = 'tw-bg-green-100 tw-text-green-800 tw-text-sm tw-font-medium tw-px-3 tw-py-1 tw-rounded-full tw-flex tw-items-center tw-gap-2';
    
    const formattedPrice = new Intl.NumberFormat('vi-VN').format(value);
    
    tag.innerHTML = `
        <span>Giá tối đa: ${formattedPrice}đ</span>
        <button onclick="removeFilter('priceRange')" class="tw-text-green-600 tw-hover:text-green-800">
            <svg class="tw-w-4 tw-h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
        </button>
    `;
    
    return tag;
}

// Remove specific filter
function removeFilter(filterType, value = null) {
    if (filterType === 'priceRange') {
        currentFilters[filterType] = 10000000;
        const priceSlider = document.getElementById('priceRange');
        if (priceSlider) {
            priceSlider.value = 10000000;
            updatePriceDisplay(10000000);
        }
        // Clear price range buttons
        document.querySelectorAll('button[data-price-range]').forEach(btn => {
            btn.classList.remove('selected');
            updateButtonStyle(btn, false);
        });
    } else if (filterType === 'q') {
        currentFilters[filterType] = '';
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.value = '';
        }
    } else if (Array.isArray(currentFilters[filterType])) {
        // Handle array filters (checkboxes)
        if (value) {
            currentFilters[filterType] = currentFilters[filterType].filter(id => id !== value);
            const dataAttr = getDataAttribute(filterType);
            const button = document.querySelector(`button[${dataAttr}="${value}"]`);
            if (button) {
                button.classList.remove('selected');
                updateButtonStyle(button, false);
            }
        }
    } else {
        // Handle single value filters (radio buttons)
        currentFilters[filterType] = '';
        const dataAttr = getDataAttribute(filterType);
        
        // Clear all selections first
        document.querySelectorAll(`button[${dataAttr}]`).forEach(btn => {
            btn.classList.remove('selected');
            updateButtonStyle(btn, false);
        });
        
        // Select the "all" option (empty value)
        const allButton = document.querySelector(`button[${dataAttr}=""]`);
        if (allButton) {
            allButton.classList.add('selected');
            updateButtonStyle(allButton, true);
        }
    }
    
    applyFilters();
}

// Clear all filters
function clearAllFilters() {
    currentFilters = {
        q: '',
        loaiThuId: '',
        danhMucId: [],
        sizeId: '',
        mauSacId: '',
        kieuDangId: '',
        thuongHieuId: [],
        xuatXuId: '',
        priceRange: 10000000
    };

    // Reset search input
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = '';
    }

    // Reset all radio button filters to "All" option
    ['sizeId', 'mauSacId', 'loaiThuId'].forEach(filterType => {
        const dataAttr = getDataAttribute(filterType);
        
        // Clear all selections
        document.querySelectorAll(`button[${dataAttr}]`).forEach(btn => {
            btn.classList.remove('selected');
            updateButtonStyle(btn, false);
        });
        
        // Select the "all" option (empty value)
        const allButton = document.querySelector(`button[${dataAttr}=""]`);
        if (allButton) {
            allButton.classList.add('selected');
            updateButtonStyle(allButton, true);
        }
    });

    // Reset all checkbox filters
    ['danhMucId', 'thuongHieuId'].forEach(filterType => {
        const dataAttr = getDataAttribute(filterType);
        document.querySelectorAll(`button[${dataAttr}]`).forEach(button => {
            button.classList.remove('selected');
            updateButtonStyle(button, false);
        });
    });

    // Reset price range buttons
    document.querySelectorAll('button[data-price-range]').forEach(btn => {
        btn.classList.remove('selected');
        updateButtonStyle(btn, false);
    });

    // Reset price slider
    const priceSlider = document.getElementById('priceRange');
    if (priceSlider) {
        priceSlider.value = 10000000;
        updatePriceDisplay(10000000);
    }

    applyFilters();
}

// Update product grid with new data
function updateProductGrid(html) {
    console.log('updateProductGrid called with HTML length:', html.length);
    const productGrid = document.getElementById('productGrid');
    const noProducts = document.getElementById('noProducts');
    
    console.log('productGrid element:', productGrid);
    console.log('noProducts element:', noProducts);
    
    // Check if there are products
    if (html.includes('tw-bg-white tw-rounded-2xl')) {
        console.log('Found products in HTML');
        // Parse HTML và lấy content bên trong
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;
        
        console.log('Parsed HTML, looking for #productGrid');
        // Lấy nội dung bên trong div productGrid từ response
        const newContent = tempDiv.querySelector('#productGrid');
        console.log('Found newContent:', newContent);
        
        if (newContent) {
            console.log('Using newContent.innerHTML');
            productGrid.innerHTML = newContent.innerHTML;
        } else {
            console.log('Using full HTML');
            productGrid.innerHTML = html;
        }
        
        // Force lại CSS grid classes
        productGrid.className = 'tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-3 xl:tw-grid-cols-4 tw-gap-6';
        
        productGrid.classList.remove('tw-hidden');
        noProducts.classList.add('tw-hidden');
    } else {
        productGrid.classList.add('tw-hidden');
        noProducts.classList.remove('tw-hidden');
    }
}

// Show loading spinner
function showLoading() {
    const spinner = document.getElementById('loadingSpinner');
    if (spinner) {
        spinner.classList.remove('tw-hidden');
        spinner.classList.add('tw-flex');
    }
}

// Hide loading spinner
function hideLoading() {
    const spinner = document.getElementById('loadingSpinner');
    if (spinner) {
        spinner.classList.add('tw-hidden');
        spinner.classList.remove('tw-flex');
    }
}

// Show error message
function showErrorMessage(message) {
    // Create a toast notification
    const toast = document.createElement('div');
    toast.className = 'tw-fixed tw-top-4 tw-right-4 tw-bg-red-500 tw-text-white tw-px-6 tw-py-3 tw-rounded-lg tw-shadow-lg tw-z-50';
    toast.textContent = message;
    
    document.body.appendChild(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 5000);
}

// Add to cart function - removed duplicate, using miniCart instead

// Add to wishlist function
function addToWishlist(productId) {
    fetch('/yeu-thich/toggle', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ productId })
    }).then(async res => {
        if (res.status === 401) {
            showErrorMessage('Vui lòng đăng nhập để sử dụng yêu thích');
            return;
        }
        const data = await res.json();
        const btn = document.querySelector(`button[data-wishlist-id="${productId}"]`);
        if (btn) {
            btn.classList.toggle('tw-text-pink-600', data.liked);
            btn.classList.toggle('tw-text-gray-400', !data.liked);
        }
    }).catch(() => showErrorMessage('Không thể cập nhật yêu thích'));
}

// Enhance color display
function enhanceColorDisplay() {
    document.querySelectorAll('button[data-color-id] span').forEach(colorSpan => {
        const backgroundColor = getComputedStyle(colorSpan).backgroundColor;
        const button = colorSpan.parentElement;
        
        // Add hover effects
        button.addEventListener('mouseenter', function() {
            if (!this.classList.contains('selected')) {
                colorSpan.style.transform = 'scale(1.05)';
                colorSpan.style.boxShadow = '0 4px 12px rgba(0,0,0,0.2)';
            }
        });
        
        button.addEventListener('mouseleave', function() {
            if (!this.classList.contains('selected')) {
                colorSpan.style.transform = 'scale(1)';
                colorSpan.style.boxShadow = '0 2px 4px rgba(0,0,0,0.1)';
            }
        });
    });
}

// Helper function to check if color is light
function isLightColor(rgbColor) {
    if (!rgbColor) return false;
    
    // Extract RGB values
    const rgb = rgbColor.match(/\d+/g);
    if (!rgb || rgb.length < 3) return false;
    
    const r = parseInt(rgb[0]);
    const g = parseInt(rgb[1]);
    const b = parseInt(rgb[2]);
    
    // Calculate luminance
    const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
    return luminance > 0.5;
} 