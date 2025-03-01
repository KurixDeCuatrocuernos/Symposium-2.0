import "./SignInMessageComponent.css";
import React from 'react';

const SignInMessageComponent = ({ onClose }) => {

    const logInRedir = () =>{
        window.location.href="/login";
    };

    return (
        <div className="overlay">
            <div className="modal">
                <h2>You need to log in!</h2>
                <button onClick={logInRedir}>Log In</button>
                <button onClick={onClose}>Cancel</button>
                <br/>
                <a href="/Register">Don't have account, register now here!</a>
            </div>
        </div>
    );
};

export default SignInMessageComponent;