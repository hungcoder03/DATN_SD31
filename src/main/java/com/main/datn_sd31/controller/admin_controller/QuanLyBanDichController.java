package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.BanDich;
import com.main.datn_sd31.entity.NgonNgu;
import com.main.datn_sd31.service.DichVuBanDichService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/ban-dich")
@RequiredArgsConstructor
public class QuanLyBanDichController {
    
    private final DichVuBanDichService dichVuBanDichService;
    
    @GetMapping
    public String trangQuanLyBanDich(Model model) {
        List<NgonNgu> danhSachNgonNgu = dichVuBanDichService.layDanhSachNgonNguHoatDong();
        model.addAttribute("danhSachNgonNgu", danhSachNgonNgu);
        return "admin/pages/quan-ly-ban-dich";
    }
    
    @GetMapping("/theo-ngon-ngu/{maNgonNgu}")
    public String layBanDichTheoNgonNgu(@PathVariable String maNgonNgu, Model model) {
        List<BanDich> danhSachBanDich = dichVuBanDichService.layTatCaBanDichTheoNgonNgu(maNgonNgu);
        model.addAttribute("danhSachBanDich", danhSachBanDich);
        model.addAttribute("maNgonNgu", maNgonNgu);
        return "admin/pages/ban-dich-theo-ngon-ngu";
    }
    
    @PostMapping("/luu")
    public String luuBanDich(@RequestParam String tenKhoa,
                            @RequestParam String maNgonNgu,
                            @RequestParam String noiDung,
                            @RequestParam String danhMuc,
                            RedirectAttributes redirectAttributes) {
        try {
            dichVuBanDichService.luuBanDich(tenKhoa, maNgonNgu, noiDung, danhMuc);
            redirectAttributes.addFlashAttribute("success", "Lưu bản dịch thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu bản dịch: " + e.getMessage());
        }
        return "redirect:/admin/ban-dich";
    }
    
    @PostMapping("/cap-nhat/{id}")
    public String capNhatBanDich(@PathVariable Integer id,
                                @RequestParam String noiDung,
                                RedirectAttributes redirectAttributes) {
        try {
            BanDich banDich = dichVuBanDichService.capNhatBanDich(id, noiDung);
            if (banDich != null) {
                redirectAttributes.addFlashAttribute("success", "Cập nhật bản dịch thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy bản dịch!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật bản dịch: " + e.getMessage());
        }
        return "redirect:/admin/ban-dich";
    }
    
    @PostMapping("/xoa/{id}")
    public String xoaBanDich(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            dichVuBanDichService.xoaBanDich(id);
            redirectAttributes.addFlashAttribute("success", "Xóa bản dịch thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa bản dịch: " + e.getMessage());
        }
        return "redirect:/admin/ban-dich";
    }
    
    @PostMapping("/dich-ai")
    public String dichBangAi(@RequestParam String vanBan,
                            @RequestParam String ngonNguNguon,
                            @RequestParam String ngonNguDich,
                            @RequestParam String tenKhoa,
                            @RequestParam String danhMuc,
                            RedirectAttributes redirectAttributes) {
        try {
            String banDich = dichVuBanDichService.dichVanBanDong(vanBan, ngonNguNguon, ngonNguDich);
            if (banDich != null && !banDich.equals(vanBan)) {
                dichVuBanDichService.luuBanDich(tenKhoa, ngonNguDich, banDich, danhMuc);
                redirectAttributes.addFlashAttribute("success", "Dịch và lưu thành công!");
                redirectAttributes.addFlashAttribute("banDich", banDich);
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể dịch văn bản này!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi dịch: " + e.getMessage());
        }
        return "redirect:/admin/ban-dich";
    }
    
    @PostMapping("/lam-moi-cache")
    public String lamMoiCache(RedirectAttributes redirectAttributes) {
        try {
            dichVuBanDichService.lamMoiCache();
            redirectAttributes.addFlashAttribute("success", "Làm mới cache thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi làm mới cache: " + e.getMessage());
        }
        return "redirect:/admin/ban-dich";
    }
}
