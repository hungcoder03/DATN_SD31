-- AI Training Data mẫu
INSERT INTO ai_training_data (question, answer, category, is_active, created_at) VALUES
('Bạn có bán quần áo cho chó không?', 'Có bạn nhé! D&G Fashion chuyên cung cấp đầy đủ các loại quần áo thời trang cho chó như áo thun, áo khoác, váy đầm, và nhiều phụ kiện khác. Tất cả đều được thiết kế đẹp mắt và chất lượng cao! 🐕✨', 'Sản phẩm', 1, GETDATE()),

('Phí giao hàng là bao nhiêu?', 'D&G Fashion có chính sách giao hàng rất ưu đãi: Giao hàng miễn phí cho đơn hàng từ 500k, giao hàng 30k cho đơn hàng dưới 500k. Giao hàng toàn quốc trong 2-5 ngày làm việc! 🚚💨', 'Phí giao hàng', 1, GETDATE()),

('Làm sao để chọn size quần áo phù hợp cho chó?', 'Để chọn size phù hợp, bạn cần đo vòng cổ và chiều dài lưng của chó. D&G Fashion có bảng size chi tiết: Size S (cổ 20-25cm, lưng 20-25cm), Size M (cổ 25-30cm, lưng 25-30cm), Size L (cổ 30-35cm, lưng 30-35cm). Nếu không chắc chắn, hãy liên hệ nhân viên để được tư vấn! 📏🐾', 'Kích thước', 1, GETDATE()),

('Chất liệu quần áo có an toàn cho chó không?', 'Tuyệt đối an toàn bạn nhé! D&G Fashion sử dụng 100% vải cotton tự nhiên, không hóa chất độc hại, mềm mại và thoáng mát. Tất cả sản phẩm đều được kiểm định chất lượng nghiêm ngặt! 🌿✅', 'Chất liệu', 1, GETDATE()),

('Có chính sách đổi trả không?', 'Có bạn nhé! D&G Fashion có chính sách đổi trả trong 3 ngày nếu sản phẩm có lỗi từ nhà sản xuất hoặc không vừa size. Chúng tôi cam kết 100% hài lòng với mọi khách hàng! 🔄💯', 'Chính sách', 1, GETDATE()),

('Bạn có tư vấn chọn quần áo theo giống chó không?', 'Có bạn nhé! D&G Fashion có đội ngũ tư vấn chuyên nghiệp, am hiểu về đặc điểm của từng giống chó. Ví dụ: Chó nhỏ (Poodle, Chihuahua) phù hợp với áo nhẹ nhàng, chó lớn (Golden, Husky) phù hợp với áo ấm áp! 🎯🐕', 'Tư vấn mua hàng', 1, GETDATE()),

('Có khuyến mãi gì không?', 'Có nhiều khuyến mãi hấp dẫn lắm bạn! D&G Fashion thường xuyên có các chương trình giảm giá 10-50%, tặng quà khi mua hàng, và ưu đãi đặc biệt cho khách hàng VIP. Theo dõi fanpage để không bỏ lỡ! 🎉🎁', 'Khuyến mãi', 1, GETDATE()),

('Quần áo có dễ giặt không?', 'Rất dễ giặt bạn nhé! Tất cả sản phẩm của D&G Fashion đều có thể giặt máy ở nhiệt độ 30-40°C, không bị phai màu hay biến dạng. Chỉ cần giặt nhẹ nhàng và phơi khô tự nhiên! 🧺✨', 'Chất liệu', 1, GETDATE()),

('Có ship ra nước ngoài không?', 'Hiện tại D&G Fashion chỉ ship trong nước Việt Nam bạn nhé! Chúng tôi đang phát triển dịch vụ ship quốc tế và sẽ thông báo sớm nhất! 🌍📦', 'Phí giao hàng', 1, GETDATE()),

-- ('Làm sao để trở thành khách hàng VIP?', 'Để trở thành khách hàng VIP của D&G Fashion, bạn cần tích lũy điểm từ các đơn hàng. Mỗi 100k = 1 điểm, tích lũy 100 điểm sẽ được nâng cấp VIP với nhiều ưu đãi đặc biệt! 👑💎', 'Chính sách', 1, GETDATE());