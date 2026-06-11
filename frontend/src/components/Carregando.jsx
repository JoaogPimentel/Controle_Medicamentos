// Indicador de carregamento reutilizável, usado nas telas que buscam dados
// de forma assíncrona.

export default function Carregando({ children = 'Carregando…' }) {
    return <p className="vazio">{children}</p>
}
