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
    public void capNhatTrangThai(String maHoaDon, Integer trangThaiMoi, String ghiChu, NhanVien nhanVien) {
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
//                .lyDoGiaoKhongThanhCong(lyDoGiaoKhongThanhCong)
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
            case CHO_XAC_NHAN -> List.of(
                    TrangThaiLichSuHoaDon.XAC_NHAN,
                    TrangThaiLichSuHoaDon.HUY);
            case XAC_NHAN -> {
                if (hoaDonDTO.getPhiVanChuyen().compareTo(BigDecimal.ZERO) != 0) {
                    yield List.of(
                            TrangThaiLichSuHoaDon.CHO_GIAO_HANG,
                            TrangThaiLichSuHoaDon.HUY);
                }

                yield List.of(TrangThaiLichSuHoaDon.CHO_GIAO_HANG,
                        TrangThaiLichSuHoaDon.HOAN_THANH,
                        TrangThaiLichSuHoaDon.HUY);
            }
            case CHO_GIAO_HANG -> List.of(
                    TrangThaiLichSuHoaDon.DA_GIAO,
                    TrangThaiLichSuHoaDon.GIAO_KHONG_THANH_CONG);

            case DA_GIAO -> List.of(
                    TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG);

            case GIAO_KHONG_THANH_CONG -> List.of(
                    TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG,
                    TrangThaiLichSuHoaDon.HUY);

            case YEU_CAU_HOAN_HANG -> List.of(
                    TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG,
                    TrangThaiLichSuHoaDon.HOAN_THANH);

            case XAC_NHAN_HOAN_HANG -> List.of(
                    TrangThaiLichSuHoaDon.DA_HOAN);
            default -> List.of(); // HOAN_THANH, DA_HOAN, HUY không được chuyển tiếp
        };
    }

    @Override
    public List<TrangThaiLichSuHoaDon> getTrangThaiTiepTheoHopLeKhachHang(TrangThaiLichSuHoaDon hienTai, HoaDonDTO hoaDonDTO) {
        return switch (hienTai) {
            case CHO_XAC_NHAN -> List.of(
                    TrangThaiLichSuHoaDon.XAC_NHAN,
                    TrangThaiLichSuHoaDon.HUY);
            case XAC_NHAN -> {
                if (hoaDonDTO.getPhiVanChuyen().compareTo(BigDecimal.ZERO) == 0) {
                    yield List.of(
                            TrangThaiLichSuHoaDon.CHO_GIAO_HANG,
                            TrangThaiLichSuHoaDon.HUY);
                }

                yield List.of(
                        TrangThaiLichSuHoaDon.CHO_GIAO_HANG,
                        TrangThaiLichSuHoaDon.HOAN_THANH,
                        TrangThaiLichSuHoaDon.HUY);
            }
            case CHO_GIAO_HANG ->List.of(
                    TrangThaiLichSuHoaDon.DA_GIAO,
                    TrangThaiLichSuHoaDon.GIAO_KHONG_THANH_CONG);

            case DA_GIAO -> List.of(TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG);

            case GIAO_KHONG_THANH_CONG -> List.of(
                    TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG,
                    TrangThaiLichSuHoaDon.HUY);

            case YEU_CAU_HOAN_HANG -> List.of(
                    TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG,
                    TrangThaiLichSuHoaDon.HOAN_THANH);

            case XAC_NHAN_HOAN_HANG -> List.of(TrangThaiLichSuHoaDon.DA_HOAN);
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
//        dto.setLyDoGiaoKhongThanhCong(lichSuHoaDon.getLyDoGiaoKhongThanhCong());

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
    public KetQuaCapNhatTrangThai xuLyCapNhatTrangThai(String maHoaDon, Integer trangThaiMoi, String ghiChu, NhanVien nhanVien) {
        HoaDonDTO hoaDonDTO = hoaDonService.getHoaDonByMa(maHoaDon);
        TrangThaiLichSuHoaDon trangThaiHienTai = hoaDonDTO.getTrangThaiLichSuHoaDon();

        if (trangThaiMoi == null) {
            return new KetQuaCapNhatTrangThai(false, "Vui lòng chọn trạng thái mới");
        }

        TrangThaiLichSuHoaDon trangThaiMoiEnum = TrangThaiLichSuHoaDon.fromValue(trangThaiMoi);

        if (trangThaiMoiEnum == trangThaiHienTai) {
            return new KetQuaCapNhatTrangThai(false, "Trạng thái mới không được trùng với trạng thái hiện tại");
        }

        String message = null;
        boolean hopLe = switch (trangThaiHienTai) {
            case CHO_XAC_NHAN -> {
                if (hoaDonDTO.getTrangThaiHoaDonInteger() == 1) {
                    message = "Hóa đơn đang chờ thanh toán, không thể chuyển về trạng thái này";
                    yield false;
                }
                yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN ||
                        trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
            }
            case XAC_NHAN -> {
                if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH
                        && "Chưa thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
                    message = "Không thể hoàn thành khi chưa thanh toán";
                    yield false;
                } else if (hoaDonDTO.getPhiVanChuyen().equals(BigDecimal.ZERO)) {
                    message = "Không thể hoàn thành vì chưa có địa chỉ giao hàng";
                    yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
                }
                yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.CHO_GIAO_HANG ||
                        trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
            }
            case DA_GIAO -> {
                if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH &&
                        !"Đã thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
                    message = "Không thể hoàn thành vì hóa đơn chưa được thanh toán";
                    yield false;
                }
                yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH ||
                        trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG;
            }
            case CHO_GIAO_HANG ->
                    trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_GIAO ||
                            trangThaiMoiEnum == TrangThaiLichSuHoaDon.GIAO_KHONG_THANH_CONG;
            case GIAO_KHONG_THANH_CONG -> {
                message = "Đơn giao không thành công, chỉ có thể hủy hoặc xác nhận hoàn hàng";
                yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY ||
                        trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG;
            }
            case YEU_CAU_HOAN_HANG ->
                    trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG ||
                            trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH;
            case XAC_NHAN_HOAN_HANG ->
                    trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_HOAN;
            case HOAN_THANH, HUY, DA_HOAN -> {
                message = "Đơn hàng đã kết thúc, không thể thay đổi trạng thái nữa";
                yield false;
            }
        };

        if (!hopLe) {
            return new KetQuaCapNhatTrangThai(false,
                    message != null ? message : "Trạng thái mới không hợp lệ theo luồng xử lý");
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
//                    if (xuLyDonHangGiaoKhongThanhCong(lyDoGiaoKhongThanhCong) == 2 || xuLyDonHangGiaoKhongThanhCong(lyDoGiaoKhongThanhCong) == 0) {
                        spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                        chitietsanphamrepository.save(spct);
//                    }
                }

                // Nếu trước đó đã GIAO_KHONG_THANH_CONG => K cong so luong
//                if (getTrangThaiTruocDo(maHoaDon) == TrangThaiLichSuHoaDon.GIAO_KHONG_THANH_CONG) {
////                    if (xuLyDonHangGiaoKhongThanhCong(lyDoGiaoKhongThanhCong) == 2 || xuLyDonHangGiaoKhongThanhCong(lyDoGiaoKhongThanhCong) == 0) {
//                    spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
//                    chitietsanphamrepository.save(spct);
////                    }
//                }
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
            hoaDon.setTrangThai(5);
            hoaDon.setNgaySua(LocalDateTime.now());
//            hoaDon.setThanhTien(BigDecimal.valueOf(0));
            hoaDonRepository.save(hoaDon);

//            System.out.println("cap nhat hoa don");

            for (HoaDonChiTietDTO ct : hdctList) {
//                System.out.println("Xem list sp");
                ChiTietSanPham spct = chitietsanphamrepository.findById(ct.getIdCTSP()).orElse(null);
//                System.out.println("Da tim thay sp");
                if (spct == null) {
//                    System.out.println("K thay sp");
                    return new KetQuaCapNhatTrangThai(false, "Không tìm thấy sản phẩm có ID: " + ct.getIdCTSP());
                }

                spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
//                System.out.println("tru sl");
                chitietsanphamrepository.save(spct);
//                System.out.println("da tru");
            }

        }

//        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.DON_CHUYEN_HOAN) {
//
//            //Đổi trạng thái thành đã thanh toán cho hóa đơn đã giao
//            HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
//            hoaDon.setTrangThai(2);
//            hoaDon.setNgaySua(LocalDateTime.now());
////            hoaDon.setThanhTien(BigDecimal.valueOf(0));
//            hoaDonRepository.save(hoaDon);
//
//        }

        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY &&
                HoaDonUtils.choPhepHuyDonKhachHang(trangThaiMoiEnum)) {
            return new KetQuaCapNhatTrangThai(false, "Đơn hàng không thể huỷ ở trạng thái hiện tại.");
        }

//        if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_HOAN &&
//                !HoaDonUtils.choPhepHoanHangKhachHang(trangThaiMoiEnum)) {
//            return new KetQuaCapNhatTrangThai(false, "Đơn hàng chưa được giao nên không thể hoàn.");
//        }

        capNhatTrangThai(maHoaDon, trangThaiMoi, ghiChu, nhanVien);
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
            case HOAN_THANH, HUY, DA_HOAN, GIAO_KHONG_THANH_CONG -> false;
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
            hoaDon.setTrangThai(5);
            hoaDon.setNgaySua(LocalDateTime.now());
//            hoaDon.setThanhTien(BigDecimal.valueOf(0));
            hoaDonRepository.save(hoaDon);


        }

        capNhatTrangThaiByKhachHang(maHoaDon, trangThaiMoi, ghiChu, khachHang);
        return new KetQuaCapNhatTrangThai(true, "Cập nhật trạng thái thành công");
    }

    // Hoàn thiện method cho Admin
//    @Override
//    public KetQuaCapNhatTrangThai xuLyCapNhatTrangThai(String maHoaDon, Integer trangThaiMoi, String ghiChu, NhanVien nhanVien) {
//        try {
//            HoaDonDTO hoaDonDTO = hoaDonService.getHoaDonByMa(maHoaDon);
//            TrangThaiLichSuHoaDon trangThaiHienTai = hoaDonDTO.getTrangThaiLichSuHoaDon();
//
//            // Kiểm tra tham số đầu vào
//            if (trangThaiMoi == null) {
//                return new KetQuaCapNhatTrangThai(false, "Vui lòng chọn trạng thái mới");
//            }
//
//            TrangThaiLichSuHoaDon trangThaiMoiEnum = TrangThaiLichSuHoaDon.fromValue(trangThaiMoi);
//            if (trangThaiMoiEnum == null) {
//                return new KetQuaCapNhatTrangThai(false, "Trạng thái không hợp lệ");
//            }
//
//            if (trangThaiMoiEnum == trangThaiHienTai) {
//                return new KetQuaCapNhatTrangThai(false, "Trạng thái mới không được trùng với trạng thái hiện tại");
//            }
//
//            // Kiểm tra quy tắc chuyển đổi trạng thái
//            String message = null;
//            boolean hopLe = switch (trangThaiHienTai) {
//                case CHO_XAC_NHAN -> {
//                    if (hoaDonDTO.getTrangThaiHoaDonInteger() == 1) {
//                        message = "Hóa đơn đang chờ thanh toán, không thể chuyển về trạng thái này";
//                        yield false;
//                    }
//                    yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN ||
//                            trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
//                }
//                case XAC_NHAN -> {
//                    if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH
//                            && "Chưa thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
//                        message = "Không thể hoàn thành khi chưa thanh toán";
//                        yield false;
//                    }
//                    if (hoaDonDTO.getPhiVanChuyen().equals(BigDecimal.ZERO)) {
//                        // Đơn hàng tại cửa hàng (không có phí vận chuyển)
//                        yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH ||
//                                trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
//                    }
//                    // Đơn hàng online
//                    yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.CHO_GIAO_HANG ||
//                            trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
//                }
//                case CHO_GIAO_HANG ->
//                        trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_GIAO ||
//                                trangThaiMoiEnum == TrangThaiLichSuHoaDon.GIAO_KHONG_THANH_CONG;
//
//                case DA_GIAO -> {
//                    if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH &&
//                            !"Đã thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
//                        message = "Không thể hoàn thành vì hóa đơn chưa được thanh toán";
//                        yield false;
//                    }
//                    yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH ||
//                            trangThaiMoiEnum == TrangThaiLichSuHoaDon.YEU_CAU_HOAN_HANG;
//                }
//                case GIAO_KHONG_THANH_CONG -> {
//                    message = "Đơn giao không thành công, chỉ có thể hủy hoặc xác nhận hoàn hàng";
//                    yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY ||
//                            trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG;
//                }
//                case YEU_CAU_HOAN_HANG ->
//                        trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG ||
//                                trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH;
//
//                case XAC_NHAN_HOAN_HANG ->
//                        trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_HOAN;
//
//                case HOAN_THANH, HUY, DA_HOAN -> {
//                    message = "Đơn hàng đã kết thúc, không thể thay đổi trạng thái nữa";
//                    yield false;
//                }
//            };
//
//            if (!hopLe) {
//                return new KetQuaCapNhatTrangThai(false,
//                        message != null ? message : "Trạng thái mới không hợp lệ theo luồng xử lý");
//            }
//
//            // Xử lý logic nghiệp vụ theo từng trạng thái
//            return xuLyLogicNghiepVu(maHoaDon, trangThaiMoiEnum, ghiChu, nhanVien, hoaDonDTO);
//
//        } catch (Exception e) {
//            return new KetQuaCapNhatTrangThai(false, "Có lỗi xảy ra: " + e.getMessage());
//        }
//    }
//
//    // Hoàn thiện method cho Khách hàng
//    @Override
//    public KetQuaCapNhatTrangThai xuLyCapNhatTrangThaiKhachHang(String maHoaDon, Integer trangThaiMoi, String ghiChu, KhachHang khachHang) {
//        try {
//            HoaDonDTO hoaDonDTO = hoaDonService.getHoaDonByMa(maHoaDon);
//            TrangThaiLichSuHoaDon trangThaiHienTai = hoaDonDTO.getTrangThaiLichSuHoaDon();
//
//            // Kiểm tra tham số đầu vào
//            if (trangThaiMoi == null) {
//                return new KetQuaCapNhatTrangThai(false, "Vui lòng chọn trạng thái mới");
//            }
//
//            TrangThaiLichSuHoaDon trangThaiMoiEnum = TrangThaiLichSuHoaDon.fromValue(trangThaiMoi);
//            if (trangThaiMoiEnum == null) {
//                return new KetQuaCapNhatTrangThai(false, "Trạng thái không hợp lệ");
//            }
//
//            if (trangThaiMoiEnum == trangThaiHienTai) {
//                return new KetQuaCapNhatTrangThai(false, "Trạng thái mới không được trùng với trạng thái hiện tại");
//            }
//
//            // Kiểm tra quyền của khách hàng (chỉ được thực hiện một số hành động nhất định)
//            String message = null;
//            boolean hopLe = switch (trangThaiHienTai) {
//                case CHO_XAC_NHAN ->
//                        trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY; // Khách hàng chỉ được hủy
//
//                case XAC_NHAN -> {
//                    if (hoaDonDTO.getPhiVanChuyen().equals(BigDecimal.ZERO)) {
//                        // Đơn tại cửa hàng - khách hàng không được thay đổi
//                        message = "Đơn hàng tại cửa hàng không thể thay đổi trạng thái";
//                        yield false;
//                    }
//                    // Đơn online - cho phép hủy trong thời gian nhất định
//                    yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
//                }
//                case CHO_GIAO_HANG -> {
//                    message = "Đơn hàng đang được giao, không thể thay đổi trạng thái";
//                    yield false;
//                }
//                case DA_GIAO -> {
//                    // Khách hàng có thể xác nhận hoàn thành hoặc yêu cầu hoàn hàng
//                    if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH &&
//                            !"Đã thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
//                        message = "Không thể hoàn thành vì hóa đơn chưa được thanh toán";
//                        yield false;
//                    }
//                    yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH ||
//                            trangThaiMoiEnum == TrangThaiLichSuHoaDon.YEU_CAU_HOAN_HANG;
//                }
//                case GIAO_KHONG_THANH_CONG -> {
//                    message = "Đơn giao không thành công, vui lòng liên hệ cửa hàng";
//                    yield false;
//                }
//                case YEU_CAU_HOAN_HANG -> {
//                    message = "Đã gửi yêu cầu hoàn hàng, vui lòng chờ xử lý";
//                    yield false;
//                }
//                case XAC_NHAN_HOAN_HANG, HOAN_THANH, HUY, DA_HOAN -> {
//                    message = "Đơn hàng đã kết thúc, không thể thay đổi trạng thái";
//                    yield false;
//                }
//            };
//
//            if (!hopLe) {
//                return new KetQuaCapNhatTrangThai(false,
//                        message != null ? message : "Bạn không có quyền thực hiện hành động này");
//            }
//
//            // Kiểm tra thời gian cho phép hủy đơn (ví dụ: chỉ cho phép hủy trong 30 phút sau khi đặt)
//            if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY) {
//                LocalDateTime ngayTao = hoaDonDTO.getNgayTao();
//                long minutesDiff = ChronoUnit.MINUTES.between(ngayTao, LocalDateTime.now());
//                if (minutesDiff > 30) { // Giới hạn 30 phút
//                    return new KetQuaCapNhatTrangThai(false, "Đã quá thời gian cho phép hủy đơn hàng (30 phút)");
//                }
//            }
//
//            // Xử lý logic nghiệp vụ
//            return xuLyLogicNghiepVuKhachHang(maHoaDon, trangThaiMoiEnum, ghiChu, khachHang, hoaDonDTO);
//
//        } catch (Exception e) {
//            return new KetQuaCapNhatTrangThai(false, "Có lỗi xảy ra: " + e.getMessage());
//        }
//    }
//
//    // Method hỗ trợ xử lý logic nghiệp vụ cho Admin
//    private KetQuaCapNhatTrangThai xuLyLogicNghiepVu(String maHoaDon, TrangThaiLichSuHoaDon trangThaiMoi,
//                                                     String ghiChu, NhanVien nhanVien, HoaDonDTO hoaDonDTO) {
//        List<HoaDonChiTietDTO> hdctList = hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(maHoaDon);
//
//        switch (trangThaiMoi) {
//            case XAC_NHAN -> {
//                // Kiểm tra và trừ số lượng tồn kho
//                for (HoaDonChiTietDTO ct : hdctList) {
//                    ChiTietSanPham spct = chitietsanphamrepository.findById(ct.getIdCTSP()).orElse(null);
//                    if (spct == null) {
//                        return new KetQuaCapNhatTrangThai(false, "Không tìm thấy sản phẩm có ID: " + ct.getIdCTSP());
//                    }
//                    if (spct.getSoLuong() < ct.getSoLuong()) {
//                        return new KetQuaCapNhatTrangThai(false, "Sản phẩm \"" + spct.getSanPham().getTen() + "\" không đủ tồn kho!");
//                    }
//                    spct.setSoLuong(spct.getSoLuong() - ct.getSoLuong());
//                    chitietsanphamrepository.save(spct);
//                }
//            }
//            case HUY -> {
//                // Cập nhật trạng thái hóa đơn và hoàn lại số lượng tồn kho nếu cần
//                HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
//                hoaDon.setTrangThai(5); // Trạng thái hủy
//                hoaDon.setNgaySua(LocalDateTime.now());
//                hoaDonRepository.save(hoaDon);
//
//                // Hoàn lại tồn kho nếu đã xác nhận trước đó
//                TrangThaiLichSuHoaDon trangThaiTruoc = getTrangThaiTruocDo(maHoaDon);
//                if (trangThaiTruoc == TrangThaiLichSuHoaDon.XAC_NHAN) {
//                    for (HoaDonChiTietDTO ct : hdctList) {
//                        ChiTietSanPham spct = chitietsanphamrepository.findById(ct.getIdCTSP()).orElse(null);
//                        if (spct != null) {
//                            spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
//                            chitietsanphamrepository.save(spct);
//                        }
//                    }
//                }
//            }
//            case DA_HOAN -> {
//                // Cập nhật trạng thái hóa đơn và hoàn lại số lượng
//                HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
//                hoaDon.setTrangThai(4); // Trạng thái hoàn hàng
//                hoaDon.setNgaySua(LocalDateTime.now());
//                hoaDonRepository.save(hoaDon);
//
//                for (HoaDonChiTietDTO ct : hdctList) {
//                    ChiTietSanPham spct = chitietsanphamrepository.findById(ct.getIdCTSP()).orElse(null);
//                    if (spct != null) {
//                        spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
//                        chitietsanphamrepository.save(spct);
//                    }
//                }
//            }
//            case DA_GIAO -> {
//                // Cập nhật trạng thái thành đã thanh toán
//                HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
//                hoaDon.setTrangThai(3); // Đã thanh toán
//                hoaDon.setNgaySua(LocalDateTime.now());
//                hoaDon.setNgayThanhToan(LocalDateTime.now());
//                hoaDonRepository.save(hoaDon);
//            }
//            case GIAO_KHONG_THANH_CONG -> {
//                // Cập nhật trạng thái và hoàn lại số lượng
//                HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
//                hoaDon.setTrangThai(5); // Hủy
//                hoaDon.setNgaySua(LocalDateTime.now());
//                hoaDonRepository.save(hoaDon);
//
//                for (HoaDonChiTietDTO ct : hdctList) {
//                    ChiTietSanPham spct = chitietsanphamrepository.findById(ct.getIdCTSP()).orElse(null);
//                    if (spct != null) {
//                        spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
//                        chitietsanphamrepository.save(spct);
//                    }
//                }
//            }
//            case HOAN_THANH -> {
//                // Không cần xử lý đặc biệt, chỉ lưu lịch sử
//            }
//            default -> {
//                // Các trạng thái khác không cần xử lý đặc biệt
//            }
//        }
//
//        // Lưu lịch sử thay đổi
//        capNhatTrangThai(maHoaDon, trangThaiMoi.getValue(), ghiChu, nhanVien);
//        return new KetQuaCapNhatTrangThai(true, "Cập nhật trạng thái thành công");
//    }
//
//    // Method hỗ trợ xử lý logic nghiệp vụ cho Khách hàng
//    private KetQuaCapNhatTrangThai xuLyLogicNghiepVuKhachHang(String maHoaDon, TrangThaiLichSuHoaDon trangThaiMoi,
//                                                              String ghiChu, KhachHang khachHang, HoaDonDTO hoaDonDTO) {
//        List<HoaDonChiTietDTO> hdctList = hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(maHoaDon);
//
//        switch (trangThaiMoi) {
//            case HUY -> {
//                // Khách hàng hủy đơn - cập nhật trạng thái và hoàn tồn kho nếu cần
//                HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(maHoaDon);
//                hoaDon.setTrangThai(5);
//                hoaDon.setNgaySua(LocalDateTime.now());
//                hoaDonRepository.save(hoaDon);
//
//                // Nếu đã xác nhận thì hoàn lại tồn kho
//                TrangThaiLichSuHoaDon trangThaiTruoc = getTrangThaiTruocDo(maHoaDon);
//                if (trangThaiTruoc == TrangThaiLichSuHoaDon.XAC_NHAN) {
//                    for (HoaDonChiTietDTO ct : hdctList) {
//                        ChiTietSanPham spct = chitietsanphamrepository.findById(ct.getIdCTSP()).orElse(null);
//                        if (spct != null) {
//                            spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
//                            chitietsanphamrepository.save(spct);
//                        }
//                    }
//                }
//            }
//            case YEU_CAU_HOAN_HANG -> {
//                // Khách hàng yêu cầu hoàn hàng - chỉ lưu lịch sử
//            }
//            case YEU_CAU_TRA_HANG_1_PHAN -> {
//                // Khách hàng yêu cầu trả hàng 1 phần - chỉ lưu lịch sử
//            }
//            case HOAN_THANH -> {
//                // Khách hàng xác nhận hoàn thành - không cần xử lý đặc biệt
//            }
//            default -> {
//                return new KetQuaCapNhatTrangThai(false, "Hành động không được phép");
//            }
//        }
//
//        // Lưu lịch sử với người tạo là khách hàng
//        capNhatTrangThaiByKhachHang(maHoaDon, trangThaiMoi.getValue(), ghiChu, khachHang);
//        return new KetQuaCapNhatTrangThai(true, "Cập nhật trạng thái thành công");
//    }

    @Override
    public void updateStatusAfter3Days() {

        List<LichSuHoaDon> listLshd = lichSuHoaDonRepository.findAll();

        for (LichSuHoaDon lshd : listLshd) {
//            System.out.println("1");
            if (lshd.getTrangThai() == TrangThaiLichSuHoaDon.DA_GIAO.getValue()) {
//                System.out.println("Tìm thấy Da_Giao");
                long daysBetween = ChronoUnit.DAYS.between(lshd.getNgayTao(), java.time.LocalDateTime.now());
                if (daysBetween >= 3) {
//                    System.out.println("ngayTao >= 3");
                    HoaDon hoaDon = lshd.getHoaDon();

                    // Kiểm tra đã có trạng thái HOAN_THANH chưa
                    boolean daHoanThanh = lichSuHoaDonRepository
                            .existsByHoaDonAndTrangThai(hoaDon, TrangThaiLichSuHoaDon.HOAN_THANH.getValue());
                    if (daHoanThanh) {
//                        System.out.println("Bo qua");
                        continue; //Bỏ qua nếu đã hoàn thành rồi

                    }

                    // Tạo bản ghi mới trong LichSuHoaDon
                    LichSuHoaDon moi = new LichSuHoaDon();
                    moi.setHoaDon(hoaDon);
                    moi.setTrangThai(TrangThaiLichSuHoaDon.HOAN_THANH.getValue());
                    moi.setNgayTao(java.time.LocalDateTime.now());
                    moi.setNguoiTao(0);
                    moi.setGhiChu("Tự động cập nhật trạng thái");
                    lichSuHoaDonRepository.save(moi);
                    System.out.println("Tự động cập nhật trạng thái Đã giao > Hoàn thành");
                }
            }
        }
    }

}