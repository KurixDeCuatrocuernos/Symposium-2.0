import "./ChatComponent.css";
import { useState, useEffect } from "react";

function ChatComponent({ usr }) {
   const [show, setShow] = useState(false);
   const [messages, setMessages] = useState([]);
   const [inputText, setInputText] = useState("");
   const [socket, setSocket] = useState(null);
   const [isNameSet, setIsNameSet] = useState(false);  // Para asegurarnos de que el nombre no se envíe más de una vez

   useEffect(() => {
      if (show) {
         // Conexión a WebSocket del servidor Spring
         const ws = new WebSocket("ws://localhost:9000/chat");

         // Configuración de los manejadores de eventos para WebSocket
         ws.onopen = () => {
            console.log("Conectado al servidor WebSocket");
         };

         ws.onmessage = (event) => {
            const newMessage = event.data;
            setMessages((prevMessages) => [...prevMessages, { text: newMessage }]);
         };

         ws.onclose = () => {
            console.log("Conexión cerrada con el servidor WebSocket");
         };

         ws.onerror = (error) => {
            if (error instanceof Event) {
                console.error("Error en la conexión WebSocket:", error.type); // Muestra el tipo de evento de error
            } else {
                console.error("Error en la conexión WebSocket:", error);
            }
         };

         // Guardar la referencia al WebSocket en el estado
         setSocket(ws);

         // Limpiar la conexión cuando el componente se desmonte o cuando se cierre el chat
         return () => {
            if (ws) {
               ws.close();
            }
         };
      }

      // Si show es false, no hacer nada
      return () => {};
   }, [show]); // Este efecto solo se ejecutará si `show` cambia a true

   // Este useEffect se encarga de enviar el nombre solo después de que la conexión esté lista
   useEffect(() => {
      if (show && socket && !isNameSet && usr) {
         if (socket.readyState === WebSocket.OPEN) {
            socket.send(usr);  // Enviar el nombre al servidor
            setIsNameSet(true);  // Marcar que el nombre ya ha sido configurado
            console.log(`Nombre enviado al servidor: ${usr}`);
         } else {
            // Si el WebSocket no está en estado OPEN, esperar hasta que se abra
            socket.onopen = () => {
               socket.send(usr);  // Enviar el nombre al servidor cuando la conexión esté abierta
               setIsNameSet(true);  // Marcar que el nombre ya ha sido configurado
               console.log(`Nombre enviado al servidor: ${usr}`);
            };
         }
      }
   }, [show, socket, isNameSet, usr]);  // Esto solo se ejecuta si show es true, socket está definido y el nombre aún no se ha enviado

   const handleInput = (event) => {
     setInputText(event.target.value);
   };

   const handleShow = () => {
     let cell = confirm("¿Are you sure you want to contact with an Admin?");
     if (cell === true) {
       setShow(true);
     } else {
       setShow(false);
     }
   };

   const handleClose = () => {
     let cell = confirm("¿Are you sure you want to abandon the chat?");
     if (cell === true) {
       setShow(false);
     }
   };

   const handleSendMessage = () => {
     if (inputText.trim() && socket) {
       socket.send(inputText);  // Enviar el mensaje al servidor WebSocket
       setMessages((prevMessages) => [...prevMessages, { text: inputText }]);  // Agregar el mensaje localmente
       setInputText("");  // Limpiar el campo de entrada
     }
   };

   return (
     <>
       {show === false ? (
         <div id="overlay-ChatComp">
           <div id="container-ChatComp" onClick={handleShow}>
             <img src="chat-img.png" alt="Chat imagen" />
           </div>
         </div>
       ) : (
         <div id="chatOverlay-ChatComp">
           <div id="chatContainer-ChatComp">
             <div id="closeChat-ChatComp" onClick={handleClose}>
               <p>X</p>
             </div>
             <div id="showMessage-ChatComp">
               {messages.map((message, index) => (
                 <div key={index} className="message">
                   <img src="usuario.png" alt="Usuario" />
                   <em>{message.text}</em>
                 </div>
               ))}
             </div>
             <div id="inputContainer-ChatComp">
               <h2>Write:</h2>
               <input
                 type="text"
                 value={inputText}
                 onChange={handleInput}
                 onKeyDown={(e) => e.key === "Enter" && handleSendMessage()}
               />
               <img
                 src="enviar-mensaje.png"
                 alt="Enviar mensaje"
                 onClick={handleSendMessage}
               />
             </div>
           </div>
         </div>
       )}
     </>
   );
}
export default ChatComponent;