import { useState, useEffect } from "react";
import "./PaintUserComponent.css";

function PaintUserComponent({ user, handleDelete, handleEdit }) {
    const [username, setUsername] = useState(user.name || "Nombre del usuario");
    const [email, setEmail] = useState(user.email || "Email del usuario");
    const [fnac, setFnac] = useState(user.fechaNac || "Fecha de nacimiento");
    const [role, setRole] = useState(user.role || "Rol del usuario");

    return (
        <tr className="tableRow-UsersList">
            <td className="tableData-UsersList">{user.id}</td>
            <td className="tableData-UsersList">{username}</td>
            <td className="tableData-UsersList">{email}</td>
            <td className="tableData-UsersList">{fnac}</td>
            <td className="tableData-UsersList">{role}</td>
            <td className="tableData-UsersList">
                <button id="EditButton-UsersList" onClick={() => handleEdit(user.id)}>Edit User</button>
                <button id="DeleteButton-UsersList" onClick={() => handleDelete(username,user.id)}>Delete User</button>
            </td>
        </tr>
    );
}


export default PaintUserComponent;