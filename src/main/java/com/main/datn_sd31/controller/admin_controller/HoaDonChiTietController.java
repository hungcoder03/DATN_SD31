package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.service.HoaDonChiTietService;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.LichSuHoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/hoa-don/detail1")
@RequiredArgsConstructor
public class HoaDonChiTietController {

    private final HoaDonChiTietService hoaDonChiTietService;

    private final HoaDonService hoaDonService;
    private final Chitietsanphamrepository chitietsanphamrepository;

    private final LichSuHoaDonService lichSuHoaDonService;

    @GetMapping("")
    public String detailHoaDon(
            @RequestParam(value = "ma-hoa-don") String maHoaDon,
            Model model
    ){
        model.addAttribute("lichSuList", lichSuHoaDonService.getLichSuHoaDonByHoaDon(maHoaDon));
        model.addAttribute("hoaDon", hoaDonService.getHoaDonByMa(maHoaDon));
        model.addAttribute("hdctList", hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(maHoaDon));
        model.addAttribute("maHoaDon", maHoaDon);
        return "admin/pages/hoa-don/hoa-don-detail";
    }

    @PostMapping("/cap-nhat-trang-thai")
    public String capNhatTrangThai(
            @RequestParam("ma-hoa-don") String maHoaDon,
            @RequestParam("trangThaiMoi") Integer trangThaiMoi,
            @RequestParam(value = "quayLui", required = false) Boolean quayLui,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes
    ) {
        // 1️⃣  Nếu chuyển sang XÁC NHẬN → trừ kho
        if (trangThaiMoi != null &&
                trangThaiMoi.equals(TrangThaiLichSuHoaDon.XAC_NHAN.getValue())) {

            var hdctList = hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(maHoaDon);

            for (var ct : hdctList) {
                Integer idCtsp = ct.getIdCTSP(); // <-- ID chi tiết sp từ hóa đơn

                var spctOptional = chitietsanphamrepository.findById(idCtsp);
                if (spctOptional.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm có ID: " + idCtsp);
                    redirectAttributes.addAttribute("ma-hoa-don", maHoaDon);
                    return "redirect:/admin/hoa-don/detail1";
                }

                var spct = spctOptional.get();
                int slDat = ct.getSoLuong();

                if (spct.getSoLuong() < slDat) {
                    redirectAttributes.addFlashAttribute("error",
                            "Sản phẩm \"" + spct.getSanPham().getTen() + "\" không đủ tồn kho!");
                    redirectAttributes.addAttribute("ma-hoa-don", maHoaDon);
                    return "redirect:/admin/hoa-don/detail1";
                }

                spct.setSoLuong(spct.getSoLuong() - slDat);
                chitietsanphamrepository.save(spct);
            }
        }

        // 2️⃣  Gọi service cập nhật trạng thái & lưu lịch sử như cũ
        lichSuHoaDonService.capNhatTrangThai(
                maHoaDon, trangThaiMoi, ghiChu, quayLui != null && quayLui);

        redirectAttributes.addAttribute("ma-hoa-don", maHoaDon);
        return "redirect:/admin/hoa-don/detail1";
    }




}