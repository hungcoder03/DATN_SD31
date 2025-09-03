-- KIỂM TRA TÍNH TOÀN VẸN HỆ THỐNG ĐA NGÔN NGỮ
-- Chạy các câu lệnh này để kiểm tra hệ thống

-- 1. Kiểm tra bảng ngôn ngữ
SELECT * FROM ngon_ngu ORDER BY thu_tu;

-- 2. Kiểm tra bảng bản dịch
SELECT * FROM ban_dich ORDER BY ma_ngon_ngu, ten_khoa;

-- 3. Kiểm tra bảng cache AI
SELECT * FROM cache_ban_dich_ai ORDER BY ngay_tao DESC;

-- 4. Kiểm tra ngôn ngữ mặc định
SELECT * FROM ngon_ngu WHERE mac_dinh = 1;

-- 5. Kiểm tra ngôn ngữ hoạt động
SELECT * FROM ngon_ngu WHERE trang_thai = 1 ORDER BY thu_tu;

-- 6. Kiểm tra bản dịch theo ngôn ngữ
SELECT b.ten_khoa, b.noi_dung, n.ten_ngon_ngu 
FROM ban_dich b 
JOIN ngon_ngu n ON b.ma_ngon_ngu = n.ma_ngon_ngu 
WHERE n.ma_ngon_ngu = 'vi'
ORDER BY b.ten_khoa;

-- 7. Kiểm tra bản dịch AI
SELECT * FROM ban_dich WHERE duoc_tao_boi_ai = 1;

-- 8. Thống kê cache AI
SELECT 
    ngon_ngu_nguon,
    ngon_ngu_dich,
    COUNT(*) as so_lan_dich,
    AVG(diem_tin_cay) as diem_tin_cay_trung_binh
FROM cache_ban_dich_ai 
GROUP BY ngon_ngu_nguon, ngon_ngu_dich
ORDER BY so_lan_dich DESC;
