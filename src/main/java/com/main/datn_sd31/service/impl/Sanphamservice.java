package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.dto.san_pham_DTO.SanPhamFilterDTO;
import com.main.datn_sd31.dto.san_pham_DTO.SanPhamListDTO;
import com.main.datn_sd31.dto.san_pham_DTO.Sanphamform;
import com.main.datn_sd31.dto.san_pham_DTO.SanPhamThongKeDTO;
import com.main.datn_sd31.dto.san_pham_DTO.SanPhamExportDTO;
import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.DotGiamGia;
import com.main.datn_sd31.entity.HinhAnh;
import com.main.datn_sd31.entity.SanPham;
import com.main.datn_sd31.repository.ChatLieuRepository;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.Danhmucrepository;
import com.main.datn_sd31.repository.Dotgiamgiarepository;
import com.main.datn_sd31.repository.Hinhanhrepository;
import com.main.datn_sd31.repository.Kieudangrepository;
import com.main.datn_sd31.repository.Loaithurepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.repository.SanPhamRepository;
import com.main.datn_sd31.repository.Thuonghieurepository;
import com.main.datn_sd31.repository.Xuatxurepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class Sanphamservice {

    @Autowired private SanPhamRepository sanPhamRepo;
    @Autowired private ChatLieuRepository chatLieuRepo;
    @Autowired private Xuatxurepository xuatXuRepo;
    @Autowired private Danhmucrepository danhmucrepository;
    @Autowired private Kieudangrepository kieudangrepository;
    @Autowired private Thuonghieurepository thuonghieurepository;
    @Autowired private NhanVienRepository nhanvienrepository;
    @Autowired private Hinhanhrepository hinhanhrepository;
    @Autowired private Chitietsanphamrepository chitietsanphamrepository;
    @Autowired private Loaithurepository loaithurepository;
    @Autowired private Dotgiamgiarepository dotgiamgiarepository;

    public List<SanPham> getAll() {
        return sanPhamRepo.findAll();
    }

    /**
     * Lấy danh sách sản phẩm đang hoạt động (cho client)
     */
    public List<SanPham> getAllActive() {
        return sanPhamRepo.findByTrangThaiTrue();
    }

    /**
     * Lấy danh sách sản phẩm với thông tin đầy đủ cho hiển thị (cho client)
     */
    public List<SanPhamListDTO> getAllForDisplay() {
        List<SanPham> sanPhams = sanPhamRepo.findByTrangThaiTrue();
        return sanPhams.stream()
                .map(this::convertToSanPhamListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách tất cả sản phẩm với thông tin đầy đủ cho hiển thị (cho admin)
     */
    public List<SanPhamListDTO> getAllForAdminDisplay() {
        List<SanPham> sanPhams = sanPhamRepo.findAll();
        return sanPhams.stream()
                .map(this::convertToSanPhamListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách sản phẩm có phân trang (cho client)
     */
    public Page<SanPhamListDTO> getAllForDisplayPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
        // Tạo Page object từ danh sách đã lọc theo trạng thái
        List<SanPham> allActiveProducts = sanPhamRepo.findByTrangThaiTrue();
        int totalElements = allActiveProducts.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        List<SanPham> pageContent = startIndex < totalElements ? 
                allActiveProducts.subList(startIndex, endIndex) : new ArrayList<>();
        
        Page<SanPham> sanPhamPage = new PageImpl<>(pageContent, pageable, totalElements);
        return sanPhamPage.map(this::convertToSanPhamListDTO);
    }

    /**
     * Lấy danh sách tất cả sản phẩm có phân trang (cho admin)
     */
    public Page<SanPhamListDTO> getAllForAdminDisplayPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
        Page<SanPham> sanPhamPage = sanPhamRepo.findAll(pageable);
        return sanPhamPage.map(this::convertToSanPhamListDTO);
    }

    /**
     * Lấy danh sách sản phẩm có phân trang với filter và sort (cho client)
     */
    public Page<SanPhamListDTO> getAllForDisplayPaginatedWithFilter(SanPhamFilterDTO filter) {
        // Lấy tất cả sản phẩm đang hoạt động
        List<SanPham> allSanPhams = getAllActive();
        
        // Chuyển đổi sang DTO
        List<SanPhamListDTO> allDTOs = allSanPhams.stream()
                .map(this::convertToSanPhamListDTO)
                .collect(Collectors.toList());
        
        // Áp dụng filter
        List<SanPhamListDTO> filteredDTOs = applyFilters(allDTOs, filter);
        
        // Áp dụng sorting
        List<SanPhamListDTO> sortedDTOs = applySorting(filteredDTOs, filter);
        
        // Tính toán pagination
        int totalElements = sortedDTOs.size();
        int totalPages = (int) Math.ceil((double) totalElements / filter.getSize());
        int startIndex = filter.getPage() * filter.getSize();
        int endIndex = Math.min(startIndex + filter.getSize(), totalElements);
        
        // Lấy content cho trang hiện tại
        List<SanPhamListDTO> pageContent = startIndex < totalElements ? 
                sortedDTOs.subList(startIndex, endIndex) : new ArrayList<>();
        
        // Tạo Page object
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
        return new PageImpl<>(pageContent, pageable, totalElements);
    }

    /**
     * Lấy danh sách tất cả sản phẩm có phân trang với filter và sort (cho admin)
     */
    public Page<SanPhamListDTO> getAllForAdminDisplayPaginatedWithFilter(SanPhamFilterDTO filter) {
        // Lấy tất cả sản phẩm
        List<SanPham> allSanPhams = getAll();
        
        // Chuyển đổi sang DTO
        List<SanPhamListDTO> allDTOs = allSanPhams.stream()
                .map(this::convertToSanPhamListDTO)
                .collect(Collectors.toList());
        
        // Áp dụng filter
        List<SanPhamListDTO> filteredDTOs = applyFilters(allDTOs, filter);
        
        // Áp dụng sorting
        List<SanPhamListDTO> sortedDTOs = applySorting(filteredDTOs, filter);
        
        // Tính toán pagination
        int totalElements = sortedDTOs.size();
        int totalPages = (int) Math.ceil((double) totalElements / filter.getSize());
        int startIndex = filter.getPage() * filter.getSize();
        int endIndex = Math.min(startIndex + filter.getSize(), totalElements);
        
        // Lấy content cho trang hiện tại
        List<SanPhamListDTO> pageContent = startIndex < totalElements ? 
                sortedDTOs.subList(startIndex, endIndex) : new ArrayList<>();
        
        // Tạo Page object
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
        return new PageImpl<>(pageContent, pageable, totalElements);
    }

    /**
     * Lấy danh sách sản phẩm với filter và sort
     */
    public List<SanPhamListDTO> getFilteredProducts(SanPhamFilterDTO filter) {
        List<SanPham> sanPhams = getAllActive(); // Lấy sản phẩm đang hoạt động
        
        List<SanPhamListDTO> result = sanPhams.stream()
                .map(this::convertToSanPhamListDTO)
                .collect(Collectors.toList());
        
        // Apply filters
        result = applyFilters(result, filter);
        
        // Apply sorting
        result = applySorting(result, filter);
        
        return result;
    }

    /**
     * Lấy danh sách tất cả sản phẩm với filter và sort (cho admin)
     */
    public List<SanPhamListDTO> getFilteredProductsForAdmin(SanPhamFilterDTO filter) {
        List<SanPham> sanPhams = getAll(); // Lấy tất cả sản phẩm
        
        List<SanPhamListDTO> result = sanPhams.stream()
                .map(this::convertToSanPhamListDTO)
                .collect(Collectors.toList());
        
        // Apply filters
        result = applyFilters(result, filter);
        
        // Apply sorting
        result = applySorting(result, filter);
        
        return result;
    }

    /**
     * Kiểm tra xem file ảnh có tồn tại không
     */
    private String validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return "/images/favicon.png";
        }
        
        // Nếu là ảnh upload, kiểm tra file có tồn tại không
        if (imageUrl.startsWith("/uploads/")) {
            String filePath = "src/main/resources/static" + imageUrl;
            if (!Files.exists(Paths.get(filePath))) {
                return "/images/favicon.png"; // Fallback về ảnh mặc định
            }
        }
        
        return imageUrl;
    }

    /**
     * Chuyển đổi SanPham entity thành SanPhamListDTO
     */
    private SanPhamListDTO convertToSanPhamListDTO(SanPham sanPham) {
        List<ChiTietSanPham> chiTietSanPhams = new ArrayList<>(sanPham.getChiTietSanPhams());
        
        // Tính toán thông tin số lượng và giá
        int tongSoLuong = chiTietSanPhams.stream()
                .mapToInt(ChiTietSanPham::getSoLuong)
                .sum();
        
        // Tính toán giá min/max
        BigDecimal giaGocMin = null, giaGocMax = null;
        BigDecimal giaBanMin = null, giaBanMax = null;
        BigDecimal giaSauGiamMin = null, giaSauGiamMax = null;
        
        if (!chiTietSanPhams.isEmpty()) {
            giaGocMin = chiTietSanPhams.stream()
                    .map(ChiTietSanPham::getGiaGoc)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(null);
            
            giaGocMax = chiTietSanPhams.stream()
                    .map(ChiTietSanPham::getGiaGoc)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(null);
            
            giaBanMin = chiTietSanPhams.stream()
                    .map(ChiTietSanPham::getGiaBan)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(null);
            
            giaBanMax = chiTietSanPhams.stream()
                    .map(ChiTietSanPham::getGiaBan)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(null);
        }
        
        // Tính toán giá sau giảm (giaBan đã được tính toán từ giaGoc và dotGiamGia)
        if (giaBanMin != null && giaBanMax != null) {
            giaSauGiamMin = giaBanMin;
            giaSauGiamMax = giaBanMax;
        }
        
        // Lấy thông tin giảm giá
        DotGiamGia dotGiamGia = chiTietSanPhams.stream()
                .map(ChiTietSanPham::getDotGiamGia)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        
        // Xác định trạng thái hiển thị
        String trangThaiHienThi = determineTrangThaiHienThi(sanPham.getTrangThai(), tongSoLuong);
        String trangThaiClass = determineTrangThaiClass(trangThaiHienThi);
        
        // Lấy ảnh sản phẩm
        List<HinhAnh> hinhAnhs = hinhanhrepository.findByhinhanhid(sanPham.getId());
        String anhChinh = hinhAnhs.stream()
                .filter(anh -> anh.getLoaiAnh() == 0) // Ảnh chính
                .map(HinhAnh::getUrl)
                .findFirst()
                .orElse("/images/favicon.png");
        
        // Kiểm tra và validate ảnh chính
        anhChinh = validateImageUrl(anhChinh);
        
        List<String> anhPhu = hinhAnhs.stream()
                .filter(anh -> anh.getLoaiAnh() == 1) // Ảnh phụ
                .map(HinhAnh::getUrl)
                .collect(Collectors.toList());
        
        // Kiểm tra và validate ảnh phụ
        List<String> anhPhuValid = anhPhu.stream()
                .map(this::validateImageUrl)
                .filter(url -> !url.equals("/images/favicon.png") || anhPhu.size() == 1) // Giữ lại ít nhất 1 ảnh
                .collect(Collectors.toList());
        
        // Nếu không có ảnh phụ nào hợp lệ, sử dụng ảnh mặc định
        if (anhPhuValid.isEmpty()) {
            anhPhuValid = Arrays.asList("/images/favicon.png");
        }
        
        // Chuyển đổi chi tiết sản phẩm
        List<SanPhamListDTO.ChiTietSanPhamDTO> chiTietDTOs = chiTietSanPhams.stream()
                .map(this::convertToChiTietDTO)
                .collect(Collectors.toList());
        
        return SanPhamListDTO.builder()
                .id(sanPham.getId())
                .ma(sanPham.getMa())
                .ten(sanPham.getTen())
                .moTa(sanPham.getMoTa())
                .trangThai(sanPham.getTrangThai())
                .ngayTao(sanPham.getNgayTao())
                .chatLieu(sanPham.getChatLieu() != null ? sanPham.getChatLieu().getTen() : "")
                .danhMuc(sanPham.getDanhMuc() != null ? sanPham.getDanhMuc().getTen() : "")
                .kieuDang(sanPham.getKieuDang() != null ? sanPham.getKieuDang().getTen() : "")
                .thuongHieu(sanPham.getThuongHieu() != null ? sanPham.getThuongHieu().getTen() : "")
                .xuatXu(sanPham.getXuatXu() != null ? sanPham.getXuatXu().getTen() : "")
                .loaiThu(sanPham.getLoaiThu() != null ? sanPham.getLoaiThu().getTen() : "")
                .tongSoLuong(tongSoLuong)
                .giaGocMin(giaGocMin)
                .giaGocMax(giaGocMax)
                .giaBanMin(giaBanMin)
                .giaBanMax(giaBanMax)
                .giaSauGiamMin(giaSauGiamMin)
                .giaSauGiamMax(giaSauGiamMax)
                .tenDotGiamGia(dotGiamGia != null ? dotGiamGia.getTen() : null)
                .phanTramGiam(dotGiamGia != null ? dotGiamGia.getGiaTriDotGiamGia() : null)
                .soTienGiam(dotGiamGia != null ? dotGiamGia.getGiaTriDotGiamGia() : null)
                .trangThaiHienThi(trangThaiHienThi)
                .trangThaiClass(trangThaiClass)
                .anhChinh(anhChinh)
                .anhPhu(anhPhuValid) // Sử dụng ảnh phụ đã validate
                .chiTietSanPhams(chiTietDTOs)
                .build();
    }

    /**
     * Chuyển đổi ChiTietSanPham thành ChiTietSanPhamDTO
     */
    private SanPhamListDTO.ChiTietSanPhamDTO convertToChiTietDTO(ChiTietSanPham chiTiet) {
        BigDecimal giaSauGiam = chiTiet.getGiaBan(); // Giá bán đã là giá sau giảm
        
        String trangThaiHienThi = determineChiTietTrangThai(chiTiet.getSoLuong());
        
        return SanPhamListDTO.ChiTietSanPhamDTO.builder()
                .id(chiTiet.getId())
                .tenCt(chiTiet.getTenCt())
                .size(chiTiet.getSize() != null ? chiTiet.getSize().getTen() : "")
                .mauSac(chiTiet.getMauSac() != null ? chiTiet.getMauSac().getTen() : "")
                .soLuong(chiTiet.getSoLuong())
                .giaGoc(chiTiet.getGiaGoc())
                .giaBan(chiTiet.getGiaBan())
                .giaSauGiam(giaSauGiam)
                .trangThaiHienThi(trangThaiHienThi)
                .build();
    }

    /**
     * Xác định trạng thái hiển thị cho sản phẩm
     */
    private String determineTrangThaiHienThi(Boolean trangThai, int tongSoLuong) {
        if (!trangThai) {
            return "Ngưng hoạt động";
        }
        return "Đang hoạt động";
    }

    /**
     * Xác định trạng thái hiển thị cho chi tiết sản phẩm
     */
    private String determineChiTietTrangThai(int soLuong) {
        if (soLuong == 0) {
            return "Hết hàng";
        }
        if (soLuong < 5) {
            return "Sắp hết";
        }
        return "Còn hàng";
    }

    /**
     * Xác định CSS class cho trạng thái
     */
    private String determineTrangThaiClass(String trangThaiHienThi) {
        switch (trangThaiHienThi) {
            case "Đang hoạt động":
                return "status-active";
            case "Ngưng hoạt động":
                return "status-inactive";
            default:
                return "status-default";
        }
    }

    /**
     * Áp dụng filter cho danh sách sản phẩm
     */
    private List<SanPhamListDTO> applyFilters(List<SanPhamListDTO> products, SanPhamFilterDTO filter) {
        return products.stream()
                .filter(product -> {
                    // Filter by keyword
                    if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                        String keyword = filter.getKeyword().toLowerCase();
                        if (!product.getTen().toLowerCase().contains(keyword) &&
                            !product.getMa().toLowerCase().contains(keyword)) {
                            return false;
                        }
                    }
                    
                    // Filter by status
                    if (filter.getTrangThaiHienThi() != null && !filter.getTrangThaiHienThi().isEmpty()) {
                        if (!filter.getTrangThaiHienThi().equals(product.getTrangThaiHienThi())) {
                            return false;
                        }
                    }
                    
                    // Filter by price range
                    if (filter.getGiaMin() != null && product.getGiaSauGiamMin() != null) {
                        if (product.getGiaSauGiamMin().compareTo(filter.getGiaMin()) < 0) {
                            return false;
                        }
                    }
                    if (filter.getGiaMax() != null && product.getGiaSauGiamMax() != null) {
                        if (product.getGiaSauGiamMax().compareTo(filter.getGiaMax()) > 0) {
                            return false;
                        }
                    }
                    
                    // Filter by quantity range
                    if (filter.getSoLuongMin() != null) {
                        if (product.getTongSoLuong() < filter.getSoLuongMin()) {
                            return false;
                        }
                    }
                    if (filter.getSoLuongMax() != null) {
                        if (product.getTongSoLuong() > filter.getSoLuongMax()) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Áp dụng sorting cho danh sách sản phẩm
     */
    private List<SanPhamListDTO> applySorting(List<SanPhamListDTO> products, SanPhamFilterDTO filter) {
        if (filter.getSortBy() == null || filter.getSortBy().isEmpty()) {
            return products;
        }
        
        Comparator<SanPhamListDTO> comparator = null;
        
        switch (filter.getSortBy()) {
            case "ten":
                comparator = Comparator.comparing(SanPhamListDTO::getTen, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "gia":
                comparator = Comparator.comparing(SanPhamListDTO::getGiaSauGiamMin, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "soLuong":
                comparator = Comparator.comparing(SanPhamListDTO::getTongSoLuong, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "ngayTao":
                comparator = Comparator.comparing(SanPhamListDTO::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "trangThai":
                comparator = Comparator.comparing(SanPhamListDTO::getTrangThaiHienThi, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            default:
                return products;
        }
        
        if ("desc".equalsIgnoreCase(filter.getSortOrder())) {
            comparator = comparator.reversed();
        }
        
        return products.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Tính toán và cập nhật giá bán cho chi tiết sản phẩm
     */
    public void tinhToanVaCapNhatGiaBan(ChiTietSanPham chiTiet) {
        if (chiTiet.getGiaGoc() == null) {
            return;
        }

        BigDecimal giaGoc = chiTiet.getGiaGoc();
        BigDecimal giaBan = giaGoc; // Mặc định giá bán = giá gốc

        // Nếu có đợt giảm giá
        if (chiTiet.getDotGiamGia() != null) {
            DotGiamGia dotGiamGia = chiTiet.getDotGiamGia();
            BigDecimal giaTriGiam = dotGiamGia.getGiaTriDotGiamGia();
            
            // Tính toán giá sau giảm
            if ("TIEN".equalsIgnoreCase(dotGiamGia.getLoai())) {
                // Giảm theo số tiền cố định
                giaBan = giaGoc.subtract(giaTriGiam);
            } else {
                // Giảm theo phần trăm
                BigDecimal phanTramGiam = giaTriGiam.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal soTienGiam = giaGoc.multiply(phanTramGiam);
                giaBan = giaGoc.subtract(soTienGiam);
            }
            
            // Đảm bảo giá bán không âm
            if (giaBan.compareTo(BigDecimal.ZERO) < 0) {
                giaBan = BigDecimal.ZERO;
            }
        }

        // Cập nhật giá bán
        chiTiet.setGiaBan(giaBan);
        chiTiet.setNgaySua(LocalDateTime.now());
        
        // Lưu vào database
        chitietsanphamrepository.save(chiTiet);
    }

    /**
     * Áp dụng đợt giảm giá cho sản phẩm
     */
    public void apDungDotGiamGia(Integer sanPhamId, Integer dotGiamGiaId) {
        List<ChiTietSanPham> chiTietList = chitietsanphamrepository.findBySanPhamId(sanPhamId);
        
        for (ChiTietSanPham chiTiet : chiTietList) {
            if (dotGiamGiaId != null) {
                // Áp dụng đợt giảm giá
                DotGiamGia dotGiamGia = dotgiamgiarepository.findById(dotGiamGiaId).orElse(null);
                if (dotGiamGia != null) {
                    chiTiet.setDotGiamGia(dotGiamGia);
                }
            } else {
                // Gỡ đợt giảm giá
                chiTiet.setDotGiamGia(null);
            }
            
            // Tính toán lại giá bán
            tinhToanVaCapNhatGiaBan(chiTiet);
        }
    }

    public List<SanPham> search(
            String q,
            Integer danhMucId,
            Integer loaiThuId,
            Integer chatLieuId,
            Integer kieuDangId,
            Integer xuatXuId,
            String priceRange
    ) {
        // normalize keyword
        String keyword = (q == null ? "" : q.trim());

        // parse priceRange
        BigDecimal min = null, max = null;
        if (priceRange != null && !priceRange.isEmpty()) {
            String[] parts = priceRange.split("-");
            min = new BigDecimal(parts[0]);
            if (parts.length > 1) {
                max = new BigDecimal(parts[1]);
            }
        }

        // if no filter at all, trả về all
        if (keyword.isEmpty() && danhMucId == null && loaiThuId == null && chatLieuId == null && kieuDangId == null && xuatXuId == null &&  min == null) {
            return sanPhamRepo.findByTrangThaiTrue();
        }

        return sanPhamRepo.filter(keyword, danhMucId, loaiThuId, chatLieuId, kieuDangId, xuatXuId, min, max);
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

    /**
     * Tìm sản phẩm theo ID (cho client - chỉ trả về sản phẩm đang hoạt động)
     */
    public SanPham findByIdActive(Integer id) {
        SanPham sanPham = sanPhamRepo.findById(id).orElse(null);
        // Chỉ trả về sản phẩm đang hoạt động
        if (sanPham != null && sanPham.getTrangThai()) {
            return sanPham;
        }
        return null;
    }

    public void delete(Integer id) {
        hinhanhrepository.findBydeleteid(id);
        chitietsanphamrepository.findBydeleteid(id);
        sanPhamRepo.deleteById(id);
    }

    public List<SanPham> searchAdvanced(
            String q,
            Integer danhMucId,
            Integer loaiThuId,
            Integer sizeId,
            Integer mauSacId,
            Integer kieuDangId,
            Integer thuongHieuId,
            Integer xuatXuId,
            Integer priceRange,
            String sortBy,
            String sortDir
    ) {
        // normalize keyword
        String keyword = (q == null ? "" : q.trim());

        // parse priceRange
        BigDecimal min = null, max = null;
        if (priceRange != null) {
            min = BigDecimal.ZERO;
            max = new BigDecimal(priceRange);
        }

        // if no filter at all, trả về all
        if (keyword.isEmpty() && danhMucId == null && loaiThuId == null && 
            sizeId == null && mauSacId == null && kieuDangId == null && 
            thuongHieuId == null && xuatXuId == null && min == null) {
            return sanPhamRepo.findByTrangThaiTrue();
        }

        // Sử dụng method filter cơ bản trước
        List<SanPham> basicFiltered = sanPhamRepo.filter(
            keyword, danhMucId, loaiThuId, null, kieuDangId, xuatXuId, min, max
        );

        // Lọc thêm theo thương hiệu nếu có
        if (thuongHieuId != null) {
            basicFiltered = basicFiltered.stream()
                .filter(sp -> sp.getThuongHieu() != null && sp.getThuongHieu().getId().equals(thuongHieuId))
                .collect(Collectors.toList());
        }

        // Lọc theo size và màu sắc nếu có
        if (sizeId != null || mauSacId != null) {
            basicFiltered = basicFiltered.stream()
                .filter(sp -> sp.getChiTietSanPhams().stream()
                    .anyMatch(ct -> (sizeId == null || ct.getSize().getId().equals(sizeId)) &&
                                   (mauSacId == null || ct.getMauSac().getId().equals(mauSacId))))
                .collect(Collectors.toList());
        }

        // Áp dụng sắp xếp nếu có
        if (sortBy != null && !sortBy.isBlank()) {
            boolean desc = "desc".equalsIgnoreCase(sortDir);
            switch (sortBy) {
                case "price" -> {
                    basicFiltered.sort((a, b) -> {
                        int cmp = getMinDiscountedPrice(a).compareTo(getMinDiscountedPrice(b));
                        return desc ? -cmp : cmp;
                    });
                }
                case "newest" -> {
                    basicFiltered.sort((a, b) -> {
                        if (a.getNgayTao() == null && b.getNgayTao() == null) return 0;
                        if (a.getNgayTao() == null) return desc ? 1 : -1;
                        if (b.getNgayTao() == null) return desc ? -1 : 1;
                        int cmp = a.getNgayTao().compareTo(b.getNgayTao());
                        return desc ? -cmp : cmp;
                    });
                }
                default -> {}
            }
        }

        return basicFiltered;
    }

    private java.math.BigDecimal getMinDiscountedPrice(SanPham sp) {
        return sp.getChiTietSanPhams().stream()
                .map(ct -> {
                    java.math.BigDecimal giaGoc = ct.getGiaGoc() == null ? java.math.BigDecimal.ZERO : ct.getGiaGoc();
                    java.math.BigDecimal giaBan = giaGoc;
                    if (ct.getDotGiamGia() != null && giaGoc != null) {
                        var dgg = ct.getDotGiamGia();
                        var giaTri = dgg.getGiaTriDotGiamGia();
                        if (giaTri != null) {
                            if ("TIEN".equalsIgnoreCase(dgg.getLoai())) {
                                giaBan = giaGoc.subtract(giaTri);
                            } else {
                                var phanTram = giaTri.divide(new java.math.BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                                giaBan = giaGoc.subtract(giaGoc.multiply(phanTram));
                            }
                            if (giaBan.compareTo(java.math.BigDecimal.ZERO) < 0) giaBan = java.math.BigDecimal.ZERO;
                        }
                    }
                    return giaBan;
                })
                .min(java.util.Comparator.naturalOrder())
                .orElse(java.math.BigDecimal.ZERO);
    }

    /**
     * Lấy thống kê tổng quan về sản phẩm
     */
    public SanPhamThongKeDTO getThongKeSanPham() {
        List<SanPham> allSanPhams = getAll();
        
        long tongSanPham = allSanPhams.size();
        long dangHoatDong = allSanPhams.stream()
                .filter(SanPham::getTrangThai)
                .count();
        long ngungHoatDong = tongSanPham - dangHoatDong;
        
        // Sắp hết hàng: sản phẩm có tổng số lượng < 5
        long sapHetHang = allSanPhams.stream()
                .filter(sanPham -> {
                    int tongSoLuong = sanPham.getChiTietSanPhams().stream()
                            .mapToInt(ChiTietSanPham::getSoLuong)
                            .sum();
                    return tongSoLuong < 5;
                })
                .count();
        
        return SanPhamThongKeDTO.builder()
                .tongSanPham(tongSanPham)
                .dangHoatDong(dangHoatDong)
                .ngungHoatDong(ngungHoatDong)
                .sapHetHang(sapHetHang)
                .build();
    }

    /**
     * Lấy danh sách sản phẩm theo filter để xuất Excel
     */
    public List<SanPhamExportDTO> getSanPhamForExport(SanPhamFilterDTO filter) {
        // Lấy tất cả sản phẩm
        List<SanPham> allSanPhams = getAll();
        
        // Chuyển đổi sang DTO
        List<SanPhamListDTO> allDTOs = allSanPhams.stream()
                .map(this::convertToSanPhamListDTO)
                .collect(Collectors.toList());
        
        // Áp dụng filter
        List<SanPhamListDTO> filteredDTOs = applyFilters(allDTOs, filter);
        
        // Áp dụng sorting
        List<SanPhamListDTO> sortedDTOs = applySorting(filteredDTOs, filter);
        
        // Chuyển đổi sang ExportDTO
        return sortedDTOs.stream()
                .map(this::convertToSanPhamExportDTO)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi SanPhamListDTO sang SanPhamExportDTO
     */
    private SanPhamExportDTO convertToSanPhamExportDTO(SanPhamListDTO dto) {
        return SanPhamExportDTO.builder()
                .ma(dto.getMa())
                .ten(dto.getTen())
                .moTa(dto.getMoTa())
                .danhMuc(dto.getDanhMuc())
                .thuongHieu(dto.getThuongHieu())
                .chatLieu(dto.getChatLieu())
                .xuatXu(dto.getXuatXu())
                .kieuDang(dto.getKieuDang())
                .loaiThu(dto.getLoaiThu())
                .gia(dto.getGiaGocMin())
                .soLuong(dto.getTongSoLuong())
                .trangThai(dto.getTrangThai() ? "Đang hoạt động" : "Ngưng hoạt động")
                .ngayTao(dto.getNgayTao())
                .build();
    }
}
