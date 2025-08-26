package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.dto.san_pham_DTO.ChiTietSanPhamForm;
import com.main.datn_sd31.dto.san_pham_DTO.SanPhamFilterDTO;
import com.main.datn_sd31.dto.san_pham_DTO.SanPhamListDTO;
import com.main.datn_sd31.dto.san_pham_DTO.Sanphamform;
import com.main.datn_sd31.dto.san_pham_DTO.SanPhamThongKeDTO;
import com.main.datn_sd31.dto.san_pham_DTO.SanPhamExportDTO;
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
import com.main.datn_sd31.service.ChiTietSanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

// Apache POI imports for Excel export
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;

import com.main.datn_sd31.dto.san_pham_DTO.MauBlock;
import com.main.datn_sd31.dto.san_pham_DTO.VariantRow;
import com.main.datn_sd31.util.ColorUtil;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/san-pham")
public class SanPhamController {

    private final Sanphamservice sanPhamService;
    private final ChiTietSanPhamService chiTietSanPhamService;
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
        // Lấy trang đầu tiên với 10 sản phẩm mỗi trang (cho admin - tất cả sản phẩm)
        Page<SanPhamListDTO> pageData = sanPhamService.getAllForAdminDisplayPaginated(0, 10);
        model.addAttribute("list", pageData.getContent());
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalElements", pageData.getTotalElements());
        model.addAttribute("pageSize", 10);
        
        // Thêm dữ liệu cho filter
        model.addAttribute("danhMucs", danhMucRepo.findAll());
        model.addAttribute("thuongHieus", thuongHieuRepo.findAll());
        model.addAttribute("chatLieus", chatLieuRepo.findAll());
        model.addAttribute("xuatXus", xuatXuRepo.findAll());
        model.addAttribute("kieuDangs", kieuDangRepo.findAll());
        model.addAttribute("loaiThus", loaithurepository.findAll());
        
        return "admin/pages/sanpham/list";
    }

    @GetMapping("/api/paginated")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPaginatedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer danhMucId,
            @RequestParam(required = false) Integer loaiThuId,
            @RequestParam(required = false) Integer chatLieuId,
            @RequestParam(required = false) Integer kieuDangId,
            @RequestParam(required = false) Integer thuongHieuId,
            @RequestParam(required = false) Integer xuatXuId,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(required = false) String trangThaiHienThi,
            @RequestParam(required = false) BigDecimal giaMin,
            @RequestParam(required = false) BigDecimal giaMax,
            @RequestParam(required = false) Integer soLuongMin,
            @RequestParam(required = false) Integer soLuongMax,
            @RequestParam(required = false, defaultValue = "ngayTao") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        
        // Tạo filter DTO
        SanPhamFilterDTO filter = SanPhamFilterDTO.builder()
                .keyword(keyword)
                .danhMucId(danhMucId)
                .loaiThuId(loaiThuId)
                .chatLieuId(chatLieuId)
                .kieuDangId(kieuDangId)
                .thuongHieuId(thuongHieuId)
                .xuatXuId(xuatXuId)
                .trangThai(trangThai)
                .trangThaiHienThi(trangThaiHienThi)
                .giaMin(giaMin)
                .giaMax(giaMax)
                .soLuongMin(soLuongMin)
                .soLuongMax(soLuongMax)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .page(page)
                .size(size)
                .build();
        
        Page<SanPhamListDTO> pageData = sanPhamService.getAllForAdminDisplayPaginatedWithFilter(filter);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", pageData.getContent());
        response.put("currentPage", pageData.getNumber());
        response.put("totalPages", pageData.getTotalPages());
        response.put("totalElements", pageData.getTotalElements());
        response.put("pageSize", pageData.getSize());
        response.put("hasNext", pageData.hasNext());
        response.put("hasPrevious", pageData.hasPrevious());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/filter")
    @ResponseBody
    public ResponseEntity<List<SanPhamListDTO>> filterProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer danhMucId,
            @RequestParam(required = false) Integer loaiThuId,
            @RequestParam(required = false) Integer chatLieuId,
            @RequestParam(required = false) Integer kieuDangId,
            @RequestParam(required = false) Integer thuongHieuId,
            @RequestParam(required = false) Integer xuatXuId,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(required = false) String trangThaiHienThi,
            @RequestParam(required = false) BigDecimal giaMin,
            @RequestParam(required = false) BigDecimal giaMax,
            @RequestParam(required = false) Integer soLuongMin,
            @RequestParam(required = false) Integer soLuongMax,
            @RequestParam(required = false, defaultValue = "ngayTao") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        
        SanPhamFilterDTO filter = SanPhamFilterDTO.builder()
                .keyword(keyword)
                .danhMucId(danhMucId)
                .loaiThuId(loaiThuId)
                .chatLieuId(chatLieuId)
                .kieuDangId(kieuDangId)
                .thuongHieuId(thuongHieuId)
                .xuatXuId(xuatXuId)
                .trangThai(trangThai)
                .trangThaiHienThi(trangThaiHienThi)
                .giaMin(giaMin)
                .giaMax(giaMax)
                .soLuongMin(soLuongMin)
                .soLuongMax(soLuongMax)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .build();
        
        List<SanPhamListDTO> result = sanPhamService.getFilteredProductsForAdmin(filter);
        return ResponseEntity.ok(result);
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
                                    @RequestParam(value = "colorIds", required = false) List<Integer> colorIds,
                                    @RequestParam(value = "themMau", required = false) Boolean themMau,
                                    @RequestParam(value = "page", defaultValue = "0") Integer page,
                                    @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
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

		        Page<ChiTietSanPham> pageChiTiet = chitietsanphamRepo.findBySanPham_Id(id, PageRequest.of(page, pageSize));
		model.addAttribute("pageChiTiet", pageChiTiet);

		// Build color blocks (multi-color if colorIds provided; fallback to single mauId)
		List<MauBlock> mauBlocks = new ArrayList<>();
		List<Integer> targetColorIds = colorIds != null && !colorIds.isEmpty()
				? colorIds
				: (mauId != null ? List.of(mauId) : List.of());
		model.addAttribute("selectedColorIds", targetColorIds);
		for (Integer colorId : targetColorIds) {
			MauSac selectedMau = mausacrepository.findById(colorId).orElse(null);
			if (selectedMau == null) continue;
			MauBlock block = new MauBlock();
			block.setMau(selectedMau);
			block.setLightText(ColorUtil.isLightColor(selectedMau.getMaMau()));
			List<VariantRow> rows = new ArrayList<>();
			int nextIndex = 0;
			for (Size s : allSizes) {
				ChiTietSanPham existing = danhSachChiTiet.stream()
						.filter(ct -> ct.getMauSac().getId().equals(selectedMau.getId()) && ct.getSize().getId().equals(s.getId()))
						.findFirst().orElse(null);
				if (existing != null) {
					rows.add(new VariantRow(s, true, existing.getGiaGoc(), existing.getGiaNhap(), existing.getGiaBan(), existing.getSoLuong(), null));
				} else {
					rows.add(new VariantRow(s, false, null, null, null, null, nextIndex));
					ChiTietSanPham ct = new ChiTietSanPham();
					ct.setSanPham(sanPham);
					ct.setMauSac(selectedMau);
					ct.setSize(s);
					block.getForm().getChiTietList().add(ct);
					nextIndex++;
				}
			}
			block.setRows(rows);
			block.setMissingCount((int) rows.stream().filter(r -> !r.isExisted()).count());
			mauBlocks.add(block);
		}
		model.addAttribute("mauBlocks", mauBlocks);

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

        // Bổ sung phân trang biến thể đã có để template không bị null
        Page<ChiTietSanPham> pageChiTiet = chitietsanphamRepo.findBySanPham_Id(id, PageRequest.of(0, 10));
        model.addAttribute("pageChiTiet", pageChiTiet);
 
        return "admin/pages/sanpham/xemchitiet";
    }

    @PostMapping("/chitietsanpham/them")
    public String luuChiTietMoi(@ModelAttribute("form") ChiTietSanPhamForm form,
                                @RequestParam("sanPhamId") Integer sanPhamId,
                                RedirectAttributes redirectAttributes) {
        SanPham sp = sanPhamService.findbyid(sanPhamId);

        int successCount = 0;
        int errorCount = 0;
        List<String> errorMessages = new ArrayList<>();

        for (ChiTietSanPham ct : form.getChiTietList()) {
            try {

//                // Nếu toàn bộ dữ liệu null/trống thì bỏ qua, không tính là error
//                if ((ct.getSize() == null || ct.getSize().getId() == null) &&
//                        (ct.getSoLuong() == null) &&
//                        (ct.getGiaGoc() == null) &&
//                        (ct.getGiaNhap() == null) &&
//                        (ct.getGiaBan() == null)) {
//                    continue; // skip biến thể này
//                }

                if (ct.getSize() == null || ct.getSize().getId() == null) {
                    errorMessages.add("Thiếu size");
                    errorCount++; continue;
                }
                if (ct.getMauSac() == null || ct.getMauSac().getId() == null) {
                    errorMessages.add("Thiếu màu sắc");
                    errorCount++; continue;
                }

                Size theSize = sizerepository.findById(ct.getSize().getId()).orElse(null);
                MauSac theMau = mausacrepository.findById(ct.getMauSac().getId()).orElse(null);
                if (theSize == null) { errorMessages.add("Size không tồn tại"); errorCount++; continue; }
                if (theMau == null) { errorMessages.add("Màu sắc không tồn tại"); errorCount++; continue; }

                ct.setSize(theSize);
                ct.setMauSac(theMau);

                if (ct.getSoLuong() == null || ct.getSoLuong() < 5 || ct.getSoLuong() > 1000) {
                    errorMessages.add("Số lượng phải từ 5-1000");
                    errorCount++; continue;
                }

                if (ct.getGiaGoc() == null || ct.getGiaGoc().compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessages.add("Giá gốc phải > 0");
                    errorCount++; continue;
                }
                if (ct.getGiaNhap() == null || ct.getGiaNhap().compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessages.add("Giá nhập phải > 0");
                    errorCount++; continue;
                }
                if (ct.getGiaNhap().compareTo(ct.getGiaGoc()) > 0) {
                    errorMessages.add("Giá nhập không được > giá gốc");
                    errorCount++; continue;
                }
                if (ct.getGiaBan() != null) {
                    if (ct.getGiaBan().compareTo(BigDecimal.ZERO) <= 0) {
                        errorMessages.add("Giá bán phải > 0");
                        errorCount++; continue;
                    }
                    if (ct.getGiaBan().compareTo(ct.getGiaGoc()) > 0) {
                        errorMessages.add("Giá bán không được > giá gốc");
                        errorCount++; continue;
                    }
                    if (ct.getGiaBan().compareTo(ct.getGiaNhap()) < 0) {
                        errorMessages.add("Giá bán không được < giá nhập");
                        errorCount++; continue;
                    }
                }

                ct.setTenCt(theMau.getTen() + " - " + theSize.getTen());
                ct.setTrangThai(true);
                ct.setMoTa(null);
                ct.setGhiChu(null);

                String randomMaVach = "SP" + System.currentTimeMillis();
                ct.setMaVach(randomMaVach);
                if (ct.getGiaBan() == null) { ct.setGiaBan(ct.getGiaGoc()); }
                if (ct.getSanPham() == null) { ct.setSanPham(sp); }

                chitietsanphamRepo.save(ct);
                successCount++;
            } catch (Exception e) {
                errorCount++;
                errorMessages.add("Lỗi hệ thống khi lưu biến thể");
            }
        }

        if (successCount > 0 && errorCount == 0) {
            redirectAttributes.addFlashAttribute("success", "Đã lưu " + successCount + " biến thể");
        } else if (successCount > 0 && errorCount > 0) {
            redirectAttributes.addFlashAttribute("success", "Đã lưu " + successCount + " biến thể");
            redirectAttributes.addFlashAttribute("warning", "Một số biến thể không được lưu: " + String.join("; ", errorMessages));
        } else {
            redirectAttributes.addFlashAttribute("error", String.join("; ", errorMessages));
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





    @PostMapping("/cap-nhat-chi-tiet")
    public String capNhatChiTietSanPham(@RequestParam("id") Integer id,
                                        @RequestParam("soLuong") int soLuong,
                                        @RequestParam("giaGoc") BigDecimal giaGoc,
                                        @RequestParam("giaNhap") BigDecimal giaNhap,
                                        RedirectAttributes redirect) {
        ChiTietSanPham ct = chitietsanphamRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm"));

        // Cập nhật số lượng, giá gốc, giá nhập
        ct.setSoLuong(soLuong);
        ct.setGiaGoc(giaGoc);
        ct.setGiaNhap(giaNhap);

        // Xử lý giá bán
        if (ct.getDotGiamGia() == null) {
            // Không có đợt giảm giá → giá bán = giá gốc
            ct.setGiaBan(giaGoc);
        } else {
            BigDecimal giaSauGiam = giaGoc; // mặc định là giá gốc

            if ("phan_tram".equalsIgnoreCase(ct.getDotGiamGia().getLoai())) {
                // Giảm theo phần trăm
                BigDecimal phanTramGiam = ct.getDotGiamGia().getGiaTriDotGiamGia(); // ví dụ 10 = 10%
                if (phanTramGiam != null) {
                    BigDecimal tiLe = BigDecimal.ONE.subtract(phanTramGiam.divide(BigDecimal.valueOf(100)));
                    giaSauGiam = giaGoc.multiply(tiLe);
                }
            } else if ("tien_mat".equalsIgnoreCase(ct.getDotGiamGia().getLoai())) {
                // Giảm trực tiếp số tiền
                BigDecimal soTienGiam = ct.getDotGiamGia().getGiaTriDotGiamGia(); // ví dụ 50000
                if (soTienGiam != null) {
                    giaSauGiam = giaGoc.subtract(soTienGiam);
                }
            }

            // Trường hợp giá sau giảm nhỏ hơn 0 thì set về 0 để tránh lỗi
            if (giaSauGiam.compareTo(BigDecimal.ZERO) < 0) {
                giaSauGiam = BigDecimal.ZERO;
            }

            ct.setGiaBan(giaSauGiam);
        }

        chitietsanphamRepo.save(ct);

        redirect.addFlashAttribute("message", "Cập nhật chi tiết thành công!");
        return "redirect:/admin/san-pham/xem/" + ct.getSanPham().getId();
    }



    @PostMapping("/tao-ma-ngau-nhien")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> taoMaNgauNhien() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String maNgauNhien;
            int maxAttempts = 10; // Giới hạn số lần thử để tránh vòng lặp vô hạn
            int attempts = 0;
            
            do {
                // Tạo mã ngẫu nhiên: SP + 6 số ngẫu nhiên
                maNgauNhien = "SP" + String.format("%06d", (int)(Math.random() * 1000000));
                attempts++;
            } while (sanPhamRepository.existsByMa(maNgauNhien) && attempts < maxAttempts);
            
            if (attempts >= maxAttempts) {
                response.put("success", false);
                response.put("message", "Không thể tạo mã ngẫu nhiên sau nhiều lần thử. Vui lòng thử lại.");
                return ResponseEntity.ok(response);
            }
            
            response.put("success", true);
            response.put("ma", maNgauNhien);
            response.put("message", "Tạo mã thành công!");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-ma")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkMaSanPham(@RequestParam("ma") String ma) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exists = sanPhamRepository.existsByMa(ma);
            response.put("exists", exists);
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * API lấy thống kê sản phẩm
     */
    @GetMapping("/api/thong-ke")
    @ResponseBody
    public ResponseEntity<SanPhamThongKeDTO> getThongKeSanPham() {
        try {
            SanPhamThongKeDTO thongKe = sanPhamService.getThongKeSanPham();
            return ResponseEntity.ok(thongKe);
        } catch (Exception e) {
            // Trả về thống kê mặc định nếu có lỗi
            SanPhamThongKeDTO thongKeMacDinh = SanPhamThongKeDTO.builder()
                    .tongSanPham(0L)
                    .dangHoatDong(0L)
                    .ngungHoatDong(0L)
                    .sapHetHang(0L)
                    .build();
            return ResponseEntity.ok(thongKeMacDinh);
        }
    }

    /**
     * API xuất Excel sản phẩm theo filter hiện tại
     */
    @GetMapping("/api/export-excel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> xuatExcelSanPham(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThaiHienThi,
            @RequestParam(required = false) Integer danhMucId,
            @RequestParam(required = false) Integer thuongHieuId,
            @RequestParam(required = false, defaultValue = "ngayTao:desc") String sortBy,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "1000") int size) {
        
        // Tạo filter DTO từ parameters
        SanPhamFilterDTO filter = SanPhamFilterDTO.builder()
                .keyword(keyword)
                .trangThaiHienThi(trangThaiHienThi)
                .danhMucId(danhMucId)
                .thuongHieuId(thuongHieuId)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Lấy danh sách sản phẩm theo filter
            List<SanPhamExportDTO> danhSachXuat = sanPhamService.getSanPhamForExport(filter);
            
            if (danhSachXuat.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không có dữ liệu để xuất Excel!");
                return ResponseEntity.ok(response);
            }
            
            // Tạo file Excel
            byte[] excelFile = createExcelFile(danhSachXuat);
            
            // Lưu file tạm thời
            String fileName = "SanPham-dgfashion-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yy-MM-dd")) + ".xlsx";
            
            response.put("success", true);
            response.put("message", "Xuất Excel thành công!");
            response.put("fileName", fileName);
            response.put("fileSize", excelFile.length);
            response.put("data", java.util.Base64.getEncoder().encodeToString(excelFile));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi xuất Excel: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Tạo file Excel từ danh sách sản phẩm
     */
    private byte[] createExcelFile(List<SanPhamExportDTO> danhSach) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Danh sách sản phẩm");
            
            // Tạo style cho header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            // Tạo style cho dữ liệu
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            // Tạo tiêu đề
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Báo cáo về sản phẩm D&G Fashion");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));
            
            // Tạo header
            Row headerRow = sheet.createRow(1);
            String[] headers = {
                "Mã SP", "Tên SP", "Mô tả", "Danh mục", "Thương hiệu", "Chất liệu", 
                "Xuất xứ", "Kiểu dáng", "Loại thú", "Giá gốc", "Số lượng", "Trạng thái", "Ngày tạo"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Thêm dữ liệu
            int rowNum = 2;
            for (SanPhamExportDTO sp : danhSach) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(sp.getMa() != null ? sp.getMa() : "");
                row.createCell(1).setCellValue(sp.getTen() != null ? sp.getTen() : "");
                row.createCell(2).setCellValue(sp.getMoTa() != null ? sp.getMoTa() : "");
                row.createCell(3).setCellValue(sp.getDanhMuc() != null ? sp.getDanhMuc() : "");
                row.createCell(4).setCellValue(sp.getThuongHieu() != null ? sp.getThuongHieu() : "");
                row.createCell(5).setCellValue(sp.getChatLieu() != null ? sp.getChatLieu() : "");
                row.createCell(6).setCellValue(sp.getXuatXu() != null ? sp.getXuatXu() : "");
                row.createCell(7).setCellValue(sp.getKieuDang() != null ? sp.getKieuDang() : "");
                row.createCell(8).setCellValue(sp.getLoaiThu() != null ? sp.getLoaiThu() : "");
                row.createCell(9).setCellValue(sp.getGia() != null ? sp.getGia().doubleValue() : 0.0);
                row.createCell(10).setCellValue(sp.getSoLuong() != null ? sp.getSoLuong() : 0);
                row.createCell(11).setCellValue(sp.getTrangThai() != null ? sp.getTrangThai() : "");
                row.createCell(12).setCellValue(sp.getNgayTao() != null ? sp.getNgayTao().toString() : "");
            }
            
            // Auto-fit columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Ghi file vào ByteArrayOutputStream
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Test endpoint để kiểm tra routing
     */
    @GetMapping("/api/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Test endpoint hoạt động!");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Xóa biến thể sản phẩm
     */
    @DeleteMapping("/bien-the/xoa/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> xoaBienThe(@PathVariable("id") Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Kiểm tra biến thể có tồn tại không
            if (!chiTietSanPhamService.findById(id).isPresent()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy biến thể sản phẩm");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Kiểm tra có thể xóa không
            if (!chiTietSanPhamService.coTheXoaBienThe(id)) {
                response.put("success", false);
                response.put("message", "Không thể xóa biến thể này. Biến thể đang được sử dụng trong đơn hàng hoặc giỏ hàng.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Thực hiện xóa
            boolean xoaThanhCong = chiTietSanPhamService.xoaBienThe(id);
            
            if (xoaThanhCong) {
                response.put("success", true);
                response.put("message", "Xóa biến thể thành công");
            } else {
                response.put("success", false);
                response.put("message", "Có lỗi xảy ra khi xóa biến thể");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }
}