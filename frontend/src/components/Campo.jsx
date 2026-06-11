// Campo de formulário reutilizável: rótulo + controle (input, select ou
// textarea). Centraliza a estrutura `.campo` repetida em todos os formulários.
//
// Uso:
//   <Campo label="Nome" id="nome" value={nome} onChange={...} />
//   <Campo as="select" label="Forma" id="forma" value={...} onChange={...}>
//       <option value="">Selecione...</option>
//   </Campo>

export default function Campo({ label, id, as = 'input', className = '', children, ...props }) {
    return (
        <div className={`campo${className ? ` ${className}` : ''}`}>
            {label && <label htmlFor={id}>{label}</label>}
            {as === 'select' ? (
                <select id={id} {...props}>{children}</select>
            ) : as === 'textarea' ? (
                <textarea id={id} {...props} />
            ) : (
                <input id={id} {...props} />
            )}
        </div>
    )
}
