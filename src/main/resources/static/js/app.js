/**
 * FlareHub — Client Helper Script (Vanilla JavaScript)
 * Đảm bảo tương thích mọi trình duyệt, không phụ thuộc thư viện ngoài.
 */
document.addEventListener('DOMContentLoaded', function () {
    // 1. Chống bấm đúp gửi form (vô hiệu hóa nút sau khi sự kiện submit đã kích hoạt)
    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            // Không can thiệp nếu form đã bị hủy bởi validate phía client
            if (e.defaultPrevented) return;

            var submitBtn = form.querySelector('button[type="submit"], input[type="submit"]');
            if (submitBtn && !submitBtn.disabled) {
                // Vô hiệu hóa nút ở tick tiếp theo để form data kịp đóng gói gửi đi
                setTimeout(function () {
                    submitBtn.disabled = true;
                    submitBtn.dataset.originalText = submitBtn.innerHTML || submitBtn.value;
                    if (submitBtn.tagName === 'BUTTON') {
                        submitBtn.innerHTML = '<span>⏳ Đang xử lý...</span>';
                    }
                }, 10);
            }
        });
    });

    // 1b. Khi người dùng bấm Back, trình duyệt có thể khôi phục trang từ bộ nhớ đệm (bfcache)
    //     với nút vẫn đang bị vô hiệu hóa. Khôi phục lại nút khi trang được hiển thị lại.
    window.addEventListener('pageshow', function (e) {
        if (!e.persisted) return;
        document.querySelectorAll('button[disabled], input[type="submit"][disabled]').forEach(function (btn) {
            if (btn.dataset.originalText !== undefined) {
                if (btn.tagName === 'BUTTON') { btn.innerHTML = btn.dataset.originalText; }
                btn.disabled = false;
            }
        });
    });

    // 2. Mobile navbar toggle
    var toggleBtn = document.getElementById('mobile-nav-toggle');
    var navMenu = document.getElementById('nav-menu-wrapper');
    if (toggleBtn && navMenu) {
        toggleBtn.addEventListener('click', function () {
            navMenu.classList.toggle('show');
            var expanded = navMenu.classList.contains('show');
            toggleBtn.setAttribute('aria-expanded', expanded);
        });
    }
});