-- ========================================================
-- SCRIPT MIGRATION HƯỚNG 3: ĐIỆN MÁY & THIẾT BỊ GIA ĐÌNH THÔNG MINH
-- CÁCH 2: DYNAMIC PRODUCT VARIANTS & ATTRIBUTES + MULTI-IMAGES
-- ========================================================

USE ECommerceDB;
GO

-- 1. TẠO BẢNG PRODUCT_VARIANTS NẾU CHƯA CÓ
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'product_variants')
BEGIN
    CREATE TABLE product_variants (
        variantid INT IDENTITY(1,1) PRIMARY KEY,
        productid INT NOT NULL,
        sku VARCHAR(50),
        variant_name NVARCHAR(100) NOT NULL,
        attribute_size NVARCHAR(50),
        attribute_color NVARCHAR(50),
        color_hex VARCHAR(20),
        price NUMERIC(18,2) NOT NULL,
        stock INT NOT NULL DEFAULT 0,
        image_url NVARCHAR(500),
        is_default BIT DEFAULT 0,
        CONSTRAINT FK_Variant_Product FOREIGN KEY (productid) REFERENCES products(productid) ON DELETE CASCADE
    );
END
GO

-- 2. TẠO BẢNG PRODUCT_IMAGES NẾU CHƯA CÓ
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'product_images')
BEGIN
    CREATE TABLE product_images (
        imageid INT IDENTITY(1,1) PRIMARY KEY,
        productid INT NOT NULL,
        image_url NVARCHAR(500) NOT NULL,
        display_order INT DEFAULT 0,
        is_primary BIT DEFAULT 0,
        CONSTRAINT FK_Image_Product FOREIGN KEY (productid) REFERENCES products(productid) ON DELETE CASCADE
    );
END
GO

-- 3. BỔ SUNG CỘT CHO BẢNG ORDER_DETAILS NẾU CHƯA CÓ
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('order_details') AND name = 'variantid')
BEGIN
    ALTER TABLE order_details ADD variantid INT NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('order_details') AND name = 'variant_info')
BEGIN
    ALTER TABLE order_details ADD variant_info NVARCHAR(100) NULL;
END
GO

-- 4. XÓA DỮ LIỆU CŨ TỪ CÁC BẢNG PHỤ THUỘC ĐỂ ĐẢM BẢO INTEGRITY
DELETE FROM product_reviews;
DELETE FROM wishlists;
DELETE FROM payments;
DELETE FROM order_details;
DELETE FROM orders;
DELETE FROM product_variants;
DELETE FROM product_images;
DELETE FROM products;
DELETE FROM categories;
GO

-- Reset Auto Identity cho các bảng
DBCC CHECKIDENT ('categories', RESEED, 0);
DBCC CHECKIDENT ('products', RESEED, 0);
DBCC CHECKIDENT ('product_variants', RESEED, 0);
DBCC CHECKIDENT ('product_images', RESEED, 0);
GO

-- 5. THÊM 5 DANH MỤC HƯỚNG 3
SET IDENTITY_INSERT categories ON;
INSERT INTO categories (categoryid, category_name) VALUES
(1, N'Tivi & Thiết bị nghe nhìn'),
(2, N'Tủ lạnh & Tủ đông'),
(3, N'Máy giặt & Máy sấy'),
(4, N'Điều hòa & Làm mát'),
(5, N'Robot & Thiết bị thông minh');
SET IDENTITY_INSERT categories OFF;
GO

-- 6. THÊM 15 SẢN PHẨM HƯỚNG 3
SET IDENTITY_INSERT products ON;
INSERT INTO products (productid, categoryid, name, description, price, stock, imageurl, created_date, updated_date) VALUES
(1, 1, N'Smart Tivi Samsung Crystal UHD 4K 2024',
 N'Độ phân giải 4K sắc nét gấp 4 lần Full HD, công nghệ Dynamic Crystal Color tái tạo 1 tỷ sắc màu thuần khiết, bộ xử lý Crystal 4K nâng cấp hình ảnh thông minh. Hệ điều hành Tizen OS giao diện mượt mà, hỗ trợ trợ lý ảo giọng nói tiếng Việt Bixby.',
 8490000, 45, '/uploads/products/tv-samsung-crystal.svg', GETDATE(), GETDATE()),

(2, 1, N'Google Tivi Sony 4K Bravia X75K',
 N'Bộ xử lý 4K X-Reality PRO nâng chuẩn mọi nội dung, chuyển động mượt mà Motionflow XR 200, âm thanh Dolby Audio sống động. Tích hợp Google TV với kho ứng dụng phong phú YouTube, Netflix, VieON cùng remote tìm kiếm giọng nói rảnh tay.',
 10990000, 35, '/uploads/products/tv-sony-bravia.svg', GETDATE(), GETDATE()),

(3, 1, N'Loa Thanh Soundbar Samsung Chuẩn 3.1.2 Kênh',
 N'Công nghệ Dolby Atmos không dây đầu tiên trên thế giới, âm thanh vòm đỉnh cao Q-Symphony đồng bộ hoàn hảo với loa TV Samsung, công suất 360W bùng nổ cùng loa trầm Subwoofer mạnh mẽ cho trải nghiệm rạp chiếu phim tại gia.',
 4890000, 30, '/uploads/products/soundbar-samsung.svg', GETDATE(), GETDATE()),

(4, 2, N'Tủ Lạnh Side By Side Inverter LG 519 Lít',
 N'Dung tích cực lớn 519L cho gia đình từ 5-7 thành viên, công nghệ Smart Inverter siêu tiết kiệm điện năng vận hành êm ái, hệ thống làm lạnh đa chiều Multi Air Flow bảo quản thực phẩm luôn tươi ngon đồng đều.',
 13990000, 25, '/uploads/products/fridge-lg-sidebyside.svg', GETDATE(), GETDATE()),

(5, 2, N'Tủ Lạnh Inverter Panasonic 322 Lít Kháng Khuẩn Ag Clean',
 N'Ngăn đông mềm thế hệ mới Prime Fresh+ chuẩn -3°C nấu ngay không cần rã đông, công nghệ Inverter kết hợp cảm biến Econavi tiết kiệm 40% điện năng, công nghệ kháng khuẩn Ag Clean tiêu diệt 99.9% vi khuẩn và mùi hôi.',
 9790000, 20, '/uploads/products/fridge-panasonic.svg', GETDATE(), GETDATE()),

(6, 3, N'Máy Giặt Cửa Ngang LG AI DD Inverter',
 N'Công nghệ trí tuệ nhân tạo AI DD tự động nhận diện độ mềm sợi vải để chọn chu trình giặt tối ưu nhất, công nghệ giặt hơi nước Steam diệt khuẩn 99.9% và bảo vệ tối đa cấu trúc sợi vải.',
 8690000, 30, '/uploads/products/washing-machine-lg.svg', GETDATE(), GETDATE()),

(7, 3, N'Máy Giặt Sấy Electrolux UltimateCare Inverter',
 N'Giải pháp 2 trong 1 hoàn hảo: Giặt 10kg, sấy khô 7kg tiện lợi cho mùa mưa nồm ẩm. Công nghệ UltraMix hòa tan hoàn toàn bột giặt, công nghệ cảm biến SensorWash tự động điều chỉnh thời gian và lượng nước.',
 14290000, 18, '/uploads/products/washing-machine-electrolux.svg', GETDATE(), GETDATE()),

(8, 4, N'Điều Hòa Daikin Inverter 1 Chiều Tiết Kiệm Điện',
 N'Máy lạnh Daikin thế hệ mới sử dụng môi chất lạnh R-32 thân thiện môi trường, luồng gió Coanda tỏa đều khắp phòng không phả trực tiếp vào người, phin lọc Enzyme Blue diệt khuẩn và khử mùi hiệu quả.',
 9290000, 40, '/uploads/products/ac-daikin-inverter.svg', GETDATE(), GETDATE()),

(9, 4, N'Điều Hòa Panasonic Inverter Nanoe-X Lọc Bụi Mịn',
 N'Công nghệ lọc khí Nanoe-X thế hệ 2 ức chế hiệu quả vi khuẩn, virus và các chất gây dị ứng suốt 24 giờ. Chế độ ECO tích hợp trí tuệ nhân tạo AI giúp cân bằng nhiệt độ tối ưu và tiết kiệm điện.',
 10490000, 30, '/uploads/products/ac-panasonic-nanoe.svg', GETDATE(), GETDATE()),

(10, 5, N'Robot Hút Bụi Lau Nhà Dreame L10s Ultra Tự Giặt Giẻ',
 N'Robot cao cấp tự động gom rác 60 ngày, tự giặt giẻ và sấy khô bằng khí nóng chống nấm mốc. Lực hút siêu mạnh 5300Pa, camera AI RGB tránh vật cản chính xác và định vị Laser LDS vẽ bản đồ 3D ngôi nhà.',
 12990000, 22, '/uploads/products/robot-dreame-l10s.svg', GETDATE(), GETDATE()),

(11, 5, N'Robot Hút Bụi Lau Nhà Roborock Q Revo Trạm Đa Năng',
 N'Hệ thống lau xoay kép 200 vòng/phút đánh bay mọi vết bẩn cứng đầu, tự nâng khăn lau 7mm khi lên thảm, lực hút 5500Pa Extreme, trạm sạc đa năng tự động nạp nước sạch và giặt giẻ.',
 13590000, 20, '/uploads/products/robot-roborock-qrevo.svg', GETDATE(), GETDATE()),

(12, 5, N'Máy Lọc Không Khí Xiaomi Smart Air Purifier 4 Pro',
 N'CADR lên tới 500m³/h phù hợp cho phòng khách và căn hộ diện tích từ 35-60m², màng lọc HEPA hiệu suất cao lọc 99.97% hạt bụi mịn PM2.5, phấn hoa và mùi khó chịu. Màn hình OLED cảm ứng hiện đại.',
 1890000, 35, '/uploads/products/airpurifier-xiaomi-pro4.svg', GETDATE(), GETDATE()),

(13, 5, N'Nồi Chiên Không Dầu Điện Tử Philips 7.2 Lít',
 N'Công nghệ Rapid Air độc quyền giảm 90% lượng chất béo trong thực phẩm, dung tích lớn 7.2L chứa vừa gà nguyên con, điều khiển cảm ứng điện tử một chạm với 16 chế độ nấu cài sẵn.',
 2790000, 25, '/uploads/products/airfryer-philips.svg', GETDATE(), GETDATE()),

(14, 5, N'Khóa Cửa Thông Minh Nhận Diện Khuôn Mặt Kaadas K20-F',
 N'Công nghệ nhận diện gương mặt 3D Face ID tiên tiến chống giả mạo bằng ảnh/video, cảm biến vân tay FPC Thụy Điển nhận diện trong 0.5s, kết nối Wi-Fi quản lý lịch sử mở cửa qua điện thoại thông minh.',
 7890000, 18, '/uploads/products/smartlock-kaadas.svg', GETDATE(), GETDATE()),

(15, 5, N'Camera An Ninh Xoay 360 Ngoài Trời Ezviz C8W 4MP 2K+',
 N'Độ phân giải 2K+ siêu nét, khả năng xoay 360 độ bao quát toàn cảnh không điểm mù, ghi hình có màu ban đêm với đèn spotlight trợ sáng, cảnh báo còi hú và đèn chớp khi phát hiện người lạ.',
 1390000, 40, '/uploads/products/camera-ezviz-c8w.svg', GETDATE(), GETDATE());
SET IDENTITY_INSERT products OFF;
GO

-- 7. THÊM BIẾN THỂ CHO CÁC SẢN PHẨM (PRODUCT_VARIANTS)
SET IDENTITY_INSERT product_variants ON;
INSERT INTO product_variants (variantid, productid, sku, variant_name, attribute_size, attribute_color, color_hex, price, stock, image_url, is_default) VALUES
-- SP 1: Smart TV Samsung
(1, 1, 'SAM-TV-43', N'43 inch - Đen Titan', N'43 inch', N'Đen Titan', '#1e293b', 8490000, 20, '/uploads/products/tv-samsung-crystal.svg', 0),
(2, 1, 'SAM-TV-55', N'55 inch - Đen Titan', N'55 inch', N'Đen Titan', '#1e293b', 11990000, 15, '/uploads/products/tv-samsung-crystal.svg', 1),
(3, 1, 'SAM-TV-65', N'65 inch - Đen Titan', N'65 inch', N'Đen Titan', '#1e293b', 15490000, 10, '/uploads/products/tv-samsung-crystal.svg', 0),

-- SP 2: Sony Bravia
(4, 2, 'SNY-TV-50', N'50 inch - Đen Carbon', N'50 inch', N'Đen Carbon', '#111827', 10990000, 15, '/uploads/products/tv-sony-bravia.svg', 0),
(5, 2, 'SNY-TV-65', N'65 inch - Đen Carbon', N'65 inch', N'Đen Carbon', '#111827', 16890000, 12, '/uploads/products/tv-sony-bravia.svg', 1),
(6, 2, 'SNY-TV-75', N'75 inch - Đen Carbon', N'75 inch', N'Đen Carbon', '#111827', 24990000, 8, '/uploads/products/tv-sony-bravia.svg', 0),

-- SP 3: Soundbar Samsung
(7, 3, 'SND-SAM-BLK', N'Tiêu chuẩn - Đen Huyền Bí', N'Tiêu chuẩn', N'Đen Huyền Bí', '#0f172a', 4890000, 20, '/uploads/products/soundbar-samsung.svg', 1),
(8, 3, 'SND-SAM-SLV', N'Tiêu chuẩn - Bạc Titan', N'Tiêu chuẩn', N'Bạc Titan', '#94a3b8', 5190000, 10, '/uploads/products/soundbar-samsung.svg', 0),

-- SP 4: Tủ lạnh LG Side by Side
(9, 4, 'FR-LG-SLV', N'519 Lít - Bạc Thép', N'519 Lít', N'Bạc Thép', '#94a3b8', 13990000, 15, '/uploads/products/fridge-lg-sidebyside.svg', 1),
(10, 4, 'FR-LG-BLK', N'519 Lít - Đen Gương', N'519 Lít', N'Đen Gương', '#1e293b', 14990000, 10, '/uploads/products/fridge-lg-sidebyside.svg', 0),

-- SP 5: Tủ lạnh Panasonic
(11, 5, 'FR-PAN-SLV', N'322 Lít - Ánh Kim Bạc', N'322 Lít', N'Ánh Kim Bạc', '#cbd5e1', 9790000, 12, '/uploads/products/fridge-panasonic.svg', 1),
(12, 5, 'FR-PAN-BLK', N'322 Lít - Đen Ánh Kim', N'322 Lít', N'Đen Ánh Kim', '#0f172a', 10290000, 8, '/uploads/products/fridge-panasonic.svg', 0),

-- SP 6: Máy giặt LG
(13, 6, 'WM-LG-9W', N'9.0 Kg - Trắng Sứ', N'9.0 Kg', N'Trắng Sứ', '#ffffff', 8690000, 12, '/uploads/products/washing-machine-lg.svg', 1),
(14, 6, 'WM-LG-9G', N'9.0 Kg - Xám Khói', N'9.0 Kg', N'Xám Khói', '#475569', 8990000, 10, '/uploads/products/washing-machine-lg.svg', 0),
(15, 6, 'WM-LG-10G', N'10.5 Kg - Xám Khói', N'10.5 Kg', N'Xám Khói', '#475569', 10990000, 8, '/uploads/products/washing-machine-lg.svg', 0),

-- SP 7: Máy giặt sấy Electrolux
(16, 7, 'WM-ELX-W', N'Giặt 10kg/Sấy 7kg - Trắng', N'Giặt 10kg - Sấy 7kg', N'Trắng Tinh Khôi', '#ffffff', 14290000, 10, '/uploads/products/washing-machine-electrolux.svg', 1),
(17, 7, 'WM-ELX-G', N'Giặt 10kg/Sấy 7kg - Xám', N'Giặt 10kg - Sấy 7kg', N'Xám Titan', '#334155', 15190000, 8, '/uploads/products/washing-machine-electrolux.svg', 0),

-- SP 8: Điều hòa Daikin
(18, 8, 'AC-DKN-10', N'1.0 HP (9000 BTU) - Trắng', N'1.0 HP (9000 BTU)', N'Trắng Ngọc', '#f8fafc', 9290000, 18, '/uploads/products/ac-daikin-inverter.svg', 1),
(19, 8, 'AC-DKN-15', N'1.5 HP (12000 BTU) - Trắng', N'1.5 HP (12000 BTU)', N'Trắng Ngọc', '#f8fafc', 11490000, 14, '/uploads/products/ac-daikin-inverter.svg', 0),
(20, 8, 'AC-DKN-20', N'2.0 HP (18000 BTU) - Trắng', N'2.0 HP (18000 BTU)', N'Trắng Ngọc', '#f8fafc', 17990000, 8, '/uploads/products/ac-daikin-inverter.svg', 0),

-- SP 9: Điều hòa Panasonic
(21, 9, 'AC-PAN-10', N'1.0 HP (9000 BTU) - Trắng', N'1.0 HP (9000 BTU)', N'Trắng Sứ', '#ffffff', 10490000, 18, '/uploads/products/ac-panasonic-nanoe.svg', 1),
(22, 9, 'AC-PAN-15', N'1.5 HP (12000 BTU) - Trắng', N'1.5 HP (12000 BTU)', N'Trắng Sứ', '#ffffff', 12890000, 12, '/uploads/products/ac-panasonic-nanoe.svg', 0),

-- SP 10: Robot Dreame L10s
(23, 10, 'RB-DRM-WHT', N'Tiêu Chuẩn - Trắng Ceramic', N'Tiêu Chuẩn', N'Trắng Ceramic', '#ffffff', 12990000, 14, '/uploads/products/robot-dreame-l10s.svg', 1),
(24, 10, 'RB-DRM-BLK', N'Bản Đặc Biệt - Đen Mờ', N'Bản Đặc Biệt', N'Đen Mờ', '#1e293b', 13490000, 8, '/uploads/products/robot-dreame-l10s.svg', 0),

-- SP 11: Robot Roborock Q Revo
(25, 11, 'RB-ROB-WHT', N'Trạm Đa Năng - Trắng Tinh Khôi', N'Trạm Đa Năng', N'Trắng Tinh Khôi', '#ffffff', 13590000, 12, '/uploads/products/robot-roborock-qrevo.svg', 1),
(26, 11, 'RB-ROB-BLK', N'Trạm Đa Năng - Đen Carbon', N'Trạm Đa Năng', N'Đen Carbon', '#111827', 13990000, 8, '/uploads/products/robot-roborock-qrevo.svg', 0),

-- SP 12: Máy lọc không khí Xiaomi
(27, 12, 'AP-MI-PRO', N'Bản Pro (35-60m²) - Trắng', N'Bản Pro (35-60m²)', N'Trắng Tinh Khôi', '#ffffff', 3990000, 20, '/uploads/products/airpurifier-xiaomi-pro4.svg', 1),
(28, 12, 'AP-MI-CMP', N'Bản Compact (16-27m²) - Trắng', N'Bản Compact (16-27m²)', N'Trắng Tinh Khôi', '#ffffff', 1890000, 15, '/uploads/products/airpurifier-xiaomi-pro4.svg', 0),

-- SP 13: Nồi chiên không dầu Philips
(29, 13, 'AF-PHI-72', N'7.2 Lít - Đen Viền Đồng', N'7.2 Lít', N'Đen Viền Đồng', '#262626', 3490000, 15, '/uploads/products/airfryer-philips.svg', 1),
(30, 13, 'AF-PHI-62', N'6.2 Lít - Đen Viền Bạc', N'6.2 Lít', N'Đen Viền Bạc', '#333333', 2790000, 10, '/uploads/products/airfryer-philips.svg', 0),

-- SP 14: Khóa thông minh Kaadas
(31, 14, 'LK-KAA-BLK', N'Mở Khóa 3D Face - Đen Nhám Titan', N'Mở Khóa 3D Face', N'Đen Nhám Titan', '#1e1e1e', 7890000, 10, '/uploads/products/smartlock-kaadas.svg', 1),
(32, 14, 'LK-KAA-COP', N'Mở Khóa 3D Face - Đồng Đỏ Cổ Điển', N'Mở Khóa 3D Face', N'Đồng Đỏ Cổ Điển', '#843b2b', 8290000, 8, '/uploads/products/smartlock-kaadas.svg', 0),

-- SP 15: Camera Ezviz C8W
(33, 15, 'CAM-EZV-32', N'Kèm Thẻ 32GB - Trắng Sứ', N'Kèm Thẻ 32GB', N'Trắng Sứ', '#ffffff', 1390000, 15, '/uploads/products/camera-ezviz-c8w.svg', 1),
(34, 15, 'CAM-EZV-64', N'Kèm Thẻ 64GB - Trắng Sứ', N'Kèm Thẻ 64GB', N'Trắng Sứ', '#ffffff', 1590000, 15, '/uploads/products/camera-ezviz-c8w.svg', 0),
(35, 15, 'CAM-EZV-128', N'Kèm Thẻ 128GB - Trắng Sứ', N'Kèm Thẻ 128GB', N'Trắng Sứ', '#ffffff', 1850000, 10, '/uploads/products/camera-ezviz-c8w.svg', 0);
SET IDENTITY_INSERT product_variants OFF;
GO

-- 8. THÊM MULTIPLE IMAGES CHO GALLERY CỦA CÁC SẢN PHẨM (PRODUCT_IMAGES)
SET IDENTITY_INSERT product_images ON;
INSERT INTO product_images (imageid, productid, image_url, display_order, is_primary) VALUES
(1, 1, '/uploads/products/tv-samsung-crystal.svg', 1, 1),
(2, 1, '/uploads/products/tv-samsung-crystal-side.svg', 2, 0),
(3, 1, '/uploads/products/tv-samsung-crystal-room.svg', 3, 0),

(4, 2, '/uploads/products/tv-sony-bravia.svg', 1, 1),
(5, 2, '/uploads/products/tv-sony-bravia-angle.svg', 2, 0),

(6, 3, '/uploads/products/soundbar-samsung.svg', 1, 1),

(7, 4, '/uploads/products/fridge-lg-sidebyside.svg', 1, 1),
(8, 4, '/uploads/products/fridge-lg-open.svg', 2, 0),

(9, 5, '/uploads/products/fridge-panasonic.svg', 1, 1),

(10, 6, '/uploads/products/washing-machine-lg.svg', 1, 1),
(11, 6, '/uploads/products/washing-machine-lg-open.svg', 2, 0),

(12, 7, '/uploads/products/washing-machine-electrolux.svg', 1, 1),

(13, 8, '/uploads/products/ac-daikin-inverter.svg', 1, 1),
(14, 8, '/uploads/products/ac-daikin-remote.svg', 2, 0),

(15, 9, '/uploads/products/ac-panasonic-nanoe.svg', 1, 1),

(16, 10, '/uploads/products/robot-dreame-l10s.svg', 1, 1),
(17, 10, '/uploads/products/robot-dreame-station.svg', 2, 0),

(18, 11, '/uploads/products/robot-roborock-qrevo.svg', 1, 1),

(19, 12, '/uploads/products/airpurifier-xiaomi-pro4.svg', 1, 1),

(20, 13, '/uploads/products/airfryer-philips.svg', 1, 1),

(21, 14, '/uploads/products/smartlock-kaadas.svg', 1, 1),

(22, 15, '/uploads/products/camera-ezviz-c8w.svg', 1, 1);
SET IDENTITY_INSERT product_images OFF;
GO

PRINT 'Migration Direction 3 executed successfully!';
GO
