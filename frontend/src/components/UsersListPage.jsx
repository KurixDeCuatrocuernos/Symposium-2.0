import PaintUserComponent from "./PaintUserComponent";
import DeleteAdviceComponent from "./DeleteAdviceComponent";
import "./UsersListPage.css";
import { useState, useEffect } from "react";
import EditUserComponent from "./EditUserComponent";

function UsersListPage() {
    const [users, setUsers] = useState([]);
    const [showDeleteAdvice, setShowDeleteAdvice] = useState(false); // Estado para controlar la visibilidad del componente DeleteAdvice
    const [showEditComp, setShowEditComp] = useState(false);
    const [userToDelete, setUserToDelete] = useState(null); // Estado para guardar el usuario que va a ser eliminado
    const [userId, setUserId] = useState("");

    useEffect(() => {        
        fetchUsers();
    }, []);

    const handleDeleteUser = (username, id) => {
        setUserToDelete(username);  // Guardar el usuario que va a ser eliminado
        setUserId(id);
        setShowDeleteAdvice(true);  // Mostrar el componente DeleteAdvice
    };

    const handleEditUser = (id) => {
        setUserId(id);
        setShowEditComp(true);  // Mostrar el componente DeleteAdvice
    };

    const fetchUsers = async () => {
        try {
            const response = await fetch("/getAllIdUsers");
            if (response.ok) {
                const data = await response.json();
                if (data.status === "true" && Array.isArray(data.array)) {
                    setUsers(data.array);  // Guardamos la lista de usuarios completa
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

    const searchUsers = async(event) => {
        const search = event.target.value;
        try{
            const response = await fetch('/getSearchUsersList?search='+search);
            if (response.ok) {
                const data = await response.json();
                if (data.status === "true"){
                    setUsers(data.array);
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


    return (
        <>
            {showDeleteAdvice && <DeleteAdviceComponent id={userId} username={userToDelete} setShowDeleteAdvice={setShowDeleteAdvice} />}
            {showEditComp && <EditUserComponent id={userId} setShowEditUser={setShowEditComp}/>}
            <div id="overlay-UsersList">
                <h1>Users List</h1>
                <div id="searchContainer-UsersList">
                    <p>Search: </p>
                    <input type="search" onChange={searchUsers}></input>
                    <img src="lupa.png" alt="Search" />
                </div>
                <div id="dataContainer-UsersList">
                    <table className="table-UsersList">
                        <thead className="thead-UsersList">
                            <tr>
                                <th className="tableHead-UsersList">ID</th>
                                <th className="tableHead-UsersList">NAME</th>
                                <th className="tableHead-UsersList">EMAIL</th>
                                <th className="tableHead-UsersList">BIRTHDAY</th>
                                <th className="tableHead-UsersList">ROLE</th>
                                <th className="tableHead-UsersList">ACTIONS</th>
                            </tr>
                        </thead>
                        <tbody id="tbody-UsersList" className="tableBody-UsersList">
                            {users.map((user) => (
                                <PaintUserComponent key={user.id} user={user} handleDelete={handleDeleteUser} handleEdit={handleEditUser} />
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </>
    );
}


export default UsersListPage;