import React from 'react';
import { Link } from 'react-router-dom';
import { UsersIcon, ChatBubbleLeftRightIcon } from '@heroicons/react/24/outline';

const Home = () => {
    return (
        <div className="min-h-screen bg-gray-100 py-8 px-4 sm:px-6 lg:px-8 flex flex-col items-center justify-center">
            <div className="w-full max-w-md">
                <div className="bg-white shadow-xl rounded-lg overflow-hidden">
                    <div className="bg-[#3366ff] p-5 sm:p-6">
                        <h1 className="text-2xl sm:text-3xl font-bold text-white text-center">
                            Панель управления
                        </h1>
                    </div>
                    <div className="p-6 sm:p-8 space-y-5">
                        <Link
                            to="/users"
                            className="w-full flex items-center justify-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition duration-150 ease-in-out"
                        >
                            <UsersIcon className="h-6 w-6 mr-3" />
                            Список пользователей
                        </Link>
                        <Link
                            to="/chats"
                            className="w-full flex items-center justify-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-teal-600 hover:bg-teal-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-teal-500 transition duration-150 ease-in-out"
                        >
                            <ChatBubbleLeftRightIcon className="h-6 w-6 mr-3" />
                            Список чатов
                        </Link>
                    </div>
                </div>
                <p className="mt-6 text-center text-sm text-gray-500">
                    Выберите раздел для начала работы.
                </p>
            </div>
        </div>
    );
};

export default Home;