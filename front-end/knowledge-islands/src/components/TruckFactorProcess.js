import axios from "axios";
import { useEffect, useState } from "react";
import { useContext } from "react";
import AuthContext from "./shared/AuthContext";
import { Table, Button } from "react-bootstrap";
import { EyeFill } from "react-bootstrap-icons";
import { useNavigate } from "react-router-dom";

const TruckFactorProcess = () => {
    const { user } = useContext(AuthContext);
    const [processes, setProcesses] = useState([]);
    const navigate = useNavigate();
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
        }, 5000)
        return () => clearInterval(interval);
    }, []);

    const detailsTruckFactor = event => {
        navigate("/truck-factor", {state: {id: event}});
    };
    return (
        <>
        <br/>
        <br/>
            <Table>
                <thead>
                        <tr style={{textAlign:"center"}}>
                            <th>Repository URL</th>
                            <th>Start Date</th>
                            <th>Stage</th>
                            <th>Details</th>
                        </tr>
                </thead>
                <tbody>
                        {processes.map((process, id) => (
                            <tr style={{textAlign:"center"}}>
                                <td>{process.repositoryUrl}</td>
                                <td>{process.startDate}</td>
                                <td>{process.stage}</td>
                                <td>
                                    <Button disabled={process.stage==='Analysis finished'?false:true}
                                    onClick={()=>detailsTruckFactor(process.truckFactor.id)} type="button">
                                        <EyeFill/>
                                    </Button>
                                </td>
                            </tr>
                        ))}
                </tbody>
            </Table>
        </>
    );
};

export default TruckFactorProcess;