let modalOverlay;
let closeModalButton;

let titleInput;
let imageUrlInput;
let contentTextarea;
let tagCheckboxes;

let editPostButton;

document.addEventListener("DOMContentLoaded", () => {

    modalOverlay = document.getElementById('modalOverlay');
    closeModalButton = document.getElementById('closeModalButton');

    titleInput = document.getElementById('post-title');
    imageUrlInput = document.getElementById('post-imageUrl');
    contentTextarea = document.getElementById('post-content');
    tagCheckboxes = document.querySelectorAll('.tag-checkbox');

    editPostButton = document.querySelector(".edit-btn");

    if (editPostButton) {
        setModalBehavior(editPostButton);
        populateEditPostModal();
    }
});

function setModalBehavior(button) {
    button.addEventListener('click', function () {
        modalOverlay.style.display = 'flex';
        document.body.style.overflow = 'hidden'; // Блокируем прокрутку основного контента

        const contentTextarea = document.getElementById('post-content');
        if (contentTextarea) {
            autoResize(contentTextarea);
        }
    });

    closeModalButton = document.getElementById('closeModalButton'); // Крестик для закрытия модального окна

    if (closeModalButton) {
        closeModalButton.addEventListener('click', function () {
            modalOverlay.style.display = 'none';
            document.body.style.overflow = 'auto'; // Восстанавливаем прокрутку основного контента
        });
    }

    modalOverlay = document.getElementById('modalOverlay');

    // Закрытие модального окна при клике на фон
    modalOverlay.addEventListener('click', function (event) {
        if (event.target === modalOverlay) {
            modalOverlay.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    });
}

function populateEditPostModal() {
    // Получаем данные поста
    const post = {
        title: document.querySelector('.post-title').innerText,
        imageUrl: document.querySelector('.post-image').src,
        content: document.querySelector('.post-content').innerText,
        tags: Array.from(document.querySelectorAll('.post-tags .tag')).map(tag => ({
            uuid: tag.getAttribute('href').split('/').pop(),
            title: tag.innerText
        }))
    };

    // Находим элементы формы
    const titleInput = document.getElementById('post-title');
    const imageUrlInput = document.getElementById('post-imageUrl');
    const contentTextarea = document.getElementById('post-content');
    const tagCheckboxes = document.querySelectorAll('.tag-checkbox');

    // Заполняем поля данными из поста
    titleInput.value = post.title;
    imageUrlInput.value = post.imageUrl;
    contentTextarea.value = post.content;

    autoResize(contentTextarea);

    contentTextarea.addEventListener('input', () => autoResize(contentTextarea));

    // Активируем чекбоксы тегов из тегов поста
    tagCheckboxes.forEach(checkbox => {
        checkbox.checked = post.tags.some(tag => tag.uuid === checkbox.value);
    });
}

/**
 * Установить высоту textarea согласно контенту
 * @param textarea
 */
function autoResize(textarea) {
    textarea.style.height = 'auto'; // Сбрасываем высоту
    textarea.style.height = textarea.scrollHeight + 'px'; // Устанавливаем новую высоту
}