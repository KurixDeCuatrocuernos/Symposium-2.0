import "./ComentarioComponent.css";
import React, { useState, useEffect } from "react";

function ComentarioComponent({ username, school, value, title, text, datetime }){

    return(
        <div id="CommentContainer">
            <div id="userContent">
                <img id="userImg" src="usuario.png"></img>
                <div id="user-text-container">
                    <p id="userName">{username}</p>
                    <p id="userRole">{school}</p>
                </div>
                
            </div>
            <div id="commentContent">
                <p id="commentValue">{value}</p>
                <div id="commentContext-text">
                    <strong id="commentTitle">{title}</strong>
                    <p id="commentText">{text}</p>
                    <p id="commentDate">{datetime}</p>
                </div>
            </div>
        </div>
    );

}
export default ComentarioComponent;