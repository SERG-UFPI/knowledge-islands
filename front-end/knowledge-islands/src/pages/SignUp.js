import { useRef } from "react";
import { Container, Button, Col, Row, Form, Alert, Card } from "react-bootstrap";
import axios from "axios";
import { useState } from "react";
import { useNavigate } from "react-router-dom"; // Import useNavigate
import SpinnerLoading from "../components/shared/SpinnerLoading";

const SignUp = () => {
    const navigate = useNavigate(); // Initialize useNavigate
    const [buttonDisable, setButtonDisable] = useState(false);
    const [loading, setLoading] = useState(false);
    const [loginError, setLoginError] = useState(false);
    const [error, setError] = useState("");
    const username = useRef("");
    const email = useRef("");
    const name = useRef("");
    const password = useRef("");
    const [signUpSuccess, setSignUpSuccess] = useState(false);

    const signup = async (event) => {
        event.preventDefault();
        setLoading(true);
        setButtonDisable(true);

        // Validate username for spaces
        if (/\s/.test(username.current.value)) {
            setError("Username cannot contain spaces.");
            setLoginError(true);
            setLoading(false);
            setButtonDisable(false);
            return;
        }

        let signUpRequest = {
            name: name.current.value,
            username: username.current.value,
            email: email.current.value,
            password: password.current.value
        };

        try {
            await axios.post(`${process.env.REACT_APP_API_URL}/auth/signup`, signUpRequest)
                .then(response => {
                    setSignUpSuccess(true);
                    setLoginError(false);
                    navigate("/signup-success"); // Redirect to success page
                });
        } catch (error) {
            setLoginError(true);
            setError(error.response.data.message);
        } finally {
            setLoading(false);
            setButtonDisable(false);
        }
    };

    return (
        <>
            {loginError ? <Alert variant="danger" dismissible>{error}</Alert> : null}
            <br />
            <Card>
                <Card.Body>
                    <div align="center">
                        <b><h3>Sign up for Knowledge Islands</h3></b>
                    </div>
                    <br />
                    <Container>
                        {!signUpSuccess ? (
                            <Row>
                                <Col className="col-md-7 offset-md-2">
                                    {loading && <SpinnerLoading />}
                                    <Form onSubmit={signup}>
                                        <Form.Group className="mb-3" controlId="formBasicsName">
                                            <Form.Label><b>Name</b></Form.Label>
                                            <Form.Control ref={name} type="text" required />
                                        </Form.Group>
                                        <Form.Group className="mb-3" controlId="formBasicsUsername">
                                            <Form.Label><b>Username</b></Form.Label>
                                            <Form.Control ref={username} type="text" required />
                                        </Form.Group>
                                        <Form.Group className="mb-3" controlId="formBasicsEmail">
                                            <Form.Label><b>Email</b></Form.Label>
                                            <Form.Control ref={email} type="email" required />
                                        </Form.Group>
                                        <Form.Group className="mb-3" controlId="formBasicsPassword">
                                            <Form.Label><b>Password</b></Form.Label>
                                            <Form.Control ref={password} type="password" required />
                                        </Form.Group>
                                        <div className="d-grid">
                                            <Button variant="primary" type="submit" disabled={buttonDisable}>
                                                Sign up
                                            </Button>
                                        </div>
                                    </Form>
                                </Col>
                            </Row>
                        ) : null}
                    </Container>
                </Card.Body>
            </Card>
        </>
    );
};

export default SignUp;