import React from "react";
import {Card, Container} from "react-bootstrap";

const SignupSuccess = () => {
    return (
        <Container>
            <br/>
            <Card>
                <Card.Body>
                    <div style={{ textAlign: "center" }}>
                        <h2>You have signed up successfully!</h2>
                        <h4>Please check your email to verify your account</h4>
                        <h3><a href="/login">Click here to Login</a></h3>
                    </div>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default SignupSuccess;