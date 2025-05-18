import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getTextModerationSettings, updateTextModerationSettings } from '../services/api';

const Moderation = () => {
    const { chatId } = useParams();
    const [settings, setSettings] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Список допустимых типов фильтров
    const allowedFilterTypes = [
        'emails', 'tags', 'mentions', 'links',
        'phone-numbers', 'bot-commands', 'custom-emojis'
    ];

    useEffect(() => {
        const fetchSettings = async () => {
            if (!chatId) {
                setError('Идентификатор чата не указан');
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                const data = await getTextModerationSettings(chatId);
                console.log('Полученные настройки модерации:', data);

                // Преобразуем ключи xxxFilterSettings в допустимые типы
                const normalizedSettings = Object.keys(data).reduce((acc, key) => {
                    let normalizedKey = key;
                    if (key === 'linksFilterSettings') normalizedKey = 'links';
                    if (key === 'botCommandsFilterSettings') normalizedKey = 'bot-commands';
                    if (key === 'customEmojisFilterSettings') normalizedKey = 'custom-emojis';
                    if (key === 'emailsFilterSettings') normalizedKey = 'emails';
                    if (key === 'mentionsFilterSettings') normalizedKey = 'mentions';
                    if (key === 'phoneNumbersFilterSettings') normalizedKey = 'phone-numbers';
                    if (key === 'tagsFilterSettings') normalizedKey = 'tags';
                    acc[normalizedKey] = data[key];
                    return acc;
                }, {});

                console.log('Нормализованные настройки:', normalizedSettings);
                setSettings(normalizedSettings);
                setError(null);
            } catch (error) {
                console.error('Ошибка загрузки настроек модерации:', error.response?.data || error);
                setError('Не удалось загрузить настройки модерации');
            } finally {
                setLoading(false);
            }
        };
        fetchSettings();
    }, [chatId]);

    const handleUpdate = async (filterType) => {
        try {
            if (!allowedFilterTypes.includes(filterType)) {
                throw new Error(`Недопустимый тип фильтра: ${filterType}`);
            }

            const updatedSettings = {
                enabled: !settings[filterType]?.enabled,
                exclusionMode: settings[filterType]?.exclusionMode || 'BLACK_LIST',
                exclusions: settings[filterType]?.exclusions || []
            };

            await updateTextModerationSettings(chatId, filterType, updatedSettings);

            setSettings(prev => ({
                ...prev,
                [filterType]: {
                    ...prev[filterType],
                    enabled: !prev[filterType]?.enabled
                }
            }));
        } catch (error) {
            console.error('Ошибка обновления модерации:', error);
            alert(`Ошибка при обновлении настроек для ${getFilterName(filterType)}: ${error.message}`);
        }
    };

    if (loading) {
        console.log('Состояние: загрузка');
        return <div>Загрузка настроек модерации...</div>;
    }

    if (error) {
        console.log('Состояние: ошибка', error);
        return <div>{error}</div>;
    }

    console.log('Состояние: рендеринг, настройки:', settings);

    return (
        <div className="moderation-container">
            <h2>Настройки модерации чата</h2>

            {Object.keys(settings).length === 0 ? (
                <p>Настройки модерации не найдены</p>
            ) : Object.keys(settings).filter(key => allowedFilterTypes.includes(key)).length === 0 ? (
                <p>Нет поддерживаемых настроек модерации. Доступные фильтры: {allowedFilterTypes.join(', ')}</p>
            ) : (
                <div className="filter-list">
                    {Object.keys(settings)
                        .filter(key => allowedFilterTypes.includes(key))
                        .map(filterType => (
                            <div key={filterType} className="filter-item">
                                <span className="filter-name">{getFilterName(filterType)}: </span>
                                <span className={`filter-status ${settings[filterType]?.enabled ? 'enabled' : 'disabled'}`}>
                                    {settings[filterType]?.enabled ? 'Включено' : 'Выключено'}
                                </span>
                                <button
                                    onClick={() => handleUpdate(filterType)}
                                    className="toggle-button"
                                >
                                    Переключить
                                </button>
                            </div>
                        ))}
                </div>
            )}
        </div>
    );
};

// Вспомогательная функция для отображения русских названий фильтров
const getFilterName = (filterType) => {
    const filterNames = {
        emails: 'Электронные адреса',
        tags: 'Теги',
        mentions: 'Упоминания',
        links: 'Ссылки',
        'phone-numbers': 'Телефонные номера',
        'bot-commands': 'Команды ботов',
        'custom-emojis': 'Кастомные эмодзи'
    };

    return filterNames[filterType] || filterType;
};

export default Moderation;