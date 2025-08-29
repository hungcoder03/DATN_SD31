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

    @GetMapping("")
    public String hoaDon(
            Model model,
            @RequestParam(name = "trang-thai", required = false) TrangThaiLichSuHoaDon trangThai,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        //Giá tri mac định
        if (startDate == null) {
            startDate = LocalDate.of(2025, 1, 1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Pagination<HoaDonDTO> hoaDonList = (trangThai == null)
                ? hoaDonService.getAll(page, size, startDate, endDate)
                : hoaDonService.getAllHoaDonByStatus(trangThai, page, size);
        model.addAttribute("hoaDonList", hoaDonList.getContent());
        model.addAttribute("pageInfo", hoaDonList);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount(hoaDonList.getContent()));

        Map<String, List<TrangThaiLichSuHoaDon>> trangThaiHopLeMap = new HashMap<>();

        for (HoaDonDTO hd : hoaDonList.getContent()) {
            trangThaiHopLeMap.put(hd.getMa(), lichSuHoaDonService.getTrangThaiTiepTheoHopLe(hd.getTrangThaiLichSuHoaDon(), hd));
        }

        model.addAttribute("trangThaiHopLeMap", trangThaiHopLeMap);
        model.addAttribute("selectedStatus", trangThai);

        return "admin/pages/hoa-don/hoa-don";
    }

    @GetMapping("/search")
    public String searchHoaDon(
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "redirect:/admin/hoa-don";
        }

        Pagination<HoaDonDTO> hoaDonList = hoaDonService.searchByKeyword(keyword, page, size);

        model.addAttribute("hoaDonList", hoaDonList.getContent());
        model.addAttribute("pageInfo", hoaDonList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount(hoaDonList.getContent()));

        Map<String, List<TrangThaiLichSuHoaDon>> trangThaiHopLeMap = new HashMap<>();
        for (HoaDonDTO hd : hoaDonList.getContent()) {
            trangThaiHopLeMap.put(hd.getMa(), lichSuHoaDonService.getTrangThaiTiepTheoHopLe(hd.getTrangThaiLichSuHoaDon(), hd));
        }
        model.addAttribute("trangThaiHopLeMap", trangThaiHopLeMap);

        return "admin/pages/hoa-don/hoa-don";
    }

    @GetMapping("/filter")
    public String searchHoaDonByLoaiHoaDon(
            Model model,
            @RequestParam(value = "loaiHoaDon", required = false, defaultValue = "") String loaiHoaDon,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        if (loaiHoaDon == null || loaiHoaDon.trim().isEmpty()) {
            return "redirect:/admin/hoa-don";
        }

        Pagination<HoaDonDTO> hoaDonList = hoaDonService.searchByLoaiHoaDon(loaiHoaDon, page, size);

        model.addAttribute("hoaDonList", hoaDonList.getContent());
        model.addAttribute("pageInfo", hoaDonList);
        model.addAttribute("loaiHoaDon", loaiHoaDon);
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount(hoaDonList.getContent()));

        Map<String, List<TrangThaiLichSuHoaDon>> trangThaiHopLeMap = new HashMap<>();
        for (HoaDonDTO hd : hoaDonList.getContent()) {
            trangThaiHopLeMap.put(hd.getMa(), lichSuHoaDonService.getTrangThaiTiepTheoHopLe(hd.getTrangThaiLichSuHoaDon(), hd));
        }
        model.addAttribute("trangThaiHopLeMap", trangThaiHopLeMap);

        return "admin/pages/hoa-don/hoa-don";
    }

//    @GetMapping("/detail")
//    public String getHoaDonDetail(
//            @RequestParam("ma") String ma,
//            Model model
//    ) {
//        model.addAttribute("hoaDonDetail", hoaDonService.getHoaDonByMa(ma));
//        model.addAttribute("hdctList", hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(ma));
//        return "/admin/pages/hoa-don/hoa-don-detail-modal";
//    }

    @PostMapping("/cap-nhat-trang-thai")
    public String capNhatTrangThai(
            @RequestParam("maHoaDon") String maHoaDon,
            @RequestParam(value = "trangThaiMoi", required = false) Integer trangThaiMoi,
//            @RequestParam(value = "lyDoGiaoKhongThanhCong", required = false) Integer lyDoGiaoKhongThanhCong,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes
    ) {
        var ketQua = lichSuHoaDonService.xuLyCapNhatTrangThai(
                maHoaDon,
                trangThaiMoi,
//                lyDoGiaoKhongThanhCong,
                ghiChu,
                getNhanVien.getCurrentNhanVien()
        );

        //Sử dụng thông báo
        if (ketQua.thanhCong()) {
            ThongBaoUtils.addSuccess(redirectAttributes, ketQua.message());
        } else {
            ThongBaoUtils.addError(redirectAttributes, ketQua.message());
        }

        redirectAttributes.addAttribute("maHoaDon", maHoaDon);

        return "redirect:/admin/hoa-don";
    }

}