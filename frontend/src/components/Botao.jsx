// Botão reutilizável. Encapsula as variantes visuais (primário, secundário,
// perigo) usadas em todas as telas, evitando repetir as classes CSS.

const VARIANTES = {
    primario: 'btn-primario',
    secundario: 'btn-secundario',
    perigo: 'btn-perigo',
}

export default function Botao({ variante = 'primario', className = '', children, ...props }) {
    const classe = `${VARIANTES[variante] || VARIANTES.primario}${className ? ` ${className}` : ''}`
    return (
        <button className={classe} {...props}>
            {children}
        </button>
    )
}
