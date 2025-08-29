/**
 * Mini Cart JavaScript - Simplified Version
 * Only handles display functionality, no interactive features
 */

class MiniCart {
    constructor() {
        this.cartItemsContainer = document.getElementById('cartItemsContainer');
        this.cartCount = document.getElementById('cartCount');
        this.cartTotal = document.getElementById('cartTotal');
        this.mobileHeaderCartCount = document.getElementById('mobileHeaderCartCount');
        this.headerCartCount = document.getElementById('headerCartCount');
        
        this.init();
    }

    init() {
        this.setupEventListeners();
    }

    setupEventListeners() {
        // Listen for cart updates from other parts of the app
        document.addEventListener('cartUpdated', () => {
            // Refresh page to get updated cart data from server
            window.location.reload();
        });

        // Listen for add to cart events (for form submissions)
        document.addEventListener('addToCart', (e) => {
            // Page will be reloaded by server-side redirect
            console.log('Product added to cart');
        });
    }

    // Public method to refresh page (for external calls)
    refresh() {
        window.location.reload();
    }
}

// Initialize mini cart when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.miniCart = new MiniCart();
});

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = MiniCart;
} 