import {Card} from "react-bootstrap";
import TruckFactorProcess from "../components/TruckFactorProcess";
import CreateTruckFactorProcess from "../components/CreateTruckFactorProcess";

const Home = () => {
    return (
        <>
            <CreateTruckFactorProcess/>
            <TruckFactorProcess/>
        </>
    );
};

export default Home;