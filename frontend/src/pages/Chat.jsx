import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getChat } from '../services/api';

const Chat = () => {
    const { chatId } = useParams();
    const [chat, setChat] = useState(null);

    useEffect(() => {
        const fetchChat = async () => {
            try {
                const data = await getChat(chatId);
                setChat(data);
            } catch (error) {
                console.error('Ошибка загрузки чата:', error);
            }
        };
        fetchChat();
    }, [chatId]);

    return (
        <div>
            <h2>Чат: {chat?.name}</h2>
            <ul>
                {chat?.messages?.map(msg => (
                    <li key={msg.id}>{msg.text}</li>
                ))}
            </ul>
        </div>
    );
};

export default Chat;