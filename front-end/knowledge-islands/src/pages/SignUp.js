import { useRef } from "react";
import { Container, Button, Col, Row, Form, Alert, Card } from "react-bootstrap";
import axios from "axios";
import { useState } from "react";

const SignUp = () =>{
    const [loginError, setLoginError] = useState(false);
    const [error, setError] = useState(""); 
    const username = useRef("");
    const email = useRef("");
    const name = useRef("");
    const password = useRef("");
    const [signUpSuccess, setSignUpSuccess] = useState(false);

    const signup = async(event) => {
        event.preventDefault();
        let signUpRequest = {
            name: name.current.value,
            username: username.current.value,
            email: email.current.value,
            password: password.current.value
        };
        try{
            await axios.post("http://localhost:8080/api/auth/signup", signUpRequest)
            .then(response => {
                setSignUpSuccess(true);
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
                {signUpSuccess === false ? <Row>
                        <Col className="col-md-8 offset-md-2">
                            <Form>
                            <Form.Group className="mb-3" controlId ="formBasicsName">
                                <Form.Label>Name</Form.Label>
                                <Form.Control ref={name} type="text" required/>
                            </Form.Group>
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
                                Sign up
                            </Button>
                            </div>
                            </Form>
                        </Col>
                    </Row>:
                    <Card>
                        <div style={{textAlign: "center"}}>
                            <h2>You have signed up successfully!</h2>
                            <h4>Please check your email to verify your account</h4>
                            <h3><a href="/login">Click here to Login</a></h3>
                        </div>
                    </Card>
                    }
                
            </Container>
        </>
    );

};

export default SignUp;