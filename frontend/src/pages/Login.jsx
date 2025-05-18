import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authenticate } from '../services/api';

const Login = () => {
    const [login, setLogin] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const data = await authenticate(login, password);
            // Сохраняем токен в localStorage
            localStorage.setItem('token', data.token);
            console.log('Успешный вход:', data);

            navigate('/chats');
        } catch (err) {
            setError('Ошибка входа: ' + (err.response?.data?.message || err.message));
        }
    };

    return (
        <div className="login-container">
            <h2>Вход в админ-панель</h2>
            <form onSubmit={handleSubmit} className="login-form">
                <div className="form-group">
                    <label htmlFor="login">Логин:</label>
                    <input
                        type="text"
                        id="login"
                        placeholder="Введите логин"
                        value={login}
                        onChange={(e) => setLogin(e.target.value)}
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="password">Пароль:</label>
                    <input
                        type="password"
                        id="password"
                        placeholder="Введите пароль"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>
                <button type="submit" className="login-button">Войти</button>
                {error && <p className="error-message">{error}</p>}
            </form>
        </div>
    );
};

export default Login;