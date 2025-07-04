package com.main.datn_sd31.controller.admin_controller;


import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.HoaDonChiTiet;
import com.main.datn_sd31.entity.LichSuHoaDon;
import com.main.datn_sd31.entity.PhieuGiamGia;
import com.main.datn_sd31.entity.SanPham;
import com.main.datn_sd31.repository.Chitiethoadonrepository;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.HoaDonRepository;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.repository.LichSuHoaDonRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.repository.PhieuGiamGiaRepository;
import com.main.datn_sd31.repository.SanPhamRepository;
import com.main.datn_sd31.service.impl.GHNService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/ban-hang")
@RequiredArgsConstructor
@Transactional
public class BanHangController {

    private final Chitietsanphamrepository chiTietSanPhamRepository;
    private final HoaDonRepository hoaDonRepository;
    private final Chitiethoadonrepository hoaDonChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final SanPhamRepository sanphamrepository;
    private final PhieuGiamGiaRepository phieugiamgiarepository;
    private final GHNService ghnService;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;

    private List<HoaDonChiTiet> getCart(String cartKey, HttpSession session) {
        Map<String, List<HoaDonChiTiet>> carts = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (carts == null) {
            carts = new HashMap<>();
            session.setAttribute("tatCaGio", carts);
        }

        return carts.computeIfAbsent(cartKey, k -> new ArrayList<>());
    }

    @GetMapping
    public String hienThiSanPham(
            @RequestParam(value = "idSanPham", required = false) Integer idSanPham,
            @RequestParam(value = "cartKey", required = false, defaultValue = "gio-1") String cartKey,
            Model model, HttpSession session) {

        List<SanPham> dsSanPham = sanphamrepository.findAll();
        List<ChiTietSanPham> dsChiTiet = idSanPham != null ?
                chiTietSanPhamRepository.findBySanPhamId(idSanPham) : new ArrayList<>();

        Map<String, List<HoaDonChiTiet>> tatCaGio = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (tatCaGio == null) {
            tatCaGio = new HashMap<>();
            session.setAttribute("tatCaGio", tatCaGio);
        }

        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        BigDecimal tongTien = gio.stream()
                .map(i -> i.getGiaSauGiam().multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tienGiam = (BigDecimal) session.getAttribute("tienGiam");
        BigDecimal phiShip = (BigDecimal) session.getAttribute("phiVanChuyen");
        if (tienGiam == null) tienGiam = BigDecimal.ZERO;
        if (phiShip == null) phiShip = BigDecimal.ZERO;

        BigDecimal tongSauGiam = tongTien.subtract(tienGiam).add(phiShip).max(BigDecimal.ZERO);

        model.addAttribute("dsSanPham", dsSanPham);
        model.addAttribute("dsChiTietSanPham", dsChiTiet);
        model.addAttribute("sanPhamDaChon", idSanPham);
        model.addAttribute("gioHang", gio);
        model.addAttribute("tatCaGio", tatCaGio.keySet());
        model.addAttribute("cartKey", cartKey);
        model.addAttribute("tongTien", tongTien);
        model.addAttribute("tongTienSauGiam", tongSauGiam);
        model.addAttribute("phiVanChuyen", phiShip);
        model.addAttribute("dsPhieuGiamGia", phieugiamgiarepository.findAll());

        return "admin/pages/banhang/banhang";
    }

    @PostMapping("/them-gio")
    public String themVaoGio(
            @RequestParam("idChiTietSp") Integer id,
            @RequestParam("soLuong") Integer soLuong,
            @RequestParam("cartKey") String cartKey,
            HttpSession session) {

        ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(id).orElseThrow();
        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        Optional<HoaDonChiTiet> daCo = gio.stream()
                .filter(i -> i.getChiTietSanPham().getId().equals(id))
                .findFirst();

        if (daCo.isPresent()) {
            daCo.get().setSoLuong(daCo.get().getSoLuong() + soLuong);
        } else {
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setChiTietSanPham(ctsp);
            hdct.setSoLuong(soLuong);
            hdct.setGiaSauGiam(ctsp.getGiaBan());
            hdct.setGiaGiam(BigDecimal.ZERO);
            hdct.setTrangThai(true);
            gio.add(hdct);
        }

        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }

    @GetMapping("/ap-dung-ma")
    public String apDungMa(@RequestParam("maGiamGia") String ma,
                           @RequestParam("cartKey") String cartKey,
                           HttpSession session) {
        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        BigDecimal tongTien = gio.stream()
                .map(i -> i.getGiaSauGiam().multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PhieuGiamGia phieu = phieugiamgiarepository.findByMa(ma.trim());
        BigDecimal tienGiam = BigDecimal.ZERO;

        if (phieu != null && Boolean.TRUE.equals(phieu.getTrangThai())) {
            LocalDate today = LocalDate.now();
            if ((phieu.getNgayBatDau() == null || !today.isBefore(phieu.getNgayBatDau().atStartOfDay().toLocalDate())) &&
                    (phieu.getNgayKetThuc() == null || !today.isAfter(phieu.getNgayKetThuc().atStartOfDay().toLocalDate()))) {

                if (phieu.getLoaiPhieuGiamGia() == 2) {
                    tienGiam = phieu.getMucDo();  // Giảm tiền cố định
                } else if (phieu.getLoaiPhieuGiamGia() == 1) {
                    // Giảm theo % (mucDo là % giảm)
                    tienGiam = tongTien.multiply(phieu.getMucDo()).divide(BigDecimal.valueOf(100));

                    // Giảm tối đa 50.000
                    if (tienGiam.compareTo(BigDecimal.valueOf(50000)) > 0) {
                        tienGiam = BigDecimal.valueOf(50000);
                    }
                }
            }
        }


        session.setAttribute("tienGiam", tienGiam);
        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }

    @PostMapping("/xoa-gio")
    public String xoaGioHang(@RequestParam("cartKey") String cartKey, HttpSession session) {
        Map<String, List<HoaDonChiTiet>> tatCaGio = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (tatCaGio != null) {
            tatCaGio.remove(cartKey);
        }
        session.removeAttribute("tienGiam");
        session.removeAttribute("phiVanChuyen");
        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }

    @PostMapping("/thanh-toan")
    @Transactional
    public String thanhToan(@RequestParam("cartKey") String cartKey, HttpSession session) {
        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        if (gio.isEmpty()) return "redirect:/admin/ban-hang?cartKey=" + cartKey;

        // 1. Tính tiền
        BigDecimal tongTien = gio.stream()
                .map(i -> i.getGiaSauGiam().multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tienGiam = Optional.ofNullable((BigDecimal) session.getAttribute("tienGiam")).orElse(BigDecimal.ZERO);
        BigDecimal phiShip = Optional.ofNullable((BigDecimal) session.getAttribute("phiVanChuyen")).orElse(BigDecimal.ZERO);

        // 2. Tạo hóa đơn
        HoaDon hd = new HoaDon();
        hd.setMa("HD" + System.currentTimeMillis());
        hd.setNgayTao(LocalDateTime.now());
        hd.setNgayMua(LocalDateTime.now());
        hd.setNgaySua(LocalDateTime.now());
        hd.setTenNguoiNhan("trực tiếp");
        hd.setLoaihoadon("Trực tiếp");
        hd.setKhachHang(khachHangRepository.findById(1).orElse(null));
        hd.setNhanVien(nhanVienRepository.findById(1).orElse(null));
        hd.setTrangThai(true);               // Chưa thanh toán hoặc đã? tùy logic
        hd.setPhuongThuc("Tiền mặt");
        hd.setNhanVien(nhanVienRepository.find(1));
        hd.setGiaGoc(tongTien);
        hd.setGiaGiamGia(tienGiam);
        hd.setPhiVanChuyen(phiShip);
        hd.setThanhTien(tongTien.subtract(tienGiam).add(phiShip));

        hoaDonRepository.save(hd);

        // 3. Lưu chi tiết + cập nhật tồn kho
        for (HoaDonChiTiet ct : gio) {
            ChiTietSanPham sp = ct.getChiTietSanPham();

            String tenSp = sp.getSanPham().getTen();
            String mau = sp.getMauSac().getTen();
            String size = sp.getSize().getTen();
            String tenCt = tenSp + " - " + mau + " / " + size;

            ct.setHoaDon(hd);
            ct.setTenCtsp(tenCt);
            hoaDonChiTietRepository.save(ct);

            sp.setSoLuong(sp.getSoLuong() - ct.getSoLuong());
            chiTietSanPhamRepository.save(sp);
        }

        /* ---------- BỔ SUNG GHI LOG LỊCH SỬ ---------- */
        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(hd);
        ls.setNgayTao(LocalDateTime.now());
        ls.setNgaySua(LocalDateTime.now());
        ls.setNguoiTao(1); // hoặc 1 nếu đang dùng mặc định
        ls.setNguoiTao(1); // tương tự

        if (hd.getDiaChi() != null && !hd.getDiaChi().trim().isEmpty()) {
            // Có địa chỉ ⇒ đơn online ⇒ chờ giao hàng
            ls.setTrangThai(TrangThaiLichSuHoaDon.CHO_GIAO_HANG.getValue());
            ls.setGhiChu("Đơn hàng đang chờ giao đến khách");
        } else {
            // Không có địa chỉ ⇒ đơn tại quầy
            ls.setTrangThai(TrangThaiLichSuHoaDon.HOAN_THANH.getValue());
            ls.setGhiChu("Thanh toán trực tiếp tại quầy");
        }

        lichSuHoaDonRepository.save(ls);
        /* --------------------------------------------- */

        // 4. Xoá session & giỏ
        Map<String, List<HoaDonChiTiet>> tatCaGio =
                (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (tatCaGio != null) tatCaGio.remove(cartKey);

        session.removeAttribute("tienGiam");
        session.removeAttribute("phiVanChuyen");

        return "redirect:/admin/ban-hang";
    }


    // ======================= GHN APIs ========================
    @GetMapping("/dia-chi/tinh")
    @ResponseBody
    public List<Map<String, Object>> getTinh() {
        return ghnService.getProvinces();
    }

    @GetMapping("/dia-chi/huyen")
    @ResponseBody
    public List<Map<String, Object>> getHuyen(@RequestParam("provinceId") int provinceId) {
        return ghnService.getDistricts(provinceId);
    }

    @GetMapping("/dia-chi/xa")
    @ResponseBody
    public List<Map<String, Object>> getXa(@RequestParam("districtId") int districtId) {
        return ghnService.getWards(districtId);
    }

    @GetMapping("/phi-ship")
    @ResponseBody
    public ResponseEntity<Integer> getPhiShip(
            @RequestParam("toDistrictId") int toDistrictId,
            @RequestParam("wardCode") String toWardCode,
            HttpSession session) {
        int fromDistrictId = 3440; // Mặc định Quận 1
        int weight = 1000;
        List<Map<String, Object>> services = ghnService.getAvailableServices(fromDistrictId, toDistrictId);
        if (services.isEmpty()) return ResponseEntity.ok(0);

        int serviceId = (int) services.get(0).get("service_id");
        Integer fee = ghnService.getShippingFee(fromDistrictId, toDistrictId, toWardCode, weight, serviceId);
        session.setAttribute("phiVanChuyen", new BigDecimal(fee));
        return ResponseEntity.ok(fee);
    }
}
