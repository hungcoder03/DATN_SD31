package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.repository.SpYeuThichRepository;
import com.main.datn_sd31.service.SpYeuThichService;
import com.main.datn_sd31.service.impl.KhachHangServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wishlist")
public class SpYeuThichController {

	private final SpYeuThichService spYeuThichService;
	private final KhachHangRepository khachHangRepository;
	private final KhachHangServiceImpl khachHangServiceImpl;
	private final SpYeuThichRepository spYeuThichRepository;

	public SpYeuThichController(SpYeuThichService spYeuThichService,
								KhachHangRepository khachHangRepository,
								KhachHangServiceImpl khachHangServiceImpl,
								SpYeuThichRepository spYeuThichRepository) {
		this.spYeuThichService = spYeuThichService;
		this.khachHangRepository = khachHangRepository;
		this.khachHangServiceImpl = khachHangServiceImpl;
		this.spYeuThichRepository = spYeuThichRepository;
	}

	@PostMapping("/toggle")
	public ResponseEntity<Map<String, Object>> toggleWishlist(@RequestParam("productId") Integer productId,
															  Principal principal) {
		Map<String, Object> response = new HashMap<>();

		try {
			// Kiểm tra đăng nhập
			if (principal == null) {
				response.put("success", false);
				response.put("error", "Bạn cần đăng nhập để sử dụng tính năng này");
				return ResponseEntity.status(401).body(response);
			}

			String username = principal.getName();
			KhachHang khachHang = khachHangServiceImpl.findByEmail(username);

			// Kiểm tra khách hàng có tồn tại
			if (khachHang == null) {
				response.put("success", false);
				response.put("error", "Không tìm thấy thông tin khách hàng");
				return ResponseEntity.status(404).body(response);
			}

			// Kiểm tra sản phẩm có tồn tại trong wishlist
			boolean exists = spYeuThichRepository.existsBySanPham_IdAndKhachHang_Id(productId, khachHang.getId());

			if (exists) {
				// Xóa khỏi wishlist
				boolean removed = spYeuThichService.xoaKhoiYeuThich(khachHang.getId(), productId);
				if (removed) {
					response.put("status", "removed");
					response.put("message", "Đã xóa khỏi danh sách yêu thích");
				} else {
					response.put("success", false);
					response.put("error", "Không thể xóa sản phẩm khỏi danh sách yêu thích");
					return ResponseEntity.status(500).body(response);
				}
			} else {
				// Thêm vào wishlist
				boolean added = spYeuThichService.themVaoYeuThich(khachHang.getId(), productId);
				if (added) {
					response.put("status", "added");
					response.put("message", "Đã thêm vào danh sách yêu thích");
				} else {
					response.put("success", false);
					response.put("error", "Không thể thêm sản phẩm vào danh sách yêu thích");
					return ResponseEntity.status(500).body(response);
				}
			}

			response.put("success", true);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("Error in wishlist toggle: " + e.getMessage());
			e.printStackTrace();

			response.put("success", false);
			response.put("error", "Có lỗi xảy ra: " + e.getMessage());
			return ResponseEntity.status(500).body(response);
		}
	}
}