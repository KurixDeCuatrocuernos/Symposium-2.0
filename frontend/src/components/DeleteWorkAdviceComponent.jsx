import "./DeleteWorkAdviceComponent.css";
import { useState, useEffect } from "react";

function DeleteWorkAdviceComponent({ title, id, setShowDeleteAdvice }){
    
    const [inputValue, setInputValue] = useState("");
    const [advice, setAdvice] = useState("");
    const [titulo, setTitulo] = useState(title);

    const handleInputChange = (event) => {
        setInputValue(event.target.value); // Actualiza el valor del estado
    };

    const handleCancel = () => {
        setShowDeleteAdvice(false); // Cerrar el componente de confirmación
    };

    const handleDelete = () => {
        
        if (inputValue === titulo) {
            console.log(`Writing ${titulo} deleted!`);
            deleteWork();
            
        } else {
            setAdvice("Writing title doesn't match, write the correct title!");
            console.log("Writing title doesn't match. Cannot delete the writing.");
        }
    };

    const deleteWork = async() =>{
        try{
            const response = await fetch('/getWorkDeleted?id='+id);
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

    return(
        <div id="overlay-DeleteWorkAdvice">
            <div id="container-DeleteWorkAdvice">
                <img src="advertencia.png"></img>
                <h2>Are you sure you want to delete the writing? This action deletes the comments too! Insert its title to confirm the delete:</h2>
                <input 
                id="input-DeleteWorkAdvice" 
                placeholder={title}
                onChange={handleInputChange}/>
                <span>{advice}</span>
                <div id="buttons-DeleteWorkAdvice">
                    <button onClick={handleDelete}>Delete</button>
                    <button onClick={handleCancel}>Cancel</button>
                </div>
            </div>
        </div>
    );
}

export default DeleteWorkAdviceComponent;