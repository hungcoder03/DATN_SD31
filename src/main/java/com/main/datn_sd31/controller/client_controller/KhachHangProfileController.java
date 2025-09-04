package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.SpYeuThich;
import com.main.datn_sd31.repository.SpYeuThichRepository;
import com.main.datn_sd31.service.impl.KhachHangServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.math.BigDecimal;
import com.main.datn_sd31.entity.ChiTietSanPham;
import java.util.Set;
import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
@RequestMapping("/khach-hang")
public class KhachHangProfileController {

    private final SpYeuThichRepository spYeuThichRepository;
    private final KhachHangServiceImpl khachHangService;

    @GetMapping("/thong-tin")
    public String thongTinTaiKhoan(Model model) {
        model.addAttribute("activePage", "profile");
        return "client/pages/profile/profile";
    }

    @GetMapping("/don-hang")
    public String donHangCuaToi(Model model) {
        model.addAttribute("activePage", "orders");
        return "client/pages/profile/orders";
    }

    @GetMapping("/yeu-thich")
    public String sanPhamYeuThich(Model model, 
                                 Principal principal,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size) {
        model.addAttribute("activePage", "wishlist");
        
        if (principal != null) {
            try {
                KhachHang khachHang = khachHangService.findByEmail(principal.getName());
                if (khachHang != null) {
                    // Tạo Pageable với sắp xếp theo thời gian thêm mới nhất
                    Pageable pageable = PageRequest.of(page, size, Sort.by("thoiGianThem").descending());
                    
                    // Lấy danh sách sản phẩm yêu thích có phân trang
                    Page<SpYeuThich> wishlistPage = spYeuThichRepository.findByKhachHang_Id(khachHang.getId(), pageable);
                    
                    // Fetch dữ liệu đầy đủ cho mỗi sản phẩm
                    List<SpYeuThich> wishlistItems = wishlistPage.getContent();
                    
                    // Tạo maps để lưu thông tin giá và giảm giá
                    Map<Integer, BigDecimal> giaBanMinMap = new HashMap<>();
                    Map<Integer, BigDecimal> giaGocMinMap = new HashMap<>();
                    Map<Integer, BigDecimal> giaGocMaxMap = new HashMap<>();
                    Map<Integer, Integer> soLuongMap = new HashMap<>();
                    Map<Integer, String> phanTramGiamMap = new HashMap<>();
                    
                    for (SpYeuThich item : wishlistItems) {
                        Integer sanPhamId = item.getSanPham().getId();
                        
                        // Lấy thông tin giá từ ChiTietSanPham
                        Set<ChiTietSanPham> chiTietSet = item.getSanPham().getChiTietSanPhams();
                        if (chiTietSet != null && !chiTietSet.isEmpty()) {
                            List<ChiTietSanPham> chiTietList = new ArrayList<>(chiTietSet);
                            
                            // Tìm giá bán thấp nhất
                            BigDecimal giaBanMin = chiTietList.stream()
                                .map(ChiTietSanPham::getGiaBan)
                                .filter(Objects::nonNull)
                                .min(BigDecimal::compareTo)
                                .orElse(null);
                            
                            // Tìm giá gốc thấp nhất và cao nhất
                            BigDecimal giaGocMin = chiTietList.stream()
                                .map(ChiTietSanPham::getGiaGoc)
                                .filter(Objects::nonNull)
                                .min(BigDecimal::compareTo)
                                .orElse(null);
                            
                            BigDecimal giaGocMax = chiTietList.stream()
                                .map(ChiTietSanPham::getGiaGoc)
                                .filter(Objects::nonNull)
                                .max(BigDecimal::compareTo)
                                .orElse(null);
                            
                            // Tính tổng số lượng
                            int tongSoLuong = chiTietList.stream()
                                .mapToInt(ChiTietSanPham::getSoLuong)
                                .sum();
                            
                            // Lấy thông tin giảm giá
                            String phanTramGiam = null;
                            for (ChiTietSanPham chiTiet : chiTietList) {
                                if (chiTiet.getDotGiamGia() != null) {
                                    if ("phan_tram".equals(chiTiet.getDotGiamGia().getLoai())) {
                                        phanTramGiam = "-" + chiTiet.getDotGiamGia().getGiaTriDotGiamGia() + "%";
                                        break;
                                    } else if ("tien_mat".equals(chiTiet.getDotGiamGia().getLoai())) {
                                        phanTramGiam = "-" + chiTiet.getDotGiamGia().getGiaTriDotGiamGia() + "₫";
                                        break;
                                    }
                                }
                            }
                            
                            giaBanMinMap.put(sanPhamId, giaBanMin);
                            giaGocMinMap.put(sanPhamId, giaGocMin);
                            giaGocMaxMap.put(sanPhamId, giaGocMax);
                            soLuongMap.put(sanPhamId, tongSoLuong);
                            phanTramGiamMap.put(sanPhamId, phanTramGiam);
                        }
                    }
                    
                    model.addAttribute("wishlistItems", wishlistItems);
                    model.addAttribute("giaBanMinMap", giaBanMinMap);
                    model.addAttribute("giaGocMinMap", giaGocMinMap);
                    model.addAttribute("giaGocMaxMap", giaGocMaxMap);
                    model.addAttribute("soLuongMap", soLuongMap);
                    model.addAttribute("phanTramGiamMap", phanTramGiamMap);
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", wishlistPage.getTotalPages());
                    model.addAttribute("totalItems", wishlistPage.getTotalElements());
                    model.addAttribute("hasNext", wishlistPage.hasNext());
                    model.addAttribute("hasPrevious", wishlistPage.hasPrevious());
                }
            } catch (Exception e) {
                System.err.println("Error loading wishlist: " + e.getMessage());
            }
        }
        
        return "client/pages/profile/wishlist";
    }

    @GetMapping("/dia-chi")
    public String diaChiGiaoHang(Model model) {
        model.addAttribute("activePage", "address");
        return "client/pages/profile/address";
    }
} 