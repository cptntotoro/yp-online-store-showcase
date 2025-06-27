/**
 * Конфиг
 */
const config = {
    headers: {
        "Content-Type": "application/json"
    },
};

/**
 * Общий метод отправки запроса
 */
async function fetchUrl(url, method, body) {
    return fetch(url, {
        method: method,
        headers: config.headers,
        body: JSON.stringify(body)
    });
}

/**
 * Добавить товар в корзину
 *
 * @param productUuid Идентификатор товара
 * @param quantity Количество товара
 * @returns {Promise<Response>}
 */
async function addToCart(productUuid, quantity) {
    return fetchUrl(`/cart/add/${productUuid}?quantity=${quantity}`, "POST")
}

/**
 * Удалить товар из корзины
 *
 * @param productUuid Идентификатор товара
 * @returns {Promise<Response>}
 */
async function removeFromCart(productUuid) {
    return fetchUrl(`/cart/remove/${productUuid}`, "DELETE")
}

/**
 * Изменить количество товара в корзине
 *
 * @param productUuid Идентификатор товара
 * @param quantity Количество товара
 * @returns {Promise<Response>}
 */
async function updateCartItem(productUuid, quantity) {
    return fetchUrl(`/cart/update/${productUuid}?quantity=${quantity}`, 'PATCH')
}