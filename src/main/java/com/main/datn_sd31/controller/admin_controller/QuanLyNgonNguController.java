package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.NgonNgu;
import com.main.datn_sd31.repository.NgonNguRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/ngon-ngu")
@RequiredArgsConstructor
public class QuanLyNgonNguController {
    
    private final NgonNguRepository ngonNguRepository;
    
    @GetMapping
    public String trangQuanLyNgonNgu(Model model) {
        List<NgonNgu> danhSachNgonNgu = ngonNguRepository.findAll();
        model.addAttribute("danhSachNgonNgu", danhSachNgonNgu);
        return "admin/pages/quan-ly-ngon-ngu";
    }
    
    @PostMapping("/luu")
    public String luuNgonNgu(@RequestParam String maNgonNgu,
                            @RequestParam String tenNgonNgu,
                            @RequestParam String tenBanDia,
                            @RequestParam(required = false) String duongDanCo,
                            @RequestParam(defaultValue = "false") Boolean trangThai,
                            @RequestParam(defaultValue = "false") Boolean macDinh,
                            @RequestParam(defaultValue = "0") Integer thuTu,
                            RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra nếu đặt làm mặc định, bỏ mặc định của ngôn ngữ khác
            if (macDinh) {
                ngonNguRepository.findByMacDinhTrue().ifPresent(ng -> {
                    ng.setMacDinh(false);
                    ngonNguRepository.save(ng);
                });
            }
            
            NgonNgu ngonNgu = NgonNgu.builder()
                .maNgonNgu(maNgonNgu)
                .tenNgonNgu(tenNgonNgu)
                .tenBanDia(tenBanDia)
                .duongDanCo(duongDanCo)
                .trangThai(trangThai)
                .macDinh(macDinh)
                .thuTu(thuTu)
                .build();
            
            ngonNguRepository.save(ngonNgu);
            redirectAttributes.addFlashAttribute("success", "Lưu ngôn ngữ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu ngôn ngữ: " + e.getMessage());
        }
        return "redirect:/admin/ngon-ngu";
    }
    
    @PostMapping("/cap-nhat/{id}")
    public String capNhatNgonNgu(@PathVariable Integer id,
                                @RequestParam String tenNgonNgu,
                                @RequestParam String tenBanDia,
                                @RequestParam(required = false) String duongDanCo,
                                @RequestParam(defaultValue = "false") Boolean trangThai,
                                @RequestParam(defaultValue = "false") Boolean macDinh,
                                @RequestParam(defaultValue = "0") Integer thuTu,
                                RedirectAttributes redirectAttributes) {
        try {
            NgonNgu ngonNgu = ngonNguRepository.findById(id).orElse(null);
            if (ngonNgu != null) {
                // Kiểm tra nếu đặt làm mặc định, bỏ mặc định của ngôn ngữ khác
                if (macDinh && !ngonNgu.getMacDinh()) {
                    ngonNguRepository.findByMacDinhTrue().ifPresent(ng -> {
                        if (!ng.getId().equals(ngonNgu.getId())) {
                        ng.setMacDinh(false);
                        ngonNguRepository.save(ng);
                    }
                    });
                }
                
                ngonNgu.setTenNgonNgu(tenNgonNgu);
                ngonNgu.setTenBanDia(tenBanDia);
                ngonNgu.setDuongDanCo(duongDanCo);
                ngonNgu.setTrangThai(trangThai);
                ngonNgu.setMacDinh(macDinh);
                ngonNgu.setThuTu(thuTu);
                
                ngonNguRepository.save(ngonNgu);
                redirectAttributes.addFlashAttribute("success", "Cập nhật ngôn ngữ thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy ngôn ngữ!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật ngôn ngữ: " + e.getMessage());
        }
        return "redirect:/admin/ngon-ngu";
    }
    
    @PostMapping("/xoa/{id}")
    public String xoaNgonNgu(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            NgonNgu ngonNgu = ngonNguRepository.findById(id).orElse(null);
            if (ngonNgu != null && ngonNgu.getMacDinh()) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa ngôn ngữ mặc định!");
            } else {
                ngonNguRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Xóa ngôn ngữ thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa ngôn ngữ: " + e.getMessage());
        }
        return "redirect:/admin/ngon-ngu";
    }
    
    @PostMapping("/doi-trang-thai/{id}")
    public String doiTrangThai(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            NgonNgu ngonNgu = ngonNguRepository.findById(id).orElse(null);
            if (ngonNgu != null) {
                if (ngonNgu.getMacDinh() && ngonNgu.getTrangThai()) {
                    redirectAttributes.addFlashAttribute("error", "Không thể tắt ngôn ngữ mặc định!");
                } else {
                    ngonNgu.setTrangThai(!ngonNgu.getTrangThai());
                    ngonNguRepository.save(ngonNgu);
                    redirectAttributes.addFlashAttribute("success", "Đổi trạng thái thành công!");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy ngôn ngữ!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đổi trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/ngon-ngu";
    }
}
