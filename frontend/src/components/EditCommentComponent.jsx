import "./EditCommentComponent.css"
import {useState, useEffect} from "react";

function EditCommentComponent({ idComment, onClose, usr, isbn}){

    const [title, setTitle] = useState("");
    const [text, setText] = useState("");
    const [value, setValue] = useState("");

    useEffect(()=>{
        getCommentData();
    }, [idComment]);

    const getCommentData = async() =>{
        if (idComment && idComment!==null){
            const response = await fetch('/getCommentEdit?id='+idComment);
            if (response.ok){
                const data = await response.json();
                if (data.status==="true"){
                    setTitle(data.title);
                    setText(data.text);
                    setValue(data.value);
                } else {
                    console.log("There was a problem in server: "+data.message);
                }
            } else {
                console.log("Error getting the response");
            }
        } else {
            console.log("Cannot get the data of the comment with id: "+idComment);
        }
    };

    const handleSliderChange = (event) =>{
        setValue(event.target.value);
    };
    
    const handleTitleChange = (event) => {
        const value = event.target.value;
        // Si el valor no está vacío, convierte la primera letra en mayúscula y el resto lo deja igual
        const formattedValue = value.charAt(0).toUpperCase() + value.slice(1);
        setTitle(formattedValue);
    };
    
    const handleTextAreaChange = (event) => {
        const value = event.target.value;
        // Convierte la primera letra del texto a mayúscula
        const formattedValue = value.charAt(0).toUpperCase() + value.slice(1);
        setText(formattedValue);
    };

    const checkData = async() => {

        if(title!=="" && text!=="" && value!==null && usr!==null && isbn){
                try{
                    const response = await fetch('/postCommentEdited',{
                        method:'POST',
                        headers: {'Content-Type':'application/json',},
                        body: JSON.stringify({
                            titulo: title,
                            texto: text,
                            valoracion: value,
                            usuario: usr,
                            obra: isbn
                        })
                           
                    });
                    if (response.ok){
                        const data = await response.json();    
                        if (data.status === "true"){
                            window.location.reload();
                        } else {
                            console.log("Error in server: "+data.message);
                        }
                    } else {
                        console.log("Couldn't recieve the response from the server"+response);
                    }
                    
                } catch (error){
                console.log("Couldn't connect with the server to send the comment, try later: "+error);
                }
            
        } else {
            alert("A comment must have a Title, a Body and a Value, check your comment!");
        }
    }

    return(
        <div id="overlay">
            <div id="editContainer">
                <button onClick={onClose}>X</button>
                <div id="title">
                    <h2>Comment Title</h2>
                    <input type="text" onChange={handleTitleChange} placeholder="I want to say..." value={title}></input>
                </div>
                <div id="text">
                    <h2>Comment Body</h2>
                    <textarea type="textarea"  onChange={handleTextAreaChange} placeholder="I like it because of..." value={text}></textarea>
                </div>
                <div id="value">
                    <h2>Select a number</h2>
                    <input id="slider" type="range" 
                    min="0" max="100" value={value} 
                    step="1" onInput={handleSliderChange}></input>
                    <h2 id="sliderValue">{value}</h2>
                </div>
                <div id="Savebutton">
                    <button onClick={checkData}>Save</button>
                </div>
            </div>
        </div>
    );

}
export default EditCommentComponent;