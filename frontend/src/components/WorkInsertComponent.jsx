import "./WorkInsertComponent.css";
import { useState, useEffect } from "react";

function WorkInsertComponent( { setShowNewWork } ){

     const [checked, setChecked] = useState("");
        const [isbn, setIsbn] = useState(""); 
        const [isbnAdvice, setIsbnAdvice] = useState("");
        const [isbnColor, setIsbnColor] = useState("");
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
            setChecked(type);
        }, [type]);
    
        const handleCancel = () => {
            setShowNewWork(false); // cierra el editor
        };
    
        const handleSave = () => {
            saveWork();
        };
    
        const handleIsbn = async (event) =>{
            const input = event.target.value;
            let isPresent = await checkIsbn(input);
            if(input.length > 13 || input.length < 8){
                setIsbnAdvice("The ISBN/ISSN number must be lower than 14 digits and bigger than 7");
                setIsbnColor("red");
            } else if (input.length===0) {
                setIsbnAdvice("The ISBN/ISSN number must be lower than 14 digits and bigger than 7");
                setIsbnColor("red");
            } else if (isPresent===true) {
                setIsbnAdvice("That ISBN/ISSN is already in the Database!");
                setIsbnColor("red");
            }else {
                setIsbn(input);
                setIsbnAdvice("");
                setIsbnColor("green");
            }
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

        const saveWork = async() =>{
            let cell = true;
            let isbnPresent = checkIsbn(isbn);
            console.log("editing user...");
            if (isbn === "") {
                cell=false;
                alert("A Writing must have an ISBN/ISSN number");
            } else if(isbn.length < 8 || isbn.length > 13) {
                cell=false;
                alert("An ISBN/ISSN number bust be lower than 14 and bigger than 7 digits");
            } else if (isbnPresent===true) {
                cell=false;
                alert("That ISBN/ISSN number is already in the database");
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

            if (type==="ARTICLE" && pageIni === "") {
                setPageIni(null);
            }
            if (type==="ARTICLE" && pageEnd === "") {
                setPageEnd(null);
            }
            let finalAbstract = abstract.trim() === "" ? "Without Abstract" : abstract;
            if (cell===true){
                const workData = {
                    isbn: isbn,
                    titulo: title,
                    autor: author,
                    tipo: type,
                    abstracto: finalAbstract,
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

        const checkIsbn = async(isbn) => {
            let isPresent=false;
            try{
                const response = await fetch('/geIsbnChecked?id='+isbn);
                if (response.ok) {
                    const data = await response.json();
                    if (data.status==="true") {
                        if(data.present==="true"){
                            isPresent=true;
                            console.log("ISBN present");
                        } else {
                            console.log("ISBN not present");
                        }
                    } else {
                        alert("There was an error inserting the work: "+data.message);
                    }
                } else {
                    console.log("response not ok: "+response)
                }
            } catch(error) {
                console.log("Couldn't connect with the server: "+error);
            }
            return(isPresent);
        };

    return(
        <>
            <div id="overlay-InsertWorkComp">
                <div id="container-InsertWorkComp">
                    <img src="advertencia.png"></img>
                    
                    <div className="container-div-InsertWorkComp">
                        <h3 className="h3Class-InsertWorkComp">ISBN: </h3>
                        <div className="inputContainer-InsertWorkComp">
                            <input className="inputClass-InsertWorkComp" type="number" onChange={handleIsbn} style={{color: isbnColor}}/>
                            <span className="spanClass-InsertWorkComp">{isbnAdvice}</span>
                        </div>
                    </div>
                    <div className="container-div-InsertWorkComp">
                        <h3 className="h3Class-InsertWorkComp">New Title: </h3>
                        <div className="inputContainer-InsertWorkComp">
                            <input className="inputClass-InsertWorkComp" type="text" onChange={handleTitle} value={title}></input>
                            <span className="spanClass-InsertWorkComp">{titleAdvice}</span>
                        </div>
                    </div>
                    <div className="container-div-InsertWorkComp">
                        <h3 className="h3Class-InsertWorkComp">New Publication Date: </h3>
                        <div className="inputContainer-InsertWorkComp">
                            <input className="inputClass-InsertWorkComp" type="date" onChange={handlePubDate} value={pubDate} max={new Date().toISOString().split("T")[0]}></input>
                            <span className="spanClass-InsertWorkComp">{pubDateAdvice}</span>
                        </div>
                    </div>
                    <div className="container-div-InsertWorkComp">
                        <h3 className="h3Class-InsertWorkComp">New Author: </h3>
                        <div className="inputContainer-InsertWorkComp">
                            <input className="inputClass-InsertWorkComp" type="text" onChange={handleAuthor} value={author}></input>
                            <span className="spanClass-InsertWorkComp">{authorAdvice}</span>
                        </div>
                    </div>
                    <div className="container-div-InsertWorkComp">
                        <h3 className="h3Class-InsertWorkComp">New Editorial: </h3>
                        <div className="inputContainer-InsertWorkComp">
                            <input className="inputClass-InsertWorkComp" type="text" onChange={handleEdit} value={edit}></input>
                            <span className="spanClass-InsertWorkComp">{editAdvice}</span>
                        </div>
                    </div>
                    <div className="container-div-InsertWorkComp">
                        <h3 className="h3Class-InsertWorkComp">New Publication Place: </h3>
                        <div className="inputContainer-InsertWorkComp">
                            <input className="inputClass-InsertWorkComp" type="text" onChange={handlePubPlace} value={pubPlace}></input>
                            <span className="spanClass-InsertWorkComp">{pubPlaceAdvice}</span>
                        </div>
                    </div>
                    <div className="container-div-InsertWorkComp">
                        <h3 className="h3Class-InsertWorkComp">New Themes: </h3>
                        <div className="inputContainer-InsertWorkComp"> {/*Revisar*/}
                            <div id="themesList-InsertWorkComp">
                                {themes.map((them) => (
                                    <p key={them} onClick={() => removeTheme(them)}>{them}</p>
                                ))}
                            </div>
                            <div id="themesList-InsertWorkComp">
                            <input className="inputClass-InsertWorkComp" type="text" onChange={handleTheme} value={theme}></input>
                            <button id="buttonTheme-InsertWorkComp" type="button" onClick={handleThemes}>OK</button>
                            </div>
                            <span className="spanClass-InsertWorkComp">{themeAdvice}</span>
                        </div>
                    </div>

                    <div className="container-div-InsertWorkComp">
                        <h3 className="h3Class-InsertWorkComp">New Abstract: </h3>
                        <div className="inputContainer-InsertWorkComp">
                            <textarea className="inputClass-InsertWorkComp" onChange={handleAbstract} value={abstract}></textarea>
                            <span className="spanClass-InsertWorkComp">{abstractAdvice}</span>
                        </div>
                    </div>

                    <div className="container-div-InsertWorkComp">
                        <h3 className="h3Class-InsertWorkComp">New Type: </h3>
                        <div id="CheckBoxRole-InsertWorkComp">
                            <label>
                                <input type="radio" name="type" value="BOOK" checked={checked==="BOOK"} onChange={()=> setType("BOOK")}></input>
                                BOOK
                            </label>
                            <label>
                                <input type="radio" name="role" value="ARTICLE" checked={checked==="ARTICLE"} onChange={()=> setType("ARTICLE")}></input>
                                ARTICLE
                            </label>
                        </div>
                        <span className="spanClass-InsertWorkComp">{typeAdvice}</span>
                    </div>
                    { checked === "ARTICLE" ?
                    <>
                        <div className="container-div-InsertWorkComp">
                            <h3 className="h3Class-InsertWorkComp">New Start Page: </h3>
                            <div className="inputContainer-InsertWorkComp">
                                <input className="inputClass-InsertWorkComp" type="number" onChange={handlePageIni} value={pageIni}></input>
                                <span className="spanClass-InsertWorkComp">{pageIniAdvice}</span>
                            </div>
                        </div>
                        <div className="container-div-InsertWorkComp">
                            <h3 className="h3Class-InsertWorkComp">New End Page: </h3>
                            <div className="inputContainer-InsertWorkComp">
                                <input className="inputClass-InsertWorkComp" type="number" onChange={handlePageEnd} value={pageEnd}></input>
                                <span className="spanClass-InsertWorkComp">{pageEndAdvice}</span>
                            </div>
                        </div>
                    </>

                    : ''
                    }
                    
                    <div id="buttons-InsertWorkComp">
                        <button onClick={handleSave}>Save Changes</button>
                        <button onClick={handleCancel}>Cancel</button>
                    </div>
                </div>
            </div>
        </>
    );
}
export default WorkInsertComponent;