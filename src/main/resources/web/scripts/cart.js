let cartTotal;

document.addEventListener("DOMContentLoaded", () => {
    cartTotal = document.querySelector(".cart-total");

    // Обработчики для кнопок +/-
    document.querySelectorAll('.quantity-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const cartItem = e.target.closest('.cart-item');
            const productUuid = cartItem.getAttribute('data-product-uuid');
            const input = cartItem.querySelector('.quantity-input');

            const delta = e.target.classList.contains('plus') ? 1 : -1;
            let newValue = parseInt(input.value) + delta;
            if (newValue < 1) newValue = 1;
            input.value = newValue;

            await updateCartItem(productUuid, newValue)
                .then(async response => {
                    if (response.ok) {
                        window.location.reload();
                    } else {
                        showNotification('Ошибка при обновлении количества товара в корзине', 'error');
                    }
                });
        });
    });

    // Обработчик изменения input вручную
    document.querySelectorAll('.quantity-input').forEach(input => {
        input.addEventListener('change', async function() {
            const cartItem = this.closest('.cart-item');
            const productUuid = cartItem.getAttribute('data-product-uuid');
            const newValue = parseInt(this.value) || 1;

            await updateCartItem(productUuid, newValue)
                .then(async response => {
                    if (response.ok) {
                        window.location.reload();
                    } else {
                        showNotification('Ошибка при обновлении количества товара в корзине', 'error');
                    }
                });
        });
    });
})

/**
 * Обновить стоимость корзины
 * @param total Стоимость корзины
 */
function updateCartTotal(total) {
    console.log("function updateCartTotal(total)");
    console.log(total);
    if (cartTotal) {
        const formattedTotal = new Intl.NumberFormat('ru-RU', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(parseFloat(total));

        cartTotal.textContent = `${formattedTotal} ₽`;
    }
}