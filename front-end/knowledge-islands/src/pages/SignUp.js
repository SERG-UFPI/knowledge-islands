import { useRef } from "react";
import { Container, Button, Col, Row, Form, Alert, Card } from "react-bootstrap";
import axios from "axios";
import { useState } from "react";
import SpinnerLoading from "../components/shared/SpinnerLoading";

const SignUp = () => {
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
            {loginError ? <Alert variant="danger" >{error}</Alert> :
                null}
            <br />
            <div align="center">
                <b><h3>Sign up for Knowledge Islands</h3></b>
            </div>
            <br />
            <Container>
                {signUpSuccess === false ? <Row>
                    <Col className="col-md-7 offset-md-2">
                    {loading && (
                            <SpinnerLoading />
                        )}
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
                </Row> :
                    <Card>
                        <div style={{ textAlign: "center" }}>
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