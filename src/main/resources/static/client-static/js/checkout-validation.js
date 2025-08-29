/**
 * CHECKOUT VALIDATION & FUNCTIONALITY
 * Handles form validation, shipping fee calculation, and checkout process
 */

class CheckoutValidator {
    constructor() {
        this.requiredFields = [
            { id: 'tenNguoiNhan', rules: { required: true, minLength: 2 } },
            { id: 'soDienThoai', rules: { required: true, pattern: /^[0-9]{9,11}$/, patternMessage: 'Số điện thoại phải có 9-11 chữ số' } },
            { id: 'email', rules: { required: true, email: true } },
            { id: 'provinceSelect', rules: { required: true } },
            { id: 'districtSelect', rules: { required: true } },
            { id: 'wardSelect', rules: { required: true } },
            { id: 'diaChi', rules: { required: true, minLength: 10 } }
        ];
        
        this.currentDiscount = 0;
        this.currentShippingFee = 0;
        this.tongTien = 0;
        
        this.init();
    }

    init() {
        this.bindEvents();
        this.initializeValidation();
        this.initializeShippingFee();
        this.initializeDiscount();
    }

    bindEvents() {
        // Form field validation events
        this.requiredFields.forEach(fieldConfig => {
            const field = document.getElementById(fieldConfig.id);
            if (field) {
                field.addEventListener('input', () => this.validateForm());
                field.addEventListener('change', () => this.validateForm());
                field.addEventListener('blur', () => this.validateForm());
            }
        });

        // Payment method selection
        const paymentInputs = document.querySelectorAll('input[name="phuongThucThanhToan"]');
        paymentInputs.forEach(input => {
            input.addEventListener('change', () => this.validateForm());
        });

        // Location selection events
        this.bindLocationEvents();
        
        // Discount code events
        this.bindDiscountEvents();
    }

    bindLocationEvents() {
        const provinceSelect = document.getElementById('provinceSelect');
        const districtSelect = document.getElementById('districtSelect');
        const wardSelect = document.getElementById('wardSelect');

        if (provinceSelect) {
            provinceSelect.addEventListener('change', (e) => this.handleProvinceChange(e));
        }

        if (districtSelect) {
            districtSelect.addEventListener('change', (e) => this.handleDistrictChange(e));
        }

        if (wardSelect) {
            wardSelect.addEventListener('change', (e) => this.handleWardChange(e));
        }
    }

    bindDiscountEvents() {
        const phieuGiamGia = document.getElementById('phieuGiamGia');
        if (phieuGiamGia) {
            phieuGiamGia.addEventListener('change', (e) => this.handleDiscountChange(e));
        }
    }

    // Validation methods
    validateField(field, validationRules) {
        const value = field.value.trim();
        const fieldId = field.id;
        const errorElement = document.getElementById(fieldId + '-error');
        let isValid = true;
        let errorMessage = '';

        // Remove existing validation classes
        field.classList.remove('is-valid', 'is-invalid');

        // Check required
        if (validationRules.required && !value) {
            isValid = false;
            errorMessage = 'Trường này là bắt buộc';
        } else if (value) {
            // Check pattern for phone
            if (validationRules.pattern && !validationRules.pattern.test(value)) {
                isValid = false;
                errorMessage = validationRules.patternMessage || 'Định dạng không hợp lệ';
            }

            // Check email format
            if (validationRules.email && !this.isValidEmail(value)) {
                isValid = false;
                errorMessage = 'Email không hợp lệ';
            }

            // Check min length
            if (validationRules.minLength && value.length < validationRules.minLength) {
                isValid = false;
                errorMessage = `Tối thiểu ${validationRules.minLength} ký tự`;
            }
        }

        // Apply validation classes and show/hide error message
        if (isValid) {
            field.classList.add('is-valid');
            if (errorElement) {
                errorElement.style.display = 'none';
            }
        } else {
            field.classList.add('is-invalid');
            if (errorElement) {
                errorElement.textContent = errorMessage;
                errorElement.style.display = 'block';
            }
        }

        return isValid;
    }

    isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    validateForm() {
        let allValid = true;
        
        this.requiredFields.forEach(fieldConfig => {
            const field = document.getElementById(fieldConfig.id);
            if (field) {
                const isValid = this.validateField(field, fieldConfig.rules);
                if (!isValid) allValid = false;
            }
        });

        // Check payment method
        const paymentMethod = document.querySelector('input[name="phuongThucThanhToan"]:checked');
        if (!paymentMethod) {
            allValid = false;
        }

        // Enable/disable submit button
        this.updateSubmitButton(allValid);
        return allValid;
    }

    updateSubmitButton(isValid) {
        const submitBtn = document.getElementById('submitBtn');
        if (submitBtn) {
            submitBtn.disabled = !isValid;
        }
    }

    // Location handling methods
    async handleProvinceChange(event) {
        const provinceId = event.target.value;
        const selectedText = event.target.options[event.target.selectedIndex]?.text || "";
        
        // Update hidden field
        document.getElementById('tenTinhHidden').value = selectedText;
        
        // Clear dependent fields
        this.clearSelect('districtSelect', '-- Chọn Quận / Huyện --');
        this.clearSelect('wardSelect', '-- Chọn Phường / Xã --');
        document.getElementById('tenHuyenHidden').value = "";
        document.getElementById('tenXaHidden').value = "";

        // Reset shipping fee
        this.resetShippingFee();

        if (!provinceId) return;

        try {
            const response = await fetch(`/gio-hang/thanh-toan/location?provinceId=${provinceId}`);
            const districts = await response.json();
            this.populateSelect('districtSelect', districts, 'DistrictID', 'DistrictName');
        } catch (err) {
            console.error('Lỗi lấy districts:', err);
        }
    }

    async handleDistrictChange(event) {
        const districtId = event.target.value;
        const selectedText = event.target.options[event.target.selectedIndex]?.text || "";
        
        // Update hidden field
        document.getElementById('tenHuyenHidden').value = selectedText;
        
        // Clear dependent fields
        this.clearSelect('wardSelect', '-- Chọn Phường / Xã --');
        document.getElementById('tenXaHidden').value = "";

        // Reset shipping fee
        this.resetShippingFee();

        if (!districtId) return;

        try {
            const response = await fetch(`/gio-hang/thanh-toan/location?districtId=${districtId}`);
            const wards = await response.json();
            this.populateSelect('wardSelect', wards, 'WardCode', 'WardName');
        } catch (err) {
            console.error('Lỗi lấy wards:', err);
        }
    }

    async handleWardChange(event) {
        const wardCode = event.target.value;
        const selectedText = event.target.options[event.target.selectedIndex]?.text || "";
        
        // Update hidden field
        document.getElementById('tenXaHidden').value = selectedText;
        
        // Calculate shipping fee
        await this.updateShippingFee();
    }

    clearSelect(selectId, placeholder = '-- Chọn --') {
        const select = document.getElementById(selectId);
        if (!select) return;
        select.innerHTML = `<option value="">${placeholder}</option>`;
    }

    populateSelect(selectId, items, valueKey, textKey) {
        const select = document.getElementById(selectId);
        if (!select) return;
        
        this.clearSelect(selectId);
        items.forEach(item => {
            const option = document.createElement('option');
            option.value = item[valueKey];
            option.textContent = item[textKey];
            select.appendChild(option);
        });
    }

    resetShippingFee() {
        this.currentShippingFee = 0;
        const shippingFeeText = document.getElementById('tienVanChuyenText');
        const shippingFeeInput = document.getElementById('tienVanChuyenInput');
        
        if (shippingFeeText) shippingFeeText.innerText = "0 ₫";
        if (shippingFeeInput) shippingFeeInput.value = 0;
        
        this.updateTotalPay();
    }

    async updateShippingFee() {
        const districtId = document.getElementById('districtSelect')?.value;
        const wardCode = document.getElementById('wardSelect')?.value;

        if (!districtId || !wardCode) {
            this.resetShippingFee();
            return;
        }

        try {
            const response = await fetch(`/gio-hang/thanh-toan/shipping-fee?districtId=${districtId}&wardCode=${wardCode}`);
            if (!response.ok) throw new Error("Không thể lấy phí vận chuyển");
            
            const fee = await response.json();
            this.currentShippingFee = fee;
            
            const shippingFeeText = document.getElementById('tienVanChuyenText');
            const shippingFeeInput = document.getElementById('tienVanChuyenInput');
            
            if (shippingFeeText) shippingFeeText.innerText = fee.toLocaleString('vi-VN') + " ₫";
            if (shippingFeeInput) shippingFeeInput.value = fee;
            
            this.updateTotalPay();
        } catch (err) {
            console.error("Lỗi tính phí ship:", err);
            this.resetShippingFee();
        }
    }

    // Discount handling methods
    async handleDiscountChange(event) {
        const maPhieu = event.target.value;

        // Cập nhật hidden input với giá trị phiếu giảm giá
        const phieuGiamGiaHidden = document.getElementById('phieuGiamGiaHidden');
        if (phieuGiamGiaHidden) {
            phieuGiamGiaHidden.value = maPhieu;
        }

        if (!maPhieu) {
            this.currentDiscount = 0;
            this.updateDiscountDisplay(0);
            this.updateTotalPay();
            return;
        }

        try {
            const response = await fetch(`/gio-hang/phieu-giam-gia/tien-giam?maPhieu=${maPhieu}&tongTien=${this.tongTien}`);
            const tienGiam = await response.json();

            this.currentDiscount = tienGiam;
            this.updateDiscountDisplay(tienGiam);
            this.updateTotalPay();
        } catch (err) {
            console.error('Lỗi tính mã giảm giá:', err);
            this.currentDiscount = 0;
            this.updateDiscountDisplay(0);
            this.updateTotalPay();
        }
    }

    updateDiscountDisplay(tienGiam) {
        const tienGiamText = document.getElementById('tienGiamText');
        const tienGiamInput = document.getElementById('tienGiamInput');
        
        if (tienGiamText) tienGiamText.innerText = tienGiam.toLocaleString('vi-VN') + " ₫";
        if (tienGiamInput) tienGiamInput.value = tienGiam;
    }

    updateTotalPay() {
        const total = this.tongTien - this.currentDiscount + this.currentShippingFee;
        const tongThanhToanText = document.getElementById('tongThanhToanText');
        
        if (tongThanhToanText) {
            tongThanhToanText.innerText = total.toLocaleString('vi-VN') + " ₫";
        }
    }

    // Initialization methods
    initializeValidation() {
        this.validateForm();
    }

    initializeShippingFee() {
        this.tongTien = parseInt(document.querySelector('input[name="tongTien"]')?.value || 0);
        this.resetShippingFee();
    }

    initializeDiscount() {
        const phieuGiamGia = document.getElementById('phieuGiamGia');
        const phieuGiamGiaHidden = document.getElementById('phieuGiamGiaHidden');

        if (phieuGiamGia) {
            // Đồng bộ giá trị ban đầu vào hidden input
            if (phieuGiamGiaHidden && phieuGiamGia.value) {
                phieuGiamGiaHidden.value = phieuGiamGia.value;
            }

            // Trigger change event nếu đã có giá trị
            if (phieuGiamGia.value) {
                phieuGiamGia.dispatchEvent(new Event('change'));
            }
        }
    }
}

// Toast notification system
class ToastManager {
    static show(type, message) {
        const toastId = type === 'success' ? 'customSuccess' : 'customError';
        const toastEl = document.getElementById(toastId);
        const msgEl = document.getElementById(type === 'success' ? 'successMsg' : 'errorMsg');
        const progressBar = toastEl.querySelector('.progress-bar');

        if (!toastEl || !msgEl) return;

        msgEl.textContent = message;
        toastEl.style.display = 'block';

        // Restart animation
        progressBar.style.animation = 'none';
        void progressBar.offsetWidth; // force reflow
        progressBar.style.animation = null;

        // Auto close after 4s
        setTimeout(() => {
            toastEl.style.display = 'none';
        }, 4000);
    }

    static close(toastId) {
        const toastEl = document.getElementById(toastId);
        if (toastEl) {
            toastEl.style.display = 'none';
        }
    }
}

// Global functions for HTML onclick
window.closeToast = function(toastId) {
    ToastManager.close(toastId);
};

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Initialize checkout validator
    window.checkoutValidator = new CheckoutValidator();
    
    // Show toast messages if any (from server)
    const successMsg = document.querySelector('[data-success-message]')?.dataset.successMessage;
    const errorMsg = document.querySelector('[data-error-message]')?.dataset.errorMessage;
    
    if (successMsg && successMsg !== "null") {
        ToastManager.show('success', successMsg);
    }
    if (errorMsg && errorMsg !== "null") {
        ToastManager.show('error', errorMsg);
    }
}); 