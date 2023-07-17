import axios from "axios";
import { useEffect, useState } from "react";
import {Container, Card} from "react-bootstrap";
import { useSearchParams } from "react-router-dom";

const VerifyRegistration = () => {
    const [queryParameters] = useSearchParams();
    const [verifyResponse, setVerifyResponse] = useState(true);
    useEffect(() => {
        let code = queryParameters.get("code");
            axios.get(`http://localhost:8080/api/user/verify`, {params: {
                code: code
            }}).then((response) => {
                setVerifyResponse(response.data);
            });
    }, []);
    return (
        <Container>
            <br/>
                {verifyResponse === true ? <Card>
                        <div style={{textAlign: "center"}}>
                            <h2>Congratulations, your account has been verified!</h2>
                            <h3><a href="/login">Click here to Login</a></h3>
                        </div>
                    </Card>:
                    <Card>
                    <div style={{textAlign: "center"}}>
                        <h2>Error on verifying account!</h2>
                    </div>
                </Card>}
        </Container>
    );
};

export default VerifyRegistration;