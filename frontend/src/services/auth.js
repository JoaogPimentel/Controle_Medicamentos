import { apiFetch, setToken, clearAuth, API_BASE } from './api'

export async function login(email, senha) {
    const res = await fetch(API_BASE + '/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, senha })
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao fazer login')
    // Resposta JWT: { token, usuario }. Guarda o token e o usuário separados —
    // as páginas continuam lendo `localStorage.usuario` como objeto do usuário.
    setToken(data.token)
    localStorage.setItem('usuario', JSON.stringify(data.usuario))
    return data.usuario
}

export async function cadastrar(body) {
    const res = await fetch(API_BASE + '/api/auth/cadastrar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    const data = await res.json()
    if (res.status !== 201) throw new Error(data.erro || 'Erro ao criar conta.')
    return data
}

export async function logout() {
    // Auth stateless: o logout é best-effort. Mesmo que a chamada falhe,
    // limpamos o token no cliente.
    try {
        await apiFetch('/api/auth/logout')
    } catch {
        // sessão já pode estar expirada; segue limpando
    }
    clearAuth()
    localStorage.removeItem('emailSalvo')
}
