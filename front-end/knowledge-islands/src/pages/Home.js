import { Button, Col, Row, Form, Card, Alert } from "react-bootstrap";
import GitRepositoryVersionProcess from "../components/GitRepositoryVersionProcess";
import AuthContext from "../components/shared/AuthContext";
import { useContext, useState, useEffect } from "react";
import axios from "axios";
import { useRef } from "react";
import SpinnerLoading from "../components/shared/SpinnerLoading";

const Home = () => {
    const [buttonDisable, setButtonDisable] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [gitRepositoryVersionProcessState, setGitRepositoryVersionProcessState] = useState(0);
    const [urlValid, setUrlValid] = useState(false);
    const { user } = useContext(AuthContext);
    const url = useRef("");
    const branch = useRef("");
    const changeUrl = (event) => {
        const urlRegEx = new RegExp('^(https:\/\/)?(www\.)?(github\.com|gitlab\.com|bitbucket\.org)\/([a-zA-Z0-9-_]+\/[a-zA-Z0-9-_]+)(\.git)?$');
        setUrlValid(urlRegEx.test(event.target.value));
    };

    useEffect(() => {
        if (success) {
            const timer = setTimeout(() => {
                setSuccess("");
            }, 5000); // Change 5000 to the number of milliseconds you want the alert to be displayed

            return () => clearTimeout(timer);
        }
    }, [success]);

    const createGitRepositoryVersionProcess = async (event) => {
        event.preventDefault();
        setLoading(true);
        setButtonDisable(true);
        let form = {
            cloneUrl: url.current.value,
            idUser: user.id,
            branch: branch.current.value
        };
        setGitRepositoryVersionProcessState(gitRepositoryVersionProcessState + 1);
        try {
            await axios.post(`${process.env.REACT_APP_API_URL}/git-repository-version-process/start-git-repository-version-process`, form);
            setSuccess("Repository analysis process started");
            setGitRepositoryVersionProcessState(gitRepositoryVersionProcessState + 1);
        } catch (error) {
            setError(error.response.data.message);
        } finally {
            setLoading(false);
            setButtonDisable(false);
            url.current.value = "";
            branch.current.value = "";
            setUrlValid(false);
        }
    };

    return (
        <>
            <br />
            <br />
            {error ? <Alert variant="danger">{error}</Alert> : null}
            {success ? <Alert variant="success">{success}</Alert> : null}
            <Card >
                <Card.Body>
                    <Card.Title>Start repository analysis</Card.Title>
                    <br/>
                    <Row>
                        <Col className="col-md-7 offset-md-2">
                            {loading && (
                                <SpinnerLoading />
                            )}
                            <Form onSubmit={createGitRepositoryVersionProcess}>
                                <Form.Group className="mb-3" controlId="formBasicsUrl">
                                    <Form.Label>GitHub URL</Form.Label>
                                    <Form.Control ref={url} type="text" required onChange={changeUrl} />
                                </Form.Group>
                                <Form.Group className="mb-3" controlId="formBasicsBranch">
                                    <Form.Label>Branch</Form.Label>
                                    <Form.Control ref={branch} type="text" />
                                </Form.Group>
                                <div className="d-grid">
                                    <Button variant="primary" type="submit" disabled={!urlValid || buttonDisable}>
                                        Start Analysis
                                    </Button>
                                </div>
                            </Form>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>
            <GitRepositoryVersionProcess key={gitRepositoryVersionProcessState} />
        </>
    );
};

export default Home;