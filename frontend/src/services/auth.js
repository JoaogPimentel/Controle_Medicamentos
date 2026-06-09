export async function login(email, senha) {
    const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, senha })
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao fazer login')
    return data
}

export async function cadastrar(body) {
    const res = await fetch('/api/auth/cadastrar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    const data = await res.json()
    if (res.status !== 201) throw new Error(data.erro || 'Erro ao criar conta.')
    return data
}

export function logout() {
    return fetch('/api/auth/logout')
}
