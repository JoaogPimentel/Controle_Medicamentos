export async function login(email, senha) {
    const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, senha })
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao fazer login')
    localStorage.setItem('usuario', JSON.stringify(data))
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

export async function logout() {
    await fetch('/api/auth/logout')
    localStorage.removeItem('usuario')
    localStorage.removeItem('emailSalvo')
}
