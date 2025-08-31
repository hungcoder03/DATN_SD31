package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.*;
import com.main.datn_sd31.repository.*;
import com.main.datn_sd31.service.ChiTietSanPhamService;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import com.main.datn_sd31.service.impl.GHNService;
import com.main.datn_sd31.service.impl.Giohangservice;
import com.main.datn_sd31.service.impl.Sanphamservice;
import com.main.datn_sd31.util.ThongBaoUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/gio-hang")
public class GiohangController {

    private final Sanphamservice sanPhamService;
    private final Sizerepository sizerepository;
    private final Mausacrepository mausacrepository;
    private final PhieuGiamGiaService phieuGiamGiaService;
    private final Chitietsanphamrepository chitietsanphamRepo;
    private final Hinhanhrepository hinhanhrepository;
    private final Giohangreposiroty giohangreposiroty;
    private final Giohangservice giohangservice;
    private final KhachHangRepository khachhangrepository;
    private final HoaDonRepository hoadonreposiroty;
    private final PhieuGiamGiaRepository phieugiamgiarepository;
    private final NhanVienRepository nhanvienrepository;
    private final HoaDonChiTietRepository hoadonCTreposiroty;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;

    private final ChiTietSanPhamService chiTietSanPhamService;

    @Autowired
    private GHNService ghnService;

    private KhachHang getCurrentKhachHang() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return khachhangrepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với email: " + email));
    }


    @Transactional
    @GetMapping("/hien_thi")
    public String hienthi(Model model, @ModelAttribute("messa") String messa) {
        // Lấy khách hàng hiện tại trước
        KhachHang kh = getCurrentKhachHang();
        
        // ✅ CHỈ lấy giỏ hàng của khách hàng đã đăng nhập
        List<GioHangChiTiet> giohangList = giohangreposiroty.findByKhachHangId(kh.getId());

        if (giohangList.isEmpty()) {
            // Nếu giỏ hàng trống, trả về danh sách rỗng
            model.addAttribute("list", new ArrayList<>());
            model.addAttribute("tongTien", BigDecimal.ZERO);
            model.addAttribute("messa", messa);
            return "client/pages/cart/list";
        }

        Map<String, GioHangChiTiet> gopMap = new LinkedHashMap<>();
        for (GioHangChiTiet item : giohangList) {
            String key = item.getChiTietSp().getId() + "_" +
                    item.getChiTietSp().getSize().getId() + "_" +
                    item.getChiTietSp().getMauSac().getId();

            int soLuongTon = item.getChiTietSp().getSoLuong(); // Lấy tồn kho sản phẩm

            if (gopMap.containsKey(key)) {
                GioHangChiTiet daCo = gopMap.get(key);
                int soLuongMoi = daCo.getSoLuong() + item.getSoLuong();

                // Check tồn kho
                if (soLuongMoi > soLuongTon) {
                    soLuongMoi = soLuongTon;
                }

                daCo.setSoLuong(soLuongMoi);
                daCo.setThanhTien(item.getChiTietSp().getGiaBan()
                        .multiply(BigDecimal.valueOf(soLuongMoi)));
            } else {
                // Nếu số lượng vượt tồn kho ngay từ đầu
                if (item.getSoLuong() > soLuongTon) {
                    item.setSoLuong(soLuongTon);
                    item.setThanhTien(item.getChiTietSp().getGiaBan()
                            .multiply(BigDecimal.valueOf(soLuongTon)));
                }
                gopMap.put(key, item);
            }
        }

        // Xóa các bản ghi cũ của khách hàng này
        giohangreposiroty.deleteByKhachHangId(kh.getId());
        giohangreposiroty.flush();

        // Tạo lại danh sách mới với khách hàng đúng
        List<GioHangChiTiet> newList = new ArrayList<>();
        for (GioHangChiTiet item : gopMap.values()) {
            GioHangChiTiet newItem = new GioHangChiTiet();
            newItem.setKhachHang(kh);
            newItem.setChiTietSp(item.getChiTietSp());
            newItem.setSoLuong(item.getSoLuong());
            newItem.setThanhTien(item.getThanhTien());
            newItem.setTrangThai(item.getTrangThai());
            newList.add(newItem);
        }
        giohangreposiroty.saveAll(newList);

        BigDecimal tongTien = newList.stream()
                .map(GioHangChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("list", newList);
        model.addAttribute("tongTien", tongTien);

        model.addAttribute("messa", messa);
        return "client/pages/cart/list";
    }


    @PostMapping("/them")
    public String xuLyThem(@RequestParam("chiTietId") Integer chiTietId,
                           @RequestParam("soLuong") Integer soLuong,
                           RedirectAttributes redirect) {

        // Validation đầu vào
        if (soLuong == null || soLuong <= 0) {
            redirect.addFlashAttribute("error", "Số lượng phải lớn hơn 0");
            return "redirect:/gio-hang/hien_thi";
        }

        KhachHang kh = getCurrentKhachHang();

        ChiTietSanPham chiTiet = chitietsanphamRepo.findById(chiTietId)
                .orElse(null);
        if (chiTiet == null) {
            redirect.addFlashAttribute("error", "Không tìm thấy sản phẩm");
            return "redirect:/gio-hang/hien_thi";
        }
        int soLuongTon = chiTiet.getSoLuong();

        // Kiểm tra sản phẩm còn hàng không
        if (soLuongTon <= 0) {
            redirect.addFlashAttribute("error", "Sản phẩm đã hết hàng");
            return "redirect:/gio-hang/hien_thi";
        }

        GioHangChiTiet gioHangHienCo = giohangreposiroty
                .findByKhachHangIdAndChiTietSpId(kh.getId(), chiTiet.getId());

        if (gioHangHienCo != null) {
            int tongSoLuong = gioHangHienCo.getSoLuong() + soLuong;
            if (tongSoLuong > soLuongTon) {
                tongSoLuong = soLuongTon;
                redirect.addFlashAttribute("error", "Số lượng vượt quá tồn kho, đã chỉnh về tối đa");
            } else {
                redirect.addFlashAttribute("success", "Đã cập nhật số lượng sản phẩm trong giỏ hàng");
            }
            gioHangHienCo.setSoLuong(tongSoLuong);
            gioHangHienCo.setThanhTien(chiTiet.getGiaBan().multiply(BigDecimal.valueOf(tongSoLuong)));
            giohangreposiroty.save(gioHangHienCo);

        } else {
            if (soLuong > soLuongTon) {
                soLuong = soLuongTon;
                redirect.addFlashAttribute("error", "Số lượng vượt quá tồn kho, đã chỉnh về tối đa");
            } else {
                redirect.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng thành công");
            }
            GioHangChiTiet gh = new GioHangChiTiet();
            gh.setKhachHang(kh);
            gh.setChiTietSp(chiTiet);
            gh.setSoLuong(soLuong);
            gh.setTrangThai(0);
            gh.setThanhTien(chiTiet.getGiaBan().multiply(BigDecimal.valueOf(soLuong)));
            giohangreposiroty.save(gh);
        }

        // Redirect về trang giỏ hàng thay vì home
        return "redirect:/gio-hang/hien_thi";
    }


    @GetMapping("/xoa/{id}")
    public String xoaSanPhamKhoiGio(@PathVariable("id") Integer id,
                                    RedirectAttributes redirectAttributes) {
        try {
            if (giohangreposiroty.existsById(id)) {
                giohangreposiroty.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại trong giỏ hàng.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/gio-hang/hien_thi";
    }

    @GetMapping("/cap-nhat/{id}")
    public String capNhatSoLuong(
            @PathVariable("id") Integer id,
            @RequestParam("action") String action,
            @RequestParam(value = "newSoluong", required = false) Integer newSoluong,
            @RequestParam(value = "soluong", required = false) Integer soluong,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Optional<GioHangChiTiet> optionalItem = giohangreposiroty.findById(id);
            if (!optionalItem.isPresent()) {
                ThongBaoUtils.addError(redirectAttributes, "Không tìm thấy sản phẩm trong giỏ hàng");
                return "redirect:/gio-hang/hien_thi";
            }
            
            GioHangChiTiet item = optionalItem.get();
            
            // Kiểm tra xem item có thuộc về khách hàng hiện tại không
            KhachHang currentKh = getCurrentKhachHang();
            if (!item.getKhachHang().getId().equals(currentKh.getId())) {
                ThongBaoUtils.addError(redirectAttributes, "Bạn không có quyền cập nhật sản phẩm này");
                return "redirect:/gio-hang/hien_thi";
            }
            
            int soLuongTonKho = item.getChiTietSp().getSoLuong();

            int soLuongMoi;
            if ("update".equals(action) && newSoluong != null && newSoluong > 0) {
                // Cập nhật trực tiếp từ JavaScript
                soLuongMoi = newSoluong;
            } else if (newSoluong != null && newSoluong > 0 && !"increase".equals(action) && !"decrease".equals(action)) {
                // Nếu nhập tay (không phải bấm + hoặc -)
                soLuongMoi = newSoluong;
            } else if ("increase".equals(action)) {
                soLuongMoi = item.getSoLuong() + 1;
            } else if ("decrease".equals(action)) {
                soLuongMoi = Math.max(item.getSoLuong() - 1, 1);
            } else {
                soLuongMoi = item.getSoLuong();
            }

            // Kiểm tra số lượng hợp lệ
            if (soLuongMoi < 1) {
                ThongBaoUtils.addError(redirectAttributes, "Số lượng không hợp lệ");
                return "redirect:/gio-hang/hien_thi";
            }

            if (soLuongMoi > soLuongTonKho) {
                ThongBaoUtils.addError(redirectAttributes, "Số lượng vượt quá tồn kho (còn " + soLuongTonKho + " sản phẩm)");
                return "redirect:/gio-hang/hien_thi";
            }

            // Cập nhật số lượng và thành tiền
            item.setSoLuong(soLuongMoi);
            BigDecimal giaBan = item.getChiTietSp().getGiaBan();
            item.setThanhTien(giaBan.multiply(BigDecimal.valueOf(soLuongMoi)));

            giohangreposiroty.save(item);

            ThongBaoUtils.addSuccess(redirectAttributes, "Cập nhật số lượng thành công");
            
        } catch (Exception e) {
            ThongBaoUtils.addError(redirectAttributes, "Có lỗi xảy ra khi cập nhật số lượng: " + e.getMessage());
        }

        return "redirect:/gio-hang/hien_thi";
    }

    public PhieuGiamGia timPhieuTotNhat(List<PhieuGiamGia> dsPhieu, BigDecimal tongTien) {
        return dsPhieu.stream()
                .filter(p -> p.getNgayKetThuc().isAfter(LocalDate.now())) // còn hạn
                .filter(p -> tongTien.compareTo(p.getDieuKien()) >= 0)    // đủ điều kiện
                .max(Comparator.comparing(p -> {
                    BigDecimal soTienGiamThucTe = BigDecimal.ZERO; // giá trị mặc định

                    if (p.getLoaiPhieuGiamGia() == 1) {
                        // Giảm theo %
                        BigDecimal giamTheoPhanTram = tongTien.multiply(p.getMucDo())
                                .divide(BigDecimal.valueOf(100));
                        soTienGiamThucTe = giamTheoPhanTram.min(p.getGiamToiDa());
                    } else if (p.getLoaiPhieuGiamGia() == 2) {
                        // Giảm theo số tiền cố định
                        soTienGiamThucTe = p.getMucDo();
                    }

                    return soTienGiamThucTe;
                }))
                .orElse(null);
    }

    @GetMapping("/thanh-toan")
    public String hienThiTrangThanhToan(
            @RequestParam(value = "selectedId", required = false) List<Integer> selectedIds,
            @RequestParam(required = false) Integer provinceId,
            @RequestParam(required = false) Integer districtId,
            @RequestParam(required = false) String wardCode,
            Model model) {

        if (selectedIds == null) {
            selectedIds = Collections.emptyList();
        }

        // Lấy khách hàng đang đăng nhập
        KhachHang kh = getCurrentKhachHang();
        model.addAttribute("khachHang", kh);

        // Nếu KH có lưu địa chỉ dạng string => parse ra
        if (kh.getDiaChi() != null && !kh.getDiaChi().isEmpty()) {
            try {
                String[] parts = kh.getDiaChi().split(",");
                if (parts.length >= 4) {
                    String chiTiet = parts[0].trim();   // ví dụ: "Tân Thanh"
                    String xa = parts[1].trim();        // "Xã Minh Tân"
                    String huyen = parts[2].trim();     // "Huyện Lương Tài"
                    String tinh = parts[3].trim();      // "Bắc Ninh"

                    // Tìm provinceId
                    if (provinceId == null) {
                        for (Map<String, Object> p : ghnService.getProvinces()) {
                            if (p.get("ProvinceName").toString().equalsIgnoreCase(tinh)) {
                                provinceId = (Integer) p.get("ProvinceID");
                                break;
                            }
                        }
                    }

                    // Tìm districtId
                    if (provinceId != null && districtId == null) {
                        for (Map<String, Object> d : ghnService.getDistricts(provinceId)) {
                            if (d.get("DistrictName").toString().equalsIgnoreCase(huyen)) {
                                districtId = (Integer) d.get("DistrictID");
                                break;
                            }
                        }
                    }

                    // Tìm wardCode
                    if (districtId != null && wardCode == null) {
                        for (Map<String, Object> w : ghnService.getWards(districtId)) {
                            if (w.get("WardName").toString().equalsIgnoreCase(xa)) {
                                wardCode = (String) w.get("WardCode");
                                break;
                            }
                        }
                    }

                    // Gửi lại địa chỉ chi tiết nếu cần
                    model.addAttribute("diaChiChiTiet", chiTiet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Lấy giỏ hàng đã chọn
        List<GioHangChiTiet> selectedItems = giohangservice.findByIds(selectedIds);
        BigDecimal tongTien = selectedItems.stream()
                .map(item -> item.getChiTietSp().getGiaBan()
                        .multiply(BigDecimal.valueOf(item.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Danh sách phiếu giảm giá hợp lệ
        LocalDate today = LocalDate.now();
        List<PhieuGiamGia> dsPhieuGiamGia = phieugiamgiarepository.findAll().stream()
                .filter(phieu -> Boolean.TRUE.equals(phieu.getTrangThai()))
                .filter(phieu -> phieu.getSoLuongTon() != null && phieu.getSoLuongTon() > 0)
                .filter(phieu -> (phieu.getNgayBatDau() == null || !today.isBefore(phieu.getNgayBatDau())))
                .filter(phieu -> (phieu.getNgayKetThuc() == null || !today.isAfter(phieu.getNgayKetThuc())))
                .collect(Collectors.toList());

        model.addAttribute("danhSachPhieuGiamGia", dsPhieuGiamGia);
        model.addAttribute("selectedItems", selectedItems);
        model.addAttribute("tongTien", tongTien);

        // Load GHN provinces/districts/wards
        List<Map<String, Object>> provinces = ghnService.getProvinces();
        model.addAttribute("provinces", provinces);

        if (provinceId != null) {
            List<Map<String, Object>> districts = ghnService.getDistricts(provinceId);
            model.addAttribute("districts", districts);
        }

        if (districtId != null) {
            List<Map<String, Object>> wards = ghnService.getWards(districtId);
            model.addAttribute("wards", wards);
        }

        // Gửi selected value cho view
        model.addAttribute("provinceId", provinceId);
        model.addAttribute("districtId", districtId);
        model.addAttribute("wardCode", wardCode);

        // Tìm phiếu giảm tốt nhất
        PhieuGiamGia phieuTotNhat = timPhieuTotNhat(dsPhieuGiamGia, tongTien);
        model.addAttribute("phieuTotNhat", phieuTotNhat);

        return "/client/pages/cart/checkout";
    }

    @PostMapping("/thanh-toan/xac-nhan")
    @Transactional
    public String xacNhanThanhToan(@RequestParam("phuongThucThanhToan") String phuongThuc,
                                   @RequestParam("selectedId") List<Integer> selectedItemIds,
                                   @RequestParam Map<String, String> formData,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {

        // Lấy địa chỉ chi tiết và các thành phần
        String diaChiChiTiet = formData.get("diaChi");
        String tenXa = formData.get("tenXa");
        String tenHuyen = formData.get("tenHuyen");
        String tenTinh = formData.get("tenTinh");

        // Check địa chỉ rỗng hoặc thiếu
        if (diaChiChiTiet == null || diaChiChiTiet.isBlank()
                || tenXa == null || tenXa.isBlank()
                || tenHuyen == null || tenHuyen.isBlank()
                || tenTinh == null || tenTinh.isBlank()) {

            ThongBaoUtils.addError(redirectAttributes, "Vui lòng nhập đầy đủ địa chỉ giao hàng.");
            String joinedIds = selectedItemIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("&selectedId="));
            return "redirect:/gio-hang/thanh-toan?selectedId=" + joinedIds;
        }

        String fullAddress = diaChiChiTiet + ", " + tenXa + ", " + tenHuyen + ", " + tenTinh;

        KhachHang khachHang = (KhachHang) session.getAttribute("khachHang");
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }

        List<GioHangChiTiet> gioHangChiTiets = giohangreposiroty.findAllById(selectedItemIds);

        // ✅ KIỂM TRA NGAY Ở ĐÂY
        if (!chiTietSanPhamService.kiemTraTonKho(gioHangChiTiets)) {
            ThongBaoUtils.addError(redirectAttributes, "Sản phẩm đã vượt quá tồn kho.");
            String joinedIds = selectedItemIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("&selectedId="));
            return "redirect:/gio-hang/thanh-toan?selectedId=" + joinedIds;
        }

        // 4. Lọc sản phẩm khách đã chọn từ giỏ hàng của chính họ
        BigDecimal tongTienGoc = BigDecimal.ZERO;
        for (GioHangChiTiet item : gioHangChiTiets) {
            BigDecimal gia = item.getChiTietSp().getGiaBan();
            tongTienGoc = tongTienGoc.add(gia.multiply(BigDecimal.valueOf(item.getSoLuong())));
        }

        BigDecimal tienGiam = new BigDecimal(formData.getOrDefault("tienGiam", "0"));
        BigDecimal phiVanChuyen = new BigDecimal(formData.getOrDefault("tienVanChuyen", "0"));
        BigDecimal thanhTien = tongTienGoc.subtract(tienGiam).add(phiVanChuyen);

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMa("HD" + System.currentTimeMillis());
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setNgayThanhToan(LocalDateTime.now());
        hoaDon.setNgaySua(LocalDateTime.now());
        hoaDon.setPhuongThuc(phuongThuc);
        hoaDon.setLoaihoadon(formData.get("loaiHoaDon"));
        hoaDon.setTenNguoiNhan(formData.get("tenNguoiNhan"));
        hoaDon.setSoDienThoai(formData.get("soDienThoai"));
        hoaDon.setEmail(formData.get("email"));
        hoaDon.setGhiChu(formData.get("ghiChu"));
        hoaDon.setDiaChi(fullAddress);
        hoaDon.setGiaGoc(tongTienGoc);
        hoaDon.setGiaGiamGia(tienGiam);
        hoaDon.setPhiVanChuyen(phiVanChuyen);
        hoaDon.setThanhTien(thanhTien);

        KhachHang kh = getCurrentKhachHang();
        NhanVien nv = nhanvienrepository.find(1);
        hoaDon.setKhachHang(kh);
        hoaDon.setNhanVien(nv);
        hoaDon.setNguoiTao(1);
        hoaDon.setNguoiSua(1);
//        System.out.println("pgg: " + formData);

        if (formData.containsKey("phieuGiamGia") && !formData.get("phieuGiamGia").isBlank()) {
            PhieuGiamGia phieu = phieugiamgiarepository.findByMa(formData.get("phieuGiamGia"));
//            System.out.println("Tim thay 1.1: " + phieu);
            hoaDon.setPhieuGiamGia(phieu);
        }
        hoaDon.setTrangThai(1);
        hoadonreposiroty.save(hoaDon);

        //Thêm lịch sử hóa đơn
        LichSuHoaDon lichSuHoaDon = new LichSuHoaDon();
        lichSuHoaDon.setNgayTao(LocalDateTime.now());
        lichSuHoaDon.setTrangThai(1);
        lichSuHoaDon.setGhiChu(getCurrentKhachHang().getTen() + " đặt hàng, chờ xác nhận.");
        lichSuHoaDon.setHoaDon(hoaDon);
        lichSuHoaDonRepository.save(lichSuHoaDon);

        if (phuongThuc.equalsIgnoreCase("tien_mat")) {
            xuLySauKhiDatHang(hoaDon, gioHangChiTiets, tienGiam, 2);
            model.addAttribute("ma", hoaDon.getMa());
            model.addAttribute("message", "Đặt hàng tiền mặt thành công!");
        }
        if ("chuyen_khoan".equalsIgnoreCase(phuongThuc)) {
            String ids = gioHangChiTiets.stream()
                    .map(ct -> String.valueOf(ct.getId()))
                    .collect(Collectors.joining(","));

            return "redirect:/thanh-toan-vnpay?maHoaDon=" + hoaDon.getMa() + "&ids=" + ids;
        }
        model.addAttribute("maHoaDon", hoaDon.getMa());
        model.addAttribute("tienThanhToanThanhCong", thanhTien);
        model.addAttribute("ngayThanhToan", LocalDateTime.now());
        return "client/pages/cart/success";
    }

    public void xuLySauKhiDatHang(HoaDon hoaDon, List<GioHangChiTiet> gioHangChiTiets, BigDecimal tienGiam, int trangThai) {
        for (GioHangChiTiet item : gioHangChiTiets) {
            ChiTietSanPham ctsp = item.getChiTietSp();
            int soLuong = item.getSoLuong();

            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(hoaDon);
            hdct.setChiTietSanPham(ctsp);
            hdct.setSoLuong(soLuong);
            hdct.setGiaGoc(ctsp.getGiaGoc());
            hdct.setGiaSauGiam(ctsp.getGiaBan());
            hdct.setGiaGiam(ctsp.getGiaGoc().subtract(ctsp.getGiaBan()));
            hdct.setTenCtsp(ctsp.getSanPham().getTen() + " - " + ctsp.getTenCt());

            hoadonCTreposiroty.save(hdct);
        }

        if (hoaDon.getPhieuGiamGia() != null) {
//            System.out.println("tim thay pgg: " + hoaDon.getPhieuGiamGia());
            PhieuGiamGia phieu = hoaDon.getPhieuGiamGia();
            if (phieu.getSoLuongTon() != null && phieu.getSoLuongTon() > 0) {
//                System.out.println("sl: " + phieu.getSoLuongTon());
                phieu.setSoLuongTon(phieu.getSoLuongTon() - 1);
                phieugiamgiarepository.save(phieu);
            }
        }

        // Xóa khỏi giỏ hàng
        giohangreposiroty.deleteAll(gioHangChiTiets);

        //Thêm hóa đơn mới
        HoaDon hd = hoadonreposiroty.findById(hoaDon.getId()).orElse(null);

        hd.setTrangThai(trangThai);
        hd.setLoaihoadon("Online");
        hoadonreposiroty.save(hd);
    }


    @GetMapping("/thanh-toan/location")
    @ResponseBody
    public List<Map<String, Object>> getLocation(
            @RequestParam(required = false) Integer provinceId,
            @RequestParam(required = false) Integer districtId) {
        if (provinceId != null) {
            return ghnService.getDistricts(provinceId);
        }
        if (districtId != null) {
            return ghnService.getWards(districtId);
        }
        return List.of();
    }

    @GetMapping("/thanh-toan/shipping-fee")
    @ResponseBody
    public ResponseEntity<?> getShippingFee(@RequestParam("districtId") int districtId,
                                            @RequestParam("wardCode") String wardCode) {
        int fromDistrictId = 3440;
        int weight = 500;

        List<Map<String, Object>> services = ghnService.getAvailableServices(fromDistrictId, districtId);

        if (services.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không tìm thấy dịch vụ vận chuyển phù hợp.");
        }

        int serviceId = (Integer) services.get(0).get("service_id");
        Integer fee = ghnService.getShippingFee(fromDistrictId, districtId, wardCode, weight, serviceId);

        if (fee == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi tính phí vận chuyển hoặc phí bằng 0.");
        }

        return ResponseEntity.ok(fee);
    }

    @GetMapping("/phieu-giam-gia/tien-giam")
    @ResponseBody
    public ResponseEntity<BigDecimal> tinhTienGiam(
            @RequestParam("maPhieu") String maPhieu,
            @RequestParam("tongTien") BigDecimal tongTien) {
        PhieuGiamGia phieu = phieugiamgiarepository.findByMa(maPhieu);
        if (phieu == null) return ResponseEntity.ok(BigDecimal.ZERO);

        LocalDate today = LocalDate.now();

        // Kiểm tra ngày hiệu lực
        if (phieu.getNgayBatDau() != null && today.isBefore(phieu.getNgayBatDau())) {
            return ResponseEntity.ok(BigDecimal.ZERO);
        }
        if (phieu.getNgayKetThuc() != null && today.isAfter(phieu.getNgayKetThuc())) {
            return ResponseEntity.ok(BigDecimal.ZERO);
        }

        // Kiểm tra tổng tiền tối thiểu áp dụng giảm giá
        if (phieu.getDieuKien() != null && tongTien.compareTo(phieu.getDieuKien()) < 0) {
            return ResponseEntity.ok(BigDecimal.ZERO);
        }

        BigDecimal tienGiam = BigDecimal.ZERO;

        if (phieu.getLoaiPhieuGiamGia() == 1) {
            tienGiam = tongTien.multiply(phieu.getMucDo())
                    .divide(BigDecimal.valueOf(100));
            if (tienGiam.compareTo(phieu.getGiamToiDa()) > 0) {
                tienGiam = phieu.getGiamToiDa();
            }
        } else {
            tienGiam = phieu.getMucDo();
        }
        
        if (tienGiam.compareTo(tongTien) > 0) {
            tienGiam = tongTien;
        }

        return ResponseEntity.ok(tienGiam);
    }


}