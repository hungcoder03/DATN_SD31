/**
 * Product Detail Page JavaScript
 * Handles product variant selection, price updates, validation, and cart functionality
 */

class ProductDetailManager {
    constructor() {
        console.log('=== ProductDetailManager Constructor ===');
        this.productData = window.productData || {};
        this.selectedSize = null;
        this.selectedColor = null;
        this.selectedQuantity = 1;
        this.currentVariant = null;
        
        console.log('Product data:', this.productData);
        console.log('Calling init()...');
        this.init();
        console.log('Init completed');
    }

    init() {
        this.bindEvents();
        this.updateAddToCartButton();
        this.setupLightbox();
        
        // Initialize fixed quantity input
        const fixedQuantityInput = document.getElementById('fixedQuantity');
        if (fixedQuantityInput) {
            fixedQuantityInput.value = this.selectedQuantity;
            console.log('Fixed quantity input initialized with:', this.selectedQuantity);
        }
        
        // Initialize selected color display
        this.initializeSelectedColorDisplay();
        
        // Initialize variant availability
        this.updateVariantAvailability();
    }

    bindEvents() {
        console.log('=== Binding Events ===');
        
        // Size selection
        const sizeRadios = document.querySelectorAll('input[name="selectedSize"]');
        console.log('Size radios found:', sizeRadios.length);
        sizeRadios.forEach(radio => {
            radio.addEventListener('change', (e) => this.handleSizeChange(e));
        });

        // Color selection
        const colorRadios = document.querySelectorAll('input[name="selectedColor"]');
        console.log('Color radios found:', colorRadios.length);
        colorRadios.forEach(radio => {
            radio.addEventListener('change', (e) => this.handleColorChange(e));
        });

        // Thumbnail clicks
        const thumbnails = document.querySelectorAll('.thumbnail-item');
        console.log('Thumbnails found:', thumbnails.length);
        thumbnails.forEach((thumb, index) => {
            thumb.addEventListener('click', (e) => this.handleThumbnailClick(e));
            
            // Add keyboard support
            thumb.setAttribute('tabindex', '0');
            thumb.setAttribute('role', 'button');
            thumb.setAttribute('aria-label', `Ảnh sản phẩm ${index + 1}`);
            
            thumb.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    this.handleThumbnailClick(e);
                } else if (e.key === 'ArrowLeft') {
                    e.preventDefault();
                    this.navigateThumbnail('prev');
                } else if (e.key === 'ArrowRight') {
                    e.preventDefault();
                    this.navigateThumbnail('next');
                }
            });
        });

        // Fixed bar events (main logic now)
        this.bindFixedBarEvents();
        
        // Scroll events for fixed bar
        this.bindScrollEvents();
    }

    handleSizeChange(event) {
        this.selectedSize = parseInt(event.target.value);
        this.updateVariantSelection();
    }

    handleColorChange(event) {
        this.selectedColor = parseInt(event.target.value);
        
        // Update selected color name display
        const selectedColorName = event.target.getAttribute('data-color');
        const colorNameElement = document.getElementById('selectedColorName');
        if (colorNameElement && selectedColorName) {
            colorNameElement.textContent = selectedColorName;
        }
        
        this.updateVariantSelection();
    }
    
    initializeSelectedColorDisplay() {
        // Find the first checked color radio and update display
        const checkedColorRadio = document.querySelector('input[name="selectedColor"]:checked');
        if (checkedColorRadio) {
            const selectedColorName = checkedColorRadio.getAttribute('data-color');
            const colorNameElement = document.getElementById('selectedColorName');
            if (colorNameElement && selectedColorName) {
                colorNameElement.textContent = selectedColorName;
            }
        }
    }

    handleQuantityChange(event) {
        const value = parseInt(event.target.value);
        if (value > 0) {
            this.selectedQuantity = value;
            this.updateStockInfo();
        }
    }

    validateQuantity(event) {
        const value = parseInt(event.target.value);
        const maxStock = this.currentVariant ? this.currentVariant.soLuongTon : 1;
        
        if (value < 1) {
            event.target.value = 1;
            this.selectedQuantity = 1;
        } else if (value > maxStock) {
            event.target.value = maxStock;
            this.selectedQuantity = maxStock;
            this.showValidationMessage(`Số lượng tối đa có thể chọn là ${maxStock}`, 'error');
        } else {
            this.hideValidationMessage();
        }
    }

    updateVariantSelection() {
        if (!this.selectedSize || !this.selectedColor) {
            this.currentVariant = null;
            this.updateAddToCartButton();
            this.updateStockInfo();
            this.updateVariantAvailability();
            return;
        }

        // Find matching variant
        this.currentVariant = this.productData.variants.find(variant => 
            variant.size.id === this.selectedSize && 
            variant.mauSac.id === this.selectedColor
        );

        if (this.currentVariant) {
            this.updatePriceDisplay();
            this.updateStockInfo();
            this.updateAddToCartButton();
            this.hideValidationMessage();
            this.updateVariantAvailability();
            console.log('Variant selected:', this.currentVariant);
        } else {
            this.currentVariant = null;
            this.updateAddToCartButton();
            this.updateStockInfo();
            this.updateVariantAvailability();
            console.log('No matching variant found');
        }
    }
    
    updateVariantAvailability() {
        // Get all available variants
        const availableVariants = this.productData.variants.filter(variant => 
            variant.soLuongTon > 0
        );
        
        // If a size is selected, only show colors that have stock for that size
        let availableSizes = [...new Set(availableVariants.map(v => v.size.id))];
        let availableColors = [...new Set(availableVariants.map(v => v.mauSac.id))];
        
        if (this.selectedSize) {
            // Only show colors that have stock for the selected size
            const colorsForSelectedSize = availableVariants
                .filter(v => v.size.id === this.selectedSize)
                .map(v => v.mauSac.id);
            availableColors = colorsForSelectedSize;
        }
        
        if (this.selectedColor) {
            // Only show sizes that have stock for the selected color
            const sizesForSelectedColor = availableVariants
                .filter(v => v.mauSac.id === this.selectedColor)
                .map(v => v.size.id);
            availableSizes = sizesForSelectedColor;
        }
        
        // Update size availability
        const sizeRadios = document.querySelectorAll('input[name="selectedSize"]');
        sizeRadios.forEach(radio => {
            const sizeId = parseInt(radio.value);
            const isAvailable = availableSizes.includes(sizeId);
            const label = document.querySelector(`label[for="${radio.id}"]`);
            
            if (isAvailable) {
                radio.disabled = false;
                if (label) {
                    label.classList.remove('disabled');
                    label.style.opacity = '1';
                    label.style.cursor = 'pointer';
                }
            } else {
                radio.disabled = true;
                if (label) {
                    label.classList.add('disabled');
                    label.style.opacity = '0.5';
                    label.style.cursor = 'not-allowed';
                }
            }
        });
        
        // Update color availability
        const colorRadios = document.querySelectorAll('input[name="selectedColor"]');
        colorRadios.forEach(radio => {
            const colorId = parseInt(radio.value);
            const isAvailable = availableColors.includes(colorId);
            const label = document.querySelector(`label[for="${radio.id}"]`);
            
            if (isAvailable) {
                radio.disabled = false;
                if (label) {
                    label.classList.remove('disabled');
                    label.style.opacity = '1';
                    label.style.cursor = 'pointer';
                }
            } else {
                radio.disabled = true;
                if (label) {
                    label.classList.add('disabled');
                    label.style.opacity = '0.5';
                    label.style.cursor = 'not-allowed';
                }
            }
        });
        
        // If current selection is not available, clear it
        if (this.selectedSize && !availableSizes.includes(this.selectedSize)) {
            this.selectedSize = null;
            const checkedSizeRadio = document.querySelector('input[name="selectedSize"]:checked');
            if (checkedSizeRadio) {
                checkedSizeRadio.checked = false;
            }
        }
        
        if (this.selectedColor && !availableColors.includes(this.selectedColor)) {
            this.selectedColor = null;
            const checkedColorRadio = document.querySelector('input[name="selectedColor"]:checked');
            if (checkedColorRadio) {
                checkedColorRadio.checked = false;
            }
        }
    }

    updatePriceDisplay() {
        if (!this.currentVariant) return;

        const currentPriceElement = document.getElementById('currentPrice');
        const originalPriceElement = document.getElementById('originalPrice');
        const discountPercentElement = document.getElementById('discountPercent');
        const discountBadge = document.querySelector('.discount-badge');
        const originalPriceContainer = document.querySelector('.original-price');

        if (currentPriceElement) {
            currentPriceElement.textContent = this.formatPrice(this.currentVariant.giaBan);
        }

        // Update original price if different from current price
        if (originalPriceElement && this.currentVariant.giaGoc) {
            originalPriceElement.textContent = this.formatPrice(this.currentVariant.giaGoc);
            
            // Show/hide original price based on discount
            if (this.currentVariant.giaGoc > this.currentVariant.giaBan) {
                if (originalPriceContainer) {
                    originalPriceContainer.style.display = 'block';
                }
            } else {
                if (originalPriceContainer) {
                    originalPriceContainer.style.display = 'none';
                }
            }
        }

        // Update discount if applicable
        if (this.currentVariant.giaGoc && this.currentVariant.giaGoc > this.currentVariant.giaBan) {
            const discountPercent = Math.round(
                ((this.currentVariant.giaGoc - this.currentVariant.giaBan) / this.currentVariant.giaGoc) * 100
            );
            
            if (discountPercentElement) {
                discountPercentElement.innerHTML = `-${discountPercent}%`;
            }
            
            // Show discount badge
            if (discountBadge) {
                discountBadge.style.display = 'inline-flex';
            }
        } else {
            // Hide discount badge if no discount
            if (discountBadge) {
                discountBadge.style.display = 'none';
            }
        }

        // Add price change animation
        this.animatePriceChange();
    }

    animatePriceChange() {
        const currentPriceElement = document.getElementById('currentPrice');
        if (currentPriceElement) {
            // Remove existing animation class
            currentPriceElement.classList.remove('animate');
            
            // Trigger reflow
            currentPriceElement.offsetHeight;
            
            // Add animation class
            currentPriceElement.classList.add('animate');
            
            // Remove animation class after animation completes
            setTimeout(() => {
                currentPriceElement.classList.remove('animate');
            }, 500);
        }
    }

    updateStockInfo() {
        const stockAmountElement = document.getElementById('stockAmount');
        if (!stockAmountElement) return;

        if (this.currentVariant) {
            const stock = this.currentVariant.soLuongTon;
            stockAmountElement.textContent = stock;
            
            // Update quantity input max value
            const quantityInput = document.getElementById('fixedQuantity');
            if (quantityInput) {
                quantityInput.max = stock;
                if (this.selectedQuantity > stock) {
                    this.selectedQuantity = stock;
                    quantityInput.value = stock;
                }
            }

            // Update stock color based on availability
            if (stock === 0) {
                stockAmountElement.style.color = '#dc3545';
                stockAmountElement.textContent = 'Hết hàng';
            } else if (stock <= 5) {
                stockAmountElement.style.color = '#ffc107';
            } else {
                stockAmountElement.style.color = '#28a745';
            }
        } else {
            stockAmountElement.textContent = '--';
            stockAmountElement.style.color = '#666';
        }
    }

    updateAddToCartButton() {
        const addToCartBtn = document.getElementById('fixedAddToCartBtn');
        if (!addToCartBtn) return;

        if (this.currentVariant && this.currentVariant.soLuongTon > 0) {
            addToCartBtn.disabled = false;
            addToCartBtn.textContent = 'ADD TO CARD';
        } else {
            addToCartBtn.disabled = true;
            if (this.currentVariant && this.currentVariant.soLuongTon === 0) {
                addToCartBtn.textContent = 'HẾT HÀNG';
            } else {
                addToCartBtn.textContent = 'CHỌN SIZE & MÀU';
            }
        }
    }

    handleAddToCart(event) {
        event.preventDefault();

        if (!this.validateSelection()) {
            return false;
        }

        // Update hidden inputs for fixed form
        const fixedChiTietIdInput = document.getElementById('fixedSelectedChiTietId');
        const fixedQuantityInput = document.getElementById('fixedSelectedQuantity');
        
        if (fixedChiTietIdInput && fixedQuantityInput) {
            fixedChiTietIdInput.value = this.currentVariant.id;
            fixedQuantityInput.value = this.selectedQuantity;
        }

        // Show loading state
        this.showLoadingState();

        // Submit form (server-side rendering)
        const form = event.target.closest('form');
        if (form) {
            form.submit();
        }

        return true;
    }

    showLoadingState() {
        const fixedAddToCartBtn = document.getElementById('fixedAddToCartBtn');
        
        if (fixedAddToCartBtn) {
            fixedAddToCartBtn.disabled = true;
            fixedAddToCartBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang thêm...';
        }
    }

    validateSelection() {
        if (!this.selectedSize) {
            this.showValidationMessage('Vui lòng chọn kích thước', 'error');
            return false;
        }

        if (!this.selectedColor) {
            this.showValidationMessage('Vui lòng chọn màu sắc', 'error');
            return false;
        }

        if (!this.currentVariant) {
            this.showValidationMessage('Không tìm thấy biến thể sản phẩm', 'error');
            return false;
        }

        if (this.currentVariant.soLuongTon === 0) {
            this.showValidationMessage('Sản phẩm đã hết hàng', 'error');
            return false;
        }

        if (this.selectedQuantity < 1 || this.selectedQuantity > this.currentVariant.soLuongTon) {
            this.showValidationMessage('Số lượng không hợp lệ', 'error');
            return false;
        }

        return true;
    }

    showValidationMessage(message, type = 'error') {
        const validationElement = document.getElementById('validationMessage');
        if (!validationElement) return;

        validationElement.textContent = message;
        validationElement.className = `validation-message ${type}`;
        validationElement.style.display = 'block';

        // Auto-hide after 5 seconds
        setTimeout(() => {
            this.hideValidationMessage();
        }, 5000);
    }

    hideValidationMessage() {
        const validationElement = document.getElementById('validationMessage');
        if (validationElement) {
            validationElement.style.display = 'none';
        }
    }

    // showToast method removed - using miniCart.showToast instead

    handleThumbnailClick(event) {
        console.log('=== Thumbnail Clicked ===');
        const thumbnail = event.currentTarget;
        const imageUrl = thumbnail.dataset.image;
        const lightboxUrl = thumbnail.dataset.lightboxUrl;
        
        console.log('Thumbnail:', thumbnail);
        console.log('Image URL:', imageUrl);
        console.log('Lightbox URL:', lightboxUrl);
        
        if (!imageUrl) {
            console.log('No image URL found');
            return;
        }

        // Update main image with loading state
        const mainImage = document.getElementById('mainImage');
        const mainImageLink = document.querySelector('.main-image-link');
        
        if (mainImage) {
            // Add loading class to thumbnail
            thumbnail.classList.add('loading');
            
            // Smooth transition effect
            mainImage.style.opacity = '0.5';
            
            // Create new image to preload
            const newImage = new Image();
            newImage.onload = () => {
                setTimeout(() => {
                    mainImage.src = imageUrl;
                    mainImage.style.opacity = '1';
                    thumbnail.classList.remove('loading');
                    console.log('Main image updated to:', imageUrl);
                }, 150);
            };
            
            newImage.onerror = () => {
                console.error('Failed to load image:', imageUrl);
                mainImage.style.opacity = '1';
                thumbnail.classList.remove('loading');
                this.showValidationMessage('Không thể tải ảnh', 'error');
            };
            
            newImage.src = imageUrl;
        }
        
        // Update lightbox link
        if (mainImageLink && lightboxUrl) {
            mainImageLink.href = lightboxUrl;
        }

        // Update active thumbnail with animation
        document.querySelectorAll('.thumbnail-item').forEach(item => {
            item.classList.remove('active');
        });
        thumbnail.classList.add('active');
        
        console.log('Thumbnail change completed');
    }

    navigateThumbnail(direction) {
        const thumbnails = document.querySelectorAll('.thumbnail-item');
        const activeThumbnail = document.querySelector('.thumbnail-item.active');
        
        if (!activeThumbnail || thumbnails.length <= 1) return;
        
        let currentIndex = Array.from(thumbnails).indexOf(activeThumbnail);
        let nextIndex;
        
        if (direction === 'next') {
            nextIndex = (currentIndex + 1) % thumbnails.length;
        } else {
            nextIndex = (currentIndex - 1 + thumbnails.length) % thumbnails.length;
        }
        
        const nextThumbnail = thumbnails[nextIndex];
        if (nextThumbnail) {
            // Trigger click event
            const clickEvent = new MouseEvent('click', {
                bubbles: true,
                cancelable: true,
                view: window
            });
            nextThumbnail.dispatchEvent(clickEvent);
            nextThumbnail.focus();
        }
    }

    setupLightbox() {
        // Lightbox is already included via CDN
        // This method can be used for additional lightbox configuration if needed
        if (typeof lightbox !== 'undefined') {
            lightbox.option({
                'resizeDuration': 200,
                'wrapAround': true,
                'albumLabel': 'Ảnh %1 / %2'
            });
        }
    }

    formatPrice(price) {
        if (!price) return '₫0';
        return '₫' + price.toLocaleString('vi-VN');
    }

    bindFixedBarEvents() {
        console.log('=== Binding Fixed Bar Events ===');
        
        // Fixed bar quantity controls
        const fixedDecreaseBtn = document.getElementById('fixedDecreaseBtn');
        const fixedIncreaseBtn = document.getElementById('fixedIncreaseBtn');
        const fixedQuantityInput = document.getElementById('fixedQuantity');
        const fixedAddToCartForm = document.getElementById('fixedAddToCartForm');

        console.log('Fixed decrease button found:', !!fixedDecreaseBtn);
        console.log('Fixed increase button found:', !!fixedIncreaseBtn);
        console.log('Fixed quantity input found:', !!fixedQuantityInput);
        console.log('Fixed add to cart form found:', !!fixedAddToCartForm);

        if (fixedDecreaseBtn) {
            fixedDecreaseBtn.addEventListener('click', () => {
                console.log('Fixed decrease button clicked');
                this.decreaseQuantity();
            });
        }

        if (fixedIncreaseBtn) {
            fixedIncreaseBtn.addEventListener('click', () => {
                console.log('Fixed increase button clicked');
                this.increaseQuantity();
            });
        }

        if (fixedQuantityInput) {
            fixedQuantityInput.addEventListener('input', (e) => {
                console.log('Fixed quantity input changed:', e.target.value);
                this.handleQuantityChange(e);
            });
            
            fixedQuantityInput.addEventListener('change', (e) => {
                console.log('Fixed quantity input validated');
                this.validateQuantity(e);
            });
        }

        if (fixedAddToCartForm) {
            fixedAddToCartForm.addEventListener('submit', (e) => {
                console.log('Fixed add to cart form submitted');
                e.preventDefault();
                this.handleAddToCart(e);
            });
        }
    }

    bindScrollEvents() {
        const fixedBar = document.getElementById('fixedCartBar');
        const mainImageContainer = document.querySelector('.main-image-container');
        
        if (!fixedBar || !mainImageContainer) return;

        let lastScrollTop = 0;
        const scrollThreshold = 50; // Khoảng cách nhỏ để ẩn thanh
        let isHidden = false;

        const handleScroll = () => {
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            const mainImageBottom = mainImageContainer.offsetTop + mainImageContainer.offsetHeight;

            // Ẩn thanh khi scroll xuống qua ảnh chính
            if (scrollTop > mainImageBottom - scrollThreshold) {
                if (!isHidden) {
                    fixedBar.classList.add('hide');
                    isHidden = true;
                }
            } else {
                // Hiển thị lại thanh khi scroll lên trên ảnh chính
                if (isHidden) {
                    fixedBar.classList.remove('hide');
                    isHidden = false;
                }
            }

            // Luôn hiển thị thanh khi ở đầu trang
            if (scrollTop < 100) {
                fixedBar.classList.remove('hide');
                isHidden = false;
            }

            lastScrollTop = scrollTop;
        };

        // Throttled scroll event
        let ticking = false;
        window.addEventListener('scroll', () => {
            if (!ticking) {
                requestAnimationFrame(() => {
                    handleScroll();
                    ticking = false;
                });
                ticking = true;
            }
        });

        // Trigger initial check
        handleScroll();
    }

    // Các method này không cần thiết nữa vì chỉ sử dụng fixed bar
    // syncFixedBarQuantity() và syncMainQuantity() đã được xóa
    // updateFixedBarButton() đã được merge vào updateAddToCartButton()

    increaseQuantity() {
        const quantityInput = document.getElementById('fixedQuantity');
        if (quantityInput) {
            const currentValue = parseInt(quantityInput.value) || 1;
            const maxValue = parseInt(quantityInput.max) || 99;
            if (currentValue < maxValue) {
                quantityInput.value = currentValue + 1;
                this.selectedQuantity = currentValue + 1;
                quantityInput.dispatchEvent(new Event('change'));
                console.log('Quantity increased to:', this.selectedQuantity);
            }
        }
    }

    decreaseQuantity() {
        const quantityInput = document.getElementById('fixedQuantity');
        if (quantityInput) {
            const currentValue = parseInt(quantityInput.value) || 1;
            if (currentValue > 1) {
                quantityInput.value = currentValue - 1;
                this.selectedQuantity = currentValue - 1;
                quantityInput.dispatchEvent(new Event('change'));
                console.log('Quantity decreased to:', this.selectedQuantity);
            }
        }
    }
}

// Quantity control functions (global scope for onclick handlers)
function increaseQuantity() {
    const quantityInput = document.getElementById('quantity');
    if (quantityInput) {
        const currentValue = parseInt(quantityInput.value) || 1;
        const maxValue = parseInt(quantityInput.max) || 99;
        if (currentValue < maxValue) {
            quantityInput.value = currentValue + 1;
            quantityInput.dispatchEvent(new Event('change'));
        }
    }
}

function decreaseQuantity() {
    const quantityInput = document.getElementById('quantity');
    if (quantityInput) {
        const currentValue = parseInt(quantityInput.value) || 1;
        if (currentValue > 1) {
            quantityInput.value = currentValue - 1;
            quantityInput.dispatchEvent(new Event('change'));
        }
    }
}

function changeMainImage(thumbnail) {
    const imageUrl = thumbnail.dataset.image;
    if (!imageUrl) return;

    // Update main image
    const mainImage = document.getElementById('mainImage');
    if (mainImage) {
        mainImage.src = imageUrl;
    }

    // Update active thumbnail
    document.querySelectorAll('.thumbnail-item').forEach(item => {
        item.classList.remove('active');
    });
    thumbnail.classList.add('active');
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('=== DOM LOADED ===');
    
    // Check if fixed bar elements exist
    console.log('Checking fixed bar elements...');
    console.log('Fixed decrease button:', document.getElementById('fixedDecreaseBtn'));
    console.log('Fixed increase button:', document.getElementById('fixedIncreaseBtn'));
    console.log('Fixed quantity input:', document.getElementById('fixedQuantity'));
    console.log('Fixed add to cart form:', document.getElementById('fixedAddToCartForm'));
    
    // Initialize product detail manager
    console.log('Creating ProductDetailManager...');
    window.productDetailManager = new ProductDetailManager();
    console.log('ProductDetailManager created:', window.productDetailManager);
    
    // Add smooth scrolling for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const href = this.getAttribute('href');
            
            // Kiểm tra href hợp lệ (không chỉ có dấu #)
            if (href && href.length > 1) {
                const target = document.querySelector(href);
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });

    // Add loading state to fixed add to cart button
    const fixedAddToCartForm = document.getElementById('fixedAddToCartForm');
    if (fixedAddToCartForm) {
        fixedAddToCartForm.addEventListener('submit', function() {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang xử lý...';
            }
        });
    }
});

// Utility functions
function formatCurrency(amount) {
    return Utils.formatCurrency(amount);
}

// THAY BẰNG:
function showNotification(message, type) {
    // Kiểm tra xem có thư viện notification nào đã load chưa
    if (typeof toastr !== 'undefined') {
        // Nếu có toastr
        if (type === 'success') {
            toastr.success(message);
        } else {
            toastr.error(message);
        }
    } else if (typeof Swal !== 'undefined') {
        // Nếu có SweetAlert2
        Swal.fire({
            title: type === 'success' ? 'Thành công!' : 'Lỗi!',
            text: message,
            icon: type === 'success' ? 'success' : 'error',
            timer: 3000,
            showConfirmButton: false
        });
    } else {
        // Fallback: Tạo notification đơn giản bằng vanilla JS
        createSimpleNotification(message, type);
    }
}

function createSimpleNotification(message, type) {
    // Tạo notification element
    const notification = document.createElement('div');
    notification.className = `alert alert-${type === 'success' ? 'success' : 'danger'} alert-dismissible fade show position-fixed`;
    notification.style.cssText = `
        top: 20px;
        right: 20px;
        z-index: 9999;
        max-width: 300px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    `;

    notification.innerHTML = `
        <strong>${type === 'success' ? 'Thành công!' : 'Lỗi!'}</strong>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    document.body.appendChild(notification);

    // Tự động ẩn sau 3 giây
    setTimeout(() => {
        if (notification.parentNode) {
            notification.classList.remove('show');
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 150); // Đợi animation fade out
        }
    }, 3000);
}