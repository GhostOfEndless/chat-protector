import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getTextModerationSettings, updateTextModerationSettings } from '../services/api';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';

const Moderation = () => {
    const { chatId } = useParams();
    const navigate = useNavigate();
    const [settings, setSettings] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [activeTab, setActiveTab] = useState('general');

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
            const currentFilterSettings = settings[filterType] || {};
            const updatedSettings = {
                enabled: !currentFilterSettings.enabled,
                exclusionMode: currentFilterSettings.exclusionMode || 'BLACK_LIST',
                exclusions: currentFilterSettings.exclusions || []
            };
            await updateTextModerationSettings(chatId, filterType, updatedSettings);
            setSettings(prev => ({
                ...prev,
                [filterType]: {
                    ...prev[filterType],
                    enabled: updatedSettings.enabled
                }
            }));
        } catch (error) {
            console.error('Ошибка обновления модерации:', error);
            alert(`Ошибка при обновлении настроек для ${getFilterName(filterType)}: ${error.message}`);
        }
    };

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

    const getFilterIcon = (filterType) => {
        const icons = {
            emails: ( <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"> <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path> </svg> ),
            tags: ( <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"> <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"></path> </svg> ),
            mentions: ( <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"> <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207"></path> </svg> ),
            links: ( <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"> <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"></path> </svg> ),
            'phone-numbers': ( <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"> <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path> </svg> ),
            'bot-commands': ( <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"> <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"></path> </svg> ),
            'custom-emojis': ( <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"> <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path> </svg> )
        };
        return icons[filterType] || ( <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"> <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4"></path> </svg> );
    };

    const handleGoBack = () => {
        navigate(-1);
    };

    if (loading) {
        return ( <div className="flex items-center justify-center min-h-screen bg-gray-50"> <div className="text-center p-8 max-w-md w-full bg-white rounded-lg shadow-lg"> <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mx-auto"></div> <p className="mt-4 text-lg text-gray-700">Загрузка настроек модерации...</p> </div> </div> );
    }

    if (error) {
        return ( <div className="flex items-center justify-center min-h-screen bg-gray-50"> <div className="text-center p-8 max-w-md w-full bg-white rounded-lg shadow-lg"> <div className="bg-red-100 p-4 rounded-lg mb-4"> <svg className="w-8 h-8 text-red-500 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"> <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path> </svg> </div> <h2 className="text-xl font-semibold text-red-600 mb-2">Ошибка</h2> <p className="text-gray-700">{error}</p> <button onClick={handleGoBack} className="mt-6 flex items-center justify-center px-4 py-2 text-sm font-medium text-blue-600 bg-blue-100 rounded-md hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500" > <ArrowLeftIcon className="w-5 h-5 mr-2" /> Назад </button> </div> </div> );
    }

    return (
        <div className="bg-gray-50 min-h-screen p-4 md:p-8">
            <div className="max-w-4xl mx-auto bg-white rounded-xl shadow-md overflow-hidden">
                <div className="p-6 md:p-8">
                    <div className="flex items-center mb-6">
                        <button
                            onClick={handleGoBack}
                            className="flex items-center mr-4 text-blue-500 hover:text-blue-700 focus:outline-none"
                        >
                            <ArrowLeftIcon className="w-5 h-5 mr-1" />
                            <span>Назад</span>
                        </button>
                        <h1 className="text-2xl font-bold text-gray-800 absolute left-1/2 transform -translate-x-1/2">
                            Настройки модерации чата
                        </h1>
                    </div>

                    <div className="bg-blue-50 border-l-4 border-blue-500 p-4 mb-6 rounded">
                        <div className="flex">
                            <div className="flex-shrink-0">
                                <svg className="h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                            </div>
                            <div className="ml-3">
                                <p className="text-sm text-blue-700">
                                    Включенный переключатель означает, что функция <strong>запрещена</strong> в чате.
                                    Выключенный переключатель означает, что функция <strong>разрешена</strong> в чате.
                                </p>
                            </div>
                        </div>
                    </div>

                    <div className="border-b border-gray-200 mb-6">
                        <nav className="flex space-x-4" aria-label="Tabs">
                            <button
                                onClick={() => setActiveTab('general')}
                                className={`px-3 py-2 text-sm font-medium rounded-t-lg ${
                                    activeTab === 'general'
                                        ? 'text-blue-600 border-b-2 border-blue-500 bg-blue-50'
                                        : 'text-gray-500 hover:text-blue-600 hover:border-blue-200 hover:bg-blue-50'
                                }`}
                            >
                                Общие
                            </button>
                        </nav>
                    </div>

                    {activeTab === 'general' && (
                        <>
                            {Object.keys(settings).length === 0 && !loading ? (
                                <div className="text-center py-8">
                                    <svg className="w-16 h-16 text-gray-300 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                                    <p className="mt-4 text-gray-500">Настройки модерации не найдены для этого чата.</p>
                                    <p className="mt-1 text-sm text-gray-400">ID чата: {chatId}</p>
                                </div>
                            ) : Object.keys(settings).filter(key => allowedFilterTypes.includes(key)).length === 0 && !loading ? (
                                <div className="text-center py-8">
                                    <svg className="w-16 h-16 text-gray-300 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                                    <p className="mt-4 text-gray-500">Нет поддерживаемых настроек модерации.</p>
                                    <p className="mt-2 text-sm text-gray-400">Доступные фильтры: {allowedFilterTypes.join(', ')}</p>
                                </div>
                            ) : (
                                <div className="grid gap-4 md:grid-cols-2">
                                    {Object.keys(settings)
                                        .filter(key => allowedFilterTypes.includes(key))
                                        .map(filterType => (
                                            <div
                                                key={filterType}
                                                className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:shadow-md transition-shadow duration-200"
                                            >
                                                <div className="flex items-center space-x-3">
                                                    <div className={`flex-shrink-0 rounded-full p-2.5 ${settings[filterType]?.enabled ? 'bg-red-100 text-red-600' : 'bg-green-100 text-green-600'}`}>
                                                        {getFilterIcon(filterType)}
                                                    </div>
                                                    <div>
                                                        <h3 className="font-medium text-gray-800">{getFilterName(filterType)}</h3>
                                                        <p className={`text-sm ${settings[filterType]?.enabled ? 'text-red-600' : 'text-green-600'}`}>
                                                            {settings[filterType]?.enabled ? 'Запрещено' : 'Разрешено'}
                                                        </p>
                                                    </div>
                                                </div>
                                                <div>
                                                    <label className="inline-flex items-center cursor-pointer">
                                                        <input
                                                            type="checkbox"
                                                            className="sr-only peer"
                                                            checked={settings[filterType]?.enabled || false}
                                                            onChange={() => handleUpdate(filterType)}
                                                        />
                                                        <div className="relative w-11 h-6 bg-gray-200 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-0.5 after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-red-600"></div>
                                                    </label>
                                                </div>
                                            </div>
                                        ))}
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Moderation;