// Cartão de conteúdo reutilizável. Opcionalmente recebe um título e uma ação
// (ex.: botão no canto), montando o `.card-header` quando há ação.
// Usa forwardRef para permitir scroll/foco programático (ex.: editar item).

import { forwardRef } from 'react'

const Card = forwardRef(function Card({ titulo, acao, className = '', children }, ref) {
    return (
        <div className={`card${className ? ` ${className}` : ''}`} ref={ref}>
            {titulo && (acao ? (
                <div className="card-header">
                    <h2>{titulo}</h2>
                    {acao}
                </div>
            ) : (
                <h2>{titulo}</h2>
            ))}
            {children}
        </div>
    )
})

export default Card
