// banhang.js - CẬP NHẬT với địa chỉ chi tiết

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

	// Biến global để lưu thông tin khách hàng đã chọn
	let selectedCustomerInfo = null;

	// Modal Tailwind
	window.openModal = function (id) {
		const el = qs('#' + id);
		if (el) el.classList.remove('hidden');
	};
	window.closeModal = function (id) {
		const el = qs('#' + id);
		if (el) el.classList.add('hidden');
	};

	// Quagga - Scanner (giữ nguyên)
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

	// CẬP NHẬT: Function để auto fill thông tin khách hàng VÀ ĐỊA CHỈ CHI TIẾT
	function autoFillCustomerInfo() {
		if (!selectedCustomerInfo) return;

		const checkbox = qs('#muonVanChuyen');
		if (!checkbox || !checkbox.checked) return;

		console.log('🎯 Bắt đầu auto fill thông tin khách hàng:', selectedCustomerInfo);

		// Fill tên người nhận
		const tenInput = qs('#ten');
		if (tenInput && selectedCustomerInfo.ten) {
			tenInput.value = selectedCustomerInfo.ten;
			console.log('✅ Đã fill tên:', selectedCustomerInfo.ten);
		}

		// Fill số điện thoại người nhận
		const soDienThoaivcInput = qs('#soDienThoaivc');
		if (soDienThoaivcInput && selectedCustomerInfo.soDienThoai) {
			soDienThoaivcInput.value = selectedCustomerInfo.soDienThoai;
			console.log('✅ Đã fill SĐT:', selectedCustomerInfo.soDienThoai);
		}

		// CẬP NHẬT: Fill địa chỉ chi tiết
		const diaChiChiTietInput = qs('#diaChiChiTiet');
		if (diaChiChiTietInput && selectedCustomerInfo.diaChiChiTiet) {
			diaChiChiTietInput.value = selectedCustomerInfo.diaChiChiTiet;
			console.log('✅ Đã fill địa chỉ chi tiết:', selectedCustomerInfo.diaChiChiTiet);
		}

		// Auto fill địa chỉ nếu có thông tin parse từ server
		if (selectedCustomerInfo.provinceId) {
			fillAddressSelects();
		}
	}

	// Function để fill địa chỉ vào các select box (giữ nguyên)
	function fillAddressSelects() {
		const provinceSelect = qs('#province');
		const districtSelect = qs('#district');
		const wardSelect = qs('#ward');

		if (!provinceSelect || !selectedCustomerInfo) return;

		console.log('🏠 Bắt đầu fill địa chỉ:', selectedCustomerInfo);

		// 1. Set tỉnh
		if (selectedCustomerInfo.provinceId) {
			provinceSelect.value = selectedCustomerInfo.provinceId;
			console.log('✅ Đã set tỉnh:', selectedCustomerInfo.provinceName);

			// 2. Load và set huyện
			if (selectedCustomerInfo.districtId) {
				loadDistrictsAndSelect(selectedCustomerInfo.provinceId, selectedCustomerInfo.districtId);
			}
		}
	}

	// Load districts và auto select
	function loadDistrictsAndSelect(provinceId, selectedDistrictId) {
		const districtSelect = qs('#district');
		if (!districtSelect) return;

		fetchJson(`/admin/ban-hang/dia-chi/huyen-theo-tinh/${provinceId}`)
			.then(districts => {
				// Clear và rebuild options
				districtSelect.innerHTML = '<option value="">-- Chọn huyện --</option>';
				districts.forEach(d => {
					districtSelect.innerHTML += `<option value="${d.DistrictID}">${d.DistrictName}</option>`;
				});

				// Auto select district
				if (selectedDistrictId) {
					districtSelect.value = selectedDistrictId;
					console.log('✅ Đã set huyện:', selectedCustomerInfo.districtName);

					// 3. Load và set xã
					if (selectedCustomerInfo.wardCode) {
						loadWardsAndSelect(selectedDistrictId, selectedCustomerInfo.wardCode);
					}
				}
			})
			.catch(err => {
				console.error('❌ Lỗi load districts:', err);
			});
	}

	// Load wards và auto select
	function loadWardsAndSelect(districtId, selectedWardCode) {
		const wardSelect = qs('#ward');
		if (!wardSelect) return;

		fetchJson(`/admin/ban-hang/dia-chi/xa-theo-huyen/${districtId}`)
			.then(wards => {
				// Clear và rebuild options
				wardSelect.innerHTML = '<option value="">-- Chọn xã --</option>';
				wards.forEach(w => {
					wardSelect.innerHTML += `<option value="${w.WardCode}">${w.WardName}</option>`;
				});

				// Auto select ward
				if (selectedWardCode) {
					wardSelect.value = selectedWardCode;
					console.log('✅ Đã set xã:', selectedCustomerInfo.wardName);

					// 4. Tự động tính phí ship sau khi đã set đầy đủ địa chỉ
					calculateShippingFeeAfterAutoFill();
				}
			})
			.catch(err => {
				console.error('❌ Lỗi load wards:', err);
			});
	}

	// Tính phí ship sau khi auto fill xong
	function calculateShippingFeeAfterAutoFill() {
		const districtSelect = qs('#district');
		const wardSelect = qs('#ward');
		const cartKey = qs('#cartKey')?.value;

		if (!districtSelect.value || !wardSelect.value || !cartKey) return;

		const districtId = districtSelect.value;
		const wardCode = wardSelect.value;

		console.log('📦 Đang tính phí ship cho:', { districtId, wardCode, cartKey });

		fetchJson(`/admin/ban-hang/phi-ship?toDistrictId=${districtId}&wardCode=${wardCode}&cartKey=${cartKey}`)
			.then(fee => {
				const shippingFeeElement = qs('#shippingFee');
				if (shippingFeeElement) {
					shippingFeeElement.innerText = Number(fee).toLocaleString('vi-VN') + ' đ';
					console.log('✅ Đã cập nhật phí ship:', fee);
				}

				// Cập nhật tổng tiền sau giảm
				updateTongTienSauGiam(Number(fee || 0));
			})
			.catch(err => {
				console.error('❌ Lỗi tính phí ship:', err);
				const shippingFeeElement = qs('#shippingFee');
				if (shippingFeeElement) shippingFeeElement.innerText = 'Không thể tính phí';
			});
	}

	// CẬP NHẬT: Function để clear thông tin khi bỏ tick checkbox hoặc đổi khách hàng
	function clearCustomerAutoFill() {
		const tenInput = qs('#ten');
		const soDienThoaivcInput = qs('#soDienThoaivc');
		const ghichuInput = qs('#ghichu');
		const diaChiChiTietInput = qs('#diaChiChiTiet'); // THÊM FIELD MỚI
		const provinceSelect = qs('#province');
		const districtSelect = qs('#district');
		const wardSelect = qs('#ward');

		if (tenInput) tenInput.value = '';
		if (soDienThoaivcInput) soDienThoaivcInput.value = '';
		if (ghichuInput) ghichuInput.value = '';
		if (diaChiChiTietInput) diaChiChiTietInput.value = ''; // CLEAR ĐỊA CHỈ CHI TIẾT

		// Reset địa chỉ về mặc định
		if (provinceSelect) provinceSelect.selectedIndex = 0;
		if (districtSelect) {
			districtSelect.innerHTML = '<option value="">-- Chọn huyện --</option>';
		}
		if (wardSelect) {
			wardSelect.innerHTML = '<option value="">-- Chọn xã --</option>';
		}

		console.log('🧹 Đã clear thông tin auto fill');
	}

	// QUẢN LÝ CHECKBOX VẬN CHUYỂN MỚI
	function initShippingCheckbox() {
		const checkbox = qs('#muonVanChuyen');
		const diaChiDiv = qs('#diaChiVanChuyen');
		if (!checkbox || !diaChiDiv) return;

		const addressInputs = qsa('#province, #district, #ward, #ten, #soDienThoaivc, #ghichu, #diaChiChiTiet'); // THÊM ĐỊA CHỈ CHI TIẾT

		function setRequired(isRequired) {
			// Chỉ set required cho các input quan trọng
			const requiredInputs = qsa('#province, #district, #ward, #ten', diaChiDiv);
			requiredInputs.forEach((input) => {
				if (isRequired) input.setAttribute('required', 'required');
				else input.removeAttribute('required');
			});
		}

		function clearAddressSession() {
			const cartKey = qs('input[name="cartKey"]')?.value || 'gio-1';
			const { token, header } = getCsrf();

			fetch('/admin/ban-hang/xoa-dia-chi-session', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					...(token ? { [header]: token } : {})
				},
				body: JSON.stringify({ cartKey })
			}).then(response => {
				console.log('Đã xóa session địa chỉ và phí vận chuyển');

				// Cập nhật UI phí ship về 0
				const shippingFeeElement = qs('#shippingFee');
				if (shippingFeeElement) shippingFeeElement.innerText = 'Chưa tính';

				// Cập nhật lại tổng tiền
				updateTongTienSauGiam(0);
			}).catch(error => {
				console.error('Lỗi xóa session:', error);
			});
		}

		function clearAllAddressInputs() {
			addressInputs.forEach(input => {
				if (input.tagName === 'SELECT') {
					input.selectedIndex = 0; // Reset về option đầu tiên
				} else {
					input.value = '';
				}
			});
		}

		checkbox.addEventListener('change', function () {
			if (this.checked) {
				diaChiDiv.classList.remove('hidden');
				setRequired(true);

				// Auto fill thông tin khách hàng nếu đã chọn
				setTimeout(() => autoFillCustomerInfo(), 100); // Delay một chút để UI render
			} else {
				diaChiDiv.classList.add('hidden');
				setRequired(false);

				// Xóa tất cả dữ liệu trong form
				clearAllAddressInputs();

				// Xóa session địa chỉ và phí ship
				clearAddressSession();
			}
		});
	}

	// QUẢN LÝ DROPDOWN KHÁCH HÀNG MỚI - CẬP NHẬT VỚI AUTO FILL ĐỊA CHỈ
	function initCustomerDropdown() {
		const soDienThoaiInput = qs('#soDienThoai');
		const customerDropdown = qs('#customerDropdown');

		if (!soDienThoaiInput) return; // Nếu không có input thì dùng datalist cũ

		let customerData = []; // Sẽ được load từ server

		// Load danh sách khách hàng từ server
		function loadCustomerData() {
			fetchJson('/admin/ban-hang/danh-sach-khach-hang')
				.then(data => {
					customerData = data;
					renderCustomerDropdown(customerData);
				})
				.catch(err => {
					console.error('Lỗi load danh sách khách hàng:', err);
				});
		}

		// Render dropdown khách hàng
		function renderCustomerDropdown(customers) {
			if (!customerDropdown) return;

			customerDropdown.innerHTML = customers.map(kh => `
                <div class="customer-item" 
                     data-phone="${kh.soDienThoai}" 
                     data-name="${kh.ten}" 
                     data-email="${kh.email || ''}" 
                     data-diachi="${kh.diaChi || ''}"
                     data-id="${kh.id}">
                    <div class="customer-name">${kh.ten}</div>
                    <div class="customer-phone">${kh.soDienThoai}</div>
                    ${kh.email ? `<div class="text-xs text-gray-500">${kh.email}</div>` : ''}
                    ${kh.diaChi ? `<div class="text-xs text-gray-500">${kh.diaChi}</div>` : ''}
                </div>
            `).join('');
		}

		// Hiển thị dropdown khi focus
		soDienThoaiInput.addEventListener('focus', function () {
			if (customerDropdown && customerData.length > 0) {
				customerDropdown.classList.remove('hidden');
			}
		});

		// Lọc khách hàng theo input
		soDienThoaiInput.addEventListener('input', function () {
			const searchText = this.value.toLowerCase();

			// Reset thông tin khách hàng đã chọn khi người dùng thay đổi input
			selectedCustomerInfo = null;

			if (searchText.length === 0) {
				renderCustomerDropdown(customerData);
			} else {
				const filtered = customerData.filter(kh =>
					kh.ten.toLowerCase().includes(searchText) ||
					kh.soDienThoai.includes(searchText)
				);
				renderCustomerDropdown(filtered);
			}

			if (customerDropdown) customerDropdown.classList.remove('hidden');
		});

		// Chọn khách hàng - CẬP NHẬT VỚI LOAD CHI TIẾT
		if (customerDropdown) {
			customerDropdown.addEventListener('click', function (e) {
				const customerItem = e.target.closest('.customer-item');
				if (customerItem) {
					const customerId = customerItem.dataset.id;
					const phone = customerItem.dataset.phone;

					// Set input value ngay
					soDienThoaiInput.value = phone;
					customerDropdown.classList.add('hidden');

					console.log(`🔍 Đang load chi tiết khách hàng ID: ${customerId}`);

					// Load chi tiết khách hàng từ server (bao gồm parse địa chỉ)
					fetchJson(`/admin/ban-hang/khach-hang-chi-tiet/${customerId}`)
						.then(customerDetail => {
							selectedCustomerInfo = customerDetail;
							console.log(`✅ Đã load chi tiết khách hàng:`, selectedCustomerInfo);

							// Nếu checkbox vận chuyển đang được tích, auto fill ngay
							const checkbox = qs('#muonVanChuyen');
							if (checkbox && checkbox.checked) {
								setTimeout(() => autoFillCustomerInfo(), 100);
							}
						})
						.catch(err => {
							console.error('❌ Lỗi load chi tiết khách hàng:', err);
							// Fallback với thông tin cơ bản
							selectedCustomerInfo = {
								id: customerId,
								ten: customerItem.dataset.name,
								soDienThoai: phone,
								email: customerItem.dataset.email,
								diaChi: customerItem.dataset.diachi
							};
						});
				}
			});
		}

		// Ẩn dropdown khi click ngoài
		document.addEventListener('click', function (e) {
			if (customerDropdown &&
				!e.target.closest('#soDienThoai') &&
				!e.target.closest('#customerDropdown')) {
				customerDropdown.classList.add('hidden');
			}
		});

		// Load dữ liệu khách hàng khi khởi tạo
		loadCustomerData();
	}

	// Cập nhật tổng tiền sau khi thay đổi phí ship
	function updateTongTienSauGiam(phiShip) {
		const tongTienElement = qs('#tongTienHidden');
		const giamGiaElement = qs('#giamGiaHidden');
		const tongSauGiamDisplay = qs('#tongTienSauGiamDisplay');
		const tongSauGiamHidden = qs('#tongTienSauGiamHidden');

		if (tongTienElement && giamGiaElement) {
			const tongTien = parseFloat(tongTienElement.value || '0');
			const giamGia = parseFloat(giamGiaElement.value || '0');
			const tongSauGiam = tongTien - giamGia + phiShip;

			if (tongSauGiamDisplay) {
				tongSauGiamDisplay.innerText = tongSauGiam.toLocaleString('vi-VN') + ' đ';
			}
			if (tongSauGiamHidden) {
				tongSauGiamHidden.value = String(tongSauGiam);
			}
		}
	}

	// Tìm kiếm + gợi ý + bảng kết quả (giữ nguyên logic cũ)
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
            <tr onclick="chonSanPham('${sp.id}', '${sp.tenSanPham} - ${sp.mauSac} / ${sp.kichThuoc} - SL: ${sp.soLuong}')">
                <td class="p-3">${index + 1}</td>
                <td class="p-3">${sp.ma || ''}</td>
                <td class="p-3"><img src="${sp.hinhAnh}" alt="Ảnh" class="w-12 h-12 object-cover rounded"></td>
                <td class="p-3">${sp.tenSanPham} - ${sp.mauSac} / ${sp.kichThuoc}</td>
                <td class="p-3">${sp.soLuong}</td>
                <td class="p-3">${sp.giaBan}</td>
                <td class="p-3 text-center">
					${ (sp.trangThaiHoatDong && sp.soLuong > 0)
			? `<button class="btn btn-success px-3 py-1 rounded-md" onclick="themVaoGioHang('${sp.id}')">Thêm vào giỏ</button>`
			: (sp.trangThaiHoatDong && sp.soLuong === 0
				? `<span class="btn btn-warning px-3 py-1 rounded-md disabled">Hết hàng</span>`
				: `<span class="btn btn-danger px-3 py-1 rounded-md disabled">Ngừng bán</span>`)
		}
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
			alert('⚠ Thiếu cartKey!');
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
				alert('⚠ Không thể thêm sản phẩm vào giỏ: ' + err.message);
			});
	};

	window.validateSoLuong = function (form) {
		const input = form.querySelector('input[name="soLuong"]');
		const soLuongMoi = parseInt(input.value, 10);
		const soLuongTon = parseInt(input.getAttribute('max'), 10);
		if (Number.isFinite(soLuongTon) && soLuongMoi > soLuongTon) {
			alert('⚠ Số lượng vượt quá tồn kho: ' + soLuongTon);
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
			alert('⚠ Số lượng đã đạt tối đa tồn kho (' + max + ').');
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

	// CẬP NHẬT: updateDiaChiHienThi với địa chỉ chi tiết
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
				if (shippingFeeElement) shippingFeeElement.innerText = Number(fee).toLocaleString('vi-VN') + ' đ';

				// Cập nhật tổng tiền sau giảm
				updateTongTienSauGiam(Number(fee || 0));
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
			// Cho phép submit bình thường để server xử lý redirect/flash nếu cần
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

		// Khởi tạo các tính năng mới
		initShippingCheckbox();
		initCustomerDropdown();
		initAddress();
		initQuickCustomerForm();
		toggleQrImage();
	});

	// Thêm vào trang ban-hang.jsp (trong thẻ <script>)

// Kiểm tra xem có cần tự động in hóa đơn không
	window.addEventListener('DOMContentLoaded', function() {
		// Lấy tham số từ URL
		const urlParams = new URLSearchParams(window.location.search);
		const printInvoice = urlParams.get('printInvoice');
		const maHoaDon = urlParams.get('maHoaDon');

		if (printInvoice === 'true' && maHoaDon) {
			// Tự động mở PDF trong tab mới
			window.open('/admin/ban-hang/auto-print-pdf?maHoaDon=' + maHoaDon, '_blank');

			// Xóa tham số khỏi URL để tránh in lại khi refresh
			const newUrl = window.location.pathname;
			window.history.replaceState({}, document.title, newUrl);
		}
	});

// Hoặc nếu bạn muốn hiển thị popup xác nhận trước khi in:
	window.addEventListener('DOMContentLoaded', function() {
		const urlParams = new URLSearchParams(window.location.search);
		const printInvoice = urlParams.get('printInvoice');
		const maHoaDon = urlParams.get('maHoaDon');

		if (printInvoice === 'true' && maHoaDon) {
			// Hiển thị popup xác nhận
			if (confirm('Thanh toán thành công! Bạn có muốn in hóa đơn không?')) {
				window.open('/admin/ban-hang/auto-print-pdf?maHoaDon=' + maHoaDon, '_blank');
			}

			// Xóa tham số khỏi URL
			const newUrl = window.location.pathname;
			window.history.replaceState({}, document.title, newUrl);
		}
	});
})();