// Product List Page JavaScript

class ProductListManager {
    constructor() {
        this.currentPage = 0;
        this.pageSize = 10;
        this.totalPages = 0;
        this.totalElements = 0;
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        // Search input
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', this.debounce(() => this.filterProducts(), 300));
        }

        // Filter dropdowns
        const filterElements = ['statusFilter', 'categoryFilter', 'brandFilter', 'sortBy'];
        filterElements.forEach(id => {
            const element = document.getElementById(id);
            if (element) {
                element.addEventListener('change', () => this.filterProducts());
            }
        });

        // Pagination controls
        const prevBtn = document.getElementById('prevPage');
        const nextBtn = document.getElementById('nextPage');
        const pageSizeSelector = document.getElementById('pageSizeSelector');

        if (prevBtn) {
            prevBtn.addEventListener('click', () => this.goToPage(this.currentPage - 1));
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', () => this.goToPage(this.currentPage + 1));
        }

        if (pageSizeSelector) {
            pageSizeSelector.addEventListener('change', (e) => {
                this.pageSize = parseInt(e.target.value);
                this.currentPage = 0;
                this.loadPage(0);
            });
        }
    }

    async filterProducts() {
        // Reset to first page when filtering
        this.currentPage = 0;
        await this.loadPage(0);
    }

    async loadPage(page) {
        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: this.pageSize.toString()
            });

            // Add filter params if they exist
            const filterParams = this.buildFilterParams();
            filterParams.forEach((value, key) => {
                if (value && value.trim() !== '') {
                    params.append(key, value);
                }
            });

            const response = await fetch(`/admin/san-pham/api/paginated?${params.toString()}`);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();
            this.updateProductTable(data.content);
            this.updatePagination(data);
            this.currentPage = data.currentPage;
        } catch (error) {
            console.error('Error loading page:', error);
            this.showError('Có lỗi xảy ra khi tải trang');
        }
    }

    goToPage(page) {
        if (page >= 0 && page < this.totalPages) {
            this.loadPage(page);
        }
    }

    updatePagination(data) {
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.currentPage = data.currentPage;

        // Update page info
        const pageInfo = document.querySelector('.tw-text-sm.tw-text-gray-700');
        if (pageInfo) {
            if (data.totalElements > 0) {
                const startItem = (data.currentPage * data.pageSize) + 1;
                const endItem = Math.min((data.currentPage + 1) * data.pageSize, data.totalElements);
                pageInfo.innerHTML = `Hiển thị <span class="tw-font-medium">${this.formatNumber(startItem)}</span> - <span class="tw-font-medium">${this.formatNumber(endItem)}</span> trong tổng số <span class="tw-font-medium">${this.formatNumber(data.totalElements)}</span> sản phẩm (Trang <span class="tw-font-medium">${data.currentPage + 1}</span> / <span class="tw-font-medium">${data.totalPages}</span>)`;
            } else {
                pageInfo.innerHTML = `Không có sản phẩm nào`;
            }
        }

        // Update navigation buttons
        const prevBtn = document.getElementById('prevPage');
        const nextBtn = document.getElementById('nextPage');
        
        if (prevBtn) {
            prevBtn.disabled = !data.hasPrevious;
            prevBtn.classList.toggle('tw-opacity-50', !data.hasPrevious);
            prevBtn.classList.toggle('tw-cursor-not-allowed', !data.hasPrevious);
        }

        if (nextBtn) {
            nextBtn.disabled = !data.hasNext;
            nextBtn.classList.toggle('tw-opacity-50', !data.hasNext);
            nextBtn.classList.toggle('tw-cursor-not-allowed', !data.hasNext);
        }

        // Generate page numbers
        this.generatePageNumbers();
    }

    generatePageNumbers() {
        const pageNumbersContainer = document.getElementById('pageNumbers');
        if (!pageNumbersContainer) return;

        pageNumbersContainer.innerHTML = '';
        
        // Nếu không có trang nào, không hiển thị gì
        if (this.totalPages <= 0) return;
        
        const maxVisiblePages = 5;
        let startPage = Math.max(0, this.currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(this.totalPages - 1, startPage + maxVisiblePages - 1);
        
        if (endPage - startPage + 1 < maxVisiblePages) {
            startPage = Math.max(0, endPage - maxVisiblePages + 1);
        }

        // First page
        if (startPage > 0) {
            this.addPageNumber(0, '1');
            if (startPage > 1) {
                this.addPageEllipsis();
            }
        }

        // Page numbers
        for (let i = startPage; i <= endPage; i++) {
            this.addPageNumber(i, (i + 1).toString());
        }

        // Last page
        if (endPage < this.totalPages - 1) {
            if (endPage < this.totalPages - 2) {
                this.addPageEllipsis();
            }
            this.addPageNumber(this.totalPages - 1, this.totalPages.toString());
        }
    }

    addPageNumber(pageNum, text) {
        const pageNumbersContainer = document.getElementById('pageNumbers');
        const pageBtn = document.createElement('button');
        pageBtn.className = `tw-px-3 tw-py-2 tw-text-sm tw-font-medium tw-rounded-lg tw-border ${
            pageNum === this.currentPage
                ? 'tw-bg-blue-600 tw-text-white tw-border-blue-600'
                : 'tw-text-gray-500 tw-bg-white tw-border-gray-300 hover:tw-bg-gray-50'
        }`;
        pageBtn.textContent = text;
        pageBtn.addEventListener('click', () => this.goToPage(pageNum));
        pageNumbersContainer.appendChild(pageBtn);
    }

    addPageEllipsis() {
        const pageNumbersContainer = document.getElementById('pageNumbers');
        const ellipsis = document.createElement('span');
        ellipsis.className = 'tw-px-2 tw-py-2 tw-text-sm tw-text-gray-500';
        ellipsis.textContent = '...';
        pageNumbersContainer.appendChild(ellipsis);
    }

    buildFilterParams() {
        const params = new URLSearchParams();
        
        const filters = {
            keyword: document.getElementById('searchInput')?.value,
            trangThaiHienThi: document.getElementById('statusFilter')?.value,
            danhMucId: document.getElementById('categoryFilter')?.value,
            thuongHieuId: document.getElementById('brandFilter')?.value
        };

        // Add non-empty filters
        Object.entries(filters).forEach(([key, value]) => {
            if (value && value.trim() !== '') {
                params.append(key, value);
            }
        });

        // Add sorting
        const sortBy = document.getElementById('sortBy')?.value;
        if (sortBy) {
            const [field, order] = sortBy.split(':');
            params.append('sortBy', field);
            params.append('sortOrder', order);
        }

        return params;
    }

    updateProductTable(products) {
        const tbody = document.querySelector('#productTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';
        
        if (products.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" class="tw-px-6 tw-py-8 tw-text-center tw-text-gray-500">
                        <div class="tw-flex tw-flex-col tw-items-center">
                            <svg class="tw-w-12 tw-h-12 tw-text-gray-400 tw-mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"></path>
                            </svg>
                            <p class="tw-text-lg tw-font-medium">Không tìm thấy sản phẩm nào</p>
                            <p class="tw-text-sm">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        products.forEach(product => {
            const row = this.createProductRow(product);
            tbody.appendChild(row);
        });
    }

    createProductRow(product) {
        const row = document.createElement('tr');
        row.className = 'hover:tw-bg-gray-50 tw-transition-colors tw-duration-150 product-row';
        row.setAttribute('data-product-id', product.id);

        const danhMuc = product.danhMuc || '';
        const loaiThu = product.loaiThu || '';
        const tooltip = `Chất liệu: ${product.chatLieu || ''}\nThương hiệu: ${product.thuongHieu || ''}\nXuất xứ: ${product.xuatXu || ''}\nKiểu dáng: ${product.kieuDang || ''}`;

        row.innerHTML = `
            <td class="tw-px-6 tw-py-4 tw-whitespace-nowrap">
                <div class="tw-flex-shrink-0 tw-h-16 tw-w-16">
                    <img src="${product.anhChinh || '/images/default-product.jpg'}" 
                         alt="${product.ten}"
                         class="tw-h-16 tw-w-16 tw-rounded-lg tw-object-cover tw-shadow-sm tw-border tw-border-gray-200" 
                         onerror="this.src='/images/default-product.jpg'" />
                </div>
            </td>
            <td class="tw-px-6 tw-py-4">
                <div class="tw-space-y-1 tw-max-w-xs">
                    <div class="tw-text-xs tw-text-gray-500">${product.ma || ''}</div>
                    <div class="tw-text-sm tw-font-semibold tw-text-gray-800 tw-line-clamp-1" title="${product.ten || ''}">${product.ten || ''}</div>
                    ${product.moTa ? `<div class="tw-text-[11px] tw-text-gray-500 tw-line-clamp-1" title="${product.moTa}">${product.moTa}</div>` : ''}
                </div>
            </td>
            <td class="tw-px-6 tw-py-4">
                <div class="tw-flex tw-flex-wrap tw-gap-1">
                    ${danhMuc ? `<span class=\"tw-inline-flex tw-items-center tw-px-2 tw-py-0.5 tw-rounded-full tw-text-[11px] tw-font-medium tw-bg-blue-50 tw-text-blue-700\">${danhMuc}</span>` : ''}
                    ${loaiThu ? `<span class=\"tw-inline-flex tw-items-center tw-px-2 tw-py-0.5 tw-rounded-full tw-text-[11px] tw-font-medium tw-bg-green-50 tw-text-green-700\">${loaiThu}</span>` : ''}
                    <span class="tw-inline-flex tw-items-center tw-px-2 tw-py-0.5 tw-rounded-full tw-text-[11px] tw-font-medium tw-bg-gray-50 tw-text-gray-600" title="${tooltip}">+ chi tiết</span>
                </div>
            </td>
            <td class="tw-px-6 tw-py-4">
                <div class="tw-text-right">
                    <div class="tw-text-xl tw-font-bold tw-text-gray-800">${this.formatNumber(product.tongSoLuong || 0)}</div>
                </div>
            </td>
            <td class="tw-px-6 tw-py-4">
                ${this.formatPriceSection(product)}
            </td>
            <td class="tw-px-6 tw-py-4">
                <span class="tw-inline-flex tw-items-center tw-px-2.5 tw-py-0.5 tw-rounded-full tw-text-xs tw-font-medium tw-border ${product.trangThaiClass}">${product.trangThaiHienThi}</span>
            </td>
            <td class="tw-px-6 tw-py-4 tw-whitespace-nowrap tw-text-sm tw-text-gray-500 tw-hidden lg:tw-table-cell">
                <div>${this.formatDate(product.ngayTao)}</div>
                <div>${this.formatTime(product.ngayTao)}</div>
            </td>
            <td class="tw-px-6 tw-py-4 tw-whitespace-nowrap tw-text-sm tw-font-medium">
                <div class="tw-flex tw-items-center tw-gap-2">
                    <a href="/admin/san-pham/xem/${product.id}" class="tw-text-gray-400 hover:tw-text-gray-700 tw-p-2 tw-transition tw-duration-150" title="Chi tiết">
                        <i class="fas fa-layer-group"></i>
                    </a>
                    <a href="/admin/san-pham/sua/${product.id}" class="tw-text-gray-400 hover:tw-text-gray-700 tw-p-2 tw-transition tw-duration-150" title="Sửa">
                        <i class="fas fa-edit"></i>
                    </a>
                    <button onclick="deleteProduct(event, ${product.id})" class="tw-text-gray-400 hover:tw-text-gray-700 tw-p-2 tw-transition tw-duration-150" title="Xóa">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;

        return row;
    }

    formatPriceSection(product) {
        const range = (min, max) => {
            const fmt = (v) => new Intl.NumberFormat('vi-VN').format(v);
            if (min == null && max == null) return '';
            if (min != null && max != null) {
                if (min === max) return fmt(min) + 'đ';
                return fmt(min) + ' - ' + fmt(max) + 'đ';
            }
            if (min != null) return fmt(min) + 'đ';
            if (max != null) return fmt(max) + 'đ';
            return '';
        };

        const giaBan = (product.giaSauGiamMin != null && product.giaSauGiamMax != null)
            ? range(product.giaSauGiamMin, product.giaSauGiamMax)
            : null;
        const giaGoc = (product.giaGocMin != null && product.giaGocMax != null)
            ? range(product.giaGocMin, product.giaGocMax)
            : null;

        const main = giaBan || giaGoc || '';
        const tooltip = giaGoc ? `title=\"${giaGoc}\"` : '';

        return `
            <div class=\"tw-text-right\">
                ${giaGoc ? `<div class=\\"tw-text-sm tw-text-gray-500\\" ${tooltip}>&nbsp;</div>` : ''}
                <div class=\"tw-text-base tw-font-semibold tw-text-gray-900\">${main}</div>
            </div>
        `;
    }

    formatPriceRange(min, max) {
        const formatPrice = (price) => {
            return new Intl.NumberFormat('vi-VN').format(price);
        };
        
        if (min === max) return formatPrice(min) + 'đ';
        return formatPrice(min) + ' - ' + formatPrice(max) + 'đ';
    }

    formatNumber(number) {
        return new Intl.NumberFormat('vi-VN').format(number);
    }

    formatDate(dateString) {
        return new Date(dateString).toLocaleDateString('vi-VN');
    }

    formatTime(dateString) {
        return new Date(dateString).toLocaleTimeString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    deleteProduct(event, productId) {
        event.stopPropagation();
        if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
            window.location.href = `/admin/san-pham/xoa/${productId}`;
        }
    }

    showError(message) {
        // Create error notification
        const notification = document.createElement('div');
        notification.className = 'tw-fixed tw-top-4 tw-right-4 tw-bg-red-100 tw-border-l-4 tw-border-red-400 tw-text-red-700 tw-p-4 tw-rounded tw-shadow-lg tw-z-50';
        notification.innerHTML = `
            <div class="tw-flex tw-items-center">
                <svg class="tw-w-5 tw-h-5 tw-mr-2" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
                </svg>
                <span>${message}</span>
            </div>
        `;
        
        document.body.appendChild(notification);
        
        // Remove after 5 seconds
        setTimeout(() => {
            notification.remove();
        }, 5000);
    }

    debounce(func, wait) {
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
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.productListManager = new ProductListManager();
    
    // Initialize pagination with current page data from server
    const paginationContainer = document.querySelector('[data-current-page]');
    if (paginationContainer) {
        window.productListManager.currentPage = parseInt(paginationContainer.dataset.currentPage);
        window.productListManager.totalPages = parseInt(paginationContainer.dataset.totalPages);
        window.productListManager.totalElements = parseInt(paginationContainer.dataset.totalElements);
        window.productListManager.pageSize = parseInt(paginationContainer.dataset.pageSize);
        
        // Update pagination display
        if (window.productListManager.totalPages > 0) {
            window.productListManager.updatePagination({
                currentPage: window.productListManager.currentPage,
                totalPages: window.productListManager.totalPages,
                totalElements: window.productListManager.totalElements,
                pageSize: window.productListManager.pageSize,
                hasNext: window.productListManager.currentPage < window.productListManager.totalPages - 1,
                hasPrevious: window.productListManager.currentPage > 0
            });
        }
        
        // Update page size selector
        const pageSizeSelector = document.getElementById('pageSizeSelector');
        if (pageSizeSelector) {
            pageSizeSelector.value = window.productListManager.pageSize;
        }
    }
});

// Global function for delete (for onclick handlers)
function deleteProduct(event, productId) {
    if (window.productListManager) {
        window.productListManager.deleteProduct(event, productId);
    }
} 