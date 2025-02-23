import "./HeaderComponent.css";
import SuggestionComponent from './SuggestionComponent';
import React, { useEffect, useState, useRef } from 'react';
import AdminToolComponent from './AdminToolComponent'; 

function HeaderComponent() {
    const devtool = false;
    const adminDivRef = useRef(null);
    const userLoginRef = useRef(null);

    const [showAdminTools, setShowAdminTools] = useState(false);
    const [userData, setUserData] = useState(null);
    const [showLogoutMenu, setShowLogoutMenu] = useState(false);

    useEffect(() => {
        tools();
    }, []);

    const tools = async () => {
        const fetchUserRole = async () => {
            const response = await fetch(`/getUserRole`);
            const data = await response.json();
            
            if (data === 'ADMIN') {
                setShowAdminTools(true);
                fetchUserAvatar();
            } else if (['STUDENT', 'TITLED'].includes(data)) {
                fetchUserAvatar();
            } else if (devtool) {
                setShowAdminTools(true);
                fetchUserAvatar();
            } else {
                setUserData(null); // El usuario no está logueado
            }
        };
        await fetchUserRole();
    };

    const fetchUserAvatar = async () => {
        try {
            const response = await fetch(`/getUserAvatar`);
            const data = await response.json();
            setUserData(data);
        } catch (error) {
            console.log('Error obteniendo avatar del usuario', error);
            setUserData(null);
        }
    };

    const handleLogoutClick = () => {
        setShowLogoutMenu(!showLogoutMenu);
    };

    const handleLogout = () => {
        const confirmLogout = window.confirm("¿Estás seguro de que quieres cerrar sesión?");
        if (confirmLogout) {
            const logout = async () => {
                try {
                    const response = await fetch("/getLogout", { method: "POST" });
                    if (response.ok) {
                        window.location.href = "/"; // Redirige a la página principal
                    } else {
                        console.error("Error al cerrar sesión");
                    }
                } catch (error) {
                    console.error("Error en la petición de logout", error);
                }
            };
            logout(); // Llamamos a la función logout después de definirla
        }
    };

    return (
        <header id="Header">
            <nav id="nav-container">
                <div id="nav-div-LogoWelcome">
                    <a id="nav-div-ancle" href="/">
                        <img id="PrincipalLogoWeb" src="./IconoWeb.png" alt="Icono web Syposium" />
                        <h1 id="PrincipalWelcomeText">Symposium Web 2.0</h1>
                    </a>
                </div>
                <div id="nav-div-Linkcontainer">
                    <div id="nav-div-Linkcontainer-div-searchContainer">
                        <SuggestionComponent />
                    </div>
                </div>
                <div id="nav-div-adminTools" ref={adminDivRef}>
                    {showAdminTools && <AdminToolComponent />}
                </div>
                <div id="nav-div-usercontainer" ref={userLoginRef}>
                    {userData ? (
                        <div id="userProfile">
                            <img id="logoutImg" 
                                src="./engranaje.png" 
                                alt="Configuración" 
                                onClick={handleLogoutClick}
                                style={{ cursor: "pointer", width: "40px" }} 
                            />
                            <img id="usuarioImagen" 
                                src={userData.avatar || "./usuario.png"} 
                                alt="Usuario" 
                            />
                            <p id="userName">{userData.username || "No UserName"}</p>

                            {showLogoutMenu && (
                                <div id="logoutMessage" className="logout-menu">
                                    <p>¿Quieres cerrar sesión?</p>
                                    <button onClick={handleLogout}>Cerrar sesión</button>
                                </div>
                            )}
                        </div>
                    ) : (
                        <div id="nonLoggedButtonContainer">
                            <button id="loginButton" onClick={() => window.location.href='/Login'}>Sign-up</button>
                            <button id="registerButton" onClick={() => window.location.href='/Register'}>Register</button>
                        </div>
                    )}
                </div>
            </nav>
        </header>
    );
}

export default HeaderComponent;




