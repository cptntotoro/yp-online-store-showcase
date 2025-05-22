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
 * @param buttonElement Кнопка, на которую нажали
 */
function setAddToCart(buttonElement) {
    // Получаем UUID продукта из data-атрибута
    const productUuid = buttonElement.getAttribute('data-product-uuid');

    // Находим соответствующий input с количеством
    const inputElement = buttonElement.closest('.add-to-cart-form').querySelector('.quantity-input');
    const quantity = parseInt(inputElement.value) || 1;

    console.log("Adding product:", productUuid, "quantity:", quantity);

    addToCart(productUuid, quantity)
        .then(async response => {
            if (response.ok) {
                const cartTotalResponse = await response.text();
                const formattedTotal = new Intl.NumberFormat('ru-RU', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                }).format(parseFloat(cartTotalResponse));

                document.querySelector(".cart-total").textContent = `${formattedTotal} ₽`;
                showNotification('Товар добавлен в корзину');
                inputElement.value = 1;
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
