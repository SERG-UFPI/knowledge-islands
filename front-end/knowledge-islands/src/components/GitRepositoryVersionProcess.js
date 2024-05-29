import axios from "axios";
import { useEffect, useState } from "react";
import { useContext } from "react";
import AuthContext from "./shared/AuthContext";
import { Table, Button, Card } from "react-bootstrap";
import { EyeFill } from "react-bootstrap-icons";
import { useNavigate } from "react-router-dom";


const GitRepositoryVersionProcess = () => {
    const { user } = useContext(AuthContext);
    const [processes, setProcesses] = useState([]);
    const navigate = useNavigate();
    const url = `http://localhost:8080/api/git-repository-version-process/user/${user.id}`;
    useEffect(() => {
        axios.get(url, { withCredentials: true })
            .then((response) => {
                setProcesses(response.data);
            });
        const interval = setInterval(() => {
            axios.get(url, { withCredentials: true })
                .then((response) => {
                    setProcesses(response.data);
                });
        }, 3000)
        return () => clearInterval(interval);
    }, []);

    const detailsGitRepositoryVersion = event => {
        navigate("/git-repository-version", { state: { id: event } });
    };
    return (
        <>
            <br />
            <br />
            {processes.length > 0 && (
                <Card>
                    <Card.Body>
                        <Card.Title>Repositories</Card.Title>
                        <br />
                        <Table striped bordered hover >
                            <thead>
                                <tr style={{ textAlign: "center" }}>
                                    <th>GitHub Repository URL</th>
                                    <th>Start Date</th>
                                    <th>Stage</th>
                                    <th>Details</th>
                                </tr>
                            </thead>
                            <tbody>
                                {processes.map((process, id) => (
                                    <tr key={process.idGitRepositoryVersion} style={{ textAlign: "center" }}>
                                        <td>{process.repositoryUrl}</td>
                                        <td>{process.startDate}</td>
                                        <td>{process.stage}</td>
                                        <td>
                                            <Button disabled={process.stage === 'Extraction finished' ? false : true}
                                                onClick={() => detailsGitRepositoryVersion(process.idGitRepositoryVersion)} type="button">
                                                <EyeFill />
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table></Card.Body></Card>
            )}
        </>
    );
};

export default GitRepositoryVersionProcess;