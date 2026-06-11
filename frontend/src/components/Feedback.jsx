// Mensagem de feedback reutilizável (sucesso ou erro). Substitui os blocos
// `.mensagem-sucesso` / `.alerta-erro` repetidos nas telas. Renderiza nada
// quando não há mensagem.

export default function Feedback({ tipo = 'erro', children }) {
    if (!children) return null
    return (
        <div className={tipo === 'sucesso' ? 'mensagem-sucesso' : 'alerta-erro'}>
            {children}
        </div>
    )
}
