import { useEffect, useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import HeaderComponent from "./components/HeaderComponent";
import WorkListPage from "./components/WorkListPage";
import HomePage from "./components/HomePage";
import LoginPage from "./components/LoginPage";
import CommentListPage from "./components/CommentListPage";
import RegistryPage from "./components/RegistryPage";
import ErrorPage from "./components/ErrorPage";
import UsersListPage from"./components/UsersListPage";
import WorkShowPage from "./components/WorkShowPage";
import ChatComponent from "./components/ChatComponent";

function App() {
  const [access, setAccess] = useState(false); // Estado para manejar el acceso
  const [usr, setUsr] = useState("");

  useEffect(()=>{
    getUsr();
  },[]);

  useEffect(() => {
    const searchAccess = async () => {
      const devtool = false; // esto es sólo para el desarrollo
      try {
        const response = await fetch("/getUserRole");
        const data = await response.json();
        console.log(data);
        if (data && data === "ADMIN") {
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

  const getUsr = async() => {
    try{
      const response = await fetch('/getUsername');
      if (response.ok){
        const data = await response.json();
        if (data.status === "true"){
          if (data.user === "false"){
             setUsr("");
          } else {
            setUsr(data.user);
            console.log("user: "+data.user);
          }
        } else {
          console.log("there was an error getting the user");
        }
      } else {
        console.log("Response is not ok");
      }
    } catch (error) {
      console.log("There was a problem trying to connect with server: "+error);
    }
  };


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
            <Route path="/CommentList" element={<CommentListPage />} />
          </>
        ) : (
          <>
            <Route path="/WorkList" element={<ErrorPage />} />
            <Route path="/UsersList" element={<ErrorPage />} />
            <Route path="/CommentList" element={<ErrorPage />} />
          </>
        )}
        <Route path="*" element= {<ErrorPage />}/>
      </Routes>
      {usr && usr!=="" && usr!==null ? 
        <ChatComponent usr={usr}/>
      :
      ''
      }
    </Router>
    
  );
}

export default App;

