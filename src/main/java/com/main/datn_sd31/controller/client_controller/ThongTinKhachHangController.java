package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.service.KhachHangService;
import com.main.datn_sd31.service.impl.GHNService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tai-khoan")
@RequiredArgsConstructor
public class ThongTinKhachHangController {

    private final KhachHangService khachHangService;
    private final GHNService ghnService;


    @GetMapping("/thong-tin")
    public String hienThiThongTinKhachHang(Model model,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        KhachHang khachHang = khachHangService.findByEmail(email);
        model.addAttribute("khachHang", khachHang);
        return "client/pages/profile/profile"; // Giao diện hiển thị
    }

    /**
     * Cập nhật địa chỉ khách hàng từ lựa chọn tỉnh/huyện/xã
     */
    @PostMapping("/cap-nhat-dia-chi")
    public String capNhatDiaChi(@RequestParam("tinh") String tinh,
                                @RequestParam("huyen") String huyen,
                                @RequestParam("xa") String xa,
                                @RequestParam("chiTiet") String chiTiet,
                                @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        KhachHang khachHang = khachHangService.findByEmail(email);

        String diaChiGop = chiTiet + ", " + xa + ", " + huyen + ", " + tinh;
        khachHang.setDiaChi(diaChiGop);

        khachHangService.save(khachHang);
        return "redirect:/tai-khoan/thong-tin";
    }

    @PostMapping("/xoa-dia-chi")
    public String xoaDiaChi(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        KhachHang khachHang = khachHangService.findByEmail(email);

        khachHang.setDiaChi(null); // Xóa địa chỉ
        khachHangService.save(khachHang);

        return "redirect:/tai-khoan/thong-tin";
    }


    // Ví dụ cập nhật email
    @PostMapping("/cap-nhat-email")
    public String capNhatEmail(@RequestParam("email") String emailMoi,
                               @AuthenticationPrincipal UserDetails userDetails) {
        String emailCu = userDetails.getUsername();
        KhachHang kh = khachHangService.findByEmail(emailCu);
        khachHangService.capNhatEmail(kh.getId(), emailMoi);
        return "redirect:/tai-khoan/thong-tin";
    }

    @PostMapping("/cap-nhat-sdt")
    public String capNhatSoDienThoai(@RequestParam("soDienThoai") String soDienThoaiMoi,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        KhachHang khachHang = khachHangService.findByEmail(email);
        khachHangService.capNhatSoDienThoai(khachHang.getId(), soDienThoaiMoi);
        return "redirect:/tai-khoan/thong-tin";
    }

    @GetMapping("/dia-chi/tinh")
    @ResponseBody
    public List<Map<String, Object>> getTinh() {
        return ghnService.getProvinces();
    }

    @GetMapping("/dia-chi/huyen")
    @ResponseBody
    public List<Map<String, Object>> getHuyen(@RequestParam("provinceId") int provinceId) {
        return ghnService.getDistricts(provinceId);
    }

    @GetMapping("/dia-chi/xa")
    @ResponseBody
    public List<Map<String, Object>> getXa(@RequestParam("districtId") int districtId) {
        return ghnService.getWards(districtId);
    }

}



