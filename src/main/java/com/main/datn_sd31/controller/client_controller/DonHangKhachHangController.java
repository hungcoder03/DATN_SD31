package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.dto.Pagination;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.service.HoaDonChiTietService;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.LichSuHoaDonService;
import com.main.datn_sd31.util.GetKhachHang;
import com.main.datn_sd31.util.GetNhanVien;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/khach-hang/don-hang/{id}")
@RequiredArgsConstructor
public class DonHangKhachHangController {

    private final GetNhanVien getNhanVien;

    private final HoaDonService hoaDonService;

    private final HoaDonChiTietService hoaDonChiTietService;

    private final LichSuHoaDonService lichSuHoaDonService;

    private final GetKhachHang getKhachHang;


    @GetMapping("")
    public String hoaDon(
            @PathVariable("id") Integer id,
            Model model,
            @RequestParam(name = "trang-thai", required = false) TrangThaiLichSuHoaDon trangThai,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Integer currentId = getKhachHang.getCurrentKhachHang().getId();
        if (!id.equals(currentId)) {
            return "admin/error/404";  // hoặc redirect
        }
        model.addAttribute("idKhachHang", currentId);

        //Giá tri mac định
        if (startDate == null) {
            startDate = LocalDate.of(2025, 1, 1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Pagination<HoaDonDTO> hoaDonList = (trangThai == null)
                ? hoaDonService.getAllDonHangKhachHang(getKhachHang.getCurrentKhachHang() ,page, size, startDate, endDate)
                : hoaDonService.getAllHoaDonKhachHangByStatus(getKhachHang.getCurrentKhachHang(), trangThai, page, size);
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

        return "khachhang/lich-su-mua-hang";
    }

    @GetMapping("/search")
    public String searchHoaDon(
            @PathVariable("id") Integer id,
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Integer currentId = getKhachHang.getCurrentKhachHang().getId();
        if (!id.equals(currentId)) {
            return "admin/error/404";  // hoặc redirect
        }
        model.addAttribute("idKhachHang", currentId);

        if (keyword == null || keyword.trim().isEmpty()) {
            return "redirect:/admin/hoa-don";
        }

        Pagination<HoaDonDTO> hoaDonList = hoaDonService.searchByKeyword(keyword, page, size);

        model.addAttribute("hoaDonList", hoaDonList.getContent());
        model.addAttribute("pageInfo", hoaDonList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount(hoaDonList.getContent()));

        return "khachhang/lich-su-mua-hang";
    }

//    @GetMapping("/detail")
//    public String getHoaDonDetail(
//            @PathVariable("id") Integer id,
//            @RequestParam("ma") String ma,
//            Model model
//    ) {
//        Integer currentId = getKhachHang.getCurrentKhachHang().getId();
//        if (!id.equals(currentId)) {
//            return "admin/error/404";  // hoặc redirect
//        }
//        model.addAttribute("idKhachHang", currentId);
//
//        model.addAttribute("hoaDonDetail", hoaDonService.getHoaDonByMa(ma));
//        model.addAttribute("hdctList", hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(ma));
//        return "/admin/pages/hoa-don/hoa-don-detail-modal";
//    }

//    @PostMapping("/cap-nhat-trang-thai")
//    public String capNhatTrangThai(
//            @PathVariable("id") Integer id,
//            @RequestParam("ma-hoa-don") String maHoaDon,
//            @RequestParam(value = "trangThaiMoi", required = false) Integer trangThaiMoi,
////            @RequestParam(value = "quayLui", required = false) Boolean quayLui,
//            @RequestParam(value = "ghiChu", required = false) String ghiChu,
//            RedirectAttributes redirectAttributes
//    ) {
//        Integer currentId = getKhachHang.getCurrentKhachHang().getId();
//        if (!id.equals(currentId)) {
//            return "admin/error/404";  // hoặc redirect
//        }
//        model.addAttribute("idKhachHang", currentId);
//
//        var ketQua = lichSuHoaDonService.xuLyCapNhatTrangThai(
//                maHoaDon,
//                trangThaiMoi,
////                quayLui,
//                ghiChu,
//                getNhanVien.getCurrentNhanVien()
//        );
//
//        //Sử dụng thông báo
//        if (ketQua.thanhCong()) {
//            ThongBaoUtils.addSuccess(redirectAttributes, ketQua.message());
//        } else {
//            ThongBaoUtils.addError(redirectAttributes, ketQua.message());
//        }
//
//        redirectAttributes.addAttribute("ma-hoa-don", maHoaDon);
//
//        return "redirect:/admin/hoa-don";
//    }
}
