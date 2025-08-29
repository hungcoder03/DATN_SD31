package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.DotGiamGia;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.Dotgiamgiarepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/dot-giam-gia")
@RequiredArgsConstructor
public class dotGiamGiaController {

    private final Dotgiamgiarepository dotGiamGiaRepository;

    //  Cập nhật trạng thái tự động theo thời gian (đơn giản hóa vì đã có service riêng)
    private void capNhatTrangThaiTuDong() {
        List<DotGiamGia> list = dotGiamGiaRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (DotGiamGia dgg : list) {
            if (dgg.getNgayBatDau() == null || dgg.getNgayKetThuc() == null) continue;

            if (now.isBefore(dgg.getNgayBatDau())) {
                dgg.setTrangThai(0); // Chuẩn bị áp dụng
            } else if (now.isAfter(dgg.getNgayKetThuc())) {
                dgg.setTrangThai(2); // Ngừng hoạt động
            } else {
                dgg.setTrangThai(1); // Đang hoạt động
            }
        }
        dotGiamGiaRepository.saveAll(list);
    }

    @Autowired
    private Chitietsanphamrepository chiTietSanPhamRepo;

    // ====== Trang thêm mới đợt giảm giá ======
    @GetMapping("/them")
    public String showAddForm(Model model) {
        // Khởi tạo form rỗng
        DotGiamGia form = new DotGiamGia();
        form.setLoai("phan_tram"); // mặc định
        model.addAttribute("dotGiamGia", form);
        return "admin/pages/dot-giam-gia/add";
    }

    // ================== Trang danh sách (server render + filter/sort/paging) ==================
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) String loai,
            @RequestParam(defaultValue = "ngayTao") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            Model model
    ) {
        capNhatTrangThaiTuDong();

        // Xây sort
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Spec filter
        Specification<DotGiamGia> spec = Specification.where(null);
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("ma")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("ten")), "%" + keyword.toLowerCase() + "%")
            ));
        }
        if (trangThai != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("trangThai"), trangThai));
        }
        if (loai != null && !loai.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("loai"), loai));
        }

        Page<DotGiamGia> pageData = dotGiamGiaRepository.findAll(spec, pageable);

        model.addAttribute("dotGiamGia", new DotGiamGia());
        model.addAttribute("dotGiamGias", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalElements", pageData.getTotalElements());
        model.addAttribute("pageSize", pageData.getSize());

        // Để giữ state form filter
        model.addAttribute("f_keyword", keyword);
        model.addAttribute("f_trangThai", trangThai);
        model.addAttribute("f_loai", loai);
        model.addAttribute("f_sortBy", sortBy);
        model.addAttribute("f_sortOrder", sortOrder);

        return "admin/pages/dot-giam-gia/dot-giam-gia";
    }

    // ================== CÁC API JSON PHỤC VỤ ÁP DỤNG ĐỢT ==================

    /**
     * API: Liệt kê biến thể sản phẩm để áp dụng đợt giảm giá
     * Tham số:
     *  - dotId: id đợt giảm giá (bắt buộc)
     *  - page/size: phân trang
     *  - keyword: tìm theo tên SP/biến thể thô (lọc đơn giản phía server)
     *  - applied: ALL | APPLIED | NOT_APPLIED
     */
    @GetMapping("/api/products")
    @ResponseBody
    public Map<String, Object> listProductsForDot(
            @RequestParam("dotId") Integer dotId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL") String applied
    ) {
        Map<String, Object> res = new HashMap<>();

        DotGiamGia dot = dotGiamGiaRepository.findById(dotId).orElse(null);
        if (dot == null) {
            res.put("success", false);
            res.put("message", "Không tìm thấy đợt giảm giá");
            return res;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ChiTietSanPham> pageData = chiTietSanPhamRepo.findAll(pageable);

//        List<ChiTietSanPham> filtered = chiTietSanPhamRepo.findAll();
        // Lọc đơn giản theo keyword
        List<ChiTietSanPham> filtered = pageData.getContent()
                .stream()
                .filter(ct -> {
                    if (keyword == null || keyword.isBlank()) return true;
                    String k = keyword.toLowerCase();
                    String ten = Optional.ofNullable(ct.getTenCt()).orElse("");
                    String ma = Optional.ofNullable(ct.getSanPham().getMa()).orElse("");
                    String tenSp = ct.getSanPham() != null ? Optional.ofNullable(ct.getSanPham().getTen()).orElse("") : "";
                    return ten.toLowerCase().contains(k) || ma.toLowerCase().contains(k) || tenSp.toLowerCase().contains(k);
                })
                .filter(ct -> {
                    boolean isAppliedToThis = ct.getDotGiamGia() != null && Objects.equals(ct.getDotGiamGia().getId(), dotId);
                    boolean isAppliedOther = ct.getDotGiamGia() != null && !Objects.equals(ct.getDotGiamGia().getId(), dotId);

                    if ("APPLIED".equalsIgnoreCase(applied)) return isAppliedToThis;
                    if ("APPLIED_OTHER".equalsIgnoreCase(applied)) return isAppliedOther;
                    if ("NOT_APPLIED".equalsIgnoreCase(applied)) return ct.getDotGiamGia() == null;
                    return true; // ALL
                })
                .collect(Collectors.toList());

        // Map ra DTO đơn giản cho UI
        List<Map<String, Object>> content = new ArrayList<>();
        for (ChiTietSanPham ct : filtered) {
            BigDecimal base = Optional.ofNullable(ct.getGiaGoc()).orElse(Optional.ofNullable(ct.getGiaGoc()).orElse(BigDecimal.ZERO));
            BigDecimal discounted = calcDiscountedPrice(base, dot);
            Map<String, Object> item = new HashMap<>();
            item.put("id", ct.getId());
            item.put("tenCt", ct.getTenCt());
            item.put("sanPham", ct.getSanPham() != null ? ct.getSanPham().getTen() : "");
            item.put("giaGoc", base);
            item.put("discountedPrice", discounted);
            int appliedStatus = 0; // 0 = chưa áp dụng, 1 = đã áp dụng đợt hiện tại, 2 = đã áp dụng đợt khác
            if (ct.getDotGiamGia() != null) {
                if (Objects.equals(ct.getDotGiamGia().getId(), dotId)) {
                    appliedStatus = 1;
                } else {
                    appliedStatus = 2;
                }
            }
            item.put("applied", appliedStatus);
//            item.put("applied", ct.getDotGiamGia() != null && Objects.equals(ct.getDotGiamGia().getId(), dotId));
//            System.out.println("SP " + ct.getId() + " -> dot=" + (ct.getDotGiamGia() != null ? ct.getDotGiamGia().getId() : null) + " applied=" + appliedStatus);
            content.add(item);
        }

        res.put("success", true);
        res.put("content", content);
        res.put("currentPage", pageData.getNumber());
        res.put("totalPages", pageData.getTotalPages());
        res.put("totalElements", pageData.getTotalElements());
        res.put("pageSize", pageData.getSize());
        return res;
    }

    /**
     * API: Áp dụng đợt cho danh sách biến thể (tự động tính toán và lưu giá bán)
     */
    @PostMapping("/{dotId}/apply-products")
    @ResponseBody
    public Map<String, Object> applyDotToProducts(@PathVariable Integer dotId, @RequestBody Map<String, Object> payload) {
        Map<String, Object> res = new HashMap<>();
        DotGiamGia dot = dotGiamGiaRepository.findById(dotId).orElse(null);
        if (dot == null) { res.put("success", false); res.put("message", "Không tìm thấy đợt giảm giá"); return res; }
        // Không cho áp dụng khi đợt đã ngừng
        if (dot.getTrangThai() != null && dot.getTrangThai() == 2) {
            res.put("success", false);
            res.put("message", "Đợt đã ngừng hoạt động, không thể áp dụng");
            return res;
        }

        List<Integer> ids = (List<Integer>) payload.getOrDefault("ids", Collections.emptyList());
        List<ChiTietSanPham> list = chiTietSanPhamRepo.findAllById(ids);

        int applied = 0; List<Integer> blocked = new ArrayList<>();
        for (ChiTietSanPham ct : list) {
            // Không cho ghi đè: nếu đã có dotGiamGia thì chặn
            if (ct.getDotGiamGia() != null) { blocked.add(ct.getId()); continue; }
            
            // Tính toán giá bán sau khi áp dụng đợt giảm giá
            BigDecimal giaGoc = ct.getGiaGoc();
            if (giaGoc != null && giaGoc.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal giaBan = calculateAndApplyDiscount(giaGoc, dot);
                ct.setGiaBan(giaBan);
            }
            
            ct.setDotGiamGia(dot);
            applied++;
        }
        chiTietSanPhamRepo.saveAll(list);

        res.put("success", true);
        res.put("applied", applied);
        res.put("blocked", blocked);
        res.put("message", blocked.isEmpty() ? "Áp dụng thành công và đã cập nhật giá bán!" : "Một số mục bị chặn do đã thuộc đợt khác");
        return res;
    }

    /**
     * API: Bỏ áp dụng đợt cho danh sách biến thể (tự động khôi phục giá gốc)
     */
    @PostMapping("/{dotId}/unapply-products")
    @ResponseBody
    public Map<String, Object> unapplyDotFromProducts(@PathVariable Integer dotId, @RequestBody Map<String, Object> payload) {
        Map<String, Object> res = new HashMap<>();
        DotGiamGia dot = dotGiamGiaRepository.findById(dotId).orElse(null);
        if (dot == null) { res.put("success", false); res.put("message", "Không tìm thấy đợt giảm giá"); return res; }

        List<Integer> ids = (List<Integer>) payload.getOrDefault("ids", Collections.emptyList());
        List<ChiTietSanPham> list = chiTietSanPhamRepo.findAllById(ids);

        int removed = 0; List<Integer> skipped = new ArrayList<>();
        for (ChiTietSanPham ct : list) {
            if (ct.getDotGiamGia() == null || !Objects.equals(ct.getDotGiamGia().getId(), dotId)) { skipped.add(ct.getId()); continue; }
            
            // Khôi phục giá gốc khi bỏ áp dụng đợt giảm giá
            ct.setGiaBan(ct.getGiaGoc());
            ct.setDotGiamGia(null);
            removed++;
        }
        chiTietSanPhamRepo.saveAll(list);

        res.put("success", true);
        res.put("removed", removed);
        res.put("skipped", skipped);
        res.put("message", skipped.isEmpty() ? "Bỏ áp dụng thành công và đã khôi phục giá gốc!" : "Một số mục không thuộc đợt này nên được bỏ qua");
        return res;
    }

    // Hàm tính giá sau giảm an toàn (không âm)
    private BigDecimal calcDiscountedPrice(BigDecimal base, DotGiamGia dot) {
        if (base == null) return BigDecimal.ZERO;
        if (dot == null || dot.getGiaTriDotGiamGia() == null || dot.getLoai() == null) return base;
        BigDecimal result = base;
        if ("phan_tram".equalsIgnoreCase(dot.getLoai())) {
            // phần trăm 1..100
            BigDecimal pct = dot.getGiaTriDotGiamGia();
            BigDecimal discount = base.multiply(pct).divide(BigDecimal.valueOf(100));
            result = base.subtract(discount);
        } else if ("tien_mat".equalsIgnoreCase(dot.getLoai())) {
            result = base.subtract(dot.getGiaTriDotGiamGia());
        }
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    // ================== PHẦN CODE CŨ GIỮ NGUYÊN BÊN DƯỚI ==================

    // Removed legacy route /{id}/san-pham in favor of /{id}/apply
    @GetMapping("/tim-kiem")
    public String index(
            @RequestParam(value = "ten", required = false) String ten,
            @RequestParam(value = "trangThai", required = false) Integer trangThai,
            @RequestParam(value = "loai", required = false) String loai,
            Model model) {
        List<DotGiamGia> dotGiamGias = dotGiamGiaRepository.findAll();

        if (ten != null && !ten.trim().isEmpty()) {
            dotGiamGias = dotGiamGias.stream()
                    .filter(d -> d.getTen() != null && d.getTen().toLowerCase().contains(ten.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (trangThai != null) {
            dotGiamGias = dotGiamGias.stream()
                    .filter(d -> d.getTrangThai() == trangThai)
                    .collect(Collectors.toList());
        }

        if (loai != null && !loai.trim().isEmpty()) {
            dotGiamGias = dotGiamGias.stream()
                    .filter(d -> d.getLoai() != null && d.getLoai().equalsIgnoreCase(loai))
                    .collect(Collectors.toList());
        }

        model.addAttribute("dotGiamGias", dotGiamGias);
        model.addAttribute("dotGiamGia", new DotGiamGia()); // nếu bạn đang dùng form thêm
        return "admin/pages/dot-giam-gia/dot-giam-gia"; // tên template của bạn
    }

    //  Khi click sửa → load lại form với dữ liệu cũ
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        capNhatTrangThaiTuDong();
        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(id).orElse(new DotGiamGia());
        model.addAttribute("dotGiamGia", dotGiamGia);
        return "admin/pages/dot-giam-gia/edit";
    }

    @GetMapping("/{id}/apply")
    public String applyPage(@PathVariable Integer id, Model model) {
        DotGiamGia dot = dotGiamGiaRepository.findById(id).orElse(null);
        if (dot == null) {
            return "redirect:/admin/dot-giam-gia";
        }
        model.addAttribute("dotGiamGia", dot);
        return "admin/pages/dot-giam-gia/apply";
    }

    //  Lưu (thêm hoặc cập nhật)
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("dotGiamGia") DotGiamGia dotGiamGia,
                       BindingResult result, RedirectAttributes redirectAttributes,
                       Model model,
                       @RequestParam(value = "backToList", required = false) Boolean backToList) {

        // 1) Validate ngày: ngày bắt đầu phải trước ngày kết thúc
        if (dotGiamGia.getNgayBatDau() != null && dotGiamGia.getNgayKetThuc() != null
                && dotGiamGia.getNgayBatDau().isAfter(dotGiamGia.getNgayKetThuc())) {
            result.rejectValue("ngayBatDau", null, "Ngày bắt đầu phải trước ngày kết thúc");
        }
        // 1.1) Validate mã trùng
        if (dotGiamGia.getMa() != null && !dotGiamGia.getMa().trim().isEmpty()) {
            boolean duplicate;
            if (dotGiamGia.getId() == null) {
                // Thêm mới → chỉ cần check tồn tại mã
                duplicate = dotGiamGiaRepository.existsByMa(dotGiamGia.getMa());
            } else {
                // Cập nhật → check trùng với record khác
                duplicate = dotGiamGiaRepository.existsByMaAndIdNot(dotGiamGia.getMa(), dotGiamGia.getId());
            }
            if (duplicate) {
                result.rejectValue("ma", null, "Mã đã tồn tại");
            }
        }
        // 2) Validate tên bắt buộc
        if (dotGiamGia.getTen() == null || dotGiamGia.getTen().trim().isEmpty()) {
            result.rejectValue("ten", null, "Tên đợt giảm giá là bắt buộc");
        }
        // 3) Validate giá trị theo loại
        if (dotGiamGia.getLoai() != null && dotGiamGia.getGiaTriDotGiamGia() != null) {
            String loai = dotGiamGia.getLoai();
            var val = dotGiamGia.getGiaTriDotGiamGia();
            if ("phan_tram".equalsIgnoreCase(loai)) {
                // phần trăm 1..100
                if (val.intValue() < 1 || val.intValue() > 100) {
                    result.rejectValue("giaTriDotGiamGia", null, "Phần trăm phải từ 1 đến 100");
                }
            } else if ("tien_mat".equalsIgnoreCase(loai)) {
                // tiền mặt >= 0
                if (val.signum() <= 0) {
                    result.rejectValue("giaTriDotGiamGia", null, "Số tiền giảm phải > 0");
                }
            }
        }

        if (result.hasErrors()) {
            // Trả về lại view nếu có lỗi
            model.addAttribute("dotGiamGias", dotGiamGiaRepository.findAll());
            return "admin/pages/dot-giam-gia/dot-giam-gia";
        }

        // 4) Tự sinh mã GG nếu để trống
        if (dotGiamGia.getMa() == null || dotGiamGia.getMa().trim().isEmpty()) {
            String generated;
            int attempts = 0;
            do {
                generated = "GG" + String.format("%06d", (int) (Math.random() * 1_000_000));
                attempts++;
            } while (dotGiamGiaRepository.existsByMa(generated) && attempts < 20);
            dotGiamGia.setMa(generated);
        }

        // 5) Thiết lập ngày tạo/sửa
        if (dotGiamGia.getId() == null) {
            dotGiamGia.setNgayTao(LocalDateTime.now());
        } else {
            DotGiamGia existing = dotGiamGiaRepository.findById(dotGiamGia.getId()).orElse(null);
            if (existing != null) {
                dotGiamGia.setNgayTao(existing.getNgayTao());
            }
        }

        dotGiamGia.setNgaySua(LocalDateTime.now());
        int adminId = 1; // TODO: Lấy từ user đăng nhập
        dotGiamGia.setNguoiTao(adminId);

        capNhatTrangThaiTuDong();
        boolean isNew = (dotGiamGia.getId() == null);
        dotGiamGiaRepository.save(dotGiamGia);
        redirectAttributes.addFlashAttribute("success", isNew ? "Thêm đợt giảm giá thành công!" : "Cập nhật đợt giảm giá thành công!");
        // Điều chỉnh luồng: nếu yêu cầu quay về danh sách (từ trang thêm), redirect về list; ngược lại về trang sửa
        if (Boolean.TRUE.equals(backToList)) {
            return "redirect:/admin/dot-giam-gia";
        }
        return "redirect:/admin/dot-giam-gia/edit/" + dotGiamGia.getId();
    }

    //  Xoá
//    @GetMapping("/delete/{id}")
//    public String delete(@PathVariable Integer id) {
//        dotGiamGiaRepository.deleteById(id);
//        return "redirect:/admin/dot-giam-gia";
//    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
        // Cập nhật lại trạng thái theo thời gian trước khi kiểm tra
        capNhatTrangThaiTuDong();

        Optional<DotGiamGia> optional = dotGiamGiaRepository.findById(id);
        if (optional.isPresent()) {
            DotGiamGia dot = optional.get();

            // Chỉ cho phép xóa khi trạng thái = 2 (Ngừng hoạt động)
            if (dot.getTrangThai() == 0) {
                attributes.addFlashAttribute("error", "Không thể xóa khi đợt đang ở trạng thái Chuẩn bị hoạt động.");
                return "redirect:/admin/dot-giam-gia";
            }
            if (dot.getTrangThai() == 1) {
                attributes.addFlashAttribute("error", "Không thể xóa khi đợt đang ở trạng thái Đang hoạt động.");
                return "redirect:/admin/dot-giam-gia";
            }
            if (dot.getTrangThai() != 2) {
                attributes.addFlashAttribute("error", "Trạng thái không hợp lệ để xóa.");
                return "redirect:/admin/dot-giam-gia";
            }

            // Nếu còn sản phẩm đang gán vào đợt này thì cảnh báo và không cho xóa (tránh orphan dữ liệu)
            long attached = chiTietSanPhamRepo.countByDotGiamGia_Id(id);
            if (attached > 0) {
                attributes.addFlashAttribute("error", "Không thể xóa: còn " + attached + " sản phẩm đang áp dụng đợt này.");
                return "redirect:/admin/dot-giam-gia";
            }

            dotGiamGiaRepository.deleteById(id);
            attributes.addFlashAttribute("success", "Xóa đợt giảm giá thành công.");
        } else {
            attributes.addFlashAttribute("error", "Không tìm thấy đợt giảm giá.");
        }
        return "redirect:/admin/dot-giam-gia";
    }

    // ================== CÁC METHOD HỖ TRỢ TÍNH TOÁN GIÁ ==================

    /**
     * Tính toán giá bán sau khi áp dụng đợt giảm giá
     * Sử dụng logic đơn giản để tránh dependency
     */
    private BigDecimal calculateAndApplyDiscount(BigDecimal giaGoc, DotGiamGia dot) {
        if (giaGoc == null || dot == null || dot.getGiaTriDotGiamGia() == null || dot.getLoai() == null) {
            return giaGoc;
        }

        BigDecimal giaBan = giaGoc;
        BigDecimal giaTri = dot.getGiaTriDotGiamGia();

        if ("phan_tram".equalsIgnoreCase(dot.getLoai())) {
            // Giảm giá theo phần trăm (1-100)
            if (giaTri.compareTo(BigDecimal.ZERO) > 0 && giaTri.compareTo(new BigDecimal(100)) <= 0) {
                BigDecimal phanTram = giaTri.divide(new BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                BigDecimal soTienGiam = giaGoc.multiply(phanTram);
                giaBan = giaGoc.subtract(soTienGiam);
            }
        } else if ("tien_mat".equalsIgnoreCase(dot.getLoai())) {
            // Giảm giá theo tiền mặt
            if (giaTri.compareTo(BigDecimal.ZERO) > 0) {
                giaBan = giaGoc.subtract(giaTri);
            }
        }

        // Đảm bảo giá không âm
        return giaBan.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : giaBan;
    }


}