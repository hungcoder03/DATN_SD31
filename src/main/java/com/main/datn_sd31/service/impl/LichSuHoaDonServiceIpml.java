package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonChiTietDTO;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.dto.lich_su_hoa_don_dto.KetQuaCapNhatTrangThai;
import com.main.datn_sd31.dto.lich_su_hoa_don_dto.LichSuHoaDonDTO;
import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.LichSuHoaDon;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.HoaDonRepository;
import com.main.datn_sd31.repository.LichSuHoaDonRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.service.HoaDonChiTietService;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.LichSuHoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
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

        Integer trangThaiGhiNhat = trangThaiMoi;

        String finalGhiChu = (ghiChu != null && !ghiChu.isBlank())
                ? ghiChu
                : "Cập nhật trạng thái: " + TrangThaiLichSuHoaDon.fromValue(trangThaiGhiNhat).getMoTa();

        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .hoaDon(hoaDon)
                .trangThai(trangThaiGhiNhat)
                .ghiChu(finalGhiChu)
                .ngayTao(LocalDateTime.now())
                .nguoiTao(nhanVien.getId())
                .build();

        lichSuHoaDonRepository.save(lichSu);
    }




    @Override
    public List<TrangThaiLichSuHoaDon> getTrangThaiTiepTheoHopLe(TrangThaiLichSuHoaDon hienTai, HoaDonDTO hoaDonDTO) {
        return switch (hienTai) {
            case CHO_XAC_NHAN -> List.of(TrangThaiLichSuHoaDon.XAC_NHAN, TrangThaiLichSuHoaDon.HUY);
            case XAC_NHAN -> {
                if ("Chưa thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
                    yield List.of(TrangThaiLichSuHoaDon.CHO_GIAO_HANG);
                }
                yield List.of(TrangThaiLichSuHoaDon.CHO_GIAO_HANG, TrangThaiLichSuHoaDon.HOAN_THANH, TrangThaiLichSuHoaDon.HUY);
            }
            case CHO_GIAO_HANG -> List.of(TrangThaiLichSuHoaDon.DA_GIAO);
            case DA_GIAO -> {
                if ("Chưa thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
                    yield List.of();
                }
                yield List.of(TrangThaiLichSuHoaDon.HOAN_THANH);
            }
            case YEU_CAU_HOAN_HANG -> List.of(TrangThaiLichSuHoaDon.XAC_NHAN_HOAN_HANG);
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

//    public Boolean flagTrangThaiTruocDo(String maHoaDon){
//        HoaDon hoaDon = hoaDonRepository.findByMaContainingIgnoreCase(maHoaDon).get(0);
//        List<LichSuHoaDon> lichSu = lichSuHoaDonRepository.findLichSuHoaDonsByHoaDonOrderByNgayTaoDesc(hoaDon);
//
//        if (lichSu.size() < 2) return true;
//
//        int trangThaiTruoc = lichSu.get(1).getTrangThai();
//
//        for (int i = 2; i < lichSu.size(); i++) {
//            LichSuHoaDon lichSuHoaDon = lichSu.get(i);
//
//            // BỎ QUA các bản ghi quay lui
////            if (Boolean.TRUE.equals(lichSuHoaDon.getIsRollBack())) continue;
//
//            if (lichSuHoaDon.getTrangThai() == trangThaiTruoc) {
//                return true; // Đã từng có trạng thái này trong lịch sử "thực sự" => vòng lặp
//            }
//        }
//
//        return false;
//    }

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

        //Cho phép thay đổi các trạng thái tiếp theo
        boolean hopLe = switch (trangThaiHienTai) {
            case CHO_XAC_NHAN -> trangThaiMoiEnum == TrangThaiLichSuHoaDon.XAC_NHAN || trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
            case XAC_NHAN -> {
                if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH && "Chưa thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
                    yield false;
                }
                yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.CHO_GIAO_HANG || trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH || trangThaiMoiEnum == TrangThaiLichSuHoaDon.HUY;
            }
            case CHO_GIAO_HANG -> trangThaiMoiEnum == TrangThaiLichSuHoaDon.DA_GIAO;
            case DA_GIAO -> {
                if (trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH && "Chưa thanh toán".equals(hoaDonDTO.getTrangThaiHoaDonString())) {
                    yield false;
                }
                yield trangThaiMoiEnum == TrangThaiLichSuHoaDon.HOAN_THANH;
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

        capNhatTrangThai(maHoaDon, trangThaiMoi, ghiChu, nhanVien);
        return new KetQuaCapNhatTrangThai(true, "Cập nhật trạng thái thành công");
    }

}