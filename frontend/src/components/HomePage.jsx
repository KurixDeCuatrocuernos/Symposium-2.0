import "./HomePage.css";
import { useState, useEffect, useRef } from "react";

function HomePage(){

    const [writing, setWriting] = useState([]);
    const [writingValue, setWritingValue] = useState([]);
    const [commentStudent, setCommentStudent] = useState([]);
    const [commentTitled, setCommentTitled] = useState([]);
    const [totalValue, setTotalValue] = useState(); // Estado del valor
    const [totalValue2, setTotalValue2] = useState(); // Estado del valor
    const valueColorRef = useRef(null); // Referencia al elemento
    const valueColorRef2 = useRef(null);

    useEffect(()=>{
        const fetchData = async () => {
            await getNewestWriting();
            await getMostValuedWriting();
            await getTitledComment(); // Esto actualizará `setTotalValue`
            await getStudentComment(); // Esto actualizará `setTotalValue2`
        };
    
        fetchData();
    }, []);

     useEffect(() => {
        console.log(totalValue);
        if (valueColorRef.current) {
            if (typeof totalValue === 'number') {
                console.log("Valor de totalValue:", totalValue);
                if (totalValue <= 20) {
                    valueColorRef.current.style.backgroundColor = "rgba(235, 0, 0, 0.9)";
                } else if (totalValue > 20 && totalValue <= 40) {
                    valueColorRef.current.style.backgroundColor = "rgba(250, 142, 1, 0.925)";
                } else if (totalValue > 40 && totalValue <= 60) {
                    valueColorRef.current.style.backgroundColor = "rgb(202, 206, 5)";
                } else if (totalValue > 60 && totalValue <= 80) {
                    valueColorRef.current.style.backgroundColor = "rgba(162, 231, 0, 0.75)";
                } else if (totalValue > 80 && totalValue <= 100) {
                    valueColorRef.current.style.backgroundColor = "rgba(4, 156, 4, 0.84)";
                }
            }
        }
    }, [totalValue]);

    useEffect(() => {
        console.log(totalValue2);
        if (valueColorRef2.current) {
            if (typeof totalValue2 === 'number') {
                console.log("Valor de totalValue2:", totalValue2);
                if (totalValue2 === 0) {
                    valueColorRef2.current.style.backgroundColor = "rgba(235, 0, 0, 0.9)"; // Color para 0
                } else if (totalValue2 > 0 && totalValue2 <= 20) {
                    valueColorRef2.current.style.backgroundColor = "rgba(235, 0, 0, 0.9)";
                } else if (totalValue2 > 20 && totalValue2 <= 40) {
                    valueColorRef2.current.style.backgroundColor = "rgba(250, 142, 1, 0.925)";
                } else if (totalValue2 > 40 && totalValue2 <= 60) {
                    valueColorRef2.current.style.backgroundColor = "rgb(202, 206, 5)";
                } else if (totalValue2 > 60 && totalValue2 <= 80) {
                    valueColorRef2.current.style.backgroundColor = "rgba(162, 231, 0, 0.75)";
                } else if (totalValue2 > 80 && totalValue2 <= 100) {
                    valueColorRef2.current.style.backgroundColor = "rgba(4, 156, 4, 0.84)";
                }
            }
        }
    }, [totalValue2]);

    const redirect = (dir) => {
        window.location.href="workShow?id="+dir;
    };

    const getNewestWriting = async() => {
        try{
            const response = await fetch('/getNewestWriting');
            if (response.ok) {
                const data = await response.json();
                if (data.status==="true") {
                    setWriting(data.writing);
                } else {
                    console.log("There was a problem in server: "+data.message);
                }
            } else {
                console.log("Response is not ok!");
            }
        } catch(error) {
            alert("Couldn't connect with server: "+error);
        }
    };

    const getMostValuedWriting = async() => {
        try{
            const response = await fetch('/getMostValuedWriting');
            if (response.ok) {
                const data = await response.json();
                if (data.status==="true") {
                    setWritingValue(data.writing);
                } else {
                    console.log("There was a problem in server: "+data.message);
                }
            } else {
                console.log("Response is not ok!");
            }
        } catch(error) {
            alert("Couldn't connect with server: "+error);
        }
    };

    const getTitledComment = async() => {
        try{
            const response = await fetch('/getTitledComment');
            if (response.ok) {
                const data = await response.json();
                if (data.status==="true") {
                    setCommentTitled(data.comment);
                    setTotalValue(data.comment.comment.valoracion);
                    console.log(data.comment.comment.valoracion);
                } else {
                    console.log("There was a problem in server: "+data.message);
                }
            } else {
                console.log("Response is not ok!");
            }
        } catch(error) {
            alert("Couldn't connect with server: "+error);
        }
    };

    const getStudentComment = async() => {
        try{
            const response = await fetch('/getStudentComment');
            if (response.ok) {
                const data = await response.json();
                if (data.status==="true") {
                    setCommentStudent(data.comment);
                    setTotalValue2(data.comment.comment.valoracion);
                    console.log(data.comment);
                } else {
                    console.log("There was a problem in server: "+data.message);
                }
            } else {
                console.log("Response is not ok!");
            }
        } catch(error) {
            alert("Couldn't connect with server: "+error);
        }
    };

   

    
    return(
        <>
        <div className="HomePage-Container" onClick={() => redirect(writing.isbn)}>
            <h1>Newest Writting</h1>
            <div className="HomePage-WritingComp">
                <img src="libro-cerrado.png"></img>
                <div className="HomePage-WritingComp-TitleAutor">
                    <h2>{writing.titulo}</h2>
                    <h3>{writing.autor}</h3>
                </div>

                <h3>{writing.abstracto}</h3>
            </div>
        </div>
        <div className="HomePage-Container" onClick={() => redirect(writingValue.isbn)}>
            <h1>Best Valued Writting</h1>
            <div className="HomePage-WritingComp">
                <img src="libro-cerrado.png"></img>
                <div className="HomePage-WritingComp-TitleAutor">
                    <h2>{writingValue.titulo}</h2>
                    <h3>{writingValue.autor}</h3>
                </div>
                <h3>{writingValue.abstracto}</h3>
            </div>
        </div>
        <div className="HomePage-Container">
            <h1>Last Comment Titled</h1>
            {commentTitled && commentTitled.username && commentTitled.comment ? (
                <div className="HomePage-CommentComp">
                    <img src="usuario.png" alt="Usuario" />
                    <div className="StudentData-CommentComp">
                        <h2>{commentTitled.username}</h2>
                        <h3>{commentTitled.userrole}</h3>
                        <div id="StudentData-CommentComp-value" ref={valueColorRef}>
                        <h3>{commentTitled.comment && commentTitled.comment.valoracion !== undefined ? commentTitled.comment.valoracion : '50'}</h3>

                        </div>
                    </div>
                    <div className="CommentData-CommentComp">
                        <h2>{commentTitled.comment.titulo}</h2>
                        <h3>{commentTitled.comment.texto}</h3> 
                    </div>
                </div>
            ) : (
                <p>No comment available</p> // En caso de que no haya datos
            )}
        </div>
        <div className="HomePage-Container">
            <h1>Last Comment Student</h1>
            {commentStudent && commentStudent.username && commentStudent.comment ? (
            <div className="HomePage-CommentComp">
                <img src="usuario.png"></img>
                <div className="StudentData-CommentComp">
                    <h2>{commentStudent.username}</h2>
                    <h3>{commentStudent.userrole}</h3>
                    <div id="StudentData-CommentComp-value" ref={valueColorRef2}>
                    <h3>{commentStudent.comment && commentStudent.comment.valoracion !== undefined ? commentStudent.comment.valoracion : 'Null'}</h3>

                    </div>
                    </div>
                    <div className="CommentData-CommentComp">
                        <h2>{commentStudent.comment.titulo}</h2>
                        <h3>{commentStudent.comment.texto}</h3> 
                    </div>
            </div>
            ) : (
                <p>No comment available</p> // En caso de que no haya datos
            )}
        </div>
        </>
    );

}
export default HomePage;