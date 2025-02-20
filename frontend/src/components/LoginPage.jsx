import './LoginPage.css';
import { useState } from "react";

function LoginPage() {
    const [showPassword, setShowPassword] = useState(false); // Estado para la visibilidad de la contraseña
    const [message, setMessage] = useState("");
    
    const verifyCreds= () =>{
        const username = document.getElementById('email');
        const pass = document.getElementById('password');
        let check = true;
        let message = "";
    
        if (!username || !pass){
            check=false;
        } 
        if(username.value.trim()===""){
            check = false;
            message += "You must insert an Email.";
        } else if (!username.value.includes('@') || (!username.value.endsWith('.com') && !username.value.endsWith('.es'))){
            check = false;
            message += "You must insert a valid Email.";
        }
        if (pass.value.trim() === ""){
            check = false;
            message += "You must insert a password.";
        }

        if(check === false){
            setMessage(message);
        } else {
            message="Correct User Sign-Up";
            setMessage(message);
            makeLogin();
        }
    };

    const makeLogin = async() =>{ 
        const username = document.getElementById('email');
        const pass = document.getElementById('password');
        console.log("email:"+username.value+"pass: "+pass.value);
        
        const requestBody = JSON.stringify({
            email: username.value,
            password: pass.value,
        });
    
        console.log("Datos enviados:", requestBody); // Verifica qué se está enviando
    
        try {
            const response = await fetch('/getLogin', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: requestBody,
            });
            const data = await response.json();
            
            if (response.ok && data){
                console.log("se ha iniciado sesión");
                setMessage(data);
                window.location.href="/";
            } else {
                console.error("error en el login: "+data.error);
                setMessage("These credentials are not match, please check the password and email, and look caps and numbers");
            }
        } catch(exception){
            console.error("error en la petición: "+exception);
            setMessage("Cannot connect with Server, try to login later");
        }
        
    };

  return (
    <div id="LoginContainer">
      <form id="formLogin">
        <h1>Sign-Up</h1>

        <div>
          <label className="formLogin-label">User Email</label>
          <input className="formLogin-input" id="email" placeholder="user.example@email.com" />
          <span className="formLogin-span">Your email</span>
        </div>

        <div>
          <label className="formLogin-label">User Password</label>
          <div id="inputContainer">
            <input className="formLogin-input" type={showPassword ? "text" : "password"} id="password" placeholder="12345abcd_!" />
            <input
              type="checkbox" id="showPasswordCheckbox" checked={showPassword} onChange={() => setShowPassword(!showPassword)} />
            <label htmlFor="showPasswordCheckbox"> Mostrar contraseña</label>
          </div>
          <span className="formLogin-span">Your password</span>
        </div>

        <div>
          <button type="button" className="formLogin-button" id="submitButton" onClick={verifyCreds}>Sign-In</button>
          <button type="button" className="formLogin-button" id="cancelButton">Cancel</button>
        </div>
      </form>
      {message && <p className="formLogin-message">{message}</p>}
    </div>
  );
}

export default LoginPage;
