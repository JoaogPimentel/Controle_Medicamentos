// Cliente HTTP central da API. Centraliza o envio do token JWT em todas as
// chamadas autenticadas e o tratamento de sessão expirada (401).

const TOKEN_KEY = 'token'
const USUARIO_KEY = 'usuario'

export function getToken() {
    return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
    if (token) localStorage.setItem(TOKEN_KEY, token)
}

/** Limpa os dados de autenticação do cliente (token + usuário). */
export function clearAuth() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USUARIO_KEY)
}

/**
 * Wrapper de fetch que injeta o header `Authorization: Bearer <token>` e
 * trata o 401 (token ausente/expirado) limpando a sessão e voltando ao login.
 */
export async function apiFetch(url, options = {}) {
    const headers = { ...(options.headers || {}) }
    const token = getToken()
    if (token) headers['Authorization'] = `Bearer ${token}`

    const res = await fetch(url, { ...options, headers })

    if (res.status === 401) {
        clearAuth()
        if (window.location.pathname !== '/login') {
            window.location.href = '/login'
        }
        throw new Error('Sessão expirada. Faça login novamente.')
    }

    return res
}
