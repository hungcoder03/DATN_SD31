package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.service.ThongKeService;
import com.main.datn_sd31.util.ThongBaoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.main.datn_sd31.repository.HoaDonRepository;

@Controller
@RequestMapping("/admin/thong-ke")
@RequiredArgsConstructor
public class ThongKeController {

    private final ThongKeService thongkeService;
    private final HoaDonRepository hoaDonRepository;

    public static LocalDate[] getDateRange(String type) {
        LocalDate today = LocalDate.now();
        LocalDate start, end;

        switch (type.toLowerCase()) {
            case "ngay":
                start = today;
                end = today;
                break;

            case "tuan":
                // Start: Thứ 2 đầu tuần | End: Chủ nhật
                DayOfWeek dow = today.getDayOfWeek();
                start = today.minusDays(dow.getValue() - 1); // MONDAY = 1
                end = today.plusDays(7 - dow.getValue());
                break;

            case "thang":
                start = today.withDayOfMonth(1);
                end = today.withDayOfMonth(today.lengthOfMonth());
                break;

            case "nam":
                start = today.withDayOfYear(1);
                end = today.withDayOfYear(today.lengthOfYear());
                break;

            case "custom":
                // Trả về null hoặc today - today để tránh lỗi
                return new LocalDate[]{today, today}; // hoặc return null nếu bạn muốn controller xử lý

            default:
                throw new IllegalArgumentException("Loại thống kê không hợp lệ: " + type);
        }

        return new LocalDate[]{start, end};
    }

    @GetMapping("")
    public String thongKe(
            Model model,
            @RequestParam(value = "typeCalendar", defaultValue = "ngay") String type,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            RedirectAttributes redirectAttributes
    ) {
        LocalDate start;
        LocalDate end;

        if ("custom".equalsIgnoreCase(type)) {
            if (startDate == null || endDate == null) {
                ThongBaoUtils.addError(redirectAttributes, "Phải chọn đầy đủ ngày bắt đầu và ngày kết thúc");
            }
            start = startDate;
            end = endDate;
        } else {
            LocalDate[] range = getDateRange(type); // loại bỏ nguy cơ gọi với custom
            start = range[0];
            end = range[1];
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);

        model.addAttribute("doanhThu", thongkeService.getDoanhThu(startDateTime, endDateTime, 3));
        model.addAttribute("donThanhCong", thongkeService.countDonHang(startDateTime, endDateTime, 3));
        model.addAttribute("donHuy", thongkeService.countDonHang(startDateTime, endDateTime, 5));
        model.addAttribute("donTra", thongkeService.countDonHang(startDateTime, endDateTime, 4));
        model.addAttribute("tongSanPham", thongkeService.getTongSanPham(startDateTime, endDateTime));

        model.addAttribute("labelThoiGian", switch (type) {
            case "tuan" -> "trong tuần này";
            case "thang" -> "trong tháng này";
            case "nam" -> "trong năm nay";
            case "custom" -> "từ " + start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đến " + end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            default -> "hôm nay";
        });

        model.addAttribute("typeCalendar", type);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);

        model.addAttribute("sanPhamThongKeList", thongkeService.getThongKeSanPham(startDateTime, endDateTime));

        return "admin/pages/dashboard";
    }

    @GetMapping("/api/revenue-series")
    @ResponseBody
    public ResponseEntity<?> apiRevenueSeries(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "granularity", defaultValue = "day") String granularity
    ) {
        // Currently support day-level granularity
        LocalDate start = startDate;
        LocalDate end = endDate;
        var labels = new java.util.ArrayList<String>();
        var values = new java.util.ArrayList<java.math.BigDecimal>();

        LocalDate d = start;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM");
        while (!d.isAfter(end)) {
            LocalDateTime from = d.atStartOfDay();
            LocalDateTime to = d.atTime(23, 59, 59);
            java.math.BigDecimal doanhThu = thongkeService.getDoanhThu(from, to, 3);
            labels.add(d.format(df));
            values.add(doanhThu == null ? java.math.BigDecimal.ZERO : doanhThu);
            d = d.plusDays(1);
        }
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("labels", labels);
        body.put("values", values);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/api/orders-vs-revenue")
    @ResponseBody
    public ResponseEntity<?> apiOrdersVsRevenue(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "granularity", defaultValue = "day") String granularity
    ) {
        LocalDate start = startDate;
        LocalDate end = endDate;
        var labels = new java.util.ArrayList<String>();
        var orders = new java.util.ArrayList<Integer>();
        var revenue = new java.util.ArrayList<java.math.BigDecimal>();

        LocalDate d = start;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM");
        while (!d.isAfter(end)) {
            LocalDateTime from = d.atStartOfDay();
            LocalDateTime to = d.atTime(23, 59, 59);
            Integer soDon = thongkeService.countDonHang(from, to, 3);
            java.math.BigDecimal doanhThu = thongkeService.getDoanhThu(from, to, 3);
            labels.add(d.format(df));
            orders.add(soDon == null ? 0 : soDon);
            revenue.add(doanhThu == null ? java.math.BigDecimal.ZERO : doanhThu);
            d = d.plusDays(1);
        }
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("labels", labels);
        body.put("orders", orders);
        body.put("revenue", revenue);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/api/top-products")
    @ResponseBody
    public ResponseEntity<?> apiTopProducts(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        var list = thongkeService.getThongKeSanPham(startDate.atStartOfDay(), endDate.atTime(23,59,59));
        list.sort((a,b) -> Long.compare(b.getSoLuongDaBan(), a.getSoLuongDaBan()));
        if (list.size() > limit) list = list.subList(0, limit);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/api/top-customers")
    @ResponseBody
    public ResponseEntity<?> apiTopCustomers(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        var list = thongkeService.getTopCustomersByRevenue(startDate.atStartOfDay(), endDate.atTime(23,59,59), limit);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/api/profit-series")
    @ResponseBody
    public ResponseEntity<?> apiProfitSeries(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "granularity", defaultValue = "day") String granularity
    ) {
        LocalDate start = startDate;
        LocalDate end = endDate;
        var labels = new java.util.ArrayList<String>();
        var revenue = new java.util.ArrayList<java.math.BigDecimal>();
        var cogs = new java.util.ArrayList<java.math.BigDecimal>();
        var profit = new java.util.ArrayList<java.math.BigDecimal>();
        var margin = new java.util.ArrayList<Double>();

        LocalDate d = start;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM");
        while (!d.isAfter(end)) {
            LocalDateTime from = d.atStartOfDay();
            LocalDateTime to = d.atTime(23, 59, 59);
            // Lọc các đơn hoàn thành (trangThai=3) theo ngày hoàn thành (ngaySua)
            java.util.List<com.main.datn_sd31.entity.HoaDon> list = hoaDonRepository.findHoaDonByNgayAndTrangThai(from, to, 3);
            java.math.BigDecimal rev = java.math.BigDecimal.ZERO;
            java.math.BigDecimal cost = java.math.BigDecimal.ZERO;
            if (list != null) {
                for (com.main.datn_sd31.entity.HoaDon hd : list) {
                    if (hd.getThanhTien() != null) rev = rev.add(hd.getThanhTien());
                    if (hd.getHoaDonChiTiets() != null) {
                        for (com.main.datn_sd31.entity.HoaDonChiTiet ct : hd.getHoaDonChiTiets()) {
                            java.math.BigDecimal giaNhap = java.math.BigDecimal.ZERO;
                            if (ct.getChiTietSanPham() != null && ct.getChiTietSanPham().getGiaNhap() != null) {
                                giaNhap = ct.getChiTietSanPham().getGiaNhap();
                            }
                            java.math.BigDecimal lineCost = giaNhap.multiply(java.math.BigDecimal.valueOf(ct.getSoLuong() != null ? ct.getSoLuong() : 0));
                            cost = cost.add(lineCost);
                        }
                    }
                }
            }
            java.math.BigDecimal prof = rev.subtract(cost);
            double mar = rev.compareTo(java.math.BigDecimal.ZERO) > 0
                    ? prof.divide(rev, java.math.MathContext.DECIMAL64).doubleValue() * 100d
                    : 0d;

            labels.add(d.format(df));
            revenue.add(rev);
            cogs.add(cost);
            profit.add(prof);
            margin.add(mar);
            d = d.plusDays(1);
        }
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("labels", labels);
        body.put("revenue", revenue);
        body.put("cogs", cogs);
        body.put("profit", profit);
        body.put("margin", margin);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/api/order-channels")
    @ResponseBody
    public ResponseEntity<?> apiOrderChannels(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.atTime(23,59,59);
        var list = hoaDonRepository.findHoaDonByNgayAndTrangThai(from, to, 3);
        int trucTiep = 0;
        int online = 0;
        for (var hd : list) {
            String loai = hd.getLoaihoadon();
            String phuongThuc = hd.getPhuongThuc();
            String bucket = null;
            if (loai != null) {
                String lower = loai.trim().toLowerCase();
                if (lower.contains("online") || lower.contains("web") || lower.contains("website")) bucket = "online";
                if (lower.contains("truc") || lower.contains("tiep") || lower.contains("tai quay") || lower.contains("taiquay") || lower.contains("quay") || lower.contains("ban hang")) bucket = "offline";
            }
            if (bucket == null && phuongThuc != null) {
                String p = phuongThuc.trim().toLowerCase();
                // nếu thanh toán tiền mặt thường là bán trực tiếp
                if (p.contains("tien") && p.contains("mat")) bucket = "offline";
                if (p.contains("chuyen") || p.contains("ngan hang") || p.contains("vnpay") || p.contains("momo")) bucket = "online";
            }
            if (bucket == null) {
                // fallback: coi đơn không xác định là trực tiếp để không mất dữ liệu (có thể chỉnh sửa nếu cần)
                bucket = "offline";
            }
            if ("offline".equals(bucket)) trucTiep++; else online++;
        }
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("labels", java.util.Arrays.asList("Trực tiếp", "Online"));
        body.put("values", new int[]{trucTiep, online});
        return ResponseEntity.ok(body);
    }

    @GetMapping("/api/customer-channels")
    @ResponseBody
    public ResponseEntity<?> apiCustomerChannels(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.atTime(23,59,59);
        var list = hoaDonRepository.findHoaDonByNgayAndTrangThai(from, to, 3);
        java.util.Set<Integer> khOffline = new java.util.HashSet<>();
        java.util.Set<Integer> khOnline = new java.util.HashSet<>();
        for (var hd : list) {
            if (hd.getKhachHang() == null) continue;
            Integer khId = hd.getKhachHang().getId();
            String loai = hd.getLoaihoadon();
            String phuongThuc = hd.getPhuongThuc();
            String bucket = null;
            if (loai != null) {
                String lower = loai.trim().toLowerCase();
                if (lower.contains("online") || lower.contains("web") || lower.contains("website")) bucket = "online";
                if (lower.contains("truc") || lower.contains("tiep") || lower.contains("tai quay") || lower.contains("taiquay") || lower.contains("quay") || lower.contains("ban hang")) bucket = "offline";
            }
            if (bucket == null && phuongThuc != null) {
                String p = phuongThuc.trim().toLowerCase();
                if (p.contains("tien") && p.contains("mat")) bucket = "offline";
                if (p.contains("chuyen") || p.contains("ngan hang") || p.contains("vnpay") || p.contains("momo")) bucket = "online";
            }
            if (bucket == null) bucket = "offline";
            if ("offline".equals(bucket)) khOffline.add(khId); else khOnline.add(khId);
        }
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("labels", java.util.Arrays.asList("KH Trực tiếp", "KH Online"));
        body.put("values", new int[]{khOffline.size(), khOnline.size()});
        return ResponseEntity.ok(body);
    }


}
