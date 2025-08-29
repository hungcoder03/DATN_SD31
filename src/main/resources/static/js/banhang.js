// banhang.js - Gom logic cho trang bán hàng, tương thích Tailwind

(function () {
	// Helpers
	function qs(sel, scope) {
		return (scope || document).querySelector(sel);
	}
	function qsa(sel, scope) {
		return Array.from((scope || document).querySelectorAll(sel));
	}
	function getCsrf() {
		const tokenMeta = qs('meta[name="_csrf"]');
		const headerMeta = qs('meta[name="_csrf_header"]');
		return {
			token: tokenMeta ? tokenMeta.content : null,
			header: headerMeta ? headerMeta.content : 'X-CSRF-TOKEN'
		};
	}
	function fetchJson(url, options = {}) {
		return fetch(url, options).then(async (res) => {
			if (!res.ok) {
				const text = await res.text();
				throw new Error(text || ('HTTP ' + res.status));
			}
			const ct = res.headers.get('content-type') || '';
			if (ct.includes('application/json')) return res.json();
			return res.text();
		});
	}

	// Modal Tailwind
	window.openModal = function (id) {
		const el = qs('#' + id);
		if (el) el.classList.remove('hidden');
	};
	window.closeModal = function (id) {
		const el = qs('#' + id);
		if (el) el.classList.add('hidden');
	};

	// Quagga - Scanner
	let quaggaInited = false;
	let scanning = false;
	window.openScanner = function () {
		const overlay = qs('#scannerModal');
		if (!overlay) return;
		overlay.classList.remove('hidden');
		if (!window.Quagga) return;
		if (scanning) return;
		window.Quagga.init({
			inputStream: {
				name: 'Live',
				type: 'LiveStream',
				target: qs('#scanner'),
				constraints: { width: 640, height: 400, facingMode: 'environment' }
			},
			decoder: { readers: ['ean_reader', 'code_128_reader', 'upc_reader', 'ean_8_reader', 'code_39_reader'] }
		}, function (err) {
			if (err) {
				console.error('Quagga init error:', err);
				return;
			}
			window.Quagga.start();
			scanning = true;
		});
		window.Quagga.offDetected();
		window.Quagga.onDetected(function (result) {
			const code = result && result.codeResult && result.codeResult.code;
			if (!code) return;
			window.Quagga.offDetected();
			window.Quagga.stop();
			scanning = false;
			const timSanPham = qs('#timSanPham');
			if (!timSanPham) return;
			fetchJson(`/admin/ban-hang/tim-kiem-theo-ma-vach?maVach=${code}`)
				.then((sp) => {
					if (sp && sp.tenSanPham) {
						timSanPham.value = `${sp.tenSanPham} - ${sp.mauSac} / ${sp.size || sp.kichThuoc || ''} - SL: ${sp.soluong} - Giá: ${sp.gia}`;
						const idEl = qs('#idChiTietSp');
						if (idEl) idEl.value = sp.idChiTietSp;
						const form = qs('#formThemGio');
						if (form) form.submit();
					} else {
						timSanPham.value = `Không tìm thấy mã vạch: ${code}`;
					}
				})
				.catch((err) => {
					console.error('Lỗi khi tìm sản phẩm:', err);
					timSanPham.value = 'Lỗi kết nối khi tìm sản phẩm.';
				});
		});
	};
	window.closeScanner = function () {
		const overlay = qs('#scannerModal');
		if (overlay) overlay.classList.add('hidden');
		if (window.Quagga && scanning) {
			window.Quagga.stop();
			scanning = false;
		}
	};

	// Tìm kiếm + gợi ý + bảng kết quả
	let searchAbortController = null;
	let searchTimer = null;
	function renderKetQua(dsSp) {
		const body = qs('#ketQuaTimSanPham');
		if (!body) return;
		if (!dsSp || dsSp.length === 0) {
			body.innerHTML = `<tr><td colspan="7" class="p-3 text-center text-gray-500">Không tìm thấy sản phẩm nào</td></tr>`;
			return;
		}
		body.innerHTML = dsSp.map((sp, index) => `
			<tr onclick=\"chonSanPham('${sp.id}', '${sp.tenSanPham} - ${sp.mauSac} / ${sp.kichThuoc} - SL: ${sp.soLuong}')\">
				<td class=\"p-3\">${index + 1}</td>
				<td class=\"p-3\">${sp.ma || ''}</td>
				<td class=\"p-3\"><img src=\"${sp.hinhAnh}\" alt=\"Ảnh\" class=\"w-12 h-12 object-cover rounded\"></td>
				<td class=\"p-3\">${sp.tenSanPham} - ${sp.mauSac} / ${sp.kichThuoc}</td>
				<td class=\"p-3\">${sp.soLuong}</td>
				<td class=\"p-3\">${sp.giaBan}</td>
				<td class=\"p-3\">
					${(sp.trangThaiHoatDong && sp.soLuong > 0)
						? `<button class=\"px-3 py-1 rounded-md bg-[#dead6f] text-[#333333] hover:bg-[#997351]\" onclick=\"themVaoGioHang('${sp.id}')\">Thêm vào giỏ</button>`
						: (sp.trangThaiHoatDong && sp.soLuong === 0
							? `<span class=\"text-yellow-600\">Hết hàng</span>`
							: `<span class=\"text-red-600\">Ngừng bán</span>`)}
				</td>
			</tr>
		`).join('');
	}
	window.chonSanPham = function (id, text) {
		const input = qs('#timSanPham');
		const hidden = qs('#idChiTietSp');
		if (input) input.value = text;
		if (hidden) hidden.value = id;
	};

	window.themVaoGioHang = function (idChiTietSp) {
		const cartKey = qs('input[name="cartKey"]')?.value;
		if (!cartKey) {
			alert('❌ Thiếu cartKey!');
			return;
		}
		const { token, header } = getCsrf();
		fetch('/admin/ban-hang/them-gio-hang', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				...(token ? { [header]: token } : {})
			},
			body: JSON.stringify({ idChiTietSp, soLuong: 1, cartKey })
		})
			.then((res) => (res.ok ? res.text() : res.text().then((t) => Promise.reject(new Error(t)))))
			.then(() => {
				alert('✅ Đã thêm vào giỏ hàng');
				window.location.reload();
			})
			.catch((err) => {
				console.error('Lỗi khi thêm sản phẩm:', err);
				alert('❌ Không thể thêm sản phẩm vào giỏ: ' + err.message);
			});
	};

	window.validateSoLuong = function (form) {
		const input = form.querySelector('input[name="soLuong"]');
		const soLuongMoi = parseInt(input.value, 10);
		const soLuongTon = parseInt(input.getAttribute('max'), 10);
		if (Number.isFinite(soLuongTon) && soLuongMoi > soLuongTon) {
			alert('❌ Số lượng vượt quá tồn kho: ' + soLuongTon);
			return false;
		}
		return true;
	};

	window.thayDoiSoLuong = function (button, delta) {
		const form = button.closest('form');
		const input = form.querySelector('.so-luong-input, input[name="soLuong"]');
		const min = parseInt(input.min || '1', 10);
		const max = parseInt(input.max || '9999', 10);
		let value = parseInt(input.value || '1', 10);
		if (delta > 0 && value >= max) {
			alert('❌ Số lượng đã đạt tối đa tồn kho (' + max + ').');
			return;
		}
		value += delta;
		if (value < min) value = min;
		if (value > max) value = max;
		input.value = value;
		form.submit();
	};

	// Toggle QR image
	window.toggleQrImage = function () {
		const ckRadio = qs('#pttt_ck');
		const qrDiv = qs('#qrChuyenKhoan');
		if (!qrDiv) return;
		if (ckRadio && ckRadio.checked) qrDiv.classList.remove('hidden');
		else qrDiv.classList.add('hidden');
	};

	window.updateDiaChiHienThi = function () {
		const checkbox = qs('#muonVanChuyen');
		if (!checkbox || !checkbox.checked) {
			qs('#hiddenTinh').value = '';
			qs('#hiddenHuyen').value = '';
			qs('#hiddenXa').value = '';
			return;
		}
		const province = qs('#province');
		const district = qs('#district');
		const ward = qs('#ward');
		qs('#hiddenTinh').value = province && province.selectedIndex > 0 ? province.options[province.selectedIndex].text : '';
		qs('#hiddenHuyen').value = district && district.selectedIndex > 0 ? district.options[district.selectedIndex].text : '';
		qs('#hiddenXa').value = ward && ward.selectedIndex > 0 ? ward.options[ward.selectedIndex].text : '';
	};

	// Load địa chỉ & phí ship
	function initAddress() {
		const provinceSelect = qs('#province');
		const districtSelect = qs('#district');
		const wardSelect = qs('#ward');
		const shippingFeeElement = qs('#shippingFee');
		if (!provinceSelect || !districtSelect || !wardSelect) return;

		function fetchData(url) { return fetchJson(url); }

		fetchData('/admin/ban-hang/dia-chi/tinh').then((provinces) => {
			provinceSelect.innerHTML = '<option value="">-- Chọn tỉnh --</option>';
			provinces.forEach((p) => { provinceSelect.innerHTML += `<option value="${p.ProvinceID}">${p.ProvinceName}</option>`; });
		});

		provinceSelect.addEventListener('change', async function () {
			districtSelect.innerHTML = '<option value="">-- Chọn huyện --</option>';
			wardSelect.innerHTML = '<option value="">-- Chọn xã --</option>';
			if (shippingFeeElement) shippingFeeElement.innerText = 'Chưa tính';
			if (!this.value) return;
			const districts = await fetchData(`/admin/ban-hang/dia-chi/huyen?provinceId=${this.value}`);
			districts.forEach((d) => { districtSelect.innerHTML += `<option value="${d.DistrictID}">${d.DistrictName}</option>`; });
		});

		districtSelect.addEventListener('change', async function () {
			wardSelect.innerHTML = '<option value="">-- Chọn xã --</option>';
			if (shippingFeeElement) shippingFeeElement.innerText = 'Chưa tính';
			if (!this.value) return;
			const wards = await fetchData(`/admin/ban-hang/dia-chi/xa?districtId=${this.value}`);
			wards.forEach((w) => { wardSelect.innerHTML += `<option value="${w.WardCode}">${w.WardName}</option>`; });
		});

		wardSelect.addEventListener('change', async function () {
			const districtId = districtSelect.value;
			const wardCode = this.value;
			const cartKey = qs('#cartKey')?.value;
			if (!districtId || !wardCode) return;
			try {
				const fee = await fetchJson(`/admin/ban-hang/phi-ship?toDistrictId=${districtId}&wardCode=${wardCode}&cartKey=${cartKey}`);
				if (shippingFeeElement) shippingFeeElement.innerText = Number(fee).toLocaleString('vi-VN') + ' ₫';
				const tongTien = parseFloat(qs('#tongTienHidden').value || '0');
				const giamGia = parseFloat(qs('#giamGiaHidden').value || '0');
				const tongSauGiam = tongTien - giamGia + Number(fee || 0);
				const display = qs('#tongTienSauGiamDisplay');
				const hidden = qs('#tongTienSauGiamHidden');
				if (display) display.innerText = tongSauGiam.toLocaleString('vi-VN') + ' ₫';
				if (hidden) hidden.value = String(tongSauGiam);
			} catch (e) {
				if (shippingFeeElement) shippingFeeElement.innerText = 'Không thể tính phí';
				console.error('Lỗi khi tính phí vận chuyển:', e);
			}
		});
	}

	// Form thêm KH nhanh
	function initQuickCustomerForm() {
		const form = qs('#formThemKhachHang');
		if (!form) return;
		form.addEventListener('submit', function (e) {
			// Cho phép submit bình thường để server xử lý redirect/flash nếu cần,
			// Nếu muốn AJAX, bật đoạn dưới và e.preventDefault().
			// e.preventDefault();
		});
	}

	document.addEventListener('DOMContentLoaded', function () {
		// Gợi ý box đóng khi click ngoài
		document.addEventListener('click', function (e) {
			const goiYBox = qs('#goiYSanPham');
			const timSanPham = qs('#timSanPham');
			if (goiYBox && !goiYBox.contains(e.target) && e.target !== timSanPham) {
				goiYBox.innerHTML = '';
				goiYBox.classList.add('hidden');
			}
		});

		// Debounce tìm kiếm
		const timSanPham = qs('#timSanPham');
		if (timSanPham) {
			timSanPham.addEventListener('input', function () {
				const keyword = this.value.trim();
				clearTimeout(searchTimer);
				searchTimer = setTimeout(() => {
					if (searchAbortController) searchAbortController.abort();
					searchAbortController = new AbortController();
					const url = keyword.length < 2
						? `/admin/ban-hang/tim-kiem-san-pham`
						: `/admin/ban-hang/tim-kiem-san-pham?keyword=${encodeURIComponent(keyword)}`;
					fetch(url, { signal: searchAbortController.signal })
						.then((res) => res.json())
						.then((dsSp) => renderKetQua(dsSp))
						.catch((err) => {
							if (err.name === 'AbortError') return;
							console.error(err);
							renderKetQua([]);
						});
				}, 350);
			});
		}

		// Toggle vận chuyển + required
		const checkbox = qs('#muonVanChuyen');
		const diaChiDiv = qs('#diaChiVanChuyen');
		if (checkbox && diaChiDiv) {
			const inputs = qsa('input:not(#ghichu), select', diaChiDiv);
			function setRequired(isRequired) {
				inputs.forEach((input) => {
					if (isRequired) input.setAttribute('required', 'required');
					else input.removeAttribute('required');
				});
			}
			checkbox.addEventListener('change', function () {
				if (this.checked) {
					diaChiDiv.classList.remove('hidden');
					setRequired(true);
				} else {
					diaChiDiv.classList.add('hidden');
					setRequired(false);
				}
			});
		}

		// Khởi tạo
		initAddress();
		initQuickCustomerForm();
		toggleQrImage();
	});
})(); 