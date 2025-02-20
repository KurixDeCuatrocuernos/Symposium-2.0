import ComentarioComponent from "./ComentarioComponent";
import "./WorkShowPage.css";
import WritingComponent from "./WritingComponent";

function WorkShowPage(){
    return(
        <div id="workShowContainer">
            <div id="writingContainer">
               <WritingComponent /> 
            </div>
            
            
            <div id="commentsContainer">
                <div id="commentTitle-value">
                    <h2 id="titleStudents" >Comentarios de los Usuarios</h2>
                    <p id="valoracionStudents">100</p>
                </div>
                <div id="commentsPlacement">
                    <ComentarioComponent />
                    <ComentarioComponent />
                    <ComentarioComponent />
                </div>
               
            </div>

        </div>
        
    );
}
export default WorkShowPage;