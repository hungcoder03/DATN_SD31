package com.main.datn_sd31.service.impl;

//import com.main.datn_sd31.dto.home.HomeProductDto;
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

//    @Override
//    public List<HomeProductDto> getLatestProducts(int limit) {
//        List<SanPham> sanPhams = sanPhamRepository.findAll().stream()
//                .sorted(Comparator.comparing(SanPham::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
//                .limit(limit)
//                .collect(Collectors.toList());
//
//        List<HomeProductDto> result = new ArrayList<>();
//        for (SanPham sp : sanPhams) {
//            String imageUrl = resolveMainImage(sp.getId());
//            var priceAndDiscount = resolvePriceAndDiscount(sp.getId());
//            Double ratingAvg = resolveRatingAvg(sp.getId());
//
//            String priceText = null;
//            if (priceAndDiscount.price != null) {
//                // Format price with Vietnamese locale thousand separators, without currency symbol
//                NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
//                nf.setMaximumFractionDigits(0);
//                nf.setMinimumFractionDigits(0);
//                priceText = nf.format(priceAndDiscount.price) + " đ";
//            }
//
//            result.add(HomeProductDto.builder()
//                    .id(sp.getId())
//                    .name(sp.getTen())
//                    .imageUrl(imageUrl)
//                    .price(priceAndDiscount.price)
//                    .discountPercent(priceAndDiscount.discountPercent)
//                    .ratingAvg(ratingAvg)
//                    .priceText(priceText)
//                    .build());
//        }
//        return result;
//    }

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