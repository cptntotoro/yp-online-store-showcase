let cartTotal;

document.addEventListener("DOMContentLoaded", () => {
    cartTotal = document.querySelector(".cart-total");

    document.querySelectorAll('.cart-action-btn').forEach(btn => {
        console.log("document.querySelectorAll('.cart-action-btn')");
        btn.addEventListener('click', async (e) => {
            const card = e.target.closest('.card');
            const productUuid = card.getAttribute('data-product-uuid');
            console.log("productUuid");
            console.log(productUuid);
            // const productUuid = card.dataset.productUuid;

            if (e.target.classList.contains('in-cart')) {
                await setRemoveFromCart(productUuid);
            } else {
                const quantity = 1; // Стандартное количество при добавлении
                await setAddToCart(productUuid, quantity);
            }
        });
    });

    document.querySelectorAll('.quantity-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            console.log("document.querySelectorAll('.quantity-btn')");
            const card = e.target.closest('.card');
            const productUuid = card.getAttribute('data-product-uuid');
            console.log("productUuid");
            console.log(productUuid);
            const input = e.target.closest('.quantity-controls').querySelector('.quantity-input');
            const delta = e.target.classList.contains('plus') ? 1 : -1;

            let newValue = parseInt(input.value) + delta;
            if (newValue < 1) newValue = 1;
            input.value = newValue;

            await setUpdateCartItem(productUuid, newValue);
        });
    });
});

/**
 * Добавить в корзину
 */
function setAddToCart(productUuid, quantity) {
    console.log("function setAddToCart(productUuid, quantity)");
    addToCart(productUuid, quantity)
        .then(async response => {
            if (response.ok) {
                const cartTotalResponse = await response.text();
                console.log("cartTotalResponse");
                console.log(cartTotalResponse);
                updateCartTotal(cartTotalResponse);
                updateProductUI(productUuid, true, 1);
                showNotification('Товар добавлен в корзину');
            } else {
                showNotification('Ошибка при добавлении в корзину', 'error');
            }
        });
}

/**
 * Удалить из корзины
 * @param productUuid Идентификатор товара
 */
function setRemoveFromCart(productUuid) {
    removeFromCart(productUuid)
        .then(async response => {
            if (response.ok) {
                const cartTotalResponse = await response.text();
                updateProductUI(productUuid, false, 1);
                updateCartTotal(cartTotalResponse);
                showNotification('Товар удален из корзины', 'success');
            } else {
                showNotification('Ошибка при удалении из корзины', 'error');
            }
        });
}

/**
 * Изменить количество товара в корзине
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
            } else {
                showNotification('Ошибка обновления количества', 'error');
            }
        });
}

/**
 * Отрисовать кнопку и количество товара после изменения карточки товара
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