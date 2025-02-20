import "./ComentarioComponent.css";

function ComentarioComponent(){

    return(
        <div id="CommentContainer">
            <div id="userContent">
                <img id="userImg" src="usuario.png"></img>
                <div id="user-text-container">
                    <p id="userName">Juan</p>
                    <p id="userRole">Titulado</p>
                </div>
                
            </div>
            <div id="commentContent">
                <p id="commentValue">100</p>
                <div id="commentContext-text">
                    <strong id="commentTitle">Una ayuda para entender El Banquete</strong>
                    <p id="commentText">Una ayuda tanto para entender la obra original, como para entendernos a nosotros mismos.</p>
                    <p id="commentDate">2025-2-12- 18:50</p>
                </div>
            </div>
        </div>
    );

}
export default ComentarioComponent;