import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getUser } from '../services/api';

const User = () => {
    const { userId } = useParams();
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchUser = async () => {
            try {
                setLoading(true);
                const data = await getUser(userId);
                setUser(data);
                setError(null);
            } catch (error) {
                console.error('Ошибка загрузки пользователя:', error);
                setError('Не удалось загрузить информацию о пользователе. Пожалуйста, попробуйте еще раз.');
            } finally {
                setLoading(false);
            }
        };
        fetchUser();
    }, [userId]);

    const handleBack = () => {
        navigate(-1);
    };

    if (loading) return (
        <div className="flex justify-center items-center min-h-screen bg-gray-100">
            <div className="p-6 bg-white rounded-lg shadow-xl text-center">
                <svg className="animate-spin h-8 w-8 text-[#3366ff] mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <p className="text-lg font-medium text-gray-700">Загрузка информации о пользователе...</p>
            </div>
        </div>
    );

    if (error) return (
        <div className="flex justify-center items-center min-h-screen bg-red-50 p-4">
            <div className="bg-white p-8 rounded-lg shadow-xl text-center max-w-md">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 bg-red-600 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <h2 className="text-2xl font-semibold text-red-700 mb-3">Ошибка</h2>
                <p className="text-md text-gray-600">{error}</p>
                <button
                    onClick={handleBack}
                    className="mt-6 inline-flex items-center px-6 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition duration-150 ease-in-out"
                >
                    Назад
                </button>
            </div>
        </div>
    );

    if (!user) return (
        <div className="flex justify-center items-center min-h-screen bg-gray-100 p-4">
            <div className="bg-white p-8 rounded-lg shadow-xl text-center max-w-md">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 bg-red-600 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <h2 className="text-2xl font-semibold text-gray-700 mb-3">Пользователь не найден</h2>
                <p className="text-md text-gray-600">Не удалось найти информацию по указанному ID.</p>
                <button
                    onClick={handleBack}
                    className="mt-6 inline-flex items-center px-6 py-2 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition duration-150 ease-in-out"
                >
                    Назад
                </button>
            </div>
        </div>
    );


    return (
        <div className="min-h-screen bg-gray-100 py-8 px-4 sm:px-6 lg:px-8 flex flex-col items-center">
            <div className="w-full max-w-2xl">
                <button
                    onClick={handleBack}
                    className="mb-6 inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition duration-150 ease-in-out group"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2 text-[#3366ff] group-hover:text-blue-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Назад
                </button>

                <div className="bg-white shadow-xl rounded-lg overflow-hidden">
                    <div className="bg-[#3366ff] p-4 sm:p-6"> {}
                        <h2 className="text-2xl sm:text-3xl font-bold text-white text-center">
                            Информация о пользователе
                        </h2>
                    </div>

                    <div className="p-6 sm:p-8 space-y-5">
                        <div className="flex flex-col sm:flex-row sm:justify-between py-3 border-b border-gray-200">
                            <span className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-1 sm:mb-0">ID:</span>
                            <span className="text-md text-gray-800 font-medium">{user.id}</span>
                        </div>
                        <div className="flex flex-col sm:flex-row sm:justify-between py-3 border-b border-gray-200">
                            <span className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-1 sm:mb-0">Имя:</span>
                            <span className="text-md text-gray-800">{user.firstName || <span className="text-gray-400 italic">Не указано</span>}</span>
                        </div>
                        <div className="flex flex-col sm:flex-row sm:justify-between py-3 border-b border-gray-200">
                            <span className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-1 sm:mb-0">Фамилия:</span>
                            <span className="text-md text-gray-800">{user.lastName || <span className="text-gray-400 italic">Не указано</span>}</span>
                        </div>
                        <div className="flex flex-col sm:flex-row sm:justify-between py-3 border-b border-gray-200">
                            <span className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-1 sm:mb-0">Имя пользователя:</span>
                            <span className="text-md text-gray-800">{user.username ? `@${user.username}` : <span className="text-gray-400 italic">Не указано</span>}</span>
                        </div>
                        <div className="flex flex-col sm:flex-row sm:justify-between py-3">
                            <span className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-1 sm:mb-0">Дата добавления:</span>
                            <span className="text-md text-gray-800">{user.additionDate ? new Date(user.additionDate).toLocaleDateString('ru-RU') : <span className="text-gray-400 italic">Не указана</span>}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default User;