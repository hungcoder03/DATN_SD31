package com.example.demo.controler;

import com.example.demo.DTO.ChiTietSanPhamForm;
import com.example.demo.DTO.Sanphamform;
import com.example.demo.entity.ChiTietSanPham;
import com.example.demo.entity.DotGiamGia;
import com.example.demo.entity.HinhAnh;
import com.example.demo.entity.MauSac;
import com.example.demo.entity.SanPham;
import com.example.demo.entity.Size;
import com.example.demo.repository.*;
import com.example.demo.service.Sanphamservice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/san-pham")
public class SanPhamController {

    private final Sanphamservice sanPhamService;
    private final Nhanvienrepository nhanvienRepo;
    private final Chatlieurepository chatLieuRepo;
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
    private final Dotgiamgiarepository dotgiamgiarepository;

    @GetMapping("/hien_thi")
    public String hienthi(Model model) {
        model.addAttribute("list", sanPhamService.getAll());
        model.addAttribute("activePage", "sanpham");
        model.addAttribute("page", "/view/sanpham/list"); // Tên file HTML chứa nội dung phần Sản Phẩm
        return "/view/index";
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
        return "/view/sanpham/add";
    }

    @PostMapping("/them")
    public String themSanPham(@ModelAttribute("form") Sanphamform form,
                              @RequestParam("anhChinh") MultipartFile anhChinh,
                              @RequestParam("anhPhu") List<MultipartFile> anhPhuList) throws IOException {

        // 1. Lưu sản phẩm trước
        SanPham sanPham = sanPhamService.createSanPham(form); // hoặc gọi service

        // 2. Tạo thư mục lưu ảnh nếu chưa có
        String uploadDir = "E:/DATN/buoi9joinbang/uploads/";
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
            anh.setNgayTao(LocalDate.now());
            anh.setNgaySua(LocalDate.now());
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
                anhPhu.setNgayTao(LocalDate.now());
                anhPhu.setNgaySua(LocalDate.now());
                anhPhu.setTrangThai(true);

                hinhanhrepository.save(anhPhu);
            }
        }

        return "redirect:/admin/san-pham/hien_thi";
    }

    @GetMapping("/xoa/{id}")
    public String xoa(@PathVariable("id") Integer id) {
        sanPhamService.delete(id);
        return "redirect:/admin/san-pham/hien_thi";
    }
    @GetMapping("/sua/{id}")
    public String sua(@PathVariable("id") Integer id,Model model) {
        model.addAttribute("sanpham",sanPhamService.findbyid(id));
        model.addAttribute("hinhanh",hinhanhrepository.findByhinhanhid(id));
        return "/view/sanpham/sua-san-pham";
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

        return "/view/sanpham/xemchitiet";
    }



//    @GetMapping("/hinh-anh/them/{id}")
//    public String hienFormThem(Model model,@PathVariable("id") Integer id) {
//        model.addAttribute("hinhanh", new HinhAnh());
//        model.addAttribute("dsSanPham", sanPhamService.findbyid(id));
//        return "/view/sanpham/themanh";
//    }
//
//    // Xử lý lưu ảnh
//    @PostMapping("/hinh-anh/luu")
//    public String luuHinhAnh(@ModelAttribute HinhAnh hinhanh,
//                             @RequestParam("file") MultipartFile file,
//                             @RequestParam("files") List<MultipartFile> files) throws IOException {
//
//        String uploadDir = "E:/DATN/buoi9joinbang/uploads/";
//        Path uploadPath = Paths.get(uploadDir);
//        if (!Files.exists(uploadPath)) {
//            Files.createDirectories(uploadPath);
//        }
//
//        // 1. Lưu ảnh chính
//        if (!file.isEmpty()) {
//            String originalFilename = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-]", "_");
//            String fileName = System.currentTimeMillis() + "_" + originalFilename;
//            Path path = uploadPath.resolve(fileName);
//            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//            hinhanh.setUrl("/uploads/" + fileName);
//            hinhanh.setLoaiAnh(0); // ảnh chính
//            hinhanhrepository.save(hinhanh);
//        }
//
//        // 2. Lưu các ảnh phụ (nếu có)
//        for (MultipartFile subFile : files) {
//            if (!subFile.isEmpty()) {
//                String originalFilename = subFile.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-]", "_");
//                String fileName = System.currentTimeMillis() + "_" + originalFilename;
//                Path path = uploadPath.resolve(fileName);
//                Files.copy(subFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//                HinhAnh anhPhu = new HinhAnh();
//                anhPhu.setMa(hinhanh.getMa());
//                anhPhu.setTen(hinhanh.getTen());
//                anhPhu.setSanPham(hinhanh.getSanPham());
//                anhPhu.setNgaySua(hinhanh.getNgaySua());
//                anhPhu.setNgayTao(hinhanh.getNgayTao());
//                anhPhu.setTrangThai(hinhanh.getTrangThai());
//                anhPhu.setUrl("/uploads/" + fileName);
//                anhPhu.setLoaiAnh(1); // ảnh phụ
//
//                hinhanhrepository.save(anhPhu);
//            }
//        }
//
//        return "redirect:/admin/san-pham/hien_thi";
//    }

    @GetMapping("/khach-hang/danh-sach")
    public String hienThiDanhSachSanPham(Model model) {
        List<SanPham> danhSach = sanPhamService.getAll();
        model.addAttribute("danhSachSanPham", danhSach);
        return "/view/khachhang/dssanpham";
    }

    @GetMapping("/khach-hang/chi-tiet/{id}")
    public String xemChiTietSanPham(@PathVariable("id") Integer id, Model model) {
        List<ChiTietSanPham> danhSachChiTiet = chitietsanphamRepo.findBySanPhamId(id);
        model.addAttribute("sanPham", sanPhamService.findbyid(id));
        model.addAttribute("dsSanPham", sanPhamService.getAll());
        model.addAttribute("hinhanh", hinhanhrepository.findByhinhanhid(id));

        // Gửi danh sách màu sắc duy nhất
        List<MauSac> dsMauSac = danhSachChiTiet.stream()
                .map(ChiTietSanPham::getMauSac)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(MauSac::getId, Function.identity(), (a, b) -> a),
                        map -> new ArrayList<>(map.values())
                ));
        model.addAttribute("dsMauSac", dsMauSac);
        model.addAttribute("mauSacCount", dsMauSac.size());

        // Gửi danh sách size duy nhất
        List<Size> dsSize = danhSachChiTiet.stream()
                .map(ChiTietSanPham::getSize)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Size::getId, Function.identity(), (a, b) -> a),
                        map -> new ArrayList<>(map.values())
                ));
        model.addAttribute("dsSize", dsSize);
        model.addAttribute("sizeCount", dsSize.size());

        // ✅ Gửi danh sách chi tiết với tồn kho
        model.addAttribute("dsChiTietSanPham", danhSachChiTiet.stream().map(ct -> Map.of(
                "id", ct.getId(),
                "giaBan", ct.getGiaBan(),
                "size", Map.of("id", ct.getSize().getId()),
                "mauSac", Map.of("id", ct.getMauSac().getId()),
                "soLuongTon", ct.getSoLuong()
        )).collect(Collectors.toList()));

        return "/view/khachhang/xemchitiet";
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

        return "/view/sanpham/xemchitiet";
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
            mauMoi.setNgayTao(LocalDate.now());
            mauMoi.setNgaySua(LocalDate.now());
            mauMoi.setNguoiTao(1); // hoặc từ user đăng nhập
            mausacrepository.save(mauMoi);

            redirectAttributes.addFlashAttribute("message", "Đã thêm màu mới: " + mauMoi.getTen());
            return "redirect:/admin/san-pham/chitietsanpham/them?id=" + sanPhamId + "&mauId=" + mauMoi.getId();
        }

        // Trường hợp không hợp lệ
        redirectAttributes.addFlashAttribute("error", "Vui lòng chọn màu hoặc nhập đầy đủ thông tin màu mới.");
        return "redirect:/admin/san-pham/xem/" + sanPhamId + "?themMau=true";
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








}




