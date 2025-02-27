import "./WorkShowPage.css";
import React, { useState,useEffect } from "react";
import { useSearchParams } from "react-router-dom";

import ComentarioComponent from "./ComentarioComponent";
import CommentInsertComponent from "./CommentInsertComponent"
import WritingComponent from "./WritingComponent";
import SignInMessageComponent from "./SignInMessageComponent";
import EditCommentComponent from "./EditCommentComponent";

function WorkShowPage(){

    const [searchParams] = useSearchParams();
    const isbn = searchParams.get("id");
    const [studentValue, setStudentvalue] = useState(0);
    const [titledValue, setTitledvalue] = useState(0);
    const [comentarios, setComentarios] = useState([]);
    const [comentariosTitle, setComentariostitle] = useState([]);
    const [edit, setEdit] = useState(false);
    const [role, setRole] = useState("");
    const [commented, setCommented] = useState(false);
    const [logged, setLogged] = useState(false);
    const [usr, setUsr]=useState();
    const [showModal, setShowModal] = useState(false); // Estado para mostrar/ocultar el modal
    const [Commentary ,setCommentary] = useState(false);
    const [idComment, setIdComment] = useState("");

    useEffect(() => {
        if (isbn){
            const fetchCommentsData = async() =>{
                getComments();   
                getTitleComments(); 
            };
            fetchCommentsData();
                
        }
    }, [isbn]);

    useEffect(() => {
        buttonCatch();
        getUsr();
    },[]);
    
    useEffect(() => {
        if (comentarios.length > 0) {
            getValues();
        }
    }, [comentarios]);

    useEffect(() => {
        if (logged === true && usr){
            catchComment();
        }
    }, [logged, usr]);

    const getComments = async() => {
        try{
            const response = await fetch('/getComentarios?id='+isbn+'&role=STUDENT');
            if (!response.ok) throw new Error("Error en la respuesta del servidor");
            const data = await response.json();
            if (data && data!==null){
                setComentarios(data);
            } else {
                console.log("The response is empty");
            }
        } catch (error) {
            console.log("Error connecting to the server");
        }    
    };

    const getTitleComments = async() => {
        try{
            const response = await fetch('/getComentarios?id='+isbn+'&role=TITLED');
            if (!response.ok) throw new Error("Error en la respuesta del servidor");
            const data = await response.json();
            if (data && data!==null){
                setComentariostitle(data);
            } else {
                console.log("The response is empty");
            }
        } catch (error) {
            console.log("Error connecting to the server");
        }    
    };

    const getValues = () => {
        let values=0;
        let counter=0;
        comentarios.forEach(comentario => {
            values += parseInt(comentario.value);
            counter++;
        });
        let totalValue = counter > 0 ? values / counter : 0;
        setStudentvalue(Math.round(totalValue));

        values=0;
        counter=0;
        comentariosTitle.forEach(comentario => {
            values += parseInt(comentario.value);
            counter++;
       });
       totalValue = counter > 0 ? values /  counter : 0;
       setTitledvalue(Math.round(totalValue));
    };
   
    const buttonCatch = async() =>{
        const response = await fetch(`/getUserRole`);
        if(response.ok){
            const data = await response.json();
            
            if (['STUDENT'].includes(data)) {
                setLogged(true);
                setRole("STUDENT");
            } else if (['TITLED'].includes(data)) {
                setLogged(true);
                setRole("TITLED");
            } else if (['ADMIN'].includes(data)) {
                setLogged(true);
                setRole("ADMIN");
            } else {
                setLogged(false);
            }  
        } else {
            console.log("Error getting the response")
        }
        
    }; 
    const getUsr = async() =>{
        const response = await fetch(`/getUserIdent`);
        if(response.ok){
            const data = await response.json();
            if(data.status==="true"){
                setUsr(data.id);
                console.log(data.id);
            } else {
                setUsr(null);
                console.log("Non logged user");
            }
  
        } else {
            console.log("Error getting the response")
        }
        
    };    

    const catchComment = async() =>{
        if(usr!==null){
            try{
                const response = await fetch('/getCommented?id='+isbn+'&user='+usr);
                if (response.ok){
                    const data = await response.json();
                    if(data){
                        if(data.commented === "true") {
                            setCommented(true);
                            console.log("data:"+data.commented+", has comentado");
                        } else {
                            setCommented(false);
                            console.log("data:"+data.commented+", no has comentado");
                        }

                    } else {
                        console.log("response is empty");
                    }
                } else {
                    console.log("Couldn't get the response");
                }
            } catch (error){
                console.log("Error connecting with server");
            }
        } else {
            console.log("Non logged user");
        }
    };

    const signInMessage = () =>{
        if(!logged){
            setShowModal(true);
        }
    };
    
    const closeModal = () => {
        setShowModal(false);
    };

    const comment = () => {
        if (logged && role!=="" && !commented && isbn!=="" && usr!==""){
            setCommentary(true);
        }
    };

    const closeComment = () => {
        setCommentary(false);
    };

    const editComment = () => {
        {/*Aquí va el método para editar los comentarios*/}
        console.log("Editing Comment...");
        if (logged && role!=="" && !commented!=="false" && isbn!=="" && usr!==""){
            setEdit(true);
        } else {
            console.log("Cannot edit comment bacause any of this reasons: you are not logged, your role is not Student or Titled, you has not commented yet this writing, this writing doesn't exist");
        }
    };

    const closeEdit = () => {
        setEdit(false);
    };

    const getIdComment = async() =>{
        console.log("Getting the comment...");
        if (isbn && usr){
            const response = await fetch('/getIdComment?isbn='+isbn+'&usr='+usr);
            if (response.ok){
                const data = await response.json();
                if (data && data.status !== "false"){
                    setIdComment(data.idComment);
                    editComment();
                } else {
                    console.log("couldn't get the id");
                }
            } else {
                console.log('Response not ok');
            }
        } else {
            console.log("Id de usuario o ISBN son null");
        }
    };
    /* THIS IS TO TRY THE getIdComment METHOD
    useEffect(() => {
        console.log("El idComment actualizado es: "+ idComment);
    }, [idComment]);
    */
    return(
        
            <div id="workShowContainer">
                <div id="writingContainer">
                <WritingComponent isbn={isbn}/> 
                </div>
                

            {/* Mostrar el modal solo si showModal es true */}
            {showModal && <SignInMessageComponent onClose={closeModal} />}
            {Commentary && <CommentInsertComponent  onClose={closeComment} role={role} usr={usr} isbn={isbn} />}
            {edit && <EditCommentComponent onClose={closeEdit} idComment={idComment} usr={usr} isbn={isbn}/>}
                <div className="commentsContainer">
                    
                    <div className="commentTitle-value">
                        <h2 className="titleStudents" >Titled Comments</h2>
                        <p className="valoracionStudents">{titledValue}</p>
                    </div>
                    <div id="buttonContainer">
                    {
                        logged === false ? (
                            <button id="buttonComment" onClick={signInMessage}>Comment</button>
                        ) : logged === true && commented === false && role === "TITLED" ? (
                            <button id="buttonComment" onClick={comment}>Comment</button>
                        ) : logged === true && commented === true && role === "TITLED" ? (
                            <button id="buttonComment" onClick={getIdComment}>Edit Comment</button>
                        ) : logged === true && role === "ADMIN" ? (
                            null // Nada se muestra si es ADMIN
                        ) : null
                    }
                    </div>
                    <div className="commentsPlacement">
                        {comentariosTitle && comentariosTitle.length > 0 ? 
                        (comentariosTitle.map((comentario, index) => (
                            <ComentarioComponent
                            key={comentario.id} // React necesita una clave única para cada elemento en listas
                            username={comentario.username}
                            school={comentario.school}
                            value={comentario.value}
                            title={comentario.title}
                            text={comentario.text}
                            datetime={comentario.datetime}
                            />
                        ))) : (<p>There are no Comments yet</p>)}
                    </div>

                </div>

                <div className="commentsContainer">

                    <div className="commentTitle-value">
                        <h2 className="titleStudents" >Students Comments</h2>
                        <p className="valoracionStudents">{studentValue}</p>
                    </div>

                    <div id="buttonContainer">
                    {
                        logged === false ? (
                            <button id="buttonComment" onClick={signInMessage}>Comment</button>
                        ) : logged === true && commented === false && role === "STUDENT" ? (
                            <button id="buttonComment" onClick={comment}>Comment</button>
                        ) : logged === true && commented === true && role === "STUDENT" ? (
                            <button id="buttonComment" onClick={getIdComment}>Edit Comment</button>
                        ) : logged === true && role === "ADMIN" ? (
                            null // Nada se muestra si es ADMIN
                        ) : null
                    }
                    </div>

                    <div className="commentsPlacement">
                        {comentarios && comentarios.length > 0 ? (comentarios.map((comentario, index) => (
                            <ComentarioComponent
                            key={comentario.id} // React necesita una clave única para cada elemento en listas
                            username={comentario.username}
                            school={comentario.school}
                            value={comentario.value}
                            title={comentario.title}
                            text={comentario.text}
                            datetime={comentario.datetime}
                            />
                        ))): (<p>There are no Comments yet</p>)}
                    </div>

                    
                    
                
                </div>

            </div>
    );
}
export default WorkShowPage;