let cartTotal;
let cartTotalValue;

document.addEventListener("DOMContentLoaded", () => {
    cartTotal = document.querySelector(".cart-total");
    cartTotalValue = document.querySelector(".summary-total-value");

    // Обработчики для кнопок корзины
    document.querySelectorAll('.cart-action-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const card = e.target.closest('.card');
            const productUuid = card.getAttribute('data-product-uuid');

            if (e.target.classList.contains('in-cart')) {
                await setRemoveFromCart(productUuid, btn.getAttribute('data-cart') ? true : false);
            } else {
                const quantity = 1; // Стандартное количество при добавлении
                await setAddToCart(productUuid, quantity);
            }
        });
    });

    // Обработчики для кнопок изменения количества
    document.querySelectorAll('.quantity-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const card = e.target.closest('.card');
            const productUuid = card.getAttribute('data-product-uuid');
            const input = e.target.closest('.quantity-controls').querySelector('.quantity-input');
            const delta = e.target.classList.contains('plus') ? 1 : -1;

            let newValue = parseInt(input.value) + delta;
            if (newValue < 1) newValue = 1;
            input.value = newValue;

            await setUpdateCartItem(productUuid, newValue);

            updateItemDisplay(card, newValue);
        });
    });
});

/**
 * Обновить количество и сумму товаров в корзине
 *
 * @param cardElement Элемент корзины
 * @param newQuantity Новое значение
 */
function updateItemDisplay(cardElement, newQuantity) {
    // Обновляем количество
    const quantityElement = cardElement.querySelector('.item-quantity');
    if (quantityElement) {
        quantityElement.textContent = newQuantity;
    }

    // Обновляем общую сумму для товара
    const priceElement = cardElement.querySelector('.item-price');
    if (priceElement) {
        const price = parseFloat(priceElement.textContent.replace(',', '.').replace(' ₽', ''));
        const totalElement = cardElement.querySelector('.item-total');
        const total = (price * newQuantity).toFixed(2).replace('.', ',');
        totalElement.textContent = `${total} ₽`;
    }

}

/**
 * Добавить в корзину
 *
 * @param productUuid Идентификатор товара
 * @param quantity Количество товара
 */
function setAddToCart(productUuid, quantity) {
    addToCart(productUuid, quantity)
        .then(async response => {
            if (response.ok) {
                const cartTotalResponse = await response.text();
                updateProductUI(productUuid, true, 1);
                updateCartTotal(cartTotalResponse);
                showNotification('Товар добавлен в корзину');
            } else {
                showNotification('Ошибка при добавлении в корзину', 'error');
            }
        });
}

/**
 * Удалить из корзины
 *
 * @param productUuid Идентификатор товара
 * @param isCart
 */
function setRemoveFromCart(productUuid, isCart) {
    removeFromCart(productUuid)
        .then(async response => {
            if (response.ok) {
                if (isCart) {
                    window.location.reload();
                } else {
                    const cartTotalResponse = await response.text();
                    updateProductUI(productUuid, false, 1);
                    updateCartTotal(cartTotalResponse);
                    showNotification('Товар удален из корзины', 'success');
                }
            } else {
                showNotification('Ошибка при удалении из корзины', 'error');
            }
        });
}

/**
 * Изменить количество товара в корзине
 *
 * @param productUuid Идентификатор товара
 * @param quantity Количество товара
 * @returns {Promise<Response>}
 */
async function setUpdateCartItem(productUuid, quantity) {
    updateCartItem(productUuid, quantity)
        .then(async response => {
            if (response.ok) {
                const cartTotalResponse = await response.text();
                updateCartTotal(cartTotalResponse);
                showNotification('Количество товара обновлено', 'success');
            } else {
                showNotification('Ошибка обновления количества', 'error');
            }
        });
}

/**
 * Отрисовать кнопку и количество товара после изменения карточки товара
 *
 * @param productUuid Идентификатор товара
 * @param inCart Находится ли товар в корзине
 * @param quantity Количество товара
 */
function updateProductUI(productUuid, inCart, quantity = 1) {
    const card = document.querySelector(`.card[data-product-uuid="${productUuid}"]`);
    if (!card) return;

    const actionBtn = card.querySelector('.cart-action-btn');
    const quantityControls = card.querySelector('.quantity-controls');
    const quantityInput = card.querySelector('.quantity-input');

    if (inCart) {
        actionBtn.textContent = 'Удалить из корзины';
        actionBtn.classList.add('btn-danger', 'in-cart');
        actionBtn.classList.remove('btn-primary');
        quantityControls.classList.remove('d-none');
        quantityInput.value = quantity;
    } else {
        actionBtn.textContent = 'Добавить в корзину';
        actionBtn.classList.add('btn-primary');
        actionBtn.classList.remove('btn-danger', 'in-cart');
        quantityControls.classList.add('d-none');
        quantityInput.value = 1;
    }
}

/**
 * Обновить стоимость корзины
 *
 * @param total Стоимость корзины
 */
function updateCartTotal(total) {
    if (cartTotal) {
        const formattedTotal = new Intl.NumberFormat('ru-RU', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(parseFloat(total));

        cartTotal.textContent = `${formattedTotal} ₽`;

        if (cartTotalValue) {
            cartTotalValue.textContent = `${formattedTotal} ₽`;
        }
    }
}