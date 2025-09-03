-- Dữ liệu mẫu cho hệ thống đa ngôn ngữ
-- Insert ngôn ngữ
INSERT INTO ngon_ngu (ma_ngon_ngu, ten_ngon_ngu, ten_ban_dia, duong_dan_co, trang_thai, mac_dinh, thu_tu) VALUES
('vi', N'Tiếng Việt', N'Tiếng Việt', '/client-static/images/flags/vi.png', 1, 1, 1),
('en', N'English', N'English', '/client-static/images/flags/en.png', 1, 0, 2),
('ja', N'日本語', N'日本語', '/client-static/images/flags/ja.png', 1, 0, 3),
('ko', N'한국어', N'한국어', '/client-static/images/flags/ko.png', 1, 0, 4),
('zh', N'中文', N'中文', '/client-static/images/flags/zh.png', 1, 0, 5);

-- Insert bản dịch cơ bản
INSERT INTO ban_dich (ten_khoa, ma_ngon_ngu, noi_dung, danh_muc) VALUES
-- Navigation
('nav.home', 'vi', N'Trang chủ', 'navigation'),
('nav.home', 'en', 'Home', 'navigation'),
('nav.home', 'ja', 'ホーム', 'navigation'),
('nav.home', 'ko', '홈', 'navigation'),
('nav.home', 'zh', '首页', 'navigation'),

('nav.products', 'vi', N'Sản phẩm', 'navigation'),
('nav.products', 'en', 'Products', 'navigation'),
('nav.products', 'ja', '商品', 'navigation'),
('nav.products', 'ko', '제품', 'navigation'),
('nav.products', 'zh', '产品', 'navigation'),

('nav.cart', 'vi', N'Giỏ hàng', 'navigation'),
('nav.cart', 'en', 'Cart', 'navigation'),
('nav.cart', 'ja', 'カート', 'navigation'),
('nav.cart', 'ko', '장바구니', 'navigation'),
('nav.cart', 'zh', '购物车', 'navigation'),

-- Home page
('home.banner.title', 'vi', N'Điểm đến tốt nhất cho thú cưng của bạn', 'home'),
('home.banner.title', 'en', 'Best destination for your pets', 'home'),
('home.banner.title', 'ja', 'あなたのペットのための最高の目的地', 'home'),
('home.banner.title', 'ko', '당신의 애완동물을 위한 최고의 목적지', 'home'),
('home.banner.title', 'zh', '您宠物的最佳目的地', 'home'),

('home.banner.subtitle', 'vi', N'Tiết kiệm 10-20%', 'home'),
('home.banner.subtitle', 'en', 'Save 10-20% off', 'home'),
('home.banner.subtitle', 'ja', '10-20%オフ', 'home'),
('home.banner.subtitle', 'ko', '10-20% 할인', 'home'),
('home.banner.subtitle', 'zh', '节省10-20%', 'home'),

-- Product
('product.add_to_cart', 'vi', N'Thêm vào giỏ hàng', 'product'),
('product.add_to_cart', 'en', 'Add to Cart', 'product'),
('product.add_to_cart', 'ja', 'カートに追加', 'product'),
('product.add_to_cart', 'ko', '장바구니에 추가', 'product'),
('product.add_to_cart', 'zh', '添加到购物车', 'product'),

-- Common
('common.search', 'vi', N'Tìm kiếm', 'common'),
('common.search', 'en', 'Search', 'common'),
('common.search', 'ja', '検索', 'common'),
('common.search', 'ko', '검색', 'common'),
('common.search', 'zh', '搜索', 'common'),

('common.loading', 'vi', N'Đang tải...', 'common'),
('common.loading', 'en', 'Loading...', 'common'),
('common.loading', 'ja', '読み込み中...', 'common'),
('common.loading', 'ko', '로딩 중...', 'common'),
('common.loading', 'zh', '加载中...', 'common'),

('common.language', 'vi', N'Ngôn ngữ', 'common'),
('common.language', 'en', 'Language', 'common'),
('common.language', 'ja', '言語', 'common'),
('common.language', 'ko', '언어', 'common'),
('common.language', 'zh', '语言', 'common');
