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

            // console.log("document.querySelectorAll('.quantity-btn')");
            // const cartItem = e.target.closest('.cart-item');
            // const productUuid = cartItem.getAttribute('data-product-uuid');
            // console.log("productUuid");
            // console.log(productUuid);
            // const input = e.target.closest('.quantity-controls').querySelector('.quantity-input');
            // const delta = e.target.classList.contains('plus') ? 1 : -1;
            //
            // let newValue = parseInt(input.value) + delta;
            // if (newValue < 1) newValue = 1;
            // input.value = newValue;
            //
            // await setUpdateCartItem(productUuid, newValue);
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

// /**
//  * Изменить количество товара в корзине
//  * @param productUuid Идентификатор товара
//  * @param quantity Количество товара
//  * @returns {Promise<Response>}
//  */
// async function setUpdateCartItem(productUuid, quantity) {
//     updateCartItem(productUuid, quantity)
//         .then(async response => {
//             if (response.ok) {
//                 const cartTotalResponse = await response.text();
//                 console.log("cartTotalResponse");
//                 console.log(cartTotalResponse);
//                 updateCartTotal(cartTotalResponse);
//             } else {
//                 showNotification('Ошибка обновления количества', 'error');
//             }
//         });
// }

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
//
// /**
//  * Удалить из корзины
//  * @param productUuid Идентификатор товара
//  */
// function setRemoveFromCart(productUuid) {
//     removeFromCart(productUuid)
//         .then(async response => {
//             if (response.ok) {
//                 const cartTotalResponse = await response.text();
//                 updateProductUI(productUuid, false, 1);
//                 updateCartTotal(cartTotalResponse);
//                 showNotification('Товар удален из корзины', 'success');
//             } else {
//                 showNotification('Ошибка при удалении из корзины', 'error');
//             }
//         });
// }