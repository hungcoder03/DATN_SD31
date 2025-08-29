package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.DanhGia;
import com.main.datn_sd31.repository.DanhGiaRepository;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.service.ChiTietSanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DanhGiaService {
    private final DanhGiaRepository repo;
    private final Sanphamservice sanPhamService;
    private final KhachHangRepository khachHangRepo;
    private final ChiTietSanPhamService chiTietSanPhamService;

    public List<DanhGia> layDanhGiaChoSanPham(Integer spId) {
        return repo.findBySanPhamIdOrderByThoiGianDesc(spId);
    }

    public double tinhDiemTrungBinh(Integer spId) {
        List<DanhGia> ds = repo.findBySanPhamIdOrderByThoiGianDesc(spId);
        if (ds.isEmpty()) return 0;
        return ds.stream().mapToInt(DanhGia::getSoSao).average().orElse(0);
    }

    public long demTheoSao(Integer spId, int sao) {
        return repo.findBySanPhamIdOrderByThoiGianDesc(spId)
                .stream().filter(d -> d.getSoSao() == sao).count();
    }

    public DanhGia themDanhGia(Integer spId, Integer khId, int sao, String noiDung, String hinhAnh) {
        if (!repo.existsBySanPhamIdAndKhachHangId(spId, khId)) {
            DanhGia d = new DanhGia();
            d.setSanPham(sanPhamService.findbyid(spId));
            d.setKhachHang(khachHangRepo.findById(khId).orElseThrow());
            d.setSoSao(sao);
            d.setNoiDung(noiDung);
            d.setHinhAnh(hinhAnh); // có thể là URL hoặc base64
            d.setThoiGian(Instant.now());
            return repo.save(d);
        }
        throw new IllegalStateException("Bạn đã đánh giá rồi");
    }
    public Page<DanhGia> layDanhGiaChoSanPham(Integer spId, int page, int size) {
        return repo.findBySanPhamIdOrderByThoiGianDesc(spId, PageRequest.of(page, size));
    }

    public boolean checkDanhGiaExist(Integer idCtsp, Integer khId) {
        // Tìm chi tiết sản phẩm trước
        ChiTietSanPham ctsp = chiTietSanPhamService.findById(idCtsp)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với id: " + idCtsp));

        Integer sanPhamId = ctsp.getSanPham().getId();
        return repo.existsBySanPhamIdAndKhachHangId(sanPhamId, khId);
    }
}