package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.dto.Pagination;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.service.HoaDonChiTietService;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.LichSuHoaDonService;
import com.main.datn_sd31.util.GetNhanVien;
import com.main.datn_sd31.util.ThongBaoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/admin/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final GetNhanVien getNhanVien;
    private final HoaDonService hoaDonService;
    private final HoaDonChiTietService hoaDonChiTietService;
    private final LichSuHoaDonService lichSuHoaDonService;

    @GetMapping({"", "/search", "/filter"})
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
        // Giá trị mặc định cho ngày tháng
        if (startDate == null) {
            startDate = LocalDate.of(2025, 1, 1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Pagination<HoaDonDTO> hoaDonList;

        // Logic xử lý theo từng trường hợp
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Trường hợp tìm kiếm theo keyword
            hoaDonList = hoaDonService.searchByKeyword(keyword, page, size);
            model.addAttribute("keyword", keyword);
        } else if (loaiHoaDon != null && !loaiHoaDon.trim().isEmpty()) {
            // Trường hợp filter theo loại hóa đơn
            hoaDonList = hoaDonService.searchByLoaiHoaDon(loaiHoaDon, page, size);
            model.addAttribute("loaiHoaDon", loaiHoaDon);
        } else if (trangThai != null) {
            // Trường hợp filter theo trạng thái
            hoaDonList = hoaDonService.getAllHoaDonByStatus(trangThai, page, size);
        } else {
            // Trường hợp mặc định - lấy tất cả theo ngày tháng
            hoaDonList = hoaDonService.getAll(page, size, startDate, endDate);
        }

        // Thêm dữ liệu vào model
        model.addAttribute("hoaDonList", hoaDonList.getContent());
        model.addAttribute("pageInfo", hoaDonList);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount(hoaDonList.getContent()));
        model.addAttribute("selectedStatus", trangThai);

        // Tạo map trạng thái hợp lệ cho từng hóa đơn
        Map<String, List<TrangThaiLichSuHoaDon>> trangThaiHopLeMap = new HashMap<>();
        for (HoaDonDTO hd : hoaDonList.getContent()) {
            trangThaiHopLeMap.put(hd.getMa(),
                    lichSuHoaDonService.getTrangThaiTiepTheoHopLe(hd.getTrangThaiLichSuHoaDon(), hd));
        }
        model.addAttribute("trangThaiHopLeMap", trangThaiHopLeMap);

        return "admin/pages/hoa-don/hoa-don";
    }

    @PostMapping("/cap-nhat-trang-thai")
    public String capNhatTrangThai(
            @RequestParam("maHoaDon") String maHoaDon,
            @RequestParam(value = "trangThaiMoi", required = false) Integer trangThaiMoi,
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
        return "redirect:/admin/hoa-don";
    }
}