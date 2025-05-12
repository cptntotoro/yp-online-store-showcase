let modalOverlay;
let closeModalButton;

let contentTextarea;

let headerButton;

document.addEventListener("DOMContentLoaded", () => {
    headerButton = document.querySelector(".header__btn");

    modalOverlay = document.getElementById('modalOverlay');
    closeModalButton = document.getElementById('closeModalButton');

    contentTextarea = document.getElementById('post-content');

    if (headerButton) {
        setModalBehavior(headerButton);
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

    contentTextarea.addEventListener('input', () => autoResize(contentTextarea));

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

/**
 * Установить высоту textarea согласно контенту
 * @param textarea
 */
function autoResize(textarea) {
    textarea.style.height = 'auto'; // Сбрасываем высоту
    textarea.style.height = textarea.scrollHeight + 'px'; // Устанавливаем новую высоту
}