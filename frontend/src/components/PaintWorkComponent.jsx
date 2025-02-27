import "./PaintWorkComponent.css";
import { useState, useEffect } from "react";

function PaintWorkComponent({ work, handleDelete, handleEdit }){

    const [title, setTitle] = useState(work.titulo || "Writing title");
    const [author, setAuthor] = useState( work.autor|| "Writing author");
    const [pubDate, setpubDate] = useState(work.fechaPublicacion || "Publication date");
    const [type, setType] = useState(work.tipo || "Writing type");
    const [edit, setEdit] = useState( work.editorial || "writing editor");

    return(
        <tr className="tableRow-WorksList">
            <td className="tableData-WorksList">{work.isbn}</td>
            <td className="tableData-WorksList">{title}</td>
            <td className="tableData-WorksList">{author}</td>
            <td className="tableData-WorksList">{pubDate}</td>
            <td className="tableData-WorksList">{type}</td>
            <td className="tableData-WorksList">{edit}</td>
            <td className="tableData-WorksList">
                <button id="EditButton-WorksList" onClick={() => handleEdit(work.isbn)}>Edit Writing</button>
                <button id="DeleteButton-WorksList" onClick={() => handleDelete(title,work.isbn)}>Delete Writing</button>
            </td>
        </tr>
    );
}
export default PaintWorkComponent;