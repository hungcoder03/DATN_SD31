package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.dto.san_pham_DTO.ChiTietSanPhamForm;
import com.main.datn_sd31.dto.san_pham_DTO.Sanphamform;
import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.DotGiamGia;
import com.main.datn_sd31.entity.HinhAnh;
import com.main.datn_sd31.entity.MauSac;
import com.main.datn_sd31.entity.SanPham;
import com.main.datn_sd31.entity.Size;
import com.main.datn_sd31.repository.ChatLieuRepository;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.Danhmucrepository;
import com.main.datn_sd31.repository.Dotgiamgiarepository;
import com.main.datn_sd31.repository.Hinhanhrepository;
import com.main.datn_sd31.repository.Kieudangrepository;
import com.main.datn_sd31.repository.Loaithurepository;
import com.main.datn_sd31.repository.Mausacrepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.repository.SanPhamRepository;
import com.main.datn_sd31.repository.Sizerepository;
import com.main.datn_sd31.repository.Thuonghieurepository;
import com.main.datn_sd31.repository.Xuatxurepository;
import com.main.datn_sd31.service.impl.Sanphamservice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/san-pham")
public class SanPhamController {

    private final Sanphamservice sanPhamService;
    private final NhanVienRepository nhanvienRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final Danhmucrepository danhMucRepo;
    private final Thuonghieurepository thuongHieuRepo;
    private final Xuatxurepository xuatXuRepo;
    private final Kieudangrepository kieuDangRepo;
    private final Sizerepository sizerepository;
    private final Mausacrepository mausacrepository;
    private final Xuatxurepository xuatxurepository;
    private final Chitietsanphamrepository chitietsanphamRepo;
    private final Hinhanhrepository hinhanhrepository;
    private final Loaithurepository loaithurepository;
    private final SanPhamRepository sanPhamRepository;
    private final Dotgiamgiarepository dotgiamgiarepository;

    @GetMapping("/hien_thi")
    public String hienthi(Model model) {
        List<SanPham> listSanPham = sanPhamService.getAll();
        model.addAttribute("list", listSanPham);
        model.addAttribute("dsDotGiamGia", dotgiamgiarepository.findAll());

        Map<Integer, BigDecimal[]> giaSanPhamMap = new HashMap<>();
        for (SanPham sp : listSanPham) {
            List<BigDecimal> giaList = sp.getChiTietSanPhams().stream()
                    .map(ChiTietSanPham::getGiaBan)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!giaList.isEmpty()) {
                BigDecimal min = Collections.min(giaList);
                BigDecimal max = Collections.max(giaList);
                giaSanPhamMap.put(sp.getId(), new BigDecimal[]{min, max});
            }
        }
        Map<Integer, DotGiamGia> dotMap = new HashMap<>();

        for (SanPham sp : listSanPham) {
            Optional<DotGiamGia> optionalDot = sp.getChiTietSanPhams().stream()
                    .map(ChiTietSanPham::getDotGiamGia)
                    .filter(Objects::nonNull)
                    .findFirst();

            optionalDot.ifPresent(dot -> dotMap.put(sp.getId(), dot));
        }

        model.addAttribute("dotMap", dotMap);

        model.addAttribute("giaSanPhamMap", giaSanPhamMap);
        return "admin/pages/sanpham/list";
    }

    @GetMapping("/them")
    public String hienThiForm(Model model) {
        model.addAttribute("form", new Sanphamform());
        model.addAttribute("nhanvien", nhanvienRepo.findAll());
        model.addAttribute("chatLieus", chatLieuRepo.findAll());
        model.addAttribute("danhMucs", danhMucRepo.findAll());
        model.addAttribute("thuongHieus", thuongHieuRepo.findAll());
        model.addAttribute("xuatXus", xuatXuRepo.findAll());
        model.addAttribute("kieuDangs", kieuDangRepo.findAll());
        model.addAttribute("loaiThus", loaithurepository.findAll());
        return "admin/pages/sanpham/add";
    }

    @PostMapping("/them")
    public String themSanPham(@ModelAttribute("form") Sanphamform form,
                              @RequestParam("anhChinh") MultipartFile anhChinh,
                              @RequestParam("anhPhu") List<MultipartFile> anhPhuList) throws IOException {

        // 1. Lưu sản phẩm trước
        SanPham sanPham = sanPhamService.createSanPham(form); // hoặc gọi service

        // 2. Tạo thư mục lưu ảnh nếu chưa có
        String uploadDir = "src/main/resources/static/uploads/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 3. Lưu ảnh chính
        if (!anhChinh.isEmpty()) {
            String tenFile = UUID.randomUUID().toString() + "_" + anhChinh.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-]", "_");
            Path path = uploadPath.resolve(tenFile);
            Files.copy(anhChinh.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            HinhAnh anh = new HinhAnh();
            anh.setSanPham(sanPham);
            anh.setMa("ANHCHINH_" + System.currentTimeMillis());
            anh.setTen("Ảnh chính");
            anh.setUrl("/uploads/" + tenFile);
            anh.setLoaiAnh(0); // ảnh chính
            anh.setNgayTao(LocalDateTime.now());
            anh.setNgaySua(LocalDateTime.now());
            anh.setTrangThai(true);

            hinhanhrepository.save(anh);
        }

        // 4. Lưu ảnh phụ
        for (MultipartFile file : anhPhuList) {
            if (!file.isEmpty()) {
                String tenFile = UUID.randomUUID().toString() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-]", "_");
                Path path = uploadPath.resolve(tenFile);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                HinhAnh anhPhu = new HinhAnh();
                anhPhu.setSanPham(sanPham);
                anhPhu.setMa("ANHPHU_" + System.currentTimeMillis());
                anhPhu.setTen("Ảnh phụ");
                anhPhu.setUrl("/uploads/" + tenFile);
                anhPhu.setLoaiAnh(1); // ảnh phụ
                anhPhu.setNgayTao(LocalDateTime.now());
                anhPhu.setNgaySua(LocalDateTime.now());
                anhPhu.setTrangThai(true);

                hinhanhrepository.save(anhPhu);
            }
        }

        return "redirect:/admin/san-pham/hien_thi";
    }

    @GetMapping("/xoa/{id}")
    public String xoa(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        SanPham sp = sanPhamService.findbyid(id);
        if (sp != null) {
            List<ChiTietSanPham> danhSachChiTiet = chitietsanphamRepo.findBySanPhamId(id);

            if (danhSachChiTiet != null && !danhSachChiTiet.isEmpty()) {
                // Không cho phép xóa nếu còn chi tiết sản phẩm
                redirectAttributes.addFlashAttribute("error", "Không thể xóa. Sản phẩm còn chi tiết tồn tại.");
                return "redirect:/admin/san-pham/hien_thi";
            }

            sp.setTrangThai(false); // Ngừng hoạt động
            sanPhamRepository.save(sp);
        }

        return "redirect:/admin/san-pham/hien_thi";
    }

    @GetMapping("/sua/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        SanPham sp = sanPhamService.findbyid(id);
        model.addAttribute("sanpham",sp);
        model.addAttribute("chatLieus", chatLieuRepo.findAll());
        model.addAttribute("danhMucs", danhMucRepo.findAll());
        model.addAttribute("thuongHieus", thuongHieuRepo.findAll());
        model.addAttribute("xuatXus", xuatXuRepo.findAll());
        model.addAttribute("kieuDangs", kieuDangRepo.findAll());
        model.addAttribute("loaiThus", loaithurepository.findAll());
        return "admin/pages/sanpham/sua-san-pham"; // HTML path
    }

    @PostMapping("/sua")
    public String suaSanPham(@ModelAttribute("sanpham") SanPham sanPham,
                             RedirectAttributes redirectAttributes) {

        SanPham spGoc = sanPhamService.findbyid(sanPham.getId());

        if (spGoc == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm để cập nhật.");
            return "redirect:/admin/san-pham/hien_thi";
        }

        // Cập nhật thông tin sản phẩm
        spGoc.setMa(sanPham.getMa());
        spGoc.setTen(sanPham.getTen());
        spGoc.setMoTa(sanPham.getMoTa());
        spGoc.setTrangThai(sanPham.getTrangThai());
        spGoc.setChatLieu(sanPham.getChatLieu());
        spGoc.setXuatXu(sanPham.getXuatXu());
        spGoc.setDanhMuc(sanPham.getDanhMuc());
        spGoc.setKieuDang(sanPham.getKieuDang());
        spGoc.setThuongHieu(sanPham.getThuongHieu());
        spGoc.setLoaiThu(sanPham.getLoaiThu());

        // Lưu lại
        sanPhamRepository.save(spGoc);

        redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công.");
        return "redirect:/admin/san-pham/hien_thi";
    }

    @GetMapping("/xem/{id}")
    public String xemSanPhamChiTiet(@PathVariable("id") Integer id,
                                    @RequestParam(value = "mauId", required = false) Integer mauId,
                                    @RequestParam(value = "themMau", required = false) Boolean themMau,
                                    Model model) {

        SanPham sanPham = sanPhamService.findbyid(id);
        List<ChiTietSanPham> danhSachChiTiet = chitietsanphamRepo.findBySanPhamId(id);
        List<Size> allSizes = sizerepository.findAll();
        List<MauSac> danhSachMauSac = mausacrepository.findAll();

        ChiTietSanPhamForm form = new ChiTietSanPhamForm();

        if (mauId != null) {
            MauSac selectedMau = mausacrepository.findById(mauId).orElse(null);
            if (selectedMau != null) {
                List<Size> sizesChuaCo = allSizes.stream()
                        .filter(size -> danhSachChiTiet.stream().noneMatch(ct ->
                                ct.getMauSac().getId().equals(mauId) &&
                                        ct.getSize().getId().equals(size.getId())))
                        .toList();

                for (Size s : sizesChuaCo) {
                    ChiTietSanPham ct = new ChiTietSanPham();
                    ct.setSanPham(sanPham);
                    ct.setMauSac(selectedMau);
                    ct.setSize(s);
                    form.getChiTietList().add(ct);
                }

                model.addAttribute("mauDangChon", selectedMau);
            }
        }

        model.addAttribute("form", form);
        model.addAttribute("sanPham", sanPham);
        model.addAttribute("dsChiTietSanPham", danhSachChiTiet);
        model.addAttribute("cacMau", danhSachMauSac);
        model.addAttribute("dsMauSac", danhSachMauSac);
        model.addAttribute("dsSize", allSizes);
        model.addAttribute("dsSanPham", sanPhamService.getAll());
        model.addAttribute("hinhanh", hinhanhrepository.findByhinhanhid(id));
        model.addAttribute("dsLoaiThu", xuatxurepository.findAll());
        model.addAttribute("themMau", themMau != null && themMau);
        model.addAttribute("dsDotGiamGia", dotgiamgiarepository.findAll());

        return "admin/pages/sanpham/xemchitiet";
    }


    @GetMapping("/chitietsanpham/them")
    public String hienThiFormChiTiet(
            @RequestParam("id") Integer id,
            @RequestParam(value = "mauId", required = false) Integer mauId,
            Model model) {

        SanPham sp = sanPhamService.findbyid(id);
        List<ChiTietSanPham> daCo = chitietsanphamRepo.findBySanPhamId(id);
        List<Size> allSizes = sizerepository.findAll();
        List<MauSac> allMauSacs = mausacrepository.findAll();

        List<MauSac> cacMau = mausacrepository.findAll();

        ChiTietSanPhamForm form = new ChiTietSanPhamForm();

        if (mauId != null) {
            MauSac selectedMau = mausacrepository.findById(mauId).orElse(null);
            if (selectedMau != null) {
                List<Size> sizesChuaCo = allSizes.stream()
                        .filter(size -> daCo.stream().noneMatch(ct ->
                                ct.getMauSac().getId().equals(mauId) &&
                                        ct.getSize().getId().equals(size.getId())))
                        .toList();

                for (Size s : sizesChuaCo) {
                    ChiTietSanPham ct = new ChiTietSanPham();
                    ct.setSanPham(sp);
                    ct.setMauSac(selectedMau);
                    ct.setSize(s);
                    form.getChiTietList().add(ct);
                }

                model.addAttribute("mauDangChon", selectedMau);
            }
        }

        model.addAttribute("form", form);
        model.addAttribute("dsChiTietSanPham", daCo);
        model.addAttribute("sanPham", sp);
        model.addAttribute("cacMau", cacMau);
        model.addAttribute("dsDotGiamGia", dotgiamgiarepository.findAll());

        return "admin/pages/sanpham/xemchitiet";
    }

    @PostMapping("/chitietsanpham/them")
    public String luuChiTietMoi(@ModelAttribute("form") ChiTietSanPhamForm form,
                                @RequestParam("sanPhamId") Integer sanPhamId) {
        SanPham sp = sanPhamService.findbyid(sanPhamId);

        for (ChiTietSanPham ct : form.getChiTietList()) {
            // Load lại Size và MauSac từ DB nếu bị thiếu
            if (ct.getSize() == null || ct.getSize().getId() == null) continue;
            if (ct.getMauSac() == null || ct.getMauSac().getId() == null) continue;

            Size size = sizerepository.findById(ct.getSize().getId()).orElse(null);
            MauSac mau = mausacrepository.findById(ct.getMauSac().getId()).orElse(null);

            if (size == null || mau == null) continue;
            ct.setSize(size);
            ct.setMauSac(mau);

            // Gán tên chi tiết từ màu + size
            ct.setTenCt(mau.getTen() + " - " + size.getTen());

            // Gán trạng thái mặc định
            ct.setTrangThai(true);

            // Gán mô tả và ghi chú null
            ct.setMoTa(null);
            ct.setGhiChu(null);

            String randomMaVach = "SP" + System.currentTimeMillis();
            ct.setMaVach(randomMaVach);
            if (ct.getGiaBan() == null && ct.getGiaGoc() != null) {
                ct.setGiaBan(ct.getGiaGoc());
            }
            if (ct.getGiaBan() == null) {
                ct.setGiaBan(ct.getGiaGoc());
            }
            // Gán sản phẩm nếu chưa có
            if (ct.getSanPham() == null) {
                ct.setSanPham(sp);
            }

            chitietsanphamRepo.save(ct);
        }

        return "redirect:/admin/san-pham/chitietsanpham/them?id=" + sanPhamId;
    }
    @PostMapping("/them-mau")
    public String themMauChoSanPham(
            @RequestParam("sanPhamId") Integer sanPhamId,
            @RequestParam(value = "mauDaCoId", required = false) Integer mauDaCoId,
            @RequestParam(value = "maMau", required = false) String maMau,
            @RequestParam(value = "tenMau", required = false) String tenMau,
            RedirectAttributes redirectAttributes) {

        // Nếu chọn màu đã có
        if (mauDaCoId != null) {
            MauSac mauDaChon = mausacrepository.findById(mauDaCoId).orElse(null);
            if (mauDaChon != null) {
                redirectAttributes.addFlashAttribute("message", "Đã chọn màu: " + mauDaChon.getTen());
                return "redirect:/admin/san-pham/chitietsanpham/them?id=" + sanPhamId + "&mauId=" + mauDaChon.getId();
            }
        }

        // Nếu thêm mới
        if (maMau != null && tenMau != null && !maMau.trim().isEmpty() && !tenMau.trim().isEmpty()) {
            // Kiểm tra mã màu đã tồn tại chưa
            boolean maDaTonTai = mausacrepository.existsByMa(maMau.trim());
            if (maDaTonTai) {
                redirectAttributes.addFlashAttribute("error", "Mã màu đã tồn tại!");
                return "redirect:/admin/san-pham/xem/" + sanPhamId + "?themMau=true";
            }

            MauSac mauMoi = new MauSac();
            mauMoi.setMa(maMau.trim());
            mauMoi.setTen(tenMau.trim());
            mauMoi.setTrangThai(true);
            mauMoi.setNgayTao(LocalDateTime.now());
            mauMoi.setNgaySua(LocalDateTime.now());
            mauMoi.setNguoiTao(1); // hoặc từ user đăng nhập
            mausacrepository.save(mauMoi);

            redirectAttributes.addFlashAttribute("message", "Đã thêm màu mới: " + mauMoi.getTen());
            return "redirect:/admin/san-pham/chitietsanpham/them?id=" + sanPhamId + "&mauId=" + mauMoi.getId();
        }

        // Trường hợp không hợp lệ
        redirectAttributes.addFlashAttribute("error", "Vui lòng chọn màu hoặc nhập đầy đủ thông tin màu mới.");
        return "redirect:/admin/san-pham/xem/" + sanPhamId + "?themMau=true";
    }

    @PostMapping("/ap-dung-giam-gia-nhieu")
    public String apDungGiamGiaNhieu(@RequestParam(value = "dotGiamGiaId", required = false) Integer dotGiamGiaId,
                                     @RequestParam(value = "sanPhamIds", required = false) List<Integer> sanPhamIds,
                                     RedirectAttributes redirectAttributes) {

        if (sanPhamIds == null || sanPhamIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa chọn sản phẩm nào để áp dụng.");
            return "redirect:/admin/san-pham/hien_thi";
        }

        // Nếu không chọn đợt nào → Gỡ đợt giảm giá khỏi CTSP
        if (dotGiamGiaId == null) {
            List<ChiTietSanPham> toUpdate = new ArrayList<>();
            for (Integer spId : sanPhamIds) {
                List<ChiTietSanPham> chiTietList = chitietsanphamRepo.findBySanPhamId(spId);
                for (ChiTietSanPham ct : chiTietList) {
                    ct.setDotGiamGia(null);
                    if (ct.getGiaGoc() != null) {
                        ct.setGiaBan(ct.getGiaGoc());
                    }
                    toUpdate.add(ct);
                }
            }
            chitietsanphamRepo.saveAll(toUpdate);
            redirectAttributes.addFlashAttribute("success", "Đã gỡ đợt giảm giá khỏi các sản phẩm đã chọn.");
            return "redirect:/admin/san-pham/hien_thi";
        }

        // Ngược lại: thực hiện áp dụng đợt giảm giá như cũ
        DotGiamGia dot = dotgiamgiarepository.findById(dotGiamGiaId).orElse(null);
        if (dot == null) {
            redirectAttributes.addFlashAttribute("error", "Đợt giảm giá không tồn tại.");
            return "redirect:/admin/san-pham/hien_thi";
        }

        List<ChiTietSanPham> toUpdate = new ArrayList<>();
        List<String> khongCapNhat = new ArrayList<>();

        for (Integer spId : sanPhamIds) {
            List<ChiTietSanPham> chiTietList = chitietsanphamRepo.findBySanPhamId(spId);
            for (ChiTietSanPham ct : chiTietList) {
                if (ct.getGiaBan() == null || ct.getGiaNhap() == null) continue;

                if (ct.getGiaGoc() == null) {
                    ct.setGiaGoc(ct.getGiaBan());
                }

                BigDecimal giaGoc = ct.getGiaGoc();
                BigDecimal giaNhap = ct.getGiaNhap();
                BigDecimal giam;

                if ("TIEN".equalsIgnoreCase(dot.getLoai())) {
                    giam = dot.getGiaTriDotGiamGia();
                } else {
                    giam = giaGoc.multiply(dot.getGiaTriDotGiamGia())
                            .divide(BigDecimal.valueOf(100));
                }

                BigDecimal giaSauGiam = giaGoc.subtract(giam);
                ct.setDotGiamGia(dot);

                if (giaSauGiam.compareTo(giaNhap) >= 0 && giaSauGiam.compareTo(giaGoc) < 0) {
                    ct.setGiaBan(giaSauGiam);
                    toUpdate.add(ct);
                } else {
                    khongCapNhat.add("CTSP ID: " + ct.getId());
                }
            }
        }

        if (!toUpdate.isEmpty()) {
            chitietsanphamRepo.saveAll(toUpdate);
        }

        if (!khongCapNhat.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning",
                    "Một số chi tiết sản phẩm không cập nhật: " + String.join(", ", khongCapNhat));
        } else {
            redirectAttributes.addFlashAttribute("success", "Áp dụng đợt giảm giá thành công cho các sản phẩm đã chọn.");
        }

        return "redirect:/admin/san-pham/hien_thi";
    }

    @PostMapping("/cap-nhat-dot-giam-gia")
    public String apDungDotGiamGia(@RequestParam("dotGiamGiaId") Integer dotGiamGiaId,
                                   @RequestParam("sanPhamId") Integer sanPhamId,
                                   @RequestParam(value = "chiTietIds", required = false) List<Integer> chiTietIds,
                                   RedirectAttributes redirectAttributes) {

        DotGiamGia dot = dotgiamgiarepository.findById(dotGiamGiaId).orElse(null);
        if (dot == null) {
            redirectAttributes.addFlashAttribute("error", "Đợt giảm giá không tồn tại.");
            return "redirect:/admin/san-pham/hien_thi";
        }

        List<ChiTietSanPham> chiTietList = (chiTietIds != null && !chiTietIds.isEmpty())
                ? chitietsanphamRepo.findAllById(chiTietIds)
                : chitietsanphamRepo.findBySanPhamId(sanPhamId);

        List<String> danhSachKhongCapNhat = new ArrayList<>();
        for (ChiTietSanPham ct : chiTietList) {
            if (ct.getGiaBan() != null && ct.getGiaNhap() != null) {

                if (ct.getGiaGoc() == null) {
                    ct.setGiaGoc(ct.getGiaBan());
                }

                BigDecimal giaGoc = ct.getGiaGoc();
                BigDecimal giaNhap = ct.getGiaNhap();
                BigDecimal giam;

                String loai = dot.getLoai() != null ? dot.getLoai().trim() : "";
                if (loai.equalsIgnoreCase("TIEN")) {
                    giam = dot.getGiaTriDotGiamGia();
                } else {
                    giam = giaGoc.multiply(dot.getGiaTriDotGiamGia()).divide(BigDecimal.valueOf(100));
                }

                BigDecimal giaSauGiam = giaGoc.subtract(giam);
                ct.setDotGiamGia(dot); // Gán đợt giảm giá

                if (giaSauGiam.compareTo(giaNhap) >= 0 && giaSauGiam.compareTo(giaGoc) < 0) {
                    ct.setGiaBan(giaSauGiam);
                } else {
                    danhSachKhongCapNhat.add("ID: " + ct.getId());
                }
            }
        }

        chitietsanphamRepo.saveAll(chiTietList);

        if (!danhSachKhongCapNhat.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning",
                    "Một số sản phẩm không cập nhật do giá sau giảm < giá nhập hoặc không hợp lệ: "
                            + String.join(", ", danhSachKhongCapNhat));
        } else {
            redirectAttributes.addFlashAttribute("success", "Áp dụng đợt giảm giá thành công!");
        }

        return "redirect:/admin/san-pham/xem/" + sanPhamId;
    }
    @PostMapping("/ap-dung-giam-gia-tat-ca")
    public String apDungGiamGiaTatCa(@RequestParam("dotGiamGiaId") Integer dotGiamGiaId,
                                     RedirectAttributes redirectAttributes) {

        DotGiamGia dot = dotgiamgiarepository.findById(dotGiamGiaId).orElse(null);
        if (dot == null) {
            redirectAttributes.addFlashAttribute("error", "Đợt giảm giá không tồn tại.");
            return "redirect:/admin/san-pham/hien_thi";
        }

        List<ChiTietSanPham> allChiTiet = chitietsanphamRepo.findAll();
        List<String> loi = new ArrayList<>();

        for (ChiTietSanPham ct : allChiTiet) {
            if (ct.getGiaBan() == null || ct.getGiaNhap() == null) continue;

            if (ct.getGiaGoc() == null) {
                ct.setGiaGoc(ct.getGiaBan());
            }

            BigDecimal giaGoc = ct.getGiaGoc();
            BigDecimal giaNhap = ct.getGiaNhap();
            BigDecimal giam;

            if ("TIEN".equalsIgnoreCase(dot.getLoai())) {
                giam = dot.getGiaTriDotGiamGia();
            } else {
                giam = giaGoc.multiply(dot.getGiaTriDotGiamGia()).divide(BigDecimal.valueOf(100));
            }

            BigDecimal giaSauGiam = giaGoc.subtract(giam);
            if (giaSauGiam.compareTo(giaNhap) >= 0 && giaSauGiam.compareTo(giaGoc) < 0) {
                ct.setGiaBan(giaSauGiam);
                ct.setDotGiamGia(dot);
            } else {
                loi.add("CTSP ID: " + ct.getId());
            }
        }

        chitietsanphamRepo.saveAll(allChiTiet);

        if (!loi.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Một số sản phẩm không áp dụng được: " + String.join(", ", loi));
        } else {
            redirectAttributes.addFlashAttribute("success", "Đã áp dụng giảm giá cho tất cả sản phẩm.");
        }

        return "redirect:/admin/san-pham/hien_thi";
    }
    @PostMapping("/admin/san-pham/cap-nhat-so-luong")
    public String capNhatSoLuongChiTiet(@RequestParam("id") Integer id,
                                        @RequestParam("soLuong") int soLuong,
                                        RedirectAttributes redirect) {
        ChiTietSanPham ct = chitietsanphamRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm"));

        ct.setSoLuong(soLuong);
        chitietsanphamRepo.save(ct);

        redirect.addFlashAttribute("message", "Cập nhật số lượng thành công!");
        return "redirect:/admin/san-pham/xem/" + ct.getSanPham().getId();
    }
}