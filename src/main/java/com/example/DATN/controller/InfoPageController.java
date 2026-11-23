package com.example.DATN.controller;

import com.example.DATN.entity.Product;
import com.example.DATN.repository.CategoryRepository;
import com.example.DATN.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/info")
public class InfoPageController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/{slug}")
    public String getInfoPage(@PathVariable String slug, Model model) {
        ArticleData article = getArticleData(slug);
        if (article == null) {
            return "redirect:/products";
        }

        model.addAttribute("article", article);
        model.addAttribute("categories", categoryRepository.findAll());

        // Lấy danh sách sản phẩm lồng ghép trong quảng cáo / bài viết
        List<Product> promoProducts = new ArrayList<>();
        if (article.getTargetProductIds() != null && !article.getTargetProductIds().isEmpty()) {
            for (Integer pid : article.getTargetProductIds()) {
                productRepository.findById(pid).ifPresent(promoProducts::add);
            }
        }
        if (promoProducts.isEmpty()) {
            // Mặc định lấy 4 sản phẩm đầu tiên nếu không chỉ định
            promoProducts = productRepository.findAll().stream().limit(4).toList();
        }
        model.addAttribute("promoProducts", promoProducts);

        return "info/article";
    }

    private ArticleData getArticleData(String slug) {
        return switch (slug) {
            case "thu-cu-doi-moi" -> new ArticleData(
                    "thu-cu-doi-moi",
                    "Chương Trình Thu Cũ Đổi Mới Điện Máy — Trợ Giá Lên Đời Đến 2.500.000₫",
                    "CHƯƠNG TRÌNH ĐỘC QUYỀN",
                    "Nâng cấp không gian sống hiện đại với chi phí tiết kiệm nhất. Áp dụng cho Smart Tivi, Tủ Lạnh, Máy Giặt, Điều Hòa các thương hiệu Samsung, LG, Sony, Panasonic.",
                    Arrays.asList(1, 4, 6, 8),
                    "Trợ giá đến 2.500.000₫",
                    List.of(
                            new ArticleSection("1. Vì sao nên tham gia Thu Cũ Đổi Mới tại FlareHub?",
                                    "Bạn đang sở hữu tivi màn hình nhỏ, tủ lạnh đóng tuyết tiêu tốn điện năng hay máy giặt rung lắc ồn ào? Đừng bỏ xó hay bán phế liệu giá rẻ! FlareHub mang tới giải pháp đổi cũ lấy mới với mức định giá cao nhất thị trường, thủ tục trong 5 phút và kỹ thuật viên hỗ trợ thu tận nhà hoàn toàn miễn phí."),
                            new ArticleSection("2. Mức trợ giá áp dụng theo từng dòng sản phẩm",
                                    "• Smart Tivi 4K từ 55 inch trở lên: Trợ giá thêm đến 2.500.000₫ trực tiếp vào giá bán.<br>" +
                                            "• Tủ Lạnh Side By Side & Inverter cao cấp: Trợ giá thêm 2.000.000₫ + Tặng kèm máy sấy tóc hoặc nồi chiên không dầu.<br>" +
                                            "• Máy Giặt lồng ngang AI DD: Trợ giá thêm 1.500.000₫ + Miễn phí vận chuyển và công lắp đặt.<br>" +
                                            "• Điều Hòa Inverter: Trợ giá 1.200.000₫ + Miễn phí hút chân không và công lắp chuẩn kỹ thuật."),
                            new ArticleSection("3. Quy trình 3 bước đổi máy nhanh gọn",
                                    "<b>Bước 1:</b> Khách hàng mang máy cũ tới bất kỳ showroom FlareHub hoặc đăng ký qua tổng đài 1800.2097 để nhân viên tới kiểm tra tại nhà.<br>" +
                                            "<b>Bước 2:</b> Kỹ thuật viên thẩm định tình trạng máy trong vòng 5 - 10 phút và thông báo mức thu mua kèm trợ giá tốt nhất.<br>" +
                                            "<b>Bước 3:</b> Khách hàng lựa chọn sản phẩm mới mong muốn, bù chênh lệch (hỗ trợ cả trả góp 0%) và nhận máy mới nguyên seal ngay lập tức.")
                    )
            );

            case "tra-gop" -> new ArticleData(
                    "tra-gop",
                    "Hướng Dẫn Mua Điện Máy Trả Góp 0% Lãi Suất — Xét Duyệt Nhanh 5 Phút",
                    "ƯU ĐÃI TÀI CHÍNH",
                    "Sở hữu ngay Smart Tivi 4K, Tủ lạnh Side By Side, Robot hút bụi Dreame mà không cần trả trước toàn bộ số tiền. Thủ tục đơn giản chỉ cần CCCD gắn chip hoặc thẻ tín dụng.",
                    Arrays.asList(2, 5, 10, 11),
                    "Trả góp 0% chỉ từ 350.000₫/tháng",
                    List.of(
                            new ArticleSection("1. Hai hình thức trả góp 0% tiện lợi tại FlareHub",
                                    "<b>Hình thức 1 — Trả góp 0% qua Thẻ tín dụng (Credit Card):</b> Hỗ trợ hơn 25 ngân hàng lớn (Vietcombank, Techcombank, VPBank, TPBank, MB, Sacombank, Shinhan, v.v.). Không cần giấy tờ, kỳ hạn linh hoạt 3 - 6 - 9 - 12 tháng, phí chuyển đổi 0₫.<br><br>" +
                                            "<b>Hình thức 2 — Trả góp qua Công ty tài chính (Home Credit / HD Saison / FE Credit):</b> Áp dụng cho mọi khách hàng từ 18 tuổi trở lên. Chỉ cần CCCD gắn chip, không cần chứng minh thu nhập, duyệt hồ sơ online chỉ trong 5 phút."),
                            new ArticleSection("2. Lợi ích vượt trội khi mua trả góp tại FlareHub",
                                    "• Giá mua trả góp 0% bằng đúng giá bán niêm yết khuyến mãi, không đội giá ảo.<br>" +
                                            "• Vẫn được hưởng trọn bộ quà tặng, bảo hành chính hãng và các mã voucher giảm giá của cửa hàng.<br>" +
                                            "• Nhận máy ngay sau khi duyệt hồ sơ, hỗ trợ giao hàng và lắp đặt tận nơi miễn phí."),
                            new ArticleSection("3. Hướng dẫn các bước đăng ký",
                                    "Khách hàng bấm chọn sản phẩm bên dưới, khi liên hệ nhân viên hoặc đặt hàng ghi chú <i>'Yêu cầu hỗ trợ trả góp 0%'</i>. Chuyên viên tài chính của FlareHub sẽ liên hệ ngay để hướng dẫn hoàn tất trong vòng vài phút.")
                    )
            );

            case "giao-hang-lap-dat" -> new ArticleData(
                    "giao-hang-lap-dat",
                    "Dịch Vụ Giao Hàng Siêu Tốc 2H & Miễn Phí Lắp Đặt Tận Nhà",
                    "DỊCH VỤ TẬN TÂM",
                    "Cam kết giao nhanh trong 2 giờ tại nội thành. Đội ngũ kỹ thuật viên tay nghề cao được đào tạo bài bản từ các tập đoàn điện tử hàng đầu, trang bị đầy đủ dụng cụ chuyên nghiệp.",
                    Arrays.asList(1, 4, 6, 7),
                    "Miễn phí vận chuyển & công lắp đặt 100%",
                    List.of(
                            new ArticleSection("1. Cam kết thời gian giao hàng siêu tốc",
                                    "• Nội thành TP. Hồ Chí Minh & Hà Nội: Giao siêu tốc trong 2 giờ đối với các đơn hàng xác nhận trước 17h00.<br>" +
                                            "• Các tỉnh thành lân cận: Giao trong vòng 24 - 48 giờ bởi mạng lưới logistics liên kết uy tín.<br>" +
                                            "• Khách hàng được hẹn chính xác khung giờ giao lắp phù hợp với lịch sinh hoạt cá nhân."),
                            new ArticleSection("2. Tiêu chuẩn lắp đặt & Bàn giao thiết bị chuẩn hãng",
                                    "Đội ngũ kỹ thuật viên của FlareHub có chứng chỉ đào tạo từ Samsung, LG, Daikin, cam kết:<br>" +
                                            "• Treo tường tivi cân chỉnh thẩm mỹ, giấu dây nguồn an toàn tuyệt đối.<br>" +
                                            "• Lắp đặt máy giặt kiểm tra thăng bằng chống rung lắc, đấu nối đường nước xả và cấp đúng kỹ thuật.<br>" +
                                            "• Hướng dẫn chi tiết cách kết nối Wifi, ứng dụng SmartThings / ThinQ và cách vận hành tiết kiệm điện cho gia đình.<br>" +
                                            "• Vệ sinh sạch sẽ khu vực làm việc trước khi rời khỏi nhà khách hàng."),
                            new ArticleSection("3. Chi phí và biểu phí dịch vụ",
                                    "FlareHub <b>MIỄN PHÍ 100% CÔNG LẮP ĐẶT</b> cho toàn bộ sản phẩm tivi, máy giặt, tủ lạnh mua tại hệ thống. Vật tư phát sinh (dây điện, ống đồng ngoài gói khuyến mãi) được tính theo bảng giá niêm yết minh bạch, báo trước cho khách hàng trước khi thi công.")
                    )
            );

            case "chinh-sach-bao-hanh" -> new ArticleData(
                    "chinh-sach-bao-hanh",
                    "Chính Sách Bảo Hành Chính Hãng 12 — 24 Tháng Toàn Diện",
                    "CHÍNH SÁCH BÁN HÀNG",
                    "Tất cả sản phẩm bán ra tại FlareHub là hàng chính hãng 100%, được bảo hành điện tử chính ngạch tại các trung tâm bảo hành ủy quyền của nhà sản xuất trên toàn quốc.",
                    Arrays.asList(1, 2, 4, 6),
                    "Bảo hành kích hoạt điện tử tức thì",
                    List.of(
                            new ArticleSection("1. Thời hạn và phạm vi bảo hành",
                                    "• Tivi thông minh Samsung, Sony: Bảo hành chính hãng 24 tháng tận nhà.<br>" +
                                            "• Tủ lạnh Inverter LG, Panasonic: Bảo hành 24 tháng cho toàn bộ thân máy, máy nén Inverter bảo hành 10 - 20 năm.<br>" +
                                            "• Máy giặt cửa ngang LG, Electrolux: Bảo hành thân máy 24 tháng, động cơ truyền động trực tiếp bảo hành 10 năm.<br>" +
                                            "• Điều hòa không khí Daikin, Panasonic: Bảo hành thân máy 12 tháng, máy nén 5 năm.<br>" +
                                            "• Robot hút bụi lau nhà Dreame, Roborock: Bảo hành chính hãng 12 - 24 tháng theo tiêu chuẩn nhà phân phối."),
                            new ArticleSection("2. Dịch vụ bảo hành tận nhà tiện lợi",
                                    "Đối với các sản phẩm kích thước lớn (Tivi từ 43 inch, tủ lạnh, máy giặt, máy sấy, điều hòa): Kỹ thuật viên chính hãng của thương hiệu sẽ đến tận nhà quý khách để kiểm tra và xử lý sự cố. Quý khách không cần vất vả tháo dỡ hay vận chuyển đến trung tâm."),
                            new ArticleSection("3. Tổng đài tiếp nhận bảo hành",
                                    "Khi cần bảo hành hoặc hỗ trợ kỹ thuật, quý khách chỉ cần đọc số điện thoại mua hàng cho hotline FlareHub <b>1800.2064</b> (miễn cước). Nhân viên sẽ kích hoạt quy trình bảo hành của hãng và cử kỹ thuật viên đến hỗ trợ nhanh chóng.")
                    )
            );

            case "chinh-sach-doi-tra" -> new ArticleData(
                    "chinh-sach-doi-tra",
                    "Chính Sách 1 Đổi 1 Trong 30 Ngày — Lỗi Là Đổi Ngay",
                    "CHÍNH SÁCH BÁN HÀNG",
                    "Bảo vệ quyền lợi tối đa cho khách hàng khi trải nghiệm sản phẩm. Đổi mới 100% nếu phát sinh lỗi kỹ thuật do nhà sản xuất trong 30 ngày đầu tiên.",
                    Arrays.asList(4, 6, 10, 14),
                    "Đổi mới nguyên hộp 100%",
                    List.of(
                            new ArticleSection("1. Điều kiện áp dụng 1 đổi 1 trong 30 ngày",
                                    "• Sản phẩm bị lỗi phần cứng xác định bởi kỹ thuật viên của FlareHub hoặc trung tâm bảo hành của hãng.<br>" +
                                            "• Sản phẩm còn nguyên vẹn, không bị cấn móp, rơi vỡ, vào nước hoặc biến dạng ngoại quan do lỗi người dùng.<br>" +
                                            "• Đầy đủ hộp, phụ kiện đi kèm (dây nguồn, điều khiển, catalogue...) và hóa đơn/số điện thoại mua hàng."),
                            new ArticleSection("2. Trường hợp sau 30 ngày",
                                    "Sản phẩm sẽ được tiếp nhận bảo hành sửa chữa miễn phí theo quy định bảo hành chính hãng của từng thương hiệu sản xuất."),
                            new ArticleSection("3. Đổi sản phẩm khác theo nhu cầu",
                                    "Nếu khách hàng muốn đổi sang dòng máy cao cấp hơn trong vòng 7 ngày đầu, FlareHub hỗ trợ đổi bù chênh lệch với mức khấu hao 0% (áp dụng với máy còn mới 100% chưa qua sử dụng).")
                    )
            );

            case "chinh-sach-thanh-toan" -> new ArticleData(
                    "chinh-sach-thanh-toan",
                    "Chính Sách Thanh Toán An Toàn & Phương Thức Đa Dạng",
                    "CHÍNH SÁCH BÁN HÀNG",
                    "Hỗ trợ nhiều phương thức thanh toán linh hoạt, tiện lợi và an toàn tuyệt đối. Mọi giao dịch điện tử đều được mã hóa theo tiêu chuẩn ngân hàng bảo mật SSL 256-bit.",
                    Arrays.asList(1, 3, 10, 15),
                    "Thanh toán an toàn bảo mật SSL 256-bit",
                    List.of(
                            new ArticleSection("1. Các phương thức thanh toán được hỗ trợ",
                                    "• <b>Thanh toán khi nhận hàng (COD):</b> Khách hàng được mở hộp kiểm tra đúng model, nguyên vẹn rồi mới thanh toán tiền mặt hoặc quẹt thẻ cho nhân viên giao hàng.<br>" +
                                            "• <b>Cổng thanh toán điện tử VNPay:</b> Quét mã VNPAY-QR nhanh chóng từ hơn 30 ứng dụng ngân hàng và ví điện tử.<br>" +
                                            "• <b>Chuyển khoản trực tiếp VietQR:</b> Hệ thống tự động xác nhận giao dịch qua mã QR chính xác tới từng đồng.<br>" +
                                            "• <b>Thẻ Quốc tế (Visa, MasterCard, JCB):</b> Hỗ trợ bảo mật 3D-Secure mã xác thực OTP gửi về điện thoại."),
                            new ArticleSection("2. Cam kết an toàn thông tin thanh toán",
                                    "FlareHub không lưu trữ thông tin số thẻ tín dụng hoặc mật khẩu thanh toán của quý khách. Mọi dữ liệu thẻ đều được xử lý trực tiếp bởi các đối tác cổng thanh toán được cấp phép bởi Ngân hàng Nhà nước Việt Nam."),
                            new ArticleSection("3. Xuất hóa đơn VAT điện tử",
                                    "100% đơn hàng xuất hóa đơn VAT điện tử minh bạch theo quy định của Bộ Tài chính, gửi trực tiếp về email khách hàng trong vòng 24 giờ sau khi nhận hàng.")
                    )
            );

            case "chinh-sach-bao-mat" -> new ArticleData(
                    "chinh-sach-bao-mat",
                    "Chính Sách Bảo Mật Thông Tin Khách Hàng Chuẩn An Ninh Mạng",
                    "CHÍNH SÁCH BÁN HÀNG",
                    "FlareHub tôn trọng và cam kết bảo vệ quyền riêng tư cá nhân của mọi khách hàng. Dữ liệu của bạn được bảo mật tuyệt đối và chỉ dùng để phục vụ giao hàng, chăm sóc đơn hàng.",
                    Arrays.asList(1, 2, 8, 12),
                    "Bảo mật dữ liệu tuyệt đối 100%",
                    List.of(
                            new ArticleSection("1. Mục đích thu thập thông tin",
                                    "Chúng tôi chỉ thu thập các thông tin cần thiết phục vụ cho việc: giao nhận hàng hóa (Họ tên, SĐT, Địa chỉ), gửi hóa đơn VAT điện tử, kích hoạt bảo hành điện tử chính hãng và thông báo tiến độ đơn hàng qua SMS/Email."),
                            new ArticleSection("2. Cam kết không chia sẻ dữ liệu cho bên thứ ba",
                                    "FlareHub cam kết tuyệt đối không bán, chia sẻ hoặc thương mại hóa thông tin khách hàng cho bất kỳ bên thứ ba nào vì mục đích quảng cáo rác hay trục lợi. Thông tin chỉ được cung cấp cho đơn vị vận chuyển nhằm thực hiện giao hàng."),
                            new ArticleSection("3. Quyền lợi của khách hàng đối với thông tin cá nhân",
                                    "Khách hàng có toàn quyền tra cứu, cập nhật số điện thoại, sổ địa chỉ hoặc yêu cầu xóa tài khoản bất cứ lúc nào tại trang Cá nhân hoặc liên hệ email <b>cskh@flarehub.vn</b>.")
                    )
            );

            default -> null;
        };
    }

    public static class ArticleData {
        private String slug;
        private String title;
        private String badge;
        private String summary;
        private List<Integer> targetProductIds;
        private String promoHighlight;
        private List<ArticleSection> sections;

        public ArticleData(String slug, String title, String badge, String summary,
                           List<Integer> targetProductIds, String promoHighlight,
                           List<ArticleSection> sections) {
            this.slug = slug;
            this.title = title;
            this.badge = badge;
            this.summary = summary;
            this.targetProductIds = targetProductIds;
            this.promoHighlight = promoHighlight;
            this.sections = sections;
        }

        public String getSlug() { return slug; }
        public String getTitle() { return title; }
        public String getBadge() { return badge; }
        public String getSummary() { return summary; }
        public List<Integer> getTargetProductIds() { return targetProductIds; }
        public String getPromoHighlight() { return promoHighlight; }
        public List<ArticleSection> getSections() { return sections; }
    }

    public static class ArticleSection {
        private String heading;
        private String body;

        public ArticleSection(String heading, String body) {
            this.heading = heading;
            this.body = body;
        }

        public String getHeading() { return heading; }
        public String getBody() { return body; }
    }
}
