document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('registrationForm');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const passwordMatchError = document.getElementById('passwordMatchError');
    const agreeTermsCheckbox = document.getElementById('agreeTerms');
    const termsError = document.getElementById('termsError');

    form.addEventListener('submit', function(event) {
        let isValid = true;

        // Проверка совпадения паролей
        if (passwordInput.value !== confirmPasswordInput.value) {
            passwordMatchError.style.display = 'block';
            confirmPasswordInput.classList.add('is-invalid');
            isValid = false;
        } else {
            passwordMatchError.style.display = 'none';
            confirmPasswordInput.classList.remove('is-invalid');
        }

        // Проверка согласия с условиями
        if (!agreeTermsCheckbox.checked) {
            termsError.style.display = 'block';
            agreeTermsCheckbox.classList.add('is-invalid');
            isValid = false;
        } else {
            termsError.style.display = 'none';
            agreeTermsCheckbox.classList.remove('is-invalid');
        }

        if (!isValid) {
            event.preventDefault();
        }
    });

    // Валидация при изменении пароля
    confirmPasswordInput.addEventListener('input', function() {
        if (passwordInput.value !== confirmPasswordInput.value) {
            passwordMatchError.style.display = 'block';
            confirmPasswordInput.classList.add('is-invalid');
        } else {
            passwordMatchError.style.display = 'none';
            confirmPasswordInput.classList.remove('is-invalid');
        }
    });

    // Валидация чекбокса
    agreeTermsCheckbox.addEventListener('change', function() {
        if (this.checked) {
            termsError.style.display = 'none';
            this.classList.remove('is-invalid');
        }
    });
});

function checkPasswordStrength(password) {
    const strengthFill = document.getElementById('password-strength-fill');
    let strength = 0;

    if (password.length >= 8) strength++;
    if (password.match(/[a-z]/) && password.match(/[A-Z]/)) strength++;
    if (password.match(/[0-9]/)) strength++;
    if (password.match(/[^a-zA-Z0-9]/)) strength++;

    strengthFill.className = 'password-strength-fill';
    if (password.length === 0) {
        strengthFill.style.width = '0%';
    } else if (strength <= 2) {
        strengthFill.classList.add('weak');
    } else if (strength === 3) {
        strengthFill.classList.add('medium');
    } else {
        strengthFill.classList.add('strong');
    }
}