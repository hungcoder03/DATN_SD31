package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.dto.Pagination;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.service.HoaDonChiTietService;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.LichSuHoaDonService;
import com.main.datn_sd31.util.GetNhanVien;
import com.main.datn_sd31.util.ThongBaoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/don-hang")
@RequiredArgsConstructor
public class DonHangController {

    private final GetNhanVien getNhanVien;
    private final HoaDonService hoaDonService;
    private final HoaDonChiTietService hoaDonChiTietService;
    private final LichSuHoaDonService lichSuHoaDonService;



    @GetMapping("")
    public String hoaDon(
            Model model,
            @RequestParam(name = "trang-thai", required = false) TrangThaiLichSuHoaDon trangThai,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "loaiHoaDon", required = false) String loaiHoaDon
    ) {
//        // Xử lý và validate ngày tháng
//        LocalDate startDate = parseDate(startDateStr, LocalDate.of(2025, 1, 1));
//        LocalDate endDate = parseDate(endDateStr, LocalDate.now());

        // Gán giá trị mặc định nếu không có
        if (startDate == null) {
            startDate = LocalDate.of(2025, 1, 1); // ví dụ mặc định từ đầu năm
        }
        if (endDate == null) {
            endDate = LocalDate.now(); // mặc định đến hôm nay
        }

        Pagination<HoaDonDTO> hoaDonList;

        // Xử lý tìm kiếm theo keyword
        if (keyword != null && !keyword.trim().isEmpty()) {
            hoaDonList = hoaDonService.searchByKeyword(keyword, page, size);
            model.addAttribute("keyword", keyword);
        }
        // Xử lý filter theo loại hóa đơn
        else if (loaiHoaDon != null && !loaiHoaDon.trim().isEmpty()) {
            hoaDonList = hoaDonService.searchByLoaiDonHang(loaiHoaDon, page, size);
            model.addAttribute("loaiHoaDon", loaiHoaDon);
        }
        // Xử lý filter theo trạng thái
        else if (trangThai != null) {
            hoaDonList = hoaDonService.getAllDonHangByStatus(trangThai, page, size);
        }
        // Hiển thị tất cả với filter theo ngày
        else {
            hoaDonList = hoaDonService.getAllDonHang(page, size, startDate, endDate);
        }

        // Thiết lập các attribute cho model
        model.addAttribute("hoaDonList", hoaDonList.getContent());
        model.addAttribute("pageInfo", hoaDonList);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount(hoaDonService.getAllHoaDon()));

        // Tạo map trạng thái hợp lệ cho từng hóa đơn
        Map<String, List<TrangThaiLichSuHoaDon>> trangThaiHopLeMap = new HashMap<>();
        for (HoaDonDTO hd : hoaDonList.getContent()) {
            trangThaiHopLeMap.put(hd.getMa(), lichSuHoaDonService.getTrangThaiTiepTheoHopLe(hd.getTrangThaiLichSuHoaDon(), hd));
        }
        model.addAttribute("trangThaiHopLeMap", trangThaiHopLeMap);

        return "admin/pages/don-hang/don-hang";
    }

    /**
     * Helper method để parse string thành LocalDate với xử lý lỗi an toàn
     */
    private LocalDate parseDate(String dateStr, LocalDate defaultValue) {
        if (dateStr == null || dateStr.trim().isEmpty() || "undefined".equals(dateStr)) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // Redirect các endpoint cũ về method chính
    @GetMapping("/search")
    public String searchHoaDon(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "redirect:/admin/don-hang";
        }

        StringBuilder redirectUrl = new StringBuilder("redirect:/admin/don-hang?keyword=" + keyword + "&page=" + page + "&size=" + size);

        if (startDateStr != null && !startDateStr.trim().isEmpty() && !"undefined".equals(startDateStr)) {
            redirectUrl.append("&startDate=").append(startDateStr);
        }
        if (endDateStr != null && !endDateStr.trim().isEmpty() && !"undefined".equals(endDateStr)) {
            redirectUrl.append("&endDate=").append(endDateStr);
        }

        return redirectUrl.toString();
    }

    @GetMapping("/filter")
    public String searchHoaDonByLoaiHoaDon(
            @RequestParam(value = "loaiHoaDon", required = false, defaultValue = "") String loaiHoaDon,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        if (loaiHoaDon == null || loaiHoaDon.trim().isEmpty()) {
            return "redirect:/admin/don-hang";
        }

        StringBuilder redirectUrl = new StringBuilder("redirect:/admin/don-hang?loaiHoaDon=" + loaiHoaDon + "&page=" + page + "&size=" + size);

        if (startDateStr != null && !startDateStr.trim().isEmpty() && !"undefined".equals(startDateStr)) {
            redirectUrl.append("&startDate=").append(startDateStr);
        }
        if (endDateStr != null && !endDateStr.trim().isEmpty() && !"undefined".equals(endDateStr)) {
            redirectUrl.append("&endDate=").append(endDateStr);
        }

        return redirectUrl.toString();
    }

    @PostMapping("/cap-nhat-trang-thai")
    public String capNhatTrangThai(
            @RequestParam("maHoaDon") String maHoaDon,
            @RequestParam(value = "trangThaiMoi", required = false) Integer trangThaiMoi,
            @RequestParam(value = "lyDoGiaoKhongThanhCong", required = false) Integer lyDoGiaoKhongThanhCong,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes
    ) {
        var ketQua = lichSuHoaDonService.xuLyCapNhatTrangThai(
                maHoaDon,
                trangThaiMoi,
                ghiChu,
                getNhanVien.getCurrentNhanVien()
        );

        // Sử dụng thông báo
        if (ketQua.thanhCong()) {
            ThongBaoUtils.addSuccess(redirectAttributes, ketQua.message());
        } else {
            ThongBaoUtils.addError(redirectAttributes, ketQua.message());
        }

        redirectAttributes.addAttribute("maHoaDon", maHoaDon);

        return "redirect:/admin/don-hang";
    }
}