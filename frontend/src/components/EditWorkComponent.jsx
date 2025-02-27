import { useState,useEffect } from "react";
import "./EditWorkComponent.css";

function EditWorkComponent({ id, setShowEditWork }){

    const [checked, setChecked] = useState("");
    const [isbn, setIsbn] = useState(id); 
    const [title, setTitle] = useState("");
    const [titleAdvice, setTitleAdvice] = useState("");
    const [pubDate, setPubDate] = useState("");
    const [pubDateAdvice, setPubDateAdvice] = useState("");
    const [author, setAuthor] = useState("");
    const [authorAdvice, setAuthorAdvice] = useState("");
    const [type, setType] = useState("");
    const [typeAdvice, setTypeAdvice] = useState("");
    const [edit, setEdit] = useState("");
    const [editAdvice, setEditAdvice] = useState("");
    const [pubPlace, setPubPlace] = useState("");
    const [pubPlaceAdvice, setPubPlaceAdvice] = useState("");
    const [themes, setThemes] = useState([]);
    const [theme, setTheme] = useState("");
    const [themeAdvice, setThemeAdvice] = useState("");
    const [abstract, setAbstract] = useState("");
    const [abstractAdvice, setAbstractAdvice] = useState("");
    const [pageIni, setPageIni] = useState("");
    const [pageIniAdvice, setPageIniAdvice] = useState("");
    const [pageEnd, setPageEnd] = useState("");
    const [pageEndAdvice, setPageEndAdvice] = useState("");

    useEffect(() => {
        getWorkData(id);
    }, []);

    useEffect(() => {
        setChecked(type);
    }, [type]);

    const handleCancel = () => {
        setShowEditWork(false); // cierra el editor
    };

    const handleSave = () => {
        saveWork();
    };

    const handleTitle = (event) => {
        setTitle(event.target.value);
    };

    const handlePubDate = (event) => {
        const selectedDate = new Date(event.target.value);
        const today = new Date();
        if (selectedDate<=today) {
        selectedDate.setMonth(0);
        selectedDate.setDate(1);
        const formattedDate = selectedDate.toISOString().split("T")[0];
        setPubDate(formattedDate);  
        } else {
            setPubDateAdvice("The writing must be pubished today at most");
            setPubDate(pubDate);
        }

    };
    
    const handleAuthor = (event) => {
        setAuthor(event.target.value);
    };

    const handleType = (event) =>{
        setType(event.target.value);
    };

    const handleEdit = (event) => {
        setEdit(event.target.value);
    };

    const handlePubPlace = (event) => {
        setPubPlace(event.target.value);
    };

    const handleTheme = (event) => {
        setTheme(event.target.value);
    };

    const handleThemes = () => {
        if (theme.trim() !== "" && !themes.includes(theme)) {
            setThemes([...themes, theme]); // Agregar nuevo tema
            setTheme(""); // Limpiar el input
        }
    };
    

    const handleAbstract = (event) => {
        setAbstract(event.target.value);
    };

    const handlePageIni = (event) => {
        setPageIni(event.target.value);
    };

    const handlePageEnd = (event) => {
        setPageEnd(event.target.value);
    };

    const removeTheme = (themeToRemove) => {
        setThemes(themes.filter(t => t !== themeToRemove)); // Filtra los temas excluyendo el seleccionado
    };

    const getWorkData = async(id) => {
        try{
            const response = await fetch('/getWorkToEdit?id='+id);
            if (response.ok){
                const data = await response.json();
                if (data.status==="true"){
                    setTitle(data.title); 
                    setAuthor(data.autor);
                    setPubDate(data.publicationDate);
                    setType(data.type);
                    setAbstract(data.abstract);
                    setEdit(data.editorial);
                    setPubPlace(data.publicationPlace)
                    setThemes(JSON.parse(data.temas))
                    
                    if(data.type==="ARTICLE"){    
                        setPageIni(data.PageIni);
                        setPageEnd(data.PaginaFin);                        
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

    const saveWork = async() =>{
        let cell = true;
        console.log("editing user...");
        if (isbn === "") {
            cell=false;
            alert("A Writing must have an ISBN number, error happened, cannot edit works, try later, if persists, check the server status.");
        }
        if (title === "") {
            cell=false;
            setTitleAdvice("Writings must have a Title!");
        }
        if (pubPlace === "") {
            cell=false;
            setPubPlaceAdvice("Writings must be published somewhere, use the Books editorial or the magazine where its publised in Articles case");
        }
        if (author === "") {
            cell=false;
            setAuthorAdvice("Writings must have an Author!");
        }
        if (edit === "") {
            cell=false;
            setEditAdvice("Writings mus be published somewhere, use the country or city in Books case, and the book's or magazine's title in Articles case!");
        }
        if (pubDate === "") {
            cell=false;
            setPubDateAdvice("Writings must have a Publication date!");
        }
        if (themes.length === 0) {
            cell=false;
            setThemeAdvice("Writings must have at least one type!");
        }
        if (type === "") {
            cell=false;
            setTypeAdvice("Choose one type, BOOK or ARTICLE!");
        }
        if (abstract === "") {
            setAbstract("Without Abstract");
        }
        if (type==="ARTICLE" && pageIni === "") {
            setPageIni(null);
        }
        if (type==="ARTICLE" && pageEnd === "") {
            setPageEnd(null);
        }
        if (cell===true){
            const workData = {
                isbn: isbn,
                titulo: title,
                autor: author,
                tipo: type,
                abstracto: abstract,
                lugar_publicacion: pubPlace,
                fechaPublicacion: pubDate,
                editorial: edit,
                temas: themes,
            };
            if (type==="ARTICLE" && pageIni!==null) {
                workData.paginaini = pageIni
            }
            if (type==="ARTICLE" && pageEnd!==null) {
                workData.paginafin = pageEnd
            }
            try{
                const response = await fetch('/postWorkInsert', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(workData)
                });
                if(response.ok){
                    const data = await response.json();
                    if (data.status === "true"){
                        window.location.reload();
                    } else {
                        alert("response status = false: "+response.message);
                    }
                } else {
                    alert("response is not ok");
                }
            } catch (error) {
                alert("There was an error connecting with server: "+error);
            }
        } else {
            alert("Some fields are empty, check the form!");
        }
    };

    return(
        <>
        <div id="overlay-EditWorkComp">
            <div id="container-EditWorkComp">
                <img src="advertencia.png"></img>
                
                <div className="container-div-EditWorkComp">
                    <h3 className="h3Class-EditWorkComp">ISBN: </h3>
                    <div className="inputContainer-EditWorkComp">
                        <input className="inputClass-EditWorkComp" type="text" value={id} readOnly />
                        <span className="spanClass-EditWorkComp">Isbn cannot be changed here</span>
                    </div>
                </div>
                <div className="container-div-EditWorkComp">
                    <h3 className="h3Class-EditWorkComp">New Title: </h3>
                    <div className="inputContainer-EditWorkComp">
                        <input className="inputClass-EditWorkComp" type="text" onChange={handleTitle} value={title}></input>
                        <span className="spanClass-EditWorkComp">{titleAdvice}</span>
                    </div>
                </div>
                <div className="container-div-EditWorkComp">
                    <h3 className="h3Class-EditWorkComp">New Publication Date: </h3>
                    <div className="inputContainer-EditWorkComp">
                        <input className="inputClass-EditWorkComp" type="date" onChange={handlePubDate} value={pubDate} max={new Date().toISOString().split("T")[0]}></input>
                        <span className="spanClass-EditWorkComp">{pubDateAdvice}</span>
                    </div>
                </div>
                <div className="container-div-EditWorkComp">
                    <h3 className="h3Class-EditWorkComp">New Author: </h3>
                    <div className="inputContainer-EditWorkComp">
                        <input className="inputClass-EditWorkComp" type="text" onChange={handleAuthor} value={author}></input>
                        <span className="spanClass-EditWorkComp">{authorAdvice}</span>
                    </div>
                </div>
                <div className="container-div-EditWorkComp">
                    <h3 className="h3Class-EditWorkComp">New Editorial: </h3>
                    <div className="inputContainer-EditWorkComp">
                        <input className="inputClass-EditWorkComp" type="text" onChange={handleEdit} value={edit}></input>
                        <span className="spanClass-EditWorkComp">{editAdvice}</span>
                    </div>
                </div>
                <div className="container-div-EditWorkComp">
                    <h3 className="h3Class-EditWorkComp">New Publication Place: </h3>
                    <div className="inputContainer-EditWorkComp">
                        <input className="inputClass-EditWorkComp" type="text" onChange={handlePubPlace} value={pubPlace}></input>
                        <span className="spanClass-EditWorkComp">{pubPlaceAdvice}</span>
                    </div>
                </div>
                <div className="container-div-EditWorkComp">
                    <h3 className="h3Class-EditWorkComp">New Themes: </h3>
                    <div className="inputContainer-EditWorkComp"> {/*Revisar*/}
                        <div id="themesList-EditWorkComp">
                            {themes.map((them) => (
                                <p key={them} onClick={() => removeTheme(them)}>{them}</p>
                            ))}
                        </div>
                        <div id="themesList-EditWorkComp">
                        <input className="inputClass-EditWorkComp" type="text" onChange={handleTheme} value={theme}></input>
                        <button id="buttonTheme-EditWorkComp" type="button" onClick={handleThemes}>OK</button>
                        </div>
                        <span className="spanClass-EditWorkComp">{themeAdvice}</span>
                    </div>
                </div>

                <div className="container-div-EditWorkComp">
                    <h3 className="h3Class-EditWorkComp">New Abstract: </h3>
                    <div className="inputContainer-EditWorkComp">
                        <textarea className="inputClass-EditWorkComp" onChange={handleAbstract} value={abstract}></textarea>
                        <span className="spanClass-EditWorkComp">{abstractAdvice}</span>
                    </div>
                </div>

                <div className="container-div-EditWorkComp">
                    <h3 className="h3Class-EditWorkComp">New Type: </h3>
                    <div id="CheckBoxRole-EditWorkComp">
                        <label>
                            <input type="radio" name="type" value="BOOK" checked={checked==="BOOK"} onChange={()=> setType("BOOK")}></input>
                            BOOK
                        </label>
                        <label>
                            <input type="radio" name="role" value="ARTICLE" checked={checked==="ARTICLE"} onChange={()=> setType("ARTICLE")}></input>
                            ARTICLE
                        </label>
                    </div>
                    <span className="spanClass-EditWorkComp">{typeAdvice}</span>
                </div>
                { checked === "ARTICLE" ?
                <>
                    <div className="container-div-EditWorkComp">
                        <h3 className="h3Class-EditWorkComp">New Start Page: </h3>
                        <div className="inputContainer-EditWorkComp">
                            <input className="inputClass-EditWorkComp" type="number" onChange={handlePageIni} value={pageIni}></input>
                            <span className="spanClass-EditWorkComp">{pageIniAdvice}</span>
                        </div>
                    </div>
                    <div className="container-div-EditWorkComp">
                        <h3 className="h3Class-EditWorkComp">New End Page: </h3>
                        <div className="inputContainer-EditWorkComp">
                            <input className="inputClass-EditWorkComp" type="number" onChange={handlePageEnd} value={pageEnd}></input>
                            <span className="spanClass-EditWorkComp">{pageEndAdvice}</span>
                        </div>
                    </div>
                </>

                : ''
                }
                
                <div id="buttons-EditWorkComp">
                    <button onClick={handleSave}>Save Changes</button>
                    <button onClick={handleCancel}>Cancel</button>
                </div>
            </div>
        </div>
        </>
    );
}

export default EditWorkComponent;