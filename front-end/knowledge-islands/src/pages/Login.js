import { useRef } from "react";
import { Button, Col, Container, Row, Form } from "react-bootstrap";

const Login = () => {
    const email = useRef("");
    const password = useRef("");

    const loginSubmit = async () => {};

    return (
        <>
            <Container>
                <Row>
                    <Col className="col-md-8 offset-md-2">
                        <legend>Login Form</legend>
                        <Form.Group className="mb-3" controlId="formBasicsEmail">
                            
                        </Form.Group>
                    </Col>
                </Row>
            </Container>
        </>
    );
}