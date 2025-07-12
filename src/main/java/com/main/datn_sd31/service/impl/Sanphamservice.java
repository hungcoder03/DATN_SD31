package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.dto.san_pham_DTO.Sanphamform;
import com.main.datn_sd31.entity.SanPham;
import com.main.datn_sd31.repository.Chatlieurepository;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.Danhmucrepository;
import com.main.datn_sd31.repository.Hinhanhrepository;
import com.main.datn_sd31.repository.Kieudangrepository;
import com.main.datn_sd31.repository.Loaithurepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.repository.SanPhamRepository;
import com.main.datn_sd31.repository.Thuonghieurepository;
import com.main.datn_sd31.repository.Xuatxurepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class Sanphamservice {

    @Autowired private SanPhamRepository sanPhamRepo;
    @Autowired private Chatlieurepository chatLieuRepo;
    @Autowired private Xuatxurepository xuatXuRepo;
    @Autowired private Danhmucrepository danhmucrepository;
    @Autowired private Kieudangrepository kieudangrepository;
    @Autowired private Thuonghieurepository thuonghieurepository;
    @Autowired private NhanVienRepository nhanvienrepository;
    @Autowired private Hinhanhrepository hinhanhrepository;
    @Autowired private Chitietsanphamrepository chitietsanphamrepository;
    @Autowired private Loaithurepository loaithurepository;

    public List<SanPham> getAll() {
        return sanPhamRepo.findAll();
    }

    public SanPham createSanPham(Sanphamform form) {
        SanPham sp = new SanPham();
        sp.setMa(form.getMa());
        sp.setTen(form.getTen());
        sp.setMoTa(form.getMota());
        sp.setNgayTao(LocalDateTime.now());
        sp.setNgaySua(LocalDateTime.now());

        // Chuyển Integer → Boolean (0 = false, 1 = true)
        sp.setTrangThai(form.getTrangthai() != null && form.getTrangthai() == 1);

        sp.setChatLieu(chatLieuRepo.findById(form.getChatLieuId()).orElse(null));
        sp.setXuatXu(xuatXuRepo.findById(form.getXuatXuId()).orElse(null));
        sp.setDanhMuc(danhmucrepository.findById(form.getDanhMucId()).orElse(null));
        sp.setKieuDang(kieudangrepository.findById(form.getKieuDangId()).orElse(null));
        sp.setThuongHieu(thuonghieurepository.findById(form.getThuongHieuId()).orElse(null));
        sp.setLoaiThu(loaithurepository.findById(form.getLoaiThuId()).orElse(null));

         return sanPhamRepo.save(sp);
    }

    public SanPham findbyid(Integer id) {
        return sanPhamRepo.findById(id).orElse(null);
    }

    public void delete(Integer id) {
        hinhanhrepository.findBydeleteid(id);
        chitietsanphamrepository.findBydeleteid(id);
        sanPhamRepo.deleteById(id);
    }
}
