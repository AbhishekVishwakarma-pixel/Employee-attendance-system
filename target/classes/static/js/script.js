/**
 * Employee Attendance Management System – Client-side JS
 */
document.addEventListener('DOMContentLoaded', function () {

    // Auto-hide alerts after 5 seconds
    document.querySelectorAll('.alert').forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-8px)';
            setTimeout(function () {
                if (alert.parentElement) alert.remove();
            }, 400);
        }, 5000);
    });

});
