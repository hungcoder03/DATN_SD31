/**
 * JavaScript cho trang chi tiết sản phẩm
 * Xử lý màu sắc, form và các tương tác
 */

// Hàm để xác định màu chữ dựa trên màu nền
function getTextColor(backgroundColor) {
    if (!backgroundColor) return 'black';
    
    // Chuyển đổi màu hex sang RGB
    let hex = backgroundColor.replace('#', '');
    if (hex.length === 3) {
        hex = hex.split('').map(char => char + char).join('');
    }
    
    const r = parseInt(hex.substr(0, 2), 16);
    const g = parseInt(hex.substr(2, 2), 16);
    const b = parseInt(hex.substr(4, 2), 16);
    
    // Tính độ sáng
    const brightness = (r * 299 + g * 587 + b * 114) / 1000;
    
    // Trả về màu chữ dựa trên độ sáng
    return brightness > 128 ? 'black' : 'white';
}

// Hàm để lấy màu mặc định dựa trên tên màu
function getDefaultColor(colorName) {
    if (!colorName) return '#f0f0f0';
    
    const colorMap = {
        'red': '#ff0000',
        'green': '#00ff00',
        'blue': '#0000ff',
        'yellow': '#ffff00',
        'pink': '#ffc0cb',
        'black': '#000000',
        'white': '#ffffff',
        'gray': '#808080',
        'GRAY': '#808080',
        'đỏ': '#ff0000',
        'xanh': '#00ff00',
        'xanh lá': '#00ff00',
        'xanh dương': '#0000ff',
        'vàng': '#ffff00',
        'hồng': '#ffc0cb',
        'đen': '#000000',
        'trắng': '#ffffff',
        'xám': '#808080'
    };
    
    const normalizedName = colorName.toLowerCase().trim();
    return colorMap[normalizedName] || '#f0f0f0';
}

// Hàm để áp dụng màu sắc cho các chip màu
function applyColors() {
    // Áp dụng cho các chip màu chính
    const colorChips = document.querySelectorAll('.color-chip');
    
    colorChips.forEach((chip) => {
        const color = chip.dataset.color;
        const name = chip.dataset.name;
        
        if (color && color !== '#f0f0f0') {
            chip.style.backgroundColor = color;
            chip.style.color = getTextColor(color);
        } else {
            // Nếu không có màu, sử dụng tên màu để tạo màu mặc định
            const defaultColor = getDefaultColor(name);
            chip.style.backgroundColor = defaultColor;
            chip.style.color = getTextColor(defaultColor);
        }
    });
    
    // Áp dụng cho các indicator màu nhỏ
    const colorIndicators = document.querySelectorAll('.color-indicator');
    
    colorIndicators.forEach((indicator) => {
        const color = indicator.dataset.color;
        if (color && color !== '#f0f0f0') {
            indicator.style.backgroundColor = color;
        } else {
            // Tìm tên màu từ text gần đó
            const colorName = indicator.closest('.tw-flex')?.querySelector('h5')?.textContent;
            if (colorName) {
                const defaultColor = getDefaultColor(colorName);
                indicator.style.backgroundColor = defaultColor;
            }
        }
    });
}

// Hàm xóa biến thể
/* removed: unused xoaBienThe */

// Hàm áp dụng mặc định toàn cục
function applyDefaultsGlobal(btn) {
	const container = btn ? btn.closest('.tw-grid') : document;
	const gGocInput = document.getElementById('globalGiaGoc');
	const gNhapInput = document.getElementById('globalGiaNhap');
	const gBanInput = document.getElementById('globalGiaBan');
	const slInput = document.getElementById('globalSoLuong');

	const gGoc = gGocInput.value;
	const gNhap = gNhapInput.value;
	const gBan = gBanInput.value;
	const sl = slInput.value;

	// Xóa lỗi cũ
	clearFieldErrors(container);

	// Phải có ít nhất 1 checkbox
	const checkedBoxes = document.querySelectorAll('.row-check:checked');
	if (checkedBoxes.length === 0) {
		addFieldError(gGocInput, 'Chưa chọn size');
		gGocInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
		gGocInput.focus();
		return;
	}

	let hasError = false;
	// Validation và hiển thị inline dưới input
	if (gGoc && parseFloat(gGoc) <= 0) { addFieldError(gGocInput, 'Phải > 0'); hasError = true; }
	if (gNhap && parseFloat(gNhap) <= 0) { addFieldError(gNhapInput, 'Phải > 0'); hasError = true; }
	if (gBan && parseFloat(gBan) <= 0) { addFieldError(gBanInput, 'Phải > 0'); hasError = true; }
	if (gGoc && gNhap && parseFloat(gNhap) > parseFloat(gGoc)) { addFieldError(gNhapInput, 'Không > giá gốc'); hasError = true; }
	if (gBan && gNhap && parseFloat(gBan) < parseFloat(gNhap)) { addFieldError(gBanInput, 'Không < giá nhập'); hasError = true; }
	if (gBan && gGoc && parseFloat(gBan) > parseFloat(gGoc)) { addFieldError(gBanInput, 'Không > giá gốc'); hasError = true; }
	if (sl && (parseInt(sl) < 5 || parseInt(sl) > 1000)) { addFieldError(slInput, '5 - 1000'); hasError = true; }
	if (hasError) { return; }

	let appliedCount = 0;
	document.querySelectorAll('.row-check:checked').forEach(chk => {
		const tr = chk.closest('tr');
		if (!tr) return;
		const gg = tr.querySelector('input[data-role="giaGoc"]');
		const gn = tr.querySelector('input[data-role="giaNhap"]');
		const gb = tr.querySelector('input[data-role="giaBan"]');
		const q = tr.querySelector('input[data-role="soLuong"]');
		if (gGoc !== '' && gg) gg.value = gGoc;
		if (gNhap !== '' && gn) gn.value = gNhap;
		if (gBan !== '' && gb) gb.value = gBan;
		if (sl !== '' && q) q.value = sl;
		appliedCount++;
	});
	computeTotals();
}

// Hàm áp dụng mặc định theo màu
function applyDefaultsForColor(colorId) {
	const gGocInput = document.getElementById('colorDefaultGiaGoc__' + colorId);
	const gNhapInput = document.getElementById('colorDefaultGiaNhap__' + colorId);
	// const gBanInput = document.getElementById('colorDefaultGiaBan__' + colorId);
	const slInput = document.getElementById('colorDefaultSoLuong__' + colorId);

	const gGoc = gGocInput.value;
	const gNhap = gNhapInput.value;
	// const gBan = gBanInput.value;
	const sl = slInput.value;

	// Xóa lỗi cũ phạm vi khối màu
	const colorCard = gGocInput.closest('.tw-grid');
	clearFieldErrors(colorCard || undefined);

	// Kiểm tra có checkbox nào của màu này
	const checkedBoxes = document.querySelectorAll('.row-check:checked');
	let hasCheckedForColor = false;
	checkedBoxes.forEach(chk => { if (String(chk.dataset.colorId) === String(colorId)) hasCheckedForColor = true; });
	if (!hasCheckedForColor) {
		addFieldError(gGocInput, 'Chưa chọn size cho màu này');
		gGocInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
		gGocInput.focus();
		return;
	}

	let hasError = false;
	if (gGoc && parseFloat(gGoc) <= 0) { addFieldError(gGocInput, 'Phải > 0'); hasError = true; }
	if (gNhap && parseFloat(gNhap) <= 0) { addFieldError(gNhapInput, 'Phải > 0'); hasError = true; }
	// if (gBan && parseFloat(gBan) <= 0) { addFieldError(gBanInput, 'Phải > 0'); hasError = true; }
	if (gGoc && gNhap && parseFloat(gNhap) > parseFloat(gGoc)) { addFieldError(gNhapInput, 'Không > giá gốc'); hasError = true; }
	// if (gBan && gNhap && parseFloat(gBan) < parseFloat(gNhap)) { addFieldError(gBanInput, 'Không < giá nhập'); hasError = true; }
	// if (gBan && gGoc && parseFloat(gBan) > parseFloat(gGoc)) { addFieldError(gBanInput, 'Không > giá gốc'); hasError = true; }
	if (sl && (parseInt(sl) < 5 || parseInt(sl) > 1000)) { addFieldError(slInput, '5 - 1000'); hasError = true; }
	if (hasError) { return; }

	let appliedCount = 0;
	document.querySelectorAll('.row-check:checked').forEach(chk => {
		if (String(chk.dataset.colorId) !== String(colorId)) return;
		const tr = chk.closest('tr');
		if (!tr) return;
		const gg = tr.querySelector('input[data-role="giaGoc"]');
		const gn = tr.querySelector('input[data-role="giaNhap"]');
		// const gb = tr.querySelector('input[data-role="giaBan"]');
		const q = tr.querySelector('input[data-role="soLuong"]');
		if (gGoc !== '' && gg) gg.value = gGoc;
		if (gNhap !== '' && gn) gn.value = gNhap;
		// if (gBan !== '' && gb) gb.value = gBan;
		if (sl !== '' && q) q.value = sl;
		appliedCount++;
	});
	computeTotals();
}

// Hàm tính tổng số lượng đã chọn và cập nhật trạng thái nút lưu
function computeTotals() {
    const counts = {};
    
    document.querySelectorAll('.row-check').forEach(chk => {
        const colorId = chk.dataset.colorId;
        counts[colorId] = counts[colorId] || 0;
        if (chk.checked) counts[colorId] += 1;
    });
    
    Object.keys(counts).forEach(colorId => {
        const el = document.getElementById('selectedCount__' + colorId);
        if (el) el.textContent = counts[colorId];
        
        // Tìm nút lưu trong form của màu này
        const form = el.closest('form');
        if (form) {
            const saveButton = form.querySelector('button[type="submit"]');
            if (saveButton) {
                if (counts[colorId] > 0) {
                    saveButton.disabled = false;
                    saveButton.classList.remove('tw-opacity-50', 'tw-cursor-not-allowed');
                    saveButton.classList.add('hover:tw-bg-green-700');
                } else {
                    saveButton.disabled = true;
                    saveButton.classList.add('tw-opacity-50', 'tw-cursor-not-allowed');
                    saveButton.classList.remove('hover:tw-bg-green-700');
                }
            }
        }
    });
}

function clearFieldErrors(scope){
    (scope || document).querySelectorAll('.tw-border-red-500').forEach(el=>{
        el.classList.remove('tw-border-red-500');
    });
    (scope || document).querySelectorAll('.field-error-hint').forEach(el=>{
        el.remove();
    });
}

function addFieldError(input, message){
    if (!input) return;
    input.classList.add('tw-border-red-500');
    const hint = document.createElement('div');
    hint.className = 'field-error-hint tw-text-xs tw-text-red-600 tw-mt-1';
    hint.textContent = message;
    input.insertAdjacentElement('afterend', hint);
}

// Hàm kiểm tra có size nào được chọn không
function ensureAnySelected(btn) {
    const form = btn.closest('form');
    const anyChecked = form.querySelector('.row-check:checked');

    clearFieldErrors(form);
    
                    if (!anyChecked) {
                    return false;
                }
    
    let hasError = false;
    let firstInvalid = null;
    const seenMessages = new Set();
    
    form.querySelectorAll('.row-check:checked').forEach(chk => {
        const tr = chk.closest('tr');
        if (!tr) return;
        
        const giaGoc = tr.querySelector('input[data-role="giaGoc"]');
        const giaNhap = tr.querySelector('input[data-role="giaNhap"]');
        // const giaBan = tr.querySelector('input[data-role="giaBan"]');
        const soLuong = tr.querySelector('input[data-role="soLuong"]');
        
        if (!giaGoc || !giaGoc.value || parseFloat(giaGoc.value) <= 0) {
            addFieldError(giaGoc, 'Giá gốc phải > 0');
            if (!firstInvalid) firstInvalid = giaGoc;
            seenMessages.add('Giá gốc phải lớn hơn 0');
            hasError = true;
        }
        if (!giaNhap || !giaNhap.value || parseFloat(giaNhap.value) <= 0) {
            addFieldError(giaNhap, 'Giá nhập phải > 0');
            if (!firstInvalid) firstInvalid = giaNhap;
            seenMessages.add('Giá nhập phải lớn hơn 0');
            hasError = true;
        }
        if (!soLuong || !soLuong.value || parseInt(soLuong.value) < 5 || parseInt(soLuong.value) > 1000) {
            addFieldError(soLuong, 'Số lượng 5-1000');
            if (!firstInvalid) firstInvalid = soLuong;
            seenMessages.add('Số lượng phải từ 5 đến 1000');
            hasError = true;
        }
        
        if (giaGoc && giaNhap && giaGoc.value && giaNhap.value && parseFloat(giaNhap.value) > parseFloat(giaGoc.value)) {
            addFieldError(giaNhap, 'Không > giá gốc');
            if (!firstInvalid) firstInvalid = giaNhap;
            seenMessages.add('Giá nhập không được cao hơn giá gốc');
            hasError = true;
        }
        // if (giaGoc && giaBan && giaGoc.value && giaBan.value && parseFloat(giaBan.value) > parseFloat(giaGoc.value)) {
        //     addFieldError(giaBan, 'Không > giá gốc');
        //     if (!firstInvalid) firstInvalid = giaBan;
        //     seenMessages.add('Giá bán không được cao hơn giá gốc');
        //     hasError = true;
        // }
        // if (giaBan && giaNhap && giaBan.value && giaNhap.value && parseFloat(giaBan.value) < parseFloat(giaNhap.value)) {
        //     addFieldError(giaBan, 'Không < giá nhập');
        //     if (!firstInvalid) firstInvalid = giaBan;
        //     seenMessages.add('Giá bán không được nhỏ hơn giá nhập');
        //     hasError = true;
        // }
    });
    
                    if (hasError) {
                    if (firstInvalid) {
                        firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        firstInvalid.focus();
                    }
                    return false;
                }
    
    return true;
}

// Xóa viền đỏ/hint khi người dùng sửa
document.addEventListener('input', function(e){
    const target = e.target;
    if (target.matches('input[data-role]')) {
        target.classList.remove('tw-border-red-500');
        const next = target.nextElementSibling;
        if (next && next.classList.contains('field-error-hint')) {
            next.remove();
        }
    }
});

// Khởi tạo khi DOM sẵn sàng
document.addEventListener('DOMContentLoaded', function() {
    // Thiết lập form màu sắc
    setupColorForm();
    
    // Áp dụng màu sắc
    setTimeout(function() {
        applyColors();
    }, 100);
});

// Thiết lập form màu sắc
function setupColorForm() {
    const form = document.getElementById('colorForm');
    if (!form) return;
    
    // Xử lý thay đổi checkbox màu
    form.querySelectorAll('.color-check').forEach(function(cb) {
        cb.addEventListener('change', function() {
            form.submit();
        });
    });
    
    // Toggle selected style
    form.querySelectorAll('label').forEach(function(label){
        const input = label.querySelector('.color-check');
        const chip = label.querySelector('.color-chip');
        
        if (!chip) return;
        
        const update = function(){
            if (input.checked) {
                chip.classList.add('tw-ring-2','tw-ring-blue-500');
            } else {
                chip.classList.remove('tw-ring-2','tw-ring-blue-500');
            }
        };
        
        update();
        input.addEventListener('change', update);
    });
}

// Hàm mở modal xác nhận xóa
let __pendingDelete = { id: null, button: null };
function openDeleteConfirm(variantId, btn){
    __pendingDelete.id = variantId;
    __pendingDelete.button = btn;
    const modal = document.getElementById('deleteConfirmModal');
    if (modal) modal.classList.remove('tw-hidden');
}

function closeDeleteConfirm(){
    const modal = document.getElementById('deleteConfirmModal');
    if (modal) modal.classList.add('tw-hidden');
}

// Gắn sự kiện cho modal và khởi tạo trạng thái nút lưu
document.addEventListener('DOMContentLoaded', function(){
    const btnCancel = document.getElementById('btnCancelDelete');
    const btnConfirm = document.getElementById('btnConfirmDelete');
    if (btnCancel) btnCancel.addEventListener('click', closeDeleteConfirm);
    if (btnConfirm) btnConfirm.addEventListener('click', function(){
        if (!__pendingDelete.id) { closeDeleteConfirm(); return; }
        deleteVariant(__pendingDelete.id, __pendingDelete.button);
        closeDeleteConfirm();
    });
    
    // Khởi tạo trạng thái nút lưu
    computeTotals();
    
    // Gắn sự kiện cho checkbox
    document.querySelectorAll('.row-check').forEach(chk => {
        chk.addEventListener('change', computeTotals);
    });
});

// Hàm xóa biến thể sản phẩm
function deleteVariant(variantId, triggerBtn) {
    // Hiển thị loading
    const button = triggerBtn || event.target.closest('button');
    const originalText = button.innerHTML;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xóa...';
    button.disabled = true;
    
    // Gọi API xóa
    fetch(`/admin/san-pham/bien-the/xoa/${variantId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            const row = document.querySelector(`tr[data-variant-id="${variantId}"]`);
            if (row) {
                row.remove();
                showMessage('Xóa biến thể thành công!', 'success');
            }
        } else {
            showMessage(data.message || 'Có lỗi xảy ra khi xóa biến thể', 'error');
        }
    })
    .catch(error => {
        console.error('Lỗi:', error);
        showMessage('Có lỗi xảy ra khi kết nối server', 'error');
    })
    .finally(() => {
        button.innerHTML = originalText;
        button.disabled = false;
    });
}

// Hàm hiển thị thông báo
function showMessage(message, type = 'info') {
    // Tạo element thông báo
    const notification = document.createElement('div');
    notification.className = `tw-fixed tw-top-4 tw-right-4 tw-px-6 tw-py-3 tw-rounded-lg tw-shadow-lg tw-text-white tw-transition-all tw-duration-300 ${
        type === 'success' ? 'tw-bg-green-500' : 
        type === 'error' ? 'tw-bg-red-500' : 
        'tw-bg-blue-500'
    }`;
// Bảo đảm nổi trên mọi layout
    notification.style.zIndex = '9999999';
    notification.textContent = message;
    
    // Thêm vào body
    document.body.appendChild(notification);
    
    // Tự động ẩn sau 3 giây
    setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 3000);
} 