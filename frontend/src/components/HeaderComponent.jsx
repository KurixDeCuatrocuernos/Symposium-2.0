import "./HeaderComponent.css";
import SuggestionComponent from './SuggestionComponent';  // Asegúrate de importar SuggestionComponent correctamente
import React, { useEffect, useState, useRef } from 'react';
import AdminToolComponent from './AdminToolComponent'; // Asegúrate de importar tu componente

function HeaderComponent() {
    const devtool = true; // Solo por demostración, ajusta según tus necesidades.
    const adminDivRef = useRef(null);
    const userLoginRef = useRef(null);

    // Estado para controlar si se debe mostrar las herramientas del admin
    const [showAdminTools, setShowAdminTools] = useState(false);

    // Definimos la función `tools` dentro del componente
    const tools = async () => {
        const adminDiv = adminDivRef.current;
        const userLogin = userLoginRef.current;

        // Función para obtener el rol del usuario
        const fetchUserRole = async () => {
            const response = await fetch(`/getUserRole`);
            const data = await response.json();
            if (data && data.role === 'ADMIN') {
                console.log("El usuario es Administrador");
                setShowAdminTools(true); // Activar la visualización de AdminToolComponent
                paintUser(userLogin);
            } else if (data && data.role === 'STUDENT') {
                console.log("El usuario es estudiante");
                paintUser(userLogin);
            } else if (data && data.role === 'TITLED') {
                console.log("El usuario es titulado");
                paintUser(userLogin);
            } else if (devtool === true) {
                setShowAdminTools(true); // Activar la visualización de AdminToolComponent
                paintUser(userLogin);
            } else {
                console.log("El usuario no se ha logueado");
                paintLogin(userLogin);
            }
        }

        // Llamamos a la función que obtiene el rol
        await fetchUserRole();
    };

    // Llamamos a `tools` dentro del useEffect para que se ejecute una vez que el componente se haya montado
    useEffect(() => {
        tools();
    }, []);

    // Pintar el contenido de login
    const paintLogin = (userLogin) => {
        if (userLogin) {
            userLogin.innerHTML = `
                <div id="nonLoggedButtonContainer">
                    <button id="loginButton" onclick="window.location.href='/login'">Sign-up</button>
                    <button id="registerButton" onclick="window.location.href='/register'">Register</button>
                </div>
            `;
        }
    };

    // Pintar el contenido del usuario
    const paintUser = (userLogin) => {
        try {
            const fetchAvatar = async () => {
                const response = await fetch(`/getUserAvatar`);
                const data = await response.json();
                if (data && data !== null) {
                    userLogin.innerHTML = `
                        <div id="userContainer">
                            <img id="usuarioImagen" src="${data}" alt="Usuario" />
                            <p id="userName">${data}</p> <!-- O usa un valor apropiado para el nombre -->
                        </div>
                    `;
                } else {
                    console.log("No se pudo acceder al avatar del usuario");
                    userLogin.innerHTML = `
                        <img id="usuarioImagen" src="./usuario.png" alt="Usuario" />
                        <p>No UserName</p>
                    `;
                }
            };
            fetchAvatar();  // Llamamos la función que obtiene el avatar
        } catch (error) {
            console.log('Error en paintUser', error);
        }
    };

    return (
        <header id="Header">
            <nav id="nav-container">
                <div id="nav-div-LogoWelcome">
                    <a id="nav-div-ancle" href="App.jsx">
                        <img id="PrincipalLogoWeb" src="./IconoWeb.png" alt="Icono web Syposium creado a partir de imagenes web" />
                        <h1 id="PrincipalWelcomeText">Symposium Web 2.0</h1>
                    </a>
                </div>
                <div id="nav-div-Linkcontainer">
                    <div id="nav-div-Linkcontainer-div-searchContainer">
                        <SuggestionComponent />
                    </div>
                </div>
                <div id="nav-div-adminTools" ref={adminDivRef}>
                    {/* Mostrar AdminToolComponent solo si showAdminTools es true */}
                    {showAdminTools && <AdminToolComponent />}
                </div>
                <div id="nav-div-usercontainer" ref={userLoginRef}>
                    {/* Aquí se actualizará dinámicamente el contenido del usuario */}
                </div>
            </nav>
        </header>
    );
}

export default HeaderComponent;



