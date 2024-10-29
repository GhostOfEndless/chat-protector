package ru.tbank.processor.service.filter.text;

/**
 * <p><b>"Стоимость" каждого фильтра:</b></p>
 * <ul>
 *   <li><b>VERY_LOW</b> - то, что может быть обработано без стороннего API на сервере за несколько мс.</li>
 *   <li><b>LOW</b> - долгая обработка на сервере (от 25 мс до 50 мс).</li>
 *   <li><b>MEDIUM</b> - обработка с запросом к API Telegram (фильтрация Premium Emoji и стикеров).</li>
 *   <li><b>HIGH</b> - требуется стороннее API, но стоимость его использования не очень высокая.</li>
 *   <li><b>VERY_HIGH</b> - высокая стоимость обработки одного запроса на стороннем API.</li>
 * </ul>
 */
public enum FilterCost {
    VERY_LOW,
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH
}
