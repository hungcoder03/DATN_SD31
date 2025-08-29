package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.GioHangChiTiet;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.service.impl.Giohangservice;
import com.main.datn_sd31.repository.Giohangreposiroty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class MiniCartController {

    @Autowired
    private Giohangservice giohangservice;
    
    @Autowired
    private Giohangreposiroty giohangreposiroty;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("MiniCartController is working!");
    }
    
    @GetMapping("/mini")
    public ResponseEntity<Map<String, Object>> getMiniCart(HttpSession session) {
        try {
            // Lấy thông tin khách hàng từ session
            Object khachHangObj = session.getAttribute("khachHang");
            if (khachHangObj == null) {
                // Nếu chưa đăng nhập, trả về giỏ hàng trống
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("items", new Object[0]);
                response.put("totalQuantity", 0);
                response.put("totalPrice", BigDecimal.ZERO);
                return ResponseEntity.ok(response);
            }
            
            // Lấy danh sách giỏ hàng theo khách hàng
            KhachHang khachHang = (KhachHang) khachHangObj;
            List<GioHangChiTiet> cartItems = giohangreposiroty.findByKhachHangId(khachHang.getId());
            
            // Eager load các relationships để tránh LazyInitializationException
            if (cartItems != null) {
                for (GioHangChiTiet item : cartItems) {
                    // Force load các relationships
                    if (item.getChiTietSp() != null) {
                        item.getChiTietSp().getSanPham();
                        item.getChiTietSp().getMauSac();
                        item.getChiTietSp().getSize();
                        if (item.getChiTietSp().getSanPham() != null) {
                            item.getChiTietSp().getSanPham().getHinhAnhs();
                        }
                    }
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            
            if (cartItems != null && !cartItems.isEmpty()) {
                // Tính tổng số lượng và tổng tiền
                int totalQuantity = 0;
                BigDecimal totalPrice = BigDecimal.ZERO;
                
                for (GioHangChiTiet item : cartItems) {
                    totalQuantity += item.getSoLuong();
                    totalPrice = totalPrice.add(item.getThanhTien());
                }
                
                response.put("success", true);
                response.put("items", cartItems);
                response.put("totalQuantity", totalQuantity);
                response.put("totalPrice", totalPrice);
            } else {
                response.put("success", true);
                response.put("items", new Object[0]);
                response.put("totalQuantity", 0);
                response.put("totalPrice", BigDecimal.ZERO);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi tải giỏ hàng: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/update/{itemId}")
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @PathVariable Integer itemId,
            @RequestParam String action,
            HttpSession session) {
        
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Kiểm tra đăng nhập
            Object khachHangObj = session.getAttribute("khachHang");
            if (khachHangObj == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để thực hiện thao tác này");
                return ResponseEntity.badRequest().body(response);
            }
            
            KhachHang khachHang = (KhachHang) khachHangObj;
            
            // Lấy item hiện tại để rollback nếu cần
            GioHangChiTiet currentItem = giohangreposiroty.findById(itemId).orElse(null);
            if (currentItem == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy sản phẩm trong giỏ hàng");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Kiểm tra xem item có thuộc về khách hàng hiện tại không
            if (!currentItem.getKhachHang().getId().equals(khachHang.getId())) {
                response.put("success", false);
                response.put("message", "Không có quyền thao tác với sản phẩm này");
                return ResponseEntity.badRequest().body(response);
            }
            
            int oldQuantity = currentItem.getSoLuong();
            int newQuantity = oldQuantity;
            
            // Xử lý action
            if ("increase".equals(action)) {
                newQuantity = oldQuantity + 1;
            } else if ("decrease".equals(action)) {
                newQuantity = oldQuantity - 1;
            }
            
            // Kiểm tra số lượng tồn kho
            if (newQuantity > currentItem.getChiTietSp().getSoLuong()) {
                response.put("success", false);
                response.put("message", "Vượt quá số lượng có sẵn");
                response.put("oldQuantity", oldQuantity);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Nếu số lượng = 0, xóa item
            if (newQuantity <= 0) {
                giohangreposiroty.deleteById(itemId);
                response.put("success", true);
                response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng");
                response.put("deleted", true);
                return ResponseEntity.ok(response);
            }
            
            // Cập nhật số lượng
            currentItem.setSoLuong(newQuantity);
            currentItem.setThanhTien(currentItem.getChiTietSp().getGiaBan().multiply(BigDecimal.valueOf(newQuantity)));
            
            giohangservice.save(currentItem);
            
            response.put("success", true);
            response.put("message", "Cập nhật thành công");
            response.put("newQuantity", newQuantity);
            response.put("newTotal", currentItem.getThanhTien());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi cập nhật: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<Map<String, Object>> removeCartItem(
            @PathVariable Integer itemId,
            HttpSession session) {
        
        try {
            // Kiểm tra đăng nhập
            Object khachHangObj = session.getAttribute("khachHang");
            if (khachHangObj == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để thực hiện thao tác này");
                return ResponseEntity.badRequest().body(response);
            }
            
            KhachHang khachHang = (KhachHang) khachHangObj;
            
            // Kiểm tra xem item có tồn tại và thuộc về khách hàng hiện tại không
            GioHangChiTiet itemToDelete = giohangreposiroty.findById(itemId).orElse(null);
            if (itemToDelete == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy sản phẩm trong giỏ hàng");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!itemToDelete.getKhachHang().getId().equals(khachHang.getId())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không có quyền thao tác với sản phẩm này");
                return ResponseEntity.badRequest().body(response);
            }
            
            giohangreposiroty.deleteById(itemId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi xóa: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
} 