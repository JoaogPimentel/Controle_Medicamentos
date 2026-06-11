// Tabela de dados reutilizável. Recebe a definição das colunas e a lista de
// dados, evitando repetir a estrutura `<table>` em cada tela.
//
// colunas: [{ chave, titulo, className?, render?(item) }]
// chaveLinha: campo único do item usado como key (ou função (item) => key).

export default function Tabela({ colunas, dados, chaveLinha, vazio = 'Nenhum item encontrado.' }) {
    if (!dados || dados.length === 0) {
        return <p className="vazio">{vazio}</p>
    }

    const keyDe = (item, i) =>
        typeof chaveLinha === 'function' ? chaveLinha(item)
        : chaveLinha ? item[chaveLinha]
        : i

    return (
        <div className="tabela-wrapper">
            <table>
                <thead>
                    <tr>{colunas.map(c => <th key={c.chave}>{c.titulo}</th>)}</tr>
                </thead>
                <tbody>
                    {dados.map((item, i) => (
                        <tr key={keyDe(item, i)}>
                            {colunas.map(c => (
                                <td key={c.chave} className={c.className}>
                                    {c.render ? c.render(item) : item[c.chave]}
                                </td>
                            ))}
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    )
}
