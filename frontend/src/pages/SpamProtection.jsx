import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getSpamProtectionSettings, updateSpamProtectionSettings } from '../services/api';
import { ArrowLeftIcon, ShieldCheckIcon, UserIcon, XMarkIcon, PlusIcon } from '@heroicons/react/24/outline';

const SpamProtection = () => {
    const { chatId } = useParams();
    const navigate = useNavigate();
    const [settings, setSettings] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [updating, setUpdating] = useState(false);
    const [newExclusion, setNewExclusion] = useState('');
    const [localCoolDownPeriod, setLocalCoolDownPeriod] = useState(0);
    const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

    useEffect(() => {
        const fetchSettings = async () => {
            if (!chatId) {
                setError('Идентификатор чата не указан');
                setLoading(false);
                return;
            }
            try {
                setLoading(true);
                const data = await getSpamProtectionSettings(chatId);
                setSettings(data);
                setLocalCoolDownPeriod(data.coolDownPeriod || 0);
                setError(null);
            } catch (error) {
                console.error('Ошибка загрузки настроек защиты от спама:', error.response?.data || error);
                setError('Не удалось загрузить настройки защиты от спама');
            } finally {
                setLoading(false);
            }
        };
        fetchSettings();
    }, [chatId]);

    const handleUpdateSettings = async (newSettings) => {
        try {
            setUpdating(true);
            const updatedSettings = await updateSpamProtectionSettings(chatId, newSettings);
            setSettings(updatedSettings);
            if (newSettings.coolDownPeriod !== undefined) {
                setLocalCoolDownPeriod(newSettings.coolDownPeriod);
                setHasUnsavedChanges(false);
            }
        } catch (error) {
            console.error('Ошибка обновления настроек защиты от спама:', error);
            alert(`Ошибка при обновлении настроек: ${error.message}`);
        } finally {
            setUpdating(false);
        }
    };

    const handleToggleEnabled = () => {
        const newSettings = {
            ...settings,
            enabled: !settings.enabled
        };
        handleUpdateSettings(newSettings);
    };

    const handleCoolDownPeriodChange = (value) => {
        const newValue = parseInt(value) || 0;
        setLocalCoolDownPeriod(newValue);
        setHasUnsavedChanges(newValue !== settings.coolDownPeriod);
    };

    const handleSaveCoolDownPeriod = () => {
        const newSettings = {
            ...settings,
            coolDownPeriod: localCoolDownPeriod
        };
        handleUpdateSettings(newSettings);
    };

    const handleAddExclusion = () => {
        if (!newExclusion.trim()) return;

        const userId = newExclusion.trim();
        if (settings.exclusions.includes(userId)) {
            alert('Этот пользователь уже в списке исключений');
            return;
        }

        const newSettings = {
            ...settings,
            exclusions: [...settings.exclusions, userId]
        };
        handleUpdateSettings(newSettings);
        setNewExclusion('');
    };

    const handleRemoveExclusion = (userId) => {
        const newSettings = {
            ...settings,
            exclusions: settings.exclusions.filter(id => id !== userId)
        };
        handleUpdateSettings(newSettings);
    };

    const handleGoBack = () => {
        navigate(-1);
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-gray-50">
                <div className="text-center p-8 max-w-md w-full bg-white rounded-lg shadow-lg">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mx-auto"></div>
                    <p className="mt-4 text-lg text-gray-700">Загрузка настроек защиты от спама...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-gray-50">
                <div className="text-center p-8 max-w-md w-full bg-white rounded-lg shadow-lg">
                    <div className="bg-red-100 p-4 rounded-lg mb-4">
                        <svg className="w-8 h-8 text-red-500 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                    </div>
                    <h2 className="text-xl font-semibold text-red-600 mb-2">Ошибка</h2>
                    <p className="text-gray-700">{error}</p>
                    <button
                        onClick={handleGoBack}
                        className="mt-6 flex items-center justify-center px-4 py-2 text-sm font-medium text-blue-600 bg-blue-100 rounded-md hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    >
                        <ArrowLeftIcon className="w-5 h-5 mr-2" />
                        Назад
                    </button>
                </div>
            </div>
        );
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
                            Защита от спама
                        </h1>
                    </div>

                    <div className="bg-blue-50 border-l-4 border-blue-500 p-4 mb-6 rounded">
                        <div className="flex">
                            <div className="flex-shrink-0">
                                <ShieldCheckIcon className="h-5 w-5 text-blue-500" />
                            </div>
                            <div className="ml-3">
                                <p className="text-sm text-blue-700">
                                    При повторной отправке сообщения до истечения периода ожидания, сообщение будет автоматически удалено.
                                </p>
                            </div>
                        </div>
                    </div>

                    {settings && (
                        <div className="space-y-6">
                            {/* Включение/отключение защиты от спама */}
                            <div className="flex items-center justify-between p-6 border border-gray-200 rounded-lg hover:shadow-md transition-shadow duration-200">
                                <div className="flex items-center space-x-3">
                                    <div className={`flex-shrink-0 rounded-full p-2.5 ${settings.enabled ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-600'}`}>
                                        <ShieldCheckIcon className="w-6 h-6" />
                                    </div>
                                    <div>
                                        <h3 className="font-medium text-gray-800">Защита от спама</h3>
                                        <p className={`text-sm ${settings.enabled ? 'text-green-600' : 'text-gray-600'}`}>
                                            {settings.enabled ? 'Включена' : 'Отключена'}
                                        </p>
                                    </div>
                                </div>
                                <div>
                                    <label className="inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            className="sr-only peer"
                                            checked={settings.enabled}
                                            onChange={handleToggleEnabled}
                                            disabled={updating}
                                        />
                                        <div className="relative w-11 h-6 bg-gray-200 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-0.5 after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-green-600"></div>
                                    </label>
                                </div>
                            </div>

                            {settings.enabled && (
                                <>
                                    {/* Период ожидания */}
                                    <div className="p-6 border border-gray-200 rounded-lg">
                                        <div className="flex items-center justify-between mb-4">
                                            <div>
                                                <h3 className="font-medium text-gray-800">Период ожидания</h3>
                                                <p className="text-sm text-gray-600">
                                                    Минимальное время в секундах между отправкой сообщений одним пользователем
                                                </p>
                                            </div>
                                        </div>
                                        <div className="flex items-center space-x-3">
                                            <input
                                                type="number"
                                                min="0"
                                                max="3600"
                                                value={localCoolDownPeriod}
                                                onChange={(e) => handleCoolDownPeriodChange(e.target.value)}
                                                disabled={updating}
                                                className="block w-24 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                                            />
                                            <span className="text-sm text-gray-600">секунд</span>
                                            <button
                                                onClick={handleSaveCoolDownPeriod}
                                                disabled={updating || !hasUnsavedChanges}
                                                className={`px-4 py-2 text-sm font-medium rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 ${
                                                    hasUnsavedChanges
                                                        ? 'bg-blue-500 text-white hover:bg-blue-600'
                                                        : 'bg-gray-200 text-gray-500 cursor-not-allowed'
                                                }`}
                                            >
                                                Сохранить
                                            </button>
                                        </div>
                                    </div>

                                    {/* Исключения */}
                                    <div className="p-6 border border-gray-200 rounded-lg">
                                        <div className="mb-4">
                                            <h3 className="font-medium text-gray-800 mb-2">Исключения</h3>
                                            <p className="text-sm text-gray-600">
                                                Пользователи, на которых не распространяются ограничения защиты от спама
                                            </p>
                                        </div>

                                        {/* Добавление нового исключения */}
                                        <div className="flex items-center space-x-3 mb-4">
                                            <input
                                                type="text"
                                                placeholder="ID пользователя"
                                                value={newExclusion}
                                                onChange={(e) => setNewExclusion(e.target.value)}
                                                onKeyPress={(e) => e.key === 'Enter' && handleAddExclusion()}
                                                disabled={updating}
                                                className="flex-1 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                                            />
                                            <button
                                                onClick={handleAddExclusion}
                                                disabled={updating || !newExclusion.trim()}
                                                className="flex items-center px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                                            >
                                                <PlusIcon className="w-4 h-4 mr-1" />
                                                Добавить
                                            </button>
                                        </div>

                                        {/* Список исключений */}
                                        {settings.exclusions && settings.exclusions.length > 0 ? (
                                            <div className="space-y-2">
                                                {settings.exclusions.map((userId, index) => (
                                                    <div
                                                        key={index}
                                                        className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                                                    >
                                                        <div className="flex items-center space-x-3">
                                                            <div className="flex-shrink-0 rounded-full p-2 bg-blue-100 text-blue-600">
                                                                <UserIcon className="w-4 h-4" />
                                                            </div>
                                                            <span className="text-sm font-medium text-gray-800">
                                                                {userId}
                                                            </span>
                                                        </div>
                                                        <button
                                                            onClick={() => handleRemoveExclusion(userId)}
                                                            disabled={updating}
                                                            className="text-red-500 hover:text-red-700 focus:outline-none disabled:opacity-50"
                                                        >
                                                            <XMarkIcon className="w-5 h-5" />
                                                        </button>
                                                    </div>
                                                ))}
                                            </div>
                                        ) : (
                                            <div className="text-center py-8 text-gray-500">
                                                <UserIcon className="w-12 h-12 mx-auto mb-3 text-gray-300" />
                                                <p className="text-sm">Исключения не добавлены</p>
                                            </div>
                                        )}
                                    </div>
                                </>
                            )}

                            {updating && (
                                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                                    <div className="bg-white p-6 rounded-lg shadow-xl">
                                        <div className="flex items-center">
                                            <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-500 mr-3"></div>
                                            <span className="text-gray-700">Сохранение настроек...</span>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SpamProtection;