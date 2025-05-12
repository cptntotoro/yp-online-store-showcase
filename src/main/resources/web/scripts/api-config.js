/**
 * Конфиг
 */
const config = {
    baseUrl: "/posts",
    headers: {
        "Content-Type": "application/json"
    },
};

/**
 * Общий метод отправки запроса
 */
function fetchUrl(url, method, body) {
    return fetch(url, {
        method: method,
        headers: config.headers,
        body: JSON.stringify(body)
    });
}