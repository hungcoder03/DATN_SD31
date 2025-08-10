package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.Enum.LyDoGiaoKhongThanhCong;
import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonChiTietDTO;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.dto.lich_su_hoa_don_dto.KetQuaCapNhatTrangThai;
import com.main.datn_sd31.dto.lich_su_hoa_don_dto.LichSuHoaDonDTO;
import com.main.datn_sd31.entity.*;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.HoaDonRepository;
import com.main.datn_sd31.repository.LichSuHoaDonRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.service.HoaDonChiTietService;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.LichSuHoaDonService;
import com.main.datn_sd31.util.HoaDonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LichSuHoaDonServiceIpml implements LichSuHoaDonService {

    private final LichSuHoaDonRepository lichSuHoaDonRepository;

    private final HoaDonRepository hoaDonRepository;

    private final NhanVienRepository nhanVienRepository;

    private final HoaDonService hoaDonService;

    private final HoaDonChiTietService hoaDonChiTietService;

    private final Chitietsanphamrepository chitietsanphamrepository;


    @Override
    public List<LichSuHoaDon> getLichSuHoaDonByHoaDon(String maHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findByMaContainingIgnoreCase(maHoaDon).get(0);
        return lichSuHoaDonRepository.findLichSuHoaDonsByHoaDon(hoaDon);
    }

    @Override
    public void capNhatTrangThai(String maHoaDon, Integer trangThaiMoi, String ghiChu, NhanVien nhanVien, Integer lyDoGiaoKhongThanhCong) {
        HoaDon hoaDon = hoaDonRepository.findByMaContainingIgnoreCase(maHoaDon).get(0);
        if (hoaDon == null) return;

        List<LichSuHoaDon> lichSuList = lichSuHoaDonRepository.findLichSuHoaDonsByHoaDon(hoaDon);

        String finalGhiChu = (trangThaiMoi == 10)
                ? LyDoGiaoKhongThanhCong.fromValue(lyDoGiaoKhongThanhCong).getMoTa()
                : (ghiChu != null && !ghiChu.isBlank()
                    ? ghiChu
                    : "Cập nhật trạng thái: " + TrangThaiLichSuHoaDon.fromValue(trangThaiMoi).getMoTa());


        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .hoaDon(hoaDon)
                .trangThai(trangThaiMoi)
                .ghiChu(finalGhiChu)
                .lyDoGiaoKhongThanhCong(lyDoGiaoKhongThanhCong)
                .ngayTao(LocalDateTime.now())
                .nguoiTao(nhanVien.getId())
                .build();

        lichSuHoaDonRepository.save(lichSu);
    }

    @Override
    public void capNhatTrangThaiByKhachHang(String maHoaDon, Integer trangThaiMoi, String ghiChu, KhachHang khachHang) {
        HoaDon hoaDon = hoaDonRepository.findByMaContainingIgnoreCase(maHoaDon).get(0);
        if (hoaDon == null) return;

        List<LichSuHoaDon> lichSuList = lichSuHoaDonRepository.findLichSuHoaDonsByHoaDon(hoaDon);

        String finalGhiChu = (ghiChu != null && !ghiChu.isBlank())
                ? ghiChu
                : "Cập nhật trạng thái: " + TrangThaiLichSuHoaDon.fromValue(trangThaiMoi).getMoTa();

        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .hoaDon(hoaDon)
                .trangThai(trangThaiMoi)
                .ghiChu(finalGhiChu)
                .ngayTao(LocalDateTime.now())
                .nguoiTao(khachHang.getId())
                .build();

        lichSuHoaDonRepository.save(lichSu);
    }

    @Override
    public List<TrangThaiLichSuHoaDon> getTrangThaiTiepTheoHopLe(TrangThaiLichSuHoaDon hienTai, HoaDonDTO hoaDonDTO) {
        return switch (hienTai) {
            case CHO_XAC_NHAN -> List.of(TrangThaiLichSuHoaDon.XAC_NHAN, TrangThaiLichSuHoaDon.HUY);
            case XAC_NHAN -> {
                if (hoaDonDTO.getDiaChi().isEmpty()) {
                    yield List.of(TrangThaiLichSuHoaDon.CHO_GIAO_HANG);
                }
                yield List.of(TrangThaiLichSuHoaDon.CHO_GIAO_HANG, TrangThaiLichSuHoaDon.HOAN_THANH, TrangThaiLichSuHoaDon.HUY);
            }
            case CHO_GIAO_HANG -> {
//                if ("Chưa thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
//                    yield List.of();
//                }
                yield List.of(TrangThaiLichSuHoaDon.DA_GIAO, TrangThaiLichSuHoaDon.GIAO_KHONG_THANH_CONG);
            }

            case GIAO_KHONG_THANH_CONG -> List.of(TrangThaiLichSuHoaDon.DON_CHUYEN_HOAN, TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG, TrangThaiLichSuHoaDon.HUY);
            case YEU_CAU_HOAN_HANG -> List.of(TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG);
            case DON_CHUYEN_HOAN, XAC_NHAN_HOAN_HANG -> List.of(TrangThaiLichSuHoaDon.DA_HOAN);
            default -> List.of(); // HOAN_THANH, DA_HOAN, HUY không được chuyển tiếp
        };
    }

    @Override
    public List<LichSuHoaDonDTO> getLichSuHoaDonDTOByHoaDon(String maHoaDon) {
        List<LichSuHoaDon> lichSuList = getLichSuHoaDonByHoaDon(maHoaDon);  // Gọi hàm bạn đã viết
        return lichSuList.stream()
                .map(this::mapToDTO)  // Gọi hàm chuyển đổi DTO
                .collect(Collectors.toList());
    }

    private LichSuHoaDonDTO mapToDTO(LichSuHoaDon lichSuHoaDon) {
        LichSuHoaDonDTO dto = new LichSuHoaDonDTO();

        dto.setNgayTao(lichSuHoaDon.getNgayTao());
        dto.setTrangThaiMoTa(lichSuHoaDon.getTrangThaiMoTa());
        dto.setGhiChu(lichSuHoaDon.getGhiChu());
        dto.setNguoiTao(lichSuHoaDon.getNguoiTao());
        dto.setTrangThaiLichSuHoaDon(TrangThaiLichSuHoaDon.fromValue(lichSuHoaDon.getTrangThai()));
        dto.setLyDoGiaoKhongThanhCong(lichSuHoaDon.getLyDoGiaoKhongThanhCong());

        // Giả sử người tạo là nhân viên → lấy tên nhân viên theo ID
        if (lichSuHoaDon.getNguoiTao() != null) {
            nhanVienRepository.findById(lichSuHoaDon.getNguoiTao())  // ← đúng là lấy theo getNguoiTao
                    .ifPresent(nv -> dto.setTenNguoiTao(nv.getTen()));
        }

        return dto;
    }

    private List<LichSuHoaDonDTO> mapToDTO(List<LichSuHoaDon> entities) {
        return entities.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuHoaDonDTO> getLichSuHoaDonDTODescByMaHoaDon(String maHoaDon) {
        List<LichSuHoaDon> lichSuHoaDons = lichSuHoaDonRepository.findByMaHoaDonDesc(maHoaDon);
        return mapToDTO(lichSuHoaDons);
    }

    public TrangThaiLichSuHoaDon getTrangThaiTruocDo(String maHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findByMaContainingIgnoreCase(maHoaDon).get(0);
        List<LichSuHoaDon> lichSu = lichSuHoaDonRepository.findLichSuHoaDonsByHoaDonOrderByNgayTaoDesc(hoaDon);

        if (lichSu.isEmpty()) return null;

        return TrangThaiLichSuHoaDon.fromValue(lichSu.get(0).getTrangThai());
    }

    @Override
    public KetQuaCapNhatTrangThai xuLyCapNhatTrangThai(String maHoaDon, Integer trangThaiMoi, Integer lyDoGiaoKhongThanhCong, String ghiChu, NhanVien nhanVien) {
        HoaDonDTO hoaDonDTO = hoaDonService.getHoaDonByMa(maHoaDon);
        TrangThaiLichSuHoaDon trangThaiHienTai = hoaDonDTO.getTrangThaiLichSuHoaDon();

        if (trangThaiMoi == null) {
            return new KetQuaCapNhatTrangThai(false, "Vui lòng chọn trạng thái mới");
        }

        TrangThaiLichSuHoaDon trangThaiMoiEnum = TrangThaiLichSuHoaDon.fromValue(trangThaiMoi);

        if (trangThaiMoiEnum == trangThaiHienTai) {
            return new KetQuaCapNhatTrangThai(false, "Trạng thái mới không được trùng với trạng thái hiện tại");
        }

        //Cho phép thay đổi các trạng thái tiếp theo
        boolean hopLe = switch (trangThaiHienTai) {
            case CHO_XAC_NHAN -> trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN || trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
            case XAC_NHAN -> {
                if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH
                        && "Chưa thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
                    yield false;
                } else if (hoaDonDTO.getDiaChi() == null || hoaDonDTO.getDiaChi().isEmpty()) {
                    yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH;
                }
                yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.CHO_GIAO_HANG || trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
            }
            case CHO_GIAO_HANG -> trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_GIAO || trangThaiMoiEnum == TrangThaiLichSuHoaDon.GIAO_KHONG_THANH_CONG;
            case DA_GIAO -> {
                if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH && !"Đã thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
                    yield false;
                }
                yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH || trangThaiMoiEnum == TrangThaiLichSuHoaDon.YEU_CAU_HOAN_HANG;
            }
            case GIAO_KHONG_THANH_CONG -> {
                if (xuLyDonHangGiaoKhongThanhCong(lyDoGiaoKhongThanhCong) == 0) {
                    yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY|| trangThaiMoiEnum == TrangThaiLichSuHoaDon.DON_CHUYEN_HOAN ||
                trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG;
                }
                if (xuLyDonHangGiaoKhongThanhCong(lyDoGiaoKhongThanhCong) == 1) {
                    yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
                } else if (xuLyDonHangGiaoKhongThanhCong(lyDoGiaoKhongThanhCong) == 2) {
                    yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.DON_CHUYEN_HOAN;
                } yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG;
            }
            case YEU_CAU_HOAN_HANG -> trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG;
            case XAC_NHAN_HOAN_HANG, DON_CHUYEN_HOAN -> trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_HOAN;
            case HOAN_THANH, HUY, DA_HOAN -> false;
        };

        if (!hopLe) {
            return new KetQuaCapNhatTrangThai(false, "Trạng thái mới không hợp lệ theo luồng xử lý");
        }

        List<HoaDonChiTietDTO> hdctList = hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(maHoaDon);

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN) {
            for (HoaDonChiTietDTO ct : hdctList) {
                ChiTietSanPham spct = chitietsanphamrepository.findById(ct.getIdCTSP()).orElse(null);
                if (spct == null) {
                    return new KetQuaCapNhatTrangThai(false, "Không tìm thấy sản phẩm có ID: " + ct.getIdCTSP());
                }
                if (spct.getSoLuong() < ct.getSoLuong()) {
                    return new KetQuaCapNhatTrangThai(false, "Sản phẩm \"" + spct.getSanPham().getTen() + "\" không đủ tồn kho!");
                }

                spct.setSoLuong(spct.getSoLuong() - ct.getSoLuong());
                chitietsanphamrepository.save(spct);
            }
        }

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY) {

            //Đổi trạng thái cho hóa đơn đã Hủy
            HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
            hoaDon.setTrangThai(5);
            hoaDon.setNgaySua(LocalDateTime.now());
            hoaDonRepository.save(hoaDon);

            for (HoaDonChiTietDTO ct : hdctList) {
                ChiTietSanPham spct = chitietsanphamrepository.findById(ct.getIdCTSP()).orElse(null);
                if (spct == null) {
                    return new KetQuaCapNhatTrangThai(false, "Không tìm thấy sản phẩm có ID: " + ct.getIdCTSP());
                }

                // Nếu trước đó đã XÁC NHẬN => cộng lại số lượng
                if (getTrangThaiTruocDo(maHoaDon) == TrangThaiLichSuHoaDon.XAC_NHAN) {
                    if (xuLyDonHangGiaoKhongThanhCong(lyDoGiaoKhongThanhCong) == 2 || xuLyDonHangGiaoKhongThanhCong(lyDoGiaoKhongThanhCong) == 0) {
                        spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                        chitietsanphamrepository.save(spct);
                    }
                }
            }

        }

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_HOAN) {

            //Đổi trạng thái thành chưa thanh toán cho hóa đơn đã Hủy
            HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
            hoaDon.setTrangThai(4);
            hoaDon.setNgaySua(LocalDateTime.now());
            hoaDonRepository.save(hoaDon);

            for (HoaDonChiTietDTO ct : hdctList) {
                ChiTietSanPham spct = chitietsanphamrepository.findById(ct.getIdCTSP()).orElse(null);
                if (spct == null) {
                    return new KetQuaCapNhatTrangThai(false, "Không tìm thấy sản phẩm có ID: " + ct.getIdCTSP());
                }

                spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                chitietsanphamrepository.save(spct);
            }
        }

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_GIAO) {

            //Đổi trạng thái thành đã thanh toán cho hóa đơn đã giao
            HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
            hoaDon.setTrangThai(3);
            hoaDon.setNgaySua(LocalDateTime.now());
            hoaDon.setNgayThanhToan(LocalDateTime.now());
            hoaDonRepository.save(hoaDon);

        }

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.GIAO_KHONG_THANH_CONG) {

            //Đổi trạng thái thành đã thanh toán cho hóa đơn đã giao
            HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
            hoaDon.setTrangThai(2);
            hoaDon.setNgaySua(LocalDateTime.now());
//            hoaDon.setThanhTien(BigDecimal.valueOf(0));
            hoaDonRepository.save(hoaDon);

        }

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.DON_CHUYEN_HOAN) {

            //Đổi trạng thái thành đã thanh toán cho hóa đơn đã giao
            HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
            hoaDon.setTrangThai(2);
            hoaDon.setNgaySua(LocalDateTime.now());
//            hoaDon.setThanhTien(BigDecimal.valueOf(0));
            hoaDonRepository.save(hoaDon);

        }

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY &&
                HoaDonUtils.choPhepHuyDonKhachHang(trangThaiMoiEnum)) {
            return new KetQuaCapNhatTrangThai(false, "Đơn hàng không thể huỷ ở trạng thái hiện tại.");
        }

//        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_HOAN &&
//                !HoaDonUtils.choPhepHoanHangKhachHang(trangThaiMoiEnum)) {
//            return new KetQuaCapNhatTrangThai(false, "Đơn hàng chưa được giao nên không thể hoàn.");
//        }

        capNhatTrangThai(maHoaDon, trangThaiMoi, ghiChu, nhanVien, lyDoGiaoKhongThanhCong);
        return new KetQuaCapNhatTrangThai(true, "Cập nhật trạng thái thành công");
    }

    private Integer xuLyDonHangGiaoKhongThanhCong(Integer lyDoGiaoKhongThanhCong) {

        if (lyDoGiaoKhongThanhCong != null) {
            LyDoGiaoKhongThanhCong lyDoGiaoKhongThanhCongEnum = LyDoGiaoKhongThanhCong.fromValue(lyDoGiaoKhongThanhCong);
            //Loi ben GHN
            if (lyDoGiaoKhongThanhCongEnum == LyDoGiaoKhongThanhCong.SHIPPER_MAT_DON
                    || lyDoGiaoKhongThanhCongEnum == LyDoGiaoKhongThanhCong.HANG_HOA_HU_HONG
                    || lyDoGiaoKhongThanhCongEnum == LyDoGiaoKhongThanhCong.DICH_BENH_HOAC_THIEN_TAI
            ){
                return 1;
            //Loi ben shop
            } else if (lyDoGiaoKhongThanhCongEnum == LyDoGiaoKhongThanhCong.DIA_CHI_KHONG_HOP_LE
                    || lyDoGiaoKhongThanhCongEnum == LyDoGiaoKhongThanhCong.SAI_SAN_PHAM
                    || lyDoGiaoKhongThanhCongEnum == LyDoGiaoKhongThanhCong.KHACH_KHONG_LIEN_LAC_DUOC) {
                return 2;
            //Loi ben KH
            } else if (lyDoGiaoKhongThanhCongEnum == LyDoGiaoKhongThanhCong.KHACH_TU_CHOI_NHAN
            ) {
                return 3;
            }
        } else {
            return 0;
        }

        return 0;
    }

    @Override
    public KetQuaCapNhatTrangThai xuLyCapNhatTrangThaiKhachHang(String maHoaDon, Integer trangThaiMoi, String ghiChu, KhachHang khachHang) {
        HoaDonDTO hoaDonDTO = hoaDonService.getHoaDonByMa(maHoaDon);
        TrangThaiLichSuHoaDon trangThaiHienTai = hoaDonDTO.getTrangThaiLichSuHoaDon();

        if (trangThaiMoi == null) {
            return new KetQuaCapNhatTrangThai(false, "Vui lòng chọn trạng thái mới");
        }

        TrangThaiLichSuHoaDon trangThaiMoiEnum = TrangThaiLichSuHoaDon.fromValue(trangThaiMoi);

        if (trangThaiMoiEnum == trangThaiHienTai) {
            return new KetQuaCapNhatTrangThai(false, "Trạng thái mới không được trùng với trạng thái hiện tại");
        }

        //Cho phép thay đổi các trạng thái tiếp theo
        boolean hopLe = switch (trangThaiHienTai) {
            case CHO_XAC_NHAN -> trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN || trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
            case XAC_NHAN -> {
                if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH
                        && "Chưa thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
                    yield false;
                } else if (hoaDonDTO.getDiaChi() == null || hoaDonDTO.getDiaChi().isEmpty()) {
                    yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH;
                }
                yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.CHO_GIAO_HANG || trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
            }
            case CHO_GIAO_HANG -> trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_GIAO || trangThaiMoiEnum == TrangThaiLichSuHoaDon.GIAO_KHONG_THANH_CONG;
            case DA_GIAO -> {
                if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH && !"Đã thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
                    yield false;
                }
                yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH || trangThaiMoiEnum == TrangThaiLichSuHoaDon.YEU_CAU_HOAN_HANG;
            }
            case YEU_CAU_HOAN_HANG -> trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG;
            case XAC_NHAN_HOAN_HANG -> trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_HOAN;
            case HOAN_THANH, HUY, DA_HOAN, GIAO_KHONG_THANH_CONG,DON_CHUYEN_HOAN -> false;
        };

        if (!hopLe) {
            return new KetQuaCapNhatTrangThai(false, "Trạng thái mới không hợp lệ theo luồng xử lý");
        }

        List<HoaDonChiTietDTO> hdctList = hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(maHoaDon);

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN) {
            for (HoaDonChiTietDTO ct : hdctList) {
                ChiTietSanPham spct = chitietsanphamrepository.findById(ct.getIdCTSP()).orElse(null);
                if (spct == null) {
                    return new KetQuaCapNhatTrangThai(false, "Không tìm thấy sản phẩm có ID: " + ct.getIdCTSP());
                }
                if (spct.getSoLuong() < ct.getSoLuong()) {
                    return new KetQuaCapNhatTrangThai(false, "Sản phẩm \"" + spct.getSanPham().getTen() + "\" không đủ tồn kho!");
                }

                spct.setSoLuong(spct.getSoLuong() - ct.getSoLuong());
                chitietsanphamrepository.save(spct);
            }
        }

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY) {

            //Đổi trạng thái thành chưa thanh toán cho hóa đơn đã Hủy
            HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
            hoaDon.setTrangThai(5);
            hoaDon.setNgaySua(LocalDateTime.now());
            hoaDonRepository.save(hoaDon);

            for (HoaDonChiTietDTO ct : hdctList) {
                ChiTietSanPham spct = chitietsanphamrepository.findById(ct.getIdCTSP()).orElse(null);
                if (spct == null) {
                    return new KetQuaCapNhatTrangThai(false, "Không tìm thấy sản phẩm có ID: " + ct.getIdCTSP());
                }

                // Nếu trước đó đã XÁC NHẬN => cộng lại số lượng
                if (getTrangThaiTruocDo(maHoaDon) == TrangThaiLichSuHoaDon.XAC_NHAN) {
                    spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                    chitietsanphamrepository.save(spct);
                }
            }
        }

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_HOAN) {

            //Đổi trạng thái thành chưa thanh toán cho hóa đơn đã Hủy
            HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
            hoaDon.setTrangThai(4);
            hoaDon.setNgaySua(LocalDateTime.now());
            hoaDonRepository.save(hoaDon);

            for (HoaDonChiTietDTO ct : hdctList) {
                ChiTietSanPham spct = chitietsanphamrepository.findById(ct.getIdCTSP()).orElse(null);
                if (spct == null) {
                    return new KetQuaCapNhatTrangThai(false, "Không tìm thấy sản phẩm có ID: " + ct.getIdCTSP());
                }

                spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                chitietsanphamrepository.save(spct);
            }
        }

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_GIAO) {

            //Đổi trạng thái thành đã thanh toán cho hóa đơn đã giao
            HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
            hoaDon.setTrangThai(3);
            hoaDon.setNgaySua(LocalDateTime.now());
            hoaDonRepository.save(hoaDon);

        }

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.GIAO_KHONG_THANH_CONG) {

            //Đổi trạng thái thành đã thanh toán cho hóa đơn đã giao
            HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
            hoaDon.setTrangThai(2);
            hoaDon.setNgaySua(LocalDateTime.now());
//            hoaDon.setThanhTien(BigDecimal.valueOf(0));
            hoaDonRepository.save(hoaDon);

        }

        capNhatTrangThaiByKhachHang(maHoaDon, trangThaiMoi, ghiChu, khachHang);
        return new KetQuaCapNhatTrangThai(true, "Cập nhật trạng thái thành công");
    }

    @Override
    public void updateStatusAfter3Days() {
        List<LichSuHoaDon> listLshd = lichSuHoaDonRepository.findAll();

        for (LichSuHoaDon lshd : listLshd) {
            if (lshd.getTrangThai() == TrangThaiLichSuHoaDon.DA_GIAO.getValue()) {
                long daysBetween = ChronoUnit.DAYS.between(lshd.getNgayTao(), java.time.LocalDateTime.now());
                if (daysBetween >= 3) {
                    HoaDon hoaDon = lshd.getHoaDon();

                    // Kiểm tra đã có trạng thái HOAN_THANH chưa
                    boolean daHoanThanh = lichSuHoaDonRepository
                            .existsByHoaDonAndTrangThai(hoaDon, TrangThaiLichSuHoaDon.HOAN_THANH.getValue());
                    if (daHoanThanh) {
                        continue; // Bỏ qua nếu đã hoàn thành rồi
                    }

                    // Tạo bản ghi mới trong LichSuHoaDon
                    LichSuHoaDon moi = new LichSuHoaDon();
                    moi.setHoaDon(hoaDon);
                    moi.setTrangThai(TrangThaiLichSuHoaDon.HOAN_THANH.getValue());
                    moi.setNgayTao(java.time.LocalDateTime.now());
                    moi.setNguoiTao(0);
                    moi.setGhiChu("Tự động cập nhật trạng thái");
                    lichSuHoaDonRepository.save(moi);
                }
            }
        }
    }

}