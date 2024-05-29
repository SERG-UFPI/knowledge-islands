import { useRef, useContext } from "react";
import { Button, Col, Container, Row, Form, Alert, Nav } from "react-bootstrap";
import AuthContext from "../components/shared/AuthContext";
import { useLocation } from "react-router-dom";
import { useState } from "react";
import SpinnerLoading from "../components/shared/SpinnerLoading";

const Login = () => {
    const [buttonDisable, setButtonDisable] = useState(false);
    const [loading, setLoading] = useState(false);
    const location = useLocation();
    const username = useRef("");
    const password = useRef("");
    const { login } = useContext(AuthContext);

    const loginSubmit = async (event) => {
        event.preventDefault();
        setLoading(true);
        setButtonDisable(true);
        let payload = {
            username: username.current.value,
            password: password.current.value
        };
        await login(payload);
    };

    return (
        <>
            <br />
            <br />
            <Container >
                {location?.state?.message ? <Alert variant="success" >{location.state.message}</Alert> :
                    null}
                <Row>
                    <Col className="col-md-6 offset-md-2">
                        {loading && (
                            <SpinnerLoading />
                        )}
                        <Form onSubmit={loginSubmit}>
                            <Form.Group className="mb-3" controlId="formBasicsUsername">
                                <Form.Label>Username</Form.Label>
                                <Form.Control ref={username} required />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicsPassword">
                                <Form.Label>Password</Form.Label>
                                <Form.Control ref={password} type="password" required />
                            </Form.Group>
                            <div className="d-grid">
                                <Button variant="primary" type="submit" disabled={buttonDisable}>
                                    Sign in
                                </Button>
                                <Nav>
                                    <Nav.Link href="/signup">Not registered? Create an account</Nav.Link></Nav>
                            </div>
                        </Form>
                    </Col>
                </Row>
            </Container>
        </>
    );
};

export default Login;