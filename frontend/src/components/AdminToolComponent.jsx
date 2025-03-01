import { Link } from 'react-router-dom';
import './AdminToolComponent.css'
import React, { useState } from 'react';

function AdminToolComponent() {
    // Declaramos el estado isMenuOpen para controlar la visibilidad del menú
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    // Función para alternar la visibilidad del desplegable
    const toggleMenu = () => {
        setIsMenuOpen(prevState => !prevState);
    };

    return (
        <div>
            {/* Imagen que activa el desplegable */}
            <img 
                id="devToolImg" 
                src='/destornillador.png' 
                alt="Herramientas de desarrollo" 
                onClick={toggleMenu} // Aquí se usa onClick y se pasa la función directamente
                className={isMenuOpen ? 'open' : 'closed'} 
            />

            {/* Menú desplegable */}
            {isMenuOpen && (
                <div id="devToolDropdown">
                    <ul  className={isMenuOpen ? 'open' : 'closed'}>
                        <li><Link to="/UsersList">Users List</Link></li>
                        <li><Link to="/WorkList">Writings List</Link></li>
                        <li><Link to="/CommentList">Comments List</Link></li>
                    </ul>
                </div>
            )}
        </div>
    );
}

export default AdminToolComponent;
