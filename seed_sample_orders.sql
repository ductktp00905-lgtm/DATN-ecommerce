USE ECommerceDB;
GO

SET IDENTITY_INSERT orders ON;
INSERT INTO orders (orderid, userid, total_amount, status, shipping_address, order_date, discount_amount) VALUES
(101, 8, 15490000, 'PENDING', N'Số 125 Cầu Giấy, Quận Cầu Giấy, Hà Nội', DATEADD(MINUTE, -25, GETDATE()), 0),
(102, 11, 12990000, 'PENDING', N'284 Nguyễn Trãi, Phường Phạm Ngũ Lão, Quận 1, TP.HCM', DATEADD(MINUTE, -10, GETDATE()), 0),
(103, 9, 8690000, 'SHIPPING', N'56 Quang Trung, Phường Thạch Thang, Hải Châu, Đà Nẵng', DATEADD(HOUR, -5, GETDATE()), 0),
(104, 8, 3490000, 'COMPLETED', N'Số 125 Cầu Giấy, Quận Cầu Giấy, Hà Nội', DATEADD(DAY, -1, GETDATE()), 0),
(105, 11, 9290000, 'COMPLETED', N'284 Nguyễn Trãi, Phường Phạm Ngũ Lão, Quận 1, TP.HCM', DATEADD(DAY, -2, GETDATE()), 0);
SET IDENTITY_INSERT orders OFF;
GO

INSERT INTO order_details (orderid, productid, quantity, price, variantid, variant_info) VALUES
(101, 1, 1, 15490000, 3, N'65 inch - Đen Titan'),
(102, 10, 1, 12990000, 23, N'Tiêu Chuẩn - Trắng Ceramic'),
(103, 6, 1, 8690000, 13, N'9.0 Kg - Trắng Sứ'),
(104, 13, 1, 3490000, 29, N'7.2 Lít - Đen Viền Đồng'),
(105, 8, 1, 9290000, 18, N'1.0 HP (9000 BTU) - Trắng Ngọc');
GO

PRINT 'Sample orders seeded successfully!';
GO
