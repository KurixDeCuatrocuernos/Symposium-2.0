import "./ComentarioComponent.css";
import React, { useState, useEffect } from "react";
import CommentInsertComponent from "./CommentInsertComponent";
import AnswerComponent from "./AnswerComponent";
import SignInMessageComponent from "./SignInMessageComponent";

function ComentarioComponent({ id, username, school, value, title, text, datetime, usr, isbn, role }){

    const [answer, setAnswer] = useState(false);
    const [answerList, setAnswerList] = useState([]);
    const [signInMessage, setSignInMessage] = useState(false);
    const [logged, setLogged] = useState(false);

    useEffect(() => {
        getAnswers();
    }, []);

    const banComment = async() => {
        
        let cell = confirm("¿Are you sure you want to ban the comment with id: "+id+"?");
        if (cell === true){
            try{
                const response = await fetch('/getBanComment?id='+id);
                if (response.ok){
                    const data = await response.json();
                    if (data.status==="true"){
                        window.location.reload();
                    } else {
                        cons("There was a problem in server: "+data.message);
                    }
                } else {
                    console.log("response is not ok!");
                }
            } catch (error){
                alert("There was a problem connecting with the server: "+error);
            }
        } 
        
    };

    const getAnswers = async() => {
        try{
            const response = await fetch('/getAnswers?id='+isbn+'&comment='+id);
            if (response.ok){
                const data = await response.json();
                if (data.status==="true"){
                    setAnswerList(data.array);
                } else {
                    cons("There was a problem in server: "+data.message);
                }
            } else {
                console.log("response is not ok!");
            }
        } catch (error){
            alert("There was a problem connecting with the server: "+error);
        }
    };

    
    const closeAnswer = () => {
        setAnswer(false);
    };


    const handleAnswer = () => {
        setAnswer(true);
    };

    const handleLoginMessage = () => {
        setSignInMessage(true);
    };

    const closeLoginMessage = () => {
        setSignInMessage(false);
    };
    
    
    return(
        <>
        {answer && <CommentInsertComponent onClose={closeAnswer} id={id} answer={answer} usr={usr} isbn={isbn}/>}
        {signInMessage && <SignInMessageComponent onClose={closeLoginMessage}/>}
        <div id="CommentContainer">
            <div id="userContent">
                <img id="userImg" src="usuario.png"></img>
                <div id="user-text-container">
                    <p id="userName">{username}</p>
                    <p id="userRole">{school}</p>
                </div>
                
            </div>
            <div id="commentContent">
                <p id="commentValue">{value}</p>
                <div id="commentContext-text">
                    <strong id="commentTitle">{title}</strong>
                    <p id="commentText">{text}</p>
                    <p id="commentDate">{datetime}</p>
                    { usr !== null && role!=="ADMIN" ? <img onClick={handleAnswer} src="pregunta-y-respuesta.png"></img> : '' }
                    { usr === null ? <img onClick={handleLoginMessage} src="pregunta-y-respuesta.png"></img> : '' }
                    {role==="ADMIN" ? <img onClick={banComment} src="prohibido.png"></img> : '' }
                    
                </div>
            </div>
        </div>
        {answerList.map((answer) => {
            if (answer.comentario === id) {
                return (
                    <AnswerComponent 
                        key={answer.id}
                        userRole={role}
                        username={answer.username} 
                        school={answer.school} 
                        role={answer.role} 
                        title={answer.title} 
                        text={answer.text} 
                        datetime={answer.datetime} 
                        id={answer.id}
                    />
                );
            }
            return null; // Si no coincide, no devolver nada
        })}
        </>
    );

}
export default ComentarioComponent;