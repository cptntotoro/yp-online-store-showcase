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