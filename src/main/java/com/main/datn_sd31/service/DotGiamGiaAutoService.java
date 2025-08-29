package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.DotGiamGia;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.Dotgiamgiarepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DotGiamGiaAutoService {

    private final Dotgiamgiarepository dotGiamGiaRepository;
    private final Chitietsanphamrepository chiTietSanPhamRepo;

    /**
     * Job tự động chạy mỗi phút để cập nhật trạng thái đợt giảm giá
     * và khôi phục giá gốc cho các đợt đã hết hạn
     */
    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    public void autoUpdateDotGiamGiaStatus() {
        try {
//            log.info("Bắt đầu job tự động cập nhật trạng thái đợt giảm giá...");
            
            List<DotGiamGia> allDots = dotGiamGiaRepository.findAll();
            LocalDateTime now = LocalDateTime.now();
            
            for (DotGiamGia dot : allDots) {
                if (dot.getNgayBatDau() == null || dot.getNgayKetThuc() == null) {
                    continue;
                }
                
                Integer oldStatus = dot.getTrangThai();
                Integer newStatus = null;
                
                if (now.isBefore(dot.getNgayBatDau())) {
                    newStatus = 0; // Chuẩn bị áp dụng
                } else if (now.isAfter(dot.getNgayKetThuc())) {
                    newStatus = 2; // Ngừng hoạt động
                } else {
                    newStatus = 1; // Đang hoạt động
                }
                
                // Nếu trạng thái thay đổi
                if (!newStatus.equals(oldStatus)) {
                    dot.setTrangThai(newStatus);
                    
                    // Nếu đợt vừa hết hạn, khôi phục giá gốc
                    if (newStatus == 2 && oldStatus != null && oldStatus != 2) {
                        restoreOriginalPricesForExpiredDot(dot.getId());
                        log.info("Đã khôi phục giá gốc cho đợt giảm giá: {} (ID: {})", dot.getTen(), dot.getId());
                    }
                    
                    log.info("Cập nhật trạng thái đợt giảm giá: {} (ID: {}) từ {} -> {}", 
                            dot.getTen(), dot.getId(), oldStatus, newStatus);
                }
            }
            
            // Lưu tất cả thay đổi
            dotGiamGiaRepository.saveAll(allDots);
//            log.info("Hoàn thành job tự động cập nhật trạng thái đợt giảm giá");
            
        } catch (Exception e) {
            log.error("Lỗi trong job tự động cập nhật trạng thái đợt giảm giá: {}", e.getMessage(), e);
        }
    }

    /**
     * Khôi phục giá gốc cho tất cả sản phẩm thuộc đợt giảm giá đã hết hạn
     */
    public void restoreOriginalPricesForExpiredDot(Integer dotId) {
        try {
            // Sử dụng Pageable để lấy tất cả sản phẩm
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<ChiTietSanPham> page = chiTietSanPhamRepo.findByDotGiamGia_Id(dotId, pageable);
            List<ChiTietSanPham> affectedProducts = page.getContent();
            
            if (!affectedProducts.isEmpty()) {
                for (ChiTietSanPham ct : affectedProducts) {
                    BigDecimal oldGiaBan = ct.getGiaBan();
                    BigDecimal newGiaBan = ct.getGiaGoc();
                    // Khôi phục giá gốc
                    ct.setGiaBan(newGiaBan);
                    // Bỏ liên kết với đợt giảm giá
                    ct.setDotGiamGia(null);
                    // Log chi tiết từng biến thể
                    log.info("Restore giaBan=giaGoc for CTSP id={}, dotId={}, oldGiaBan={}, newGiaBan={}",
                            ct.getId(), dotId, oldGiaBan, newGiaBan);
                }
                
                chiTietSanPhamRepo.saveAll(affectedProducts);
                log.info("Đã khôi phục giá gốc cho {} sản phẩm thuộc đợt giảm giá ID: {}", affectedProducts.size(), dotId);
            }
        } catch (Exception e) {
            log.error("Lỗi khi khôi phục giá gốc cho đợt giảm giá ID: {}: {}", dotId, e.getMessage(), e);
        }
    }

    /**
     * Tính toán giá bán sau khi áp dụng đợt giảm giá
     */
    public BigDecimal calculateDiscountedPrice(BigDecimal giaGoc, DotGiamGia dot) {
        if (giaGoc == null || dot == null || dot.getGiaTriDotGiamGia() == null || dot.getLoai() == null) {
            return giaGoc;
        }

        BigDecimal giaBan = giaGoc;
        BigDecimal giaTri = dot.getGiaTriDotGiamGia();

        if ("phan_tram".equalsIgnoreCase(dot.getLoai())) {
            // Giảm giá theo phần trăm (1-100)
            if (giaTri.compareTo(BigDecimal.ZERO) > 0 && giaTri.compareTo(new BigDecimal(100)) <= 0) {
                BigDecimal phanTram = giaTri.divide(new BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                BigDecimal soTienGiam = giaGoc.multiply(phanTram);
                giaBan = giaGoc.subtract(soTienGiam);
            }
        } else if ("tien_mat".equalsIgnoreCase(dot.getLoai())) {
            // Giảm giá theo tiền mặt
            if (giaTri.compareTo(BigDecimal.ZERO) > 0) {
                giaBan = giaGoc.subtract(giaTri);
            }
        }

        // Đảm bảo giá không âm
        return giaBan.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : giaBan;
    }
} 