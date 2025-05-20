import React, { useEffect, useState } from 'react';
import { getChatList } from '../services/api';
import { Link } from 'react-router-dom';

const ChatList = () => {
    const [chats, setChats] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        const fetchChats = async () => {
            try {
                setLoading(true);
                const data = await getChatList();
                setChats(data);
                setError(null);
            } catch (error) {
                console.error('Ошибка загрузки чатов:', error);
                setError('Не удалось загрузить список чатов. Пожалуйста, попробуйте позже.');
            } finally {
                setLoading(false);
            }
        };
        fetchChats();
    }, []);

    const filteredChats = chats.filter(chat =>
        chat.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const getChatIcon = (type) => {
        switch (type?.toLowerCase()) {
            case 'group':
                return (
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
                    </svg>
                );
            case 'channel':
                return (
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"></path>
                    </svg>
                );
            case 'private':
                return (
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                    </svg>
                );
            default:
                return (
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
                    </svg>
                );
        }
    };

    const getChatAvatar = (chat) => {
        if (chat.avatarUrl) {
            return (
                <img
                    src={chat.avatarUrl}
                    alt={`${chat.name} avatar`}
                    className="w-12 h-12 rounded-full object-cover"
                />
            );
        } else {
            const initials = chat.name
                .split(' ')
                .map((n) => n[0])
                .join('')
                .substring(0, 2)
                .toUpperCase();

            const stringToColor = (str) => {
                let hash = 0;
                for (let i = 0; i < str.length; i++) {
                    hash = str.charCodeAt(i) + ((hash << 5) - hash);
                }
                let color = '#';
                for (let i = 0; i < 3; i++) {
                    const value = (hash >> (i * 8)) & 0xFF;
                    color += ('00' + value.toString(16)).substr(-2);
                }
                return color;
            };

            const bgColor = stringToColor(chat.name);

            return (
                <div
                    className="w-12 h-12 rounded-full flex items-center justify-center text-white font-medium text-lg"
                    style={{ backgroundColor: bgColor }}
                >
                    {initials}
                </div>
            );
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-gray-50">
                <div className="text-center p-8 max-w-md w-full bg-white rounded-lg shadow-lg">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mx-auto"></div>
                    <p className="mt-4 text-lg text-gray-700">Загрузка списка чатов...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-gray-50">
                <div className="text-center p-8 max-w-md w-full bg-white rounded-lg shadow-lg">
                    <div className="bg-red-100 p-4 rounded-lg mb-4">
                        <svg className="w-8 h-8 text-red-500 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                        </svg>
                    </div>
                    <h2 className="text-xl font-semibold text-red-600 mb-2">Ошибка</h2>
                    <p className="text-gray-700">{error}</p>
                    <button
                        onClick={() => window.location.reload()}
                        className="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors"
                    >
                        Попробовать снова
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-gray-50 min-h-screen p-4 md:p-8">
            <div className="max-w-4xl mx-auto">
                <div className="bg-white rounded-xl shadow-md overflow-hidden mb-6">
                    <div className="p-6">
                        <div className="flex items-center justify-between mb-6">
                            <div className="flex items-center">
                                <svg className="w-8 h-8 text-blue-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 8h2a2 2 0 012 2v6a2 2 0 01-2 2h-2v4l-4-4H9a1.994 1.994 0 01-1.414-.586m0 0L11 14h4a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2v4l.586-.586z"></path>
                                </svg>
                                <h1 className="text-2xl font-bold text-gray-800">Управление чатами</h1>
                            </div>
                            <span className="px-3 py-1 bg-blue-100 text-blue-800 text-sm font-medium rounded-full">
                                {chats.length} {getNumEnding(chats.length, ['чат', 'чата', 'чатов'])}
                            </span>
                        </div>

                        <div className="relative mb-6">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                                </svg>
                            </div>
                            <input
                                type="text"
                                placeholder="Поиск чатов..."
                                className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 transition-colors"
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                        </div>

                        {filteredChats.length === 0 ? (
                            <div className="text-center py-10">
                                <svg className="w-16 h-16 text-gray-300 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"></path>
                                </svg>
                                <p className="mt-4 text-gray-500">
                                    {searchTerm ? 'По вашему запросу ничего не найдено' : 'Список чатов пуст'}
                                </p>
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {filteredChats.map(chat => (
                                    <Link
                                        key={chat.id}
                                        to={`/chats/${chat.id}`}
                                        className="block"
                                    >
                                        <div className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-blue-50 transition-colors duration-200">
                                            <div className="flex-shrink-0 mr-4">
                                                {getChatAvatar(chat)}
                                            </div>
                                            <div className="flex-grow min-w-0">
                                                <div className="flex items-center mt-1">
                                                    <div className="text-sm text-gray-500 truncate flex items-center">
                                                        <span className="mr-2">{getChatIcon(chat.type)}</span>
                                                        <span>{chat.type === 'channel' ? 'Канал' : chat.type === 'group' ? 'Группа' : 'Приватный чат'}</span>
                                                    </div>
                                                </div>
                                                {chat.membersCount && (
                                                    <div className="flex items-center mt-1">
                                                        <svg className="w-4 h-4 text-gray-400 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path>
                                                        </svg>
                                                        <span className="text-xs text-gray-500">
                                                            {chat.membersCount} {getNumEnding(chat.membersCount, ['участник', 'участника', 'участников'])}
                                                        </span>
                                                    </div>
                                                )}
                                            </div>
                                            <div className="flex-shrink-0 ml-4">
                                                <div className="p-2 rounded-full bg-gray-100 hover:bg-gray-200 transition-colors">
                                                    <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7"></path>
                                                    </svg>
                                                </div>
                                            </div>
                                        </div>
                                    </Link>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

function getNumEnding(number, endings) {
    const cases = [2, 0, 1, 1, 1, 2];
    return endings[(number % 100 > 4 && number % 100 < 20) ? 2 : cases[Math.min(number % 10, 5)]];
}

export default ChatList;