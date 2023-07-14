import {Button, Col, Row, Form, Card, Alert} from "react-bootstrap";
import TruckFactorProcess from "../components/TruckFactorProcess";
import AuthContext from "../components/shared/AuthContext";
import { useContext, useState } from "react";
import axios from "axios";
import { useRef } from "react";

const Home = () => {
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [truckFactorProcessState, setTruckFactorProcessState] = useState(0);
    const [urlValid, setUrlValid] = useState(false);
    const { user } = useContext(AuthContext);
    const url = useRef("");

    const changeUrl = (event) => {
        const urlRegEx = new RegExp('^(https:\/\/)?(www\.)?(github\.com|gitlab\.com|bitbucket\.org)\/([a-zA-Z0-9-_]+\/[a-zA-Z0-9-_]+)(\.git)?$');
        setUrlValid(urlRegEx.test(event.target.value));
    };
    const branch = useRef("");
    const createTruckFactorProcess = async(event) => {
        event.preventDefault();
        let form = {
            url: url.current.value,
            idUser: user.id,
            branch: branch.current.value
        };    
        setTruckFactorProcessState(truckFactorProcessState+1);
        try{
            await axios.post("http://localhost:8080/api/truck-factor/clone-and-truck-factor", form)
            .then(response => {
                setSuccess("Truck Factor analyzes initiated");
                setTruckFactorProcessState(truckFactorProcessState+1);
            });    
        }catch(error){
            setError(error.response.data.message);
        }
    }
    return (
        <>
            <br/>
            <br/>
            {error?<Alert variant="danger">{error}</Alert>:null}
            {success?<Alert variant="success">{success}</Alert>:null}
            <Card >
                <br/>
                <Row>
                    <Col className="col-md-8 offset-md-2">
                        <Form>
                        <Form.Group className="mb-3" controlId="formBasicsUrl">
                            <Form.Label>Url Repository</Form.Label>
                            <Form.Control ref={url} type="text" required onChange={changeUrl}/>
                        </Form.Group>
                        <Form.Group className="mb-3" controlId="formBasicsBranch">
                            <Form.Label>Branch</Form.Label>
                            <Form.Control ref={branch} type="text"/>
                        </Form.Group>
                        <div className="d-grid">
                        <Button variant="primary" type="submit" disabled={!urlValid}
                        onClick={createTruckFactorProcess}>
                            Analyze
                        </Button>
                        </div>
                        </Form>
                    </Col>
                </Row>
                <br/>
            </Card>
            <TruckFactorProcess key={truckFactorProcessState}/>
        </>
    );
};

export default Home;