package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.BanDich;
import com.main.datn_sd31.entity.CacheBanDichAi;
import com.main.datn_sd31.entity.NgonNgu;
import com.main.datn_sd31.entity.LoaiThu;
import com.main.datn_sd31.entity.DanhMuc;
import com.main.datn_sd31.entity.KieuDang;
import com.main.datn_sd31.entity.XuatXu;
import com.main.datn_sd31.entity.ChatLieu;
import com.main.datn_sd31.repository.BanDichRepository;
import com.main.datn_sd31.repository.CacheBanDichAiRepository;
import com.main.datn_sd31.repository.NgonNguRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class DichVuBanDichService {
    
    private final BanDichRepository banDichRepository;
    private final NgonNguRepository ngonNguRepository;
    private final CacheBanDichAiRepository cacheBanDichAiRepository;
    private final DichVuBanDichAiService dichVuBanDichAiService;
    
    // Cache in-memory cho hiệu suất
    private final Map<String, Map<String, String>> translationCache = new ConcurrentHashMap<>();
    
    /**
     * Lấy bản dịch theo key và ngôn ngữ
     */
    public String layBanDich(String tenKhoa, String maNgonNgu) {
        try {
            // 1. Kiểm tra cache in-memory trước
            Map<String, String> langCache = translationCache.get(maNgonNgu);
            if (langCache != null && langCache.containsKey(tenKhoa)) {
                return langCache.get(tenKhoa);
            }
            
            // 2. Tìm trong database
            Optional<BanDich> banDich = banDichRepository.findByTenKhoaAndMaNgonNgu(tenKhoa, maNgonNgu);
            if (banDich.isPresent()) {
                String noiDung = banDich.get().getNoiDung();
                
                // Cập nhật cache
                langCache = translationCache.computeIfAbsent(maNgonNgu, k -> new ConcurrentHashMap<>());
                langCache.put(tenKhoa, noiDung);
                
                return noiDung;
            }
            
            // 3. Fallback về ngôn ngữ mặc định
            Optional<NgonNgu> ngonNguMacDinh = ngonNguRepository.findByMacDinhTrue();
            if (ngonNguMacDinh.isPresent() && !maNgonNgu.equals(ngonNguMacDinh.get().getMaNgonNgu())) {
                return layBanDich(tenKhoa, ngonNguMacDinh.get().getMaNgonNgu());
            }
            
            // 4. Trả về key nếu không tìm thấy
            return tenKhoa;
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy bản dịch cho key: {} và ngôn ngữ: {}", tenKhoa, maNgonNgu, e);
            return tenKhoa;
        }
    }
    
    /**
     * Lấy bản dịch; nếu chưa có thì dùng AI dịch từ văn bản nguồn và lưu vào DB, sau đó trả về.
     * sourceLang là mã ngôn ngữ của sourceText (thường là ngôn ngữ mặc định).
     */
    public String layBanDichOrTranslate(String tenKhoa, String targetLang, String sourceText, String sourceLang) {
        try {
            // Thử lấy từ cache/DB trước
            String existing = layBanDich(tenKhoa, targetLang);
            // Nếu layBanDich trả về đúng nội dung đã dịch (khác với key) thì dùng luôn
            if (existing != null && !existing.equals(tenKhoa)) {
                return existing;
            }
            // Nếu targetLang trùng sourceLang thì dùng nguồn
            if (targetLang != null && targetLang.equalsIgnoreCase(sourceLang)) {
                return sourceText;
            }
            // Gọi AI dịch
            String translated = dichVuBanDichAiService.dichVanBan(sourceText, sourceLang, targetLang);
            if (translated == null || translated.trim().isEmpty()) {
                return sourceText; // fallback cuối
            }
            // Lưu vào DB
            String danhMuc = "product"; // default
            if (tenKhoa.startsWith("loai_thu.") || tenKhoa.startsWith("danh_muc.") || 
                tenKhoa.startsWith("kieu_dang.") || tenKhoa.startsWith("xuat_xu.") || 
                tenKhoa.startsWith("chat_lieu.")) {
                danhMuc = "entity";
            }
            
            BanDich banDich = BanDich.builder()
                .tenKhoa(tenKhoa)
                .maNgonNgu(targetLang)
                .noiDung(translated)
                .danhMuc(danhMuc)
                .duocTaoBoiAi(true)
                .ngayTao(LocalDateTime.now())
                .ngaySua(LocalDateTime.now())
                .build();
            banDichRepository.save(banDich);

            // Cập nhật cache
            Map<String, String> langCache = translationCache.computeIfAbsent(targetLang, k -> new ConcurrentHashMap<>());
            langCache.put(tenKhoa, translated);

            // Đồng thời lưu vào bảng cache_ban_dich_ai để có thể theo dõi và tái sử dụng
            try {
                Optional<CacheBanDichAi> existingCache = cacheBanDichAiRepository.findByVanBanNguonAndNgonNguNguonAndNgonNguDich(
                        sourceText, sourceLang, targetLang);
                if (existingCache.isEmpty()) {
                    CacheBanDichAi cache = CacheBanDichAi.builder()
                            .vanBanNguon(sourceText)
                            .ngonNguNguon(sourceLang)
                            .ngonNguDich(targetLang)
                            .vanBanDaDich(translated)
                            .diemTinCay(new BigDecimal("0.85"))
                            .soLanSuDung(1)
                            .ngayTao(LocalDateTime.now())
                            .lanCuoiSuDung(LocalDateTime.now())
                            .build();
                    cacheBanDichAiRepository.save(cache);
                }
            } catch (Exception ex) {
                log.debug("Không thể lưu cache AI cho key {}: {}", tenKhoa, ex.getMessage());
            }
            
            return translated;
        } catch (Exception e) {
            log.error("Lỗi khi auto-translate key: {} từ {} sang {}", tenKhoa, sourceLang, targetLang, e);
            return sourceText;
        }
    }
    
    /**
     * Dịch văn bản động bằng AI
     */
    public String dichVanBanDong(String vanBan, String ngonNguNguon, String ngonNguDich) {
        try {
            // 1. Kiểm tra cache AI trước
            Optional<CacheBanDichAi> cache = cacheBanDichAiRepository
                .findByVanBanNguonAndNgonNguNguonAndNgonNguDich(vanBan, ngonNguNguon, ngonNguDich);
            
            if (cache.isPresent()) {
                // Tăng số lần sử dụng
                cacheBanDichAiRepository.incrementUsageCount(cache.get().getId(), LocalDateTime.now());
                return cache.get().getVanBanDaDich();
            }
            
            // 2. Dịch bằng AI
            String vanBanDaDich = dichVuBanDichAiService.dichVanBan(vanBan, ngonNguNguon, ngonNguDich);
            
            if (vanBanDaDich != null && !vanBanDaDich.trim().isEmpty()) {
                // 3. Lưu vào cache
                CacheBanDichAi cacheMoi = CacheBanDichAi.builder()
                    .vanBanNguon(vanBan)
                    .ngonNguNguon(ngonNguNguon)
                    .ngonNguDich(ngonNguDich)
                    .vanBanDaDich(vanBanDaDich)
                    .diemTinCay(new BigDecimal("0.85")) // Điểm tin cậy mặc định
                    .soLanSuDung(1)
                    .ngayTao(LocalDateTime.now())
                    .lanCuoiSuDung(LocalDateTime.now())
                    .build();
                
                cacheBanDichAiRepository.save(cacheMoi);
                return vanBanDaDich;
            }
            
            return vanBan; // Trả về văn bản gốc nếu dịch thất bại
            
        } catch (Exception e) {
            log.error("Lỗi khi dịch văn bản: {} từ {} sang {}", vanBan, ngonNguNguon, ngonNguDich, e);
            return vanBan;
        }
    }
    
    /**
     * Lấy danh sách ngôn ngữ hoạt động
     */
    public List<NgonNgu> layDanhSachNgonNguHoatDong() {
        return ngonNguRepository.findByTrangThaiTrueOrderByThuTu();
    }
    
    /**
     * Lấy ngôn ngữ mặc định
     */
    public Optional<NgonNgu> layNgonNguMacDinh() {
        return ngonNguRepository.findByMacDinhTrue();
    }
    
    /**
     * Làm mới cache
     */
    public void lamMoiCache() {
        translationCache.clear();
        log.info("Đã làm mới cache bản dịch");
    }
    
    /**
     * Lưu bản dịch mới
     */
    public BanDich luuBanDich(String tenKhoa, String maNgonNgu, String noiDung, String danhMuc) {
        BanDich banDich = BanDich.builder()
            .tenKhoa(tenKhoa)
            .maNgonNgu(maNgonNgu)
            .noiDung(noiDung)
            .danhMuc(danhMuc)
            .duocTaoBoiAi(false)
            .ngayTao(LocalDateTime.now())
            .ngaySua(LocalDateTime.now())
            .build();
        
        BanDich banDichLuu = banDichRepository.save(banDich);
        
        // Cập nhật cache
        Map<String, String> langCache = translationCache.computeIfAbsent(maNgonNgu, k -> new ConcurrentHashMap<>());
        langCache.put(tenKhoa, noiDung);
        
        return banDichLuu;
    }
    
    /**
     * Dịch nhiều văn bản cùng lúc
     */
    public Map<String, String> dichNhieuVanBan(Map<String, String> vanBanMap, String ngonNguNguon, String ngonNguDich) {
        Map<String, String> ketQua = new ConcurrentHashMap<>();
        
        for (Map.Entry<String, String> entry : vanBanMap.entrySet()) {
            String key = entry.getKey();
            String vanBan = entry.getValue();
            
            String banDich = dichVanBanDong(vanBan, ngonNguNguon, ngonNguDich);
            if (banDich != null) {
                ketQua.put(key, banDich);
            } else {
                ketQua.put(key, vanBan); // Fallback về văn bản gốc
            }
        }
        
        return ketQua;
    }
    
    /**
     * Lấy tất cả bản dịch theo ngôn ngữ
     */
    public List<BanDich> layTatCaBanDichTheoNgonNgu(String maNgonNgu) {
        return banDichRepository.findByMaNgonNgu(maNgonNgu);
    }
    
    /**
     * Lấy bản dịch theo danh mục
     */
    public List<BanDich> layBanDichTheoDanhMuc(String danhMuc) {
        return banDichRepository.findByDanhMuc(danhMuc);
    }
    
    /**
     * Kiểm tra bản dịch có tồn tại không
     */
    public boolean kiemTraBanDichTonTai(String tenKhoa, String maNgonNgu) {
        return banDichRepository.existsByTenKhoaAndMaNgonNgu(tenKhoa, maNgonNgu);
    }
    
    /**
     * Xóa bản dịch
     */
    public void xoaBanDich(Integer id) {
        banDichRepository.deleteById(id);
        lamMoiCache(); // Làm mới cache sau khi xóa
    }
    
    /**
     * Cập nhật bản dịch
     */
    public BanDich capNhatBanDich(Integer id, String noiDung) {
        Optional<BanDich> banDichOpt = banDichRepository.findById(id);
        if (banDichOpt.isPresent()) {
            BanDich banDich = banDichOpt.get();
            banDich.setNoiDung(noiDung);
            banDich.setNgaySua(LocalDateTime.now());
            
            BanDich banDichCapNhat = banDichRepository.save(banDich);
            
            // Cập nhật cache
            Map<String, String> langCache = translationCache.computeIfAbsent(banDich.getMaNgonNgu(), k -> new ConcurrentHashMap<>());
            langCache.put(banDich.getTenKhoa(), noiDung);
            
            return banDichCapNhat;
        }
        return null;
    }
    
    /**
     * Dịch tên loại thú
     */
    public Map<Integer, String> dichTenLoaiThu(List<LoaiThu> danhSachLoaiThu, String targetLang) {
        String sourceLang = "vi";
        Map<Integer, String> tenLoaiThuDaDich = new HashMap<>();
        
        try {
            for (LoaiThu loaiThu : danhSachLoaiThu) {
                String key = "loai_thu.name." + loaiThu.getId();
                String translated = layBanDichOrTranslate(key, targetLang, loaiThu.getTen(), sourceLang);
                tenLoaiThuDaDich.put(loaiThu.getId(), translated);
            }
        } catch (Exception e) {
            log.debug("Không thể dịch tên loại thú: {}", e.getMessage());
        }
        
        return tenLoaiThuDaDich;
    }
    
    /**
     * Dịch tên danh mục
     */
    public Map<Integer, String> dichTenDanhMuc(List<DanhMuc> danhSachDanhMuc, String targetLang) {
        String sourceLang = "vi";
        Map<Integer, String> tenDanhMucDaDich = new HashMap<>();
        
        try {
            for (DanhMuc danhMuc : danhSachDanhMuc) {
                String key = "danh_muc.name." + danhMuc.getId();
                String translated = layBanDichOrTranslate(key, targetLang, danhMuc.getTen(), sourceLang);
                tenDanhMucDaDich.put(danhMuc.getId(), translated);
            }
        } catch (Exception e) {
            log.debug("Không thể dịch tên danh mục: {}", e.getMessage());
        }
        
        return tenDanhMucDaDich;
    }
    
    /**
     * Dịch tên kiểu dáng
     */
    public Map<Integer, String> dichTenKieuDang(List<KieuDang> danhSachKieuDang, String targetLang) {
        String sourceLang = "vi";
        Map<Integer, String> tenKieuDangDaDich = new HashMap<>();
        
        try {
            for (KieuDang kieuDang : danhSachKieuDang) {
                String key = "kieu_dang.name." + kieuDang.getId();
                String translated = layBanDichOrTranslate(key, targetLang, kieuDang.getTen(), sourceLang);
                tenKieuDangDaDich.put(kieuDang.getId(), translated);
            }
        } catch (Exception e) {
            log.debug("Không thể dịch tên kiểu dáng: {}", e.getMessage());
        }
        
        return tenKieuDangDaDich;
    }
    
    /**
     * Dịch tên xuất xứ
     */
    public Map<Integer, String> dichTenXuatXu(List<XuatXu> danhSachXuatXu, String targetLang) {
        String sourceLang = "vi";
        Map<Integer, String> tenXuatXuDaDich = new HashMap<>();
        
        try {
            for (XuatXu xuatXu : danhSachXuatXu) {
                String key = "xuat_xu.name." + xuatXu.getId();
                String translated = layBanDichOrTranslate(key, targetLang, xuatXu.getTen(), sourceLang);
                tenXuatXuDaDich.put(xuatXu.getId(), translated);
            }
        } catch (Exception e) {
            log.debug("Không thể dịch tên xuất xứ: {}", e.getMessage());
        }
        
        return tenXuatXuDaDich;
    }
    
    /**
     * Dịch tên chất liệu
     */
    public Map<Integer, String> dichTenChatLieu(List<ChatLieu> danhSachChatLieu, String targetLang) {
        String sourceLang = "vi";
        Map<Integer, String> tenChatLieuDaDich = new HashMap<>();
        
        try {
            for (ChatLieu chatLieu : danhSachChatLieu) {
                String key = "chat_lieu.name." + chatLieu.getId();
                String translated = layBanDichOrTranslate(key, targetLang, chatLieu.getTen(), sourceLang);
                tenChatLieuDaDich.put(chatLieu.getId(), translated);
            }
        } catch (Exception e) {
            log.debug("Không thể dịch tên chất liệu: {}", e.getMessage());
        }
        
        return tenChatLieuDaDich;
    }
}
