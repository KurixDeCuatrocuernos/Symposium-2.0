import { useEffect, useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import HeaderComponent from "./components/HeaderComponent";
import WorkListPage from "./components/WorkListPage";
import HomePage from "./components/HomePage";
import LoginPage from "./components/LoginPage";
import RegistryPage from "./components/RegistryPage";
import ErrorPage from "./components/ErrorPage";
import UsersListPage from"./components/UsersListPage";
import WorkShowPage from "./components/WorkShowPage";
import ChatComponent from "./components/ChatComponent";

function App() {
  const [access, setAccess] = useState(false); // Estado para manejar el acceso
  const [elements, setElements] = useState(null); // Estado para manejar las rutas dinámicas

  useEffect(() => {
    const searchAccess = async () => {
      const devtool = false; // esto es sólo para el desarrollo
      try {
        const response = await fetch("/getUserRole");
        const data = await response.json();
        console.log(data);
        if (data && data === "ADMIN") {
          console.log("El usuario es Administrador");
          setAccess(true);
        } else if (devtool === true) { // sólo para desarrollo
          setAccess(true);
        } else {
          setAccess(false);
        }
      } catch (error) {
        console.error("Error obteniendo el rol del usuario", error);
        setAccess(false);
      }
    };

    searchAccess();
  }, []);


  return (
    <Router>
      <HeaderComponent />

      <Routes>
        <Route path="/" element={<HomePage />} />
       
        <Route path="/Login" element={<LoginPage />} />
        <Route path="/workShow" element={<WorkShowPage />}/>
        <Route path="/Register" element={<RegistryPage />} />
        
        {access ? (
          <>
            <Route path="/WorkList" element={<WorkListPage />} />
            <Route path="/UsersList" element={<UsersListPage />} />
          </>
        ) : (
          <>
            <Route path="/WorkList" element={<ErrorPage />} />
            <Route path="/UsersList" element={<ErrorPage />} />
          </>
        )}
        <Route path="*" element= {<ErrorPage />}/>
      </Routes>
      <ChatComponent />
    </Router>
    
  );
}

export default App;

