package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.SpYeuThich;
import com.main.datn_sd31.repository.SpYeuThichRepository;
import com.main.datn_sd31.service.impl.KhachHangServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/khach-hang/yeu-thich")
public class WishlistController {

    private final SpYeuThichRepository spYeuThichRepository;
    private final KhachHangServiceImpl khachHangService;

    /**
     * Xóa một sản phẩm khỏi danh sách yêu thích
     */
    @PostMapping("/toggle")
    public String toggleWishlist(@RequestParam Integer productId, 
                                Principal principal, 
                                RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thực hiện thao tác này");
            return "redirect:/khach-hang/dang-nhap";
        }

        try {
            KhachHang khachHang = khachHangService.findByEmail(principal.getName());
            if (khachHang == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng");
                return "redirect:/khach-hang/dang-nhap";
            }

            // Tìm sản phẩm yêu thích
            Optional<SpYeuThich> spYeuThichOpt = spYeuThichRepository
                .findBySanPham_IdAndKhachHang_Id(productId, khachHang.getId());

            if (spYeuThichOpt.isPresent()) {
                // Xóa khỏi danh sách yêu thích
                spYeuThichRepository.delete(spYeuThichOpt.get());
                redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi danh sách yêu thích");
            } else {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không có trong danh sách yêu thích");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/khach-hang/yeu-thich";
    }

    /**
     * Xóa nhiều sản phẩm khỏi danh sách yêu thích
     */
    @PostMapping("/xoa-nhieu")
    public String removeMultipleWishlist(@RequestParam List<Integer> productIds, 
                                       Principal principal, 
                                       RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thực hiện thao tác này");
            return "redirect:/khach-hang/dang-nhap";
        }

        try {
            KhachHang khachHang = khachHangService.findByEmail(principal.getName());
            if (khachHang == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng");
                return "redirect:/khach-hang/dang-nhap";
            }

            // Xóa các sản phẩm yêu thích
            int deletedCount = 0;
            for (Integer productId : productIds) {
                Optional<SpYeuThich> spYeuThichOpt = spYeuThichRepository
                    .findBySanPham_IdAndKhachHang_Id(productId, khachHang.getId());
                
                if (spYeuThichOpt.isPresent()) {
                    spYeuThichRepository.delete(spYeuThichOpt.get());
                    deletedCount++;
                }
            }

            if (deletedCount > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    "Đã xóa " + deletedCount + " sản phẩm khỏi danh sách yêu thích");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm nào để xóa");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/khach-hang/yeu-thich";
    }
} 