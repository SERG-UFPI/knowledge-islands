import { Card, Row, Col, Accordion, Table } from "react-bootstrap";
import { useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "axios";

const TruckFactor = () => {
    const location = useLocation();
    const [truckFactor, setTruckFactor] = useState(null);
    useEffect(() => {
        let id = location?.state?.id;
        axios.get(`http://localhost:8080/api/truck-factor/${id}`, {withCredentials: true})
        .then(response => {
            setTruckFactor(response.data);
        });
    }, []);
    return (
        <>
        <br/>
        <div className={"text-center"}>
            <h3>Truck Factor analyzes of {truckFactor?.projectVersion.project.fullName}</h3>
        </div>
        <br/>
        <Card>
            <br/>
            <div className={"text-center"}>
                <Row>
                    <Col>Truck Factor: {truckFactor?.truckfactor}</Col>
                    <Col>Version date: {truckFactor?.projectVersion.dateVersion}</Col>
                    <Col>Version id: {truckFactor?.projectVersion.versionId}</Col>
                </Row>
                <br/>
                <Row>
                    <Col>Number of analyzed devs: {truckFactor?.projectVersion.numberAnalysedDevs}</Col>
                    <Col>Number of core devs: {truckFactor?.projectVersion.numberAuthors}</Col>
                    <Col>Number of analyzed files: {truckFactor?.projectVersion.numberAnalysedFiles}</Col>
                    <Col>Number of analyzed commits: {truckFactor?.projectVersion.numberAnalysedCommits}</Col>
                </Row>
            </div>
            <br/>
        </Card>
        <br/>
        <Accordion>
                <Accordion.Item eventKey="0">
                    <Accordion.Header>
                        Truck Factor Developers
                    </Accordion.Header>
                    <Accordion.Body>
                        <Table>
                            <thead>
                                <tr style={{textAlign:"center"}}>
                                    <th>Name</th>
                                    <th>Email</th>
                                    <th>Percentage of files authored</th>
                                </tr>
                            </thead>
                            <tbody>
                                {truckFactor?.truckFactorDevelopers.map((developer, id) => (
                                    <tr style={{textAlign:"center"}}>
                                        <td>{developer.name}</td>
                                        <td>{developer.email}</td>
                                        <td>{developer.percentOfFilesAuthored}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    </Accordion.Body>
                </Accordion.Item>
                <Accordion.Item>
                    <Accordion.Header>
                        Abondoned Files
                    </Accordion.Header>
                    <Accordion.Body>
                        <Table>
                            <thead>
                                <tr style={{textAlign:"center"}}>
                                    <th>File</th>
                                </tr>
                            </thead>
                            <tbody>
                                {truckFactor?.implicatedFiles.map((file, id) => (
                                    <tr style={{textAlign:"center"}}>
                                        <td>{file}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    </Accordion.Body>
                </Accordion.Item>
            </Accordion>
    </>    
    );
};

export default TruckFactor;