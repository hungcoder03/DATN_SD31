package com.main.datn_sd31.component;

import com.main.datn_sd31.service.LichSuHoaDonService;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TuDongCapNhatTrangThaiHoaDon {

    private final LichSuHoaDonService lichSuHoaDonService;

    private final PhieuGiamGiaService phieuGiamGiaService;

//    private final LichSuHoaDonRepository lichSuHoaDonRepository;

//    private final HoaDonService hoaDonService;

//    private final HoaDonRepository hoaDonRepository;

//    @Scheduled(cron = "0 0 2 * * ?") // chạy lúc 2 giờ sáng mỗi ngày
//    @Transactional
    @Scheduled(fixedRate = 100000) // 100 giây
    public void updateStatusAfter3Days() {
        lichSuHoaDonService.updateStatusAfter3Days();
        phieuGiamGiaService.autoUpdateStatus();
    }

}
