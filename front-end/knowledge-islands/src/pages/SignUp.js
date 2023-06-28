import { useRef } from "react";
import { Container, Button, Col, Row, Form, Alert } from "react-bootstrap";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useState } from "react";

const SignUp = () =>{
    const [loginError, setLoginError] = useState(false);
    const [error, setError] = useState(""); 
    const username = useRef("");
    const email = useRef("");
    const password = useRef("");

    const navigate = useNavigate();

    const signup = async(event) => {
        event.preventDefault();
        let signUpRequest = {
            username: username.current.value,
            email: email.current.value,
            password: password.current.value
        };
        try{
            let apiResponse = await axios.post("http://localhost:8080/api/auth/signup", signUpRequest)
            .then(response => {
                navigate("/login", {state:{message: response.data.message}});    
            });
        }catch (error){
            setLoginError(true);
            setError(error.response.data.message);
        }
    };

    return (
        <>
            {loginError?<Alert variant="danger" >{error}</Alert>:
        null}
            <br/>
            <br/>
            <Container>
                <Row>
                        <Col className="col-md-8 offset-md-2">
                            <Form>
                            <Form.Group className="mb-3" controlId="formBasicsUsername">
                                <Form.Label>Username</Form.Label>
                                <Form.Control ref={username} type="text" required/>
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicsEmail">
                                <Form.Label>Email</Form.Label>
                                <Form.Control ref={email} type="email" required/>
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicsPassword">
                                <Form.Label>Password</Form.Label>
                                <Form.Control ref={password} type="password" required/>
                            </Form.Group>
                            <div className="d-grid">
                            <Button variant="primary" type="submit" onClick={signup}>
                                Sign in
                            </Button>
                            </div>
                            </Form>
                        </Col>
                    </Row>
            </Container>
        </>
    );

};

export default SignUp;