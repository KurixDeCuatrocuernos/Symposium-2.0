import "./DeleteAdviceComponent.css";
import { useState } from "react";

function DeleteAdviceComponent({ id, username, setShowDeleteAdvice }) {
    
    const [inputValue, setInputValue] = useState("");
    const [advice, setAdvice] = useState("");

    const handleInputChange = (event) => {
        setInputValue(event.target.value); // Actualiza el valor del estado
    };

    
    const handleCancel = () => {
        setShowDeleteAdvice(false); // Cerrar el componente de confirmación
    };

    const handleDelete = () => {
        
        if (inputValue === username) {
            console.log(`User ${username} deleted!`);
            deleteUser();
            
        } else {
            setAdvice("Username does not match, write the correct name!");
            console.log("Username does not match. Cannot delete user.");
        }
    };

    const deleteUser = async() =>{
        try{
            const response = await fetch('/getUserDeleted?id='+id);
            if(response.ok){
                const data = await response.json();
                if (data.status==="true"){
                    setShowDeleteAdvice(false); // Cerrar el componente después de eliminar
                    window.location.reload();
                } else {
                    console.log("There was a problem deleting the user in server: "+data.message);
                }
                
            } else {
                console.log("There was a problem collecting the response, try later or contact an Admin");
            }
        } catch (error){
            console.log("There was a problem connecting with server, try later or contact with an Admin");
        }
        
    };

    return (
        <div id="overlay-DeleteAdvice">
            <div id="container-DeleteAdvice">
                <img src="advertencia.png"></img>
                <h2>Are you sure you want to delete the user? Insert its username to confirm the delete:</h2>
                <input 
                id="input-DeleteAdvice" 
                placeholder={username} 
                value={inputValue}
                onChange={handleInputChange}/>
                <span>{advice}</span>
                <div id="buttons-DeleteAdvice">
                    <button onClick={handleDelete}>Delete</button>
                    <button onClick={handleCancel}>Cancel</button>
                </div>
            </div>
        </div>
    );
}

export default DeleteAdviceComponent;