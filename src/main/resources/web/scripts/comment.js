let addCommentForm;
let addCommentTextarea;
let addCommentButton;

let commentsContainer;
let commentsList;

document.addEventListener("DOMContentLoaded", () => {
    addCommentForm = document.querySelector('.add-comment-form');
    addCommentTextarea = addCommentForm.querySelector('.add-comment-form-text');
    addCommentButton = document.querySelector('.add-comment-btn');

    commentsContainer = document.querySelector('.comments-container');
    commentsList = document.querySelectorAll('.comment');
});

/**
 * Отобразить форму добавления комментария
 */
function showAddCommentForm() {
    if (addCommentForm) {
        addCommentButton.style.display = 'none';
        addCommentForm.style.display = 'flex';
    }
}

/**
 * Показать форму редактирования комментария
 * @param commentUuid Идентификатор комментария
 * @param isToShow Показать или скрыть форму
 */
function toggleEditCommentForm(commentUuid, isToShow) {
    const comment = commentsContainer.querySelector(`#comment-${commentUuid}`);

    const commentText = comment.querySelector(`.comment-text`);
    const commentDate = comment.querySelector(`.comment-date`);
    const editCommentButton = comment.querySelector(`.edit-comment-btn`);
    const deleteCommentButton = comment.querySelector(`.delete-comment-btn`);

    const editForm = commentsContainer.querySelector(`#edit-form-${commentUuid}`);

    if (isToShow) {
        // Скрыть текст и кнопки комментария и показать форму редактирования
        commentText.style.display = 'none';
        commentDate.style.display = 'none';
        editCommentButton.style.display = 'none';
        deleteCommentButton.style.display = 'none';

        editForm.style.display = 'block';

        // Сфокусироваться на текстовом поле
        const textarea = editForm.querySelector('textarea');
        // Установить текст комментария в textarea
        textarea.value = commentText.textContent;
        textarea.focus();
    } else {
        // Отобразить текст и кнопки комментария и скрыть форму редактирования
        commentText.style.display = 'block';
        commentDate.style.display = 'block';
        editCommentButton.style.display = 'block';
        deleteCommentButton.style.display = 'block';

        editForm.style.display = 'none';

        const textarea = editForm.querySelector('textarea');
        if (textarea) {
            textarea.value = "";
        }
    }
}

/**
 * Обработка нажатия Ctrl + Enter
 * @param event Событие
 * @param postUuid Идентификатор поста
 * @param commentUuid Идентификатор комментария
 */
function handleKeyDown(event, postUuid, commentUuid) {
    if (event.ctrlKey && event.key === 'Enter') {
        event.preventDefault();
        updateComment(postUuid, commentUuid);
    }
}

/**
 * Получить значение отредактированного комментария
 * @param commentUuid Идентификатор комментария
 */
function getCommentContent(commentUuid) {
    return document.getElementById(`comment-content-${commentUuid}`).value;
}

/**
 * Обновить комментарий
 * @param postUuid Идентификатор поста
 * @param commentUuid Идентификатор комментария
 */
function updateComment(postUuid, commentUuid) {
    const content = getCommentContent(commentUuid);

    fetchUrl(`${config.baseUrl}/${postUuid}/comments/${commentUuid}`, 'POST', content)
        .then(res => {
            if (res.ok) {
                // Закрываем форму редактирования
                toggleEditCommentForm(commentUuid, false);

                // Обновляем текст комментария на странице
                document.querySelector(`#comment-${commentUuid} .comment-text`).textContent = content;
            }});
}

/**
 * Удалить комментарий
 * @param postUuid Идентификатор поста
 * @param commentUuid Идентификатор комментария
 */
function deleteComment(postUuid, commentUuid) {
    return fetchUrl(`${config.baseUrl}/${postUuid}/comments/${commentUuid}`, 'DELETE')
        .then(res => {
            if (res.ok) {
                commentsList.forEach(comment => {
                    if (comment.getAttribute('value') === commentUuid) {
                        comment.remove()
                    }
                })
            } else {
                console.error("Ошибка удаления");
            }
        })
}