package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.GioHangChiTiet;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.HoaDonChiTiet;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.Chatlieurepository;
import com.main.datn_sd31.repository.Chitiethoadonrepository;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.Danhmucrepository;
import com.main.datn_sd31.repository.Giohangreposiroty;
import com.main.datn_sd31.repository.Hinhanhrepository;
import com.main.datn_sd31.repository.HoaDonRepository;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.repository.Kieudangrepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.repository.SanPhamRepository;
import com.main.datn_sd31.repository.Thuonghieurepository;
import com.main.datn_sd31.repository.Xuatxurepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class Giohangservice {
    @Autowired
    private SanPhamRepository sanPhamRepo;
    @Autowired private Chatlieurepository chatLieuRepo;
    @Autowired private Xuatxurepository xuatXuRepo;
    @Autowired private Danhmucrepository danhmucrepository;
    @Autowired private Kieudangrepository kieudangrepository;
    @Autowired private Thuonghieurepository thuonghieurepository;
    @Autowired private NhanVienRepository nhanvienrepository;
    @Autowired private Hinhanhrepository hinhanhrepository;
    @Autowired private Chitietsanphamrepository chitietsanphamrepository;
    @Autowired private Giohangreposiroty giohangreposiroty;
    @Autowired private KhachHangRepository khachhangrepository;
    @Autowired private HoaDonRepository hoadonreposiroty;
    @Autowired private Chitiethoadonrepository cHitiethoadonrepository;

    public void save(GioHangChiTiet gioHang) {
            giohangreposiroty.save(gioHang);

    }
    public void danhDauDaChonThanhToan(List<Integer> ids) {
        for (Integer id : ids) {
            GioHangChiTiet item = giohangreposiroty.findById(id).orElse(null);
            if (item != null) {
                item.setTrangThai(1); // đã xác nhận
                giohangreposiroty.save(item);
            }
        }
    }

    public List<GioHangChiTiet> findByIds(List<Integer> ids) {
        return giohangreposiroty.findByIdIn(ids);
    }
    @Transactional
    public HoaDon taoHoaDonTuGioHang(List<GioHangChiTiet> items, Integer userId) {
        KhachHang kh=khachhangrepository.find(userId);
        HoaDon hoaDon = new HoaDon();
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setKhachHang(kh);
        hoaDon.setThanhTien(
                items.stream()
                        .map(i -> BigDecimal.valueOf(i.getChiTietSp().getGiaBan().intValue())
                                .multiply(BigDecimal.valueOf(i.getSoLuong().intValue())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );




        hoadonreposiroty.save(hoaDon);

        for (GioHangChiTiet item : items) {
            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHoaDon(hoaDon);
            chiTiet.setChiTietSanPham(item.getChiTietSp());
            chiTiet.setSoLuong(item.getSoLuong());
            chiTiet.setGiaSauGiam(item.getChiTietSp().getGiaBan());
            cHitiethoadonrepository.save(chiTiet);
        }

        giohangreposiroty.deleteAll(items);

        return hoaDon;
    }
    public void mergeDuplicateGiohangItems() {
        List<Object[]> groups = giohangreposiroty.findGroupsToMerge();

        for (Object[] group : groups) {
            Integer chiTietSanPhamId = (Integer) group[0];
            Integer sizeId = (Integer) group[1];
            Integer mausacId = (Integer) group[2];
            Integer tongSoLuong = (Integer) group[3]; // SUM có thể trả về Long
            Integer tongThanhTienDouble = (Integer) group[4]; // SUM trả về Double
            Integer tongThanhTien = tongThanhTienDouble;

            // Lấy tất cả bản ghi trùng
            List<GioHangChiTiet> items = giohangreposiroty.findByChiTietSp_IdAndChiTietSp_Size_IdAndChiTietSp_MauSac_Id(
                    chiTietSanPhamId, sizeId, mausacId);

            if (items.size() <= 1) continue;

            // Giữ lại bản ghi đầu tiên
            GioHangChiTiet itemGiuLai = items.get(0);
            itemGiuLai.setSoLuong(tongSoLuong.intValue());
            itemGiuLai.setThanhTien(BigDecimal.valueOf(tongThanhTien));
            giohangreposiroty.save(itemGiuLai);

            // Xóa các bản ghi còn lại
            for (int i = 1; i < items.size(); i++) {
                giohangreposiroty.delete(items.get(i));
            }
        }
    }
}
