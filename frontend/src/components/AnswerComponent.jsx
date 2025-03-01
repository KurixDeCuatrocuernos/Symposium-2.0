import "./AnswerComponent.css"

function AnswerComponent({ username, school, role, title, text, datetime, id, admin }) {
    
    const banComment = async() => {
        
        let cell = confirm("¿Are you sure you want to ban the comment with id: "+id+"?");
        if (cell === true){
            try{
                const response = await fetch('/getBanComment?id='+id);
                if (response.ok){
                    const data = await response.json();
                    if (data.status==="true"){
                        window.location.reload();
                    } else {
                        cons("There was a problem in server: "+data.message);
                    }
                } else {
                    console.log("response is not ok!");
                }
            } catch (error){
                alert("There was a problem connecting with the server: "+error);
            }
        } 
    };

    const deleteComment = async() => {
        let cell = confirm("Are you sure you want to delete the comment with id: "+id+"?");
        if (cell === true) {
            try{
                const response = await fetch('/getDelComment?id='+id);
                if (response.ok){
                    const data = await response.json();
                    if (data.status==="true"){
                        window.location.reload();
                    } else {
                        console.log("There was a problem in server: "+data.message);
                    }
                } else {
                    console.log("response is not ok!");
                }
            } catch (error){
                alert("There was a problem connecting with the server: "+error);
            }
        }
    };

    const unbanComment = async() => {
        let cell = confirm("Are you sure you want to Unban the comment with id: "+id+"?");
        if (cell === true) {
            try{
                const response = await fetch('/getUnbanComment?id='+id);
                if (response.ok){
                    const data = await response.json();
                    if (data.status==="true"){
                        window.location.reload();
                    } else {
                        console.log("There was a problem in server: "+data.message);
                    }
                } else {
                    console.log("response is not ok!");
                }
            } catch (error){
                alert("There was a problem connecting with the server: "+error);
            }
        }
    };
    
    return(
        <>
        {admin===true ?
        <div id="AnswerContainer">
            <div id="userContent-AnswerComp">
                <img id="userImg-AnswerComp" src="usuario.png"></img>
                <div id="user-text-container-AnswerComp">
                    <p id="userName-AnswerComp">{username}</p>
                    <p id="userRole-AnswerComp">{school}</p>
                </div>
                
            </div>
            <div id="commentContent-AnswerComp">
                <p id="commentValue-AnswerComp">{role}</p>
                <div id="commentContext-text-AnswerComp">
                    <strong id="commentTitle-AnswerComp">{title}</strong>
                    <p id="commentText-AnswerComp">{text}</p>
                    <p id="commentDate-AnswerComp">{datetime}</p>
                    <del id="AdminButtons-AnswerComp">
                        <img onClick={deleteComment} src="prohibido.png"></img>
                        <img onClick={unbanComment} src="check.png"></img>
                    </del>
                </div>
            </div>
        </div>
        :
        <div id="AnswerContainer">
            <div id="userContent-AnswerComp">
                <img id="userImg-AnswerComp" src="usuario.png"></img>
                <div id="user-text-container-AnswerComp">
                    <p id="userName-AnswerComp">{username}</p>
                    <p id="userRole-AnswerComp">{school}</p>
                </div>
                
            </div>
            <div id="commentContent-AnswerComp">
                <p id="commentValue-AnswerComp">{role}</p>
                <div id="commentContext-text-AnswerComp">
                    <strong id="commentTitle-AnswerComp">{title}</strong>
                    <p id="commentText-AnswerComp">{text}</p>
                    <p id="commentDate-AnswerComp">{datetime}</p>
                    <img onClick={banComment} src="prohibido.png"></img>
                </div>
            </div>
        </div>
        }
        
        </>
    );
}

export default AnswerComponent;