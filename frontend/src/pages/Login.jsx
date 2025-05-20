import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authenticate } from '../services/api';

const Login = () => {
    const [login, setLogin] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const data = await authenticate(login, password);
            localStorage.setItem('token', data.token);
            console.log('Успешный вход:', data);
            navigate('/chats');
        } catch (err) {
            setError('Ошибка входа: ' + (err.response?.data?.message || err.message));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100">
            <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8 space-y-6">
                <div className="text-center">
                    <h2 className="text-3xl font-bold text-gray-800">Система модерации Telegram</h2>
                    <p className="mt-2 text-gray-500">Войдите в свою учетную запись</p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label htmlFor="login" className="block text-sm font-medium text-gray-700 mb-1">
                            Логин
                        </label>
                        <input
                            type="text"
                            id="login"
                            placeholder="Введите логин"
                            value={login}
                            onChange={(e) => setLogin(e.target.value)}
                            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-colors"
                            required
                        />
                    </div>

                    <div>
                        <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                            Пароль
                        </label>
                        <input
                            type="password"
                            id="password"
                            placeholder="Введите пароль"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-colors"
                            required
                        />
                    </div>

                    <div>
                        <button
                            type="submit"
                            className={`w-full py-2 px-4 rounded-md text-white font-medium transition-colors 
                                ${loading
                                ? 'bg-blue-400 cursor-not-allowed'
                                : 'bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2'}`}
                            disabled={loading}
                        >
                            {loading ? 'Выполняется вход...' : 'Войти'}
                        </button>
                    </div>

                    {error && (
                        <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded">
                            <div className="flex items-center">
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-red-500" viewBox="0 0 20 20" fill="currentColor">
                                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                                </svg>
                                <p className="ml-2 text-sm text-red-700">{error}</p>
                            </div>
                        </div>
                    )}
                </form>
            </div>
        </div>
    );
};

export default Login;