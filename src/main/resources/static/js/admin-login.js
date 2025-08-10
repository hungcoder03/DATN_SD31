// Admin Login JavaScript
document.addEventListener('DOMContentLoaded', function() {

    // Form elements
    const loginForm = document.getElementById('loginForm');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const loginBtn = document.querySelector('.login-btn');
    const errorMessage = document.querySelector('.error-message');

    // Add floating label effect
    function addFloatingLabels() {
        const inputs = document.querySelectorAll('.form-group input');

        inputs.forEach(input => {
            // Add floating class if input has value
            if (input.value) {
                input.parentElement.classList.add('has-value');
            }

            // Add event listeners
            input.addEventListener('focus', function() {
                this.parentElement.classList.add('focused');
                // Clear error styling when user starts typing
                clearInputError(this);
            });

            input.addEventListener('blur', function() {
                this.parentElement.classList.remove('focused');
                if (this.value) {
                    this.parentElement.classList.add('has-value');
                } else {
                    this.parentElement.classList.remove('has-value');
                }
            });

            input.addEventListener('input', function() {
                if (this.value) {
                    this.parentElement.classList.add('has-value');
                } else {
                    this.parentElement.classList.remove('has-value');
                }
                clearInputError(this);
            });
        });
    }

    // Clear input error styling
    function clearInputError(input) {
        input.style.borderColor = '';
        input.style.boxShadow = '';
        input.classList.remove('error-input');
    }

    // Form validation with detailed error messages
    function validateForm() {
        let isValid = true;
        const errors = [];

        // Email validation
        const email = usernameInput.value.trim();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!email) {
            errors.push('Email không được để trống');
            markInputError(usernameInput);
            isValid = false;
        } else if (!emailRegex.test(email)) {
            errors.push('Email không đúng định dạng (VD: admin@example.com)');
            markInputError(usernameInput);
            isValid = false;
        } else {
            clearInputError(usernameInput);
        }

        // Password validation
        const password = passwordInput.value.trim();
        if (!password) {
            errors.push('Mật khẩu không được để trống');
            markInputError(passwordInput);
            isValid = false;
        } else {
            clearInputError(passwordInput);
        }

        return { isValid, errors };
    }

    // Mark input as error
    function markInputError(input) {
        input.style.borderColor = '#ff3b30';
        input.style.boxShadow = '0 0 15px rgba(255, 59, 48, 0.4)';
        input.classList.add('error-input');
    }

    // Show error message
    function showError(message) {
        if (errorMessage) {
            errorMessage.innerHTML = `<strong>❌ Lỗi!</strong><br>${message}`;
            errorMessage.style.display = 'block';
        }
    }

    // Hide error message
    function hideError() {
        if (errorMessage) {
            errorMessage.style.display = 'none';
        }
    }

    // Loading state for button
    function setLoadingState(isLoading) {
        if (isLoading) {
            loginBtn.classList.add('loading');
            loginBtn.textContent = 'Đang xử lý...';
            loginBtn.disabled = true;
        } else {
            loginBtn.classList.remove('loading');
            loginBtn.textContent = 'Đăng nhập';
            loginBtn.disabled = false;
        }
    }

    // Form submission handler
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();

            // Hide previous errors
            hideError();

            // Validate form
            const validation = validateForm();

            if (!validation.isValid) {
                showError(validation.errors.join('<br>'));
                return;
            }

            // Show loading state
            setLoadingState(true);

            // Submit the form
            setTimeout(() => {
                this.submit();
            }, 1000);
        });
    }

    // Input validation on blur with specific error messages
    if (usernameInput) {
        usernameInput.addEventListener('blur', function() {
            const email = this.value.trim();
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

            if (email && !emailRegex.test(email)) {
                markInputError(this);
                showError('Email không đúng định dạng. Vui lòng kiểm tra lại.');
            } else if (email) {
                clearInputError(this);
            }
        });
    }

    if (passwordInput) {
        passwordInput.addEventListener('blur', function() {
            const password = this.value.trim();

            if (password && password.length < 1) {
                markInputError(this);
                showError('Mật khẩu không được để trống.');
            } else if (password) {
                clearInputError(this);
            }
        });
    }

    // Add keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Ctrl + Enter to submit form
        if (e.ctrlKey && e.key === 'Enter') {
            if (loginForm) {
                loginForm.dispatchEvent(new Event('submit'));
            }
        }

        // Escape to clear form
        if (e.key === 'Escape') {
            if (loginForm) {
                loginForm.reset();
                hideError();
                setLoadingState(false);
                // Clear all error styling
                document.querySelectorAll('.error-input').forEach(input => {
                    clearInputError(input);
                });
            }
        }
    });

    // Add particle animation enhancement
    function enhanceParticles() {
        const particles = document.querySelectorAll('.particle');

        particles.forEach((particle, index) => {
            // Add random movement
            setInterval(() => {
                const randomX = Math.random() * 20 - 10;
                const randomY = Math.random() * 20 - 10;
                particle.style.transform += ` translate(${randomX}px, ${randomY}px)`;
            }, 3000 + index * 1000);
        });
    }

    // Initialize all features
    addFloatingLabels();
    enhanceParticles();

    // Add smooth scroll to top when page loads
    window.scrollTo({ top: 0, behavior: 'smooth' });

    // Console welcome message
    console.log('🎨 Admin Login Interface Loaded Successfully!');
    console.log('💡 Tips: Use Ctrl+Enter to submit, Escape to clear form');
}); 