/**
 * Product Add Form JavaScript
 * Handles form validation, file uploads, and product code generation
 */

class ProductAddForm {
    constructor() {
        this.form = document.getElementById('formSanPham');
        this.submitBtn = document.getElementById('submitBtn');
        this.fields = {};
        this.errors = {};
        this.validators = {};
        
        this.init();
    }

    init() {
        this.initializeFields();
        this.initializeValidators();
        this.setupEventListeners();
        this.setupFileUploads();
        this.validateForm(); // Initial validation
    }

    initializeFields() {
        // Map all form fields
        this.fields = {
            ma: document.getElementById('maSanPham'),
            ten: document.getElementById('tenSanPham'),
            mota: document.getElementById('motaSanPham'),
            trangThai: document.getElementById('trangThai'),
            chatLieuId: document.getElementById('chatLieuId'),
            xuatXuId: document.getElementById('xuatXuId'),
            danhMucId: document.getElementById('danhMucId'),
            kieuDangId: document.getElementById('kieuDangId'),
            thuongHieuId: document.getElementById('thuongHieuId'),
            loaiThuId: document.getElementById('loaiThuId'),
            anhChinh: document.getElementById('anhChinh'),
            anhPhu: document.getElementById('anhPhu')
        };

        // Map error containers
        this.errors = {
            ma: document.getElementById('maError'),
            ten: document.getElementById('tenError'),
            mota: document.getElementById('motaError'),
            trangThai: document.getElementById('trangThaiError'),
            chatLieuId: document.getElementById('chatLieuError'),
            xuatXuId: document.getElementById('xuatXuError'),
            danhMucId: document.getElementById('danhMucError'),
            kieuDangId: document.getElementById('kieuDangError'),
            thuongHieuId: document.getElementById('thuongHieuError'),
            loaiThuId: document.getElementById('loaiThuError'),
            anhChinh: document.getElementById('anhChinhError'),
            anhPhu: document.getElementById('anhPhuError')
        };
    }

    initializeValidators() {
        this.validators = {
            ma: (value) => this.validateProductCode(value),
            ten: (value) => this.validateProductName(value),
            mota: (value) => this.validateDescription(value),
            trangThai: (value) => this.validateRequired(value, 'trạng thái'),
            chatLieuId: (value) => this.validateRequired(value, 'chất liệu'),
            xuatXuId: (value) => this.validateRequired(value, 'xuất xứ'),
            danhMucId: (value) => this.validateRequired(value, 'danh mục'),
            kieuDangId: (value) => this.validateRequired(value, 'kiểu dáng'),
            thuongHieuId: (value) => this.validateRequired(value, 'thương hiệu'),
            loaiThuId: (value) => this.validateRequired(value, 'loại thú'),
            anhChinh: (value) => this.validateMainImage(value),
            anhPhu: (value) => this.validateSubImages(value)
        };
    }

    // Product Code Validation: SP + 6 digits, check for duplicates
    async validateProductCode(value) {
        const maField = this.fields && this.fields.ma ? this.fields.ma : null;
        if ((maField && (maField.disabled || maField.readOnly)) || this.isEditMode()) {
            return null;
        }
        if (!value || value.trim().length === 0) {
            return 'Mã sản phẩm không được để trống';
        }

        const trimmedValue = value.trim();
        
        // Check format: SP + 6 digits
        if (!/^SP\d{6}$/.test(trimmedValue)) {
            return 'Mã sản phẩm phải có định dạng SP + 6 chữ số (VD: SP000001)';
        }

        // Check for duplicates via API
        try {
            const isDuplicate = await this.checkProductCodeDuplicate(trimmedValue);
            if (isDuplicate) {
                return 'Mã sản phẩm đã tồn tại, vui lòng chọn mã khác';
            }
        } catch (error) {
            console.error('Error checking product code duplicate:', error);
            return 'Không thể kiểm tra mã sản phẩm, vui lòng thử lại';
        }

        return null;
    }

    // Check if product code already exists
    async checkProductCodeDuplicate(productCode) {
        try {
            const response = await fetch(`/admin/san-pham/check-ma?ma=${encodeURIComponent(productCode)}`, {
                method: 'GET',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            if (response.ok) {
                const data = await response.json();
                return data.exists; // Assuming API returns {exists: true/false}
            }
            return false;
        } catch (error) {
            console.error('Error checking product code:', error);
            return false;
        }
    }

    validateProductName(value) {
        if (!value || value.trim().length === 0) {
            return 'Tên sản phẩm không được để trống';
        }
        if (value.trim().length < 2) {
            return 'Tên sản phẩm phải có ít nhất 2 ký tự';
        }
        if (value.trim().length > 100) {
            return 'Tên sản phẩm không được quá 100 ký tự';
        }
        return null;
    }

    validateDescription(value) {
        if (value && value.trim().length > 1000) {
            return 'Mô tả không được quá 1000 ký tự';
        }
        return null;
    }

    validateRequired(value, fieldName) {
        if (!value || value === '') {
            return `Vui lòng chọn ${fieldName}`;
        }
        return null;
    }

    validateMainImage(value) {
        if (this.isEditMode()) {
            if (!value || value.files.length === 0) {
                return null;
            }
        }
        if (!value || value.files.length === 0) {
            return 'Vui lòng chọn ảnh chính';
        }
        return this.validateImageFile(value.files[0]);
    }

    validateSubImages(value) {
        if (value && value.files.length > 0) {
            const files = Array.from(value.files);
            const maxCount = 10;
            
            if (files.length > maxCount) {
                return `Tối đa ${maxCount} ảnh phụ`;
            }
            
            for (let i = 0; i < files.length; i++) {
                const fileError = this.validateImageFile(files[i]);
                if (fileError) {
                    return fileError;
                }
            }
        }
        return null;
    }

    validateImageFile(file) {
        const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
        const maxSize = 5 * 1024 * 1024; // 5MB
        
        if (!allowedTypes.includes(file.type)) {
            return 'Chỉ chấp nhận file JPG, PNG, WEBP';
        }
        if (file.size > maxSize) {
            return 'File không được quá 5MB';
        }
        return null;
    }

    setupEventListeners() {
        // Add validation listeners to all fields
        Object.keys(this.fields).forEach(fieldName => {
            const field = this.fields[fieldName];
            if (field) {
                // Real-time validation on input
                field.addEventListener('input', () => {
                    this.validateField(fieldName);
                });
                
                // Validation on blur
                field.addEventListener('blur', () => {
                    this.validateField(fieldName);
                });
                
                // For select elements, also validate on change
                if (field.tagName === 'SELECT') {
                    field.addEventListener('change', () => {
                        this.validateField(fieldName);
                    });
                }
            }
        });

        // Form submission validation
        this.form.addEventListener('submit', (e) => {
            if (!this.validateForm()) {
                e.preventDefault();
                alert('Vui lòng kiểm tra lại thông tin trước khi lưu!');
            }
        });
    }

    setupFileUploads() {
        this.setupFileUpload('anhChinh', 'anhChinhPreview', 'anhChinhName');
        this.setupFileUpload('anhPhu', 'anhPhuPreview', 'anhPhuList', true);
    }

    setupFileUpload(inputId, previewId, nameId, isMultiple = false) {
        const input = document.getElementById(inputId);
        const preview = document.getElementById(previewId);
        const nameElement = document.getElementById(nameId);

        if (!input || !preview || !nameElement) return;

        input.addEventListener('change', (e) => {
            const files = e.target.files;
            if (files.length > 0) {
                preview.classList.remove('tw-hidden');
                
                if (isMultiple) {
                    nameElement.innerHTML = '';
                    Array.from(files).forEach((file, index) => {
                        const fileItem = document.createElement('div');
                        fileItem.className = 'file-item';
                        fileItem.innerHTML = `
                            <span class="file-name">${file.name}</span>
                            <button type="button" class="file-remove" onclick="productAddForm.removeFileItem(this, ${index})">
                                <i class="fas fa-times"></i>
                            </button>
                        `;
                        nameElement.appendChild(fileItem);
                    });
                } else {
                    nameElement.textContent = files[0].name;
                }
            }
            this.validateField(inputId);
        });
    }

    removeFile(inputId) {
        const input = document.getElementById(inputId);
        const preview = document.getElementById(inputId + 'Preview');
        const nameElement = document.getElementById(inputId + 'Name');
        
        if (input) input.value = '';
        if (preview) preview.classList.add('tw-hidden');
        if (nameElement) nameElement.textContent = '';
        
        this.validateField(inputId);
    }

    removeFileItem(button, index) {
        const fileItem = button.closest('.file-item');
        if (fileItem) {
            fileItem.remove();
        }
        
        // If no more files, hide preview
        const preview = document.getElementById('anhPhuPreview');
        const fileList = document.getElementById('anhPhuList');
        if (fileList && fileList.children.length === 0 && preview) {
            preview.classList.add('tw-hidden');
        }
        
        this.validateField('anhPhu');
    }

    showError(fieldName, message) {
        const errorElement = this.errors[fieldName];
        const inputElement = this.fields[fieldName];
        
        if (errorElement && inputElement) {
            errorElement.textContent = message;
            errorElement.classList.add('show');
            inputElement.classList.add('error');
            inputElement.classList.remove('valid');
        }
    }

    hideError(fieldName) {
        const errorElement = this.errors[fieldName];
        const inputElement = this.fields[fieldName];
        
        if (errorElement && inputElement) {
            errorElement.classList.remove('show');
            inputElement.classList.remove('error');
            inputElement.classList.add('valid');
        }
    }

    async validateField(fieldName) {
        const field = this.fields[fieldName];
        const validator = this.validators[fieldName];
        
        if (!field || !validator) return true;
        
        const error = await validator(field.value);
        if (error) {
            this.showError(fieldName, error);
            return false;
        } else {
            this.hideError(fieldName);
            return true;
        }
    }

    async validateForm() {
        let isValid = true;
        
        for (const fieldName of Object.keys(this.validators)) {
            if (!(await this.validateField(fieldName))) {
                isValid = false;
            }
        }
        
        return isValid;
    }

    // Generate random product code
    async generateProductCode() {
        const maInput = document.getElementById('maSanPham');
        const maMessage = document.getElementById('maMessage');
        const button = event.target.closest('button');
        
        if (!maInput || !maMessage || !button) {
            console.error('Không tìm thấy các element cần thiết');
            return;
        }
        
        // Show loading state
        button.disabled = true;
        button.classList.add('btn-loading');
        button.innerHTML = '<i class="fas fa-spinner fa-spin spinner tw-mr-2"></i>Đang tạo...';
        maMessage.innerHTML = '';
        
        try {
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
            
            const headers = {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            };
            
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }
            
            const response = await fetch('/admin/san-pham/tao-ma-ngau-nhien', {
                method: 'POST',
                headers: headers,
                credentials: 'same-origin'
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            
            if (data.success) {
                maInput.value = data.ma;
                maMessage.innerHTML = `<span class="message-success"><i class="fas fa-check tw-mr-1"></i>${data.message}</span>`;
                this.validateField('ma');
            } else {
                maMessage.innerHTML = `<span class="message-error"><i class="fas fa-exclamation-triangle tw-mr-1"></i>${data.message}</span>`;
            }
        } catch (error) {
            console.error('Error:', error);
            maMessage.innerHTML = '<span class="message-error"><i class="fas fa-exclamation-triangle tw-mr-1"></i>Có lỗi xảy ra khi tạo mã</span>';
        } finally {
            // Reset button state
            button.disabled = false;
            button.classList.remove('btn-loading');
            button.innerHTML = '<i class="fas fa-dice tw-mr-2"></i>Tạo mã';
        }
    }

    isEditMode() {
        const idInput = this.form ? this.form.querySelector('input[name="id"]') : null;
        return !!(idInput && idInput.value && idInput.value.trim() !== '');
    }
}

// Initialize when DOM is loaded
let productAddForm;
document.addEventListener('DOMContentLoaded', function() {
    productAddForm = new ProductAddForm();
    
    // Make generateProductCode globally accessible
    window.taoMaNgauNhien = function() {
        productAddForm.generateProductCode();
    };
    
    // Make removeFile globally accessible
    window.removeFile = function(inputId) {
        productAddForm.removeFile(inputId);
    };
    
    // Make removeFileItem globally accessible
    window.removeFileItem = function(button, index) {
        productAddForm.removeFileItem(button, index);
    };
}); 