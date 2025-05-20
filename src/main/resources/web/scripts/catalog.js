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
                showNotification('Товар добавлен в корзину');

                quantityInput.value = 1;
            } else {
                console.warn('Сетевая ошибка');
                showNotification('Ошибка при добавлении в корзину', 'error');
            }
        });
}

/**
 * Добавить товар в корзину
 * @param productUuid Идентификатор товара
 * @param quantity Количество товара
 */
async function addToCart(productUuid, quantity) {
    return fetchUrl(`/cart/add/${productUuid}?quantity=${quantity}`, "POST")
}
