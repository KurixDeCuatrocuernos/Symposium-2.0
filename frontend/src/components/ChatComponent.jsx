import "./ChatComponent.css";
import { useState, useEffect } from "react";
import MessageComponent from "./MessageComponent";

function ChatComponent (usr) {

   const [show, setShow] = useState(false);
   const [number, setNumber] = useState(0);
   const [inputText, setInputText] = useState("");
   const [messages, setMessages] = useState([]);

   const handleInput = (event) => {
      setInputText(event.target.value);
   };

   const handleNumber = () => {
      setNumber(number+1);
   };

   const handleShow = () => {
      let cell = confirm("¿Are you sure you want to contact with an Admin?");
      if (cell==true){
         setShow(true);
         setNumber(number+1);
      } else {
         setShow(false);
      }

   };

   const handleClose = () => {
      let cell = confirm("¿Are you sure you want to abandon the chat, none admin will talk with you if you leaves?");
      if (cell==true){
         setNumber(number-1);
         setShow(false);
      }
   };

   return (
      <>
      {show===false ? 
         <div id="overlay-ChatComp">
            <div id="container-ChatComp" onClick={handleShow}>
               {number > 0 ? 
                  <div id="numberContainer-ChatComp">
                     <p>{number}</p>
                  </div>
               :
                  ''
               }
               
               <img src="chat.png"></img>
            </div>
         </div>
      :
         <div id="chatOverlay-ChatComp">
            <div id="chatContainer-ChatComp">
               <div id="closeChat-ChatComp" onClick={handleClose}>
                  <p>X</p>
               </div>
               <div id="showMessage-ChatComp">
                  {messages.map((message)=>{
                     <MessageComponent
                        text={message.text}
                        username={usr}
                     />
                  })};
               </div>
               <div id="inputContainer-ChatComp">
                  <h2>Write:</h2>
                  <input type="text" onChange={handleInput}></input>
                  <img src="enviar-mensaje.png"></img>
               </div>
            </div>
         </div>
      }
      </>
   );
}
export default ChatComponent;