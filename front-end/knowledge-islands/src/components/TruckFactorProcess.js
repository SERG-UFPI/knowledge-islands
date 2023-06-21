import axios from "axios";
import { useEffect, useState } from "react";
import { useContext } from "react";
import AuthContext from "./shared/AuthContext"

const TruckFactorProcess = () => {
    const { user } = useContext(AuthContext);
    const [processes, setProcesses] = useState([]);
    useEffect(() => {
        axios.get(`http://localhost:8080/api/truck-factor-process/${user.id}`, { withCredentials: true})
        .then((response) => {
            setProcesses(response.data);
        });
    }, []);
    return (
        <div>{processes}</div>
    );
};

export default TruckFactorProcess;