import axios from "axios";
import { useEffect, useState, useRef } from "react";
import { useContext } from "react";
import { Button, Col, Container, Row, Form, Card } from "react-bootstrap";
import AuthContext from "./shared/AuthContext"

const CreateTruckFactorProcess = () => {
    const { user } = useContext(AuthContext);
    const { url } = useRef(null);
    const { branch } = useRef(null);
    return (
        <>
                <br/>
            <br/>
            <Card >
                <br/>
                <Row>
                    <Col className="col-md-8 offset-md-2">
                        <Form.Group className="mb-3" controlId="formBasicsUsername">
                            <Form.Label>Url Repository</Form.Label>
                            <Form.Control ref={url} type="text" required/>
                        </Form.Group>
                        <Form.Group className="mb-3" controlId="formBasicsPassword">
                            <Form.Label>Branch</Form.Label>
                            <Form.Control ref={branch} type="text"/>
                        </Form.Group>
                        <div className="d-grid">
                        <Button variant="primary" type="button">
                            Analyze
                        </Button>
                        </div>
                    </Col>
                </Row>
                <br/>
            </Card>
        </>
    );
};

export default CreateTruckFactorProcess;