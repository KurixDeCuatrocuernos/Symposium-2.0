import "./EditUserComponent.css";
import { useEffect, useState } from "react";

function isNotEmpty(value) {
    return value !== null && value !== undefined && value !== '';
}

function EditUserComponent({ id, setShowEditUser }){

    const maxDate = new Date();
    maxDate.setFullYear(maxDate.getFullYear() - 10);
    const maxDateString = maxDate.toISOString().split("T")[0];
    const minDate = new Date();
    minDate.setFullYear(minDate.getFullYear() - 200);
    const minDateString = minDate.toISOString().split("T")[0];
    const today = new Date();
    today.setFullYear(today.getFullYear());
    const todayString = today.toISOString().split("T")[0];
    const isInteger = (value) => Number.isInteger(value);

    const [checked, setChecked] = useState("");
    const [roleAdvice, setRoleAdvice] = useState("");
    const [username, setUsername] = useState("");
    const [nameColor, setNameColor] = useState("");
    const [usernameAdvice, setUsernameAdvice] = useState("");
    const [oldEmail, setOldEmail] = useState("");
    const [email, setEmail] = useState("");
    const [emailColor, setEmailColor] = useState("");
    const [emailAdvice, setEmailAdvice] = useState("");
    const [birthDate, setBirthDate] = useState("");
    const [birthDateAdvice, setBirthDateAdvice] = useState("");
    const [pass, setPass] = useState("");
    const [passColor, setPassColor] = useState("");
    const [showPassword, setShowPassword] = useState("");
    const [passAdvice, setPassAdvice] = useState("");
    const [rePass, setRePass] = useState("");
    const [rePassColor, setRePassColor] = useState("");
    const [rePassAdvice, setRePassAdvice] = useState("");
    
    const [studies, setStudies] = useState("");
    const [studiesAdvice, setStudiesAdvice] = useState("");
    const [school, setSchool] = useState("");
    const [schoolAdvice, setSchoolAdvice] = useState("");
    const [studiesTitle, setStudiesTitle] = useState("");
    const [studiesTitleAdvice, setStudiesTitleAdvice] = useState("");
    const [studyPlace, setStudyPlace] = useState("");
    const [studyPlaceAdvice, setStudyPlaceadvice] = useState("");
    const [titleDate, settitleDate] = useState("");
    const [titleDateAdvice, setTitleDateAdvice] = useState("");
    const [phone, setPhone] = useState("");
    const [phoneColor, setPhoneColor] = useState("");
    const [phoneAdvice, setPhoneAdvice] = useState("");
    /*TO TRIES CHECKED CHANGES
    useEffect(()=>{
        if(checked==="STUDENT") {
            console.log("El usuario será STUDENT");
        } else if (checked==="TITLED") {
            console.log("El usuario será TITLED");
        } else if (checked==="ADMIN") {
            console.log("El usuario será ADMIN");
        } else {
            console.log("ERROR when reading checked");
        }
    },[checked]);
    */
    useEffect(() => {
        checkEmail(email);
    }, [email]);

    useEffect(() => {
        getUserData(id);
    }, []);

    const handleEdit = () =>{
        if (checked === ""){
            setRoleAdvice("A User must have a role!");
        } else {
            let message = "";

            if (username === "") {
                message+="A User must have a name! \n";
                setUsernameAdvice("Insert a User name!");
            }

            if (email === "") {
                message+="A User must have an email! \n";
                setEmailAdvice("Insert an Email!");
            }
            
            if (birthDate === "") {
                message+="A User must have a birth date! \n";
                setBirthDateAdvice("Insert a Date!");
            }

            if (pass !== rePass) {
                message+="Passwords doesn't match! \n";
                setRePassAdvice("If you change password you must repeat it!")
            }

            if(checked === "STUDENT"){

                if (studies === ""){

                }

                if (school === "") {

                }

            } else if (checked === "TITLED"){

                if (studiesTitle === ""){
                    message+="A titled user must have any studies! \n";
                    setStudiesTitleAdvice("A titled user must have any studies!");
                }

                if (studyPlace === ""){
                    message+="A titled user must have gotten his education from somewhere! \n";
                    setStudyPlaceadvice("A titled user must have gotten his education from somewhere!");
                }

                if (titleDate === ""){
                    message+="A titled user must have gotten his title somewhen!\n";
                    setTitleDateAdvice("A titled user must have gotten his title somewhen!");
                }
                
            } else if (checked==="ADMIN"){

                if (phone === "") {
                    message+="An Admin must have a phone to contact her/him!\n";
                    setPhoneAdvice("An Admin must have a phone to contact her/him!");
                }

            } else {
                setRoleAdvice("A User must have a role!");
            }

            if(message!==""){
                alert("Check the fields!");
            } else {
                editUser();
                window.location.reload();// recarga la página
            }
        }
       

    };

    const handleName = (event) => {
        const name= event.target.value;
        setUsername(name);
        setUsernameAdvice("");
        if (name.length===0){
            setUsernameAdvice("");
            setNameColor("black");
        } else if (name.length > 20){
            setUsernameAdvice("User name must have at most 20 characters");
            setNameColor("red");
        } else  if (name.length < 5) {
            setUsernameAdvice("User name must have at least 5 characters");
            setNameColor("red");
        } else {
            setUsernameAdvice("");
            setNameColor("lime");
        }
    };

    const handleEmail = (event) => {
        setEmail(event.target.value);
        setEmailAdvice("");
    };

    const handleDate = (event) => {
        setBirthDate(event.target.value);
        setBirthDateAdvice("");
    };

    const handlePass = (event) => {
        const newPass = event.target.value;
        setPass(newPass);
        setPassAdvice("");
        const hasRepeatedChars = /(.)\1{1,}/.test(newPass); // Verifica si hay caracteres repetidos seguidos
    
        if(newPass.length<1){
            setPassAdvice(" ");
        } else if (newPass.length < 5) {
            setPassAdvice("Password must have at least 5 characters");
            setPassColor("yellow");
        } else if (newPass.length < 8) {
            hasRepeatedChars ? setPassAdvice("Password is too weak") : setPassAdvice("Password is weak but acceptable");
            setPassColor("rgb(187, 255, 0)");
        } else if (newPass.length < 12) {
            hasRepeatedChars ? setPassAdvice("Password is good") : setPassAdvice("Password is strong");
            setPassColor("rgb(115, 255, 0)")
        } else {
            hasRepeatedChars ? setPassAdvice("Password is very good") : setPassAdvice("Password is excellent");
            setPassColor("lime");
        }
    };

    const handleRePass = (event) => {
        
        const newPass = event.target.value;
        setRePass(newPass);
        setRePassAdvice("");
        if (!newPass || newPass===""){
            setRePassAdvice("");
        } else if (newPass !== pass){
            setRePassAdvice("Passwords are different!");
        } else {
            setRePassAdvice("The passwords are the same!");
            setRePassColor("lime");
        }
    };

    const handleStudies = (event) => {
        setStudies(event.target.value);
    };

    const handleSchool = (event) => {
        setSchool(event.target.value);
    };

    const handlePhone = (event) => {
        const newPhone = event.target.value;
        setPhone(newPhone);
        setPhoneAdvice("");
        if (newPhone.length===0){
            setPhoneColor("black");
            setPhoneAdvice(" ");
        } else if (newPhone.length<9 || newPhone.length > 15) {
            setPhoneColor("red");
            setPhoneAdvice("A Phone number must have at least 9 digits and 15 digits at most");
        } else if (/[^0-9]/.test(newPhone) && !Number.isInteger(Number(newPhone))) {
            setPhoneColor("red");
            setPhoneAdvice("Phone must have numbers only")
        } else {
            setPhoneColor("green");
            setPhoneAdvice(" ");
        }
    };

    const handleStudiesTitle = (event) => {
        setStudiesTitle(event.target.value);
        setStudiesTitleAdvice("");
    };

    const handleStudyPlace = (event) => {
        setStudyPlace(event.target.value);
        setStudyPlaceadvice("");
    };

    const handleTitleDate = (event) => {
        settitleDate(event.target.value);
        setTitleDateAdvice("");
    };
    
    const handleCancel = () => {
        setShowEditUser(false); // cierra el editor
    };

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    const checkEmail = async (email) =>{
        const response = await fetch('/getEmailsEdit?email='+email+'&currentEmail='+oldEmail);
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

    const getUserData = async(id) => {
        try{
            const response = await fetch('/getUserToEdit?id='+id);
            if (response.ok){
                const data = await response.json();
                if (data.status==="true"){
                    setChecked(data.role); 
                    setOldEmail(data.email);
                    setEmail(data.email);
                    setUsername(data.name);
                    setBirthDate(data.fechaNac);
                    
                    if(data.role==="STUDENT"){    
                        setStudies(data.studies);
                        setSchool(data.school);                        
                    } else if (data.role==="TITLED") {
                        setStudiesTitle(data.studiesTitle);
                        setStudyPlace(data.studyPlace);
                        settitleDate(data.titleDate);
                    } else if (data.role==="ADMIN") {
                        setPhone(data.phone);
                    }

                } else {
                    alert("Problem getting the user to edit in server: "+data.message);
                }
            } else {
                alert("There was a problem getting the response, try again or contact an Admin");
            }
        } catch (error) {
            alert("Error connecting with server: "+error);
        }

    };

    const editUser = async() =>{
        let userData = {};

        if(checked === "STUDENT"){
            userData = {
                id: isNotEmpty(id) ? id : "",
                role: isNotEmpty(checked) ? checked : "",
                name: isNotEmpty(username) ? username : "",
                fechaNac: isNotEmpty(birthDate) ? birthDate : "",
                email: isNotEmpty(email) ? email : "",
                password: isNotEmpty(pass) ? pass : "",
                studies: isNotEmpty(studies) ? studies : "",
                school: isNotEmpty(school) ? school : "",
            };
        } else if (checked === "TITLED"){
            userData = {
                id: isNotEmpty(id) ? id : "",
                role: isNotEmpty(checked) ? checked : "",
                name: isNotEmpty(username) ? username : "",
                fechaNac: isNotEmpty(birthDate) ? birthDate : "",
                email: isNotEmpty(email) ? email : "",
                password: isNotEmpty(pass) ? pass : "",
                studies_title: isNotEmpty(studiesTitle) ? studiesTitle : "",
                study_place: isNotEmpty(studyPlace) ? studyPlace : "",
                title_date: isNotEmpty(titleDate) ? titleDate : "",
            };
        } else if (checked === "ADMIN") {
            userData = {
                id: isNotEmpty(id) ? id : "",
                role: isNotEmpty(checked) ? checked : "",
                name: isNotEmpty(username) ? username : "",
                fechaNac: isNotEmpty(birthDate) ? birthDate : "",
                email: isNotEmpty(email) ? email : "",
                password: isNotEmpty(pass) ? pass : "",
                phone: isNotEmpty(phone) ? phone : ""
            };
        }
        try{
            const response = await fetch('/postUserEdited', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });
            if(response.ok) {
                const data = await response.json();
                if (data.status === "true"){
                    window.location.reload();
                } else {
                    alert("There was a server problem editing the user in server: "+data.message);
                }
            } else {
                alert("There was a problem with the server response: "+response);
            }
        } catch(error){
            console.log("Error connecting with server: "+error);
        }
    };

    return(
        <>
        <div id="overlay-EditUserComp">
            <div id="container-EditUserComp">
                <img src="advertencia.png"></img>
                <div className="container-div-EditUserComp">
                    <h3 className="h3Class-EditUserComp">Id: </h3>
                    <div className="inputContainer-EditUserComp">
                        <input className="inputClass-EditUserComp" type="text" value={id} readOnly />
                        <span className="spanClass-EditUserComp">Id cannot be changed here</span>
                    </div>
                </div>
                <div className="container-div-EditUserComp">
                    <h3 className="h3Class-EditUserComp">New Name: </h3>
                    <div className="inputContainer-EditUserComp">
                        <input className="inputClass-EditUserComp" type="text" onChange={handleName} style={{ color: nameColor }} value={username}></input>
                        <span className="spanClass-EditUserComp">{usernameAdvice}</span>
                    </div>
                </div>
                <div className="container-div-EditUserComp">
                    <h3 className="h3Class-EditUserComp">New Date: </h3>
                    <div className="inputContainer-EditUserComp">
                        <input className="inputClass-EditUserComp" type="date" min={minDateString} max={maxDateString} onChange={handleDate} value={birthDate}></input>
                        <span className="spanClass-EditUserComp">{birthDateAdvice}</span>
                    </div>
                </div>
                <div className="container-div-EditUserComp">
                    <h3 className="h3Class-EditUserComp">New Email: </h3>
                    <div className="inputContainer-EditUserComp">
                        <input className="inputClass-EditUserComp" type="text" onChange={handleEmail} style={{ color: emailColor }} value={email}></input>
                        <span className="spanClass-EditUserComp">{emailAdvice}</span>
                    </div>
                </div>
                <div className="container-div-EditUserComp">
                    <h3 className="h3Class-EditUserComp">New Password: </h3>
                    <div className="inputContainer-EditUserComp">
                        <div id="inputPasswordContainer-EditUserComp">
                            <input className="inputClass-EditUserComp" type={showPassword? "text" : "password"} onChange={handlePass} style={{ color: passColor}} value={pass}></input>
                            <input id="checkBox" type="checkbox" onChange={togglePasswordVisibility}></input>
                            <p>Show</p>
                        </div>
                       
                        <span className="spanClass-EditUserComp" style={{ color: passColor}}>{passAdvice}</span>
                    </div>
                </div>
                <div className="container-div-EditUserComp">
                    <h3 className="h3Class-EditUserComp">Repeat Password: </h3>
                    <div className="inputContainer-EditUserComp">
                        <input className="inputClass-EditUserComp" type={showPassword? "text" : "password"} onChange={handleRePass} value={rePass}></input>
                        <span className="spanClass-EditUserComp" style={{ color: rePassColor}}>{rePassAdvice}</span>
                    </div>
                </div>
                <div className="container-div-EditUserComp">
                    <h3 className="h3Class-EditUserComp">New Role: </h3>
                    <div id="CheckBoxRole-EditUserComp">
                        <label>
                            <input type="radio" name="role" value="STUDENT" checked={checked==="STUDENT"} onChange={()=> setChecked("STUDENT")}></input>
                            Student
                        </label>
                        <label>
                            <input type="radio" name="role" value="TITLED" checked={checked==="TITLED"} onChange={()=> setChecked("TITLED")}></input>
                            Titled
                        </label>
                        <label>
                            <input type="radio" name="role" value="ADMIN" checked={checked==="ADMIN"} onChange={()=> setChecked("ADMIN")}></input>
                            Admin
                        </label>
                    </div>
                    <span className="spanClass-EditUserComp">{roleAdvice}</span>
                </div>
                {checked  === "STUDENT" ?
                    <>
                    <div className="container-div-EditUserComp">
                        <h3 className="h3Class-EditUserComp">New Studies: </h3>
                        <div className="inputContainer-EditUserComp">
                            <input className="inputClass-EditUserComp" type="text" onChange={handleStudies} value={studies}></input>
                            <span className="spanClass-EditUserComp">{studiesAdvice}</span>
                        </div>
                    </div>
                    <div className="container-div-EditUserComp">
                        <h3 className="h3Class-EditUserComp">New School: </h3>
                        <div className="inputContainer-EditUserComp">
                            <input className="inputClass-EditUserComp" type="text" onChange={handleSchool} value={school}></input>
                            <span className="spanClass-EditUserComp">{schoolAdvice}</span>
                        </div>
                    </div>
                    </>
                    : ''
                }
                {checked === "ADMIN" ?
                    <>
                    <span id="AdminChecked-span">If you converts the user in Admin, all his/her comments and answers will be deleted!</span>
                    <div className="container-div-EditUserComp">
                        <h3 className="h3Class-EditUserComp">Phone: </h3>
                        <div className="inputContainer-EditUserComp">
                            <input className="inputClass-EditUserComp" type="number" onChange={handlePhone} style= {{ color: phoneColor }} value={phone}></input>
                            <span className="spanClass-EditUserComp">{phoneAdvice}</span>
                        </div>
                    </div>
                    </>
                : ''
                }
                {checked === "TITLED" ?
                    <>
                    <div className="container-div-EditUserComp">
                        <h3 className="h3Class-EditUserComp">Studies Title: </h3>
                        <div className="inputContainer-EditUserComp">
                            <input className="inputClass-EditUserComp" type="text" onChange={handleStudiesTitle} value={studiesTitle}></input>
                            <span className="spanClass-EditUserComp">{studiesTitleAdvice}</span>
                        </div>
                    </div>
                    <div className="container-div-EditUserComp">
                        <h3 className="h3Class-EditUserComp">Study Place: </h3>
                        <div className="inputContainer-EditUserComp">
                            <input className="inputClass-EditUserComp" type="text" onChange={handleStudyPlace} value={studyPlace}></input>
                            <span className="spanClass-EditUserComp">{studyPlaceAdvice}</span>
                        </div>
                    </div>
                    <div className="container-div-EditUserComp">
                        <h3 className="h3Class-EditUserComp">Title Date: </h3>
                        <div className="inputContainer-EditUserComp">
                            <input className="inputClass-EditUserComp" type="Date" min={minDateString} max={todayString} onChange={handleTitleDate} value={titleDate}></input>
                            <span className="spanClass-EditUserComp">{titleDateAdvice}</span>
                        </div>
                    </div>
                    </>
                : ''
                }
                <div id="buttons-EditUserComp">
                    <button onClick={handleEdit}>Save Changes</button>
                    <button onClick={handleCancel}>Cancel</button>
                </div>
            </div>
        </div>
        </>
    );

}
export default EditUserComponent;