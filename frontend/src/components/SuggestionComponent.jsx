import "./SuggestionComponent.css";
import React, { useState, useEffect } from 'react';

function SuggestionComponent() {
    const [sugerencias, setSugerencias] = useState([]);
    const [query, setQuery] = useState('');
    const [filteredSuggestions, setFilteredSuggestions] = useState([]);

    // Función para hacer la solicitud HTTP
    const fetchSugerencias = async () => {
        const response = await fetch(`/sugerencias?searchTerm=${query}`);
        const data = await response.json();
        setSugerencias(data);
    };

    // Llamada a fetchSugerencias cuando el query cambia
    useEffect(() => {
        if (query.trim() !== '') {
            fetchSugerencias();
        } else {
            setSugerencias([]);  // Si la búsqueda está vacía, limpiar sugerencias
        }
    }, [query]);  // Solo se ejecuta cuando 'query' cambia

    // Maneja los cambios en el campo de búsqueda
    const handleSearchChange = (event) => {
        const searchQuery = event.target.value;
        setQuery(searchQuery);  // Actualiza el valor de la búsqueda
    };

    useEffect(() => {
        // Filtra las sugerencias basadas en la búsqueda
        const normalizedQuery = query.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
        if (normalizedQuery === '') {
            setFilteredSuggestions([]);  // Si la búsqueda está vacía, limpiar las sugerencias
        } else {
            const filtered = sugerencias.filter(option => {
                const normalizedOption = option.titulo.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
                return normalizedOption.includes(normalizedQuery); // Filtra las sugerencias que coinciden
            });
            setFilteredSuggestions(filtered.slice(0, 5));  // Limita las sugerencias a las 5 primeras
        }
    }, [query, sugerencias]);  // Este efecto se ejecuta cada vez que cambian 'query' o 'sugerencias'

    return (
        <div id="searchContainer">
            <div id="searchContainerInput">
                <input
                id="buscadorObras"
                type="search"
                placeholder="Introduce Nombre del libro o artículo"
                value={query}
                onChange={handleSearchChange}
                />
                <img id="searchImg" src="/lupa.png"></img>
            </div>
            
            <div id="searchResults">
                {filteredSuggestions.map((option, index) => (
                    <div key={index} className="searchResults-reference">
                        <a className="searchResults-reference" href={`/workShow?id=${option.isbn}`}>
                            {option.titulo} 
                        </a>
                    </div>
                ))}
            </div>
        </div>
        
    );
}

export default SuggestionComponent;

