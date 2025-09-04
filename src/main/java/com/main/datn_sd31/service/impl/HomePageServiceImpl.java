package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.dto.HomeProductDto;
import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.DanhGia;
import com.main.datn_sd31.entity.HinhAnh;
import com.main.datn_sd31.entity.SanPham;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.DanhGiaRepository;
import com.main.datn_sd31.repository.Hinhanhrepository;
import com.main.datn_sd31.repository.SanPhamRepository;
import com.main.datn_sd31.service.HomePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomePageServiceImpl implements HomePageService {

    private final SanPhamRepository sanPhamRepository;
    private final Chitietsanphamrepository chiTietRepo;
    private final Hinhanhrepository hinhAnhRepository;
    private final DanhGiaRepository danhGiaRepository;

    @Override
    public List<HomeProductDto> getLatestProducts(int limit) {
        List<SanPham> sanPhams = sanPhamRepository.findByTrangThaiTrue().stream()
                .sorted(Comparator.comparing(SanPham::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        List<HomeProductDto> result = new ArrayList<>();
        for (SanPham sp : sanPhams) {
            // Lấy ảnh chính của sản phẩm
            String imageUrl = resolveMainImage(sp.getId());
            
            // Lấy thông tin giá và giảm giá 
            PriceDiscount priceAndDiscount = resolvePriceAndDiscount(sp.getId());
            
            // Lấy đánh giá trung bình
            Double ratingAvg = resolveRatingAvg(sp.getId());

            // Tạo DTO và thêm vào kết quả
            HomeProductDto dto = new HomeProductDto();
            dto.setId(sp.getId());
            dto.setName(sp.getTen());
            dto.setImageUrl(imageUrl);
            dto.setPrice(priceAndDiscount.price());
            dto.setPriceText(priceAndDiscount.price() != null ? String.format("%,.0f đ", priceAndDiscount.price()) : "Liên hệ");
            dto.setDiscountPercent(priceAndDiscount.discountPercent());
            dto.setRatingAvg(ratingAvg);
            
            result.add(dto);
        }
        
        return result;
    }

    private String resolveMainImage(Integer sanPhamId) {
        List<HinhAnh> hinhAnhs = hinhAnhRepository.findByhinhanhid(sanPhamId);
        if (hinhAnhs == null || hinhAnhs.isEmpty()) {
            // Fallback to a default static image path exposed by web server
            return "/client-static/images/insta1.jpg";
        }
        return hinhAnhs.stream()
                .filter(ha -> "Ảnh chính".equalsIgnoreCase(ha.getTen()))
                .map(HinhAnh::getUrl)
                .findFirst()
                .orElse(hinhAnhs.get(0).getUrl());
    }

    private class PriceDiscount {
        private final BigDecimal price;
        private final Integer discountPercent;

        private PriceDiscount(BigDecimal price, Integer discountPercent) {
            this.price = price;
            this.discountPercent = discountPercent;
        }

        public BigDecimal price() {
            return price;
        }

        public Integer discountPercent() {
            return discountPercent;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (PriceDiscount) obj;
            return Objects.equals(this.price, that.price) &&
                    Objects.equals(this.discountPercent, that.discountPercent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(price, discountPercent);
        }

        @Override
        public String toString() {
            return "PriceDiscount[" +
                    "price=" + price + ", " +
                    "discountPercent=" + discountPercent + ']';
        }
    }

    private PriceDiscount resolvePriceAndDiscount(Integer sanPhamId) {
        List<ChiTietSanPham> ctList = chiTietRepo.findBySanPhamId(sanPhamId);
        if (ctList == null || ctList.isEmpty()) return new PriceDiscount(null, null);
        ChiTietSanPham chosen = ctList.stream()
                .filter(ct -> ct.getGiaBan() != null)
                .min(Comparator.comparing(ChiTietSanPham::getGiaBan))
                .orElse(ctList.get(0));
        BigDecimal price = chosen.getGiaBan();
        Integer discountPercent = null;
        if (chosen.getGiaGoc() != null && chosen.getGiaBan() != null
                && chosen.getGiaGoc().compareTo(chosen.getGiaBan()) > 0) {
            BigDecimal diff = chosen.getGiaGoc().subtract(chosen.getGiaBan());
            BigDecimal percent = diff.multiply(BigDecimal.valueOf(100))
                    .divide(chosen.getGiaGoc(), 0, RoundingMode.HALF_UP);
            discountPercent = percent.intValue();
        }
        return new PriceDiscount(price, discountPercent);
    }

    private Double resolveRatingAvg(Integer sanPhamId) {
        List<DanhGia> danhGias = danhGiaRepository.findBySanPhamIdOrderByThoiGianDesc(sanPhamId);
        if (danhGias == null || danhGias.isEmpty()) return null;
        return danhGias.stream()
                .filter(dg -> dg.getSoSao() != null)
                .mapToInt(DanhGia::getSoSao)
                .average()
                .orElse(0.0);
    }
} 