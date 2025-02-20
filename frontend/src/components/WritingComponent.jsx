import "./WritingComponent.css";
import { useEffect, useState, useRef } from "react";
import { useSearchParams } from "react-router-dom";

function WritingComponent(){

    const [titulo, setTitulo] = useState();
    const [totalValue, setTotalValue] = useState(); // Estado del valor
    const valueColorRef = useRef(null); // Referencia al elemento

    useEffect(() => {
        if (totalValue <= 20 && valueColorRef.current) {
            valueColorRef.current.style.backgroundColor = "rgba(235, 0, 0, 0.9)";
        } else if (totalValue > 20  && totalValue <= 40 && valueColorRef.current) {
            valueColorRef.current.style.backgroundColor = "rgba(250, 142, 1, 0.925)";
        } else if (totalValue > 40 && totalValue <= 60 && valueColorRef.current){
            valueColorRef.current.style.backgroundColor = "rgb(202, 206, 5)";
        } else if (totalValue > 60 && totalValue <= 80 && valueColorRef.current){
            valueColorRef.current.style.backgroundColor ="rgba(162, 231, 0, 0.75)"
        } else if (totalValue > 80 && totalValue <= 100 && valueColorRef.current){
            valueColorRef.current.style.backgroundColor ="rgba(4, 156, 4, 0.84)"
        }
    }, [totalValue]); 

   {/*Long isbn, LocalDate fechaPublicacion, String titulo, String autor, 
    String tipo, String abstracto, String lugar_publicacion, 
    List<String> temas, String editorial, int paginaini, int paginafin;*/}
    const [searchParams] = useSearchParams();
    const isbn = searchParams.get("id");
    const publicDate = "1999-2-23";
    const [autor, setAutor] = useState();
    const [place, setPlace] = useState();
    const [edit, setEdit]= useState();
    const [article, setArticle] = useState();
    const [iniPage, setIniPage] = useState();
    const [endPage, setEndPage] = useState();
    const [abstract, setAbstract] = useState();

    const writing = async() =>{
        if(!isbn) return;
        try{
            const response = await fetch('/getWriting?id='+isbn);
            const data = await response.json();

            if (data && data !== null && data.type==="BOOK"){
                console.log(data);
                setArticle(false);
                setTitulo(data.titulo);
                setAutor(data.autor);
                setPlace(data.place);
                setEdit(data.edit);
                setAbstract(data.abstract);
                setTotalValue(data.valoracion);

            } else if (data && data !== null && data.type==="ARTICLE") {
                setArticle(true);
                setTitulo(data.titulo)
                setAutor(data.autor);
                setPlace(data.place);
                setEdit(data.edit);
                setAbstract(data.abstract);
                setTotalValue(data.valoracion);
                setIniPage(data.paginaIni);
                setEndPage(data.paginaEnd);
            }else {
                console.log("Server has not returned data");
            }
        } catch (error){
            console.log("There was an error getting the data from the server");
            
        }
    }

    useEffect(()=>{
        writing();
    }, [isbn]);

    return(
        <div id="WritingContainer">
            <div id="WritingContainer-head">
                {/*Imagen, titulo y valoración*/ }
                <img id="bookImg" src="libro-cerrado.png"></img>
                <div id="titleContainer">
                    <h1>{titulo}</h1>
                </div>
                <h1 id="globalValue" ref={valueColorRef}>{totalValue}</h1>
            </div>
            <div id="WritingContainer-data">
                {/*Datos*/}
                <div className="data-label">
                    <strong>Isbn: </strong><p className="workData">{isbn}</p>
                </div>
                <div className="data-label">
                    <strong>Publication Date: </strong><p className="workData">{publicDate}</p>
                </div>
                <div className="data-label">
                    <strong>Author: </strong><p className="workData">{autor}</p>
                </div>
                <div className="data-label">
                    <strong>Publication place: </strong><p className="workData">{place}</p>
                </div>
                <div className="data-label">
                    <strong>Editorial: </strong><p className="workData">{edit}</p>
                </div>
                
                {article ? (
                    <>
                    <div className="data-label">
                        <strong>Init Page: </strong><p className="workData">{iniPage}</p>
                    </div>
                    <div className="data-label">
                        <strong>End Page: </strong><p className="workData">{endPage}</p>
                    </div>
                    </>
                ) : (
                    <>
                    </>
                )}
            </div>
            <div id="WritingContainer-description">
                {/*Sinopsis*/}
                <p className="workData">{abstract}</p>
            </div>
        </div>
    );

}

export default WritingComponent;