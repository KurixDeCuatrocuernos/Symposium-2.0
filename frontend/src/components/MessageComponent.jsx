import "./MessageComponent.css";

function MessageComponent( text, username) {

    return(
        <>
        <div id="overlay-messageComp">
            <img src="usuario.png"></img>
            <h3>{username}</h3>
            <em>{text}</em>
        </div>
        </>
    );
} 
export default MessageComponent;