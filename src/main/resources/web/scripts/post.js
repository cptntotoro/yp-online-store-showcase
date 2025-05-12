let likeButton;

document.addEventListener("DOMContentLoaded", () => {
    console.log("DOM fully loaded and parsed");
    likeButton = document.querySelector(".like-btn");
    headerButton = document.querySelector(".header__btn");
    headerButton.textContent = "Назад к ленте";
    headerButton.setAttribute('href', '/posts');
});

/**
 * Поставить лайк посту
 * @param postUuid Идентификатор поста
 */
function setLike(postUuid) {
    return fetchUrl(`${config.baseUrl}/${postUuid}/like`, "PUT")
}

/**
 * Удалить пост
 * @param postUuid Идентификатор поста
 */
function deletePost(postUuid) {
    return fetchUrl(`${config.baseUrl}/${postUuid}`, 'DELETE')
        .then(res => res.ok ? window.location.href = `${config.baseUrl}` : console.warn(`Ошибка: ${res.status}`))
}

/**
 * Поставить лайк посту
 * @param postUuid Идентификатор поста
 */
function setLikeToPost(postUuid) {
    return setLike(postUuid)
        .then(response => {
            if (response.ok) {
                let buttonText = likeButton.textContent;
                const currentLikes = parseInt(buttonText.match(/\d+/)[0], 10); // Ищем число в тексте

                const newLikes = currentLikes + 1;

                // Обновляем текст кнопки
                likeButton.textContent = buttonText.slice(0, -currentLikes.toString().length).concat(newLikes.toString());
            } else {
                console.warn('Сетевая ошибка');
            }
        });
}
