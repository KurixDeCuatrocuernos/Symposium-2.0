import "./WorkListPage.css";
import { useState, useEffect } from "react";
import PaintWorkComponent from "./PaintWorkComponent";
import DeleteWorkAdviceComponent from "./DeleteWorkAdviceComponent";
import EditWorkComponent from "./EditWorkComponent";
import WorkInsertComponent from "./WorkInsertComponent";

function WorkListPage(){

    const [showEditComp, setShowEditComp] = useState(false);
    const [showDeleteAdvice, setShowDeleteAdvice] = useState(false); // Estado para controlar la visibilidad del componente DeleteAdvice
    const [showNewWork, setShowNewWork] = useState(false);
    const [id, setId] = useState("");
    const [title, setTitle] = useState("");
    const [works, setWorks] = useState([]);

    useEffect(() => {
        fetchWorks();
    }, []);

    const handleNewWork = () => {
        setShowNewWork(true);
        console.log("Writing form open!");
    };

    const handleDeleteWork = (title, id) => {
        setTitle(title);  // Guardar el usuario que va a ser eliminado
        setId(id);
        setShowDeleteAdvice(true);  // Mostrar el componente DeleteAdvice
    };

    const handleEditWork = (id) => {
        setId(id);
        setShowEditComp(true);  // Mostrar el componente DeleteAdvice
    };

    const fetchWorks = async () => {
        try {
            const response = await fetch("/getAllIdWorks");
            if (response.ok) {
                const data = await response.json();
                if (data.status === "true" && Array.isArray(data.array)) {
                    setWorks(data.array);  // Guardamos la lista de obras completa
                    console.log(data.array);
                } else {
                    console.error("Server error: " + data.message);
                }
            } else {
                console.error("Couldn't connect with server");
            }
        } catch (error) {
            console.error("Error fetching users: " + error);
        }
    };

    const searchWorks = async(event) => {
        const search = event.target.value;
        try{
            const response = await fetch('/getSearchWorkList?search='+search);
            if (response.ok) {
                const data = await response.json();
                if (data.status === "true"){
                    setWorks(data.array);
                } else {
                    alert(data.message);
                }
            } else {
                console.log("response not ok");
            }
        } catch(error) {
            console.log("error connecting with server to search: "+error)
        }
    };

    return(
        
        <>
        
        {showDeleteAdvice && <DeleteWorkAdviceComponent id={id} title={title} setShowDeleteAdvice={setShowDeleteAdvice} />}
        {showEditComp && <EditWorkComponent id={id} setShowEditWork={setShowEditComp} />}
        {showNewWork && <WorkInsertComponent setShowNewWork={setShowNewWork} />}
        <div id="overlay-WorkList">
            <h1>Writings List</h1>
            <div id="searchContainer-WorkList">
                <p>Search: </p>
                <input type="search" onChange={searchWorks}></input>
                <img src="lupa.png" alt="Search" />
                <button id="ButtonWorkInsert-WorkListPage" onClick={handleNewWork} >New Writing</button>
            </div>
            
            <div id="dataContainer-WorkList">
                <table className="table-WorkList">
                    <thead className="thead-WorkList">
                        <tr>
                            <th className="tableHead-WorkList">ISBN</th>
                            <th className="tableHead-WorkList">TITLE</th>
                            <th className="tableHead-WorkList">AUTHOR</th>
                            <th className="tableHead-WorkList">PUBLICATION DATE</th>
                            <th className="tableHead-WorkList">TYPE</th>
                            <th className="tableHead-WorkList">EDITOR</th>
                            <th className="tableHead-WorkList">ACTIONS</th>
                        </tr>
                    </thead>
                    <tbody id="tbody-WorkList" className="tableBody-WorkList">
                        
                        {works.map((work) => (
                            <PaintWorkComponent key={work.isbn} work={work} handleDelete={handleDeleteWork} handleEdit={handleEditWork} />
                        ))}

                    </tbody>
                </table>
            </div>
        </div>
    </>
    );
}
export default WorkListPage;