/**
 * Показать уведомление
 *
 * @param {string} message Текст сообщения
 * @param {'success'|'error'} type Тип уведомления
 * @param {number} [duration=3000] Длительность показа в ms
 */
function showNotification(message, type = 'success', duration = 3000) {
    const container = document.querySelector('.notification-container');
    const notification = document.createElement('div');

    notification.className = `notification notification--${type}`;
    notification.innerHTML = `
        <span class="notification__icon">${type === 'success' ? '✓' : '⚠'}</span>
        <span>${message}</span>
    `;

    // Удаляем предыдущие уведомления
    container.innerHTML = '';
    container.appendChild(notification);

    // Автоматическое скрытие
    setTimeout(() => {
        notification.classList.add('notification--hide');
        notification.addEventListener('animationend', () => notification.remove());
    }, duration);
}