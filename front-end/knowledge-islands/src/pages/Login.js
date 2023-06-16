import { useRef, useContext } from "react";
import { Button, Col, Container, Row, Form } from "react-bootstrap";
import AuthContext from "../components/shared/AuthContext";

const Login = () => {
    const username = useRef("");
    const password = useRef("");
    const { login } = useContext(AuthContext);

    const loginSubmit = async () => {
        let payload = {
            username: username.current.value,
            password: password.current.value
        };
        await login(payload);
    };

    return (
        <>
            <br/>
            <br/>
            <Container >
                <Row>
                    <Col className="col-md-8 offset-md-2">
                        <Form.Group className="mb-3" controlId="formBasicsUsername">
                            <Form.Label>Username</Form.Label>
                            <Form.Control ref={username}/>
                        </Form.Group>
                        <Form.Group className="mb-3" controlId="formBasicsPassword">
                            <Form.Label>Password</Form.Label>
                            <Form.Control ref={password} type="password"/>
                        </Form.Group>
                        <div className="d-grid">
                        <Button variant="primary" type="button" onClick={loginSubmit}>
                            Sign in
                        </Button>
                        </div>
                    </Col>
                </Row>
                <br/>
            </Container>
        </>
    );
};

export default Login;