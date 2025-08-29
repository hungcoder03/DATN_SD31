/* ===== CART PAGE JAVASCRIPT ===== */

console.log('=== CART PAGE JAVASCRIPT LOADED ===');

// Cart management class
class CartManager {
    constructor() {
        this.initializeEventListeners();
        this.updateUI();
        console.log('CartManager initialized');
    }

    initializeEventListeners() {
        // Select all checkbox
        const chonTatCa = document.getElementById("chonTatCa");
        if (chonTatCa) {
            chonTatCa.addEventListener("click", (e) => {
                e.preventDefault();
                const currentState = chonTatCa.getAttribute('data-state');
                const newState = currentState === 'checked' ? 'unchecked' : 'checked';
                this.toggleAllItems(newState === 'checked');
            });
        }

        // Individual item checkboxes
        document.querySelectorAll(".item-checkbox").forEach(cb => {
            cb.addEventListener("click", (e) => {
                e.preventDefault();
                const currentState = cb.getAttribute('data-state');
                const newState = currentState === 'checked' ? 'unchecked' : 'checked';
                cb.setAttribute('data-state', newState);
                cb.setAttribute('aria-checked', newState === 'checked');
                this.updateSelectAllState();
                this.updateUI();
            });
        });

        // Quantity input changes
        document.querySelectorAll(".quantity-input-field").forEach(input => {
            input.addEventListener("change", (e) => {
                this.handleQuantityInputChange(e.target);
            });
            
            // Lưu giá trị ban đầu
            input.setAttribute('data-original-value', input.value);
        });

        // Checkout button
        const checkoutBtn = document.getElementById("submitThanhToan");
        if (checkoutBtn) {
            checkoutBtn.addEventListener("click", (e) => {
                e.preventDefault();
                this.handleCheckout();
            });
        }
    }

    toggleAllItems(checked) {
        const chonTatCa = document.getElementById("chonTatCa");
        const newState = checked ? 'checked' : 'unchecked';
        
        if (chonTatCa) {
            chonTatCa.setAttribute('data-state', newState);
            chonTatCa.setAttribute('aria-checked', checked);
        }

        document.querySelectorAll(".item-checkbox").forEach(cb => {
            cb.setAttribute('data-state', newState);
            cb.setAttribute('aria-checked', checked);
        });
        
        this.updateUI();
    }

    updateSelectAllState() {
        const checkboxes = document.querySelectorAll(".item-checkbox");
        const chonTatCa = document.getElementById("chonTatCa");
        
        if (chonTatCa) {
            const checkedCount = [...checkboxes].filter(cb => cb.getAttribute('data-state') === 'checked').length;
            const totalCount = checkboxes.length;
            
            if (checkedCount === 0) {
                chonTatCa.setAttribute('data-state', 'unchecked');
                chonTatCa.setAttribute('aria-checked', false);
            } else if (checkedCount === totalCount) {
                chonTatCa.setAttribute('data-state', 'checked');
                chonTatCa.setAttribute('aria-checked', true);
            } else {
                chonTatCa.setAttribute('data-state', 'indeterminate');
                chonTatCa.setAttribute('aria-checked', 'mixed');
            }
        }
    }

    updateUI() {
        this.updateTotal();
        this.updateSelectedCount();
        this.updateCheckoutButton();
    }

    updateTotal() {
        let total = 0;
        document.querySelectorAll(".item-checkbox").forEach(cb => {
            if (cb.getAttribute('data-state') === 'checked') {
                const card = cb.closest('.flex.max-h-\\[116px\\]');
                if (card) {
                    const price = parseInt(card.getAttribute("data-price")) || 0;
                    const quantity = parseInt(card.querySelector(".quantity-input-field").value) || 1;
                    total += price * quantity;
                }
            }
        });
        
        const totalElement = document.getElementById("totalTien");
        const finalTotalElement = document.getElementById("finalTotal");
        
        if (totalElement) {
            totalElement.textContent = this.formatMoney(total);
        }
        if (finalTotalElement) {
            finalTotalElement.textContent = this.formatMoney(total);
        }
    }

    updateSelectedCount() {
        const checked = document.querySelectorAll(".item-checkbox[data-state='checked']");
        const countElement = document.getElementById("selectedCount");
        if (countElement) {
            countElement.textContent = checked.length;
        }
    }

    updateCheckoutButton() {
        const checked = document.querySelectorAll(".item-checkbox[data-state='checked']");
        const checkoutBtn = document.getElementById("submitThanhToan");
        if (checkoutBtn) {
            checkoutBtn.disabled = checked.length === 0;
        }
    }

    formatMoney(number) {
        return new Intl.NumberFormat('vi-VN').format(number) + "₫";
    }

    handleQuantityInputChange(input) {
        const itemId = input.getAttribute("data-item-id");
        const newQuantity = parseInt(input.value);
        
        if (newQuantity < 1) {
            input.value = 1;
            return;
        }

        // Cập nhật giá trị ban đầu
        input.setAttribute('data-original-value', input.value);

        // Gọi API để cập nhật database
        this.updateQuantityInDatabase(itemId, newQuantity);
    }

    updateQuantityInDatabase(itemId, newQuantity) {
        // Hiển thị loading state
        const input = document.querySelector(`input[data-item-id="${itemId}"]`);
        if (input) {
            input.disabled = true;
            input.style.opacity = '0.6';
        }

        // Gọi API cập nhật
        fetch(`/gio-hang/cap-nhat/${itemId}?action=update&newSoluong=${newQuantity}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => {
            if (response.ok) {
                // Cập nhật thành công - refresh trang để lấy dữ liệu mới
                window.location.reload();
            } else {
                // Có lỗi - khôi phục giá trị cũ
                if (input) {
                    input.value = parseInt(input.getAttribute('data-original-value')) || 1;
                    input.disabled = false;
                    input.style.opacity = '1';
                }
                this.showToast('error', 'Không thể cập nhật số lượng. Vui lòng thử lại!');
            }
        })
        .catch(error => {
            console.error('Error updating quantity:', error);
            // Khôi phục giá trị cũ
            if (input) {
                input.value = parseInt(input.getAttribute('data-original-value')) || 1;
                input.disabled = false;
                input.style.opacity = '1';
            }
            this.showToast('error', 'Có lỗi xảy ra khi cập nhật số lượng!');
        });
    }

    handleCheckout() {
        const checked = document.querySelectorAll(".item-checkbox[data-state='checked']");
        if (checked.length === 0) {
            this.showToast("error", "Vui lòng chọn ít nhất một sản phẩm để thanh toán!");
            return;
        }

        const params = new URLSearchParams();
        checked.forEach(cb => {
            const itemId = cb.getAttribute('th:value') || cb.id.replace('item-', '');
            params.append("selectedId", itemId);
        });
        const url = "/gio-hang/thanh-toan?" + params.toString();
        window.location.href = url;
    }

    showToast(type, message) {
        const toastId = type === 'success' ? 'customSuccess' : 'customError';
        const toastEl = document.getElementById(toastId);
        const msgEl = document.getElementById(type === 'success' ? 'successMsg' : 'errorMsg');
        
        if (toastEl && msgEl) {
            msgEl.textContent = message;
            toastEl.style.display = 'block';
            
            setTimeout(() => {
                toastEl.style.display = 'none';
            }, 4000);
        }
    }
}

// Toast notification functions
function showCustomToast(type, message) {
    const toastId = type === 'success' ? 'customSuccess' : 'customError';
    const toastEl = document.getElementById(toastId);
    const msgEl = document.getElementById(type === 'success' ? 'successMsg' : 'errorMsg');
    const progressBar = toastEl.querySelector('.progress-bar');

    if (toastEl && msgEl) {
        msgEl.textContent = message;
        toastEl.style.display = 'block';

        // Reset animation
        if (progressBar) {
            progressBar.style.animation = 'none';
            void progressBar.offsetWidth;
            progressBar.style.animation = null;
        }

        setTimeout(() => {
            toastEl.style.display = 'none';
        }, 4000);
    }
}

function closeToast(toastId) {
    const toastEl = document.getElementById(toastId);
    if (toastEl) {
        toastEl.style.display = 'none';
    }
}

// Global functions for onclick handlers
function updateQuantity(itemId, change) {
    const input = document.querySelector(`input[data-item-id="${itemId}"]`);
    if (input) {
        const currentValue = parseInt(input.value) || 1;
        const newValue = Math.max(1, currentValue + change);
        
        // Cập nhật UI trước
        input.value = newValue;
        
        // Sử dụng CartManager để cập nhật database
        if (window.cartManager) {
            window.cartManager.updateQuantityInDatabase(itemId, newValue);
        }
    }
}

function removeItem(itemId) {
    if (confirm('Bạn có chắc muốn xóa sản phẩm này?')) {
        window.location.href = `/gio-hang/xoa/${itemId}`;
    }
}

// Initialize cart manager when DOM is loaded
document.addEventListener("DOMContentLoaded", function() {
    console.log('=== CART PAGE DOM LOADED ===');
    
    // Initialize cart manager
    window.cartManager = new CartManager();
    
    // Show toast messages if any
    const successMsg = window.successMessage || null;
    const errorMsg = window.errorMessage || null;
    
    console.log('Success message:', successMsg);
    console.log('Error message:', errorMsg);
    
    if (successMsg && successMsg !== null && successMsg !== "null") {
        showCustomToast('success', successMsg);
    }
    if (errorMsg && errorMsg !== null && errorMsg !== "null") {
        showCustomToast('error', errorMsg);
    }
    
    console.log('=== CART PAGE INITIALIZATION COMPLETE ===');
}); 