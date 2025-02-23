import "./CommentInsertComponent.css";
import {useState, useRef} from 'react';

function CommentInsertComponent({onClose, role, usr, isbn}){

    const [sliderValue, setSliderValue] = useState(50);
    const [titleinput, setTitleInput]= useState("");
    const [textarea, setTextArea] = useState("");

    const handleSliderChange = (event) =>{
        setSliderValue(event.target.value);
    };


    const handleTitleChange = (event) => {
        const value = event.target.value;
        // Si el valor no está vacío, convierte la primera letra en mayúscula y el resto lo deja igual
        const formattedValue = value.charAt(0).toUpperCase() + value.slice(1);
        setTitleInput(formattedValue);
    };
    
    const handleTextAreaChange = (event) => {
        const value = event.target.value;
        // Convierte la primera letra del texto a mayúscula
        const formattedValue = value.charAt(0).toUpperCase() + value.slice(1);
        setTextArea(formattedValue);
    };

    const checkData = async() => {

        if(titleinput!=="" && textarea!=="" && sliderValue!==null && usr && isbn){
                try{
                    const response = await fetch('/postCommentInserted',{
                        method:'POST',
                        headers: {'Content-Type':'application/json',},
                        body: JSON.stringify({
                            titulo: titleinput,
                            texto: textarea,
                            valoracion: sliderValue,
                            usuario: usr,
                            obra: isbn
                        })
                           
                    });
                    if (response.ok){
                        const data = await response.json();    
                        if (data.status === "true"){
                            window.location.reload();
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
    };

    return(
        <div id="overlay">
            <div id="container">
                <button onClick={onClose}>X</button>
                <div id="title">
                    <h2>Comment Title</h2>
                    <input type="text" onChange={handleTitleChange} placeholder="I want to say..."></input>
                </div>
                <div id="text">
                    <h2>Comment Body</h2>
                    <textarea type="textarea"  onChange={handleTextAreaChange} placeholder="I like it because of..."></textarea>
                </div>
                <div id="value">
                    <h2>Select a number</h2>
                    <input id="slider" type="range" 
                    min="0" max="100" value={sliderValue} 
                    step="1" onInput={handleSliderChange}></input>
                    <h2 id="sliderValue">{sliderValue}</h2>
                </div>
                <div id="Savebutton">
                    <button onClick={checkData}>Save</button>
                </div>
            </div>
        </div>
    );

}
export default CommentInsertComponent;