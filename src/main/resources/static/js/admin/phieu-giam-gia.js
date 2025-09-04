/**
 * Voucher Form Validation and Enhancement Script
 * Handles both Create and Edit forms with comprehensive validation
 */

class VoucherFormValidator {
    constructor(formId) {
        this.form = document.getElementById(formId);
        this.isEditMode = formId === 'editVoucherForm';
        this.errors = {};

        if (!this.form) {
            console.error('Form not found:', formId);
            return;
        }

        this.initializeElements();
        this.attachEventListeners();
        this.setDefaultDates();
    }

    initializeElements() {
        this.elements = {
            ma: this.form.querySelector('input[name="ma"]'),
            ten: this.form.querySelector('input[name="ten"]'),
            loaiPhieu: this.form.querySelector('select[name="loaiPhieuGiamGia"]'),
            mucDo: this.form.querySelector('input[name="mucDo"]'),
            giamToiDa: this.form.querySelector('input[name="giamToiDa"]'),
            dieuKien: this.form.querySelector('input[name="dieuKien"]'),
            ngayBatDau: this.form.querySelector('input[name="ngayBatDau"]'),
            ngayKetThuc: this.form.querySelector('input[name="ngayKetThuc"]'),
            soLuongTon: this.form.querySelector('input[name="soLuongTon"]'),
            trangThai: this.form.querySelectorAll('input[name="trangThai"]'),
            submitBtn: this.form.querySelector('button[type="submit"]'),
            mucDoUnit: this.form.querySelector('#mucDoUnit'),
            divGiamToiDa: this.form.querySelector('#divGiamToiDa')
        };
    }

    attachEventListeners() {
        // Form submission
        this.form.addEventListener('submit', (e) => this.handleSubmit(e));

        // Loại phiếu change
        this.elements.loaiPhieu?.addEventListener('change', () => this.handleLoaiPhieuChange());

        // Real-time validation
        this.elements.ma?.addEventListener('input', () => this.validateMa());
        this.elements.ten?.addEventListener('input', () => this.validateTen());
        this.elements.mucDo?.addEventListener('input', () => this.validateMucDo());
        this.elements.giamToiDa?.addEventListener('input', () => this.validateGiamToiDa());
        this.elements.dieuKien?.addEventListener('input', () => this.validateDieuKien());
        this.elements.soLuongTon?.addEventListener('input', () => this.validateSoLuong());

        // Date validation
        this.elements.ngayBatDau?.addEventListener('change', () => this.validateDates());
        this.elements.ngayKetThuc?.addEventListener('change', () => this.validateDates());

        // Number input restrictions
        this.elements.mucDo?.addEventListener('input', (e) => this.limitNumberInput(e.target));
        this.elements.giamToiDa?.addEventListener('input', (e) => this.limitNumberInput(e.target, 18));
        this.elements.dieuKien?.addEventListener('input', (e) => this.limitNumberInput(e.target, 18));
        this.elements.soLuongTon?.addEventListener('input', (e) => this.limitNumberInput(e.target, 6));

        // Format currency on blur
        [this.elements.giamToiDa, this.elements.dieuKien].forEach(el => {
            if (el) {
                el.addEventListener('blur', () => this.formatCurrency(el));
            }
        });
    }

    setDefaultDates() {
        if (!this.isEditMode && this.elements.ngayBatDau && !this.elements.ngayBatDau.value) {
            const today = new Date().toISOString().split('T')[0];
            const nextMonth = new Date();
            nextMonth.setMonth(nextMonth.getMonth() + 1);

            this.elements.ngayBatDau.value = today;
            this.elements.ngayBatDau.min = today;
            this.elements.ngayKetThuc.value = nextMonth.toISOString().split('T')[0];
        }
    }

    handleLoaiPhieuChange() {
        const loaiPhieu = this.elements.loaiPhieu.value;
        const mucDo = this.elements.mucDo;
        const giamToiDa = this.elements.giamToiDa;
        const mucDoUnit = this.elements.mucDoUnit;
        const divGiamToiDa = this.elements.divGiamToiDa;

        if (loaiPhieu === '1') { // Phần trăm
            mucDo.placeholder = 'Nhập phần trăm (1-100)';
            mucDo.min = '1';
            mucDo.max = '100';
            mucDo.step = '0.01';
            mucDoUnit.textContent = '%';
            divGiamToiDa.style.display = 'block';
            giamToiDa.required = true;

            // Clear value if switching from currency to percentage
            if (mucDo.value > 100) {
                mucDo.value = '';
            }
        } else if (loaiPhieu === '2') { // Tiền mặt
            mucDo.placeholder = 'Nhập số tiền giảm (tối thiểu 1,000 VNĐ)';
            mucDo.min = '1000';
            mucDo.max = '';
            mucDo.step = '1000';
            mucDoUnit.textContent = 'VNĐ';
            divGiamToiDa.style.display = 'none';
            giamToiDa.required = false;
            giamToiDa.value = mucDo.value || '';
        }

        this.validateMucDo();
        this.clearError('loaiPhieuGiamGia');
    }

    validateMa() {
        const ma = this.elements.ma?.value?.trim();

        if (!ma) {
            this.clearError('ma');
            return true; // Cho phép để trống khi tạo mới
        }

        // Validate format
        if (!/^[A-Z0-9]{3,50}$/.test(ma)) {
            this.setError('ma', 'Mã phải từ 3-50 ký tự, chỉ chứa chữ hoa và số');
            return false;
        }

        this.clearError('ma');
        return true;
    }

    validateTen() {
        const ten = this.elements.ten.value.trim();

        if (!ten) {
            this.setError('ten', 'Tên không được để trống');
            return false;
        }

        if (ten.length > 100) {
            this.setError('ten', 'Tên không được vượt quá 100 ký tự');
            return false;
        }

        if (ten.length < 3) {
            this.setError('ten', 'Tên phải có ít nhất 3 ký tự');
            return false;
        }

        this.clearError('ten');
        return true;
    }

    validateMucDo() {
        const mucDo = parseFloat(this.elements.mucDo.value);
        const loaiPhieu = this.elements.loaiPhieu.value;

        if (!mucDo || mucDo <= 0) {
            this.setError('mucDo', 'Mức độ giảm giá không được để trống');
            return false;
        }

        if (loaiPhieu === '1') { // Phần trăm
            if (mucDo < 1 || mucDo > 100) {
                this.setError('mucDo', 'Mức giảm phần trăm phải từ 1% đến 100%');
                return false;
            }
        } else if (loaiPhieu === '2') { // Tiền mặt
            if (mucDo < 1000) {
                this.setError('mucDo', 'Mức giảm tiền mặt tối thiểu 1,000 VNĐ');
                return false;
            }
            if (mucDo > 10000000) { // 10 triệu
                this.setError('mucDo', 'Mức giảm tiền mặt tối đa 10,000,000 VNĐ');
                return false;
            }
        }

        this.clearError('mucDo');
        this.updateBorderColor(this.elements.mucDo, true);
        return true;
    }

    validateGiamToiDa() {
        const giamToiDa = parseFloat(this.elements.giamToiDa.value);
        const loaiPhieu = this.elements.loaiPhieu.value;

        // Only validate if percentage type
        if (loaiPhieu !== '1') {
            this.clearError('giamToiDa');
            return true;
        }

        if (!giamToiDa || giamToiDa <= 0) {
            this.setError('giamToiDa', 'Giảm tối đa không được để trống');
            return false;
        }

        if (giamToiDa > 500000) { // 500k
            this.setError('giamToiDa', 'Giảm tối đa không được vượt quá 500,000 VNĐ');
            return false;
        }

        const dieuKien = parseFloat(this.elements.dieuKien.value);
        const mucDo = parseFloat(this.elements.mucDo.value);

        // if (dieuKien && mucDo) {
        //     const maxPossibleDiscount = (dieuKien * mucDo) / 100;
        //     if (giamToiDa > maxPossibleDiscount) {
        //         this.setError('giamToiDa',
        //             `Giảm tối đa không được lớn hơn ${this.formatMoney(maxPossibleDiscount)} VNĐ (${mucDo}% của điều kiện)`);
        //         return false;
        //     }
        // }

        this.clearError('giamToiDa');
        this.updateBorderColor(this.elements.giamToiDa, true);
        return true;
    }

    validateDieuKien() {
        const dieuKien = parseFloat(this.elements.dieuKien.value);

        if (!dieuKien || dieuKien < 0) {
            this.setError('dieuKien', 'Điều kiện áp dụng không được để trống');
            return false;
        }

        if (dieuKien > 50000000) { // 50 triệu
            this.setError('dieuKien', 'Điều kiện không được vượt quá 50,000,000 VNĐ');
            return false;
        }

        // Validate logic with mức giảm for currency type
        const loaiPhieu = this.elements.loaiPhieu.value;
        const mucDo = parseFloat(this.elements.mucDo.value);

        if (loaiPhieu === '2' && mucDo && dieuKien < mucDo) {
            this.setError('dieuKien', 'Điều kiện phải lớn hơn mức giảm tiền mặt');
            return false;
        }

        this.clearError('dieuKien');
        this.updateBorderColor(this.elements.dieuKien, true);
        return true;
    }

    validateSoLuong() {
        const soLuong = parseInt(this.elements.soLuongTon.value);

        // Cho phép để trống (unlimited)
        if (!this.elements.soLuongTon.value.trim()) {
            this.clearError('soLuongTon');
            return true;
        }

        if (soLuong < 0) {
            this.setError('soLuongTon', 'Số lượng không được âm');
            return false;
        }

        if (soLuong > 100000) {
            this.setError('soLuongTon', 'Số lượng không được vượt quá 100,000');
            return false;
        }

        this.clearError('soLuongTon');
        this.updateBorderColor(this.elements.soLuongTon, true);
        return true;
    }

    validateDates() {
        const ngayBatDau = this.elements.ngayBatDau.value;
        const ngayKetThuc = this.elements.ngayKetThuc.value;
        const today = new Date().toISOString().split('T')[0];

        if (!ngayBatDau) {
            this.setError('ngayBatDau', 'Ngày bắt đầu không được để trống');
            return false;
        }

        if (!ngayKetThuc) {
            this.setError('ngayKetThuc', 'Ngày kết thúc không được để trống');
            return false;
        }

        // For create form, start date should not be in the past
        // if (!this.isEditMode && ngayBatDau < today) {
        //     this.setError('ngayBatDau', 'Ngày bắt đầu không được nhỏ hơn ngày hiện tại');
        //     return false;
        // }

        if (ngayKetThuc <= ngayBatDau) {
            this.setError('ngayKetThuc', 'Ngày kết thúc phải sau ngày bắt đầu');
            return false;
        }

        // Check minimum duration (1 day)
        const startDate = new Date(ngayBatDau);
        const endDate = new Date(ngayKetThuc);
        const diffTime = endDate - startDate;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays < 1) {
            this.setError('ngayKetThuc', 'Phiếu giảm giá phải có thời hạn tối thiểu 1 ngày');
            return false;
        }

        // Update min date for end date
        this.elements.ngayKetThuc.min = ngayBatDau;

        this.clearError('ngayBatDau');
        this.clearError('ngayKetThuc');
        return true;
    }

    validateAll() {
        let isValid = true;

        // Validate all fields
        if (!this.validateTen()) isValid = false;
        if (!this.validateMucDo()) isValid = false;
        if (!this.validateGiamToiDa()) isValid = false;
        if (!this.validateDieuKien()) isValid = false;
        if (!this.validateSoLuong()) isValid = false;
        if (!this.validateDates()) isValid = false;

        if (!this.isEditMode && !this.validateMa()) isValid = false;

        return isValid;
    }

    handleSubmit(e) {
        e.preventDefault();

        // Show loading state
        const originalText = this.elements.submitBtn.innerHTML;
        this.elements.submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin tw-mr-2"></i>Đang xử lý...';
        // this.elements.submitBtn.disabled = true;

        // Validate form
        if (!this.validateAll()) {
            // Show error notification
            this.showNotification('Vui lòng kiểm tra và sửa các lỗi trong form', 'error');

            // Reset button
            this.elements.submitBtn.innerHTML = originalText;
            this.elements.submitBtn.disabled = false;
            return;
        }

        // Auto-set giảm tối đa for currency type
        if (this.elements.loaiPhieu.value === '2') {
            this.elements.giamToiDa.value = this.elements.mucDo.value;
        }

        // Submit form
        this.form.submit();
    }

    limitNumberInput(element, maxLength = 10) {
        let value = element.value;

        // Remove non-numeric characters except decimal point
        value = value.replace(/[^0-9.]/g, '');

        // Ensure only one decimal point
        const parts = value.split('.');
        if (parts.length > 2) {
            value = parts[0] + '.' + parts.slice(1).join('');
        }

        // Limit length
        if (value.length > maxLength) {
            value = value.substring(0, maxLength);
        }

        element.value = value;
    }

    formatCurrency(element) {
        const value = parseFloat(element.value);
        if (!isNaN(value) && value > 0) {
            // Round to nearest thousand for VND
            element.value = Math.round(value);
        }
    }

    formatMoney(amount) {
        return new Intl.NumberFormat('vi-VN').format(amount);
    }

    setError(fieldName, message) {
        this.errors[fieldName] = message;
        const field = this.elements[fieldName] || this.form.querySelector(`[name="${fieldName}"]`);

        if (field) {
            this.updateBorderColor(field, false);
            this.showFieldError(field, message);
        }
    }

    clearError(fieldName) {
        delete this.errors[fieldName];
        const field = this.elements[fieldName] || this.form.querySelector(`[name="${fieldName}"]`);

        if (field) {
            this.updateBorderColor(field, true);
            this.hideFieldError(field);
        }
    }

    updateBorderColor(element, isValid) {
        element.classList.remove('tw-border-red-500', 'tw-border-green-500', 'tw-border-gray-300');

        if (element.value.trim()) {
            element.classList.add(isValid ? 'tw-border-green-500' : 'tw-border-red-500');
        } else {
            element.classList.add('tw-border-gray-300');
        }
    }

    showFieldError(field, message) {
        this.hideFieldError(field);

        const errorDiv = document.createElement('div');
        errorDiv.className = 'tw-text-sm tw-text-red-600 tw-mt-1 field-error';
        errorDiv.innerHTML = `<i class="fas fa-exclamation-circle tw-mr-1"></i>${message}`;

        field.parentNode.appendChild(errorDiv);
    }

    hideFieldError(field) {
        const existingError = field.parentNode.querySelector('.field-error');
        if (existingError) {
            existingError.remove();
        }
    }

    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `tw-fixed tw-top-4 tw-right-4 tw-px-6 tw-py-4 tw-rounded-lg tw-shadow-lg tw-z-50 tw-max-w-sm tw-animate-fade-in`;

        const icons = {
            success: 'fas fa-check-circle tw-text-green-500',
            error: 'fas fa-exclamation-circle tw-text-red-500',
            warning: 'fas fa-exclamation-triangle tw-text-yellow-500',
            info: 'fas fa-info-circle tw-text-blue-500'
        };

        const colors = {
            success: 'tw-bg-green-50 tw-border tw-border-green-200 tw-text-green-800',
            error: 'tw-bg-red-50 tw-border tw-border-red-200 tw-text-red-800',
            warning: 'tw-bg-yellow-50 tw-border tw-border-yellow-200 tw-text-yellow-800',
            info: 'tw-bg-blue-50 tw-border tw-border-blue-200 tw-text-blue-800'
        };

        notification.className += ` ${colors[type]}`;
        notification.innerHTML = `
            <div class="tw-flex tw-items-center">
                <i class="${icons[type]} tw-mr-3"></i>
                <span>${message}</span>
                <button class="tw-ml-4 tw-text-gray-500 hover:tw-text-gray-700" onclick="this.parentElement.parentElement.remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;

        document.body.appendChild(notification);

        // Auto remove after 5 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 5000);
    }
}

// Initialize validator when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on create or edit page
    if (document.getElementById('voucherForm')) {
        new VoucherFormValidator('voucherForm');
    } else if (document.getElementById('editVoucherForm')) {
        new VoucherFormValidator('editVoucherForm');
    }
});

// Additional utility functions
function limitNumberLength(element, maxLength) {
    if (element.value.length > maxLength) {
        element.value = element.value.slice(0, maxLength);
    }
}

// Format Vietnamese currency
function formatVietnameseCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Prevent negative numbers
function preventNegative(event) {
    if (event.key === '-' || event.key === 'e' || event.key === 'E') {
        event.preventDefault();
    }
}

// Add event listeners to prevent negative input
document.addEventListener('DOMContentLoaded', function() {
    const numberInputs = document.querySelectorAll('input[type="number"]');
    numberInputs.forEach(input => {
        input.addEventListener('keydown', preventNegative);
    });
});