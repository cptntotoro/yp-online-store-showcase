let addToCartBtn;
let cartTotal;
let quantityInput;

document.addEventListener("DOMContentLoaded", () => {
    addToCartBtn = document.querySelector(".add-to-cart-btn");
    cartTotal = document.querySelector(".cart-total");
    quantityInput = document.querySelector(".quantity-input");
});

/**
 * Добавить в корзину
 * @param productUuid Идентификатор товара
 */
function setAddToCart(productUuid) {
    const quantity = parseInt(quantityInput.value) || 1;
    addToCart(productUuid, quantity)
        .then(async response => {
            if (response.ok) {
                // Обновляем сумму корзины quantity
                const cartTotalResponse = await response.text();

                const formattedTotal = new Intl.NumberFormat('ru-RU', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                }).format(parseFloat(cartTotalResponse));

                // Обновляем отображение суммы в корзине
                cartTotal.textContent = `${formattedTotal} ₽`;

                // Можно добавить анимацию или уведомление
                showCartNotification('Товар добавлен в корзину');

                quantityInput.value = 1;
            } else {
                console.warn('Сетевая ошибка');
                showCartNotification('Ошибка при добавлении в корзину', 'error');
            }
        });
}

/**
 * Вспомогательная функция для показа уведомлений
 * @param {string} message
 * @param {string} type
 */
function showCartNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `cart-notification ${type}`;
    notification.textContent = message;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.classList.add('fade-out');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}


/**
 * Добавить товар в корзину
 * @param productUuid Идентификатор товара
 * @param quantity Количество товара
 */
async function addToCart(productUuid, quantity) {
    return fetchUrl(`/cart/add/${productUuid}?quantity=${quantity}`, "POST")
}
