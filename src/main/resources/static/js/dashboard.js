// dashboard.js - Khởi tạo 3 biểu đồ cốt lõi bằng Chart.js
// Giả định backend có API sau (sau có thể nối dữ liệu thật):
// /admin/api/analytics/revenue-series?from&to&granularity
// /admin/api/analytics/payment-methods?from&to
// /admin/api/analytics/orders-vs-revenue?from&to&granularity

(function () {
	function qs(sel) { return document.querySelector(sel); }
	function fetchJson(url) {
		return fetch(url, { credentials: 'same-origin' })
			.then(async r => {
				const ct = r.headers.get('content-type') || '';
				const text = await r.text();
				if (!r.ok) {
					throw new Error(`HTTP ${r.status} ${r.statusText}: ${text.substring(0,200)}`);
				}
				if (ct.includes('application/json')) {
					try { return JSON.parse(text); } catch (e) { throw new Error('JSON parse error: ' + e.message + ' | body: ' + text.substring(0,200)); }
				}
				// Nếu server trả về HTML (ví dụ redirect login) => điều hướng tới login
				if (ct.includes('text/html') || text.trim().startsWith('<!DOCTYPE') || text.trim().startsWith('<html')) {
					// cố gắng phát hiện trang đăng nhập admin
					if (text.includes('/admin/dang-nhap')) {
						window.location.href = '/admin/dang-nhap';
						return Promise.reject(new Error('Redirected to admin login'));
					}
					throw new Error('Expected JSON but received HTML.');
				}
				throw new Error('Unsupported content-type: ' + ct);
			});
	}
	function fmtMoney(v) { try { return Number(v).toLocaleString('vi-VN') + ' ₫'; } catch(e) { return v; } }

	// Helper màu
	const colors = {
		primary: '#4f46e5', // indigo-600
		primaryFill: 'rgba(79,70,229,0.15)',
		green: '#16a34a',
		red: '#dc2626',
		gray: '#64748b'
	};

	// Đọc khoảng thời gian hiện tại từ input (đã render server)
	function getDateRange() {
		let from = qs('#startDateInput')?.value;
		let to = qs('#endDateInput')?.value;
		if (!from || !to) {
			const today = new Date();
			const y = today.getFullYear();
			const m = String(today.getMonth()+1).padStart(2,'0');
			const d = String(today.getDate()).padStart(2,'0');
			from = `${y}-${m}-${d}`;
			to = `${y}-${m}-${d}`;
		}
		return { from, to };
	}

	// Charts instances
	let revenueChart, paymentMethodChart, ordersRevenueChart;
	let multiProfitChart;
	function initMultiLineProfitChart(labels, revenue, cogs, profit, margin) {
		const holder = qs('#revenueChart');
		if (!holder) return;
		// render into the same canvas or consider another canvas; here reuse revenueChart canvas by destroying it first
		if (revenueChart) { revenueChart.destroy(); revenueChart = null; }
		if (multiProfitChart) multiProfitChart.destroy();
		multiProfitChart = new Chart(holder, {
			type: 'line',
			data: {
				labels: labels,
				datasets: [
					{ label: 'Doanh thu', data: revenue, borderColor: '#2563eb', backgroundColor: 'rgba(37,99,235,0.1)', fill: false, yAxisID: 'y-axis-0', tension: 0.3 },
					{ label: 'Giá vốn', data: cogs, borderColor: '#dc2626', backgroundColor: 'rgba(220,38,38,0.1)', fill: false, yAxisID: 'y-axis-0', tension: 0.3 },
					{ label: 'Lợi nhuận', data: profit, borderColor: '#16a34a', backgroundColor: 'rgba(22,163,74,0.1)', fill: false, yAxisID: 'y-axis-0', tension: 0.3 },
					{ label: 'Biên lợi nhuận %', data: margin, borderColor: '#7c3aed', backgroundColor: 'rgba(124,58,237,0.1)', fill: false, yAxisID: 'y-axis-1', tension: 0.3 }
				]
			},
			options: {
				legend: { position: 'bottom' },
				elements: { point: { radius: 2 } },
				scales: {
					yAxes: [
						{ id: 'y-axis-0', position: 'left', ticks: { beginAtZero: true, callback: function(v){ return v.toLocaleString('vi-VN'); } } },
						{ id: 'y-axis-1', position: 'right', gridLines: { drawOnChartArea: false }, ticks: { beginAtZero: true, callback: function(v){ return v + '%'; } } }
					],
					xAxes: [{ ticks: { autoSkip: true } }]
				},
				tooltips: {
					mode: 'index', intersect: false,
					callbacks: {
						label: function(item, data){
							var dsLabel = data.datasets[item.datasetIndex].label || '';
							var val = item.yLabel;
							if (dsLabel.indexOf('%') >= 0) return dsLabel + ': ' + val.toFixed(2) + '%';
							return dsLabel + ': ' + Number(val).toLocaleString('vi-VN');
						}
					}
				}
			}
		});
	}

	function initRevenueChart(labels, values) {
		const ctx = qs('#revenueChart');
		if (!ctx) return;
		if (revenueChart) revenueChart.destroy();
		revenueChart = new Chart(ctx, {
			type: 'line',
			data: {
				labels,
				datasets: [{
					label: 'Doanh thu',
					data: values,
					borderColor: colors.primary,
					backgroundColor: colors.primaryFill,
					tension: 0.3,
					fill: true,
					pointRadius: 2
				}]
			},
			options: {
				plugins: { legend: { display: false } },
				scales: {
					x: { ticks: { color: colors.gray } },
					y: { ticks: { color: colors.gray }, beginAtZero: true }
				}
			}
		});
	}

	function initPaymentMethodChart(labels, values) {
		const ctx = qs('#paymentMethodChart');
		if (!ctx) return;
		if (paymentMethodChart) paymentMethodChart.destroy();
		paymentMethodChart = new Chart(ctx, {
			type: 'doughnut',
			data: {
				labels,
				datasets: [{
					data: values,
					backgroundColor: ['#4f46e5', '#16a34a', '#f59e0b', '#ef4444'],
					borderWidth: 0
				}]
			},
			options: {
				plugins: { legend: { position: 'bottom' } },
				cutout: '60%'
			}
		});
	}

	function initOrdersRevenueChart(labels, orders, revenue) {
		const ctx = qs('#ordersRevenueChart');
		if (!ctx) return;
		if (ordersRevenueChart) ordersRevenueChart.destroy();
		ordersRevenueChart = new Chart(ctx, {
			type: 'bar',
			data: {
				labels: labels,
				datasets: [
					{
						label: 'Số đơn',
						data: orders,
						backgroundColor: 'rgba(79,70,229,0.4)',
						yAxisID: 'y-axis-0'
					},
					{
						label: 'Doanh thu',
						data: revenue,
						type: 'line',
						borderColor: colors.green,
						backgroundColor: 'rgba(22,163,74,0.15)',
						tension: 0.3,
						yAxisID: 'y-axis-1'
					}
				]
			},
			options: {
				legend: { position: 'bottom' },
				scales: {
					yAxes: [
						{ id: 'y-axis-0', position: 'left', ticks: { beginAtZero: true } },
						{ id: 'y-axis-1', position: 'right', ticks: { beginAtZero: true }, gridLines: { drawOnChartArea: false } }
					],
					xAxes: [{ ticks: { autoSkip: true } }]
				}
			}
		});
	}

	async function loadTopProducts() {
		const from = qs('#startDateInput')?.value;
		const to = qs('#endDateInput')?.value;
		const body = qs('#topProductsBody');
		if (!body) return;
		body.innerHTML = '<tr><td class="p-3" colspan="5">Đang tải...</td></tr>';
		try {
			const data = await fetchJson(`/admin/thong-ke/api/top-products?startDate=${from}&endDate=${to}&limit=10`);
			if (!data || data.length === 0) {
				body.innerHTML = '<tr><td class="p-3 text-center text-gray-500" colspan="5">Không có dữ liệu</td></tr>';
				return;
			}
			body.innerHTML = data.map((sp, idx) => `
				<tr class="${sp.soLuongTon === 0 ? 'bg-red-50' : ''}">
					<td class="p-3">${idx + 1}</td>
					<td class="p-3">${sp.maSp || ''}</td>
					<td class="p-3">${sp.tenCt || ''}</td>
					<td class="p-3">${sp.soLuongDaBan || 0}</td>
					<td class="p-3">${sp.soLuongTon ?? ''}</td>
				</tr>
			`).join('');
		} catch (e) {
			console.error('Lỗi tải top products:', e);
			body.innerHTML = '<tr><td class="p-3 text-red-600" colspan="5">Lỗi tải dữ liệu</td></tr>';
		}
	}

	async function loadTopCustomers() {
		const from = qs('#startDateInput')?.value;
		const to = qs('#endDateInput')?.value;
		const body = qs('#topCustomersBody');
		if (!body) return;
		body.innerHTML = '<tr><td class="p-3" colspan="4">Đang tải...</td></tr>';
		try {
			const data = await fetchJson(`/admin/thong-ke/api/top-customers?startDate=${from}&endDate=${to}&limit=10`);
			if (!data || data.length === 0) {
				body.innerHTML = '<tr><td class="p-3 text-center text-gray-500" colspan="4">Không có dữ liệu</td></tr>';
				return;
			}
			body.innerHTML = data.map((kh, idx) => `
				<tr>
					<td class="p-3">${idx + 1}</td>
					<td class="p-3">${kh.ten || ''}</td>
					<td class="p-3">${kh.soDon || 0}</td>
					<td class="p-3">${fmtMoney(kh.tongChi || 0)}</td>
				</tr>
			`).join('');
		} catch (e) {
			console.error('Lỗi tải top customers:', e);
			body.innerHTML = '<tr><td class="p-3 text-red-600" colspan="4">Lỗi tải dữ liệu</td></tr>';
		}
	}

	async function loadAndRender() {
		const { from, to } = getDateRange();
		try {
			const pf = await fetchJson(`/admin/thong-ke/api/profit-series?startDate=${from}&endDate=${to}&granularity=day`);
			const labels = pf.labels || [];
			const revenueValues = (pf.revenue || []).map(v => Number(v));
			const cogsValues = (pf.cogs || []).map(v => Number(v));
			const profitValues = (pf.profit || []).map(v => Number(v));
			const marginValues = (pf.margin || []).map(v => Number(v));
			initRevenueChart(labels, revenueValues);
			// Replace revenue chart with multi-series line: revenue, cogs, profit, margin%
			initMultiLineProfitChart(labels, revenueValues, cogsValues, profitValues, marginValues);

			// Order channels (Trực tiếp vs Online)
			// Customer segments by order channel
			const ch = await fetchJson(`/admin/thong-ke/api/order-channels?startDate=${from}&endDate=${to}`);
			initPaymentMethodChart(ch.labels || ['KH Trực tiếp','KH Online'], ch.values || [0,0]);

			const ov = await fetchJson(`/admin/thong-ke/api/orders-vs-revenue?startDate=${from}&endDate=${to}&granularity=day`);
			const orders = ov.orders || [];
			const revenue = (ov.revenue || []).map(v => typeof v === 'number' ? v : Number(v));
			initOrdersRevenueChart(ov.labels || labels, orders, revenue);

			await Promise.all([loadTopProducts(), loadTopCustomers()]);
		} catch (e) {
			console.error('Lỗi tải dữ liệu dashboard:', e);
		}
	}

	document.addEventListener('DOMContentLoaded', loadAndRender);
})();