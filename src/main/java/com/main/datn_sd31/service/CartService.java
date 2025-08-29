package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.GioHangChiTiet;
import com.main.datn_sd31.entity.HinhAnh;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.Giohangreposiroty;
import com.main.datn_sd31.repository.Hinhanhrepository;
import com.main.datn_sd31.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CartService {

    @Autowired
    private Giohangreposiroty giohangreposiroty;

    @Autowired
    private Hinhanhrepository hinhanhrepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    /**
     * Lấy dữ liệu mini cart cho Thymeleaf
     */
    public Map<String, Object> getMiniCartData() {
        try {
            // Check if user is authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || "anonymousUser".equals(authentication.getName())) {
                // User not authenticated, return empty cart
                return createEmptyCartData();
            }

            KhachHang kh = khachHangRepository.findByEmail(authentication.getName())
                    .orElse(null);
            
            if (kh == null) {
                // Khách hàng không tồn tại
                return createEmptyCartData();
            }

            List<GioHangChiTiet> gioHangList = giohangreposiroty.findByKhachHangId(kh.getId());

            if (gioHangList.isEmpty()) {
                return createEmptyCartData();
            }

            // Gộp các sản phẩm trùng lặp
            Map<String, GioHangChiTiet> gopMap = new LinkedHashMap<>();
            for (GioHangChiTiet item : gioHangList) {
                String key = item.getChiTietSp().getId() + "_" +
                        item.getChiTietSp().getSize().getId() + "_" +
                        item.getChiTietSp().getMauSac().getId();

                if (gopMap.containsKey(key)) {
                    GioHangChiTiet daCo = gopMap.get(key);
                    int soLuongMoi = daCo.getSoLuong() + item.getSoLuong();
                    int soLuongTon = item.getChiTietSp().getSoLuong();
                    
                    if (soLuongMoi > soLuongTon) {
                        soLuongMoi = soLuongTon;
                    }
                    
                    daCo.setSoLuong(soLuongMoi);
                    daCo.setThanhTien(item.getChiTietSp().getGiaBan()
                            .multiply(BigDecimal.valueOf(soLuongMoi)));
                } else {
                    gopMap.put(key, item);
                }
            }

            List<Map<String, Object>> miniCartItems = new ArrayList<>();
            BigDecimal tongTien = BigDecimal.ZERO;
            int tongSoLuong = 0;

            for (GioHangChiTiet item : gopMap.values()) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("id", item.getId());
                itemData.put("tenSanPham", item.getChiTietSp().getSanPham().getTen());
                itemData.put("tenSize", item.getChiTietSp().getSize().getTen());
                itemData.put("tenMauSac", item.getChiTietSp().getMauSac().getTen());
                itemData.put("soLuong", item.getSoLuong());
                itemData.put("soLuongTonKho", item.getChiTietSp().getSoLuong());
                itemData.put("thanhTien", item.getThanhTien());
                itemData.put("giaBan", item.getChiTietSp().getGiaBan());
                
                // Lấy hình ảnh đầu tiên của sản phẩm
                List<HinhAnh> hinhAnhs = hinhanhrepository.findByhinhanhid(item.getChiTietSp().getSanPham().getId());
                if (!hinhAnhs.isEmpty()) {
                    // Sử dụng URL từ entity thay vì tự tạo đường dẫn
                    itemData.put("imageUrl", hinhAnhs.get(0).getUrl());
                } else {
                    itemData.put("imageUrl", "/client-static/images/item1.jpg");
                }

                miniCartItems.add(itemData);
                tongTien = tongTien.add(item.getThanhTien());
                tongSoLuong += item.getSoLuong();
            }

            Map<String, Object> cartData = new HashMap<>();
            cartData.put("items", miniCartItems);
            cartData.put("totalItems", tongSoLuong);
            cartData.put("totalAmount", tongTien);
            cartData.put("totalDistinctItems", miniCartItems.size());
            return cartData;

        } catch (Exception e) {
            e.printStackTrace();
            return createEmptyCartData();
        }
    }

    /**
     * Tạo dữ liệu cart trống
     */
    private Map<String, Object> createEmptyCartData() {
        Map<String, Object> emptyData = new HashMap<>();
        emptyData.put("items", new ArrayList<>());
        emptyData.put("totalItems", 0);
        emptyData.put("totalAmount", BigDecimal.ZERO);
        emptyData.put("totalDistinctItems", 0);
        return emptyData;
    }

    /**
     * Kiểm tra user có đăng nhập không
     */
    public boolean isUserLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && !"anonymousUser".equals(authentication.getName());
    }

    /**
     * Lấy số lượng sản phẩm trong giỏ hàng
     */
    public int getCartItemCount() {
        Map<String, Object> cartData = getMiniCartData();
        return (Integer) cartData.get("totalItems");
    }
} 