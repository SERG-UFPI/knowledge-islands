import axios from "axios";
import { useEffect, useState } from "react";
import { useContext } from "react";
import AuthContext from "./shared/AuthContext";
import { Table, Button } from "react-bootstrap";
import { EyeFill } from "react-bootstrap-icons";

const TruckFactorProcess = () => {
    const { user } = useContext(AuthContext);
    const [processes, setProcesses] = useState([]);
    useEffect(() => {
        axios.get(`http://localhost:8080/api/truck-factor-process/${user.id}`, { withCredentials: true})
            .then((response) => {
                setProcesses(response.data);
            });
        const interval = setInterval(() => {
            axios.get(`http://localhost:8080/api/truck-factor-process/${user.id}`, { withCredentials: true})
            .then((response) => {
                setProcesses(response.data);
            });
        }, 10000)
        return () => clearInterval(interval);
    }, []);
    return (
        <>
        <br/>
        <br/>
            <Table>
                <thead>
                        <tr>
                            <th>Repository URL</th>
                            <th>Start Date</th>
                            <th>Stage</th>
                            <th>Details</th>
                        </tr>
                        {processes.map((process, id) => (
                            <tr>
                                <th>{process.repositoryUrl}</th>
                                <th>{process.startDate}</th>
                                <th>{process.stage}</th>
                                <th>
                                    <Button disabled={process.stage==='Analysis finished'?false:true}>
                                        <EyeFill/>
                                    </Button>
                                </th>
                            </tr>
                        ))}
                </thead>            
            </Table>
        </>
    );
};

export default TruckFactorProcess;