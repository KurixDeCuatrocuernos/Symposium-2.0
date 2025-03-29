import "./RegistryPage.css";
import {useEffect, useState} from "react";

function RegistryPage() {

    const [nameAdvice, setNameAdvice] = useState("");
    const [emailInput, setEmailInput] = useState("");
    const [emailColor, setEmailColor] = useState("");
    const [emailAdvice, setEmailAdvice] = useState("");
    const [pass, setPass] = useState(""); 
    const [passwordAdvice, setPasswordAdvice] = useState("");
    const [passColor,setPassColor] = useState("");
    const [passwordRepeatAdvice, setPasswordRepeatAdvice] = useState("");
    const [showPassword, setShowPassword] = useState("");
    const [birthdate, setBirthdate] = useState("");
    const minDate = new Date();
    minDate.setFullYear(minDate.getFullYear() - 10);
    const minDateString = minDate.toISOString().split("T")[0];
    const maxDate = new Date();
    maxDate.setFullYear(maxDate.getFullYear() - 200);
    const maxDateString = maxDate.toISOString().split("T")[0];

    const handleDateChange = (event) => {
        const today = new Date();
        setBirthdate(event.target.value);
        
        const birthDate = new Date(event.target.value);
        const monthDiff = today.getMonth() - birthDate.getMonth();
        const dayDiff = today.getDate() - birthDate.getDate();
        let age = today.getFullYear() - birthDate.getFullYear();
        if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
            age--; // Aún no ha cumplido años este año, restamos 1
        }
        if(age<18){
            alert("You are under-age, be careful you talk with, if you recieve suspicious messages contact with an Admin!");
        } else {
            ;
        }
    };

    const handleNameInput = (event) => {
        const name= event.target.value;
        if (name.length > 20){
            setNameAdvice("User name can only have 20 caracters at most");
        } else if (name.length < 5){
            setNameAdvice("User name must have at least 5 characters");
        } else {
            setNameAdvice(" ");
        }
    };

    const checkEmail = async (email) =>{
        const response = await fetch('/getEmails?email='+email);
        if (response.ok){
            const data = await response.json();
            if (data.status==="true"){
                if(data.resp!==null){
                    setEmailColor(data.checkEmail === "true" ? "green" : "red");
                    setEmailAdvice(data.resp);
                } else {
                    setEmailColor(data.checkEmail === "true" ? "green" : "red");
                }
            } else {
                console.log("Error getting emails from server: "+data.message);
            }
        } else {
            console.log("Couldn't connect to server");
        }
    };

    const handleEmail = (event) => {
        const newEmail = event.target.value;
        setEmailInput(newEmail);
        checkEmail(newEmail);
    };

    const handlePassword = (event) => {
        const newPass = event.target.value;
        setPass(newPass);
        const hasRepeatedChars = /(.)\1{1,}/.test(newPass); // Verifica si hay caracteres repetidos seguidos
    
        if(newPass.length<1){
            setPasswordAdvice(" ");
        } else if (newPass.length < 5) {
            setPasswordAdvice("Password must have at least 5 characters");
            setPassColor("yellow");
        } else if (newPass.length < 8) {
            hasRepeatedChars ? setPasswordAdvice("Password is too weak") : setPasswordAdvice("Password is weak but acceptable");
            setPassColor("rgb(187, 255, 0)");
        } else if (newPass.length < 12) {
            hasRepeatedChars ? setPasswordAdvice("Password is good") : setPasswordAdvice("Password is strong");
            setPassColor("rgb(115, 255, 0)")
        } else {
            hasRepeatedChars ? setPasswordAdvice("Password is very good") : setPasswordAdvice("Password is excellent");
            setPassColor("lime");
        }
    };

    const handlePasswordRepeat = (event)=>{
        const newPass = event.target.value;
        if (!newPass || newPass===""){
            setPasswordRepeatAdvice("");
        } else if (newPass !== pass){
            setPasswordRepeatAdvice("Passwords are different!");
        } else {
            setPasswordRepeatAdvice("The passwords are the same!");
        }
    };

    const cancelRegistry = () => {
        window.history.back();
    };

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    const checkData = () => {
        const username = document.getElementById("usernameInput");
        const birth = document.getElementById("birthDateInput");
        const email = document.getElementById("emailInput");
        const pass1 = document.getElementById("passInput");
        const pass2 = document.getElementById("passInput2");
        const study = document.getElementById("studiesInput");
        const school = document.getElementById("schoolInput");
        let cell = true;
        let message ="";
        
        if (username.value===""||birth.value===""||email.value===""||pass1.value===""||pass2.value===""||study.value===""||school.value===""){
            cell=false;
            message+="All the fields must be completed!";
        } else {
            
            if (username.value.length>20||username.value.length<5){
                cell=false;
                message+="Username must be lower than 20 characters and longer than 5.\n";
            }
            if (emailColor!=="green"){
                cell=false;
                message+="You must insert a valid email.\n";
            }
            if (pass1.value!==pass2.value) {
                cell=false;
                message+="Password and Password repeat are not equals, check the passwords inserted.\n";
            }
            
            if (passColor==="red"){
                cell=false;
                message+="Password must have at least 5 characters.\n";
            }
            
            /*HERE WE COULD FILTER study AND school 
            if (study.value){
                cell=false;
                message=+"";
            }
            if (school.value){
                cell=false;
                message=+"";
            }
            */
           

        }

        if (cell===true){
            registryUser(username.value, birth.value, email.value, pass1.value, study.value, school.value)
        } else {
            alert(message);
        }
    };

    const registryUser= async(username, birth, email, pass1, study, school) =>{
        if (username!==""||birth!==""||email!==""||pass1!==""||study!==""||school!==""){
            /*console.log(username+", "+birth+", "+email+", "+pass1+", "+study+", "+schoo);*/
            const response = await fetch('/postRegistryUser', {
                method: "POST",  // Método POST
                headers: {
                    "Content-Type": "application/json",  // Indicar que se envía JSON
                },
                body: JSON.stringify({  // Convertir el objeto a JSON
                    name: username,
                    fechaNac: birth,
                    email: email,
                    password: pass1,  // Se usa pass1 como contraseña
                    studies: study,
                    school
                })
            });
            if (response.ok){
                const data = await response.json();
                if(data.status==="true"){
                    console.log("User registered");
                    loginRegistered(email, pass1);
                } else {
                    console.log("There was a problem registering user: "+data.message);
                }
            } else {
                console.log("Cannot connect with the server to registry user");
            }
        } else {
            console.log("user data is empty");
        
        }
    };

    const loginRegistered = async(email, pass1) =>{
        if (email && pass1){
            try{
                const response = await fetch('/getLogin',{
                    method: "POST",  // Método POST
                    headers: {
                        "Content-Type": "application/json",  // Indicar que se envía JSON
                    },
                    body: JSON.stringify({  // Convertir el objeto a JSON
                        email: email,
                        password: pass1,
                    })
                });
                if (response.ok){
                    const data = await response.json();
                    if (data.status==="true"){
                        window.history.back();
                    } else {
                        console.log("There was a problem making the login: "+data.message);
                    }
                } else {
                    console.log("Recieved a bad response");
                }
            } catch(error){
                console.log("Error trying to login: "+error);
            }
        } else {
            console.log("Couldn't recieve the credentials to log-in");
        }
    };

    return(
        <div id="registerOverlay">
            <div id="registerContainer">
                <h1>Registry form</h1>
                <div id="username">
                    <h3>*Username: </h3>
                    <input className="input" id="usernameInput" type="text" placeholder="user123" onChange={handleNameInput}></input>
                    <span className="advice">{nameAdvice}</span>
                </div>
                <div id="birthdate">
                    <h3>*Birth date</h3>
                    <input className="input" id="birthDateInput" type="date" min={maxDateString} max={minDateString}  onChange={handleDateChange}></input>
                    <span className="advice"></span>
                </div>
                <div id="email">
                    <h3>*Email: </h3>
                    <input className="input" id="emailInput" type="text" onChange={handleEmail} style={{ color: emailColor }} placeholder="user.example@email.com"></input>
                    <span className="advice">{emailAdvice}</span>
                </div>
                <div id="password">
                    <h3>*Password: </h3>
                    <div id="password-inputs">
                        <input className="input" id="passInput" type={showPassword? "text" : "password"} onChange={handlePassword}></input>
                        <input id="checkBox" type="checkbox" onChange={togglePasswordVisibility}></input>
                        <p>Show Password</p>
                    </div>
                    <span className="advice" style={{color: passColor}}>{passwordAdvice}</span>
                    
                </div>
                <div id="password2">
                    <h3>*Repeat password: </h3>
                    <input className="input" id="passInput2" type={showPassword? "text" : "password"} onChange={handlePasswordRepeat}></input>
                    <span className="advice">{passwordRepeatAdvice}</span>
                </div>
                <div id="studies">
                    <h3>Studies</h3>
                    <input className="input" id="studiesInput" type="text" placeholder="Student of High School"></input>
                    <span className="advice"></span>
                </div>
                <div id="school">
                    <h3>School/College</h3>
                    <input className="input" id="schoolInput" type="text" placeholder="High School of Economics of London"></input>
                    <span className="advice"></span>
                </div>

                <a id="Ancle-Register" href="/Login">You have account?, Sign-in here!</a>
                <div id="buttons">
                    <button onClick={checkData}>Save</button>
                    <button onClick={cancelRegistry}>Cancel</button>
                </div>
            </div>

        </div>
    );

}
export default RegistryPage;