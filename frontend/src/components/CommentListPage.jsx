import "./CommentListPage.css";
import { useState, useEffect } from "react";
import AnswerComponent from "./AnswerComponent";

function CommentListPage() {
    const [commentsList, setCommentsList] = useState([]);
    const [emptyMessage, setEmptyMessage] = useState("");
    const [commentDelList, setCommentDelList] = useState(true);

    useEffect(() => {
        getAllBanComments();
    }, []);

    const getAllBanComments = async () => {
        try {
            const response = await fetch('/getAllBanComments');
            if (response.ok) {
                const data = await response.json();
                if (data.status === "true") {
                    if (data.array === "false") {
                        setEmptyMessage("There are no banned comments yet");
                        console.log(data);
                    } else {
                        setCommentsList(data.array);
                    }
                } else {
                    alert(data.message);
                }
            } else {
                console.log("response not ok");
            }
        } catch (error) {
            console.log("error connecting with server to search: " + error);
        }
    };

    const searchComments = async (event) => {
        const search = event.target.value;
        if (search === "") {
            getAllBanComments();
        } else {
            try {
                const response = await fetch('/getSearchCommentList?search=' + search);
                if (response.ok) {
                    const data = await response.json();
                    if (data.status === "true") {
                        setCommentsList(data.array);
                    } else {
                        alert(data.message);
                    }
                } else {
                    console.log("response not ok");
                }
            } catch (error) {
                console.log("error connecting with server to search: " + error);
            }
        }
    };

    return (
        <>
            <div id="overlay-CommentListPage">
                <h1>Writings List</h1>
                <div id="searchContainer-WorkList">
                    <p>Search: </p>
                    <input type="search" onChange={searchComments}></input>
                    <img src="lupa.png" alt="Search" />
                </div>
                <div id="commentsContainer-CommentListPage">
                    {emptyMessage && <h1>{emptyMessage}</h1>}
                    {commentsList.length > 0 ? (
                        commentsList.map((comment) => (
                            <AnswerComponent
                                key={comment.comment.id}
                                id={comment.comment.id}
                                username={comment.username}
                                role={comment.userrole}
                                title={comment.comment.titulo}
                                text={comment.comment.texto}
                                datetime={comment.comment.fecha}
                                admin={commentDelList}
                            />
                        ))
                    ) : (
                        <p>No comments available</p>
                    )}
                </div>
            </div>
        </>
    );
}

export default CommentListPage;