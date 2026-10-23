import { useState, useEffect } from 'react'

export default function App() {
    const [message, setMessage] = useState('Chargement...')

    useEffect(() => {
        fetch('api/v1/test')
            .then((response) => response.json())
            .then((data) => setMessage(data.message))
            .catch(() => setMessage('Erreur : le backend ne répond pas'))
    }, [])

    return (
        <main className="p-8">
            <h1 className="text-3xl font-bold">Restox</h1>
            <p className="mt-4 text-gray-700">{message}</p>
        </main>
    )
}
